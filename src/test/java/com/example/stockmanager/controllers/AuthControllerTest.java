package com.example.stockmanager.controllers;

import com.example.stockmanager.configurations.TestMailConfig;
import com.example.stockmanager.entities.Role;
import com.example.stockmanager.entities.Users;
import com.example.stockmanager.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new Users("Test", "User", "test@example.com", "1234567890");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);
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
}

