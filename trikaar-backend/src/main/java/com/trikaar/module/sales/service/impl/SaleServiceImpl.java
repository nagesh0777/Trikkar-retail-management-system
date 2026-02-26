package com.trikaar.module.sales.service.impl;

import com.trikaar.module.audit.entity.AuditLog;
import com.trikaar.module.audit.service.AuditService;
import com.trikaar.module.customer.entity.Customer;
import com.trikaar.module.customer.repository.CustomerRepository;
import com.trikaar.module.employee.entity.Employee;
import com.trikaar.module.employee.repository.EmployeeRepository;
import com.trikaar.module.inventory.entity.Product;
import com.trikaar.module.inventory.entity.StockMovement;
import com.trikaar.module.inventory.repository.ProductRepository;
import com.trikaar.module.inventory.repository.StockMovementRepository;
import com.trikaar.module.loyalty.service.LoyaltyService;
import com.trikaar.module.sales.dto.CreateSaleRequest;
import com.trikaar.module.sales.dto.RefundRequest;
import com.trikaar.module.sales.dto.SaleResponse;
import com.trikaar.module.sales.entity.*;
import com.trikaar.module.sales.repository.RefundRepository;
import com.trikaar.module.sales.repository.SaleRepository;
import com.trikaar.module.sales.service.SaleService;
import com.trikaar.shared.context.TenantContext;
import com.trikaar.shared.dto.PagedResponse;
import com.trikaar.shared.exception.BusinessRuleException;
import com.trikaar.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final RefundRepository refundRepository;
    private final ProductRepository productRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final StockMovementRepository stockMovementRepository;
    private final LoyaltyService loyaltyService;
    private final AuditService auditService;

    private final AtomicLong transactionCounter = new AtomicLong(System.currentTimeMillis() % 100000);

    @Override
    @Transactional
    public SaleResponse createSale(CreateSaleRequest request) {
        UUID businessId = TenantContext.getBusinessId();

        // 1. Validate employee exists
        Employee employee = employeeRepository.findByIdAndBusinessIdAndDeletedFalse(
                request.getEmployeeId(), businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", request.getEmployeeId()));

        // 2. Validate customer if provided
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findByIdAndBusinessIdAndDeletedFalse(
                    request.getCustomerId(), businessId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));
        }

        // 3. Generate transaction number
        String transactionNumber = generateTransactionNumber(businessId);

        // 4. Build sale
        Sale sale = Sale.builder()
                .transactionNumber(transactionNumber)
                .employeeId(employee.getId())
                .customerId(customer != null ? customer.getId() : null)
                .saleDate(LocalDateTime.now())
                .paymentMethod(Sale.PaymentMethod.valueOf(request.getPaymentMethod().toUpperCase()))
                .status(Sale.SaleStatus.COMPLETED)
                .notes(request.getNotes())
                .build();
        sale.setBusinessId(businessId);

        // 5. Process each item — validate stock, calculate totals, deduct inventory
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        List<SaleItem> saleItems = new ArrayList<>();

        for (CreateSaleRequest.SaleItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findByIdAndBusinessIdAndDeletedFalse(
                    itemReq.getProductId(), businessId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemReq.getProductId()));

            if (!product.isActive()) {
                throw new BusinessRuleException("INACTIVE_PRODUCT",
                        "Product '" + product.getProductName() + "' is inactive");
            }

            // Check stock availability
            if (product.getCurrentStock().compareTo(itemReq.getQuantity()) < 0) {
                throw new BusinessRuleException("INSUFFICIENT_STOCK",
                        "Insufficient stock for '" + product.getProductName()
                                + "'. Available: " + product.getCurrentStock()
                                + ", Requested: " + itemReq.getQuantity());
            }

            // Calculate line totals
            BigDecimal lineSubtotal = product.getSellingPrice().multiply(itemReq.getQuantity());
            BigDecimal itemDiscount = itemReq.getDiscountAmount() != null
                    ? itemReq.getDiscountAmount()
                    : BigDecimal.ZERO;
            BigDecimal taxableAmount = lineSubtotal.subtract(itemDiscount);
            BigDecimal itemTax = product.isTaxable()
                    ? taxableAmount.multiply(product.getTaxPercentage())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal lineTotal = taxableAmount.add(itemTax);

            SaleItem saleItem = SaleItem.builder()
                    .sale(sale)
                    .productId(product.getId())
                    .productName(product.getProductName())
                    .sku(product.getSku())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getSellingPrice())
                    .costPrice(product.getCostPrice())
                    .discountAmount(itemDiscount)
                    .taxAmount(itemTax)
                    .taxPercentage(product.getTaxPercentage())
                    .lineTotal(lineTotal)
                    .build();
            saleItem.setBusinessId(businessId);
            saleItems.add(saleItem);

            subtotal = subtotal.add(lineSubtotal);
            totalTax = totalTax.add(itemTax);

            // Deduct stock
            BigDecimal stockBefore = product.getCurrentStock();
            product.setCurrentStock(stockBefore.subtract(itemReq.getQuantity()));
            productRepository.save(product);

            // Record stock movement
            StockMovement movement = StockMovement.builder()
                    .productId(product.getId())
                    .movementType(StockMovement.MovementType.SALE_OUT)
                    .quantity(itemReq.getQuantity().negate())
                    .stockBefore(stockBefore)
                    .stockAfter(product.getCurrentStock())
                    .referenceType("SALE")
                    .notes("Sale: " + transactionNumber)
                    .build();
            movement.setBusinessId(businessId);
            stockMovementRepository.save(movement);
        }

        // 6. Apply sale-level discount
        BigDecimal saleDiscount = request.getDiscountAmount() != null
                ? request.getDiscountAmount()
                : BigDecimal.ZERO;

        // 7. Handle loyalty redemption
        BigDecimal loyaltyDiscount = BigDecimal.ZERO;
        BigDecimal loyaltyPointsRedeemed = BigDecimal.ZERO;
        if (customer != null && request.getLoyaltyPointsToRedeem() != null
                && request.getLoyaltyPointsToRedeem().compareTo(BigDecimal.ZERO) > 0) {
            loyaltyPointsRedeemed = request.getLoyaltyPointsToRedeem();
            loyaltyDiscount = loyaltyService.calculateRedemptionValue(
                    businessId, loyaltyPointsRedeemed);

            if (customer.getLoyaltyPoints().compareTo(loyaltyPointsRedeemed) < 0) {
                throw new BusinessRuleException("INSUFFICIENT_LOYALTY_POINTS",
                        "Customer has only " + customer.getLoyaltyPoints() + " loyalty points");
            }
        }

        // 8. Calculate totals
        BigDecimal totalAmount = subtotal.add(totalTax)
                .subtract(saleDiscount).subtract(loyaltyDiscount);

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        BigDecimal changeAmount = request.getAmountPaid().subtract(totalAmount);
        if (changeAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("INSUFFICIENT_PAYMENT",
                    "Amount paid (" + request.getAmountPaid()
                            + ") is less than total (" + totalAmount + ")");
        }

        // 9. Finalize sale
        sale.setItems(saleItems);
        sale.setSubtotal(subtotal);
        sale.setTaxAmount(totalTax);
        sale.setDiscountAmount(saleDiscount);
        sale.setLoyaltyPointsRedeemed(loyaltyPointsRedeemed);
        sale.setLoyaltyDiscount(loyaltyDiscount);
        sale.setTotalAmount(totalAmount);
        sale.setAmountPaid(request.getAmountPaid());
        sale.setChangeAmount(changeAmount);
        sale.setLocked(true); // Lock record immediately on completion

        // 10. Calculate and assign loyalty points earned
        if (customer != null) {
            BigDecimal pointsEarned = loyaltyService.calculatePointsEarned(businessId, totalAmount);
            sale.setLoyaltyPointsEarned(pointsEarned);

            // Update customer loyalty
            loyaltyService.processLoyaltyTransaction(
                    businessId, customer.getId(), sale.getId(),
                    pointsEarned, loyaltyPointsRedeemed, totalAmount);
        }

        sale = saleRepository.save(sale);

        // 11. Audit log
        auditService.logAction(
                AuditLog.AuditAction.SALE_CREATED,
                "Sale",
                sale.getId(),
                "Sale created: " + transactionNumber + " | Total: " + totalAmount);

        log.info("Sale '{}' created. Total: {}, Employee: {}, Customer: {}",
                transactionNumber, totalAmount, employee.getFullName(),
                customer != null ? customer.getFullName() : "Walk-in");

        return mapToResponse(sale, employee.getFullName(),
                customer != null ? customer.getFullName() : null);
    }

    @Override
    @Transactional
    public SaleResponse processRefund(RefundRequest request) {
        UUID businessId = TenantContext.getBusinessId();
        UUID userId = TenantContext.getUserId();

        // 1. Find original sale
        Sale originalSale = saleRepository.findByIdAndBusinessIdAndDeletedFalse(
                request.getOriginalSaleId(), businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", request.getOriginalSaleId()));

        if (originalSale.getStatus() == Sale.SaleStatus.REFUNDED) {
            throw new BusinessRuleException("ALREADY_REFUNDED", "This sale has already been fully refunded");
        }

        if (originalSale.getStatus() == Sale.SaleStatus.VOID) {
            throw new BusinessRuleException("VOIDED_SALE", "Cannot refund a voided sale");
        }

        // 2. Generate refund number
        String refundNumber = "RFN-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + transactionCounter.incrementAndGet();

        // 3. Build refund
        boolean isFullRefund = request.getItems() == null || request.getItems().isEmpty();

        Refund refund = Refund.builder()
                .refundNumber(refundNumber)
                .originalSaleId(originalSale.getId())
                .originalTransactionNumber(originalSale.getTransactionNumber())
                .refundDate(LocalDateTime.now())
                .reason(request.getReason())
                .refundType(isFullRefund ? Refund.RefundType.FULL : Refund.RefundType.PARTIAL)
                .processedById(userId)
                .notes(request.getNotes())
                .build();
        refund.setBusinessId(businessId);

        BigDecimal totalRefundAmount = BigDecimal.ZERO;
        List<RefundItem> refundItems = new ArrayList<>();

        if (isFullRefund) {
            // Full refund — reverse all items
            for (SaleItem saleItem : originalSale.getItems()) {
                RefundItem refundItem = createRefundItem(refund, saleItem,
                        saleItem.getQuantity(), businessId);
                refundItems.add(refundItem);
                totalRefundAmount = totalRefundAmount.add(refundItem.getRefundAmount());

                // Reverse stock
                reverseStock(saleItem.getProductId(), saleItem.getQuantity(),
                        businessId, originalSale.getTransactionNumber());
            }
            originalSale.setStatus(Sale.SaleStatus.REFUNDED);
        } else {
            // Partial refund
            for (RefundRequest.RefundItemRequest itemReq : request.getItems()) {
                SaleItem matchingSaleItem = originalSale.getItems().stream()
                        .filter(si -> si.getProductId().equals(itemReq.getProductId()))
                        .findFirst()
                        .orElseThrow(() -> new BusinessRuleException("INVALID_REFUND_ITEM",
                                "Product " + itemReq.getProductId()
                                        + " was not part of the original sale"));

                if (itemReq.getQuantity().compareTo(matchingSaleItem.getQuantity()) > 0) {
                    throw new BusinessRuleException("EXCESSIVE_REFUND_QUANTITY",
                            "Cannot refund more than sold quantity for product "
                                    + matchingSaleItem.getProductName());
                }

                RefundItem refundItem = createRefundItem(refund, matchingSaleItem,
                        itemReq.getQuantity(), businessId);
                refundItems.add(refundItem);
                totalRefundAmount = totalRefundAmount.add(refundItem.getRefundAmount());

                reverseStock(matchingSaleItem.getProductId(), itemReq.getQuantity(),
                        businessId, originalSale.getTransactionNumber());
            }
            originalSale.setStatus(Sale.SaleStatus.PARTIALLY_REFUNDED);
        }

        refund.setRefundAmount(totalRefundAmount);
        refund.setItems(refundItems);
        refundRepository.save(refund);
        saleRepository.save(originalSale);

        // Audit log
        auditService.logAction(
                AuditLog.AuditAction.REFUND_PROCESSED,
                "Refund",
                refund.getId(),
                "Refund processed: " + refundNumber + " for sale "
                        + originalSale.getTransactionNumber()
                        + " | Amount: " + totalRefundAmount + " | Reason: " + request.getReason());

        log.info("Refund '{}' processed for sale '{}'. Amount: {}",
                refundNumber, originalSale.getTransactionNumber(), totalRefundAmount);

        return getSaleById(originalSale.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse getSaleById(UUID saleId) {
        UUID businessId = TenantContext.getBusinessId();
        Sale sale = saleRepository.findByIdAndBusinessIdAndDeletedFalse(saleId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", saleId));
        return mapToResponse(sale, resolveEmployeeName(sale.getEmployeeId(), businessId),
                resolveCustomerName(sale.getCustomerId(), businessId));
    }

    @Override
    @Transactional(readOnly = true)
    public SaleResponse getSaleByTransactionNumber(String transactionNumber) {
        UUID businessId = TenantContext.getBusinessId();
        Sale sale = saleRepository.findByTransactionNumberAndBusinessIdAndDeletedFalse(
                transactionNumber, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "transactionNumber", transactionNumber));
        return mapToResponse(sale, resolveEmployeeName(sale.getEmployeeId(), businessId),
                resolveCustomerName(sale.getCustomerId(), businessId));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SaleResponse> getAllSales(int page, int size, String sortBy, String sortDir) {
        UUID businessId = TenantContext.getBusinessId();
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Page<Sale> salePage = saleRepository.findAllByBusinessIdAndDeletedFalse(
                businessId, PageRequest.of(page, size, sort));
        return buildPagedResponse(salePage, businessId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SaleResponse> getSalesByEmployee(UUID employeeId, int page, int size) {
        UUID businessId = TenantContext.getBusinessId();
        Page<Sale> salePage = saleRepository.findAllByEmployeeIdAndBusinessIdAndDeletedFalse(
                employeeId, businessId, PageRequest.of(page, size, Sort.by("saleDate").descending()));
        return buildPagedResponse(salePage, businessId);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<SaleResponse> getSalesByCustomer(UUID customerId, int page, int size) {
        UUID businessId = TenantContext.getBusinessId();
        Page<Sale> salePage = saleRepository.findAllByCustomerIdAndBusinessIdAndDeletedFalse(
                customerId, businessId, PageRequest.of(page, size, Sort.by("saleDate").descending()));
        return buildPagedResponse(salePage, businessId);
    }

    // ═══════════════════ Private Helpers ═══════════════════

    private String generateTransactionNumber(UUID businessId) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = saleRepository.countTodaySales(businessId,
                LocalDate.now().atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay());
        return "TXN-" + dateStr + "-" + String.format("%05d", count + 1);
    }

    private RefundItem createRefundItem(Refund refund, SaleItem saleItem,
            BigDecimal quantity, UUID businessId) {
        BigDecimal proportionalRefund = saleItem.getUnitPrice().multiply(quantity);
        RefundItem item = RefundItem.builder()
                .refund(refund)
                .productId(saleItem.getProductId())
                .productName(saleItem.getProductName())
                .quantity(quantity)
                .unitPrice(saleItem.getUnitPrice())
                .refundAmount(proportionalRefund)
                .build();
        item.setBusinessId(businessId);
        return item;
    }

    private void reverseStock(UUID productId, BigDecimal quantity,
            UUID businessId, String transactionNumber) {
        Product product = productRepository.findByIdAndBusinessIdAndDeletedFalse(productId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        BigDecimal stockBefore = product.getCurrentStock();
        product.setCurrentStock(stockBefore.add(quantity));
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
                .productId(productId)
                .movementType(StockMovement.MovementType.REFUND_IN)
                .quantity(quantity)
                .stockBefore(stockBefore)
                .stockAfter(product.getCurrentStock())
                .referenceType("REFUND")
                .notes("Refund for sale: " + transactionNumber)
                .build();
        movement.setBusinessId(businessId);
        stockMovementRepository.save(movement);
    }

    private String resolveEmployeeName(UUID employeeId, UUID businessId) {
        return employeeRepository.findByIdAndBusinessIdAndDeletedFalse(employeeId, businessId)
                .map(Employee::getFullName).orElse("Unknown");
    }

    private String resolveCustomerName(UUID customerId, UUID businessId) {
        if (customerId == null)
            return null;
        return customerRepository.findByIdAndBusinessIdAndDeletedFalse(customerId, businessId)
                .map(Customer::getFullName).orElse("Unknown");
    }

    private SaleResponse mapToResponse(Sale sale, String employeeName, String customerName) {
        List<SaleResponse.SaleItemResponse> itemResponses = sale.getItems().stream()
                .map(item -> SaleResponse.SaleItemResponse.builder()
                        .id(item.getId().toString())
                        .productId(item.getProductId().toString())
                        .productName(item.getProductName())
                        .sku(item.getSku())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .discountAmount(item.getDiscountAmount())
                        .taxAmount(item.getTaxAmount())
                        .lineTotal(item.getLineTotal())
                        .build())
                .toList();

        return SaleResponse.builder()
                .id(sale.getId().toString())
                .transactionNumber(sale.getTransactionNumber())
                .employeeId(sale.getEmployeeId().toString())
                .employeeName(employeeName)
                .customerId(sale.getCustomerId() != null ? sale.getCustomerId().toString() : null)
                .customerName(customerName)
                .saleDate(sale.getSaleDate())
                .subtotal(sale.getSubtotal())
                .taxAmount(sale.getTaxAmount())
                .discountAmount(sale.getDiscountAmount())
                .loyaltyPointsRedeemed(sale.getLoyaltyPointsRedeemed())
                .loyaltyDiscount(sale.getLoyaltyDiscount())
                .totalAmount(sale.getTotalAmount())
                .amountPaid(sale.getAmountPaid())
                .changeAmount(sale.getChangeAmount())
                .paymentMethod(sale.getPaymentMethod().name())
                .status(sale.getStatus().name())
                .loyaltyPointsEarned(sale.getLoyaltyPointsEarned())
                .locked(sale.isLocked())
                .notes(sale.getNotes())
                .items(itemResponses)
                .build();
    }

    private PagedResponse<SaleResponse> buildPagedResponse(Page<Sale> page, UUID businessId) {
        List<SaleResponse> responses = page.getContent().stream()
                .map(sale -> mapToResponse(sale,
                        resolveEmployeeName(sale.getEmployeeId(), businessId),
                        resolveCustomerName(sale.getCustomerId(), businessId)))
                .toList();

        return PagedResponse.<SaleResponse>builder()
                .content(responses)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}
