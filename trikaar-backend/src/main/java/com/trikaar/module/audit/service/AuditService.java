package com.trikaar.module.audit.service;

import com.trikaar.module.audit.entity.AuditLog;

import java.util.UUID;

/**
 * Audit service for logging critical business events.
 */
public interface AuditService {

    void logAction(AuditLog.AuditAction action, String entityType, UUID entityId, String description);

    void logAction(AuditLog.AuditAction action, String entityType, UUID entityId,
            String description, String oldValue, String newValue);
}
