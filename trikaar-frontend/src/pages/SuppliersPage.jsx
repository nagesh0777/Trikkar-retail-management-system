import { useState } from 'react';
import { Plus, Search, Edit2, Truck, Phone, Mail, MapPin } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import store from '../store';

const emptyForm = { name: '', contactPerson: '', phone: '', email: '', gstin: '', address: '', city: '', state: '', paymentTerms: 30, category: '' };

export default function SuppliersPage() {
    const { user } = useAuth();
    const { addToast } = useToast();
    const [search, setSearch] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [showDetail, setShowDetail] = useState(null);
    const [editId, setEditId] = useState(null);
    const [form, setForm] = useState({ ...emptyForm });
    const [, refresh] = useState(0);

    const suppliers = store.getSuppliers().filter(s => s.active);
    const purchases = store.getPurchases();
    const filtered = suppliers.filter(s => `${s.name} ${s.contactPerson} ${s.gstin}`.toLowerCase().includes(search.toLowerCase()));
    const totalPayable = suppliers.reduce((s, x) => s + (x.balance || 0), 0);

    const s = (k, v) => setForm(f => ({ ...f, [k]: v }));
    const openAdd = () => { setEditId(null); setForm({ ...emptyForm }); setShowForm(true); };
    const openEdit = (sup) => { setEditId(sup.id); setForm({ ...sup }); setShowForm(true); };

    const handleSave = () => {
        if (!form.name || !form.phone) { addToast('Name and phone are required', 'error'); return; }
        if (editId) { store.updateSupplier(editId, form, user.fullName); addToast('Supplier updated', 'success'); }
        else { store.addSupplier(form, user.fullName); addToast('Supplier added', 'success'); }
        setShowForm(false); refresh(n => n + 1);
    };

    return (
        <div className="responsive-page">
            <div className="stats-grid">
                <div className="stat-card purple"><div className="stat-card-header"><div className="stat-icon purple"><Truck size={22} /></div></div><div className="stat-value">{suppliers.length}</div><div className="stat-label">Total Suppliers</div></div>
                <div className="stat-card amber"><div className="stat-card-header"><div className="stat-icon amber"><Truck size={22} /></div></div><div className="stat-value">₹{totalPayable.toLocaleString()}</div><div className="stat-label">Accounts Payable</div></div>
                <div className="stat-card green"><div className="stat-card-header"><div className="stat-icon green"><Truck size={22} /></div></div><div className="stat-value">{purchases.length}</div><div className="stat-label">Purchase Orders</div></div>
            </div>

            <div className="page-actions">
                <p className="page-subtitle">{filtered.length} suppliers • Outstanding: ₹{totalPayable.toLocaleString()}</p>
                <button className="btn btn-primary" onClick={openAdd}><Plus size={16} /> Add Supplier</button>
            </div>

            {/* Mobile Cards */}
            <div className="mobile-cards">
                {filtered.map(sup => (
                    <div key={sup.id} className="card mobile-card" onClick={() => setShowDetail(sup)}>
                        <div className="card-body padded">
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 8 }}>
                                <div><div style={{ fontWeight: 600, color: '#f1f5f9', fontSize: 15 }}>{sup.name}</div><div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{sup.contactPerson}</div></div>
                                <span className="badge approved">{sup.category}</span>
                            </div>
                            <div style={{ display: 'flex', gap: 16, fontSize: 12, color: 'var(--text-secondary)', flexWrap: 'wrap' }}>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}><Phone size={11} /> {sup.phone}</span>
                                <span style={{ display: 'flex', alignItems: 'center', gap: 4 }}><MapPin size={11} /> {sup.city}</span>
                            </div>
                            {sup.balance > 0 && <div style={{ marginTop: 8, fontSize: 13, fontWeight: 700, color: '#f59e0b' }}>Outstanding: ₹{sup.balance.toLocaleString()}</div>}
                        </div>
                    </div>
                ))}
            </div>

            {/* Desktop Table */}
            <div className="card desktop-table">
                <div className="card-header">
                    <h3>Suppliers</h3>
                    <div className="search-bar" style={{ width: 260 }}><Search size={14} style={{ color: 'var(--text-muted)' }} /><input placeholder="Search supplier..." value={search} onChange={e => setSearch(e.target.value)} /></div>
                </div>
                <div className="card-body">
                    <table className="data-table">
                        <thead><tr><th>Supplier</th><th>Contact</th><th>GSTIN</th><th>Category</th><th>Terms</th><th>Balance Due</th><th>Actions</th></tr></thead>
                        <tbody>
                            {filtered.map(sup => (
                                <tr key={sup.id}>
                                    <td><div style={{ color: '#f1f5f9', fontWeight: 500 }}>{sup.name}</div><div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{sup.city}, {sup.state}</div></td>
                                    <td><div>{sup.contactPerson}</div><div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{sup.phone}</div></td>
                                    <td style={{ fontSize: 12 }}>{sup.gstin || '—'}</td>
                                    <td><span className="badge approved">{sup.category}</span></td>
                                    <td>{sup.paymentTerms} days</td>
                                    <td style={{ fontWeight: 700, color: sup.balance > 0 ? '#f59e0b' : '#10b981' }}>{sup.balance > 0 ? `₹${sup.balance.toLocaleString()}` : 'Paid'}</td>
                                    <td><button className="btn btn-sm btn-secondary" onClick={() => openEdit(sup)}><Edit2 size={13} /></button></td>
                                </tr>
                            ))}
                            {filtered.length === 0 && <tr><td colSpan={7} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>No suppliers</td></tr>}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Add/Edit Modal */}
            {showForm && (
                <div className="modal-overlay" onClick={() => setShowForm(false)}>
                    <div className="modal modal-responsive" onClick={e => e.stopPropagation()}>
                        <div className="modal-header"><h3>{editId ? 'Edit' : 'Add'} Supplier</h3><button className="icon-button" onClick={() => setShowForm(false)}>✕</button></div>
                        <div className="modal-body">
                            <div className="form-group"><label>Company Name *</label><input className="form-control" value={form.name} onChange={e => s('name', e.target.value)} /></div>
                            <div className="form-row">
                                <div className="form-group"><label>Contact Person</label><input className="form-control" value={form.contactPerson} onChange={e => s('contactPerson', e.target.value)} /></div>
                                <div className="form-group"><label>Phone *</label><input className="form-control" value={form.phone} onChange={e => s('phone', e.target.value)} /></div>
                            </div>
                            <div className="form-row">
                                <div className="form-group"><label>Email</label><input className="form-control" value={form.email} onChange={e => s('email', e.target.value)} /></div>
                                <div className="form-group"><label>GSTIN</label><input className="form-control" value={form.gstin} onChange={e => s('gstin', e.target.value)} placeholder="e.g. 27AABCM0001A1Z5" /></div>
                            </div>
                            <div className="form-group"><label>Address</label><input className="form-control" value={form.address} onChange={e => s('address', e.target.value)} /></div>
                            <div className="form-row-3">
                                <div className="form-group"><label>City</label><input className="form-control" value={form.city} onChange={e => s('city', e.target.value)} /></div>
                                <div className="form-group"><label>State</label><input className="form-control" value={form.state} onChange={e => s('state', e.target.value)} /></div>
                                <div className="form-group"><label>Category</label><input className="form-control" value={form.category} onChange={e => s('category', e.target.value)} placeholder="e.g. Electronics" /></div>
                            </div>
                            <div className="form-group"><label>Payment Terms (days)</label><input className="form-control" type="number" value={form.paymentTerms} onChange={e => s('paymentTerms', Number(e.target.value))} /></div>
                        </div>
                        <div className="modal-footer"><button className="btn btn-secondary" onClick={() => setShowForm(false)}>Cancel</button><button className="btn btn-primary" onClick={handleSave}>{editId ? 'Update' : 'Add'} Supplier</button></div>
                    </div>
                </div>
            )}

            {/* Detail Modal */}
            {showDetail && (
                <div className="modal-overlay" onClick={() => setShowDetail(null)}>
                    <div className="modal modal-responsive" onClick={e => e.stopPropagation()}>
                        <div className="modal-header"><h3>{showDetail.name}</h3><button className="icon-button" onClick={() => setShowDetail(null)}>✕</button></div>
                        <div className="modal-body">
                            <div className="detail-grid">
                                <div className="detail-item"><span className="detail-label">Contact</span><span className="detail-value">{showDetail.contactPerson}</span></div>
                                <div className="detail-item"><span className="detail-label">Phone</span><span className="detail-value">{showDetail.phone}</span></div>
                                <div className="detail-item"><span className="detail-label">Email</span><span className="detail-value">{showDetail.email || '—'}</span></div>
                                <div className="detail-item"><span className="detail-label">GSTIN</span><span className="detail-value">{showDetail.gstin || '—'}</span></div>
                                <div className="detail-item"><span className="detail-label">Address</span><span className="detail-value">{showDetail.address}, {showDetail.city}</span></div>
                                <div className="detail-item"><span className="detail-label">Terms</span><span className="detail-value">{showDetail.paymentTerms} days</span></div>
                                <div className="detail-item"><span className="detail-label">Balance</span><span className="detail-value" style={{ color: showDetail.balance > 0 ? '#f59e0b' : '#10b981', fontWeight: 700 }}>₹{(showDetail.balance || 0).toLocaleString()}</span></div>
                            </div>
                            <h4 style={{ marginTop: 20, marginBottom: 10, fontSize: 14 }}>Purchase History</h4>
                            {purchases.filter(p => p.supplierId === showDetail.id).map(po => (
                                <div key={po.id} style={{ padding: '10px 12px', background: 'var(--bg-primary)', borderRadius: 8, marginBottom: 6, fontSize: 13 }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between' }}><span style={{ color: '#818cf8', fontWeight: 600 }}>{po.poNumber}</span><span className={`badge ${po.paymentStatus.toLowerCase()}`}>{po.paymentStatus}</span></div>
                                    <div style={{ color: 'var(--text-secondary)', fontSize: 12 }}>₹{po.grandTotal.toLocaleString()} • {po.orderStatus}</div>
                                </div>
                            ))}
                        </div>
                        <div className="modal-footer">
                            <button className="btn btn-secondary" onClick={() => { setShowDetail(null); openEdit(showDetail); }}>Edit</button>
                            <button className="btn btn-primary" onClick={() => setShowDetail(null)}>Close</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
