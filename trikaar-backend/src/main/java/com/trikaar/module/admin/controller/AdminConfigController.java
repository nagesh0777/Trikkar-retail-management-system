package com.trikaar.module.admin.controller;

import com.trikaar.module.admin.dto.AdminConfigRequest;
import com.trikaar.module.admin.dto.AdminConfigResponse;
import com.trikaar.module.admin.service.AdminConfigService;
import com.trikaar.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Config", description = "System configuration management - Admin only")
public class AdminConfigController {

    private final AdminConfigService adminConfigService;

    @PostMapping
    @Operation(summary = "Create or update a configuration")
    public ResponseEntity<ApiResponse<AdminConfigResponse>> createOrUpdate(
            @Valid @RequestBody AdminConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                adminConfigService.createOrUpdateConfig(request), "Configuration saved"));
    }

    @GetMapping
    @Operation(summary = "List all configurations")
    public ResponseEntity<ApiResponse<List<AdminConfigResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(adminConfigService.getAllConfigs()));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get configurations by category")
    public ResponseEntity<ApiResponse<List<AdminConfigResponse>>> getByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.success(
                adminConfigService.getConfigsByCategory(category)));
    }

    @GetMapping("/key/{key}")
    @Operation(summary = "Get configuration by key")
    public ResponseEntity<ApiResponse<AdminConfigResponse>> getByKey(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.success(adminConfigService.getConfigByKey(key)));
    }

    @DeleteMapping("/key/{key}")
    @Operation(summary = "Delete a configuration")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String key) {
        adminConfigService.deleteConfig(key);
        return ResponseEntity.ok(ApiResponse.success(null, "Configuration deleted"));
    }
}
