package com.wealthlink.identity.dto;

public record RegisterResponse(
        String message,
        String username,
        String role
) {
}
