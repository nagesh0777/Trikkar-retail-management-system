package com.trikaar.shared.exception;

/**
 * Thrown when a multi-tenant access violation is detected.
 * This is a critical security exception — cross-tenant data leakage.
 */
public class TenantAccessDeniedException extends RuntimeException {

    public TenantAccessDeniedException() {
        super("Access denied: cross-tenant data access attempted");
    }

    public TenantAccessDeniedException(String message) {
        super(message);
    }
}
