package com.trikaar.module.loyalty.repository;

import com.trikaar.module.loyalty.entity.LoyaltyTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, UUID> {

    Page<LoyaltyTransaction> findAllByCustomerIdAndBusinessIdAndDeletedFalse(
            UUID customerId, UUID businessId, Pageable pageable);
}
