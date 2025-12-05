package com.example.stockmanager.services;

import com.example.stockmanager.dtos.PageDto;
import com.example.stockmanager.dtos.UserDto;
import com.example.stockmanager.entities.Role;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface UserServices {
    String logIn(UserDto.LoginDto loginDto);
    String passwordReset (String email);
    UserDto.ViewUserDto signup(UserDto.SignupDto signupDto, Role role);
    void passwordChange(UserDto.ChangePasswordDto changePasswordDto);
    void deleteUser (UUID id);
    UserDto.ViewUserDto updateUser (UUID id, UserDto.UpdateUserDto signupDto);
    Page<UserDto.ViewUserDto> viewAllUsers (PageDto pageDto);
    UserDto.ViewDefaultAdminUserDto defaultAdminUser();
}
