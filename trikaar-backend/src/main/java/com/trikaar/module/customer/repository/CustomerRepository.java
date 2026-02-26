package com.trikaar.module.customer.repository;

import com.trikaar.module.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Page<Customer> findAllByBusinessIdAndDeletedFalse(UUID businessId, Pageable pageable);

    Optional<Customer> findByIdAndBusinessIdAndDeletedFalse(UUID id, UUID businessId);

    Optional<Customer> findByPhoneAndBusinessIdAndDeletedFalse(String phone, UUID businessId);

    boolean existsByPhoneAndBusinessIdAndDeletedFalse(String phone, UUID businessId);

    boolean existsByEmailAndBusinessIdAndDeletedFalse(String email, UUID businessId);

    @Query("SELECT c FROM Customer c WHERE c.businessId = :businessId AND c.deleted = false " +
            "AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR c.phone LIKE CONCAT('%', :search, '%'))")
    Page<Customer> searchCustomers(@Param("businessId") UUID businessId,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT c FROM Customer c WHERE c.businessId = :businessId AND c.deleted = false " +
            "ORDER BY c.totalSpent DESC")
    List<Customer> findTopCustomers(@Param("businessId") UUID businessId, Pageable pageable);

    long countByBusinessIdAndActiveAndDeletedFalse(UUID businessId, boolean active);
}
