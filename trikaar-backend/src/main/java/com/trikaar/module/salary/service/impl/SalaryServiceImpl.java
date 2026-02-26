package com.trikaar.module.salary.service.impl;

import com.trikaar.module.audit.entity.AuditLog;
import com.trikaar.module.audit.service.AuditService;
import com.trikaar.module.employee.entity.Employee;
import com.trikaar.module.employee.repository.AttendanceRepository;
import com.trikaar.module.employee.repository.EmployeeRepository;
import com.trikaar.module.salary.dto.GenerateSalaryRequest;
import com.trikaar.module.salary.dto.SalaryPayoutResponse;
import com.trikaar.module.salary.entity.SalaryPayout;
import com.trikaar.module.salary.repository.SalaryPayoutRepository;
import com.trikaar.module.salary.service.SalaryService;
import com.trikaar.module.sales.repository.SaleRepository;
import com.trikaar.shared.context.TenantContext;
import com.trikaar.shared.dto.PagedResponse;
import com.trikaar.shared.exception.BusinessRuleException;
import com.trikaar.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryServiceImpl implements SalaryService {

    private final SalaryPayoutRepository salaryPayoutRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final SaleRepository saleRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public SalaryPayoutResponse generateSalary(GenerateSalaryRequest request) {
        UUID businessId = TenantContext.getBusinessId();

        Employee employee = employeeRepository.findByIdAndBusinessIdAndDeletedFalse(
                request.getEmployeeId(), businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        // 1. Calculate base amount based on wage type
        BigDecimal baseAmount;
        int daysWorked = 0;
        BigDecimal hoursWorked = BigDecimal.ZERO;

        switch (employee.getWageType()) {
            case MONTHLY -> baseAmount = employee.getBaseSalary();
            case DAILY -> {
                long presentDays = attendanceRepository.countPresentDays(
                        employee.getId(), businessId, request.getPeriodStart(), request.getPeriodEnd());
                daysWorked = (int) presentDays;
                baseAmount = employee.getDailyRate().multiply(BigDecimal.valueOf(presentDays));
            }
            case HOURLY -> {
                // For hourly, we'd sum actual hours from attendance
                long presentDays2 = attendanceRepository.countPresentDays(
                        employee.getId(), businessId, request.getPeriodStart(), request.getPeriodEnd());
                daysWorked = (int) presentDays2;
                hoursWorked = BigDecimal.valueOf(presentDays2 * 8); // Assume 8-hour days
                baseAmount = employee.getHourlyRate().multiply(hoursWorked);
            }
            default -> throw new BusinessRuleException("INVALID_WAGE_TYPE",
                    "Unknown wage type: " + employee.getWageType());
        }

        // 2. Calculate overtime
        double overtimeHrs = attendanceRepository.sumOvertimeHours(
                employee.getId(), businessId, request.getPeriodStart(), request.getPeriodEnd());
        BigDecimal overtimeHours = BigDecimal.valueOf(overtimeHrs);
        BigDecimal overtimeRate = employee.getHourlyRate() != null
                ? employee.getHourlyRate().multiply(new BigDecimal("1.5"))
                : employee.getBaseSalary().divide(BigDecimal.valueOf(240), 2, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("1.5"));
        BigDecimal overtimeAmount = overtimeRate.multiply(overtimeHours);

        // 3. Calculate sales-based incentive
        BigDecimal salesAmount = BigDecimal.ZERO;
        BigDecimal incentiveAmount = BigDecimal.ZERO;
        BigDecimal incentivePercentage = employee.getSalesIncentivePercentage() != null
                ? employee.getSalesIncentivePercentage()
                : BigDecimal.ZERO;

        if (incentivePercentage.compareTo(BigDecimal.ZERO) > 0) {
            salesAmount = saleRepository.calculateEmployeeSales(
                    businessId, employee.getId(),
                    request.getPeriodStart().atStartOfDay(),
                    request.getPeriodEnd().plusDays(1).atStartOfDay());
            incentiveAmount = salesAmount.multiply(incentivePercentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        // 4. Apply bonus and deductions
        BigDecimal bonus = request.getBonus() != null ? request.getBonus() : BigDecimal.ZERO;
        BigDecimal deductions = request.getDeductions() != null ? request.getDeductions() : BigDecimal.ZERO;

        // 5. Calculate totals
        BigDecimal grossSalary = baseAmount.add(overtimeAmount).add(incentiveAmount).add(bonus);
        BigDecimal netSalary = grossSalary.subtract(deductions);

        if (netSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("NEGATIVE_SALARY",
                    "Net salary cannot be negative. Deductions exceed earnings.");
        }

        // 6. Create payout record
        SalaryPayout payout = SalaryPayout.builder()
                .employeeId(employee.getId())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .baseAmount(baseAmount)
                .daysWorked(daysWorked)
                .hoursWorked(hoursWorked)
                .overtimeHours(overtimeHours)
                .overtimeAmount(overtimeAmount)
                .salesAmount(salesAmount)
                .incentivePercentage(incentivePercentage)
                .incentiveAmount(incentiveAmount)
                .bonus(bonus)
                .deductions(deductions)
                .deductionReason(request.getDeductionReason())
                .grossSalary(grossSalary)
                .netSalary(netSalary)
                .notes(request.getNotes())
                .build();
        payout.setBusinessId(businessId);
        payout = salaryPayoutRepository.save(payout);

        log.info("Salary generated for '{}': Net={}, Gross={}, Period={} to {}",
                employee.getFullName(), netSalary, grossSalary,
                request.getPeriodStart(), request.getPeriodEnd());

        return mapToResponse(payout, employee.getFullName());
    }

    @Override
    @Transactional
    public SalaryPayoutResponse approvePayout(UUID payoutId) {
        UUID businessId = TenantContext.getBusinessId();
        SalaryPayout payout = findPayout(payoutId, businessId);

        if (payout.getStatus() != SalaryPayout.PayoutStatus.PENDING) {
            throw new BusinessRuleException("INVALID_STATUS",
                    "Only PENDING payouts can be approved");
        }

        payout.setStatus(SalaryPayout.PayoutStatus.APPROVED);
        payout = salaryPayoutRepository.save(payout);

        Employee employee = employeeRepository.findByIdAndBusinessIdAndDeletedFalse(
                payout.getEmployeeId(), businessId).orElse(null);
        return mapToResponse(payout, employee != null ? employee.getFullName() : "Unknown");
    }

    @Override
    @Transactional
    public SalaryPayoutResponse markAsPaid(UUID payoutId, String paymentReference) {
        UUID businessId = TenantContext.getBusinessId();
        SalaryPayout payout = findPayout(payoutId, businessId);

        if (payout.getStatus() != SalaryPayout.PayoutStatus.APPROVED) {
            throw new BusinessRuleException("INVALID_STATUS",
                    "Only APPROVED payouts can be marked as paid");
        }

        payout.setStatus(SalaryPayout.PayoutStatus.PAID);
        payout.setPaymentReference(paymentReference);
        payout.setPaidOn(LocalDate.now());
        payout = salaryPayoutRepository.save(payout);

        // Audit log for salary payout
        auditService.logAction(
                AuditLog.AuditAction.SALARY_PAYOUT,
                "SalaryPayout",
                payout.getId(),
                "Salary paid: Employee=" + payout.getEmployeeId()
                        + " | Net=" + payout.getNetSalary()
                        + " | Ref=" + paymentReference);

        Employee employee = employeeRepository.findByIdAndBusinessIdAndDeletedFalse(
                payout.getEmployeeId(), businessId).orElse(null);

        log.info("Salary payout '{}' marked as paid. Ref: {}", payoutId, paymentReference);
        return mapToResponse(payout, employee != null ? employee.getFullName() : "Unknown");
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SalaryPayoutResponse> getPayoutsByEmployee(UUID employeeId, int page, int size) {
        UUID businessId = TenantContext.getBusinessId();
        Page<SalaryPayout> payoutPage = salaryPayoutRepository
                .findAllByEmployeeIdAndBusinessIdAndDeletedFalse(
                        employeeId, businessId,
                        PageRequest.of(page, size, Sort.by("periodStart").descending()));
        return buildPagedResponse(payoutPage, businessId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SalaryPayoutResponse> getAllPayouts(int page, int size) {
        UUID businessId = TenantContext.getBusinessId();
        Page<SalaryPayout> payoutPage = salaryPayoutRepository
                .findAllByBusinessIdAndDeletedFalse(businessId,
                        PageRequest.of(page, size, Sort.by("periodStart").descending()));
        return buildPagedResponse(payoutPage, businessId);
    }

    // ═══════════════════ Private Helpers ═══════════════════

    private SalaryPayout findPayout(UUID payoutId, UUID businessId) {
        return salaryPayoutRepository.findById(payoutId)
                .filter(p -> p.getBusinessId().equals(businessId) && !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("SalaryPayout", "id", payoutId));
    }

    private SalaryPayoutResponse mapToResponse(SalaryPayout payout, String employeeName) {
        return SalaryPayoutResponse.builder()
                .id(payout.getId().toString())
                .employeeId(payout.getEmployeeId().toString())
                .employeeName(employeeName)
                .periodStart(payout.getPeriodStart())
                .periodEnd(payout.getPeriodEnd())
                .baseAmount(payout.getBaseAmount())
                .daysWorked(payout.getDaysWorked())
                .hoursWorked(payout.getHoursWorked())
                .overtimeHours(payout.getOvertimeHours())
                .overtimeAmount(payout.getOvertimeAmount())
                .salesAmount(payout.getSalesAmount())
                .incentivePercentage(payout.getIncentivePercentage())
                .incentiveAmount(payout.getIncentiveAmount())
                .bonus(payout.getBonus())
                .deductions(payout.getDeductions())
                .deductionReason(payout.getDeductionReason())
                .grossSalary(payout.getGrossSalary())
                .netSalary(payout.getNetSalary())
                .status(payout.getStatus().name())
                .paymentReference(payout.getPaymentReference())
                .paidOn(payout.getPaidOn())
                .notes(payout.getNotes())
                .build();
    }

    private PagedResponse<SalaryPayoutResponse> buildPagedResponse(
            Page<SalaryPayout> page, UUID businessId) {
        return PagedResponse.<SalaryPayoutResponse>builder()
                .content(page.getContent().stream().map(p -> {
                    Employee emp = employeeRepository.findByIdAndBusinessIdAndDeletedFalse(
                            p.getEmployeeId(), businessId).orElse(null);
                    return mapToResponse(p, emp != null ? emp.getFullName() : "Unknown");
                }).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
