import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { Download, TrendingUp, ShoppingCart } from 'lucide-react';
import store from '../store';

export default function ReportsPage() {
    const sales = store.getSales().filter(s => s.status === 'COMPLETED');
    const products = store.getProducts();
    const customers = store.getCustomers();

    const totalRevenue = sales.reduce((s, x) => s + x.grandTotal, 0);
    const totalTax = sales.reduce((s, x) => s + x.taxTotal, 0);
    const totalCost = sales.reduce((s, x) => s + x.items.reduce((c, i) => { const p = products.find(pp => pp.id === i.productId); return c + (p?.costPrice || 0) * i.qty; }, 0), 0);
    const profit = totalRevenue - totalCost;

    // Category breakdown
    const catMap = {};
    sales.forEach(s => s.items.forEach(i => { const p = products.find(pp => pp.id === i.productId); const cat = p?.category || 'Other'; catMap[cat] = (catMap[cat] || 0) + i.total; }));
    const catChart = Object.entries(catMap).map(([name, value]) => ({ name, value })).sort((a, b) => b.value - a.value);

    // Payment breakdown
    const payMap = {};
    sales.forEach(s => { payMap[s.paymentMethod] = (payMap[s.paymentMethod] || 0) + s.grandTotal; });
    const payColors = { Cash: '#22d3ee', UPI: '#6366f1', Card: '#10b981', Credit: '#f59e0b' };
    const payChart = Object.entries(payMap).map(([name, value]) => ({ name, value, color: payColors[name] || '#64748b' }));

    // Top products
    const prodMap = {};
    sales.forEach(s => s.items.forEach(i => { if (!prodMap[i.productId]) prodMap[i.productId] = { name: i.name, qty: 0, revenue: 0 }; prodMap[i.productId].qty += i.qty; prodMap[i.productId].revenue += i.total; }));
    const topProds = Object.values(prodMap).sort((a, b) => b.revenue - a.revenue).slice(0, 8);

    // Top customers
    const topCust = [...customers].sort((a, b) => b.totalSpent - a.totalSpent).slice(0, 5);

    return (
        <div>
            <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)' }}>
                <div className="stat-card purple"><div className="stat-card-header"><div className="stat-icon purple"><TrendingUp size={22} /></div></div><div className="stat-value">₹{totalRevenue.toLocaleString()}</div><div className="stat-label">Total Revenue</div></div>
                <div className="stat-card green"><div className="stat-card-header"><div className="stat-icon green"><TrendingUp size={22} /></div></div><div className="stat-value">₹{profit.toLocaleString()}</div><div className="stat-label">Gross Profit</div></div>
                <div className="stat-card cyan"><div className="stat-card-header"><div className="stat-icon cyan"><ShoppingCart size={22} /></div></div><div className="stat-value">{sales.length}</div><div className="stat-label">Transactions</div></div>
                <div className="stat-card amber"><div className="stat-card-header"><div className="stat-icon amber"><TrendingUp size={22} /></div></div><div className="stat-value">₹{totalTax.toLocaleString()}</div><div className="stat-label">Tax Collected</div></div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20, marginBottom: 24 }}>
                {catChart.length > 0 && <div className="chart-card"><h4>Revenue by Category</h4><ResponsiveContainer width="100%" height={250}><BarChart data={catChart}><CartesianGrid strokeDasharray="3 3" stroke="#1e293b" /><XAxis dataKey="name" stroke="#64748b" fontSize={11} /><YAxis stroke="#64748b" fontSize={11} tickFormatter={v => `₹${(v / 1000).toFixed(0)}k`} /><Tooltip contentStyle={{ background: '#1a2235', border: '1px solid #1e293b', borderRadius: 8 }} formatter={v => `₹${v.toLocaleString()}`} /><Bar dataKey="value" fill="#6366f1" radius={[4, 4, 0, 0]} /></BarChart></ResponsiveContainer></div>}
                {payChart.length > 0 && <div className="chart-card"><h4>Payment Methods</h4><div style={{ display: 'flex', alignItems: 'center', gap: 20 }}><ResponsiveContainer width="60%" height={200}><PieChart><Pie data={payChart} cx="50%" cy="50%" innerRadius={45} outerRadius={75} dataKey="value">{payChart.map((e, i) => <Cell key={i} fill={e.color} />)}</Pie><Tooltip contentStyle={{ background: '#1a2235', border: '1px solid #1e293b', borderRadius: 8 }} formatter={v => `₹${v.toLocaleString()}`} /></PieChart></ResponsiveContainer><div>{payChart.map(p => <div key={p.name} style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 8 }}><span style={{ width: 10, height: 10, borderRadius: '50%', background: p.color }} /><span style={{ fontSize: 12, color: 'var(--text-secondary)' }}>{p.name}: ₹{p.value.toLocaleString()}</span></div>)}</div></div></div>}
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
                <div className="card"><div className="card-header"><h3>Top Products</h3></div><div className="card-body"><table className="data-table"><thead><tr><th>#</th><th>Product</th><th>Qty</th><th>Revenue</th></tr></thead><tbody>{topProds.map((p, i) => <tr key={i}><td style={{ fontWeight: 700, color: i < 3 ? '#f59e0b' : 'var(--text-muted)' }}>{i + 1}</td><td style={{ color: '#f1f5f9', fontWeight: 500 }}>{p.name}</td><td>{p.qty}</td><td style={{ fontWeight: 700 }}>₹{p.revenue.toLocaleString()}</td></tr>)}{topProds.length === 0 && <tr><td colSpan={4} style={{ textAlign: 'center', padding: 30, color: 'var(--text-muted)' }}>No sales data</td></tr>}</tbody></table></div></div>
                <div className="card"><div className="card-header"><h3>Top Customers</h3></div><div className="card-body"><table className="data-table"><thead><tr><th>#</th><th>Customer</th><th>Visits</th><th>Total Spent</th></tr></thead><tbody>{topCust.map((c, i) => <tr key={i}><td style={{ fontWeight: 700, color: i < 3 ? '#f59e0b' : 'var(--text-muted)' }}>{i + 1}</td><td style={{ color: '#f1f5f9', fontWeight: 500 }}>{c.firstName} {c.lastName}</td><td>{c.totalVisits}</td><td style={{ fontWeight: 700 }}>₹{c.totalSpent.toLocaleString()}</td></tr>)}</tbody></table></div></div>
            </div>
        </div>
    );
}
