package com.trikaar.module.inventory.repository;

import com.trikaar.module.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    Page<StockMovement> findAllByProductIdAndBusinessIdAndDeletedFalse(
            UUID productId, UUID businessId, Pageable pageable);

    List<StockMovement> findAllByProductIdAndBusinessIdAndCreatedAtBetweenAndDeletedFalse(
            UUID productId, UUID businessId, LocalDateTime start, LocalDateTime end);

    Page<StockMovement> findAllByBusinessIdAndDeletedFalse(UUID businessId, Pageable pageable);
}
