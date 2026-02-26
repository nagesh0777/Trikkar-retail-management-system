package com.trikaar.module.loyalty.service;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Loyalty engine service contract.
 */
public interface LoyaltyService {

    BigDecimal calculatePointsEarned(UUID businessId, BigDecimal saleAmount);

    BigDecimal calculateRedemptionValue(UUID businessId, BigDecimal pointsToRedeem);

    void processLoyaltyTransaction(UUID businessId, UUID customerId, UUID saleId,
            BigDecimal pointsEarned, BigDecimal pointsRedeemed,
            BigDecimal saleAmount);
}
