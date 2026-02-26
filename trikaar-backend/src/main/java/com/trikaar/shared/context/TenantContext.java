package com.trikaar.shared.context;

import java.util.UUID;

/**
 * Thread-local holder for the current tenant (businessId).
 * Set by the JWT authentication filter and consumed by repositories
 * and services to enforce multi-tenant data isolation.
 *
 * CRITICAL: Must be cleared after each request to prevent data leakage
 * in thread-pooled environments.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_BUSINESS_ID = new ThreadLocal<>();
    private static final ThreadLocal<UUID> CURRENT_USER_ID = new ThreadLocal<>();

    private TenantContext() {
        // Utility class — prevent instantiation
    }

    public static UUID getBusinessId() {
        return CURRENT_BUSINESS_ID.get();
    }

    public static void setBusinessId(UUID businessId) {
        CURRENT_BUSINESS_ID.set(businessId);
    }

    public static UUID getUserId() {
        return CURRENT_USER_ID.get();
    }

    public static void setUserId(UUID userId) {
        CURRENT_USER_ID.set(userId);
    }

    /**
     * MUST be called at the end of every request (e.g., in a filter's finally
     * block)
     * to prevent tenant context leakage across pooled threads.
     */
    public static void clear() {
        CURRENT_BUSINESS_ID.remove();
        CURRENT_USER_ID.remove();
    }
}
