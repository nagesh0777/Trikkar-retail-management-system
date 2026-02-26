import { useState } from 'react';
import { DollarSign, Check, Clock, Calculator } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import store from '../store';

export default function SalaryPage() {
    const { user } = useAuth();
    const { addToast } = useToast();
    const [showGen, setShowGen] = useState(false);
    const [period, setPeriod] = useState('Feb 2026');
    const [selectedEmp, setSelectedEmp] = useState('');
    const [bonus, setBonus] = useState(0);
    const [deductions, setDeductions] = useState(0);
    const [, refresh] = useState(0);

    const employees = store.getEmployees().filter(e => e.status === 'ACTIVE');
    const salaries = store.getSalary();
    const totalPaid = salaries.filter(s => s.status === 'PAID').reduce((s, p) => s + p.net, 0);
    const totalPending = salaries.filter(s => s.status !== 'PAID').reduce((s, p) => s + p.net, 0);

    const generate = () => {
        const emp = employees.find(e => e.id === selectedEmp);
        if (!emp) { addToast('Select an employee', 'error'); return; }
        const existing = salaries.find(s => s.employeeId === emp.id && s.period === period);
        if (existing) { addToast('Salary already generated for this period', 'error'); return; }

        const attendance = store.getAttendance().filter(a => a.employeeId === emp.id && a.status === 'PRESENT');
        const base = emp.wageType === 'MONTHLY' ? Number(emp.baseSalary) : emp.wageType === 'DAILY' ? attendance.length * Number(emp.dailyRate) : attendance.reduce((s, a) => s + 9, 0) * Number(emp.hourlyRate);
        const overtime = attendance.reduce((s, a) => s + (a.overtimeHours || 0), 0) * (Number(emp.hourlyRate) || Number(emp.baseSalary) / 200);
        const sales = store.getSales().filter(s => s.employeeId === emp.id && s.status === 'COMPLETED');
        const incentive = Math.round(sales.reduce((s, x) => s + x.grandTotal, 0) * (Number(emp.incentivePercent) || 0) / 100);
        const net = Math.round(base + overtime + incentive + Number(bonus) - Number(deductions));

        store.generateSalary({
            employeeId: emp.id, employeeName: `${emp.firstName} ${emp.lastName}`, period,
            base: Math.round(base), overtime: Math.round(overtime), incentive, bonus: Number(bonus), deductions: Number(deductions), net,
        }, user.fullName);
        addToast(`Salary generated for ${emp.firstName}: ₹${net.toLocaleString()}`, 'success');
        setShowGen(false); setBonus(0); setDeductions(0); refresh(n => n + 1);
    };

    const approve = (id) => { store.approveSalary(id, user.fullName); addToast('Salary approved', 'success'); refresh(n => n + 1); };
    const pay = (id) => { store.paySalary(id, user.fullName); addToast('Salary marked as paid', 'success'); refresh(n => n + 1); };

    return (
        <div>
            <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
                <div className="stat-card green"><div className="stat-card-header"><div className="stat-icon green"><Check size={22} /></div></div><div className="stat-value">₹{totalPaid.toLocaleString()}</div><div className="stat-label">Total Paid</div></div>
                <div className="stat-card amber"><div className="stat-card-header"><div className="stat-icon amber"><Clock size={22} /></div></div><div className="stat-value">₹{totalPending.toLocaleString()}</div><div className="stat-label">Pending</div></div>
                <div className="stat-card purple"><div className="stat-card-header"><div className="stat-icon purple"><DollarSign size={22} /></div></div><div className="stat-value">{salaries.length}</div><div className="stat-label">Total Payouts</div></div>
            </div>

            <div style={{ marginBottom: 20 }}><button className="btn btn-primary" onClick={() => setShowGen(true)}><Calculator size={16} /> Generate Salary</button></div>

            <div className="card">
                <div className="card-header"><h3>Salary Payouts</h3></div>
                <div className="card-body">
                    <table className="data-table">
                        <thead><tr><th>Employee</th><th>Period</th><th>Base</th><th>OT</th><th>Incentive</th><th>Bonus</th><th>Deductions</th><th>Net</th><th>Status</th><th>Actions</th></tr></thead>
                        <tbody>
                            {salaries.map(p => (
                                <tr key={p.id}>
                                    <td style={{ color: '#f1f5f9', fontWeight: 500 }}>{p.employeeName}</td>
                                    <td>{p.period}</td>
                                    <td>₹{p.base.toLocaleString()}</td>
                                    <td>{p.overtime > 0 ? `₹${p.overtime.toLocaleString()}` : '—'}</td>
                                    <td>{p.incentive > 0 ? `₹${p.incentive.toLocaleString()}` : '—'}</td>
                                    <td>{p.bonus > 0 ? `₹${p.bonus.toLocaleString()}` : '—'}</td>
                                    <td style={{ color: p.deductions > 0 ? '#ef4444' : 'inherit' }}>{p.deductions > 0 ? `-₹${p.deductions.toLocaleString()}` : '—'}</td>
                                    <td style={{ fontWeight: 800, color: '#f1f5f9' }}>₹{p.net.toLocaleString()}</td>
                                    <td><span className={`badge ${p.status.toLowerCase()}`}>{p.status}</span></td>
                                    <td>
                                        {p.status === 'PENDING' && <button className="btn btn-sm btn-secondary" onClick={() => approve(p.id)}>Approve</button>}
                                        {p.status === 'APPROVED' && <button className="btn btn-sm btn-primary" onClick={() => pay(p.id)}>Pay</button>}
                                        {p.status === 'PAID' && <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>✓ {p.paidOn}</span>}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {showGen && (
                <div className="modal-overlay" onClick={() => setShowGen(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 480 }}>
                        <div className="modal-header"><h3>Generate Salary</h3><button className="icon-button" onClick={() => setShowGen(false)}>✕</button></div>
                        <div className="modal-body">
                            <div className="form-group"><label>Employee *</label><select className="form-control" value={selectedEmp} onChange={e => setSelectedEmp(e.target.value)}><option value="">Select employee...</option>{employees.map(e => <option key={e.id} value={e.id}>{e.code} — {e.firstName} {e.lastName} ({e.wageType})</option>)}</select></div>
                            <div className="form-group"><label>Period</label><input className="form-control" value={period} onChange={e => setPeriod(e.target.value)} placeholder="e.g. Feb 2026" /></div>
                            <div className="form-row">
                                <div className="form-group"><label>Bonus</label><input className="form-control" type="number" value={bonus} onChange={e => setBonus(e.target.value)} /></div>
                                <div className="form-group"><label>Deductions</label><input className="form-control" type="number" value={deductions} onChange={e => setDeductions(e.target.value)} /></div>
                            </div>
                            <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>Base salary, overtime, and sales incentive are auto-calculated from attendance and sales data.</p>
                        </div>
                        <div className="modal-footer"><button className="btn btn-secondary" onClick={() => setShowGen(false)}>Cancel</button><button className="btn btn-primary" onClick={generate}>Generate</button></div>
                    </div>
                </div>
            )}
        </div>
    );
}
