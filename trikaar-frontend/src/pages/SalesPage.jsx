import { useState } from 'react';
import { ShoppingCart, Plus, Minus, Trash2, Search, Receipt } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import store from '../store';

export default function SalesPage() {
    const { user } = useAuth();
    const { addToast } = useToast();
    const [showPOS, setShowPOS] = useState(false);
    const [cart, setCart] = useState([]);
    const [searchProd, setSearchProd] = useState('');
    const [selectedCustomer, setSelectedCustomer] = useState(null);
    const [paymentMethod, setPaymentMethod] = useState('Cash');
    const [discount, setDiscount] = useState(0);
    const [showReceipt, setShowReceipt] = useState(null);
    const [, refresh] = useState(0);

    const sales = store.getSales();
    const products = store.getProducts().filter(p => p.active && p.stock > 0);
    const customers = store.getCustomers().filter(c => c.active);
    const filteredProds = products.filter(p => `${p.name} ${p.sku} ${p.barcode}`.toLowerCase().includes(searchProd.toLowerCase()));

    const addToCart = (product) => {
        setCart(prev => {
            const existing = prev.find(i => i.productId === product.id);
            if (existing) {
                if (existing.qty >= product.stock) { addToast('Not enough stock', 'error'); return prev; }
                return prev.map(i => i.productId === product.id ? { ...i, qty: i.qty + 1, total: (i.qty + 1) * i.price + (i.qty + 1) * i.taxPerUnit } : i);
            }
            const taxPerUnit = Math.round(product.sellingPrice * product.taxRate / 100);
            return [...prev, { productId: product.id, name: product.name, price: product.sellingPrice, qty: 1, taxPerUnit, total: product.sellingPrice + taxPerUnit, stock: product.stock }];
        });
    };

    const updateQty = (productId, delta) => {
        setCart(prev => prev.map(i => {
            if (i.productId !== productId) return i;
            const newQty = i.qty + delta;
            if (newQty <= 0) return null;
            if (newQty > i.stock) { addToast('Not enough stock', 'error'); return i; }
            return { ...i, qty: newQty, total: newQty * i.price + newQty * i.taxPerUnit };
        }).filter(Boolean));
    };

    const removeItem = (productId) => setCart(prev => prev.filter(i => i.productId !== productId));

    const subtotal = cart.reduce((s, i) => s + i.price * i.qty, 0);
    const taxTotal = cart.reduce((s, i) => s + i.taxPerUnit * i.qty, 0);
    const grandTotal = subtotal + taxTotal - discount;

    const completeSale = () => {
        if (cart.length === 0) { addToast('Cart is empty', 'error'); return; }
        const sale = {
            customerId: selectedCustomer?.id || null,
            customerName: selectedCustomer ? `${selectedCustomer.firstName} ${selectedCustomer.lastName}` : 'Walk-in',
            employeeId: user.employeeId, employeeName: user.fullName,
            items: cart.map(i => ({ productId: i.productId, name: i.name, qty: i.qty, price: i.price, tax: i.taxPerUnit * i.qty, total: i.total })),
            subtotal, taxTotal, discount, loyaltyPointsUsed: 0, grandTotal, paymentMethod,
        };
        const created = store.createSale(sale, user.fullName);
        setShowReceipt(created);
        setCart([]); setSelectedCustomer(null); setDiscount(0); setShowPOS(false);
        addToast(`Sale ${created.txnNo} completed — ₹${grandTotal.toLocaleString()}`, 'success');
        refresh(n => n + 1);
    };

    const refund = (saleId) => {
        if (confirm('Process refund? Stock will be restored.')) {
            store.refundSale(saleId, user.fullName);
            addToast('Refund processed', 'warning');
            refresh(n => n + 1);
        }
    };

    const todaySales = sales.filter(s => s.status === 'COMPLETED');
    const todayRevenue = todaySales.reduce((s, x) => s + x.grandTotal, 0);

    return (
        <div>
            <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
                <div className="stat-card purple"><div className="stat-card-header"><div className="stat-icon purple"><ShoppingCart size={22} /></div></div><div className="stat-value">{todaySales.length}</div><div className="stat-label">Total Transactions</div></div>
                <div className="stat-card cyan"><div className="stat-card-header"><div className="stat-icon cyan"><Receipt size={22} /></div></div><div className="stat-value">₹{todayRevenue.toLocaleString()}</div><div className="stat-label">Total Revenue</div></div>
                <div className="stat-card green"><div className="stat-card-header"><div className="stat-icon green"><ShoppingCart size={22} /></div></div><div className="stat-value">₹{sales.reduce((s, x) => s + x.taxTotal, 0).toLocaleString()}</div><div className="stat-label">Tax Collected</div></div>
            </div>

            <div style={{ marginBottom: 24 }}>
                <button className="btn btn-primary" onClick={() => setShowPOS(true)} style={{ padding: '14px 28px', fontSize: 15 }}>
                    <ShoppingCart size={18} /> Open POS — New Sale
                </button>
            </div>

            <div className="card">
                <div className="card-header"><h3>Sales History</h3></div>
                <div className="card-body">
                    <table className="data-table">
                        <thead><tr><th>TXN #</th><th>Customer</th><th>Cashier</th><th>Items</th><th>Total</th><th>Payment</th><th>Status</th><th>Actions</th></tr></thead>
                        <tbody>
                            {sales.map(s => (
                                <tr key={s.id}>
                                    <td style={{ color: '#818cf8', fontWeight: 600 }}>{s.txnNo}</td>
                                    <td style={{ color: '#f1f5f9' }}>{s.customerName}</td>
                                    <td>{s.employeeName}</td>
                                    <td>{s.items.length}</td>
                                    <td style={{ fontWeight: 700 }}>₹{s.grandTotal.toLocaleString()}</td>
                                    <td><span className="badge approved">{s.paymentMethod}</span></td>
                                    <td><span className={`badge ${s.status === 'COMPLETED' ? 'completed' : 'inactive'}`}>{s.status}</span></td>
                                    <td>{s.status === 'COMPLETED' ? <button className="btn btn-sm btn-danger" onClick={() => refund(s.id)}>Refund</button> : <span style={{ fontSize: 12, color: '#ef4444' }}>Refunded</span>}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* POS Modal */}
            {showPOS && (
                <div className="modal-overlay" onClick={() => setShowPOS(false)}>
                    <div onClick={e => e.stopPropagation()} style={{ display: 'grid', gridTemplateColumns: '1fr 380px', gap: 0, width: '90%', maxWidth: 1100, height: '85vh', background: 'var(--bg-secondary)', borderRadius: 'var(--radius-xl)', border: '1px solid var(--border-color)', overflow: 'hidden' }}>
                        {/* Products */}
                        <div style={{ display: 'flex', flexDirection: 'column', borderRight: '1px solid var(--border-color)' }}>
                            <div style={{ padding: '16px 20px', borderBottom: '1px solid var(--border-color)', display: 'flex', gap: 12, alignItems: 'center' }}>
                                <h3 style={{ fontSize: 16 }}>Products</h3>
                                <div className="search-bar" style={{ flex: 1 }}><Search size={14} style={{ color: 'var(--text-muted)' }} /><input placeholder="Search product, SKU or barcode..." value={searchProd} onChange={e => setSearchProd(e.target.value)} autoFocus /></div>
                            </div>
                            <div style={{ flex: 1, overflow: 'auto', padding: 16 }}>
                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: 10 }}>
                                    {filteredProds.map(p => (
                                        <div key={p.id} onClick={() => addToCart(p)} style={{ padding: 14, background: 'var(--bg-card)', border: '1px solid var(--border-color)', borderRadius: 10, cursor: 'pointer', transition: 'all 0.2s' }} onMouseOver={e => e.currentTarget.style.borderColor = '#6366f1'} onMouseOut={e => e.currentTarget.style.borderColor = 'var(--border-color)'}>
                                            <div style={{ fontSize: 13, fontWeight: 600, color: '#f1f5f9', marginBottom: 4 }}>{p.name}</div>
                                            <div style={{ fontSize: 11, color: 'var(--text-muted)', marginBottom: 6 }}>{p.sku} • Stock: {p.stock}</div>
                                            <div style={{ fontSize: 16, fontWeight: 800, color: '#818cf8' }}>₹{p.sellingPrice.toLocaleString()}</div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>

                        {/* Cart */}
                        <div style={{ display: 'flex', flexDirection: 'column', background: 'var(--bg-card)' }}>
                            <div style={{ padding: '16px 20px', borderBottom: '1px solid var(--border-color)' }}>
                                <h3 style={{ fontSize: 16 }}>Cart ({cart.length})</h3>
                            </div>
                            <div style={{ padding: '12px 16px', borderBottom: '1px solid var(--border-color)' }}>
                                <select className="form-control" value={selectedCustomer?.id || ''} onChange={e => setSelectedCustomer(customers.find(c => c.id === e.target.value) || null)} style={{ fontSize: 12 }}>
                                    <option value="">Walk-in Customer</option>
                                    {customers.map(c => <option key={c.id} value={c.id}>{c.firstName} {c.lastName} — {c.phone}</option>)}
                                </select>
                            </div>
                            <div style={{ flex: 1, overflow: 'auto', padding: '8px 16px' }}>
                                {cart.map(item => (
                                    <div key={item.productId} style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '10px 0', borderBottom: '1px solid rgba(255,255,255,0.04)' }}>
                                        <div style={{ flex: 1 }}>
                                            <div style={{ fontSize: 13, fontWeight: 500, color: '#f1f5f9' }}>{item.name}</div>
                                            <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>₹{item.price} + ₹{item.taxPerUnit} tax</div>
                                        </div>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                            <button onClick={() => updateQty(item.productId, -1)} style={{ width: 26, height: 26, borderRadius: 6, border: '1px solid var(--border-color)', background: 'var(--bg-primary)', color: '#f1f5f9', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}><Minus size={12} /></button>
                                            <span style={{ fontWeight: 700, width: 24, textAlign: 'center' }}>{item.qty}</span>
                                            <button onClick={() => updateQty(item.productId, 1)} style={{ width: 26, height: 26, borderRadius: 6, border: '1px solid var(--border-color)', background: 'var(--bg-primary)', color: '#f1f5f9', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' }}><Plus size={12} /></button>
                                        </div>
                                        <div style={{ fontWeight: 700, width: 70, textAlign: 'right', fontSize: 13 }}>₹{item.total.toLocaleString()}</div>
                                        <button onClick={() => removeItem(item.productId)} style={{ background: 'none', border: 'none', color: '#ef4444', cursor: 'pointer' }}><Trash2 size={14} /></button>
                                    </div>
                                ))}
                                {cart.length === 0 && <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)', fontSize: 13 }}>Click a product to add</div>}
                            </div>
                            <div style={{ padding: 16, borderTop: '1px solid var(--border-color)', background: 'var(--bg-primary)' }}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 6 }}><span style={{ color: 'var(--text-secondary)' }}>Subtotal</span><span>₹{subtotal.toLocaleString()}</span></div>
                                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 6 }}><span style={{ color: 'var(--text-secondary)' }}>Tax</span><span>₹{taxTotal.toLocaleString()}</span></div>
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 13, marginBottom: 8 }}>
                                    <span style={{ color: 'var(--text-secondary)' }}>Discount</span>
                                    <input type="number" value={discount} onChange={e => setDiscount(Number(e.target.value))} style={{ width: 80, padding: '4px 8px', background: 'var(--bg-card)', border: '1px solid var(--border-color)', borderRadius: 6, color: '#f1f5f9', textAlign: 'right', fontFamily: 'inherit' }} />
                                </div>
                                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 18, fontWeight: 800, marginBottom: 12, paddingTop: 8, borderTop: '1px solid var(--border-color)' }}><span>Grand Total</span><span style={{ color: '#818cf8' }}>₹{grandTotal.toLocaleString()}</span></div>
                                <select className="form-control" value={paymentMethod} onChange={e => setPaymentMethod(e.target.value)} style={{ marginBottom: 10 }}>
                                    <option>Cash</option><option>UPI</option><option>Card</option><option>Credit</option>
                                </select>
                                <button className="btn btn-primary" style={{ width: '100%', padding: 12, fontSize: 15, justifyContent: 'center' }} onClick={completeSale}>
                                    Complete Sale — ₹{grandTotal.toLocaleString()}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Receipt Modal */}
            {showReceipt && (
                <div className="modal-overlay" onClick={() => setShowReceipt(null)}>
                    <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 400 }}>
                        <div className="modal-header"><h3>🧾 Receipt</h3><button className="icon-button" onClick={() => setShowReceipt(null)}>✕</button></div>
                        <div className="modal-body" style={{ fontFamily: 'monospace', fontSize: 12 }}>
                            <div style={{ textAlign: 'center', marginBottom: 12 }}><strong style={{ fontSize: 16 }}>TRIKAAR RETAIL</strong><br />123 MG Road, Mumbai<br />GSTIN: 27AABCT0001B1Z5</div>
                            <div style={{ borderTop: '1px dashed var(--border-color)', margin: '8px 0' }} />
                            <div>TXN: {showReceipt.txnNo}<br />Date: {new Date(showReceipt.createdAt).toLocaleString('en-IN')}<br />Customer: {showReceipt.customerName}<br />Cashier: {showReceipt.employeeName}</div>
                            <div style={{ borderTop: '1px dashed var(--border-color)', margin: '8px 0' }} />
                            {showReceipt.items.map((it, i) => <div key={i} style={{ display: 'flex', justifyContent: 'space-between' }}><span>{it.name} x{it.qty}</span><span>₹{it.total}</span></div>)}
                            <div style={{ borderTop: '1px dashed var(--border-color)', margin: '8px 0' }} />
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}><span>Subtotal</span><span>₹{showReceipt.subtotal}</span></div>
                            <div style={{ display: 'flex', justifyContent: 'space-between' }}><span>Tax</span><span>₹{showReceipt.taxTotal}</span></div>
                            {showReceipt.discount > 0 && <div style={{ display: 'flex', justifyContent: 'space-between' }}><span>Discount</span><span>-₹{showReceipt.discount}</span></div>}
                            <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 800, fontSize: 16, marginTop: 8 }}><span>TOTAL</span><span>₹{showReceipt.grandTotal}</span></div>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4 }}><span>Payment</span><span>{showReceipt.paymentMethod}</span></div>
                            <div style={{ textAlign: 'center', marginTop: 16, color: 'var(--text-muted)' }}>Thank you for shopping!</div>
                        </div>
                        <div className="modal-footer"><button className="btn btn-primary" onClick={() => setShowReceipt(null)}>Close</button></div>
                    </div>
                </div>
            )}
        </div>
    );
}
