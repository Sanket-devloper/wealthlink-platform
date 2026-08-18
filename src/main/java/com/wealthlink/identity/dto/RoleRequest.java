package com.wealthlink.identity.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleRequest(
        @NotBlank String name
) {}
