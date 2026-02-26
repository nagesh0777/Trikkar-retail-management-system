package com.trikaar.module.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private String id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private LocalDate dateOfBirth;
    private LocalDate dateOfJoining;
    private LocalDate dateOfLeaving;
    private String department;
    private String designation;
    private String employmentType;
    private String wageType;
    private BigDecimal baseSalary;
    private BigDecimal hourlyRate;
    private BigDecimal dailyRate;
    private BigDecimal salesIncentivePercentage;
    private String status;
    private String bankAccountNumber;
    private String panNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private LocalDateTime createdAt;
}
