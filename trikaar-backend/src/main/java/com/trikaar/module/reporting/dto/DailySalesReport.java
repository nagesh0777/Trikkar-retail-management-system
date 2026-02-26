package com.trikaar.module.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySalesReport {

    private LocalDate reportDate;
    private long totalTransactions;
    private BigDecimal totalRevenue;
    private BigDecimal totalRefunds;
    private BigDecimal netRevenue;
    private BigDecimal totalTax;
    private BigDecimal totalDiscount;
    private BigDecimal cashTotal;
    private BigDecimal cardTotal;
    private BigDecimal upiTotal;
    private BigDecimal averageTransactionValue;
}
