package com.wealthlink.identity.service.impl;

import com.wealthlink.identity.service.AppUserService;

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
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public List<AppUserResponse> findAll() {
        return appUserRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public AppUserResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Override
    public AppUserResponse create(AppUserRequest request) {
        AppUser user = AppUser.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(request.passwordHash())
                .status(request.status())
                .build();
        return toResponse(appUserRepository.save(user));
    }

    @Override
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

    @Override
    public void delete(UUID id) {
        appUserRepository.delete(getOrThrow(id));
    }

    @Override
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

    @Override
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
