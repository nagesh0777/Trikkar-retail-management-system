package com.trikaar.module.audit.controller;

import com.trikaar.module.audit.entity.AuditLog;
import com.trikaar.module.audit.repository.AuditLogRepository;
import com.trikaar.shared.context.TenantContext;
import com.trikaar.shared.dto.ApiResponse;
import com.trikaar.shared.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit Logs", description = "Audit trail APIs - Admin only")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @Operation(summary = "List all audit logs")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID businessId = TenantContext.getBusinessId();
        Page<AuditLog> logPage = auditLogRepository.findAllByBusinessIdAndDeletedFalse(
                businessId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(buildPagedResponse(logPage)));
    }

    @GetMapping("/by-action/{action}")
    @Operation(summary = "Get audit logs by action type")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> getByAction(
            @PathVariable AuditLog.AuditAction action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID businessId = TenantContext.getBusinessId();
        Page<AuditLog> logPage = auditLogRepository.findAllByBusinessIdAndActionAndDeletedFalse(
                businessId, action, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(buildPagedResponse(logPage)));
    }

    @GetMapping("/by-entity/{entityType}/{entityId}")
    @Operation(summary = "Get audit logs for a specific entity")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> getByEntity(
            @PathVariable String entityType,
            @PathVariable UUID entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID businessId = TenantContext.getBusinessId();
        Page<AuditLog> logPage = auditLogRepository
                .findAllByBusinessIdAndEntityTypeAndEntityIdAndDeletedFalse(
                        businessId, entityType, entityId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(buildPagedResponse(logPage)));
    }

    @GetMapping("/by-date-range")
    @Operation(summary = "Get audit logs within a date range")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        UUID businessId = TenantContext.getBusinessId();
        Page<AuditLog> logPage = auditLogRepository
                .findAllByBusinessIdAndCreatedAtBetweenAndDeletedFalse(
                        businessId, start, end,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(buildPagedResponse(logPage)));
    }

    private PagedResponse<AuditLog> buildPagedResponse(Page<AuditLog> page) {
        return PagedResponse.<AuditLog>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
