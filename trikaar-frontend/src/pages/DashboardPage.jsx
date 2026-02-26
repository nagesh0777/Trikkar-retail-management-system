import { useState, useEffect } from 'react';
import { DollarSign, Users, Package, ShoppingCart, AlertTriangle, TrendingUp } from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar } from 'recharts';
import store from '../store';

export default function DashboardPage() {
    const [, setTick] = useState(0);
    useEffect(() => { setTick(t => t + 1); }, []);

    const sales = store.getSales();
    const products = store.getProducts();
    const customers = store.getCustomers();
    const employees = store.getEmployees().filter(e => e.status === 'ACTIVE');
    const lowStock = products.filter(p => p.stock <= p.reorderLevel);

    const totalRevenue = sales.filter(s => s.status === 'COMPLETED').reduce((s, x) => s + x.grandTotal, 0);
    const todaySales = sales.filter(s => s.status === 'COMPLETED' && s.createdAt?.startsWith(new Date().toISOString().split('T')[0])).reduce((s, x) => s + x.grandTotal, 0);

    const catData = {};
    sales.filter(s => s.status === 'COMPLETED').forEach(s => s.items.forEach(i => {
        const prod = products.find(p => p.id === i.productId);
        const cat = prod?.category || 'Other';
        catData[cat] = (catData[cat] || 0) + i.total;
    }));
    const chartCat = Object.entries(catData).map(([name, value]) => ({ name, value })).sort((a, b) => b.value - a.value);

    const stats = [
        { label: 'Total Revenue', value: `₹${totalRevenue.toLocaleString()}`, icon: DollarSign, color: 'purple', change: '+12.5%', pos: true },
        { label: "Today's Sales", value: `₹${todaySales.toLocaleString()}`, icon: ShoppingCart, color: 'cyan', change: `${sales.filter(s => s.createdAt?.startsWith(new Date().toISOString().split('T')[0])).length} txns`, pos: true },
        { label: 'Active Employees', value: employees.length, icon: Users, color: 'green', change: `${customers.filter(c => c.active).length} customers`, pos: true },
        { label: 'Low Stock Items', value: lowStock.length, icon: AlertTriangle, color: 'amber', change: `${products.length} products`, pos: lowStock.length === 0 },
    ];

    return (
        <div>
            <div className="stats-grid">
                {stats.map((c, i) => (
                    <div key={i} className={`stat-card ${c.color}`}>
                        <div className="stat-card-header">
                            <div className={`stat-icon ${c.color}`}><c.icon size={22} /></div>
                            <span className={`stat-change ${c.pos ? 'positive' : 'negative'}`}>{c.change}</span>
                        </div>
                        <div className="stat-value">{c.value}</div>
                        <div className="stat-label">{c.label}</div>
                    </div>
                ))}
            </div>

            {chartCat.length > 0 && (
                <div className="chart-grid">
                    <div className="chart-card" style={{ gridColumn: 'span 2' }}>
                        <h4>Revenue by Category</h4>
                        <ResponsiveContainer width="100%" height={260}>
                            <BarChart data={chartCat}>
                                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                                <XAxis dataKey="name" stroke="#64748b" fontSize={12} />
                                <YAxis stroke="#64748b" fontSize={12} tickFormatter={v => `₹${(v / 1000).toFixed(0)}k`} />
                                <Tooltip contentStyle={{ background: '#1a2235', border: '1px solid #1e293b', borderRadius: 8 }} formatter={v => `₹${v.toLocaleString()}`} />
                                <Bar dataKey="value" fill="#6366f1" radius={[6, 6, 0, 0]} barSize={46} />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            )}

            <div className="card">
                <div className="card-header"><h3>Recent Sales ({sales.length})</h3></div>
                <div className="card-body">
                    <table className="data-table">
                        <thead><tr><th>TXN #</th><th>Customer</th><th>Cashier</th><th>Items</th><th>Total</th><th>Payment</th><th>Status</th></tr></thead>
                        <tbody>
                            {sales.slice(0, 10).map(s => (
                                <tr key={s.id}>
                                    <td style={{ color: '#818cf8', fontWeight: 600 }}>{s.txnNo}</td>
                                    <td style={{ color: '#f1f5f9' }}>{s.customerName || 'Walk-in'}</td>
                                    <td>{s.employeeName}</td>
                                    <td>{s.items.length}</td>
                                    <td style={{ fontWeight: 700 }}>₹{s.grandTotal.toLocaleString()}</td>
                                    <td><span className="badge approved">{s.paymentMethod}</span></td>
                                    <td><span className={`badge ${s.status === 'COMPLETED' ? 'completed' : 'inactive'}`}>{s.status}</span></td>
                                </tr>
                            ))}
                            {sales.length === 0 && <tr><td colSpan={7} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>No sales yet. Create your first sale from Sales & POS.</td></tr>}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}
