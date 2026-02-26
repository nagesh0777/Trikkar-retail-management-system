package com.trikaar.module.inventory.controller;

import com.trikaar.module.inventory.entity.Product;
import com.trikaar.module.inventory.repository.ProductRepository;
import com.trikaar.shared.context.TenantContext;
import com.trikaar.shared.dto.ApiResponse;
import com.trikaar.shared.dto.PagedResponse;
import com.trikaar.shared.exception.DuplicateResourceException;
import com.trikaar.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Product and inventory management APIs")
public class InventoryController {

    private final ProductRepository productRepository;

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKER', 'ACCOUNTANT', 'ANALYST')")
    @Operation(summary = "List all products")
    public ResponseEntity<ApiResponse<PagedResponse<Product>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "productName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        UUID businessId = TenantContext.getBusinessId();
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Page<Product> productPage = productRepository.findAllByBusinessIdAndDeletedFalse(
                businessId, PageRequest.of(page, size, sort));

        PagedResponse<Product> response = PagedResponse.<Product>builder()
                .content(productPage.getContent())
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .first(productPage.isFirst())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/products/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKER', 'ACCOUNTANT', 'ANALYST')")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<Product>> getProduct(@PathVariable UUID id) {
        UUID businessId = TenantContext.getBusinessId();
        Product product = productRepository.findByIdAndBusinessIdAndDeletedFalse(id, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/products/sku/{sku}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKER')")
    @Operation(summary = "Get product by SKU")
    public ResponseEntity<ApiResponse<Product>> getProductBySku(@PathVariable String sku) {
        UUID businessId = TenantContext.getBusinessId();
        Product product = productRepository.findBySkuAndBusinessIdAndDeletedFalse(sku, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/products/barcode/{barcode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKER')")
    @Operation(summary = "Get product by barcode (POS scan)")
    public ResponseEntity<ApiResponse<Product>> getProductByBarcode(@PathVariable String barcode) {
        UUID businessId = TenantContext.getBusinessId();
        Product product = productRepository.findByBarcodeAndBusinessIdAndDeletedFalse(barcode, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "barcode", barcode));
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/products/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'WORKER')")
    @Operation(summary = "Search products by name, SKU, or barcode")
    public ResponseEntity<ApiResponse<PagedResponse<Product>>> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID businessId = TenantContext.getBusinessId();
        Page<Product> productPage = productRepository.searchProducts(
                businessId, q, PageRequest.of(page, size));
        PagedResponse<Product> response = PagedResponse.<Product>builder()
                .content(productPage.getContent())
                .pageNumber(productPage.getNumber())
                .pageSize(productPage.getSize())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .last(productPage.isLast())
                .first(productPage.isFirst())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/products/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")
    @Operation(summary = "Get products with low stock")
    public ResponseEntity<ApiResponse<List<Product>>> getLowStockProducts() {
        UUID businessId = TenantContext.getBusinessId();
        List<Product> lowStockProducts = productRepository.findLowStockProducts(businessId);
        return ResponseEntity.ok(ApiResponse.success(lowStockProducts,
                "Found " + lowStockProducts.size() + " low stock products"));
    }

    @GetMapping("/stock-value")
    @PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT', 'ANALYST')")
    @Operation(summary = "Get total stock valuation")
    public ResponseEntity<ApiResponse<BigDecimal>> getStockValue() {
        UUID businessId = TenantContext.getBusinessId();
        BigDecimal stockValue = productRepository.calculateTotalStockValue(businessId);
        return ResponseEntity.ok(ApiResponse.success(stockValue, "Total stock valuation"));
    }
}
