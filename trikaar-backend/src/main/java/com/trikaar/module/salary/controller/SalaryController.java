package com.trikaar.module.salary.controller;

import com.trikaar.module.salary.dto.GenerateSalaryRequest;
import com.trikaar.module.salary.dto.SalaryPayoutResponse;
import com.trikaar.module.salary.service.SalaryService;
import com.trikaar.shared.dto.ApiResponse;
import com.trikaar.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/salary")
@RequiredArgsConstructor
@Tag(name = "Salary", description = "Salary and payroll management APIs")
public class SalaryController {

    private final SalaryService salaryService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    @Operation(summary = "Generate salary for an employee")
    public ResponseEntity<ApiResponse<SalaryPayoutResponse>> generateSalary(
            @Valid @RequestBody GenerateSalaryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(salaryService.generateSalary(request),
                        "Salary generated successfully"));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a salary payout")
    public ResponseEntity<ApiResponse<SalaryPayoutResponse>> approvePayout(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                salaryService.approvePayout(id), "Payout approved"));
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    @Operation(summary = "Mark payout as paid")
    public ResponseEntity<ApiResponse<SalaryPayoutResponse>> markAsPaid(
            @PathVariable UUID id,
            @RequestParam String paymentReference) {
        return ResponseEntity.ok(ApiResponse.success(
                salaryService.markAsPaid(id, paymentReference), "Payout marked as paid"));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    @Operation(summary = "Get salary history for an employee")
    public ResponseEntity<ApiResponse<PagedResponse<SalaryPayoutResponse>>> getByEmployee(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                salaryService.getPayoutsByEmployee(employeeId, page, size)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    @Operation(summary = "List all salary payouts")
    public ResponseEntity<ApiResponse<PagedResponse<SalaryPayoutResponse>>> getAllPayouts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                salaryService.getAllPayouts(page, size)));
    }
}
