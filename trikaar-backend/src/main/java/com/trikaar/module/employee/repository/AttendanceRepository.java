package com.trikaar.module.employee.repository;

import com.trikaar.module.employee.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    Page<Attendance> findAllByEmployeeIdAndBusinessIdAndDeletedFalse(
            UUID employeeId, UUID businessId, Pageable pageable);

    List<Attendance> findAllByEmployeeIdAndBusinessIdAndAttendanceDateBetweenAndDeletedFalse(
            UUID employeeId, UUID businessId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employeeId = :employeeId " +
            "AND a.businessId = :businessId AND a.attendanceDate BETWEEN :startDate AND :endDate " +
            "AND a.status = 'PRESENT' AND a.deleted = false")
    long countPresentDays(@Param("employeeId") UUID employeeId,
            @Param("businessId") UUID businessId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(a.overtimeHours), 0) FROM Attendance a " +
            "WHERE a.employeeId = :employeeId AND a.businessId = :businessId " +
            "AND a.attendanceDate BETWEEN :startDate AND :endDate AND a.deleted = false")
    double sumOvertimeHours(@Param("employeeId") UUID employeeId,
            @Param("businessId") UUID businessId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
