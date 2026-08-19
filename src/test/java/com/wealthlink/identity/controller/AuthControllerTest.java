package com.wealthlink.identity.controller;

import com.wealthlink.identity.dto.RegisterRequest;
import com.wealthlink.identity.dto.RegisterResponse;
import com.wealthlink.identity.entity.AppUser;
import com.wealthlink.identity.repository.AppUserRepository;
import com.wealthlink.security.jwt.JwtService;
import org.junit.jupiter.api.Test;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Test
    void registerCreatesUserWithoutGeneratingJwt() {

        String username = "testuser";
        String email = "test@example.com";
        String password = "testPassword123";
        String encodedPassword = "encoded-test-password";

        var authenticationManager =
                mock(org.springframework.security.authentication.AuthenticationManager.class);

        var jwtService = mock(JwtService.class);
        var repository = mock(AppUserRepository.class);
        var passwordEncoder = mock(PasswordEncoder.class);

        when(repository.findByUsername(username))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(password))
                .thenReturn(encodedPassword);

        when(repository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var controller = new AuthController(
                authenticationManager,
                jwtService,
                repository,
                passwordEncoder
        );

        RegisterResponse response = controller.register(
                new RegisterRequest(username, email, password)
        );

        assertEquals(username, response.username());
        assertTrue(response.message().contains("login"));

        // Registration must NOT generate JWT
        verify(jwtService, never()).generateToken(any());

        // User must be saved
        verify(repository).save(any(AppUser.class));

        // Password must be encoded
        verify(passwordEncoder).encode(password);
    }
}