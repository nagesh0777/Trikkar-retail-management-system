package com.trikaar.module.reporting.controller;

import com.trikaar.module.reporting.dto.DailySalesReport;
import com.trikaar.module.reporting.dto.MonthlyRevenueReport;
import com.trikaar.module.reporting.service.ReportingService;
import com.trikaar.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'ANALYST')")
@Tag(name = "Reporting", description = "Business intelligence and reporting APIs")
public class ReportingController {

    private final ReportingService reportingService;

    @GetMapping("/daily-sales")
    @Operation(summary = "Generate daily sales report")
    public ResponseEntity<ApiResponse<DailySalesReport>> getDailySalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(
                reportingService.generateDailySalesReport(date)));
    }

    @GetMapping("/monthly-revenue")
    @Operation(summary = "Generate monthly revenue report")
    public ResponseEntity<ApiResponse<MonthlyRevenueReport>> getMonthlyRevenueReport(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(ApiResponse.success(
                reportingService.generateMonthlyRevenueReport(year, month)));
    }
}
