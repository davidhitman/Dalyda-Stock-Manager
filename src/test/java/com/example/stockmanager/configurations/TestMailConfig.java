package com.example.stockmanager.configurations;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestMailConfig {

    @Bean
    @Primary
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl mockMailSender = mock(JavaMailSenderImpl.class);
        doNothing().when(mockMailSender).send(any(SimpleMailMessage.class));
        return mockMailSender;
    }
}
