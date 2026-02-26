package com.trikaar.module.sales.repository;

import com.trikaar.module.sales.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {

    Page<Sale> findAllByBusinessIdAndDeletedFalse(UUID businessId, Pageable pageable);

    Optional<Sale> findByIdAndBusinessIdAndDeletedFalse(UUID id, UUID businessId);

    Optional<Sale> findByTransactionNumberAndBusinessIdAndDeletedFalse(
            String transactionNumber, UUID businessId);

    Page<Sale> findAllByEmployeeIdAndBusinessIdAndDeletedFalse(
            UUID employeeId, UUID businessId, Pageable pageable);

    Page<Sale> findAllByCustomerIdAndBusinessIdAndDeletedFalse(
            UUID customerId, UUID businessId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s " +
            "WHERE s.businessId = :businessId AND s.status = 'COMPLETED' " +
            "AND s.saleDate BETWEEN :startDate AND :endDate AND s.deleted = false")
    BigDecimal calculateRevenue(@Param("businessId") UUID businessId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.businessId = :businessId " +
            "AND s.status = 'COMPLETED' AND s.saleDate BETWEEN :startDate AND :endDate " +
            "AND s.deleted = false")
    long countSales(@Param("businessId") UUID businessId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s " +
            "WHERE s.businessId = :businessId AND s.status = 'COMPLETED' " +
            "AND s.employeeId = :employeeId AND s.saleDate BETWEEN :startDate AND :endDate " +
            "AND s.deleted = false")
    BigDecimal calculateEmployeeSales(@Param("businessId") UUID businessId,
            @Param("employeeId") UUID employeeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.businessId = :businessId " +
            "AND s.saleDate >= :startOfDay AND s.saleDate < :endOfDay " +
            "AND s.deleted = false")
    long countTodaySales(@Param("businessId") UUID businessId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);
}
