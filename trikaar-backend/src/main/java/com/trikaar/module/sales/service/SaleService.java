package com.trikaar.module.sales.service;

import com.trikaar.module.sales.dto.CreateSaleRequest;
import com.trikaar.module.sales.dto.RefundRequest;
import com.trikaar.module.sales.dto.SaleResponse;
import com.trikaar.shared.dto.PagedResponse;

import java.util.UUID;

public interface SaleService {

    SaleResponse createSale(CreateSaleRequest request);

    SaleResponse getSaleById(UUID saleId);

    SaleResponse getSaleByTransactionNumber(String transactionNumber);

    PagedResponse<SaleResponse> getAllSales(int page, int size, String sortBy, String sortDir);

    PagedResponse<SaleResponse> getSalesByEmployee(UUID employeeId, int page, int size);

    PagedResponse<SaleResponse> getSalesByCustomer(UUID customerId, int page, int size);

    SaleResponse processRefund(RefundRequest request);
}
