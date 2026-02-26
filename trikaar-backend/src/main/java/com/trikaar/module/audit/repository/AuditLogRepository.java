package com.trikaar.module.audit.repository;

import com.trikaar.module.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findAllByBusinessIdAndDeletedFalse(UUID businessId, Pageable pageable);

    Page<AuditLog> findAllByBusinessIdAndActionAndDeletedFalse(
            UUID businessId, AuditLog.AuditAction action, Pageable pageable);

    Page<AuditLog> findAllByBusinessIdAndEntityTypeAndEntityIdAndDeletedFalse(
            UUID businessId, String entityType, UUID entityId, Pageable pageable);

    Page<AuditLog> findAllByBusinessIdAndCreatedAtBetweenAndDeletedFalse(
            UUID businessId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<AuditLog> findAllByBusinessIdAndPerformedByAndDeletedFalse(
            UUID businessId, UUID performedBy, Pageable pageable);
}
