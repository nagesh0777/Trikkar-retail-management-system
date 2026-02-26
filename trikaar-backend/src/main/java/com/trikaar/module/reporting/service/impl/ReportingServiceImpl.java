package com.trikaar.module.reporting.service.impl;

import com.trikaar.module.inventory.repository.ProductRepository;
import com.trikaar.module.reporting.dto.DailySalesReport;
import com.trikaar.module.reporting.dto.MonthlyRevenueReport;
import com.trikaar.module.reporting.service.ReportingService;
import com.trikaar.module.salary.repository.SalaryPayoutRepository;
import com.trikaar.module.sales.repository.RefundRepository;
import com.trikaar.module.sales.repository.SaleRepository;
import com.trikaar.shared.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportingServiceImpl implements ReportingService {

    private final SaleRepository saleRepository;
    private final RefundRepository refundRepository;
    private final SalaryPayoutRepository salaryPayoutRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public DailySalesReport generateDailySalesReport(LocalDate date) {
        UUID businessId = TenantContext.getBusinessId();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        BigDecimal totalRevenue = saleRepository.calculateRevenue(businessId, startOfDay, endOfDay);
        long totalTransactions = saleRepository.countSales(businessId, startOfDay, endOfDay);
        BigDecimal totalRefunds = refundRepository.calculateTotalRefunds(businessId, startOfDay, endOfDay);
        BigDecimal netRevenue = totalRevenue.subtract(totalRefunds);
        BigDecimal averageTransactionValue = totalTransactions > 0
                ? totalRevenue.divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return DailySalesReport.builder()
                .reportDate(date)
                .totalTransactions(totalTransactions)
                .totalRevenue(totalRevenue)
                .totalRefunds(totalRefunds)
                .netRevenue(netRevenue)
                .averageTransactionValue(averageTransactionValue)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyRevenueReport generateMonthlyRevenueReport(int year, int month) {
        UUID businessId = TenantContext.getBusinessId();
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        BigDecimal totalRevenue = saleRepository.calculateRevenue(businessId, startOfMonth, endOfMonth);
        long totalTransactions = saleRepository.countSales(businessId, startOfMonth, endOfMonth);
        BigDecimal totalRefunds = refundRepository.calculateTotalRefunds(businessId, startOfMonth, endOfMonth);
        BigDecimal netRevenue = totalRevenue.subtract(totalRefunds);
        BigDecimal totalSalaryPayouts = salaryPayoutRepository.calculateTotalPayouts(
                businessId, yearMonth.atDay(1), yearMonth.atEndOfMonth());
        BigDecimal stockValue = productRepository.calculateTotalStockValue(businessId);
        BigDecimal grossProfit = netRevenue.subtract(totalSalaryPayouts);

        return MonthlyRevenueReport.builder()
                .year(year)
                .month(month)
                .totalRevenue(totalRevenue)
                .totalRefunds(totalRefunds)
                .netRevenue(netRevenue)
                .totalTransactions(totalTransactions)
                .totalSalaryPayouts(totalSalaryPayouts)
                .stockValue(stockValue)
                .grossProfit(grossProfit)
                .build();
    }
}
