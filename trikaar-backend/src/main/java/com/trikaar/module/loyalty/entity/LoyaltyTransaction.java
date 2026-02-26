package com.trikaar.module.loyalty.entity;

import com.trikaar.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Individual loyalty point transaction — earn or redeem.
 * Provides a complete audit trail of loyalty point movements.
 */
@Entity
@Table(name = "loyalty_transactions", indexes = {
        @Index(name = "idx_loyalty_txn_business", columnList = "business_id"),
        @Index(name = "idx_loyalty_txn_customer", columnList = "customer_id"),
        @Index(name = "idx_loyalty_txn_type", columnList = "transaction_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyTransaction extends BaseEntity {

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(name = "points", nullable = false, precision = 12, scale = 2)
    private BigDecimal points;

    @Column(name = "balance_after", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "sale_id")
    private UUID saleId;

    @Column(name = "sale_amount", precision = 14, scale = 2)
    private BigDecimal saleAmount;

    @Column(name = "description", length = 500)
    private String description;

    public enum TransactionType {
        EARNED, REDEEMED, ADJUSTMENT, EXPIRED
    }
}
