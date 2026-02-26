# 🏗️ TRIKAAR — Retail Management Platform

> Production-grade, multi-tenant SaaS retail management system built with Spring Boot 3.2 and Java 21.

---

## 📋 System Architecture Overview

### Architecture Style
**Modular Monolith** — Each business domain is a self-contained module with its own Controller → Service → Repository → Entity layering, but deployed as a single application for operational simplicity.

### Multi-Tenancy Strategy
- **Type:** Discriminator-column based (`business_id` in every table)
- **Enforcement layers:**
  - **JWT Token:** Contains `businessId` as a custom claim
  - **TenantContext:** Thread-local holder populated by JWT filter
  - **Repository:** All queries filtered by `businessId`
  - **API Layer:** Automatic injection via security context
  - **Audit:** Every audit log tied to `businessId`

### Security Architecture
- **Authentication:** Stateless JWT with access + refresh token rotation
- **Authorization:** Role-based (`@PreAuthorize`) with 4 roles: ADMIN, ACCOUNTANT, WORKER, ANALYST
- **Password Storage:** BCrypt with strength factor 12
- **Account Protection:** Auto-lockout after 5 failed login attempts
- **Token Security:** Refresh token stored in DB for revocation support

---

## 📁 Folder Structure

```
trikaar-backend/
├── pom.xml
├── src/main/java/com/trikaar/
│   ├── TrikaarApplication.java              # Entry point
│   ├── config/
│   │   ├── SecurityConfig.java              # Spring Security configuration
│   │   └── SecurityProperties.java          # JWT/CORS config properties
│   ├── shared/
│   │   ├── entity/BaseEntity.java           # Abstract base (UUID, businessId, audit)
│   │   ├── enums/Role.java                  # System roles
│   │   ├── dto/
│   │   │   ├── ApiResponse.java             # Standard response envelope
│   │   │   └── PagedResponse.java           # Paginated response wrapper
│   │   ├── context/TenantContext.java        # Thread-local tenant holder
│   │   ├── audit/AuditorAwareImpl.java       # JPA auditing provider
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java   # Centralized error handling
│   │       ├── ResourceNotFoundException.java
│   │       ├── BusinessRuleException.java
│   │       ├── DuplicateResourceException.java
│   │       └── TenantAccessDeniedException.java
│   └── module/
│       ├── auth/                            # 1️⃣ Auth Module
│       │   ├── controller/AuthController
│       │   ├── dto/ (LoginRequest, RegisterBusinessRequest, etc.)
│       │   ├── entity/ (User, Business, RefreshToken)
│       │   ├── repository/
│       │   ├── security/ (JwtTokenProvider, JwtAuthFilter, UserPrincipal)
│       │   └── service/ (AuthService → AuthServiceImpl)
│       ├── employee/                        # 2️⃣ Employee Module
│       │   ├── controller/EmployeeController
│       │   ├── dto/ (EmployeeRequest, EmployeeResponse)
│       │   ├── entity/ (Employee, Attendance)
│       │   ├── mapper/EmployeeMapper
│       │   ├── repository/
│       │   └── service/ (EmployeeService → EmployeeServiceImpl)
│       ├── customer/                        # 3️⃣ Customer Module
│       │   ├── controller/CustomerController
│       │   ├── dto/ (CustomerRequest, CustomerResponse)
│       │   ├── entity/Customer
│       │   ├── mapper/CustomerMapper
│       │   ├── repository/
│       │   └── service/ (CustomerService → CustomerServiceImpl)
│       ├── inventory/                       # 4️⃣ Inventory Module
│       │   ├── controller/InventoryController
│       │   ├── entity/ (Product, Supplier, PurchaseEntry, PurchaseEntryItem, StockMovement)
│       │   └── repository/ (ProductRepository, SupplierRepository, StockMovementRepository)
│       ├── sales/                           # 5️⃣ Sales & POS Module
│       │   ├── controller/SaleController
│       │   ├── dto/ (CreateSaleRequest, SaleResponse, RefundRequest)
│       │   ├── entity/ (Sale, SaleItem, Refund, RefundItem)
│       │   ├── repository/
│       │   └── service/ (SaleService → SaleServiceImpl)
│       ├── loyalty/                         # 6️⃣ Loyalty Module
│       │   ├── entity/ (LoyaltyConfig, LoyaltyTransaction)
│       │   ├── repository/
│       │   └── service/ (LoyaltyService → LoyaltyServiceImpl)
│       ├── salary/                          # 7️⃣ Salary Module
│       │   ├── controller/SalaryController
│       │   ├── dto/ (GenerateSalaryRequest, SalaryPayoutResponse)
│       │   ├── entity/SalaryPayout
│       │   ├── repository/
│       │   └── service/ (SalaryService → SalaryServiceImpl)
│       ├── reporting/                       # 8️⃣ Reporting Module
│       │   ├── controller/ReportingController
│       │   ├── dto/ (DailySalesReport, MonthlyRevenueReport)
│       │   └── service/ (ReportingService → ReportingServiceImpl)
│       ├── admin/                           # 9️⃣ Admin Config Module
│       │   ├── controller/AdminConfigController
│       │   ├── dto/ (AdminConfigRequest, AdminConfigResponse)
│       │   ├── entity/AdminConfig
│       │   ├── repository/
│       │   └── service/ (AdminConfigService → AdminConfigServiceImpl)
│       └── audit/                           # 🔟 Audit Module
│           ├── controller/AuditLogController
│           ├── entity/AuditLog
│           ├── repository/
│           └── service/ (AuditService → AuditServiceImpl)
└── src/main/resources/
    ├── application.properties
    └── db/migration/V1__initial_schema.sql
```

---

## 🗄️ Entity Relationship Diagram (Text Form)

```
┌─────────────┐     ┌─────────────┐
│  Business    │──┬──│    User     │
│  (Tenant)    │  │  │  (Auth)     │
└──────┬───────┘  │  └─────────────┘
       │          │
       │          │  ┌─────────────┐     ┌──────────────┐
       ├──────────┼──│  Employee   │──┬──│  Attendance  │
       │          │  └──────┬──────┘  │  └──────────────┘
       │          │         │         │
       │          │         │         │  ┌───────────────┐
       │          │         ├─────────┼──│ SalaryPayout  │
       │          │         │         │  └───────────────┘
       │          │         │         │
       │          │  ┌──────┴──────┐  │  ┌─────────────┐
       ├──────────┼──│    Sale     │──┼──│  SaleItem   │
       │          │  │ (POS Trans) │  │  └──────┬──────┘
       │          │  └──────┬──────┘  │         │
       │          │         │         │         │
       │          │  ┌──────┴──────┐  │  ┌──────┴──────┐
       │          │  │   Refund    │──┼──│ RefundItem  │
       │          │  └─────────────┘  │  └─────────────┘
       │          │                   │
       │          │  ┌─────────────┐  │
       ├──────────┼──│  Customer   │──┘
       │          │  └──────┬──────┘
       │          │         │
       │          │  ┌──────┴──────────┐
       │          │  │LoyaltyTransaction│
       │          │  └─────────────────┘
       │          │
       │          │  ┌─────────────┐     ┌──────────────┐
       ├──────────┼──│  Product    │──┬──│StockMovement │
       │          │  └──────┬──────┘  │  └──────────────┘
       │          │         │         │
       │          │  ┌──────┴───────┐ │  ┌───────────────────┐
       │          │  │PurchaseEntry │─┼──│PurchaseEntryItem  │
       │          │  └──────────────┘ │  └───────────────────┘
       │          │                   │
       │          │  ┌─────────────┐  │  ┌──────────────┐
       ├──────────┼──│  Supplier   │──┘  │ AdminConfig  │
       │          │  └─────────────┘     └──────────────┘
       │          │
       │          │  ┌─────────────┐     ┌──────────────┐
       └──────────┴──│ AuditLog    │     │LoyaltyConfig │
                     └─────────────┘     └──────────────┘
```

---

## 🔒 Security Design

### Role-Based Access Control Matrix

| Endpoint Area      | ADMIN | ACCOUNTANT | WORKER | ANALYST |
|--------------------|-------|------------|--------|---------|
| Auth (register)    | ✅     | ❌          | ❌      | ❌       |
| Employees (write)  | ✅     | ❌          | ❌      | ❌       |
| Employees (read)   | ✅     | ✅          | ❌      | ✅       |
| Customers (write)  | ✅     | ❌          | ✅      | ❌       |
| Customers (read)   | ✅     | ❌          | ✅      | ✅       |
| Sales (create)     | ✅     | ❌          | ✅      | ❌       |
| Sales (read)       | ✅     | ✅          | ✅      | ✅       |
| Refunds            | ✅     | ✅          | ❌      | ❌       |
| Inventory          | ✅     | ✅          | ✅      | ✅       |
| Salary             | ✅     | ✅          | ❌      | ❌       |
| Reports            | ✅     | ✅          | ❌      | ✅       |
| Admin Config       | ✅     | ❌          | ❌      | ❌       |
| Audit Logs         | ✅     | ❌          | ❌      | ❌       |

### Audited Actions
- Sale creation
- Refund processing
- Salary payout
- Config changes
- Employee creation/termination
- User registration
- Account lockouts

---

## 🌐 API Endpoints

### Authentication
| Method | Endpoint                  | Description               | Auth  |
|--------|---------------------------|---------------------------|-------|
| POST   | /api/auth/login           | User login                | Public|
| POST   | /api/auth/register-business| Register new business     | Public|
| POST   | /api/auth/register        | Create user (admin only)  | ADMIN |
| POST   | /api/auth/refresh-token   | Refresh access token      | Public|
| POST   | /api/auth/logout          | Revoke refresh token      | Auth  |

### Employees
| Method | Endpoint                         | Description              |
|--------|----------------------------------|--------------------------|
| POST   | /api/employees                   | Create employee          |
| GET    | /api/employees                   | List employees (paged)   |
| GET    | /api/employees/{id}              | Get by ID                |
| GET    | /api/employees/search?q=         | Search employees         |
| PUT    | /api/employees/{id}              | Update employee          |
| PATCH  | /api/employees/{id}/deactivate   | Deactivate               |
| PATCH  | /api/employees/{id}/terminate    | Terminate                |

### Customers
| Method | Endpoint                         | Description              |
|--------|----------------------------------|--------------------------|
| POST   | /api/customers                   | Create customer          |
| GET    | /api/customers                   | List customers (paged)   |
| GET    | /api/customers/{id}              | Get by ID                |
| GET    | /api/customers/phone/{phone}     | Get by phone             |
| GET    | /api/customers/top?limit=10      | Top customers by spend   |
| PUT    | /api/customers/{id}              | Update customer          |

### Sales & POS
| Method | Endpoint                                        | Description           |
|--------|--------------------------------------------------|-----------------------|
| POST   | /api/sales                                      | Create sale           |
| GET    | /api/sales                                      | List sales (paged)    |
| GET    | /api/sales/{id}                                 | Get by ID             |
| GET    | /api/sales/transaction/{txnNumber}              | Get by transaction #  |
| GET    | /api/sales/by-employee/{employeeId}             | Sales by employee     |
| GET    | /api/sales/by-customer/{customerId}             | Sales by customer     |
| POST   | /api/sales/refund                               | Process refund        |

### Inventory
| Method | Endpoint                                  | Description              |
|--------|-------------------------------------------|--------------------------|
| GET    | /api/inventory/products                   | List products (paged)    |
| GET    | /api/inventory/products/{id}              | Get by ID                |
| GET    | /api/inventory/products/sku/{sku}         | Get by SKU               |
| GET    | /api/inventory/products/barcode/{barcode} | Get by barcode (POS)     |
| GET    | /api/inventory/products/search?q=         | Search products          |
| GET    | /api/inventory/products/low-stock         | Low stock alerts         |
| GET    | /api/inventory/stock-value                | Total stock valuation    |

### Salary
| Method | Endpoint                            | Description              |
|--------|-------------------------------------|--------------------------|
| POST   | /api/salary/generate                | Generate salary          |
| PATCH  | /api/salary/{id}/approve            | Approve payout           |
| PATCH  | /api/salary/{id}/pay                | Mark as paid             |
| GET    | /api/salary/employee/{employeeId}   | Employee salary history  |
| GET    | /api/salary                         | All payouts              |

### Reports
| Method | Endpoint                                    | Description          |
|--------|---------------------------------------------|----------------------|
| GET    | /api/reports/daily-sales?date=2026-02-26    | Daily sales report   |
| GET    | /api/reports/monthly-revenue?year=&month=   | Monthly revenue      |

### Admin Config & Audit
| Method | Endpoint                                    | Description          |
|--------|---------------------------------------------|----------------------|
| POST   | /api/admin/config                           | Create/update config |
| GET    | /api/admin/config                           | List all configs     |
| GET    | /api/admin/config/category/{cat}            | By category          |
| GET    | /api/audit-logs                             | List audit logs      |
| GET    | /api/audit-logs/by-action/{action}          | By action type       |
| GET    | /api/audit-logs/by-date-range               | By date range        |

---

## 🚀 Production Recommendations

1. **Environment Variables:** Set `JWT_SECRET`, `DB_USERNAME`, `DB_PASSWORD` via env vars (never commit secrets)
2. **Database:** Use PostgreSQL 15+ with connection pooling (PgBouncer in production)
3. **Monitoring:** Actuator endpoints exposed for Prometheus/Grafana integration
4. **API Documentation:** Available at `/api/swagger-ui.html` (disable in production)
5. **Rate Limiting:** Add Spring Cloud Gateway or API Gateway for rate limiting
6. **Caching:** Redis for frequently accessed configs and product catalog
7. **Backup:** Automated PostgreSQL WAL archiving for point-in-time recovery
8. **SSL/TLS:** Terminate TLS at load balancer (Nginx/ALB)
9. **Logging:** Structured JSON logging with ELK stack for production
10. **Health Checks:** Kubernetes readiness/liveness probes via Actuator

---

## 🛠️ Getting Started

```bash
# 1. Create PostgreSQL database
createdb trikaar_db

# 2. Set environment variables
export DB_USERNAME=trikaar_user
export DB_PASSWORD=trikaar_pass
export JWT_SECRET=<your-base64-secret>

# 3. Build
mvn clean package -DskipTests

# 4. Run
java -jar target/trikaar-backend-1.0.0-SNAPSHOT.jar

# 5. Access Swagger UI
open http://localhost:8080/api/swagger-ui.html
```
