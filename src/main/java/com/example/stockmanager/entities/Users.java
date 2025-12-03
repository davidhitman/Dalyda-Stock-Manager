package com.example.stockmanager.entities;

import com.example.stockmanager.dtos.UserDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Users implements UserDetails {

    @Id
    private UUID id = UUID.randomUUID();
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    private Role role;

    public Users(@NotBlank(message = "firstName can not be blank") String firstName,
                 @NotBlank(message = "lastName can not be blank") String lastName,
                 @Email(message="Invalid Email Format") @NotBlank(message="Email is Required") String email,
                 @NotBlank(message="Phone Number can not blank") String phoneNumber) {
        this.firstname = firstName;
        this.lastname = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {

        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public void updateUser(UserDto.UpdateUserDto updateUserDto) {
        this.setFirstname(updateUserDto.getFirstName());
        this.setLastname(updateUserDto.getLastName());
        this.setEmail(updateUserDto.getEmail());
        this.setPhoneNumber(updateUserDto.getPhoneNumber());
        this.setRole(updateUserDto.getRole());
    }
}
