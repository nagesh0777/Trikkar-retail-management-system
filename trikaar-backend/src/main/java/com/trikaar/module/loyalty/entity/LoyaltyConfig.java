package com.trikaar.module.loyalty.entity;

import com.trikaar.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Configurable loyalty rules per business.
 * Example: 1 point per ₹100 spent, 1 point = ₹1 redemption value.
 */
@Entity
@Table(name = "loyalty_configs", uniqueConstraints = {
        @UniqueConstraint(name = "uk_loyalty_config_business", columnNames = { "business_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyConfig extends BaseEntity {

    @Column(name = "points_per_currency_unit", nullable = false, precision = 10, scale = 4)
    @Builder.Default
    private BigDecimal pointsPerCurrencyUnit = new BigDecimal("0.01"); // 1 point per ₹100

    @Column(name = "currency_units_per_point", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal currencyUnitsPerPoint = BigDecimal.ONE; // 1 point = ₹1

    @Column(name = "minimum_purchase_for_points", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minimumPurchaseForPoints = BigDecimal.ZERO;

    @Column(name = "minimum_points_for_redemption", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minimumPointsForRedemption = BigDecimal.TEN;

    @Column(name = "max_redemption_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal maxRedemptionPercentage = new BigDecimal("50"); // Max 50% of bill

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
