-- ══════════════════════════════════════════════════════════════
--  TRIKAAR Retail Management Platform
--  Database Schema - V1 Initial Migration
--  PostgreSQL 15+
-- ══════════════════════════════════════════════════════════════

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ── Businesses (Tenants) ─────────────────────────────────────
CREATE TABLE businesses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    business_name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    registration_number VARCHAR(100),
    gstin VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10),
    phone VARCHAR(20),
    email VARCHAR(255),
    currency_code VARCHAR(5) NOT NULL DEFAULT 'INR',
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Kolkata',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    owner_user_id UUID,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uk_business_slug UNIQUE (slug),
    CONSTRAINT uk_business_registration UNIQUE (registration_number)
);

CREATE INDEX idx_business_slug ON businesses(slug);
CREATE INDEX idx_business_active ON businesses(is_active);

-- ── Users ────────────────────────────────────────────────────
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(30) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_attempts INT DEFAULT 0,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uk_users_email_business UNIQUE (email, business_id),
    CONSTRAINT uk_users_username_business UNIQUE (username, business_id),
    CONSTRAINT fk_users_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX idx_users_business_id ON users(business_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- ── Refresh Tokens ───────────────────────────────────────────
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    token VARCHAR(1000) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    replaced_by_token VARCHAR(1000),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_refresh_token_value ON refresh_tokens(token);
CREATE INDEX idx_refresh_token_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_token_business ON refresh_tokens(business_id);

-- ── Employees ────────────────────────────────────────────────
CREATE TABLE employees (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    employee_code VARCHAR(50) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(500),
    date_of_birth DATE,
    date_of_joining DATE NOT NULL,
    date_of_leaving DATE,
    department VARCHAR(100),
    designation VARCHAR(100),
    employment_type VARCHAR(30) NOT NULL,
    wage_type VARCHAR(30) NOT NULL,
    base_salary NUMERIC(12,2) NOT NULL,
    hourly_rate NUMERIC(10,2),
    daily_rate NUMERIC(10,2),
    sales_incentive_percentage NUMERIC(5,2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    user_id UUID,
    bank_account_number VARCHAR(50),
    ifsc_code VARCHAR(20),
    pan_number VARCHAR(20),
    aadhar_number VARCHAR(20),
    emergency_contact_name VARCHAR(200),
    emergency_contact_phone VARCHAR(20),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uk_employee_code_business UNIQUE (employee_code, business_id),
    CONSTRAINT uk_employee_email_business UNIQUE (email, business_id),
    CONSTRAINT fk_employee_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX idx_employee_business_id ON employees(business_id);
CREATE INDEX idx_employee_status ON employees(status);
CREATE INDEX idx_employee_department ON employees(department);

-- ── Attendance ───────────────────────────────────────────────
CREATE TABLE attendance (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    check_in_time TIME,
    check_out_time TIME,
    status VARCHAR(20) NOT NULL,
    overtime_hours DOUBLE PRECISION,
    notes VARCHAR(500),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uk_attendance_employee_date UNIQUE (employee_id, attendance_date, business_id),
    CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_attendance_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX idx_attendance_business_id ON attendance(business_id);
CREATE INDEX idx_attendance_employee_id ON attendance(employee_id);
CREATE INDEX idx_attendance_date ON attendance(attendance_date);

-- ── Customers ────────────────────────────────────────────────
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(20) NOT NULL,
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10),
    date_of_birth DATE,
    gender VARCHAR(15),
    loyalty_points NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_spent NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_visits INT NOT NULL DEFAULT 0,
    loyalty_tier VARCHAR(20) NOT NULL DEFAULT 'BRONZE',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes VARCHAR(1000),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uk_customer_phone_business UNIQUE (phone, business_id),
    CONSTRAINT uk_customer_email_business UNIQUE (email, business_id),
    CONSTRAINT fk_customer_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX idx_customer_business_id ON customers(business_id);
CREATE INDEX idx_customer_phone ON customers(phone);
CREATE INDEX idx_customer_loyalty_tier ON customers(loyalty_tier);

-- ── Products ─────────────────────────────────────────────────
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    sku VARCHAR(100) NOT NULL,
    barcode VARCHAR(100),
    product_name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    category VARCHAR(100),
    sub_category VARCHAR(100),
    brand VARCHAR(100),
    unit VARCHAR(30) NOT NULL,
    cost_price NUMERIC(12,2) NOT NULL,
    selling_price NUMERIC(12,2) NOT NULL,
    mrp NUMERIC(12,2),
    tax_percentage NUMERIC(5,2) DEFAULT 0,
    hsn_code VARCHAR(20),
    current_stock NUMERIC(12,3) NOT NULL DEFAULT 0,
    reorder_level NUMERIC(12,3) NOT NULL DEFAULT 0,
    max_stock_level NUMERIC(12,3),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_taxable BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uk_product_sku_business UNIQUE (sku, business_id),
    CONSTRAINT uk_product_barcode_business UNIQUE (barcode, business_id),
    CONSTRAINT fk_product_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX idx_product_business_id ON products(business_id);
CREATE INDEX idx_product_sku ON products(sku);
CREATE INDEX idx_product_barcode ON products(barcode);
CREATE INDEX idx_product_category ON products(category);
CREATE INDEX idx_product_low_stock ON products(current_stock, reorder_level);

-- ── Suppliers ────────────────────────────────────────────────
CREATE TABLE suppliers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    supplier_code VARCHAR(50) NOT NULL,
    supplier_name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(200),
    email VARCHAR(255),
    phone VARCHAR(20),
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10),
    gstin VARCHAR(20),
    payment_terms VARCHAR(200),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uk_supplier_code_business UNIQUE (supplier_code, business_id),
    CONSTRAINT fk_supplier_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX idx_supplier_business_id ON suppliers(business_id);

-- ── Purchase Entries ─────────────────────────────────────────
CREATE TABLE purchase_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    purchase_number VARCHAR(50) NOT NULL UNIQUE,
    supplier_id UUID NOT NULL,
    purchase_date DATE NOT NULL,
    invoice_number VARCHAR(100),
    total_amount NUMERIC(14,2) NOT NULL,
    tax_amount NUMERIC(12,2) DEFAULT 0,
    discount_amount NUMERIC(12,2) DEFAULT 0,
    net_amount NUMERIC(14,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',
    notes VARCHAR(1000),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_purchase_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT fk_purchase_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX idx_purchase_business_id ON purchase_entries(business_id);
CREATE INDEX idx_purchase_supplier_id ON purchase_entries(supplier_id);
CREATE INDEX idx_purchase_date ON purchase_entries(purchase_date);

-- ── Purchase Entry Items ─────────────────────────────────────
CREATE TABLE purchase_entry_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    purchase_entry_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,
    unit_cost NUMERIC(12,2) NOT NULL,
    total_cost NUMERIC(14,2) NOT NULL,
    tax_amount NUMERIC(12,2) DEFAULT 0,
    batch_number VARCHAR(50),
    expiry_date DATE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_pei_purchase FOREIGN KEY (purchase_entry_id) REFERENCES purchase_entries(id),
    CONSTRAINT fk_pei_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_pei_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX idx_purchase_item_product ON purchase_entry_items(product_id);
CREATE INDEX idx_purchase_item_business ON purchase_entry_items(business_id);

-- ── Stock Movements ──────────────────────────────────────────
CREATE TABLE stock_movements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    product_id UUID NOT NULL,
    movement_type VARCHAR(30) NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,
    stock_before NUMERIC(12,3) NOT NULL,
    stock_after NUMERIC(12,3) NOT NULL,
    reference_id UUID,
    reference_type VARCHAR(50),
    notes VARCHAR(500),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_stock_movement_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_stock_movement_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX idx_stock_movement_business ON stock_movements(business_id);
CREATE INDEX idx_stock_movement_product ON stock_movements(product_id);
CREATE INDEX idx_stock_movement_type ON stock_movements(movement_type);
CREATE INDEX idx_stock_movement_created ON stock_movements(created_at);

-- ── Sales ────────────────────────────────────────────────────
CREATE TABLE sales (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    transaction_number VARCHAR(50) NOT NULL UNIQUE,
    employee_id UUID NOT NULL,
    customer_id UUID,
    sale_date TIMESTAMP NOT NULL,
    subtotal NUMERIC(14,2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(12,2) DEFAULT 0,
    loyalty_points_redeemed NUMERIC(12,2) DEFAULT 0,
    loyalty_discount NUMERIC(12,2) DEFAULT 0,
    total_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    amount_paid NUMERIC(14,2) NOT NULL DEFAULT 0,
    change_amount NUMERIC(12,2) DEFAULT 0,
    payment_method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    loyalty_points_earned NUMERIC(12,2) DEFAULT 0,
    is_locked BOOLEAN NOT NULL DEFAULT FALSE,
    notes VARCHAR(1000),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_sale_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_sale_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_sale_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX idx_sale_business_id ON sales(business_id);
CREATE INDEX idx_sale_transaction_number ON sales(transaction_number);
CREATE INDEX idx_sale_employee_id ON sales(employee_id);
CREATE INDEX idx_sale_customer_id ON sales(customer_id);
CREATE INDEX idx_sale_date ON sales(sale_date);
CREATE INDEX idx_sale_status ON sales(status);

-- ── Sale Items ───────────────────────────────────────────────
CREATE TABLE sale_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    sale_id UUID NOT NULL,
    product_id UUID NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    cost_price NUMERIC(12,2) NOT NULL,
    discount_amount NUMERIC(12,2) DEFAULT 0,
    tax_amount NUMERIC(12,2) DEFAULT 0,
    tax_percentage NUMERIC(5,2) DEFAULT 0,
    line_total NUMERIC(14,2) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_sale_item_sale FOREIGN KEY (sale_id) REFERENCES sales(id),
    CONSTRAINT fk_sale_item_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_sale_item_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX idx_sale_item_business ON sale_items(business_id);
CREATE INDEX idx_sale_item_product ON sale_items(product_id);

-- ── Refunds ──────────────────────────────────────────────────
CREATE TABLE refunds (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    refund_number VARCHAR(50) NOT NULL UNIQUE,
    original_sale_id UUID NOT NULL,
    original_transaction_number VARCHAR(50) NOT NULL,
    refund_date TIMESTAMP NOT NULL,
    refund_amount NUMERIC(14,2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    refund_type VARCHAR(20) NOT NULL,
    processed_by_id UUID NOT NULL,
    notes VARCHAR(1000),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_refund_sale FOREIGN KEY (original_sale_id) REFERENCES sales(id),
    CONSTRAINT fk_refund_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX idx_refund_business_id ON refunds(business_id);
CREATE INDEX idx_refund_sale_id ON refunds(original_sale_id);
CREATE INDEX idx_refund_date ON refunds(refund_date);

-- ── Refund Items ─────────────────────────────────────────────
CREATE TABLE refund_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    refund_id UUID NOT NULL,
    product_id UUID NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity NUMERIC(12,3) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    refund_amount NUMERIC(14,2) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_refund_item_refund FOREIGN KEY (refund_id) REFERENCES refunds(id),
    CONSTRAINT fk_refund_item_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX idx_refund_item_business ON refund_items(business_id);
CREATE INDEX idx_refund_item_product ON refund_items(product_id);

-- ── Loyalty Configs ──────────────────────────────────────────
CREATE TABLE loyalty_configs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    points_per_currency_unit NUMERIC(10,4) NOT NULL DEFAULT 0.01,
    currency_units_per_point NUMERIC(10,2) NOT NULL DEFAULT 1,
    minimum_purchase_for_points NUMERIC(10,2) DEFAULT 0,
    minimum_points_for_redemption NUMERIC(10,2) DEFAULT 10,
    max_redemption_percentage NUMERIC(5,2) DEFAULT 50,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uk_loyalty_config_business UNIQUE (business_id),
    CONSTRAINT fk_loyalty_config_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

-- ── Loyalty Transactions ─────────────────────────────────────
CREATE TABLE loyalty_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    points NUMERIC(12,2) NOT NULL,
    balance_after NUMERIC(12,2) NOT NULL,
    sale_id UUID,
    sale_amount NUMERIC(14,2),
    description VARCHAR(500),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_loyalty_txn_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_loyalty_txn_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX idx_loyalty_txn_business ON loyalty_transactions(business_id);
CREATE INDEX idx_loyalty_txn_customer ON loyalty_transactions(customer_id);
CREATE INDEX idx_loyalty_txn_type ON loyalty_transactions(transaction_type);

-- ── Salary Payouts ───────────────────────────────────────────
CREATE TABLE salary_payouts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    base_amount NUMERIC(12,2) NOT NULL,
    days_worked INT,
    hours_worked NUMERIC(8,2),
    overtime_hours NUMERIC(8,2) DEFAULT 0,
    overtime_amount NUMERIC(12,2) DEFAULT 0,
    sales_amount NUMERIC(14,2) DEFAULT 0,
    incentive_percentage NUMERIC(5,2) DEFAULT 0,
    incentive_amount NUMERIC(12,2) DEFAULT 0,
    bonus NUMERIC(12,2) DEFAULT 0,
    deductions NUMERIC(12,2) DEFAULT 0,
    deduction_reason VARCHAR(500),
    gross_salary NUMERIC(14,2) NOT NULL,
    net_salary NUMERIC(14,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_reference VARCHAR(100),
    paid_on DATE,
    notes VARCHAR(1000),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_salary_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_salary_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX idx_salary_business_id ON salary_payouts(business_id);
CREATE INDEX idx_salary_employee_id ON salary_payouts(employee_id);
CREATE INDEX idx_salary_period ON salary_payouts(period_start, period_end);
CREATE INDEX idx_salary_status ON salary_payouts(status);

-- ── Admin Configs ────────────────────────────────────────────
CREATE TABLE admin_configs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    config_key VARCHAR(100) NOT NULL,
    config_value VARCHAR(2000) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    data_type VARCHAR(30) NOT NULL DEFAULT 'STRING',
    is_editable BOOLEAN NOT NULL DEFAULT TRUE,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uk_admin_config_key_business UNIQUE (config_key, business_id),
    CONSTRAINT fk_admin_config_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX idx_admin_config_business ON admin_configs(business_id);
CREATE INDEX idx_admin_config_category ON admin_configs(category);

-- ── Audit Logs ───────────────────────────────────────────────
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID,
    description VARCHAR(2000) NOT NULL,
    performed_by UUID,
    ip_address VARCHAR(50),
    old_value TEXT,
    new_value TEXT,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    CONSTRAINT fk_audit_business FOREIGN KEY (business_id) REFERENCES businesses(id)
);

CREATE INDEX idx_audit_business_id ON audit_logs(business_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_entity_id ON audit_logs(entity_id);
CREATE INDEX idx_audit_performed_by ON audit_logs(performed_by);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at);
