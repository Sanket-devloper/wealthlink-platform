package com.wealthlink.identity.controller;

import com.wealthlink.common.exception.InvalidRoleSelectionException;
import com.wealthlink.common.exception.ResourceNotFoundException;
import com.wealthlink.identity.dto.LoginRequest;
import com.wealthlink.identity.dto.LoginResponse;
import com.wealthlink.identity.dto.RegisterRequest;
import com.wealthlink.identity.dto.RegisterResponse;
import com.wealthlink.identity.entity.AppUser;
import com.wealthlink.identity.entity.Role;
import com.wealthlink.identity.entity.UserRole;
import com.wealthlink.identity.entity.UserRoleId;
import com.wealthlink.identity.entity.UserStatus;
import com.wealthlink.identity.repository.AppUserRepository;
import com.wealthlink.identity.repository.RoleRepository;
import com.wealthlink.identity.repository.UserRoleRepository;
import com.wealthlink.security.config.Dev1RolePermissions;
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
import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * Roles a caller may request for themselves at registration time.
     * ADMIN and COMPLIANCE_OFFICER are deliberately excluded - those are
     * privileged roles that only an existing ADMIN can grant, via
     * POST /api/v1/users/{id}/roles/{roleId}. Letting self-registration
     * hand out either would be a privilege escalation hole on a financial
     * platform.
     */
    private static final Set<String> SELF_SERVICE_ROLES = Set.of(
            Dev1RolePermissions.TRADER,
            Dev1RolePermissions.RECONCILER
    );

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
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
     * Registers an active internal user without issuing an access token -
     * creating an account and authenticating are two separate actions.
     * The caller may optionally request one role from SELF_SERVICE_ROLES;
     * ADMIN/COMPLIANCE_OFFICER requests are rejected with 400. Omitting
     * role entirely creates an account with no roles, same as before -
     * an ADMIN assigns one later via the identity RBAC endpoints.
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
        AppUser saved = appUserRepository.save(user);

        String assignedRole = null;

        if (request.role() != null && !request.role().isBlank()) {
            String requestedRole = request.role().trim().toUpperCase();

            if (!SELF_SERVICE_ROLES.contains(requestedRole)) {
                throw new InvalidRoleSelectionException(
                        "Role '" + requestedRole + "' cannot be self-assigned at registration. "
                                + "Allowed roles here: " + SELF_SERVICE_ROLES
                                + ". ADMIN/COMPLIANCE_OFFICER must be granted by an existing ADMIN.");
            }

            Role role = roleRepository.findByName(requestedRole)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", null));

            UserRole userRole = UserRole.builder()
                    .id(new UserRoleId(saved.getId(), role.getId()))
                    .user(saved)
                    .role(role)
                    .build();
            userRoleRepository.save(userRole);

            assignedRole = requestedRole;
        }

        String message = assignedRole != null
                ? "User registered successfully with role " + assignedRole + ". Please login to obtain a JWT."
                : "User registered successfully with no role assigned. Please login to obtain a JWT.";

        return new RegisterResponse(message, saved.getUsername(), assignedRole);
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
