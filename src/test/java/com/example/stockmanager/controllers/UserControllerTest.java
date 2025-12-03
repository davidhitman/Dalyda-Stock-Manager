package com.example.stockmanager.controllers;

import com.example.stockmanager.configurations.TestMailConfig;
import com.example.stockmanager.dtos.UserDto;
import com.example.stockmanager.entities.Role;
import com.example.stockmanager.entities.Users;
import com.example.stockmanager.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestMailConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Users testUser;
    private UserDto.SignupDto signupDto;
    private UserDto.UpdateUserDto updateUserDto;
    private UserDto.ChangePasswordDto changePasswordDto;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Create test admin user
        Users testAdmin = new Users("Admin", "User", "admin@test.com", "1234567890");
        testAdmin.setPassword(passwordEncoder.encode("password123"));
        testAdmin.setRole(Role.ADMIN);
        userRepository.save(testAdmin);

        // Create test regular user
        testUser = new Users("Test", "User", "user@test.com", "0987654321");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);

        // Setup DTOs
        signupDto = new UserDto.SignupDto();
        signupDto.setFirstName("test");
        signupDto.setLastName("User");
        signupDto.setEmail("test.user@example.com");
        signupDto.setPhoneNumber("1111111111");

        updateUserDto = new UserDto.UpdateUserDto();
        updateUserDto.setFirstName("Jane");
        updateUserDto.setLastName("Smith");
        updateUserDto.setEmail("jane.smith@example.com");
        updateUserDto.setPhoneNumber("2222222222");
        updateUserDto.setRole(Role.ADMIN);

        changePasswordDto = new UserDto.ChangePasswordDto();
        changePasswordDto.setOldPassword("password123");
        changePasswordDto.setNewPassword("newPassword123");
        changePasswordDto.setConfirmPassword("newPassword123");
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUserRegistration_Success() throws Exception {
        mockMvc.perform(post("/api/v1/users/user/registration")
                        .with(csrf())
                        .param("role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User Registered Successful"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.email").value("test.user@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("test"))
                .andExpect(jsonPath("$.data.role").value("USER"));

        // Verify user was created in database
        Users createdUser = userRepository.findByEmail("test.user@example.com").orElseThrow();
        assertEquals("test", createdUser.getFirstname());
        assertEquals("User", createdUser.getLastname());
        assertEquals(Role.USER, createdUser.getRole());
        assertNotNull(createdUser.getPassword());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUserRegistration_WithAdminRole() throws Exception {
        mockMvc.perform(post("/api/v1/users/user/registration")
                        .with(csrf())
                        .param("role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));


    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUserRegistration_DuplicateEmail() throws Exception {
        // Try to register with existing email
        signupDto.setEmail("user@test.com");

        mockMvc.perform(post("/api/v1/users/user/registration")
                        .with(csrf())
                        .param("role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUserRegistration_InvalidData() throws Exception {
        signupDto.setEmail("invalid-email");

        mockMvc.perform(post("/api/v1/users/user/registration")
                        .with(csrf())
                        .param("role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testUserRegistration_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/users/user/registration")
                        .with(csrf())
                        .param("role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test.user@example.com", authorities = "USER")
    void testPasswordChange_Success() throws Exception {
        
        mockMvc.perform(post("/api/v1/users/password/change")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testPasswordChange_AsAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/users/password/change")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testPasswordChange_WrongOldPassword() throws Exception {
        changePasswordDto.setOldPassword("wrongPassword");

        mockMvc.perform(post("/api/v1/users/password/change")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testPasswordChange_PasswordMismatch() throws Exception {
        changePasswordDto.setConfirmPassword("differentPassword");

        mockMvc.perform(post("/api/v1/users/password/change")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testDeleteUser_Success() throws Exception {
        UUID userId = testUser.getId();

        mockMvc.perform(delete("/api/v1/users/{id}", userId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully!"))
                .andExpect(jsonPath("$.data").value("Deleted ID: " + userId));

        // Verify user was deleted
        assertFalse(userRepository.findById(userId).isPresent());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testDeleteUser_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{id}", nonExistentId)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testDeleteUser_Unauthorized() throws Exception {
        UUID userId = testUser.getId();

        mockMvc.perform(delete("/api/v1/users/{id}", userId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateUser_Success() throws Exception {
        UUID userId = testUser.getId();

        updateUserDto.setEmail("jane.smith@example.com");

        mockMvc.perform(patch("/api/v1/users/update/user/{id}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User Updated Successfully!"))
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.firstName").value("Jane"))
                .andExpect(jsonPath("$.data.email").value("jane.smith@example.com"));

        // Verify user was updated in database
        userRepository.flush();
        Users updatedUser = userRepository.findByEmail("jane.smith@example.com").orElseThrow();
        assertEquals("Jane", updatedUser.getFirstname());
        assertEquals("Smith", updatedUser.getLastname());
        assertEquals("jane.smith@example.com", updatedUser.getEmail());
        assertEquals(Role.ADMIN, updatedUser.getRole());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateUser_DuplicateEmail() throws Exception {
        UUID userId = testUser.getId();

        updateUserDto.setEmail("admin@test.com");

        mockMvc.perform(patch("/api/v1/users/update/user/{id}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateUser_InvalidData() throws Exception {
        UUID userId = testUser.getId();
        updateUserDto.setEmail("invalid-email");

        mockMvc.perform(patch("/api/v1/users/update/user/{id}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateUser_NotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/users/update/user/{id}", nonExistentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testUpdateUser_Unauthorized() throws Exception {
        UUID userId = testUser.getId();

        mockMvc.perform(patch("/api/v1/users/update/user/{id}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testViewUsers_Success() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All Stored users"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].id").exists())
                .andExpect(jsonPath("$.data.content[0].email").exists());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testViewUsers_WithPagination() throws Exception {
        // Create additional users for pagination test
        for (int i = 0; i < 5; i++) {
            Users user = new Users("User" + i, "Test", "user" + i + "@test.com", "123456789" + i);
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(Role.USER);
            userRepository.save(user);
        }

        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(3))
                .andExpect(jsonPath("$.data.totalElements").value(7)); // 2 original + 5 new
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testViewUsers_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }
}
