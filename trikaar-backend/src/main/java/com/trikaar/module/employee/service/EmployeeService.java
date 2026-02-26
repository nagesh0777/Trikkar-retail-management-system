package com.trikaar.module.employee.service;

import com.trikaar.module.employee.dto.EmployeeRequest;
import com.trikaar.module.employee.dto.EmployeeResponse;
import com.trikaar.shared.dto.PagedResponse;

import java.util.UUID;

public interface EmployeeService {

    EmployeeResponse createEmployee(EmployeeRequest request);

    EmployeeResponse getEmployeeById(UUID employeeId);

    EmployeeResponse getEmployeeByCode(String employeeCode);

    PagedResponse<EmployeeResponse> getAllEmployees(int page, int size, String sortBy, String sortDir);

    PagedResponse<EmployeeResponse> searchEmployees(String search, int page, int size);

    EmployeeResponse updateEmployee(UUID employeeId, EmployeeRequest request);

    void deactivateEmployee(UUID employeeId);

    void terminateEmployee(UUID employeeId);
}
