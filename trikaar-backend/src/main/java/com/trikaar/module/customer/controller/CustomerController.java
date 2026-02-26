package com.trikaar.module.customer.controller;

import com.trikaar.module.customer.dto.CustomerRequest;
import com.trikaar.module.customer.dto.CustomerResponse;
import com.trikaar.module.customer.service.CustomerService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management APIs")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKER')")
    @Operation(summary = "Create customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(customerService.createCustomer(request), "Customer created"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKER', 'ANALYST')")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomer(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerById(id)));
    }

    @GetMapping("/phone/{phone}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKER')")
    @Operation(summary = "Get customer by phone")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerByPhone(phone)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKER', 'ANALYST')")
    @Operation(summary = "List all customers")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success(
                customerService.getAllCustomers(page, size, sortBy, sortDir)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKER', 'ANALYST')")
    @Operation(summary = "Search customers")
    public ResponseEntity<ApiResponse<PagedResponse<CustomerResponse>>> searchCustomers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                customerService.searchCustomers(q, page, size)));
    }

    @GetMapping("/top")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(summary = "Get top customers by spend")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getTopCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getTopCustomers(limit)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKER')")
    @Operation(summary = "Update customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @PathVariable UUID id, @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                customerService.updateCustomer(id, request), "Customer updated"));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate customer")
    public ResponseEntity<ApiResponse<Void>> deactivateCustomer(@PathVariable UUID id) {
        customerService.deactivateCustomer(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Customer deactivated"));
    }
}
