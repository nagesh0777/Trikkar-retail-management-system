import { useState } from 'react';
import { Plus, Trash2, Receipt, TrendingDown, Calendar } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import store from '../store';

const CATEGORIES = ['Rent', 'Electricity', 'Internet', 'Maintenance', 'Tea & Snacks', 'Packaging', 'Transport', 'Cleaning', 'Stationery', 'Miscellaneous'];
const CAT_COLORS = ['#6366f1', '#22d3ee', '#10b981', '#f59e0b', '#ec4899', '#a78bfa', '#f97316', '#14b8a6', '#84cc16', '#64748b'];

const emptyForm = { category: 'Miscellaneous', description: '', amount: '', paymentMethod: 'Cash', date: new Date().toISOString().split('T')[0], recurring: false };

export default function ExpensesPage() {
    const { user } = useAuth();
    const { addToast } = useToast();
    const [showForm, setShowForm] = useState(false);
    const [form, setForm] = useState({ ...emptyForm });
    const [filterMonth, setFilterMonth] = useState(new Date().toISOString().slice(0, 7));
    const [, refresh] = useState(0);

    const expenses = store.getExpenses();
    const monthExpenses = expenses.filter(e => e.date?.startsWith(filterMonth));
    const totalMonth = monthExpenses.reduce((s, e) => s + e.amount, 0);
    const totalAll = expenses.reduce((s, e) => s + e.amount, 0);

    // Category breakdown
    const catMap = {};
    monthExpenses.forEach(e => { catMap[e.category] = (catMap[e.category] || 0) + e.amount; });
    const catChart = Object.entries(catMap).map(([name, value], i) => ({ name, value, color: CAT_COLORS[CATEGORIES.indexOf(name) % CAT_COLORS.length] || '#64748b' })).sort((a, b) => b.value - a.value);

    const s = (k, v) => setForm(f => ({ ...f, [k]: v }));

    const handleSave = () => {
        if (!form.description || !form.amount) { addToast('Description and amount required', 'error'); return; }
        store.addExpense({ ...form, amount: Number(form.amount) }, user.fullName);
        addToast('Expense recorded', 'success');
        setShowForm(false); setForm({ ...emptyForm }); refresh(n => n + 1);
    };

    const handleDelete = (id) => {
        if (confirm('Delete this expense?')) { store.deleteExpense(id, user.fullName); addToast('Expense deleted', 'warning'); refresh(n => n + 1); }
    };

    // P&L data
    const sales = store.getSales().filter(s => s.status === 'COMPLETED');
    const salaries = store.getSalary().filter(s => s.status === 'PAID');
    const totalRevenue = sales.reduce((s, x) => s + x.grandTotal, 0);
    const totalCOGS = sales.reduce((s, x) => s + x.items.reduce((c, i) => { const p = store.getProducts().find(pp => pp.id === i.productId); return c + (p?.costPrice || 0) * i.qty; }, 0), 0);
    const grossProfit = totalRevenue - totalCOGS;
    const totalSalaryPaid = salaries.reduce((s, x) => s + x.net, 0);
    const netProfit = grossProfit - totalAll - totalSalaryPaid;

    return (
        <div className="responsive-page">
            <div className="stats-grid">
                <div className="stat-card amber"><div className="stat-card-header"><div className="stat-icon amber"><TrendingDown size={22} /></div></div><div className="stat-value">₹{totalMonth.toLocaleString()}</div><div className="stat-label">This Month</div></div>
                <div className="stat-card purple"><div className="stat-card-header"><div className="stat-icon purple"><Receipt size={22} /></div></div><div className="stat-value">₹{totalAll.toLocaleString()}</div><div className="stat-label">Total Expenses</div></div>
                <div className={`stat-card ${netProfit >= 0 ? 'green' : 'amber'}`}><div className="stat-card-header"><div className={`stat-icon ${netProfit >= 0 ? 'green' : 'amber'}`}><TrendingDown size={22} /></div></div><div className="stat-value" style={{ color: netProfit >= 0 ? '#10b981' : '#ef4444' }}>₹{netProfit.toLocaleString()}</div><div className="stat-label">Net Profit (P&L)</div></div>
            </div>

            {/* P&L Summary */}
            <div className="card" style={{ marginBottom: 20 }}>
                <div className="card-header"><h3>📊 Profit & Loss Statement</h3></div>
                <div className="card-body padded">
                    <div className="pnl-grid">
                        <div className="pnl-row"><span>Total Revenue (Sales)</span><span style={{ fontWeight: 700, color: '#10b981' }}>₹{totalRevenue.toLocaleString()}</span></div>
                        <div className="pnl-row"><span>(-) Cost of Goods Sold</span><span style={{ color: '#ef4444' }}>₹{totalCOGS.toLocaleString()}</span></div>
                        <div className="pnl-row pnl-subtotal"><span>Gross Profit</span><span style={{ fontWeight: 800, color: grossProfit >= 0 ? '#10b981' : '#ef4444' }}>₹{grossProfit.toLocaleString()}</span></div>
                        <div className="pnl-row"><span>(-) Operating Expenses</span><span style={{ color: '#f59e0b' }}>₹{totalAll.toLocaleString()}</span></div>
                        <div className="pnl-row"><span>(-) Salary & Payroll</span><span style={{ color: '#f59e0b' }}>₹{totalSalaryPaid.toLocaleString()}</span></div>
                        <div className="pnl-row pnl-total"><span>Net Profit / Loss</span><span style={{ fontWeight: 900, fontSize: 20, color: netProfit >= 0 ? '#10b981' : '#ef4444' }}>₹{netProfit.toLocaleString()}</span></div>
                    </div>
                </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: 20, marginBottom: 24 }}>
                {catChart.length > 0 && (
                    <div className="chart-card">
                        <h4>Expenses by Category</h4>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                            <ResponsiveContainer width="55%" height={180}>
                                <PieChart><Pie data={catChart} cx="50%" cy="50%" innerRadius={40} outerRadius={70} dataKey="value">{catChart.map((e, i) => <Cell key={i} fill={e.color} />)}</Pie><Tooltip contentStyle={{ background: '#1a2235', border: '1px solid #1e293b', borderRadius: 8 }} formatter={v => `₹${v.toLocaleString()}`} /></PieChart>
                            </ResponsiveContainer>
                            <div>{catChart.map(c => <div key={c.name} style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 6 }}><span style={{ width: 8, height: 8, borderRadius: '50%', background: c.color, flexShrink: 0 }} /><span style={{ fontSize: 11, color: 'var(--text-secondary)' }}>{c.name}: ₹{c.value.toLocaleString()}</span></div>)}</div>
                        </div>
                    </div>
                )}
            </div>

            <div className="page-actions">
                <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                    <Calendar size={16} style={{ color: 'var(--text-muted)' }} />
                    <input type="month" className="form-control" style={{ width: 170 }} value={filterMonth} onChange={e => setFilterMonth(e.target.value)} />
                </div>
                <button className="btn btn-primary" onClick={() => setShowForm(true)}><Plus size={16} /> Add Expense</button>
            </div>

            {/* Mobile Cards */}
            <div className="mobile-cards">
                {monthExpenses.map(exp => (
                    <div key={exp.id} className="card mobile-card">
                        <div className="card-body padded">
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                                <span className="badge approved">{exp.category}</span>
                                <span style={{ fontWeight: 700, color: '#f1f5f9' }}>₹{exp.amount.toLocaleString()}</span>
                            </div>
                            <div style={{ fontSize: 13, color: '#f1f5f9', marginBottom: 4 }}>{exp.description}</div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>{exp.date} • {exp.paymentMethod}</span>
                                <button className="btn btn-sm btn-danger" onClick={() => handleDelete(exp.id)}><Trash2 size={12} /></button>
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            {/* Desktop Table */}
            <div className="card desktop-table">
                <div className="card-header"><h3>Expenses — {new Date(filterMonth + '-01').toLocaleDateString('en-IN', { month: 'long', year: 'numeric' })} ({monthExpenses.length})</h3></div>
                <div className="card-body">
                    <table className="data-table">
                        <thead><tr><th>Date</th><th>Category</th><th>Description</th><th>Amount</th><th>Payment</th><th>Recurring</th><th></th></tr></thead>
                        <tbody>
                            {monthExpenses.map(exp => (
                                <tr key={exp.id}>
                                    <td style={{ fontSize: 12 }}>{exp.date}</td>
                                    <td><span className="badge approved">{exp.category}</span></td>
                                    <td style={{ color: '#f1f5f9' }}>{exp.description}</td>
                                    <td style={{ fontWeight: 700 }}>₹{exp.amount.toLocaleString()}</td>
                                    <td>{exp.paymentMethod}</td>
                                    <td>{exp.recurring ? '🔁 Yes' : '—'}</td>
                                    <td><button className="btn btn-sm btn-danger" onClick={() => handleDelete(exp.id)}><Trash2 size={13} /></button></td>
                                </tr>
                            ))}
                            {monthExpenses.length > 0 && <tr style={{ fontWeight: 700, background: 'rgba(99,102,241,0.05)' }}><td colSpan={3} style={{ textAlign: 'right' }}>Total</td><td>₹{totalMonth.toLocaleString()}</td><td colSpan={3}></td></tr>}
                            {monthExpenses.length === 0 && <tr><td colSpan={7} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>No expenses for this month</td></tr>}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Add Expense Modal */}
            {showForm && (
                <div className="modal-overlay" onClick={() => setShowForm(false)}>
                    <div className="modal modal-responsive" onClick={e => e.stopPropagation()}>
                        <div className="modal-header"><h3>Add Expense</h3><button className="icon-button" onClick={() => setShowForm(false)}>✕</button></div>
                        <div className="modal-body">
                            <div className="form-row">
                                <div className="form-group"><label>Category</label><select className="form-control" value={form.category} onChange={e => s('category', e.target.value)}>{CATEGORIES.map(c => <option key={c}>{c}</option>)}</select></div>
                                <div className="form-group"><label>Date</label><input className="form-control" type="date" value={form.date} onChange={e => s('date', e.target.value)} /></div>
                            </div>
                            <div className="form-group"><label>Description *</label><input className="form-control" value={form.description} onChange={e => s('description', e.target.value)} placeholder="What was this expense for?" /></div>
                            <div className="form-row">
                                <div className="form-group"><label>Amount * (₹)</label><input className="form-control" type="number" value={form.amount} onChange={e => s('amount', e.target.value)} placeholder="0" /></div>
                                <div className="form-group"><label>Payment Method</label><select className="form-control" value={form.paymentMethod} onChange={e => s('paymentMethod', e.target.value)}><option>Cash</option><option>UPI</option><option>Bank Transfer</option><option>Card</option></select></div>
                            </div>
                            <label style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13, cursor: 'pointer' }}>
                                <input type="checkbox" checked={form.recurring} onChange={e => s('recurring', e.target.checked)} style={{ accentColor: '#6366f1' }} />
                                Recurring monthly expense
                            </label>
                        </div>
                        <div className="modal-footer"><button className="btn btn-secondary" onClick={() => setShowForm(false)}>Cancel</button><button className="btn btn-primary" onClick={handleSave}>Add Expense</button></div>
                    </div>
                </div>
            )}
        </div>
    );
}
