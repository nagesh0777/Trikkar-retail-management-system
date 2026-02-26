package com.trikaar.module.admin.service.impl;

import com.trikaar.module.admin.dto.AdminConfigRequest;
import com.trikaar.module.admin.dto.AdminConfigResponse;
import com.trikaar.module.admin.entity.AdminConfig;
import com.trikaar.module.admin.repository.AdminConfigRepository;
import com.trikaar.module.admin.service.AdminConfigService;
import com.trikaar.module.audit.entity.AuditLog;
import com.trikaar.module.audit.service.AuditService;
import com.trikaar.shared.context.TenantContext;
import com.trikaar.shared.exception.BusinessRuleException;
import com.trikaar.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminConfigServiceImpl implements AdminConfigService {

    private final AdminConfigRepository adminConfigRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public AdminConfigResponse createOrUpdateConfig(AdminConfigRequest request) {
        UUID businessId = TenantContext.getBusinessId();

        AdminConfig config = adminConfigRepository
                .findByConfigKeyAndBusinessIdAndDeletedFalse(request.getConfigKey(), businessId)
                .orElse(null);

        String oldValue = null;

        if (config != null) {
            if (!config.isEditable()) {
                throw new BusinessRuleException("CONFIG_NOT_EDITABLE",
                        "Configuration '" + request.getConfigKey() + "' is not editable");
            }
            oldValue = config.getConfigValue();
            config.setConfigValue(request.getConfigValue());
            config.setDescription(request.getDescription());
            if (request.getDataType() != null) {
                config.setDataType(request.getDataType());
            }
        } else {
            config = AdminConfig.builder()
                    .configKey(request.getConfigKey())
                    .configValue(request.getConfigValue())
                    .category(request.getCategory())
                    .description(request.getDescription())
                    .dataType(request.getDataType() != null ? request.getDataType() : "STRING")
                    .build();
            config.setBusinessId(businessId);
        }

        config = adminConfigRepository.save(config);

        auditService.logAction(
                AuditLog.AuditAction.CONFIG_CHANGED,
                "AdminConfig",
                config.getId(),
                "Config '" + request.getConfigKey() + "' updated",
                oldValue, request.getConfigValue());

        log.info("Admin config '{}' = '{}' (category: {})",
                config.getConfigKey(), config.getConfigValue(), config.getCategory());

        return mapToResponse(config);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminConfigResponse> getAllConfigs() {
        UUID businessId = TenantContext.getBusinessId();
        return adminConfigRepository.findAllByBusinessIdAndDeletedFalse(businessId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminConfigResponse> getConfigsByCategory(String category) {
        UUID businessId = TenantContext.getBusinessId();
        return adminConfigRepository.findAllByBusinessIdAndCategoryAndDeletedFalse(businessId, category)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminConfigResponse getConfigByKey(String key) {
        UUID businessId = TenantContext.getBusinessId();
        AdminConfig config = adminConfigRepository.findByConfigKeyAndBusinessIdAndDeletedFalse(key, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("AdminConfig", "key", key));
        return mapToResponse(config);
    }

    @Override
    @Transactional(readOnly = true)
    public String getConfigValue(String key, String defaultValue) {
        UUID businessId = TenantContext.getBusinessId();
        return adminConfigRepository.findByConfigKeyAndBusinessIdAndDeletedFalse(key, businessId)
                .map(AdminConfig::getConfigValue)
                .orElse(defaultValue);
    }

    @Override
    @Transactional
    public void deleteConfig(String key) {
        UUID businessId = TenantContext.getBusinessId();
        AdminConfig config = adminConfigRepository.findByConfigKeyAndBusinessIdAndDeletedFalse(key, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("AdminConfig", "key", key));
        config.setDeleted(true);
        adminConfigRepository.save(config);
        log.info("Admin config '{}' deleted", key);
    }

    private AdminConfigResponse mapToResponse(AdminConfig config) {
        return AdminConfigResponse.builder()
                .id(config.getId().toString())
                .configKey(config.getConfigKey())
                .configValue(config.getConfigValue())
                .category(config.getCategory())
                .description(config.getDescription())
                .dataType(config.getDataType())
                .editable(config.isEditable())
                .build();
    }
}
