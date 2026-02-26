package com.trikaar.module.audit.entity;

import com.trikaar.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Immutable audit log entry.
 * Records critical business events for compliance and security.
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_business_id", columnList = "business_id"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_entity_type", columnList = "entity_type"),
        @Index(name = "idx_audit_entity_id", columnList = "entity_id"),
        @Index(name = "idx_audit_performed_by", columnList = "performed_by"),
        @Index(name = "idx_audit_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditAction action;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "performed_by")
    private UUID performedBy;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    public enum AuditAction {
        // Auth
        USER_LOGIN,
        USER_LOGOUT,
        USER_REGISTERED,
        PASSWORD_CHANGED,
        ACCOUNT_LOCKED,

        // Sales
        SALE_CREATED,
        REFUND_PROCESSED,

        // Salary
        SALARY_PAYOUT,

        // Inventory
        STOCK_ADJUSTED,
        PURCHASE_RECEIVED,

        // Config
        CONFIG_CHANGED,
        LOYALTY_CONFIG_CHANGED,

        // Employee
        EMPLOYEE_TERMINATED,
        EMPLOYEE_CREATED,

        // General
        RECORD_UPDATED,
        RECORD_DELETED
    }
}
