package com.trikaar.module.customer.mapper;

import com.trikaar.module.customer.dto.CustomerRequest;
import com.trikaar.module.customer.dto.CustomerResponse;
import com.trikaar.module.customer.entity.Customer;
import org.springframework.stereotype.Component;

/**
 * Manual mapper for Customer entity ↔ DTO transformations.
 */
@Component
public class CustomerMapper {

    public CustomerResponse toResponse(Customer customer) {
        if (customer == null)
            return null;

        return CustomerResponse.builder()
                .id(customer.getId().toString())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .city(customer.getCity())
                .state(customer.getState())
                .pincode(customer.getPincode())
                .dateOfBirth(customer.getDateOfBirth())
                .gender(customer.getGender())
                .loyaltyPoints(customer.getLoyaltyPoints())
                .totalSpent(customer.getTotalSpent())
                .totalVisits(customer.getTotalVisits())
                .loyaltyTier(customer.getLoyaltyTier().name())
                .active(customer.isActive())
                .notes(customer.getNotes())
                .createdAt(customer.getCreatedAt())
                .build();
    }

    public Customer toEntity(CustomerRequest request) {
        if (request == null)
            return null;

        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPincode(request.getPincode());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setGender(request.getGender());
        customer.setNotes(request.getNotes());
        return customer;
    }

    public void updateEntity(CustomerRequest request, Customer customer) {
        if (request == null || customer == null)
            return;

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setPincode(request.getPincode());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setGender(request.getGender());
        customer.setNotes(request.getNotes());
    }
}
