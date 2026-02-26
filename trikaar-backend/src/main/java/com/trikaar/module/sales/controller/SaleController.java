package com.trikaar.module.sales.controller;

import com.trikaar.module.sales.dto.CreateSaleRequest;
import com.trikaar.module.sales.dto.RefundRequest;
import com.trikaar.module.sales.dto.SaleResponse;
import com.trikaar.module.sales.service.SaleService;
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
@RequestMapping("/sales")
@RequiredArgsConstructor
@Tag(name = "Sales & POS", description = "Point of Sale and sales management APIs")
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKER')")
    @Operation(summary = "Create a new sale", description = "Process a complete POS transaction")
    public ResponseEntity<ApiResponse<SaleResponse>> createSale(
            @Valid @RequestBody CreateSaleRequest request) {
        SaleResponse response = saleService.createSale(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Sale completed successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKER', 'ACCOUNTANT', 'ANALYST')")
    @Operation(summary = "Get sale by ID")
    public ResponseEntity<ApiResponse<SaleResponse>> getSale(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(saleService.getSaleById(id)));
    }

    @GetMapping("/transaction/{transactionNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKER', 'ACCOUNTANT', 'ANALYST')")
    @Operation(summary = "Get sale by transaction number")
    public ResponseEntity<ApiResponse<SaleResponse>> getSaleByTransaction(
            @PathVariable String transactionNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                saleService.getSaleByTransactionNumber(transactionNumber)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'ANALYST')")
    @Operation(summary = "List all sales")
    public ResponseEntity<ApiResponse<PagedResponse<SaleResponse>>> getAllSales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "saleDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success(
                saleService.getAllSales(page, size, sortBy, sortDir)));
    }

    @GetMapping("/by-employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'ANALYST')")
    @Operation(summary = "Get sales by employee")
    public ResponseEntity<ApiResponse<PagedResponse<SaleResponse>>> getSalesByEmployee(
            @PathVariable UUID employeeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                saleService.getSalesByEmployee(employeeId, page, size)));
    }

    @GetMapping("/by-customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKER', 'ANALYST')")
    @Operation(summary = "Get sales by customer")
    public ResponseEntity<ApiResponse<PagedResponse<SaleResponse>>> getSalesByCustomer(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                saleService.getSalesByCustomer(customerId, page, size)));
    }

    @PostMapping("/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    @Operation(summary = "Process a refund", description = "Full or partial refund of a completed sale")
    public ResponseEntity<ApiResponse<SaleResponse>> processRefund(
            @Valid @RequestBody RefundRequest request) {
        SaleResponse response = saleService.processRefund(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Refund processed successfully"));
    }
}
