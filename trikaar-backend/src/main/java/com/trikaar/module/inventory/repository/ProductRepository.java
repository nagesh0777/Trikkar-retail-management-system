package com.trikaar.module.inventory.repository;

import com.trikaar.module.inventory.entity.Product;
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
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findAllByBusinessIdAndDeletedFalse(UUID businessId, Pageable pageable);

    Optional<Product> findByIdAndBusinessIdAndDeletedFalse(UUID id, UUID businessId);

    Optional<Product> findBySkuAndBusinessIdAndDeletedFalse(String sku, UUID businessId);

    Optional<Product> findByBarcodeAndBusinessIdAndDeletedFalse(String barcode, UUID businessId);

    boolean existsBySkuAndBusinessIdAndDeletedFalse(String sku, UUID businessId);

    boolean existsByBarcodeAndBusinessIdAndDeletedFalse(String barcode, UUID businessId);

    @Query("SELECT p FROM Product p WHERE p.businessId = :businessId AND p.deleted = false " +
            "AND (LOWER(p.productName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR p.barcode LIKE CONCAT('%', :search, '%'))")
    Page<Product> searchProducts(@Param("businessId") UUID businessId,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.businessId = :businessId AND p.deleted = false " +
            "AND p.currentStock <= p.reorderLevel AND p.active = true")
    List<Product> findLowStockProducts(@Param("businessId") UUID businessId);

    @Query("SELECT p FROM Product p WHERE p.businessId = :businessId AND p.deleted = false " +
            "AND p.category = :category")
    Page<Product> findByCategory(@Param("businessId") UUID businessId,
            @Param("category") String category,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.currentStock * p.costPrice), 0) FROM Product p " +
            "WHERE p.businessId = :businessId AND p.deleted = false AND p.active = true")
    java.math.BigDecimal calculateTotalStockValue(@Param("businessId") UUID businessId);
}
