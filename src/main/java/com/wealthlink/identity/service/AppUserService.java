package com.wealthlink.identity.service;

import com.wealthlink.common.exception.ResourceNotFoundException;
import com.wealthlink.identity.dto.AppUserRequest;
import com.wealthlink.identity.dto.AppUserResponse;
import com.wealthlink.identity.entity.AppUser;
import com.wealthlink.identity.entity.Role;
import com.wealthlink.identity.entity.UserRole;
import com.wealthlink.identity.entity.UserRoleId;
import com.wealthlink.identity.repository.AppUserRepository;
import com.wealthlink.identity.repository.RoleRepository;
import com.wealthlink.identity.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public List<AppUserResponse> findAll() {
        return appUserRepository.findAll().stream().map(this::toResponse).toList();
    }

    public AppUserResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public AppUserResponse create(AppUserRequest request) {
        AppUser user = AppUser.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(request.passwordHash()) // caller is responsible for hashing before sending
                .status(request.status())
                .build();
        return toResponse(appUserRepository.save(user));
    }

    public AppUserResponse update(UUID id, AppUserRequest request) {
        AppUser user = getOrThrow(id);
        user.setUsername(request.username());
        user.setEmail(request.email());
        if (request.passwordHash() != null && !request.passwordHash().isBlank()) {
            user.setPasswordHash(request.passwordHash());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }
        return toResponse(appUserRepository.save(user));
    }

    public void delete(UUID id) {
        appUserRepository.delete(getOrThrow(id));
    }

    public AppUserResponse assignRole(UUID userId, UUID roleId) {
        AppUser user = getOrThrow(userId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", roleId));

        UserRole userRole = UserRole.builder()
                .id(new UserRoleId(userId, roleId))
                .user(user)
                .role(role)
                .build();
        userRoleRepository.save(userRole);

        return toResponse(getOrThrow(userId));
    }

    public void removeRole(UUID userId, UUID roleId) {
        userRoleRepository.deleteById(new UserRoleId(userId, roleId));
    }

    private AppUser getOrThrow(UUID id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AppUser", id));
    }

    private AppUserResponse toResponse(AppUser u) {
        List<String> roleNames = userRoleRepository.findByUserId(u.getId()).stream()
                .map(ur -> ur.getRole().getName())
                .toList();
        return new AppUserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getStatus(),
                u.getCreatedAt(), u.getUpdatedAt(), roleNames);
    }
}
