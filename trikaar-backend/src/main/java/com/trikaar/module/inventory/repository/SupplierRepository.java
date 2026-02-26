package com.trikaar.module.inventory.repository;

import com.trikaar.module.inventory.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    Page<Supplier> findAllByBusinessIdAndDeletedFalse(UUID businessId, Pageable pageable);

    Optional<Supplier> findByIdAndBusinessIdAndDeletedFalse(UUID id, UUID businessId);

    Optional<Supplier> findBySupplierCodeAndBusinessIdAndDeletedFalse(String code, UUID businessId);

    boolean existsBySupplierCodeAndBusinessIdAndDeletedFalse(String code, UUID businessId);
}
