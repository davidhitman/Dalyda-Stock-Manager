package com.example.stockmanager.services.Impl;

import com.example.stockmanager.dtos.PageDto;
import com.example.stockmanager.dtos.UserDto;
import com.example.stockmanager.entities.Role;
import com.example.stockmanager.entities.Users;
import com.example.stockmanager.exceptions.AdminUserExistsException;
import com.example.stockmanager.exceptions.ForbiddenActionException;
import com.example.stockmanager.mappers.UserMapper;
import com.example.stockmanager.repositories.UserRepository;
import com.example.stockmanager.responses.AuthenticatedUser;
import com.example.stockmanager.services.UserServices;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserServices {

    private final AuthenticationManager authenticationManager;
    private final JwtServiceImpl jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSenderImpl mailSender;
    private final PageServiceImpl pageService;

    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 10);
    }

    private void sendWelcomeEmail(String to, String password, String firstName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Welcome to DALYDA");
        message.setText("Hello," + " "+firstName +
                "\n\nWelcome to DALYDA, Your temporary password is: " + password +
                "\n\nPlease log in and change it to your own password.\n\nThank you.");

        mailSender.send(message);
    }

    @Override
    public String logIn(UserDto.LoginDto loginDto) {
       authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
        var user = userRepository.findByEmail(loginDto.getEmail()).orElseThrow();
        return jwtService.generateToken(user);
    }

    @Override
    public String passwordReset(String email) {

        if (!userRepository.existsByEmail(email)) throw new ResourceNotFoundException("Email does not exist");

        var user = userRepository.findByEmail(email).orElseThrow();
        String password = generateRandomPassword();

        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset Password");
        message.setText("Hello," +
                "\n\n Your new Password is:" +" "+ password +
                "\n\nPlease log in and update your password.\n\nThank you.");

        mailSender.send(message);
        return email;
    }

    @Override
    public UserDto.ViewUserDto signup(UserDto.SignupDto signupDto, Role role) {
        var user = UserMapper.map(signupDto);

        if (userRepository.existsByEmail(user.getEmail())) throw new DuplicateKeyException("Email is already in use");

        String rawPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        user.setRole(role);
        userRepository.save(user);

        sendWelcomeEmail(user.getEmail(), rawPassword, user.getFirstname());
        return UserMapper.map(user);
    }

    @Override
    public void passwordChange(UserDto.ChangePasswordDto changePasswordDto) {
        var user = AuthenticatedUser.getAuthenticatedUser();
        if (user == null) throw new ResourceNotFoundException("User not found, Please LogIn to Change your password");

        if (!passwordEncoder.matches(changePasswordDto.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Old password is incorrect");
        }

        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getConfirmPassword())) {
            throw new BadCredentialsException("Confirm password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID id) {
        var users = userRepository.findUserById(id);
        if (users.isEmpty()) throw new ResourceNotFoundException("User not found");
        var email = users.get().getEmail();
        if (email.equals(AuthenticatedUser.getAuthenticatedUserEmail())) throw new ForbiddenActionException("You cannot delete a signed in user");
        userRepository.deleteById(id);
    }

    @Override
    public UserDto.ViewUserDto updateUser(UUID id, UserDto.UpdateUserDto updateUserDto) {
        if (!userRepository.existsById(id)) throw new ResourceNotFoundException("User not found");
        if (userRepository.existsByEmail(updateUserDto.getEmail())
                && !updateUserDto.getEmail().equals(AuthenticatedUser.getAuthenticatedUserEmail()))
            throw new DuplicateKeyException("Email is already in use");
        var user = userRepository.findUserById(id).orElseThrow();
        user.updateUser(updateUserDto);
        userRepository.save(user);
        return UserMapper.map(user);
    }

    @Override
    public Page<UserDto.ViewUserDto> viewAllUsers(PageDto pageDto) {
        var pageable = pageService.getPageable(pageDto);
        var users = userRepository.findAllUsers(pageable);
        if (users.isEmpty()) throw new ResourceNotFoundException("No Registered Users Yet");
        return users.map(UserMapper::map);
    }

    @Override
    public UserDto.ViewDefaultAdminUserDto defaultAdminUser() {

        if (userRepository.existsByRole(Role.ADMIN)) {
            throw new AdminUserExistsException(
                "There is already an admin user in the system. " +
                "If you forgot your password, please use the password reset functionality to change it."
            );
        }
        

        String defaultEmail = "admin@stockmanager.com";
        String defaultPassword = "Admin@123";
        String defaultFirstName = "Admin";
        String defaultLastName = "User";
        String defaultPhoneNumber = "+1234567890";
        
        Users adminUser = new Users(defaultFirstName, defaultLastName, defaultEmail, defaultPhoneNumber);
        String encodedPassword = passwordEncoder.encode(defaultPassword);
        adminUser.setPassword(encodedPassword);
        adminUser.setRole(Role.ADMIN);
        
        userRepository.save(adminUser);

        UserDto.ViewDefaultAdminUserDto response = new UserDto.ViewDefaultAdminUserDto();
        response.setEmail(defaultEmail);
        response.setPassword(defaultPassword);
        
        return response;
    }

}
