package com.example.stockmanager.controllers;

import com.example.stockmanager.configurations.TestMailConfig;
import com.example.stockmanager.dtos.UserDto;
import com.example.stockmanager.entities.Role;
import com.example.stockmanager.entities.Users;
import com.example.stockmanager.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;


import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestMailConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Users testUser;

    private UserDto.LoginDto loginDto;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new Users("Test", "User", "test@example.com", "1234567890");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);

        loginDto = new UserDto.LoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");
    }

    @Test
    void testPasswordReset_Success() throws Exception {
        String email = "test@example.com";
        String originalPassword = testUser.getPassword();

        mockMvc.perform(post("/api/v1/auth/password/reset")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Password Reset Successful, a default password was sent to your email"))
                .andExpect(jsonPath("$.data").value(email));

        userRepository.flush();

        Users updatedUser = userRepository.findByEmail(email).orElseThrow();

        assertNotEquals(originalPassword, updatedUser.getPassword());
        assertNotNull(updatedUser.getPassword());
        assertFalse(updatedUser.getPassword().isEmpty());
    }

    @Test
    void testLogin_Success() throws Exception {
        String jsonBody = String.format(
            "{\"email\":\"%s\",\"password\":\"%s\"}",
            loginDto.getEmail(),
            loginDto.getPassword()
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("token to be used to login"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data").isString());
    }



//    @Test
//    void testLogin_InvalidEmail() throws Exception {
//        loginDto.setEmail("invalid-email");
//
//        mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginDto)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void testLogin_WrongPassword() throws Exception {
//        loginDto.setPassword("wrongPassword");
//
//        mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginDto)))
//                .andExpect(status().isUnauthorized())
//                .andExpect(jsonPath("$.data").isEmpty());
//    }
//
//    @Test
//    void testLogin_UserNotFound() throws Exception {
//        loginDto.setEmail("nonexistent@example.com");
//
//        mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginDto)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void testLogin_MissingPassword() throws Exception {
//        loginDto.setPassword("");
//
//        mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginDto)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void testLogin_UserNotFound() throws Exception {
//        loginDto.setEmail("nonexistent@example.com");
//
//        mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginDto)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void testLogin_MissingPassword() throws Exception {
//        loginDto.setPassword("");
//
//        mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginDto)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void testPasswordReset_Success() throws Exception {
//        String email = "test@example.com";
//
//        String originalPassword = testUser.getPassword();
//
//        mockMvc.perform(post("/api/v1/auth/password/reset")
//                        .param("email", email))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Password Reset Successful, a default password was sent to your email"))
//                .andExpect(jsonPath("$.data").value(email));
//
//        userRepository.flush();
//
//        Users updatedUser = userRepository.findByEmail(email).orElseThrow();
//        assertNotEquals(originalPassword, updatedUser.getPassword());
//
//        assertNotNull(updatedUser.getPassword());
//        assertFalse(updatedUser.getPassword().isEmpty());
//    }
//
//    @Test
//    void testPasswordReset_InvalidEmail() throws Exception {
//        mockMvc.perform(post("/api/v1/auth/password/reset")
//                        .param("email", "invalid-email"))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void testPasswordReset_BlankEmail() throws Exception {
//        mockMvc.perform(post("/api/v1/auth/password/reset")
//                        .param("email", ""))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void testPasswordReset_UserNotFound() throws Exception {
//        mockMvc.perform(post("/api/v1/auth/password/reset")
//                        .param("email", "nonexistent@example.com"))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.data").isEmpty());
//    }


}

