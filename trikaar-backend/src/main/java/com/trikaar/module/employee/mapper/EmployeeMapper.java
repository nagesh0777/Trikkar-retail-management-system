package com.trikaar.module.employee.mapper;

import com.trikaar.module.employee.dto.EmployeeRequest;
import com.trikaar.module.employee.dto.EmployeeResponse;
import com.trikaar.module.employee.entity.Employee;
import org.springframework.stereotype.Component;

/**
 * Manual mapper for Employee entity ↔ DTO transformations.
 */
@Component
public class EmployeeMapper {

    public EmployeeResponse toResponse(Employee employee) {
        if (employee == null)
            return null;

        return EmployeeResponse.builder()
                .id(employee.getId().toString())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .address(employee.getAddress())
                .dateOfBirth(employee.getDateOfBirth())
                .dateOfJoining(employee.getDateOfJoining())
                .dateOfLeaving(employee.getDateOfLeaving())
                .department(employee.getDepartment())
                .designation(employee.getDesignation())
                .employmentType(employee.getEmploymentType() != null ? employee.getEmploymentType().name() : null)
                .wageType(employee.getWageType() != null ? employee.getWageType().name() : null)
                .baseSalary(employee.getBaseSalary())
                .hourlyRate(employee.getHourlyRate())
                .dailyRate(employee.getDailyRate())
                .salesIncentivePercentage(employee.getSalesIncentivePercentage())
                .status(employee.getStatus() != null ? employee.getStatus().name() : null)
                .bankAccountNumber(employee.getBankAccountNumber())
                .panNumber(employee.getPanNumber())
                .emergencyContactName(employee.getEmergencyContactName())
                .emergencyContactPhone(employee.getEmergencyContactPhone())
                .createdAt(employee.getCreatedAt())
                .build();
    }

    public Employee toEntity(EmployeeRequest request) {
        if (request == null)
            return null;

        Employee employee = new Employee();
        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setDateOfJoining(request.getDateOfJoining());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setEmploymentType(request.getEmploymentType());
        employee.setWageType(request.getWageType());
        employee.setBaseSalary(request.getBaseSalary());
        employee.setHourlyRate(request.getHourlyRate());
        employee.setDailyRate(request.getDailyRate());
        employee.setSalesIncentivePercentage(request.getSalesIncentivePercentage());
        employee.setBankAccountNumber(request.getBankAccountNumber());
        employee.setIfscCode(request.getIfscCode());
        employee.setPanNumber(request.getPanNumber());
        employee.setAadharNumber(request.getAadharNumber());
        employee.setEmergencyContactName(request.getEmergencyContactName());
        employee.setEmergencyContactPhone(request.getEmergencyContactPhone());
        return employee;
    }

    public void updateEntity(EmployeeRequest request, Employee employee) {
        if (request == null || employee == null)
            return;

        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setAddress(request.getAddress());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setDateOfJoining(request.getDateOfJoining());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());
        employee.setEmploymentType(request.getEmploymentType());
        employee.setWageType(request.getWageType());
        employee.setBaseSalary(request.getBaseSalary());
        employee.setHourlyRate(request.getHourlyRate());
        employee.setDailyRate(request.getDailyRate());
        employee.setSalesIncentivePercentage(request.getSalesIncentivePercentage());
        employee.setBankAccountNumber(request.getBankAccountNumber());
        employee.setIfscCode(request.getIfscCode());
        employee.setPanNumber(request.getPanNumber());
        employee.setAadharNumber(request.getAadharNumber());
        employee.setEmergencyContactName(request.getEmergencyContactName());
        employee.setEmergencyContactPhone(request.getEmergencyContactPhone());
    }
}
