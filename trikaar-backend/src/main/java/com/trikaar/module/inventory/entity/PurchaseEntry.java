package com.trikaar.module.inventory.entity;

import com.trikaar.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Purchase entry header — represents a goods-received transaction from a
 * supplier.
 */
@Entity
@Table(name = "purchase_entries", indexes = {
        @Index(name = "idx_purchase_business_id", columnList = "business_id"),
        @Index(name = "idx_purchase_supplier_id", columnList = "supplier_id"),
        @Index(name = "idx_purchase_date", columnList = "purchase_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseEntry extends BaseEntity {

    @Column(name = "purchase_number", nullable = false, unique = true, length = 50)
    private String purchaseNumber;

    @Column(name = "supplier_id", nullable = false)
    private UUID supplierId;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PurchaseStatus status = PurchaseStatus.RECEIVED;

    @Column(name = "notes", length = 1000)
    private String notes;

    @OneToMany(mappedBy = "purchaseEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseEntryItem> items = new ArrayList<>();

    public enum PurchaseStatus {
        DRAFT, RECEIVED, PARTIALLY_RECEIVED, CANCELLED
    }
}
