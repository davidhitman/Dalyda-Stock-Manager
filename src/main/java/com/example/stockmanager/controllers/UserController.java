package com.example.stockmanager.controllers;

import com.example.stockmanager.dtos.PageDto;
import com.example.stockmanager.dtos.UserDto;
import com.example.stockmanager.entities.Role;
import com.example.stockmanager.responses.GenericResponse;
import com.example.stockmanager.services.UserServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "User Controller", description = "Handles Users")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/users")
@PreAuthorize("hasAuthority('ADMIN')")
public class UserController {

    private final UserServices userService;

    // user registration
    @Operation(summary = "User Registration", description = "Register Users")
    @PostMapping("/user/registration")
    public ResponseEntity<GenericResponse<UserDto.ViewUserDto>> userRegistration(@Valid @RequestBody UserDto.SignupDto signupDto, @RequestParam Role role) {
        var userSignup = userService.signup(signupDto, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericResponse<>("User Registered Successful", userSignup));
    }

    // user registration
    @Operation(summary = "Change Password", description = "Endpoint that Allows Users to change their password")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @PostMapping("/password/change")
    public ResponseEntity<GenericResponse<String>> passwordChange(@Valid @RequestBody UserDto.ChangePasswordDto changePasswordDto) {
        userService.passwordChange(changePasswordDto);
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>("Password has been changed Successfully", "Please LogIn again"));
    }

    // Delete User
    @Operation(summary = "Delete User", description = "Allow Admins to delete Users")
    @DeleteMapping("/{id}")
    public ResponseEntity<GenericResponse<String>> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>("User deleted successfully!", "Deleted ID: " + id));
    }

    @Operation(summary = "Update User", description = "Allows Admin to Update Users")
    @PatchMapping("/update/user/{id}")
    public ResponseEntity<GenericResponse<UserDto.ViewUserDto>> updateUser(@PathVariable UUID id, @Valid @RequestBody UserDto.UpdateUserDto updateUser) {
        var updatedUser = userService.updateUser(id, updateUser);
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>("User Updated Successfully!",  updatedUser));
    }

    @Operation(summary = "View all Users", description = "Allows Admins to view all signed Up users")
    @GetMapping
    public ResponseEntity<GenericResponse<Page<UserDto.ViewUserDto>>> viewUsers(PageDto pageDto) {
        var users = userService.viewAllUsers(pageDto);
        return ResponseEntity.status(HttpStatus.OK).body(new GenericResponse<>("All Stored users",  users));
    }
}
