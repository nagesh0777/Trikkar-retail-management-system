package com.trikaar.module.employee.controller;

import com.trikaar.module.employee.dto.EmployeeRequest;
import com.trikaar.module.employee.dto.EmployeeResponse;
import com.trikaar.module.employee.service.EmployeeService;
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
@RequestMapping("/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee management APIs")
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create employee")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Employee created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'ANALYST')")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeeById(id)));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'ANALYST')")
    @Operation(summary = "Get employee by code")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.getEmployeeByCode(code)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'ANALYST')")
    @Operation(summary = "List all employees with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<EmployeeResponse>>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "firstName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success(
                employeeService.getAllEmployees(page, size, sortBy, sortDir)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'ANALYST')")
    @Operation(summary = "Search employees by name or code")
    public ResponseEntity<ApiResponse<PagedResponse<EmployeeResponse>>> searchEmployees(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                employeeService.searchEmployees(q, page, size)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update employee")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable UUID id, @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                employeeService.updateEmployee(id, request), "Employee updated successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate employee")
    public ResponseEntity<ApiResponse<Void>> deactivateEmployee(@PathVariable UUID id) {
        employeeService.deactivateEmployee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Employee deactivated"));
    }

    @PatchMapping("/{id}/terminate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Terminate employee")
    public ResponseEntity<ApiResponse<Void>> terminateEmployee(@PathVariable UUID id) {
        employeeService.terminateEmployee(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Employee terminated"));
    }
}
