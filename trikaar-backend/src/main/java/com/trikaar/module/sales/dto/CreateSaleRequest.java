package com.trikaar.module.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSaleRequest {

    @NotNull(message = "Employee ID is required")
    private UUID employeeId;

    private UUID customerId; // Optional for walk-in customers

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<SaleItemRequest> items;

    @NotNull(message = "Payment method is required")
    private String paymentMethod;

    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.01", message = "Amount paid must be positive")
    private BigDecimal amountPaid;

    @DecimalMin(value = "0.0")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.0")
    private BigDecimal loyaltyPointsToRedeem;

    private String notes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleItemRequest {

        @NotNull(message = "Product ID is required")
        private UUID productId;

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.001", message = "Quantity must be positive")
        private BigDecimal quantity;

        @DecimalMin(value = "0.0")
        private BigDecimal discountAmount;
    }
}
