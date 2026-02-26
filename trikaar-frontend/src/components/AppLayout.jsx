import { NavLink, Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useState } from 'react';
import { LayoutDashboard, Users, UserCheck, Package, ShoppingCart, Heart, DollarSign, FileText, Settings, Shield, Search, Bell, LogOut, Calendar, Truck, ClipboardList, Receipt, Landmark, Menu, X } from 'lucide-react';

const navItems = [
    {
        section: 'Overview', items: [
            { to: '/', icon: LayoutDashboard, label: 'Dashboard', page: 'dashboard' },
        ]
    },
    {
        section: 'People', items: [
            { to: '/employees', icon: Users, label: 'Employees', page: 'employees' },
            { to: '/attendance', icon: Calendar, label: 'Attendance', page: 'attendance' },
            { to: '/customers', icon: UserCheck, label: 'Customers', page: 'customers' },
        ]
    },
    {
        section: 'Operations', items: [
            { to: '/inventory', icon: Package, label: 'Inventory', page: 'inventory' },
            { to: '/sales', icon: ShoppingCart, label: 'Sales & POS', page: 'sales' },
            { to: '/suppliers', icon: Truck, label: 'Suppliers', page: 'suppliers' },
            { to: '/purchases', icon: ClipboardList, label: 'Purchases', page: 'purchases' },
            { to: '/cash-register', icon: Landmark, label: 'Cash Register', page: 'cash_register' },
        ]
    },
    {
        section: 'Finance', items: [
            { to: '/salary', icon: DollarSign, label: 'Salary', page: 'salary' },
            { to: '/expenses', icon: Receipt, label: 'Expenses & P&L', page: 'expenses' },
            { to: '/loyalty', icon: Heart, label: 'Loyalty', page: 'loyalty' },
            { to: '/reports', icon: FileText, label: 'Reports', page: 'reports' },
        ]
    },
    {
        section: 'System', items: [
            { to: '/config', icon: Settings, label: 'Admin Config', page: 'config' },
            { to: '/audit', icon: Shield, label: 'Audit Logs', page: 'audit' },
        ]
    },
];

export default function AppLayout() {
    const { user, logout, hasAccess } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const [mobileOpen, setMobileOpen] = useState(false);
    const handleLogout = () => { logout(); navigate('/login'); };
    const initials = user?.fullName?.split(' ').map(n => n[0]).join('') || 'U';

    const closeMobile = () => setMobileOpen(false);

    return (
        <div className="app-layout">
            {/* Mobile hamburger */}
            <button className="mobile-menu-btn" onClick={() => setMobileOpen(!mobileOpen)}>
                {mobileOpen ? <X size={22} /> : <Menu size={22} />}
            </button>

            {/* Overlay */}
            {mobileOpen && <div className="mobile-overlay" onClick={closeMobile} />}

            <aside className={`sidebar ${mobileOpen ? 'sidebar-open' : ''}`}>
                <div className="sidebar-header">
                    <div className="logo-container">
                        <div className="logo-icon">T</div>
                        <div className="logo-text"><h1>TRIKAAR</h1><span>Retail Platform</span></div>
                    </div>
                </div>
                <nav className="sidebar-nav">
                    {navItems.map(section => {
                        const visible = section.items.filter(i => hasAccess(i.page));
                        if (!visible.length) return null;
                        return (
                            <div key={section.section}>
                                <div className="nav-section-title">{section.section}</div>
                                {visible.map(item => (
                                    <NavLink key={item.to} to={item.to} end={item.to === '/'} className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`} onClick={closeMobile}>
                                        <item.icon size={18} /> {item.label}
                                    </NavLink>
                                ))}
                            </div>
                        );
                    })}
                </nav>
                <div className="sidebar-footer">
                    <div className="user-card">
                        <div className="user-avatar">{initials}</div>
                        <div className="user-info" style={{ flex: 1 }}>
                            <h4>{user?.fullName || 'User'}</h4>
                            <span>{user?.role || 'USER'}</span>
                        </div>
                        <button className="icon-button" onClick={handleLogout} title="Logout" style={{ width: 32, height: 32 }}>
                            <LogOut size={16} />
                        </button>
                    </div>
                </div>
            </aside>
            <main className="main-content">
                <header className="topbar">
                    <div className="topbar-left"><h2>{getTitle(location.pathname)}</h2></div>
                    <div className="topbar-right">
                        <div className="search-bar hide-mobile"><Search size={16} style={{ color: 'var(--text-muted)' }} /><input placeholder="Search anything..." /></div>
                        <button className="icon-button"><Bell size={18} /></button>
                    </div>
                </header>
                <div className="page-content"><Outlet /></div>
            </main>
        </div>
    );
}

function getTitle(p) {
    const m = { '/': 'Dashboard', '/employees': 'Employees', '/attendance': 'Attendance', '/customers': 'Customers', '/inventory': 'Inventory', '/sales': 'Sales & POS', '/salary': 'Salary & Payroll', '/loyalty': 'Loyalty Program', '/reports': 'Reports', '/config': 'Admin Config', '/audit': 'Audit Logs', '/suppliers': 'Suppliers', '/purchases': 'Purchase Orders', '/expenses': 'Expenses & P&L', '/cash-register': 'Cash Register' };
    return m[p] || 'TRIKAAR';
}
