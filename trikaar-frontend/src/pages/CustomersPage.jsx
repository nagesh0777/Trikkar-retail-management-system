import { useState } from 'react';
import { Plus, Search, Edit2, Star, Trash2 } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import store from '../store';

const tierColors = { PLATINUM: '#a78bfa', GOLD: '#f59e0b', SILVER: '#94a3b8', BRONZE: '#d97706' };
const emptyForm = { firstName: '', lastName: '', phone: '', email: '', city: '', state: '', pincode: '', gender: 'Male', notes: '' };

export default function CustomersPage() {
    const { user } = useAuth();
    const { addToast } = useToast();
    const [search, setSearch] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [editId, setEditId] = useState(null);
    const [form, setForm] = useState({ ...emptyForm });
    const [, refresh] = useState(0);

    const customers = store.getCustomers().filter(c => c.active);
    const filtered = customers.filter(c => `${c.firstName} ${c.lastName} ${c.phone}`.toLowerCase().includes(search.toLowerCase()));
    const s = (k, v) => setForm(f => ({ ...f, [k]: v }));

    const openAdd = () => { setEditId(null); setForm({ ...emptyForm }); setShowForm(true); };
    const openEdit = (c) => { setEditId(c.id); setForm({ ...c }); setShowForm(true); };

    const handleSave = () => {
        if (!form.firstName || !form.phone) { addToast('Name and phone required', 'error'); return; }
        if (editId) { store.updateCustomer(editId, form, user.fullName); addToast('Customer updated', 'success'); }
        else { store.addCustomer(form, user.fullName); addToast('Customer added', 'success'); }
        setShowForm(false); refresh(n => n + 1);
    };

    const handleDelete = (c) => { if (confirm(`Deactivate ${c.firstName}?`)) { store.deleteCustomer(c.id, user.fullName); addToast('Customer deactivated', 'warning'); refresh(n => n + 1); } };

    return (
        <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
                <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>{filtered.length} active customers • Total spent: ₹{customers.reduce((s, c) => s + c.totalSpent, 0).toLocaleString()}</p>
                <button className="btn btn-primary" onClick={openAdd}><Plus size={16} /> Add Customer</button>
            </div>

            <div className="card">
                <div className="card-header">
                    <h3>Customers</h3>
                    <div className="search-bar" style={{ width: 260 }}><Search size={14} style={{ color: 'var(--text-muted)' }} /><input placeholder="Search name or phone..." value={search} onChange={e => setSearch(e.target.value)} /></div>
                </div>
                <div className="card-body">
                    <table className="data-table">
                        <thead><tr><th>Customer</th><th>Phone</th><th>City</th><th>Tier</th><th>Points</th><th>Total Spent</th><th>Visits</th><th>Actions</th></tr></thead>
                        <tbody>
                            {filtered.map(c => (
                                <tr key={c.id}>
                                    <td><div style={{ color: '#f1f5f9', fontWeight: 500 }}>{c.firstName} {c.lastName}</div><div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{c.email || '—'}</div></td>
                                    <td>{c.phone}</td>
                                    <td>{c.city}</td>
                                    <td><span style={{ display: 'inline-flex', alignItems: 'center', gap: 4, padding: '4px 10px', borderRadius: 20, fontSize: 11, fontWeight: 600, background: `${tierColors[c.loyaltyTier]}18`, color: tierColors[c.loyaltyTier] }}><Star size={11} /> {c.loyaltyTier}</span></td>
                                    <td style={{ fontWeight: 600 }}>{c.loyaltyPoints.toLocaleString()}</td>
                                    <td style={{ fontWeight: 600 }}>₹{c.totalSpent.toLocaleString()}</td>
                                    <td>{c.totalVisits}</td>
                                    <td><div style={{ display: 'flex', gap: 4 }}><button className="btn btn-sm btn-secondary" onClick={() => openEdit(c)}><Edit2 size={13} /></button><button className="btn btn-sm btn-danger" onClick={() => handleDelete(c)}><Trash2 size={13} /></button></div></td>
                                </tr>
                            ))}
                            {filtered.length === 0 && <tr><td colSpan={8} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>No customers found</td></tr>}
                        </tbody>
                    </table>
                </div>
            </div>

            {showForm && (
                <div className="modal-overlay" onClick={() => setShowForm(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()}>
                        <div className="modal-header"><h3>{editId ? 'Edit' : 'Add'} Customer</h3><button className="icon-button" onClick={() => setShowForm(false)}>✕</button></div>
                        <div className="modal-body">
                            <div className="form-row">
                                <div className="form-group"><label>First Name *</label><input className="form-control" value={form.firstName} onChange={e => s('firstName', e.target.value)} /></div>
                                <div className="form-group"><label>Last Name</label><input className="form-control" value={form.lastName} onChange={e => s('lastName', e.target.value)} /></div>
                            </div>
                            <div className="form-row">
                                <div className="form-group"><label>Phone *</label><input className="form-control" value={form.phone} onChange={e => s('phone', e.target.value)} /></div>
                                <div className="form-group"><label>Email</label><input className="form-control" value={form.email} onChange={e => s('email', e.target.value)} /></div>
                            </div>
                            <div className="form-row-3">
                                <div className="form-group"><label>City</label><input className="form-control" value={form.city} onChange={e => s('city', e.target.value)} /></div>
                                <div className="form-group"><label>State</label><input className="form-control" value={form.state} onChange={e => s('state', e.target.value)} /></div>
                                <div className="form-group"><label>Pincode</label><input className="form-control" value={form.pincode} onChange={e => s('pincode', e.target.value)} /></div>
                            </div>
                            <div className="form-group"><label>Gender</label><select className="form-control" value={form.gender} onChange={e => s('gender', e.target.value)}><option>Male</option><option>Female</option><option>Other</option></select></div>
                        </div>
                        <div className="modal-footer"><button className="btn btn-secondary" onClick={() => setShowForm(false)}>Cancel</button><button className="btn btn-primary" onClick={handleSave}>{editId ? 'Update' : 'Add'} Customer</button></div>
                    </div>
                </div>
            )}
        </div>
    );
}
