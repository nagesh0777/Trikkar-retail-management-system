package com.trikaar.module.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterBusinessRequest {

    @NotBlank(message = "Business name is required")
    @Size(max = 255, message = "Business name cannot exceed 255 characters")
    private String businessName;

    @NotBlank(message = "Business slug is required")
    @Size(min = 3, max = 100, message = "Slug must be between 3 and 100 characters")
    private String slug;

    @Size(max = 100, message = "Registration number cannot exceed 100 characters")
    private String registrationNumber;

    @Size(max = 20, message = "GSTIN cannot exceed 20 characters")
    private String gstin;

    private String address;
    private String city;
    private String state;
    private String pincode;
    private String phone;

    @Email(message = "Invalid business email format")
    private String email;

    // Owner (first admin user) details
    @NotBlank(message = "Admin username is required")
    private String adminUsername;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid admin email format")
    private String adminEmail;

    @NotBlank(message = "Admin password is required")
    @Size(min = 8, max = 128, message = "Admin password must be between 8 and 128 characters")
    private String adminPassword;

    @NotBlank(message = "Admin full name is required")
    private String adminFullName;
}
