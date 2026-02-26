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
 * Refund transaction linked to an original sale.
 * Supports both full and partial refunds.
 */
@Entity
@Table(name = "refunds", indexes = {
        @Index(name = "idx_refund_business_id", columnList = "business_id"),
        @Index(name = "idx_refund_sale_id", columnList = "original_sale_id"),
        @Index(name = "idx_refund_date", columnList = "refund_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund extends BaseEntity {

    @Column(name = "refund_number", nullable = false, unique = true, length = 50)
    private String refundNumber;

    @Column(name = "original_sale_id", nullable = false)
    private UUID originalSaleId;

    @Column(name = "original_transaction_number", nullable = false, length = 50)
    private String originalTransactionNumber;

    @Column(name = "refund_date", nullable = false)
    private LocalDateTime refundDate;

    @Column(name = "refund_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type", nullable = false, length = 20)
    private RefundType refundType;

    @Column(name = "processed_by_id", nullable = false)
    private UUID processedById;

    @Column(name = "notes", length = 1000)
    private String notes;

    @OneToMany(mappedBy = "refund", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RefundItem> items = new ArrayList<>();

    public enum RefundType {
        FULL, PARTIAL
    }
}
