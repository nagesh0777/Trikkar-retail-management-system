import { useState } from 'react';
import { DollarSign, Lock, Unlock, AlertTriangle, Check } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import store from '../store';

export default function CashRegisterPage() {
    const { user } = useAuth();
    const { addToast } = useToast();
    const [showOpen, setShowOpen] = useState(false);
    const [showClose, setShowClose] = useState(null);
    const [openingAmt, setOpeningAmt] = useState('');
    const [closingAmt, setClosingAmt] = useState('');
    const [, refresh] = useState(0);

    const registers = store.getCashRegisters();
    const employees = store.getEmployees().filter(e => e.status === 'ACTIVE');
    const [selectedEmp, setSelectedEmp] = useState('');

    const todayDate = new Date().toISOString().split('T')[0];
    const openRegisters = registers.filter(r => r.status === 'OPEN');
    const todayClosed = registers.filter(r => r.status === 'CLOSED' && r.date === todayDate);
    const totalShort = registers.filter(r => r.difference < 0).reduce((s, r) => s + Math.abs(r.difference), 0);

    const handleOpen = () => {
        const emp = employees.find(e => e.id === selectedEmp);
        if (!emp || !openingAmt) { addToast('Select cashier and enter opening amount', 'error'); return; }
        const existing = openRegisters.find(r => r.cashierId === emp.id);
        if (existing) { addToast(`${emp.firstName} already has an open register`, 'error'); return; }
        store.openRegister({ cashierId: emp.id, cashierName: `${emp.firstName} ${emp.lastName}`, date: todayDate, openingAmount: Number(openingAmt) }, user.fullName);
        addToast(`Register opened for ${emp.firstName} with ₹${Number(openingAmt).toLocaleString()}`, 'success');
        setShowOpen(false); setOpeningAmt(''); setSelectedEmp(''); refresh(n => n + 1);
    };

    const handleClose = () => {
        if (!closingAmt) { addToast('Enter closing cash count', 'error'); return; }
        store.closeRegister(showClose.id, Number(closingAmt), user.fullName);
        addToast('Register closed', 'success');
        setShowClose(null); setClosingAmt(''); refresh(n => n + 1);
    };

    return (
        <div className="responsive-page">
            <div className="stats-grid">
                <div className="stat-card green"><div className="stat-card-header"><div className="stat-icon green"><Unlock size={22} /></div></div><div className="stat-value">{openRegisters.length}</div><div className="stat-label">Open Registers</div></div>
                <div className="stat-card cyan"><div className="stat-card-header"><div className="stat-icon cyan"><Lock size={22} /></div></div><div className="stat-value">{todayClosed.length}</div><div className="stat-label">Closed Today</div></div>
                <div className="stat-card amber"><div className="stat-card-header"><div className="stat-icon amber"><AlertTriangle size={22} /></div></div><div className="stat-value">₹{totalShort.toLocaleString()}</div><div className="stat-label">Total Shortages</div></div>
            </div>

            <div className="page-actions">
                <p className="page-subtitle">Manage cash drawer for each shift</p>
                <button className="btn btn-primary" onClick={() => setShowOpen(true)}><Unlock size={16} /> Open Register</button>
            </div>

            {/* Active Registers */}
            {openRegisters.length > 0 && (
                <div style={{ marginBottom: 24 }}>
                    <h3 style={{ fontSize: 16, marginBottom: 12, color: '#f1f5f9' }}>🟢 Active Registers</h3>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: 12 }}>
                        {openRegisters.map(r => (
                            <div key={r.id} className="card">
                                <div className="card-body padded">
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 12 }}>
                                        <div>
                                            <div style={{ fontWeight: 600, color: '#f1f5f9', fontSize: 15 }}>{r.cashierName}</div>
                                            <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>Opened: {new Date(r.openedAt).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' })}</div>
                                        </div>
                                        <span className="badge active">OPEN</span>
                                    </div>
                                    <div className="detail-grid" style={{ marginBottom: 12 }}>
                                        <div className="detail-item"><span className="detail-label">Opening Cash</span><span className="detail-value" style={{ fontWeight: 700 }}>₹{r.openingAmount.toLocaleString()}</span></div>
                                    </div>
                                    <button className="btn btn-primary" style={{ width: '100%', justifyContent: 'center' }} onClick={() => { setShowClose(r); setClosingAmt(''); }}>
                                        <Lock size={14} /> Close Register
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Closed Register History */}
            <div className="card">
                <div className="card-header"><h3>Register History</h3></div>
                <div className="card-body">

                    {/* Mobile Cards */}
                    <div className="mobile-cards" style={{ display: 'none' }}>
                        {registers.filter(r => r.status === 'CLOSED').map(r => (
                            <div key={r.id} style={{ padding: 14, borderBottom: '1px solid var(--border-color)' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                                    <span style={{ fontWeight: 600, color: '#f1f5f9' }}>{r.cashierName}</span>
                                    <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>{r.date}</span>
                                </div>
                                <div style={{ display: 'flex', gap: 12, fontSize: 12, color: 'var(--text-secondary)', flexWrap: 'wrap' }}>
                                    <span>Open: ₹{r.openingAmount.toLocaleString()}</span>
                                    <span>Sales: ₹{r.salesCash.toLocaleString()}</span>
                                    <span>Close: ₹{r.closingAmount.toLocaleString()}</span>
                                </div>
                                <div style={{ marginTop: 6 }}>
                                    {r.difference === 0 ? <span className="badge active"><Check size={10} /> Balanced</span>
                                        : r.difference > 0 ? <span className="badge pending">Excess ₹{r.difference}</span>
                                            : <span className="badge inactive">Short ₹{Math.abs(r.difference)}</span>}
                                </div>
                            </div>
                        ))}
                    </div>

                    {/* Desktop Table */}
                    <table className="data-table">
                        <thead><tr><th>Date</th><th>Cashier</th><th>Opening</th><th>Cash Sales</th><th>Expected</th><th>Declared</th><th>Difference</th><th>Status</th></tr></thead>
                        <tbody>
                            {registers.filter(r => r.status === 'CLOSED').map(r => (
                                <tr key={r.id}>
                                    <td style={{ fontSize: 12 }}>{r.date}</td>
                                    <td style={{ color: '#f1f5f9', fontWeight: 500 }}>{r.cashierName}</td>
                                    <td>₹{r.openingAmount.toLocaleString()}</td>
                                    <td>₹{r.salesCash.toLocaleString()}</td>
                                    <td style={{ fontWeight: 600 }}>₹{r.expectedAmount.toLocaleString()}</td>
                                    <td style={{ fontWeight: 600 }}>₹{r.closingAmount.toLocaleString()}</td>
                                    <td style={{ fontWeight: 700, color: r.difference === 0 ? '#10b981' : r.difference > 0 ? '#f59e0b' : '#ef4444' }}>
                                        {r.difference === 0 ? '✓ 0' : r.difference > 0 ? `+₹${r.difference}` : `-₹${Math.abs(r.difference)}`}
                                    </td>
                                    <td>
                                        {r.difference === 0 ? <span className="badge active">Balanced</span>
                                            : r.difference > 0 ? <span className="badge pending">Excess</span>
                                                : <span className="badge inactive">Short</span>}
                                    </td>
                                </tr>
                            ))}
                            {registers.filter(r => r.status === 'CLOSED').length === 0 && <tr><td colSpan={8} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>No closed registers</td></tr>}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Open Register Modal */}
            {showOpen && (
                <div className="modal-overlay" onClick={() => setShowOpen(false)}>
                    <div className="modal modal-responsive" onClick={e => e.stopPropagation()} style={{ maxWidth: 420 }}>
                        <div className="modal-header"><h3>Open Cash Register</h3><button className="icon-button" onClick={() => setShowOpen(false)}>✕</button></div>
                        <div className="modal-body">
                            <div className="form-group"><label>Cashier</label><select className="form-control" value={selectedEmp} onChange={e => setSelectedEmp(e.target.value)}><option value="">Select cashier...</option>{employees.map(e => <option key={e.id} value={e.id}>{e.firstName} {e.lastName} — {e.designation}</option>)}</select></div>
                            <div className="form-group"><label>Opening Cash Amount (₹)</label><input className="form-control" type="number" value={openingAmt} onChange={e => setOpeningAmt(e.target.value)} placeholder="Count the cash in the drawer" style={{ textAlign: 'center', fontSize: 20, fontWeight: 700 }} /></div>
                            <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>Count the physical cash in the drawer before starting the shift.</p>
                        </div>
                        <div className="modal-footer"><button className="btn btn-secondary" onClick={() => setShowOpen(false)}>Cancel</button><button className="btn btn-primary" onClick={handleOpen}>Open Register</button></div>
                    </div>
                </div>
            )}

            {/* Close Register Modal */}
            {showClose && (
                <div className="modal-overlay" onClick={() => setShowClose(null)}>
                    <div className="modal modal-responsive" onClick={e => e.stopPropagation()} style={{ maxWidth: 440 }}>
                        <div className="modal-header"><h3>Close Register — {showClose.cashierName}</h3><button className="icon-button" onClick={() => setShowClose(null)}>✕</button></div>
                        <div className="modal-body" style={{ textAlign: 'center' }}>
                            <div style={{ background: 'var(--bg-primary)', borderRadius: 10, padding: 16, marginBottom: 16 }}>
                                <div style={{ fontSize: 13, color: 'var(--text-secondary)' }}>Opening Cash</div>
                                <div style={{ fontSize: 22, fontWeight: 800, color: '#f1f5f9' }}>₹{showClose.openingAmount.toLocaleString()}</div>
                            </div>
                            <div className="form-group">
                                <label>Closing Cash Count (₹)</label>
                                <input className="form-control" type="number" value={closingAmt} onChange={e => setClosingAmt(e.target.value)} placeholder="Count all cash in the drawer now" style={{ textAlign: 'center', fontSize: 22, fontWeight: 700 }} />
                            </div>
                            <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>Count every note and coin in the physical drawer. The system will compare it against opening cash + cash sales to check for discrepancies.</p>
                        </div>
                        <div className="modal-footer"><button className="btn btn-secondary" onClick={() => setShowClose(null)}>Cancel</button><button className="btn btn-primary" onClick={handleClose}><Lock size={14} /> Close & Reconcile</button></div>
                    </div>
                </div>
            )}
        </div>
    );
}
