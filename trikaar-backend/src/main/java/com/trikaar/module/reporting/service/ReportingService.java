package com.trikaar.module.reporting.service;

import com.trikaar.module.reporting.dto.DailySalesReport;
import com.trikaar.module.reporting.dto.MonthlyRevenueReport;

import java.time.LocalDate;

public interface ReportingService {

    DailySalesReport generateDailySalesReport(LocalDate date);

    MonthlyRevenueReport generateMonthlyRevenueReport(int year, int month);
}
