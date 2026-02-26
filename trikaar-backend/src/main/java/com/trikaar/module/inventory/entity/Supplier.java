package com.trikaar.module.inventory.entity;

import com.trikaar.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Supplier master for purchase management.
 */
@Entity
@Table(name = "suppliers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_supplier_code_business", columnNames = { "supplier_code", "business_id" })
}, indexes = {
        @Index(name = "idx_supplier_business_id", columnList = "business_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier extends BaseEntity {

    @Column(name = "supplier_code", nullable = false, length = 50)
    private String supplierCode;

    @Column(name = "supplier_name", nullable = false, length = 255)
    private String supplierName;

    @Column(name = "contact_person", length = 200)
    private String contactPerson;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "gstin", length = 20)
    private String gstin;

    @Column(name = "payment_terms", length = 200)
    private String paymentTerms;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
