// ══════════════════════════════════════════════════════════════
// TRIKAAR DATA STORE — localStorage-powered business data layer
// ══════════════════════════════════════════════════════════════

const KEYS = {
    EMPLOYEES: 'trikaar_employees',
    CUSTOMERS: 'trikaar_customers',
    PRODUCTS: 'trikaar_products',
    SALES: 'trikaar_sales',
    ATTENDANCE: 'trikaar_attendance',
    SALARY: 'trikaar_salary',
    LOYALTY_TXN: 'trikaar_loyalty_txn',
    AUDIT: 'trikaar_audit',
    CONFIG: 'trikaar_config',
    USERS: 'trikaar_users',
    COUNTERS: 'trikaar_counters',
    SUPPLIERS: 'trikaar_suppliers',
    PURCHASES: 'trikaar_purchases',
    EXPENSES: 'trikaar_expenses',
    CASH_REGISTERS: 'trikaar_cash_registers',
    STOCK_TRANSFERS: 'trikaar_stock_transfers',
};

function get(key) {
    try { return JSON.parse(localStorage.getItem(key)) || []; }
    catch { return []; }
}
function set(key, data) { localStorage.setItem(key, JSON.stringify(data)); }
function getObj(key) {
    try { return JSON.parse(localStorage.getItem(key)) || {}; }
    catch { return {}; }
}

function nextId(prefix) {
    const counters = getObj(KEYS.COUNTERS);
    const num = (counters[prefix] || 0) + 1;
    counters[prefix] = num;
    set(KEYS.COUNTERS, counters);
    return `${prefix}-${String(num).padStart(3, '0')}`;
}

function uuid() { return crypto.randomUUID ? crypto.randomUUID() : Date.now().toString(36) + Math.random().toString(36).slice(2); }
function now() { return new Date().toISOString(); }
function today() { return new Date().toISOString().split('T')[0]; }

// ── Audit Logger ─────────────────────────
function logAudit(action, entityType, description, user) {
    const logs = get(KEYS.AUDIT);
    logs.unshift({ id: uuid(), action, entityType, description, performedBy: user || 'System', createdAt: now() });
    if (logs.length > 500) logs.length = 500;
    set(KEYS.AUDIT, logs);
}

// ══════════════════════════════════════════
// SEED DATA
// ══════════════════════════════════════════
function seedIfEmpty() {
    if (localStorage.getItem('trikaar_seeded_v2')) return;
    // Clear old seed if exists
    Object.values(KEYS).forEach(k => localStorage.removeItem(k));
    localStorage.removeItem('trikaar_seeded');

    // Users (login accounts)
    set(KEYS.USERS, [
        {
            id: 'u1', username: 'admin', password: 'admin123', fullName: 'Arjun Mehta', role: 'SUPER_ADMIN', employeeId: null,
            access: ['dashboard', 'employees', 'customers', 'inventory', 'sales', 'salary', 'loyalty', 'reports', 'config', 'audit', 'attendance', 'suppliers', 'purchases', 'expenses', 'cash_register']
        },
        {
            id: 'u2', username: 'vikram', password: 'pass123', fullName: 'Vikram Singh', role: 'MANAGER', employeeId: 'e1',
            access: ['dashboard', 'employees', 'customers', 'inventory', 'sales', 'salary', 'attendance', 'reports', 'suppliers', 'purchases', 'expenses', 'cash_register']
        },
        {
            id: 'u3', username: 'meena', password: 'pass123', fullName: 'Meena Kumari', role: 'CASHIER', employeeId: 'e2',
            access: ['dashboard', 'sales', 'customers', 'attendance', 'cash_register']
        },
        {
            id: 'u4', username: 'ramesh', password: 'pass123', fullName: 'Ramesh Babu', role: 'STOCK_MANAGER', employeeId: 'e3',
            access: ['dashboard', 'inventory', 'attendance', 'suppliers', 'purchases']
        },
        {
            id: 'u5', username: 'sneha', password: 'pass123', fullName: 'Sneha Kapoor', role: 'ACCOUNTANT', employeeId: 'e5',
            access: ['dashboard', 'salary', 'reports', 'audit', 'attendance', 'expenses', 'suppliers', 'purchases']
        },
    ]);

    // Employees
    set(KEYS.EMPLOYEES, [
        { id: 'e1', code: 'EMP-001', firstName: 'Vikram', lastName: 'Singh', email: 'vikram@trikaar.in', phone: '9876543210', department: 'Sales', designation: 'Sales Manager', employmentType: 'FULL_TIME', wageType: 'MONTHLY', baseSalary: 35000, hourlyRate: 0, dailyRate: 0, incentivePercent: 5, status: 'ACTIVE', dateOfJoining: '2024-01-15', address: 'Plot 12, Sector 5, Noida', bankAccount: 'HDFC-12345678', pan: 'ABCDE1234F', userId: 'u2', createdAt: '2024-01-15T10:00:00Z' },
        { id: 'e2', code: 'EMP-002', firstName: 'Meena', lastName: 'Kumari', email: 'meena@trikaar.in', phone: '9876543211', department: 'Billing', designation: 'Senior Cashier', employmentType: 'FULL_TIME', wageType: 'MONTHLY', baseSalary: 22000, hourlyRate: 0, dailyRate: 0, incentivePercent: 0, status: 'ACTIVE', dateOfJoining: '2024-03-01', address: '45 MG Road, Delhi', bankAccount: 'SBI-98765432', pan: 'FGHIJ5678K', userId: 'u3', createdAt: '2024-03-01T10:00:00Z' },
        { id: 'e3', code: 'EMP-003', firstName: 'Ramesh', lastName: 'Babu', email: 'ramesh@trikaar.in', phone: '9876543212', department: 'Inventory', designation: 'Stock Manager', employmentType: 'FULL_TIME', wageType: 'MONTHLY', baseSalary: 28000, hourlyRate: 0, dailyRate: 0, incentivePercent: 0, status: 'ACTIVE', dateOfJoining: '2024-02-01', address: '78 Gandhi Nagar, Mumbai', bankAccount: 'ICICI-11223344', pan: 'KLMNO9012P', userId: 'u4', createdAt: '2024-02-01T10:00:00Z' },
        { id: 'e4', code: 'EMP-004', firstName: 'Anjali', lastName: 'Patel', email: 'anjali@trikaar.in', phone: '9876543213', department: 'Sales', designation: 'Sales Associate', employmentType: 'PART_TIME', wageType: 'DAILY', baseSalary: 0, hourlyRate: 0, dailyRate: 800, incentivePercent: 3, status: 'ACTIVE', dateOfJoining: '2024-06-01', address: '12 CG Road, Ahmedabad', bankAccount: '', pan: '', userId: null, createdAt: '2024-06-01T10:00:00Z' },
        { id: 'e5', code: 'EMP-005', firstName: 'Sneha', lastName: 'Kapoor', email: 'sneha@trikaar.in', phone: '9876543214', department: 'Accounts', designation: 'Accountant', employmentType: 'FULL_TIME', wageType: 'MONTHLY', baseSalary: 30000, hourlyRate: 0, dailyRate: 0, incentivePercent: 0, status: 'ACTIVE', dateOfJoining: '2024-04-01', address: '23 Park Street, Kolkata', bankAccount: 'AXIS-55667788', pan: 'QRSTU3456V', userId: 'u5', createdAt: '2024-04-01T10:00:00Z' },
    ]);

    // Customers
    set(KEYS.CUSTOMERS, [
        { id: 'c1', firstName: 'Rajesh', lastName: 'Kumar', phone: '9998887770', email: 'rajesh@gmail.com', city: 'Mumbai', state: 'Maharashtra', pincode: '400001', gender: 'Male', loyaltyTier: 'GOLD', loyaltyPoints: 2450, totalSpent: 125000, totalVisits: 34, active: true, createdAt: '2024-02-01T10:00:00Z' },
        { id: 'c2', firstName: 'Priya', lastName: 'Sharma', phone: '9998887771', email: 'priya@gmail.com', city: 'Delhi', state: 'Delhi', pincode: '110001', gender: 'Female', loyaltyTier: 'SILVER', loyaltyPoints: 890, totalSpent: 52000, totalVisits: 18, active: true, createdAt: '2024-03-01T10:00:00Z' },
        { id: 'c3', firstName: 'Amit', lastName: 'Patel', phone: '9998887772', email: 'amit@gmail.com', city: 'Ahmedabad', state: 'Gujarat', pincode: '380001', gender: 'Male', loyaltyTier: 'PLATINUM', loyaltyPoints: 5200, totalSpent: 280000, totalVisits: 62, active: true, createdAt: '2024-01-15T10:00:00Z' },
        { id: 'c4', firstName: 'Sunita', lastName: 'Devi', phone: '9998887773', email: '', city: 'Jaipur', state: 'Rajasthan', pincode: '302001', gender: 'Female', loyaltyTier: 'BRONZE', loyaltyPoints: 120, totalSpent: 8500, totalVisits: 5, active: true, createdAt: '2024-07-01T10:00:00Z' },
        { id: 'c5', firstName: 'Mohd', lastName: 'Irfan', phone: '9998887774', email: 'irfan@yahoo.com', city: 'Lucknow', state: 'UP', pincode: '226001', gender: 'Male', loyaltyTier: 'GOLD', loyaltyPoints: 1800, totalSpent: 95000, totalVisits: 28, active: true, createdAt: '2024-04-01T10:00:00Z' },
    ]);

    // Products
    set(KEYS.PRODUCTS, [
        { id: 'p1', sku: 'SKU-001', barcode: '8901234560001', name: 'Samsung Galaxy A15', category: 'Electronics', brand: 'Samsung', costPrice: 12000, sellingPrice: 14999, stock: 25, reorderLevel: 10, unit: 'Pcs', taxRate: 18, location: 'Main Store', active: true, createdAt: '2024-01-15T10:00:00Z' },
        { id: 'p2', sku: 'SKU-002', barcode: '8901234560002', name: 'Colgate MaxFresh 150g', category: 'FMCG', brand: 'Colgate', costPrice: 80, sellingPrice: 120, stock: 200, reorderLevel: 50, unit: 'Pcs', taxRate: 12, location: 'Main Store', active: true, createdAt: '2024-01-15T10:00:00Z' },
        { id: 'p3', sku: 'SKU-003', barcode: '8901234560003', name: "Levi's 501 Jeans", category: 'Clothing', brand: "Levi's", costPrice: 2200, sellingPrice: 3499, stock: 8, reorderLevel: 15, unit: 'Pcs', taxRate: 5, location: 'Main Store', active: true, createdAt: '2024-02-01T10:00:00Z' },
        { id: 'p4', sku: 'SKU-004', barcode: '8901234560004', name: 'Boat Airdopes 131', category: 'Electronics', brand: 'Boat', costPrice: 600, sellingPrice: 999, stock: 3, reorderLevel: 10, unit: 'Pcs', taxRate: 18, location: 'Main Store', active: true, createdAt: '2024-02-01T10:00:00Z' },
        { id: 'p5', sku: 'SKU-005', barcode: '8901234560005', name: 'Tata Tea Gold 500g', category: 'Groceries', brand: 'Tata', costPrice: 180, sellingPrice: 250, stock: 85, reorderLevel: 30, unit: 'Pcs', taxRate: 5, location: 'Main Store', active: true, createdAt: '2024-03-01T10:00:00Z' },
        { id: 'p6', sku: 'SKU-006', barcode: '8901234560006', name: 'Nivea Men Cream 75ml', category: 'Cosmetics', brand: 'Nivea', costPrice: 140, sellingPrice: 199, stock: 45, reorderLevel: 20, unit: 'Pcs', taxRate: 18, location: 'Main Store', active: true, createdAt: '2024-03-01T10:00:00Z' },
        { id: 'p7', sku: 'SKU-007', barcode: '8901234560007', name: 'Classmate Notebook 200pg', category: 'Stationery', brand: 'Classmate', costPrice: 40, sellingPrice: 65, stock: 150, reorderLevel: 40, unit: 'Pcs', taxRate: 12, location: 'Main Store', active: true, createdAt: '2024-04-01T10:00:00Z' },
        { id: 'p8', sku: 'SKU-008', barcode: '8901234560008', name: 'Amul Butter 500g', category: 'Groceries', brand: 'Amul', costPrice: 220, sellingPrice: 280, stock: 60, reorderLevel: 25, unit: 'Pcs', taxRate: 5, location: 'Main Store', active: true, createdAt: '2024-04-01T10:00:00Z' },
    ]);

    // Suppliers
    set(KEYS.SUPPLIERS, [
        { id: 'sup1', name: 'Metro Electronics Pvt Ltd', contactPerson: 'Suresh Reddy', phone: '9112233440', email: 'suresh@metroelec.in', gstin: '27AABCM0001A1Z5', address: '45 Industrial Area, Pune', city: 'Pune', state: 'Maharashtra', paymentTerms: 30, category: 'Electronics', balance: 0, active: true, createdAt: '2024-01-10T10:00:00Z' },
        { id: 'sup2', name: 'National FMCG Distributors', contactPerson: 'Manoj Gupta', phone: '9112233441', email: 'manoj@natfmcg.in', gstin: '07AABCN0002B1Z3', address: '78 Kirti Nagar, Delhi', city: 'Delhi', state: 'Delhi', paymentTerms: 15, category: 'FMCG', balance: 24500, active: true, createdAt: '2024-02-01T10:00:00Z' },
        { id: 'sup3', name: 'Fashion Forward Exports', contactPerson: 'Ritu Agarwal', phone: '9112233442', email: 'ritu@fashionfw.in', gstin: '24AABCF0003C1Z1', address: '12 Textile Market, Surat', city: 'Surat', state: 'Gujarat', paymentTerms: 45, category: 'Clothing', balance: 55000, active: true, createdAt: '2024-01-20T10:00:00Z' },
        { id: 'sup4', name: 'Amul Dairy Distributors', contactPerson: 'Kiran Patel', phone: '9112233443', email: 'kiran@amuld.in', gstin: '24AABCA0004D1Z8', address: '5 Dairy Road, Anand', city: 'Anand', state: 'Gujarat', paymentTerms: 7, category: 'Groceries', balance: 0, active: true, createdAt: '2024-03-01T10:00:00Z' },
    ]);

    // Purchase Orders
    set(KEYS.PURCHASES, [
        {
            id: 'po1', poNumber: 'PO-001', supplierId: 'sup1', supplierName: 'Metro Electronics Pvt Ltd', items: [
                { productId: 'p1', name: 'Samsung Galaxy A15', qty: 20, unitCost: 11800, tax: 2124, total: 237724 },
                { productId: 'p4', name: 'Boat Airdopes 131', qty: 15, unitCost: 580, tax: 1044, total: 9744 },
            ], subtotal: 245300, taxTotal: 3168, grandTotal: 248468, paymentStatus: 'PAID', orderStatus: 'RECEIVED', invoiceNo: 'INV-ME-2401', dueDate: '2026-02-15', receivedAt: '2026-01-20T10:00:00Z', createdAt: '2026-01-15T10:00:00Z'
        },
        {
            id: 'po2', poNumber: 'PO-002', supplierId: 'sup2', supplierName: 'National FMCG Distributors', items: [
                { productId: 'p2', name: 'Colgate MaxFresh 150g', qty: 100, unitCost: 78, tax: 936, total: 8736 },
                { productId: 'p5', name: 'Tata Tea Gold 500g', qty: 50, unitCost: 175, tax: 437, total: 9187 },
            ], subtotal: 16550, taxTotal: 1373, grandTotal: 17923, paymentStatus: 'PARTIAL', orderStatus: 'RECEIVED', invoiceNo: 'INV-NF-2402', dueDate: '2026-03-10', receivedAt: '2026-02-20T10:00:00Z', createdAt: '2026-02-18T10:00:00Z', paidAmount: 0
        },
        {
            id: 'po3', poNumber: 'PO-003', supplierId: 'sup3', supplierName: 'Fashion Forward Exports', items: [
                { productId: 'p3', name: "Levi's 501 Jeans", qty: 30, unitCost: 2100, tax: 3150, total: 66150 },
            ], subtotal: 63000, taxTotal: 3150, grandTotal: 66150, paymentStatus: 'UNPAID', orderStatus: 'ORDERED', invoiceNo: '', dueDate: '2026-04-05', receivedAt: null, createdAt: '2026-02-24T10:00:00Z', paidAmount: 0
        },
    ]);

    // Expenses
    set(KEYS.EXPENSES, [
        { id: 'exp1', category: 'Rent', description: 'Monthly shop rent', amount: 45000, paymentMethod: 'Bank Transfer', date: '2026-02-01', recurring: true, createdAt: '2026-02-01T10:00:00Z' },
        { id: 'exp2', category: 'Electricity', description: 'Electricity bill Feb', amount: 8500, paymentMethod: 'UPI', date: '2026-02-05', recurring: true, createdAt: '2026-02-05T10:00:00Z' },
        { id: 'exp3', category: 'Internet', description: 'Airtel broadband', amount: 1499, paymentMethod: 'UPI', date: '2026-02-03', recurring: true, createdAt: '2026-02-03T10:00:00Z' },
        { id: 'exp4', category: 'Maintenance', description: 'AC servicing', amount: 3500, paymentMethod: 'Cash', date: '2026-02-10', recurring: false, createdAt: '2026-02-10T10:00:00Z' },
        { id: 'exp5', category: 'Tea & Snacks', description: 'Staff daily tea/coffee', amount: 2400, paymentMethod: 'Cash', date: '2026-02-15', recurring: true, createdAt: '2026-02-15T10:00:00Z' },
        { id: 'exp6', category: 'Packaging', description: 'Carry bags & packaging material', amount: 1800, paymentMethod: 'Cash', date: '2026-02-12', recurring: false, createdAt: '2026-02-12T10:00:00Z' },
    ]);

    // Cash Registers
    set(KEYS.CASH_REGISTERS, [
        { id: 'cr1', cashierId: 'e2', cashierName: 'Meena Kumari', date: '2026-02-25', openingAmount: 5000, closingAmount: 23650, expectedAmount: 23800, difference: -150, salesCash: 18800, status: 'CLOSED', openedAt: '2026-02-25T09:00:00Z', closedAt: '2026-02-25T18:00:00Z', notes: 'Short by ₹150' },
        { id: 'cr2', cashierId: 'e1', cashierName: 'Vikram Singh', date: '2026-02-25', openingAmount: 3000, closingAmount: 15200, expectedAmount: 15200, difference: 0, salesCash: 12200, status: 'CLOSED', openedAt: '2026-02-25T09:00:00Z', closedAt: '2026-02-25T18:00:00Z', notes: 'Balanced' },
    ]);

    // Stock Transfers
    set(KEYS.STOCK_TRANSFERS, [
        { id: 'st1', transferNo: 'TRF-001', fromLocation: 'Main Store', toLocation: 'Branch - Andheri', items: [{ productId: 'p2', name: 'Colgate MaxFresh 150g', qty: 30 }, { productId: 'p7', name: 'Classmate Notebook 200pg', qty: 20 }], status: 'COMPLETED', transferredBy: 'Ramesh Babu', createdAt: '2026-02-20T10:00:00Z' },
    ]);

    // Sales
    const saleItems1 = [
        { productId: 'p1', name: 'Samsung Galaxy A15', qty: 1, price: 14999, tax: 2700, total: 17699 },
        { productId: 'p2', name: 'Colgate MaxFresh 150g', qty: 2, price: 120, tax: 29, total: 269 },
    ];
    const saleItems2 = [
        { productId: 'p5', name: 'Tata Tea Gold 500g', qty: 3, price: 250, tax: 38, total: 788 },
    ];
    set(KEYS.SALES, [
        { id: 's1', txnNo: 'TXN-001', customerId: 'c1', customerName: 'Rajesh Kumar', employeeId: 'e1', employeeName: 'Vikram Singh', items: saleItems1, subtotal: 15239, taxTotal: 2729, discount: 0, loyaltyPointsUsed: 0, grandTotal: 17968, paymentMethod: 'UPI', status: 'COMPLETED', createdAt: '2026-02-26T10:30:00Z' },
        { id: 's2', txnNo: 'TXN-002', customerId: 'c2', customerName: 'Priya Sharma', employeeId: 'e2', employeeName: 'Meena Kumari', items: saleItems2, subtotal: 750, taxTotal: 38, discount: 0, loyaltyPointsUsed: 0, grandTotal: 788, paymentMethod: 'Cash', status: 'COMPLETED', createdAt: '2026-02-26T11:15:00Z' },
    ]);

    // Attendance
    set(KEYS.ATTENDANCE, [
        { id: 'a1', employeeId: 'e1', employeeName: 'Vikram Singh', date: '2026-02-25', checkIn: '09:05', checkOut: '18:30', status: 'PRESENT', overtimeHours: 1.5, notes: '' },
        { id: 'a2', employeeId: 'e2', employeeName: 'Meena Kumari', date: '2026-02-25', checkIn: '09:00', checkOut: '18:00', status: 'PRESENT', overtimeHours: 0, notes: '' },
        { id: 'a3', employeeId: 'e3', employeeName: 'Ramesh Babu', date: '2026-02-25', checkIn: '09:10', checkOut: '18:00', status: 'PRESENT', overtimeHours: 0, notes: '' },
        { id: 'a4', employeeId: 'e4', employeeName: 'Anjali Patel', date: '2026-02-25', checkIn: null, checkOut: null, status: 'ABSENT', overtimeHours: 0, notes: 'Personal leave' },
        { id: 'a5', employeeId: 'e5', employeeName: 'Sneha Kapoor', date: '2026-02-25', checkIn: '09:00', checkOut: '18:00', status: 'PRESENT', overtimeHours: 0, notes: '' },
    ]);

    // Salary
    set(KEYS.SALARY, [
        { id: 'sal1', employeeId: 'e1', employeeName: 'Vikram Singh', period: 'Jan 2026', base: 35000, overtime: 3500, incentive: 2800, bonus: 0, deductions: 500, net: 40800, status: 'PAID', paidOn: '2026-01-31', createdAt: '2026-01-28T10:00:00Z' },
        { id: 'sal2', employeeId: 'e2', employeeName: 'Meena Kumari', period: 'Jan 2026', base: 22000, overtime: 0, incentive: 0, bonus: 1000, deductions: 0, net: 23000, status: 'PAID', paidOn: '2026-01-31', createdAt: '2026-01-28T10:00:00Z' },
    ]);

    // Config
    set(KEYS.CONFIG, [
        { id: 'cfg1', key: 'business.name', value: 'Trikaar Retail Store', category: 'Business', editable: true },
        { id: 'cfg2', key: 'business.gstin', value: '27AABCT0001B1Z5', category: 'Business', editable: true },
        { id: 'cfg3', key: 'business.phone', value: '+91 9876500000', category: 'Business', editable: true },
        { id: 'cfg4', key: 'business.address', value: '123 MG Road, Mumbai, Maharashtra 400001', category: 'Business', editable: true },
        { id: 'cfg5', key: 'tax.default_gst', value: '18', category: 'Tax', editable: true },
        { id: 'cfg6', key: 'loyalty.points_per_rupee', value: '0.01', category: 'Loyalty', editable: true },
        { id: 'cfg7', key: 'loyalty.point_value', value: '1', category: 'Loyalty', editable: true },
        { id: 'cfg8', key: 'loyalty.silver_threshold', value: '25000', category: 'Loyalty', editable: true },
        { id: 'cfg9', key: 'loyalty.gold_threshold', value: '75000', category: 'Loyalty', editable: true },
        { id: 'cfg10', key: 'loyalty.platinum_threshold', value: '200000', category: 'Loyalty', editable: true },
        { id: 'cfg11', key: 'invoice.prefix', value: 'TXN', category: 'Invoice', editable: true },
        { id: 'cfg12', key: 'system.currency', value: 'INR', category: 'System', editable: false },
        { id: 'cfg13', key: 'system.timezone', value: 'Asia/Kolkata', category: 'System', editable: false },
        { id: 'cfg14', key: 'store.locations', value: 'Main Store,Branch - Andheri,Warehouse - Bhiwandi', category: 'Store', editable: true },
    ]);

    set(KEYS.COUNTERS, { 'EMP': 5, 'TXN': 2, 'SAL': 2, 'PO': 3, 'TRF': 1 });
    set(KEYS.LOYALTY_TXN, []);

    logAudit('SYSTEM_INIT', 'System', 'System initialized with seed data v2', 'System');
    localStorage.setItem('trikaar_seeded_v2', 'true');
}

// ══════════════════════════════════════════
// CRUD OPERATIONS
// ══════════════════════════════════════════

export const store = {
    init: seedIfEmpty,
    reset: () => { Object.values(KEYS).forEach(k => localStorage.removeItem(k)); localStorage.removeItem('trikaar_seeded'); localStorage.removeItem('trikaar_seeded_v2'); seedIfEmpty(); },

    // ── Users ─────────────────────
    getUsers: () => get(KEYS.USERS),
    getUserByUsername: (u) => get(KEYS.USERS).find(x => x.username === u),
    addUser: (user) => { const users = get(KEYS.USERS); user.id = uuid(); users.push(user); set(KEYS.USERS, users); return user; },
    updateUser: (id, data) => { const users = get(KEYS.USERS); const i = users.findIndex(u => u.id === id); if (i >= 0) { users[i] = { ...users[i], ...data }; set(KEYS.USERS, users); } return users[i]; },
    deleteUser: (id) => { set(KEYS.USERS, get(KEYS.USERS).filter(u => u.id !== id)); },

    // ── Employees ─────────────────
    getEmployees: () => get(KEYS.EMPLOYEES),
    getEmployee: (id) => get(KEYS.EMPLOYEES).find(e => e.id === id),
    addEmployee: (emp, currentUser) => {
        const employees = get(KEYS.EMPLOYEES);
        emp.id = uuid();
        emp.code = nextId('EMP');
        emp.status = 'ACTIVE';
        emp.createdAt = now();
        employees.push(emp);
        set(KEYS.EMPLOYEES, employees);
        const username = (emp.firstName + emp.lastName.charAt(0)).toLowerCase().replace(/\s/g, '');
        const password = 'pass123';
        const user = { id: uuid(), username, password, fullName: `${emp.firstName} ${emp.lastName}`, role: emp.role || 'WORKER', employeeId: emp.id, access: emp.access || ['dashboard', 'attendance'] };
        const users = get(KEYS.USERS);
        users.push(user);
        set(KEYS.USERS, users);
        emp.userId = user.id;
        emp.loginUsername = username;
        emp.loginPassword = password;
        set(KEYS.EMPLOYEES, employees);
        logAudit('EMPLOYEE_CREATED', 'Employee', `${emp.code} ${emp.firstName} ${emp.lastName} created`, currentUser);
        return { employee: emp, credentials: { username, password } };
    },
    updateEmployee: (id, data, currentUser) => {
        const employees = get(KEYS.EMPLOYEES);
        const i = employees.findIndex(e => e.id === id);
        if (i >= 0) {
            employees[i] = { ...employees[i], ...data };
            set(KEYS.EMPLOYEES, employees);
            if (data.access) {
                const users = get(KEYS.USERS);
                const ui = users.findIndex(u => u.employeeId === id);
                if (ui >= 0) { users[ui].access = data.access; if (data.role) users[ui].role = data.role; set(KEYS.USERS, users); }
            }
            logAudit('EMPLOYEE_UPDATED', 'Employee', `${employees[i].code} updated`, currentUser);
        }
        return employees[i];
    },
    deleteEmployee: (id, currentUser) => {
        const employees = get(KEYS.EMPLOYEES);
        const emp = employees.find(e => e.id === id);
        if (emp) { emp.status = 'TERMINATED'; set(KEYS.EMPLOYEES, employees); logAudit('EMPLOYEE_TERMINATED', 'Employee', `${emp.code} terminated`, currentUser); }
    },

    // ── Customers ─────────────────
    getCustomers: () => get(KEYS.CUSTOMERS),
    getCustomer: (id) => get(KEYS.CUSTOMERS).find(c => c.id === id),
    addCustomer: (cust, currentUser) => {
        const customers = get(KEYS.CUSTOMERS);
        cust.id = uuid(); cust.loyaltyTier = 'BRONZE'; cust.loyaltyPoints = 0; cust.totalSpent = 0; cust.totalVisits = 0; cust.active = true; cust.createdAt = now();
        customers.push(cust); set(KEYS.CUSTOMERS, customers);
        logAudit('CUSTOMER_CREATED', 'Customer', `${cust.firstName} ${cust.lastName} created`, currentUser);
        return cust;
    },
    updateCustomer: (id, data, currentUser) => {
        const customers = get(KEYS.CUSTOMERS);
        const i = customers.findIndex(c => c.id === id);
        if (i >= 0) { customers[i] = { ...customers[i], ...data }; set(KEYS.CUSTOMERS, customers); logAudit('CUSTOMER_UPDATED', 'Customer', `${customers[i].firstName} updated`, currentUser); }
        return customers[i];
    },
    deleteCustomer: (id, currentUser) => {
        const customers = get(KEYS.CUSTOMERS);
        const c = customers.find(x => x.id === id);
        if (c) { c.active = false; set(KEYS.CUSTOMERS, customers); logAudit('CUSTOMER_DEACTIVATED', 'Customer', `${c.firstName} deactivated`, currentUser); }
    },

    // ── Products ──────────────────
    getProducts: () => get(KEYS.PRODUCTS),
    getProduct: (id) => get(KEYS.PRODUCTS).find(p => p.id === id),
    addProduct: (prod, currentUser) => {
        const products = get(KEYS.PRODUCTS);
        prod.id = uuid(); prod.active = true; prod.createdAt = now();
        products.push(prod); set(KEYS.PRODUCTS, products);
        logAudit('PRODUCT_CREATED', 'Product', `${prod.sku} ${prod.name} added`, currentUser);
        return prod;
    },
    updateProduct: (id, data, currentUser) => {
        const products = get(KEYS.PRODUCTS);
        const i = products.findIndex(p => p.id === id);
        if (i >= 0) { products[i] = { ...products[i], ...data }; set(KEYS.PRODUCTS, products); logAudit('PRODUCT_UPDATED', 'Product', `${products[i].sku} updated`, currentUser); }
        return products[i];
    },
    updateStock: (id, qty, currentUser) => {
        const products = get(KEYS.PRODUCTS);
        const i = products.findIndex(p => p.id === id);
        if (i >= 0) { products[i].stock += qty; set(KEYS.PRODUCTS, products); logAudit('STOCK_UPDATED', 'Product', `${products[i].sku} stock ${qty > 0 ? '+' : ''}${qty} → ${products[i].stock}`, currentUser); }
    },

    // ── Sales ─────────────────────
    getSales: () => get(KEYS.SALES),
    createSale: (sale, currentUser) => {
        const sales = get(KEYS.SALES);
        sale.id = uuid(); sale.txnNo = nextId('TXN'); sale.status = 'COMPLETED'; sale.createdAt = now();
        sales.unshift(sale); set(KEYS.SALES, sales);
        const products = get(KEYS.PRODUCTS);
        sale.items.forEach(item => { const pi = products.findIndex(p => p.id === item.productId); if (pi >= 0) products[pi].stock -= item.qty; });
        set(KEYS.PRODUCTS, products);
        if (sale.customerId) {
            const customers = get(KEYS.CUSTOMERS);
            const ci = customers.findIndex(c => c.id === sale.customerId);
            if (ci >= 0) {
                customers[ci].totalSpent += sale.grandTotal; customers[ci].totalVisits += 1;
                customers[ci].loyaltyPoints += Math.floor(sale.grandTotal * 0.01);
                const spent = customers[ci].totalSpent;
                if (spent >= 200000) customers[ci].loyaltyTier = 'PLATINUM';
                else if (spent >= 75000) customers[ci].loyaltyTier = 'GOLD';
                else if (spent >= 25000) customers[ci].loyaltyTier = 'SILVER';
                set(KEYS.CUSTOMERS, customers);
            }
        }
        logAudit('SALE_CREATED', 'Sale', `${sale.txnNo} ₹${sale.grandTotal} (${sale.paymentMethod})`, currentUser);
        return sale;
    },
    refundSale: (saleId, currentUser) => {
        const sales = get(KEYS.SALES);
        const si = sales.findIndex(s => s.id === saleId);
        if (si >= 0) {
            sales[si].status = 'REFUNDED'; set(KEYS.SALES, sales);
            const products = get(KEYS.PRODUCTS);
            sales[si].items.forEach(item => { const pi = products.findIndex(p => p.id === item.productId); if (pi >= 0) products[pi].stock += item.qty; });
            set(KEYS.PRODUCTS, products);
            logAudit('SALE_REFUNDED', 'Sale', `${sales[si].txnNo} refunded`, currentUser);
        }
    },

    // ── Attendance ────────────────
    getAttendance: () => get(KEYS.ATTENDANCE),
    markAttendance: (record, currentUser) => {
        const attendance = get(KEYS.ATTENDANCE);
        const existing = attendance.findIndex(a => a.employeeId === record.employeeId && a.date === record.date);
        if (existing >= 0) { attendance[existing] = { ...attendance[existing], ...record }; }
        else { record.id = uuid(); attendance.unshift(record); }
        set(KEYS.ATTENDANCE, attendance);
        logAudit('ATTENDANCE_MARKED', 'Attendance', `${record.employeeName} ${record.status} on ${record.date}`, currentUser);
    },

    // ── Salary ────────────────────
    getSalary: () => get(KEYS.SALARY),
    generateSalary: (payout, currentUser) => {
        const salaries = get(KEYS.SALARY);
        payout.id = uuid(); payout.status = 'PENDING'; payout.paidOn = null; payout.createdAt = now();
        salaries.unshift(payout); set(KEYS.SALARY, salaries);
        logAudit('SALARY_GENERATED', 'Salary', `${payout.employeeName} ${payout.period} ₹${payout.net}`, currentUser);
        return payout;
    },
    approveSalary: (id, currentUser) => {
        const salaries = get(KEYS.SALARY);
        const i = salaries.findIndex(s => s.id === id);
        if (i >= 0) { salaries[i].status = 'APPROVED'; set(KEYS.SALARY, salaries); logAudit('SALARY_APPROVED', 'Salary', `${salaries[i].employeeName} approved`, currentUser); }
    },
    paySalary: (id, currentUser) => {
        const salaries = get(KEYS.SALARY);
        const i = salaries.findIndex(s => s.id === id);
        if (i >= 0) { salaries[i].status = 'PAID'; salaries[i].paidOn = today(); set(KEYS.SALARY, salaries); logAudit('SALARY_PAID', 'Salary', `${salaries[i].employeeName} ₹${salaries[i].net} paid`, currentUser); }
    },

    // ── Suppliers ──────────────────
    getSuppliers: () => get(KEYS.SUPPLIERS),
    getSupplier: (id) => get(KEYS.SUPPLIERS).find(s => s.id === id),
    addSupplier: (sup, currentUser) => {
        const suppliers = get(KEYS.SUPPLIERS);
        sup.id = uuid(); sup.balance = 0; sup.active = true; sup.createdAt = now();
        suppliers.push(sup); set(KEYS.SUPPLIERS, suppliers);
        logAudit('SUPPLIER_CREATED', 'Supplier', `${sup.name} added`, currentUser);
        return sup;
    },
    updateSupplier: (id, data, currentUser) => {
        const suppliers = get(KEYS.SUPPLIERS);
        const i = suppliers.findIndex(s => s.id === id);
        if (i >= 0) { suppliers[i] = { ...suppliers[i], ...data }; set(KEYS.SUPPLIERS, suppliers); logAudit('SUPPLIER_UPDATED', 'Supplier', `${suppliers[i].name} updated`, currentUser); }
        return suppliers[i];
    },

    // ── Purchase Orders ────────────
    getPurchases: () => get(KEYS.PURCHASES),
    createPurchase: (po, currentUser) => {
        const purchases = get(KEYS.PURCHASES);
        po.id = uuid(); po.poNumber = nextId('PO'); po.orderStatus = 'ORDERED'; po.paymentStatus = 'UNPAID'; po.paidAmount = 0; po.createdAt = now();
        purchases.unshift(po); set(KEYS.PURCHASES, purchases);
        // Update supplier balance
        const suppliers = get(KEYS.SUPPLIERS);
        const si = suppliers.findIndex(s => s.id === po.supplierId);
        if (si >= 0) { suppliers[si].balance += po.grandTotal; set(KEYS.SUPPLIERS, suppliers); }
        logAudit('PO_CREATED', 'Purchase', `${po.poNumber} ₹${po.grandTotal} for ${po.supplierName}`, currentUser);
        return po;
    },
    receivePurchase: (id, currentUser) => {
        const purchases = get(KEYS.PURCHASES);
        const i = purchases.findIndex(p => p.id === id);
        if (i >= 0 && purchases[i].orderStatus === 'ORDERED') {
            purchases[i].orderStatus = 'RECEIVED'; purchases[i].receivedAt = now();
            set(KEYS.PURCHASES, purchases);
            // Increase stock and update cost price
            const products = get(KEYS.PRODUCTS);
            purchases[i].items.forEach(item => {
                const pi = products.findIndex(p => p.id === item.productId);
                if (pi >= 0) {
                    const oldStock = products[pi].stock;
                    const oldCost = products[pi].costPrice;
                    const newStock = oldStock + item.qty;
                    products[pi].costPrice = Math.round((oldCost * oldStock + item.unitCost * item.qty) / newStock);
                    products[pi].stock = newStock;
                }
            });
            set(KEYS.PRODUCTS, products);
            logAudit('PO_RECEIVED', 'Purchase', `${purchases[i].poNumber} received — stock updated`, currentUser);
        }
    },
    payPurchase: (id, amount, currentUser) => {
        const purchases = get(KEYS.PURCHASES);
        const i = purchases.findIndex(p => p.id === id);
        if (i >= 0) {
            purchases[i].paidAmount = (purchases[i].paidAmount || 0) + amount;
            purchases[i].paymentStatus = purchases[i].paidAmount >= purchases[i].grandTotal ? 'PAID' : 'PARTIAL';
            set(KEYS.PURCHASES, purchases);
            // Reduce supplier balance
            const suppliers = get(KEYS.SUPPLIERS);
            const si = suppliers.findIndex(s => s.id === purchases[i].supplierId);
            if (si >= 0) { suppliers[si].balance = Math.max(0, suppliers[si].balance - amount); set(KEYS.SUPPLIERS, suppliers); }
            logAudit('PO_PAYMENT', 'Purchase', `${purchases[i].poNumber} paid ₹${amount}`, currentUser);
        }
    },

    // ── Expenses ──────────────────
    getExpenses: () => get(KEYS.EXPENSES),
    addExpense: (exp, currentUser) => {
        const expenses = get(KEYS.EXPENSES);
        exp.id = uuid(); exp.createdAt = now();
        expenses.unshift(exp); set(KEYS.EXPENSES, expenses);
        logAudit('EXPENSE_ADDED', 'Expense', `${exp.category}: ₹${exp.amount} — ${exp.description}`, currentUser);
        return exp;
    },
    deleteExpense: (id, currentUser) => {
        const expenses = get(KEYS.EXPENSES);
        const exp = expenses.find(e => e.id === id);
        set(KEYS.EXPENSES, expenses.filter(e => e.id !== id));
        if (exp) logAudit('EXPENSE_DELETED', 'Expense', `${exp.category}: ₹${exp.amount} deleted`, currentUser);
    },

    // ── Cash Register ──────────────
    getCashRegisters: () => get(KEYS.CASH_REGISTERS),
    openRegister: (data, currentUser) => {
        const registers = get(KEYS.CASH_REGISTERS);
        data.id = uuid(); data.status = 'OPEN'; data.openedAt = now(); data.closingAmount = 0; data.expectedAmount = 0; data.difference = 0; data.salesCash = 0;
        registers.unshift(data); set(KEYS.CASH_REGISTERS, registers);
        logAudit('REGISTER_OPENED', 'CashRegister', `${data.cashierName} opened with ₹${data.openingAmount}`, currentUser);
        return data;
    },
    closeRegister: (id, closingAmount, currentUser) => {
        const registers = get(KEYS.CASH_REGISTERS);
        const i = registers.findIndex(r => r.id === id);
        if (i >= 0) {
            const sales = get(KEYS.SALES);
            const regDate = registers[i].date;
            const cashSales = sales.filter(s => s.status === 'COMPLETED' && s.paymentMethod === 'Cash' && s.createdAt?.startsWith(regDate)).reduce((s, x) => s + x.grandTotal, 0);
            registers[i].salesCash = cashSales;
            registers[i].expectedAmount = registers[i].openingAmount + cashSales;
            registers[i].closingAmount = closingAmount;
            registers[i].difference = closingAmount - registers[i].expectedAmount;
            registers[i].status = 'CLOSED';
            registers[i].closedAt = now();
            registers[i].notes = registers[i].difference === 0 ? 'Balanced' : registers[i].difference > 0 ? `Excess ₹${registers[i].difference}` : `Short by ₹${Math.abs(registers[i].difference)}`;
            set(KEYS.CASH_REGISTERS, registers);
            logAudit('REGISTER_CLOSED', 'CashRegister', `${registers[i].cashierName} closed — ${registers[i].notes}`, currentUser);
        }
    },

    // ── Stock Transfers ────────────
    getStockTransfers: () => get(KEYS.STOCK_TRANSFERS),
    createStockTransfer: (transfer, currentUser) => {
        const transfers = get(KEYS.STOCK_TRANSFERS);
        transfer.id = uuid(); transfer.transferNo = nextId('TRF'); transfer.status = 'COMPLETED'; transfer.createdAt = now();
        transfers.unshift(transfer); set(KEYS.STOCK_TRANSFERS, transfers);
        // Deduct stock from source
        const products = get(KEYS.PRODUCTS);
        transfer.items.forEach(item => {
            const pi = products.findIndex(p => p.id === item.productId);
            if (pi >= 0) products[pi].stock -= item.qty;
        });
        set(KEYS.PRODUCTS, products);
        logAudit('STOCK_TRANSFER', 'Transfer', `${transfer.transferNo}: ${transfer.fromLocation} → ${transfer.toLocation}`, currentUser);
        return transfer;
    },

    // ── Config ────────────────────
    getConfig: () => get(KEYS.CONFIG),
    updateConfig: (id, value, currentUser) => {
        const configs = get(KEYS.CONFIG);
        const i = configs.findIndex(c => c.id === id);
        if (i >= 0) { const old = configs[i].value; configs[i].value = value; set(KEYS.CONFIG, configs); logAudit('CONFIG_CHANGED', 'Config', `${configs[i].key}: ${old} → ${value}`, currentUser); }
    },

    // ── Audit ─────────────────────
    getAudit: () => get(KEYS.AUDIT),
    logAudit,
};

export default store;
