package com.trikaar.module.salary.service;

import com.trikaar.module.salary.dto.GenerateSalaryRequest;
import com.trikaar.module.salary.dto.SalaryPayoutResponse;
import com.trikaar.shared.dto.PagedResponse;

import java.util.UUID;

public interface SalaryService {

    SalaryPayoutResponse generateSalary(GenerateSalaryRequest request);

    SalaryPayoutResponse approvePayout(UUID payoutId);

    SalaryPayoutResponse markAsPaid(UUID payoutId, String paymentReference);

    PagedResponse<SalaryPayoutResponse> getPayoutsByEmployee(UUID employeeId, int page, int size);

    PagedResponse<SalaryPayoutResponse> getAllPayouts(int page, int size);
}
