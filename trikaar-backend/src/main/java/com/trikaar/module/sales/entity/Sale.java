package com.trikaar.module.sales.entity;

import com.trikaar.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Sale transaction header — represents a complete POS sale.
 * Once finalized (COMPLETED), this record becomes IMMUTABLE.
 *
 * Sale lifecycle:
 * DRAFT → COMPLETED → (optionally) REFUNDED / PARTIALLY_REFUNDED
 */
@Entity
@Table(name = "sales", indexes = {
        @Index(name = "idx_sale_business_id", columnList = "business_id"),
        @Index(name = "idx_sale_transaction_number", columnList = "transaction_number"),
        @Index(name = "idx_sale_employee_id", columnList = "employee_id"),
        @Index(name = "idx_sale_customer_id", columnList = "customer_id"),
        @Index(name = "idx_sale_date", columnList = "sale_date"),
        @Index(name = "idx_sale_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale extends BaseEntity {

    @Column(name = "transaction_number", nullable = false, unique = true, length = 50)
    private String transactionNumber;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "customer_id")
    private UUID customerId; // Optional — walk-in customers may not have a profile

    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    @Column(name = "subtotal", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "loyalty_points_redeemed", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal loyaltyPointsRedeemed = BigDecimal.ZERO;

    @Column(name = "loyalty_discount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal loyaltyDiscount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "amount_paid", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "change_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal changeAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private SaleStatus status = SaleStatus.DRAFT;

    @Column(name = "loyalty_points_earned", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal loyaltyPointsEarned = BigDecimal.ZERO;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private boolean locked = false;

    @Column(name = "notes", length = 1000)
    private String notes;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SaleItem> items = new ArrayList<>();

    public enum PaymentMethod {
        CASH, CARD, UPI, MIXED, CREDIT
    }

    public enum SaleStatus {
        DRAFT, COMPLETED, REFUNDED, PARTIALLY_REFUNDED, VOID
    }
}
