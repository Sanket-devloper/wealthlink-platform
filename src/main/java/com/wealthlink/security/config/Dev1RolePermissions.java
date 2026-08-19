package com.wealthlink.security.config;

/**
 * Centralized RBAC policy for the Dev1 Foundation APIs.
 *
 * Dev2/Dev3/Dev4 APIs are intentionally not defined here.
 * Those teams can extend authorization independently.
 */
public final class Dev1RolePermissions {

    public static final String ADMIN = "ADMIN";
    public static final String TRADER = "TRADER";
    public static final String RECONCILER = "RECONCILER";
    public static final String COMPLIANCE_OFFICER = "COMPLIANCE_OFFICER";

    private Dev1RolePermissions() {
    }
}
