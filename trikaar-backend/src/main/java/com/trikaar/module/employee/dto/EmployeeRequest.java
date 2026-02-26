package com.trikaar.module.employee.dto;

import com.trikaar.module.employee.entity.Employee;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "Employee code is required")
    @Size(max = 50, message = "Employee code cannot exceed 50 characters")
    private String employeeCode;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @Email(message = "Invalid email format")
    private String email;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String phone;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;

    private LocalDate dateOfBirth;

    @NotNull(message = "Date of joining is required")
    private LocalDate dateOfJoining;

    @Size(max = 100)
    private String department;

    @Size(max = 100)
    private String designation;

    @NotNull(message = "Employment type is required")
    private Employee.EmploymentType employmentType;

    @NotNull(message = "Wage type is required")
    private Employee.WageType wageType;

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", message = "Base salary must be non-negative")
    private BigDecimal baseSalary;

    @DecimalMin(value = "0.0", message = "Hourly rate must be non-negative")
    private BigDecimal hourlyRate;

    @DecimalMin(value = "0.0", message = "Daily rate must be non-negative")
    private BigDecimal dailyRate;

    @DecimalMin(value = "0.0", message = "Sales incentive percentage must be non-negative")
    @DecimalMax(value = "100.0", message = "Sales incentive percentage cannot exceed 100")
    private BigDecimal salesIncentivePercentage;

    private String bankAccountNumber;
    private String ifscCode;
    private String panNumber;
    private String aadharNumber;
    private String emergencyContactName;
    private String emergencyContactPhone;
}
