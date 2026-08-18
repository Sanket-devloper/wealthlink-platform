package com.wealthlink.identity.dto;

import java.util.UUID;

public record RoleResponse(
        UUID id,
        String name
) {}
