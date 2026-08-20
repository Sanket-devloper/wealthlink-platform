package com.wealthlink.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "password must be at least 8 characters") String password,

        // Optional. If omitted, the account is created with no roles at
        // all (an ADMIN must assign one later). If provided, it must be
        // one of the self-service-safe roles - see
        // AuthController.SELF_SERVICE_ROLES. Requesting ADMIN or
        // COMPLIANCE_OFFICER here is rejected: those are privileged roles
        // that must be granted by an existing ADMIN via
        // POST /api/v1/users/{id}/roles/{roleId}, never by self-registration.
        String role
) {
}
