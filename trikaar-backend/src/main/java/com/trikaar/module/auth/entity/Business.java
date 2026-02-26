package com.trikaar.module.auth.entity;

import com.trikaar.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Represents a registered business (tenant) in the system.
 * Each business has its own isolated data space.
 */
@Entity
@Table(name = "businesses", uniqueConstraints = {
        @UniqueConstraint(name = "uk_business_registration", columnNames = "registration_number"),
        @UniqueConstraint(name = "uk_business_slug", columnNames = "slug")
}, indexes = {
        @Index(name = "idx_business_slug", columnList = "slug"),
        @Index(name = "idx_business_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business extends BaseEntity {

    @Column(name = "business_name", nullable = false, length = 255)
    private String businessName;

    @Column(name = "slug", nullable = false, length = 100)
    private String slug;

    @Column(name = "registration_number", length = 100)
    private String registrationNumber;

    @Column(name = "gstin", length = 20)
    private String gstin;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "currency_code", nullable = false, length = 5)
    @Builder.Default
    private String currencyCode = "INR";

    @Column(name = "timezone", nullable = false, length = 50)
    @Builder.Default
    private String timezone = "Asia/Kolkata";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "owner_user_id")
    private UUID ownerUserId;
}
