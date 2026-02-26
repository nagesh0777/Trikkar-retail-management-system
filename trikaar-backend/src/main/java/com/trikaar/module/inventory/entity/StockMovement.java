package com.trikaar.module.inventory.entity;

import com.trikaar.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Stock movement log — immutable ledger of every stock change.
 * Used for stock audit trail, reconciliation, and movement tracking.
 */
@Entity
@Table(name = "stock_movements", indexes = {
        @Index(name = "idx_stock_movement_business", columnList = "business_id"),
        @Index(name = "idx_stock_movement_product", columnList = "product_id"),
        @Index(name = "idx_stock_movement_type", columnList = "movement_type"),
        @Index(name = "idx_stock_movement_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 30)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "stock_before", nullable = false, precision = 12, scale = 3)
    private BigDecimal stockBefore;

    @Column(name = "stock_after", nullable = false, precision = 12, scale = 3)
    private BigDecimal stockAfter;

    @Column(name = "reference_id")
    private UUID referenceId; // Sale ID, Purchase ID, Adjustment ID

    @Column(name = "reference_type", length = 50)
    private String referenceType; // SALE, PURCHASE, ADJUSTMENT, REFUND

    @Column(name = "notes", length = 500)
    private String notes;

    public enum MovementType {
        PURCHASE_IN,
        SALE_OUT,
        REFUND_IN,
        ADJUSTMENT_IN,
        ADJUSTMENT_OUT,
        TRANSFER_IN,
        TRANSFER_OUT,
        DAMAGE_OUT,
        RETURN_TO_SUPPLIER
    }
}
