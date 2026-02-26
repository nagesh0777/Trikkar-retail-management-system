import { useState } from 'react';
import { Plus, Package, Check, CreditCard, Search, Truck } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import store from '../store';

export default function PurchasesPage() {
    const { user } = useAuth();
    const { addToast } = useToast();
    const [showCreate, setShowCreate] = useState(false);
    const [showPay, setShowPay] = useState(null);
    const [payAmount, setPayAmount] = useState('');
    const [tab, setTab] = useState('all');
    const [, refresh] = useState(0);

    // PO form state
    const [selectedSupplier, setSelectedSupplier] = useState('');
    const [invoiceNo, setInvoiceNo] = useState('');
    const [dueDate, setDueDate] = useState('');
    const [poItems, setPoItems] = useState([]);
    const [addProdId, setAddProdId] = useState('');
    const [addQty, setAddQty] = useState('');
    const [addCost, setAddCost] = useState('');

    const purchases = store.getPurchases();
    const suppliers = store.getSuppliers().filter(s => s.active);
    const products = store.getProducts();
    const filtered = purchases.filter(p => {
        if (tab === 'ordered') return p.orderStatus === 'ORDERED';
        if (tab === 'received') return p.orderStatus === 'RECEIVED';
        if (tab === 'unpaid') return p.paymentStatus !== 'PAID';
        return true;
    });

    const totalPayable = purchases.filter(p => p.paymentStatus !== 'PAID').reduce((s, p) => s + p.grandTotal - (p.paidAmount || 0), 0);

    const addItemToPO = () => {
        const prod = products.find(p => p.id === addProdId);
        if (!prod || !addQty || !addCost) { addToast('Select product, qty and cost', 'error'); return; }
        const qty = Number(addQty);
        const unitCost = Number(addCost);
        const tax = Math.round(unitCost * qty * prod.taxRate / 100);
        const total = unitCost * qty + tax;
        setPoItems(prev => [...prev, { productId: prod.id, name: prod.name, qty, unitCost, tax, total }]);
        setAddProdId(''); setAddQty(''); setAddCost('');
    };

    const removePOItem = (idx) => setPoItems(prev => prev.filter((_, i) => i !== idx));

    const createPO = () => {
        if (!selectedSupplier || poItems.length === 0) { addToast('Select supplier and add items', 'error'); return; }
        const sup = suppliers.find(s => s.id === selectedSupplier);
        const subtotal = poItems.reduce((s, i) => s + i.unitCost * i.qty, 0);
        const taxTotal = poItems.reduce((s, i) => s + i.tax, 0);
        const grandTotal = subtotal + taxTotal;
        const po = { supplierId: sup.id, supplierName: sup.name, items: poItems, subtotal, taxTotal, grandTotal, invoiceNo, dueDate };
        store.createPurchase(po, user.fullName);
        addToast(`Purchase Order created — ₹${grandTotal.toLocaleString()}`, 'success');
        setShowCreate(false); setPoItems([]); setSelectedSupplier(''); setInvoiceNo(''); setDueDate('');
        refresh(n => n + 1);
    };

    const receiveOrder = (id) => {
        store.receivePurchase(id, user.fullName);
        addToast('Goods received — stock updated & cost price recalculated', 'success');
        refresh(n => n + 1);
    };

    const makePayment = () => {
        const amt = Number(payAmount);
        if (!amt || amt <= 0) { addToast('Enter valid amount', 'error'); return; }
        store.payPurchase(showPay.id, amt, user.fullName);
        addToast(`Payment of ₹${amt.toLocaleString()} recorded`, 'success');
        setShowPay(null); setPayAmount(''); refresh(n => n + 1);
    };

    return (
        <div className="responsive-page">
            <div className="stats-grid">
                <div className="stat-card purple"><div className="stat-card-header"><div className="stat-icon purple"><Package size={22} /></div></div><div className="stat-value">{purchases.length}</div><div className="stat-label">Total POs</div></div>
                <div className="stat-card cyan"><div className="stat-card-header"><div className="stat-icon cyan"><Truck size={22} /></div></div><div className="stat-value">{purchases.filter(p => p.orderStatus === 'ORDERED').length}</div><div className="stat-label">Awaiting Delivery</div></div>
                <div className="stat-card amber"><div className="stat-card-header"><div className="stat-icon amber"><CreditCard size={22} /></div></div><div className="stat-value">₹{totalPayable.toLocaleString()}</div><div className="stat-label">Outstanding</div></div>
            </div>

            <div className="page-actions">
                <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                    {[['all', 'All'], ['ordered', 'Pending Delivery'], ['unpaid', 'Unpaid'], ['received', 'Received']].map(([val, label]) => (
                        <button key={val} className={`btn btn-sm ${tab === val ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setTab(val)}>{label}</button>
                    ))}
                </div>
                <button className="btn btn-primary" onClick={() => setShowCreate(true)}><Plus size={16} /> New Purchase Order</button>
            </div>

            {/* Mobile Cards */}
            <div className="mobile-cards">
                {filtered.map(po => (
                    <div key={po.id} className="card mobile-card">
                        <div className="card-body padded">
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
                                <span style={{ color: '#818cf8', fontWeight: 700 }}>{po.poNumber}</span>
                                <span className={`badge ${po.orderStatus === 'RECEIVED' ? 'completed' : po.orderStatus === 'ORDERED' ? 'pending' : 'inactive'}`}>{po.orderStatus}</span>
                            </div>
                            <div style={{ fontWeight: 500, color: '#f1f5f9', marginBottom: 4 }}>{po.supplierName}</div>
                            <div style={{ fontSize: 12, color: 'var(--text-muted)', marginBottom: 6 }}>{po.items.length} items • {po.invoiceNo || 'No invoice'}</div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <span style={{ fontWeight: 700 }}>₹{po.grandTotal.toLocaleString()}</span>
                                <span className={`badge ${po.paymentStatus === 'PAID' ? 'active' : 'inactive'}`}>{po.paymentStatus}</span>
                            </div>
                            <div style={{ display: 'flex', gap: 6, marginTop: 10 }}>
                                {po.orderStatus === 'ORDERED' && <button className="btn btn-sm btn-primary" onClick={() => receiveOrder(po.id)}><Check size={12} /> Receive</button>}
                                {po.paymentStatus !== 'PAID' && <button className="btn btn-sm btn-secondary" onClick={() => { setShowPay(po); setPayAmount(''); }}><CreditCard size={12} /> Pay</button>}
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            {/* Desktop Table */}
            <div className="card desktop-table">
                <div className="card-header"><h3>Purchase Orders</h3></div>
                <div className="card-body">
                    <table className="data-table">
                        <thead><tr><th>PO #</th><th>Supplier</th><th>Items</th><th>Total</th><th>Invoice</th><th>Due Date</th><th>Delivery</th><th>Payment</th><th>Actions</th></tr></thead>
                        <tbody>
                            {filtered.map(po => (
                                <tr key={po.id}>
                                    <td style={{ color: '#818cf8', fontWeight: 600 }}>{po.poNumber}</td>
                                    <td style={{ color: '#f1f5f9', fontWeight: 500 }}>{po.supplierName}</td>
                                    <td>{po.items.length}</td>
                                    <td style={{ fontWeight: 700 }}>₹{po.grandTotal.toLocaleString()}</td>
                                    <td style={{ fontSize: 12 }}>{po.invoiceNo || '—'}</td>
                                    <td style={{ fontSize: 12 }}>{po.dueDate || '—'}</td>
                                    <td><span className={`badge ${po.orderStatus === 'RECEIVED' ? 'completed' : 'pending'}`}>{po.orderStatus}</span></td>
                                    <td><span className={`badge ${po.paymentStatus === 'PAID' ? 'active' : po.paymentStatus === 'PARTIAL' ? 'pending' : 'inactive'}`}>{po.paymentStatus}{po.paymentStatus === 'PARTIAL' && ` (₹${(po.paidAmount || 0).toLocaleString()})`}</span></td>
                                    <td>
                                        <div style={{ display: 'flex', gap: 4 }}>
                                            {po.orderStatus === 'ORDERED' && <button className="btn btn-sm btn-primary" onClick={() => receiveOrder(po.id)} title="Mark Received"><Check size={13} /></button>}
                                            {po.paymentStatus !== 'PAID' && <button className="btn btn-sm btn-secondary" onClick={() => { setShowPay(po); setPayAmount(''); }} title="Record Payment"><CreditCard size={13} /></button>}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Create PO Modal */}
            {showCreate && (
                <div className="modal-overlay" onClick={() => setShowCreate(false)}>
                    <div className="modal modal-responsive modal-lg" onClick={e => e.stopPropagation()}>
                        <div className="modal-header"><h3>New Purchase Order</h3><button className="icon-button" onClick={() => setShowCreate(false)}>✕</button></div>
                        <div className="modal-body">
                            <div className="form-row">
                                <div className="form-group"><label>Supplier *</label><select className="form-control" value={selectedSupplier} onChange={e => setSelectedSupplier(e.target.value)}><option value="">Select supplier...</option>{suppliers.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}</select></div>
                                <div className="form-group"><label>Supplier Invoice #</label><input className="form-control" value={invoiceNo} onChange={e => setInvoiceNo(e.target.value)} /></div>
                            </div>
                            <div className="form-group"><label>Due Date</label><input className="form-control" type="date" value={dueDate} onChange={e => setDueDate(e.target.value)} /></div>

                            <div style={{ background: 'var(--bg-primary)', borderRadius: 10, padding: 14, marginTop: 10, marginBottom: 10, border: '1px solid var(--border-color)' }}>
                                <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 8, display: 'block' }}>Add Items</label>
                                <div className="form-row" style={{ alignItems: 'flex-end' }}>
                                    <div className="form-group" style={{ flex: 2 }}><label>Product</label><select className="form-control" value={addProdId} onChange={e => { setAddProdId(e.target.value); const p = products.find(x => x.id === e.target.value); if (p) setAddCost(p.costPrice); }}><option value="">Choose...</option>{products.map(p => <option key={p.id} value={p.id}>{p.name} ({p.sku})</option>)}</select></div>
                                    <div className="form-group" style={{ flex: 1 }}><label>Qty</label><input className="form-control" type="number" value={addQty} onChange={e => setAddQty(e.target.value)} /></div>
                                    <div className="form-group" style={{ flex: 1 }}><label>Unit Cost</label><input className="form-control" type="number" value={addCost} onChange={e => setAddCost(e.target.value)} /></div>
                                    <button className="btn btn-primary btn-sm" onClick={addItemToPO} style={{ marginBottom: 16, height: 38 }}>Add</button>
                                </div>
                            </div>

                            {poItems.length > 0 && (
                                <table className="data-table" style={{ fontSize: 13 }}>
                                    <thead><tr><th>Product</th><th>Qty</th><th>Unit Cost</th><th>Tax</th><th>Total</th><th></th></tr></thead>
                                    <tbody>
                                        {poItems.map((item, i) => (
                                            <tr key={i}>
                                                <td>{item.name}</td>
                                                <td>{item.qty}</td>
                                                <td>₹{item.unitCost}</td>
                                                <td>₹{item.tax.toLocaleString()}</td>
                                                <td style={{ fontWeight: 700 }}>₹{item.total.toLocaleString()}</td>
                                                <td><button className="btn btn-sm btn-danger" onClick={() => removePOItem(i)}>✕</button></td>
                                            </tr>
                                        ))}
                                        <tr style={{ fontWeight: 700 }}>
                                            <td colSpan={4} style={{ textAlign: 'right' }}>Grand Total</td>
                                            <td>₹{poItems.reduce((s, i) => s + i.total, 0).toLocaleString()}</td>
                                            <td></td>
                                        </tr>
                                    </tbody>
                                </table>
                            )}
                        </div>
                        <div className="modal-footer"><button className="btn btn-secondary" onClick={() => setShowCreate(false)}>Cancel</button><button className="btn btn-primary" onClick={createPO}>Create Purchase Order</button></div>
                    </div>
                </div>
            )}

            {/* Payment Modal */}
            {showPay && (
                <div className="modal-overlay" onClick={() => setShowPay(null)}>
                    <div className="modal modal-responsive" onClick={e => e.stopPropagation()} style={{ maxWidth: 420 }}>
                        <div className="modal-header"><h3>Record Payment — {showPay.poNumber}</h3><button className="icon-button" onClick={() => setShowPay(null)}>✕</button></div>
                        <div className="modal-body" style={{ textAlign: 'center' }}>
                            <div style={{ fontSize: 14, color: 'var(--text-secondary)', marginBottom: 8 }}>
                                Total: <strong>₹{showPay.grandTotal.toLocaleString()}</strong> •
                                Paid: <strong style={{ color: '#10b981' }}>₹{(showPay.paidAmount || 0).toLocaleString()}</strong> •
                                Due: <strong style={{ color: '#ef4444' }}>₹{(showPay.grandTotal - (showPay.paidAmount || 0)).toLocaleString()}</strong>
                            </div>
                            <div className="form-group"><label>Payment Amount</label><input className="form-control" type="number" value={payAmount} onChange={e => setPayAmount(e.target.value)} placeholder="Enter amount..." style={{ textAlign: 'center', fontSize: 18 }} /></div>
                        </div>
                        <div className="modal-footer"><button className="btn btn-secondary" onClick={() => setShowPay(null)}>Cancel</button><button className="btn btn-primary" onClick={makePayment}>Record Payment</button></div>
                    </div>
                </div>
            )}
        </div>
    );
}
