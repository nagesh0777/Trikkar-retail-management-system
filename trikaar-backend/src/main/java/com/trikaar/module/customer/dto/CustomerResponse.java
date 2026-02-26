package com.trikaar.module.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private String id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private LocalDate dateOfBirth;
    private String gender;
    private BigDecimal loyaltyPoints;
    private BigDecimal totalSpent;
    private int totalVisits;
    private String loyaltyTier;
    private boolean active;
    private String notes;
    private LocalDateTime createdAt;
}
