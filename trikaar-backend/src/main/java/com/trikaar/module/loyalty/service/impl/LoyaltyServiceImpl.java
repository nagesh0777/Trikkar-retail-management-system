package com.trikaar.module.loyalty.service.impl;

import com.trikaar.module.customer.entity.Customer;
import com.trikaar.module.customer.repository.CustomerRepository;
import com.trikaar.module.loyalty.entity.LoyaltyConfig;
import com.trikaar.module.loyalty.entity.LoyaltyTransaction;
import com.trikaar.module.loyalty.repository.LoyaltyConfigRepository;
import com.trikaar.module.loyalty.repository.LoyaltyTransactionRepository;
import com.trikaar.module.loyalty.service.LoyaltyService;
import com.trikaar.shared.exception.BusinessRuleException;
import com.trikaar.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoyaltyServiceImpl implements LoyaltyService {

    private final LoyaltyConfigRepository loyaltyConfigRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final CustomerRepository customerRepository;

    @Override
    public BigDecimal calculatePointsEarned(UUID businessId, BigDecimal saleAmount) {
        LoyaltyConfig config = getActiveConfig(businessId);

        if (saleAmount.compareTo(config.getMinimumPurchaseForPoints()) < 0) {
            return BigDecimal.ZERO;
        }

        // Example: ₹500 sale * 0.01 pointsPerCurrencyUnit = 5 points
        return saleAmount.multiply(config.getPointsPerCurrencyUnit())
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateRedemptionValue(UUID businessId, BigDecimal pointsToRedeem) {
        LoyaltyConfig config = getActiveConfig(businessId);

        if (pointsToRedeem.compareTo(config.getMinimumPointsForRedemption()) < 0) {
            throw new BusinessRuleException("INSUFFICIENT_LOYALTY_POINTS",
                    "Minimum " + config.getMinimumPointsForRedemption() + " points required for redemption");
        }

        // Example: 10 points * ₹1 currencyUnitsPerPoint = ₹10 discount
        return pointsToRedeem.multiply(config.getCurrencyUnitsPerPoint())
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public void processLoyaltyTransaction(UUID businessId, UUID customerId, UUID saleId,
            BigDecimal pointsEarned, BigDecimal pointsRedeemed,
            BigDecimal saleAmount) {
        Customer customer = customerRepository.findByIdAndBusinessIdAndDeletedFalse(customerId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        BigDecimal currentBalance = customer.getLoyaltyPoints();

        // Process redemption first
        if (pointsRedeemed != null && pointsRedeemed.compareTo(BigDecimal.ZERO) > 0) {
            if (currentBalance.compareTo(pointsRedeemed) < 0) {
                throw new BusinessRuleException("INSUFFICIENT_LOYALTY_POINTS",
                        "Customer has " + currentBalance + " points but tried to redeem " + pointsRedeemed);
            }

            currentBalance = currentBalance.subtract(pointsRedeemed);

            LoyaltyTransaction redeemTxn = LoyaltyTransaction.builder()
                    .customerId(customerId)
                    .transactionType(LoyaltyTransaction.TransactionType.REDEEMED)
                    .points(pointsRedeemed.negate())
                    .balanceAfter(currentBalance)
                    .saleId(saleId)
                    .saleAmount(saleAmount)
                    .description("Points redeemed for sale")
                    .build();
            redeemTxn.setBusinessId(businessId);
            loyaltyTransactionRepository.save(redeemTxn);

            log.info("Customer '{}' redeemed {} points", customerId, pointsRedeemed);
        }

        // Process earning
        if (pointsEarned != null && pointsEarned.compareTo(BigDecimal.ZERO) > 0) {
            currentBalance = currentBalance.add(pointsEarned);

            LoyaltyTransaction earnTxn = LoyaltyTransaction.builder()
                    .customerId(customerId)
                    .transactionType(LoyaltyTransaction.TransactionType.EARNED)
                    .points(pointsEarned)
                    .balanceAfter(currentBalance)
                    .saleId(saleId)
                    .saleAmount(saleAmount)
                    .description("Points earned from sale")
                    .build();
            earnTxn.setBusinessId(businessId);
            loyaltyTransactionRepository.save(earnTxn);

            log.info("Customer '{}' earned {} points", customerId, pointsEarned);
        }

        // Update customer balance and stats
        customer.setLoyaltyPoints(currentBalance);
        customer.setTotalSpent(customer.getTotalSpent().add(saleAmount));
        customer.setTotalVisits(customer.getTotalVisits() + 1);

        // Update tier based on total spent
        customer.setLoyaltyTier(calculateTier(customer.getTotalSpent()));
        customerRepository.save(customer);
    }

    private LoyaltyConfig getActiveConfig(UUID businessId) {
        return loyaltyConfigRepository.findByBusinessIdAndDeletedFalse(businessId)
                .filter(LoyaltyConfig::isActive)
                .orElseGet(() -> {
                    // Create default config if none exists
                    LoyaltyConfig defaultConfig = LoyaltyConfig.builder().build();
                    defaultConfig.setBusinessId(businessId);
                    return loyaltyConfigRepository.save(defaultConfig);
                });
    }

    private Customer.LoyaltyTier calculateTier(BigDecimal totalSpent) {
        if (totalSpent.compareTo(new BigDecimal("100000")) >= 0)
            return Customer.LoyaltyTier.PLATINUM;
        if (totalSpent.compareTo(new BigDecimal("50000")) >= 0)
            return Customer.LoyaltyTier.GOLD;
        if (totalSpent.compareTo(new BigDecimal("20000")) >= 0)
            return Customer.LoyaltyTier.SILVER;
        return Customer.LoyaltyTier.BRONZE;
    }
}
