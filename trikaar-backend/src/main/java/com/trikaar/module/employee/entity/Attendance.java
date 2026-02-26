package com.trikaar.module.employee.entity;

import com.trikaar.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Attendance record for an employee.
 * Tracks daily check-in/check-out and overtime hours.
 */
@Entity
@Table(name = "attendance", uniqueConstraints = {
        @UniqueConstraint(name = "uk_attendance_employee_date", columnNames = { "employee_id", "attendance_date",
                "business_id" })
}, indexes = {
        @Index(name = "idx_attendance_business_id", columnList = "business_id"),
        @Index(name = "idx_attendance_employee_id", columnList = "employee_id"),
        @Index(name = "idx_attendance_date", columnList = "attendance_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(name = "overtime_hours", precision = 5)
    private Double overtimeHours;

    @Column(name = "notes", length = 500)
    private String notes;

    public enum AttendanceStatus {
        PRESENT, ABSENT, HALF_DAY, LEAVE, HOLIDAY
    }
}
