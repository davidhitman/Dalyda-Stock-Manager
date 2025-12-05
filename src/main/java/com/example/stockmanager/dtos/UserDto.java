package com.example.stockmanager.dtos;

import com.example.stockmanager.entities.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

public class UserDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginDto {
        @Email(message="Invalid Email Format")
        @NotBlank(message="Email is Required")
        private String email;

        @NotBlank(message="Password is Required")
        private String password;
    }

    // format for viewing user (ViewUserDto)
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ViewUserDto {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
        private String password;
        private String phoneNumber;
        private Role role;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SignupDto {

        @NotBlank(message = "firstName can not be blank")
        private String firstName;

        @NotBlank(message = "lastName can not be blank")
        private String lastName;

        @Email(message="Invalid Email Format")
        @NotBlank(message="Email is Required")
        private String email;

        @NotBlank(message="Phone Number can not blank")
        private String phoneNumber;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateUserDto {

        @NotBlank(message = "firstName can not be blank")
        private String firstName;

        @NotBlank(message = "lastName can not be blank")
        private String lastName;

        @Email(message="Invalid Email Format")
        @NotBlank(message="Email is Required")
        private String email;

        @NotBlank(message="Phone Number can not blank")
        private String phoneNumber;

        @NotNull(message = "Role can not be Blank")
        private Role role;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChangePasswordDto {
        @NotBlank(message="The Old Password can not be Blank")
        private String oldPassword;
        @NotBlank(message="The newPassword can not be blank")
        private String newPassword;
        @NotBlank(message="The confirmation password can not be Blank")
        private String confirmPassword;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ViewDefaultAdminUserDto{
        private String Email;
        private String Password;
    }

}
