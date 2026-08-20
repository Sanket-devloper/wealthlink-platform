package com.wealthlink.common.exception;

/**
 * Thrown when a registration request asks for a role that either doesn't
 * exist, or exists but isn't allowed to be self-service granted (e.g.
 * ADMIN, COMPLIANCE_OFFICER). Mapped to 400 Bad Request by
 * GlobalExceptionHandler - this is a client input problem, not a 403
 * (403 would imply the caller IS authenticated but lacks permission;
 * here they're not authenticated at all yet, they're registering).
 */
public class InvalidRoleSelectionException extends RuntimeException {
    public InvalidRoleSelectionException(String message) {
        super(message);
    }
}
