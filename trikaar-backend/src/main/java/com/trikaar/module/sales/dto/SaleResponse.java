package com.trikaar.module.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponse {

    private String id;
    private String transactionNumber;
    private String employeeId;
    private String employeeName;
    private String customerId;
    private String customerName;
    private LocalDateTime saleDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal loyaltyPointsRedeemed;
    private BigDecimal loyaltyDiscount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal changeAmount;
    private String paymentMethod;
    private String status;
    private BigDecimal loyaltyPointsEarned;
    private boolean locked;
    private String notes;
    private List<SaleItemResponse> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleItemResponse {
        private String id;
        private String productId;
        private String productName;
        private String sku;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal discountAmount;
        private BigDecimal taxAmount;
        private BigDecimal lineTotal;
    }
}
