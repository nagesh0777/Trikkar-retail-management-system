package com.trikaar.module.audit.service.impl;

import com.trikaar.module.audit.entity.AuditLog;
import com.trikaar.module.audit.repository.AuditLogRepository;
import com.trikaar.module.audit.service.AuditService;
import com.trikaar.shared.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(AuditLog.AuditAction action, String entityType,
            UUID entityId, String description) {
        logAction(action, entityType, entityId, description, null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(AuditLog.AuditAction action, String entityType,
            UUID entityId, String description,
            String oldValue, String newValue) {
        try {
            UUID businessId = TenantContext.getBusinessId();
            UUID userId = TenantContext.getUserId();

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .description(description)
                    .performedBy(userId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build();
            auditLog.setBusinessId(businessId);

            auditLogRepository.save(auditLog);

            log.debug("Audit logged: [{}] {} on {}:{} - {}",
                    action, entityType, entityId, businessId, description);
        } catch (Exception e) {
            // Audit logging should NEVER break business operations
            log.error("Failed to write audit log: {} - {}", action, description, e);
        }
    }
}
