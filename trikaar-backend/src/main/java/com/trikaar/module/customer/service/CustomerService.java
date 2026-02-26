package com.trikaar.module.customer.service;

import com.trikaar.module.customer.dto.CustomerRequest;
import com.trikaar.module.customer.dto.CustomerResponse;
import com.trikaar.shared.dto.PagedResponse;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest request);

    CustomerResponse getCustomerById(UUID customerId);

    CustomerResponse getCustomerByPhone(String phone);

    PagedResponse<CustomerResponse> getAllCustomers(int page, int size, String sortBy, String sortDir);

    PagedResponse<CustomerResponse> searchCustomers(String search, int page, int size);

    CustomerResponse updateCustomer(UUID customerId, CustomerRequest request);

    void deactivateCustomer(UUID customerId);

    List<CustomerResponse> getTopCustomers(int limit);
}
