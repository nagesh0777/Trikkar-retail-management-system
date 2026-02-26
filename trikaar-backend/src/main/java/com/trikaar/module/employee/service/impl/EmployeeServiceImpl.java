package com.trikaar.module.employee.service.impl;

import com.trikaar.module.employee.dto.EmployeeRequest;
import com.trikaar.module.employee.dto.EmployeeResponse;
import com.trikaar.module.employee.entity.Employee;
import com.trikaar.module.employee.mapper.EmployeeMapper;
import com.trikaar.module.employee.repository.EmployeeRepository;
import com.trikaar.module.employee.service.EmployeeService;
import com.trikaar.shared.context.TenantContext;
import com.trikaar.shared.dto.PagedResponse;
import com.trikaar.shared.exception.DuplicateResourceException;
import com.trikaar.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        UUID businessId = TenantContext.getBusinessId();

        if (employeeRepository.existsByEmployeeCodeAndBusinessIdAndDeletedFalse(
                request.getEmployeeCode(), businessId)) {
            throw new DuplicateResourceException("Employee", "employeeCode", request.getEmployeeCode());
        }

        if (request.getEmail() != null &&
                employeeRepository.existsByEmailAndBusinessIdAndDeletedFalse(request.getEmail(), businessId)) {
            throw new DuplicateResourceException("Employee", "email", request.getEmail());
        }

        Employee employee = employeeMapper.toEntity(request);
        employee.setBusinessId(businessId);
        employee.setStatus(Employee.EmployeeStatus.ACTIVE);
        employee = employeeRepository.save(employee);

        log.info("Employee '{}' created with code '{}'", employee.getFullName(), employee.getEmployeeCode());
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeById(UUID employeeId) {
        UUID businessId = TenantContext.getBusinessId();
        Employee employee = employeeRepository.findByIdAndBusinessIdAndDeletedFalse(employeeId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployeeByCode(String employeeCode) {
        UUID businessId = TenantContext.getBusinessId();
        Employee employee = employeeRepository.findByEmployeeCodeAndBusinessIdAndDeletedFalse(
                employeeCode, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "code", employeeCode));
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EmployeeResponse> getAllEmployees(int page, int size, String sortBy, String sortDir) {
        UUID businessId = TenantContext.getBusinessId();
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Employee> employeePage = employeeRepository.findAllByBusinessIdAndDeletedFalse(businessId, pageRequest);
        return buildPagedResponse(employeePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<EmployeeResponse> searchEmployees(String search, int page, int size) {
        UUID businessId = TenantContext.getBusinessId();
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Employee> employeePage = employeeRepository.searchEmployees(businessId, search, pageRequest);
        return buildPagedResponse(employeePage);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(UUID employeeId, EmployeeRequest request) {
        UUID businessId = TenantContext.getBusinessId();
        Employee employee = employeeRepository.findByIdAndBusinessIdAndDeletedFalse(employeeId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        employeeMapper.updateEntity(request, employee);
        employee = employeeRepository.save(employee);

        log.info("Employee '{}' updated", employee.getEmployeeCode());
        return employeeMapper.toResponse(employee);
    }

    @Override
    @Transactional
    public void deactivateEmployee(UUID employeeId) {
        UUID businessId = TenantContext.getBusinessId();
        Employee employee = employeeRepository.findByIdAndBusinessIdAndDeletedFalse(employeeId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        employee.setStatus(Employee.EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);
        log.info("Employee '{}' deactivated", employee.getEmployeeCode());
    }

    @Override
    @Transactional
    public void terminateEmployee(UUID employeeId) {
        UUID businessId = TenantContext.getBusinessId();
        Employee employee = employeeRepository.findByIdAndBusinessIdAndDeletedFalse(employeeId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
        employee.setStatus(Employee.EmployeeStatus.TERMINATED);
        employee.setDateOfLeaving(java.time.LocalDate.now());
        employeeRepository.save(employee);
        log.info("Employee '{}' terminated", employee.getEmployeeCode());
    }

    private PagedResponse<EmployeeResponse> buildPagedResponse(Page<Employee> page) {
        return PagedResponse.<EmployeeResponse>builder()
                .content(page.getContent().stream().map(employeeMapper::toResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
