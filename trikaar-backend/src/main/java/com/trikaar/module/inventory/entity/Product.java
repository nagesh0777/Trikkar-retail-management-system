package com.trikaar.module.inventory.entity;

import com.trikaar.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Product master entity representing items available for sale.
 * Tracks pricing, stock levels, and categorization.
 */
@Entity
@Table(name = "products", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_sku_business", columnNames = { "sku", "business_id" }),
        @UniqueConstraint(name = "uk_product_barcode_business", columnNames = { "barcode", "business_id" })
}, indexes = {
        @Index(name = "idx_product_business_id", columnList = "business_id"),
        @Index(name = "idx_product_sku", columnList = "sku"),
        @Index(name = "idx_product_barcode", columnList = "barcode"),
        @Index(name = "idx_product_category", columnList = "category"),
        @Index(name = "idx_product_low_stock", columnList = "current_stock, reorder_level")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(name = "sku", nullable = false, length = 100)
    private String sku;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "sub_category", length = 100)
    private String subCategory;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "unit", nullable = false, length = 30)
    private String unit; // PCS, KG, LTR, MTR, etc.

    @Column(name = "cost_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "selling_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "mrp", precision = 12, scale = 2)
    private BigDecimal mrp;

    @Column(name = "tax_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxPercentage = BigDecimal.ZERO;

    @Column(name = "hsn_code", length = 20)
    private String hsnCode;

    @Column(name = "current_stock", nullable = false, precision = 12, scale = 3)
    @Builder.Default
    private BigDecimal currentStock = BigDecimal.ZERO;

    @Column(name = "reorder_level", nullable = false, precision = 12, scale = 3)
    @Builder.Default
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    @Column(name = "max_stock_level", precision = 12, scale = 3)
    private BigDecimal maxStockLevel;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "is_taxable", nullable = false)
    @Builder.Default
    private boolean taxable = true;

    public boolean isLowStock() {
        return currentStock.compareTo(reorderLevel) <= 0;
    }

    public BigDecimal getStockValue() {
        return currentStock.multiply(costPrice);
    }
}
