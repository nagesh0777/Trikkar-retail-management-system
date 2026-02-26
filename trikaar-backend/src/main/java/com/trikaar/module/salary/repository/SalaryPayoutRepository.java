package com.trikaar.module.salary.repository;

import com.trikaar.module.salary.entity.SalaryPayout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface SalaryPayoutRepository extends JpaRepository<SalaryPayout, UUID> {

    Page<SalaryPayout> findAllByBusinessIdAndDeletedFalse(UUID businessId, Pageable pageable);

    Page<SalaryPayout> findAllByEmployeeIdAndBusinessIdAndDeletedFalse(
            UUID employeeId, UUID businessId, Pageable pageable);

    List<SalaryPayout> findAllByEmployeeIdAndBusinessIdAndPeriodStartGreaterThanEqualAndPeriodEndLessThanEqualAndDeletedFalse(
            UUID employeeId, UUID businessId, LocalDate periodStart, LocalDate periodEnd);

    @Query("SELECT COALESCE(SUM(sp.netSalary), 0) FROM SalaryPayout sp " +
            "WHERE sp.businessId = :businessId AND sp.status = 'PAID' " +
            "AND sp.periodStart >= :start AND sp.periodEnd <= :end AND sp.deleted = false")
    BigDecimal calculateTotalPayouts(@Param("businessId") UUID businessId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);
}
