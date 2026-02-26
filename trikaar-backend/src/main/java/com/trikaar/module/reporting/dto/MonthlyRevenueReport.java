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
public class MonthlyRevenueReport {

    private int year;
    private int month;
    private BigDecimal totalRevenue;
    private BigDecimal totalRefunds;
    private BigDecimal netRevenue;
    private long totalTransactions;
    private BigDecimal totalSalaryPayouts;
    private BigDecimal stockValue;
    private BigDecimal grossProfit;
}
