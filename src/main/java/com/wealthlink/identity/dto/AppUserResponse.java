package com.wealthlink.identity.dto;

import com.wealthlink.identity.entity.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Deliberately excludes passwordHash - never serialize credentials back to a client.
public record AppUserResponse(
        UUID id,
        String username,
        String email,
        UserStatus status,
        Instant createdAt,
        Instant updatedAt,
        List<String> roles
) {}
