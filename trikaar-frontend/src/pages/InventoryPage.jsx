import { useState } from 'react';
import { Plus, Search, Edit2, Package as Pkg, AlertTriangle, ArrowUpDown } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import store from '../store';

const emptyForm = { sku: '', barcode: '', name: '', category: '', brand: '', costPrice: '', sellingPrice: '', stock: '', reorderLevel: 10, unit: 'Pcs', taxRate: 18 };

export default function InventoryPage() {
    const { user } = useAuth();
    const { addToast } = useToast();
    const [search, setSearch] = useState('');
    const [tab, setTab] = useState('all');
    const [showForm, setShowForm] = useState(false);
    const [showStock, setShowStock] = useState(null);
    const [stockQty, setStockQty] = useState('');
    const [editId, setEditId] = useState(null);
    const [form, setForm] = useState({ ...emptyForm });
    const [, refresh] = useState(0);

    const products = store.getProducts().filter(p => p.active);
    const lowStock = products.filter(p => p.stock <= p.reorderLevel);
    const filtered = products.filter(p => {
        const match = `${p.name} ${p.sku} ${p.barcode}`.toLowerCase().includes(search.toLowerCase());
        if (tab === 'low') return match && p.stock <= p.reorderLevel;
        return match;
    });
    const totalValue = products.reduce((s, p) => s + p.costPrice * p.stock, 0);

    const s = (k, v) => setForm(f => ({ ...f, [k]: v }));
    const openAdd = () => { setEditId(null); setForm({ ...emptyForm }); setShowForm(true); };
    const openEdit = (p) => { setEditId(p.id); setForm({ ...p }); setShowForm(true); };

    const handleSave = () => {
        if (!form.name || !form.sku) { addToast('Name and SKU required', 'error'); return; }
        const data = { ...form, costPrice: Number(form.costPrice), sellingPrice: Number(form.sellingPrice), stock: Number(form.stock), reorderLevel: Number(form.reorderLevel), taxRate: Number(form.taxRate) };
        if (editId) { store.updateProduct(editId, data, user.fullName); addToast('Product updated', 'success'); }
        else { store.addProduct(data, user.fullName); addToast('Product added', 'success'); }
        setShowForm(false); refresh(n => n + 1);
    };

    const handleStockUpdate = () => {
        const qty = parseInt(stockQty);
        if (!qty) { addToast('Enter a valid quantity', 'error'); return; }
        store.updateStock(showStock.id, qty, user.fullName);
        addToast(`Stock ${qty > 0 ? 'added' : 'removed'}: ${Math.abs(qty)} units`, 'success');
        setShowStock(null); setStockQty(''); refresh(n => n + 1);
    };

    return (
        <div>
            <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
                <div className="stat-card purple"><div className="stat-card-header"><div className="stat-icon purple"><Pkg size={22} /></div></div><div className="stat-value">{products.length}</div><div className="stat-label">Total Products</div></div>
                <div className="stat-card amber"><div className="stat-card-header"><div className="stat-icon amber"><AlertTriangle size={22} /></div></div><div className="stat-value">{lowStock.length}</div><div className="stat-label">Low Stock Alerts</div></div>
                <div className="stat-card green"><div className="stat-card-header"><div className="stat-icon green"><Pkg size={22} /></div></div><div className="stat-value">₹{totalValue.toLocaleString()}</div><div className="stat-label">Stock Valuation</div></div>
            </div>

            <div className="card">
                <div className="card-header">
                    <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                        <button className={`btn btn-sm ${tab === 'all' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTab('all')}>All ({products.length})</button>
                        <button className={`btn btn-sm ${tab === 'low' ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTab('low')}>Low Stock ({lowStock.length})</button>
                    </div>
                    <div style={{ display: 'flex', gap: 12 }}>
                        <div className="search-bar" style={{ width: 240 }}><Search size={14} style={{ color: 'var(--text-muted)' }} /><input placeholder="Search products/SKU/barcode..." value={search} onChange={e => setSearch(e.target.value)} /></div>
                        <button className="btn btn-primary btn-sm" onClick={openAdd}><Plus size={14} /> Add Product</button>
                    </div>
                </div>
                <div className="card-body">
                    <table className="data-table">
                        <thead><tr><th>SKU</th><th>Product</th><th>Category</th><th>Cost</th><th>Selling</th><th>Margin</th><th>Stock</th><th>Status</th><th>Actions</th></tr></thead>
                        <tbody>
                            {filtered.map(p => {
                                const margin = Math.round((p.sellingPrice - p.costPrice) / p.costPrice * 100);
                                return (
                                    <tr key={p.id}>
                                        <td style={{ color: '#818cf8', fontWeight: 600, fontSize: 12 }}>{p.sku}</td>
                                        <td><div style={{ color: '#f1f5f9', fontWeight: 500 }}>{p.name}</div><div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{p.brand} • {p.barcode}</div></td>
                                        <td><span className="badge approved">{p.category}</span></td>
                                        <td>₹{p.costPrice.toLocaleString()}</td>
                                        <td style={{ fontWeight: 600 }}>₹{p.sellingPrice.toLocaleString()}</td>
                                        <td style={{ color: margin >= 20 ? '#10b981' : '#f59e0b', fontWeight: 600 }}>{margin}%</td>
                                        <td><span style={{ fontWeight: 700, color: p.stock <= p.reorderLevel ? '#ef4444' : '#10b981' }}>{p.stock}</span><span style={{ fontSize: 11, color: 'var(--text-muted)' }}> / {p.reorderLevel} min</span></td>
                                        <td>{p.stock <= p.reorderLevel ? <span className="badge inactive">⚠ Low</span> : <span className="badge active">OK</span>}</td>
                                        <td><div style={{ display: 'flex', gap: 4 }}>
                                            <button className="btn btn-sm btn-secondary" onClick={() => openEdit(p)} title="Edit"><Edit2 size={13} /></button>
                                            <button className="btn btn-sm btn-secondary" onClick={() => { setShowStock(p); setStockQty(''); }} title="Update Stock"><ArrowUpDown size={13} /></button>
                                        </div></td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            </div>

            {showForm && (
                <div className="modal-overlay" onClick={() => setShowForm(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 600 }}>
                        <div className="modal-header"><h3>{editId ? 'Edit' : 'Add'} Product</h3><button className="icon-button" onClick={() => setShowForm(false)}>✕</button></div>
                        <div className="modal-body">
                            <div className="form-row"><div className="form-group"><label>SKU *</label><input className="form-control" value={form.sku} onChange={e => s('sku', e.target.value)} /></div><div className="form-group"><label>Barcode</label><input className="form-control" value={form.barcode} onChange={e => s('barcode', e.target.value)} /></div></div>
                            <div className="form-group"><label>Product Name *</label><input className="form-control" value={form.name} onChange={e => s('name', e.target.value)} /></div>
                            <div className="form-row-3"><div className="form-group"><label>Category</label><input className="form-control" value={form.category} onChange={e => s('category', e.target.value)} /></div><div className="form-group"><label>Brand</label><input className="form-control" value={form.brand} onChange={e => s('brand', e.target.value)} /></div><div className="form-group"><label>Unit</label><select className="form-control" value={form.unit} onChange={e => s('unit', e.target.value)}><option>Pcs</option><option>Kg</option><option>Ltr</option><option>Box</option></select></div></div>
                            <div className="form-row-3"><div className="form-group"><label>Cost Price *</label><input className="form-control" type="number" value={form.costPrice} onChange={e => s('costPrice', e.target.value)} /></div><div className="form-group"><label>Selling Price *</label><input className="form-control" type="number" value={form.sellingPrice} onChange={e => s('sellingPrice', e.target.value)} /></div><div className="form-group"><label>Tax Rate %</label><input className="form-control" type="number" value={form.taxRate} onChange={e => s('taxRate', e.target.value)} /></div></div>
                            <div className="form-row"><div className="form-group"><label>Current Stock</label><input className="form-control" type="number" value={form.stock} onChange={e => s('stock', e.target.value)} /></div><div className="form-group"><label>Reorder Level</label><input className="form-control" type="number" value={form.reorderLevel} onChange={e => s('reorderLevel', e.target.value)} /></div></div>
                        </div>
                        <div className="modal-footer"><button className="btn btn-secondary" onClick={() => setShowForm(false)}>Cancel</button><button className="btn btn-primary" onClick={handleSave}>{editId ? 'Update' : 'Add'} Product</button></div>
                    </div>
                </div>
            )}

            {showStock && (
                <div className="modal-overlay" onClick={() => setShowStock(null)}>
                    <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 400 }}>
                        <div className="modal-header"><h3>Update Stock — {showStock.name}</h3><button className="icon-button" onClick={() => setShowStock(null)}>✕</button></div>
                        <div className="modal-body" style={{ textAlign: 'center' }}>
                            <div style={{ fontSize: 14, color: 'var(--text-secondary)', marginBottom: 8 }}>Current Stock: <strong style={{ color: '#f1f5f9', fontSize: 20 }}>{showStock.stock}</strong></div>
                            <div className="form-group"><label>Quantity to Add/Remove (use negative to remove)</label><input className="form-control" type="number" value={stockQty} onChange={e => setStockQty(e.target.value)} placeholder="e.g. 50 or -5" style={{ textAlign: 'center', fontSize: 18 }} /></div>
                            {stockQty && <div style={{ fontSize: 13, color: 'var(--text-secondary)' }}>New Stock: <strong style={{ color: Number(stockQty) > 0 ? '#10b981' : '#ef4444' }}>{showStock.stock + Number(stockQty)}</strong></div>}
                        </div>
                        <div className="modal-footer"><button className="btn btn-secondary" onClick={() => setShowStock(null)}>Cancel</button><button className="btn btn-primary" onClick={handleStockUpdate}>Update Stock</button></div>
                    </div>
                </div>
            )}
        </div>
    );
}
