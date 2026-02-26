package com.trikaar.module.customer.entity;

import com.trikaar.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Customer entity for the retail business.
 * Tracks customer profile, loyalty points, and purchase history metadata.
 */
@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_phone_business", columnNames = { "phone", "business_id" }),
        @UniqueConstraint(name = "uk_customer_email_business", columnNames = { "email", "business_id" })
}, indexes = {
        @Index(name = "idx_customer_business_id", columnList = "business_id"),
        @Index(name = "idx_customer_phone", columnList = "phone"),
        @Index(name = "idx_customer_loyalty_tier", columnList = "loyalty_tier")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 15)
    private String gender;

    @Column(name = "loyalty_points", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal loyaltyPoints = BigDecimal.ZERO;

    @Column(name = "total_spent", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "total_visits", nullable = false)
    @Builder.Default
    private int totalVisits = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "loyalty_tier", nullable = false, length = 20)
    @Builder.Default
    private LoyaltyTier loyaltyTier = LoyaltyTier.BRONZE;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "notes", length = 1000)
    private String notes;

    public String getFullName() {
        return lastName != null ? firstName + " " + lastName : firstName;
    }

    public enum LoyaltyTier {
        BRONZE, SILVER, GOLD, PLATINUM
    }
}
