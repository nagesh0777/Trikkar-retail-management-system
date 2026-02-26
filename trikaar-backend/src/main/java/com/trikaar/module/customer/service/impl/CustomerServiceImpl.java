package com.trikaar.module.customer.service.impl;

import com.trikaar.module.customer.dto.CustomerRequest;
import com.trikaar.module.customer.dto.CustomerResponse;
import com.trikaar.module.customer.entity.Customer;
import com.trikaar.module.customer.mapper.CustomerMapper;
import com.trikaar.module.customer.repository.CustomerRepository;
import com.trikaar.module.customer.service.CustomerService;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        UUID businessId = TenantContext.getBusinessId();

        if (customerRepository.existsByPhoneAndBusinessIdAndDeletedFalse(request.getPhone(), businessId)) {
            throw new DuplicateResourceException("Customer", "phone", request.getPhone());
        }

        if (request.getEmail() != null &&
                customerRepository.existsByEmailAndBusinessIdAndDeletedFalse(request.getEmail(), businessId)) {
            throw new DuplicateResourceException("Customer", "email", request.getEmail());
        }

        Customer customer = customerMapper.toEntity(request);
        customer.setBusinessId(businessId);
        customer = customerRepository.save(customer);

        log.info("Customer '{}' created with phone '{}'", customer.getFullName(), customer.getPhone());
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID customerId) {
        UUID businessId = TenantContext.getBusinessId();
        Customer customer = customerRepository.findByIdAndBusinessIdAndDeletedFalse(customerId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByPhone(String phone) {
        UUID businessId = TenantContext.getBusinessId();
        Customer customer = customerRepository.findByPhoneAndBusinessIdAndDeletedFalse(phone, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "phone", phone));
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponse> getAllCustomers(int page, int size, String sortBy, String sortDir) {
        UUID businessId = TenantContext.getBusinessId();
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Page<Customer> customerPage = customerRepository.findAllByBusinessIdAndDeletedFalse(
                businessId, PageRequest.of(page, size, sort));
        return buildPagedResponse(customerPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponse> searchCustomers(String search, int page, int size) {
        UUID businessId = TenantContext.getBusinessId();
        Page<Customer> customerPage = customerRepository.searchCustomers(
                businessId, search, PageRequest.of(page, size));
        return buildPagedResponse(customerPage);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(UUID customerId, CustomerRequest request) {
        UUID businessId = TenantContext.getBusinessId();
        Customer customer = customerRepository.findByIdAndBusinessIdAndDeletedFalse(customerId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
        customerMapper.updateEntity(request, customer);
        customer = customerRepository.save(customer);
        log.info("Customer '{}' updated", customer.getPhone());
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public void deactivateCustomer(UUID customerId) {
        UUID businessId = TenantContext.getBusinessId();
        Customer customer = customerRepository.findByIdAndBusinessIdAndDeletedFalse(customerId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
        customer.setActive(false);
        customerRepository.save(customer);
        log.info("Customer '{}' deactivated", customer.getPhone());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getTopCustomers(int limit) {
        UUID businessId = TenantContext.getBusinessId();
        return customerRepository.findTopCustomers(businessId, PageRequest.of(0, limit))
                .stream().map(customerMapper::toResponse).toList();
    }

    private PagedResponse<CustomerResponse> buildPagedResponse(Page<Customer> page) {
        return PagedResponse.<CustomerResponse>builder()
                .content(page.getContent().stream().map(customerMapper::toResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
