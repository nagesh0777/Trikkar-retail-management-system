package com.trikaar.module.salary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryPayoutResponse {

    private String id;
    private String employeeId;
    private String employeeName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal baseAmount;
    private Integer daysWorked;
    private BigDecimal hoursWorked;
    private BigDecimal overtimeHours;
    private BigDecimal overtimeAmount;
    private BigDecimal salesAmount;
    private BigDecimal incentivePercentage;
    private BigDecimal incentiveAmount;
    private BigDecimal bonus;
    private BigDecimal deductions;
    private String deductionReason;
    private BigDecimal grossSalary;
    private BigDecimal netSalary;
    private String status;
    private String paymentReference;
    private LocalDate paidOn;
    private String notes;
}
