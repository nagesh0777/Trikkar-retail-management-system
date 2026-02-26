package com.trikaar.module.salary.entity;

import com.trikaar.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Salary payout record.
 * Formula: Salary = Base + Overtime + Incentives - Deductions
 *
 * Supports: Monthly salary, Daily wage, Hourly wage, Sales-based incentive %
 */
@Entity
@Table(name = "salary_payouts", indexes = {
        @Index(name = "idx_salary_business_id", columnList = "business_id"),
        @Index(name = "idx_salary_employee_id", columnList = "employee_id"),
        @Index(name = "idx_salary_period", columnList = "period_start, period_end"),
        @Index(name = "idx_salary_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryPayout extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    // ── Earnings Breakdown ─────────────────────────────────────
    @Column(name = "base_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "days_worked")
    private Integer daysWorked;

    @Column(name = "hours_worked", precision = 8, scale = 2)
    private BigDecimal hoursWorked;

    @Column(name = "overtime_hours", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal overtimeHours = BigDecimal.ZERO;

    @Column(name = "overtime_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal overtimeAmount = BigDecimal.ZERO;

    @Column(name = "sales_amount", precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal salesAmount = BigDecimal.ZERO;

    @Column(name = "incentive_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal incentivePercentage = BigDecimal.ZERO;

    @Column(name = "incentive_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal incentiveAmount = BigDecimal.ZERO;

    @Column(name = "bonus", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal bonus = BigDecimal.ZERO;

    // ── Deductions ─────────────────────────────────────────────
    @Column(name = "deductions", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal deductions = BigDecimal.ZERO;

    @Column(name = "deduction_reason", length = 500)
    private String deductionReason;

    // ── Totals ─────────────────────────────────────────────────
    @Column(name = "gross_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal grossSalary;

    @Column(name = "net_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal netSalary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PayoutStatus status = PayoutStatus.PENDING;

    @Column(name = "payment_reference", length = 100)
    private String paymentReference;

    @Column(name = "paid_on")
    private LocalDate paidOn;

    @Column(name = "notes", length = 1000)
    private String notes;

    public enum PayoutStatus {
        PENDING, APPROVED, PAID, CANCELLED
    }
}
