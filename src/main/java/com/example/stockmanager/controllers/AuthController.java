package com.example.stockmanager.controllers;

import com.example.stockmanager.dtos.UserDto;
import com.example.stockmanager.responses.GenericResponse;
import com.example.stockmanager.services.UserServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication Controller", description = "Handles user login authentication and resetting password")
@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserServices userService;

    // login endpoint
    @Operation(summary = "User Login", description = "User Login Endpoint")
    @PostMapping("/login")
    public ResponseEntity<GenericResponse<String>> login(@Valid @RequestBody UserDto.LoginDto loginDto) {
        var token = userService.logIn(loginDto);
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>("token to be used to login", token));
    }

    // default password reset and sent to your email endpoint
    @Operation(summary = "Password Reset", description = "Reset your password, a default password is sent to your email")
    @PostMapping("/password/reset")
    public ResponseEntity<GenericResponse<String>> defaultPassword(
            @Email(message = "Invalid Email Format")
            @NotBlank(message="Email is required")
            @RequestParam String email) {
        var userEmail = userService.passwordReset(email);
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>("Password Reset Successful, a default password was sent to your email", userEmail));
    }

    @Operation(summary= "Create a default Admin user", description = "Create an Admin user when the user table is empty, and there is no way to login and access the system")
    @PostMapping("default/user")
    public ResponseEntity<GenericResponse<UserDto.ViewDefaultAdminUserDto>> defaultAdimnUser(){
        var defaultAdminUser = userService.defaultAdminUser();
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>("The default user has been created", defaultAdminUser));
    }
}

//   http://localhost:8080/swagger-ui/index.html