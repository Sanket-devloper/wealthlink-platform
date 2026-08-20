package com.wealthlink.identity.controller;

import com.wealthlink.common.exception.InvalidRoleSelectionException;
import com.wealthlink.identity.dto.RegisterRequest;
import com.wealthlink.identity.dto.RegisterResponse;
import com.wealthlink.identity.entity.AppUser;
import com.wealthlink.identity.entity.Role;
import com.wealthlink.identity.repository.AppUserRepository;
import com.wealthlink.identity.repository.RoleRepository;
import com.wealthlink.identity.repository.UserRoleRepository;
import com.wealthlink.security.jwt.JwtService;
import org.junit.jupiter.api.Test;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

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
        var roleRepository = mock(RoleRepository.class);
        var userRoleRepository = mock(UserRoleRepository.class);
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
                roleRepository,
                userRoleRepository,
                passwordEncoder
        );

        // No role field supplied -> account should still be created with no roles
        RegisterResponse response = controller.register(
                new RegisterRequest(username, email, password, null)
        );

        assertEquals(username, response.username());
        assertNull(response.role());
        assertTrue(response.message().contains("login"));

        // Registration must NOT generate JWT
        verify(jwtService, never()).generateToken(any());

        // User must be saved
        verify(repository).save(any(AppUser.class));

        // Password must be encoded
        verify(passwordEncoder).encode(password);

        // No role requested -> no role lookup/assignment should happen
        verify(roleRepository, never()).findByName(any());
        verify(userRoleRepository, never()).save(any());
    }

    @Test
    void registerWithSelfServiceRoleAssignsIt() {

        String username = "trader1";
        String email = "trader1@example.com";
        String password = "testPassword123";
        String encodedPassword = "encoded-test-password";

        var authenticationManager =
                mock(org.springframework.security.authentication.AuthenticationManager.class);
        var jwtService = mock(JwtService.class);
        var repository = mock(AppUserRepository.class);
        var roleRepository = mock(RoleRepository.class);
        var userRoleRepository = mock(UserRoleRepository.class);
        var passwordEncoder = mock(PasswordEncoder.class);

        when(repository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(repository.save(any(AppUser.class)))
                .thenAnswer(invocation -> {
                    AppUser u = invocation.getArgument(0);
                    u.setId(UUID.randomUUID());
                    return u;
                });

        Role traderRole = Role.builder().id(UUID.randomUUID()).name("TRADER").build();
        when(roleRepository.findByName("TRADER")).thenReturn(Optional.of(traderRole));

        var controller = new AuthController(
                authenticationManager, jwtService, repository, roleRepository, userRoleRepository, passwordEncoder);

        RegisterResponse response = controller.register(
                new RegisterRequest(username, email, password, "TRADER"));

        assertEquals("TRADER", response.role());
        verify(userRoleRepository).save(any());
    }

    @Test
    void registerRejectsPrivilegedRoleSelfAssignment() {

        String username = "wannabeadmin";
        String email = "wannabeadmin@example.com";
        String password = "testPassword123";

        var authenticationManager =
                mock(org.springframework.security.authentication.AuthenticationManager.class);
        var jwtService = mock(JwtService.class);
        var repository = mock(AppUserRepository.class);
        var roleRepository = mock(RoleRepository.class);
        var userRoleRepository = mock(UserRoleRepository.class);
        var passwordEncoder = mock(PasswordEncoder.class);

        when(repository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encoded");
        when(repository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var controller = new AuthController(
                authenticationManager, jwtService, repository, roleRepository, userRoleRepository, passwordEncoder);

        // Requesting ADMIN at registration must be rejected - this is the
        // core security behavior this whole feature exists to enforce.
        assertThrows(InvalidRoleSelectionException.class, () ->
                controller.register(new RegisterRequest(username, email, password, "ADMIN")));

        verify(userRoleRepository, never()).save(any());
    }
}
