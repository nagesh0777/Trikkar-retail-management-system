package com.trikaar.module.sales.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {

    @NotNull(message = "Original sale ID is required")
    private UUID originalSaleId;

    @NotBlank(message = "Refund reason is required")
    private String reason;

    private List<RefundItemRequest> items; // null = full refund

    private String notes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundItemRequest {

        @NotNull(message = "Product ID is required")
        private UUID productId;

        @NotNull(message = "Quantity is required")
        @DecimalMin(value = "0.001", message = "Quantity must be positive")
        private BigDecimal quantity;
    }
}
