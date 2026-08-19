package com.wealthlink.identity.controller;

import com.wealthlink.identity.dto.LoginRequest;
import com.wealthlink.identity.dto.LoginResponse;
import com.wealthlink.identity.dto.RegisterRequest;
import com.wealthlink.identity.dto.RegisterResponse;
import com.wealthlink.identity.entity.AppUser;
import com.wealthlink.identity.entity.UserStatus;
import com.wealthlink.identity.repository.AppUserRepository;
import com.wealthlink.security.jwt.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username(),
                                request.password()));

        UserDetails principal = (UserDetails) authentication.getPrincipal();

        return buildLoginResponse(principal);
    }

    /**
     * Registers an active internal user without issuing an access token.
     * The user must authenticate through /login to receive a JWT.
     * Roles are assigned separately through the identity RBAC endpoints.
     */
    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {

        if (appUserRepository.findByUsername(request.username()).isPresent()) {
            throw new DataIntegrityViolationException("Username already taken");
        }

        AppUser user = AppUser.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .status(UserStatus.ACTIVE)
                .build();

        appUserRepository.save(user);

        return new RegisterResponse(
                "User registered successfully. Please login to obtain a JWT.",
                user.getUsername());
    }

    private LoginResponse buildLoginResponse(UserDetails principal) {
        String token = jwtService.generateToken(principal);

        List<String> roles = principal.getAuthorities().stream()
                .map(authority -> authority.getAuthority()
                        .replaceFirst("^ROLE_", ""))
                .toList();

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs() / 1000,
                principal.getUsername(),
                roles);
    }
}
