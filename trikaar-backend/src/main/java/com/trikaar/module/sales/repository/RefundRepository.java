package com.trikaar.module.sales.repository;

import com.trikaar.module.sales.entity.Refund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {

    Page<Refund> findAllByBusinessIdAndDeletedFalse(UUID businessId, Pageable pageable);

    Page<Refund> findAllByOriginalSaleIdAndBusinessIdAndDeletedFalse(
            UUID originalSaleId, UUID businessId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(r.refundAmount), 0) FROM Refund r " +
            "WHERE r.businessId = :businessId AND r.refundDate BETWEEN :startDate AND :endDate " +
            "AND r.deleted = false")
    BigDecimal calculateTotalRefunds(@Param("businessId") UUID businessId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
