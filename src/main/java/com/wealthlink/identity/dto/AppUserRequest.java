package com.wealthlink.identity.dto;

import com.wealthlink.identity.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AppUserRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String passwordHash,
        UserStatus status
) {}
