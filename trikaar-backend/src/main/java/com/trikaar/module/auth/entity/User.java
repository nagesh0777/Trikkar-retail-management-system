package com.trikaar.module.auth.entity;

import com.trikaar.shared.entity.BaseEntity;
import com.trikaar.shared.enums.Role;
import jakarta.persistence.*;
import lombok.*;

/**
 * Core user entity for authentication and authorization.
 * Linked to a specific business (tenant) via BaseEntity.businessId.
 *
 * This entity is NOT the Employee entity — it represents system access
 * credentials.
 * An Employee may or may not have a corresponding User account.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email_business", columnNames = { "email", "business_id" }),
        @UniqueConstraint(name = "uk_users_username_business", columnNames = { "username", "business_id" })
}, indexes = {
        @Index(name = "idx_users_business_id", columnList = "business_id"),
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_username", columnList = "username")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private Role role;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "is_locked", nullable = false)
    private boolean locked = false;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts = 0;
}
