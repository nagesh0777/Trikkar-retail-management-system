import { useState } from 'react';
import { Shield, Search, RefreshCw } from 'lucide-react';
import store from '../store';

const actionColors = {
    SALE_CREATED: '#10b981', USER_LOGIN: '#3b82f6', USER_LOGOUT: '#64748b', REFUND_PROCESSED: '#f59e0b',
    SALARY_GENERATED: '#22d3ee', SALARY_APPROVED: '#a78bfa', SALARY_PAID: '#10b981', CONFIG_CHANGED: '#f59e0b',
    EMPLOYEE_CREATED: '#6366f1', EMPLOYEE_UPDATED: '#818cf8', EMPLOYEE_TERMINATED: '#ef4444',
    CUSTOMER_CREATED: '#22d3ee', CUSTOMER_UPDATED: '#06b6d4', CUSTOMER_DEACTIVATED: '#ef4444',
    PRODUCT_CREATED: '#10b981', PRODUCT_UPDATED: '#34d399', STOCK_UPDATED: '#f59e0b',
    ATTENDANCE_MARKED: '#3b82f6', SALE_REFUNDED: '#ef4444', SYSTEM_INIT: '#64748b',
};

export default function AuditPage() {
    const [search, setSearch] = useState('');
    const [filter, setFilter] = useState('ALL');
    const [, refresh] = useState(0);

    const logs = store.getAudit();
    const filtered = logs.filter(l => {
        const matchSearch = `${l.action} ${l.description} ${l.performedBy}`.toLowerCase().includes(search.toLowerCase());
        const matchFilter = filter === 'ALL' || l.action.startsWith(filter);
        return matchSearch && matchFilter;
    });

    const categories = ['ALL', 'SALE', 'USER', 'EMPLOYEE', 'CUSTOMER', 'PRODUCT', 'STOCK', 'SALARY', 'CONFIG', 'ATTENDANCE'];

    return (
        <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
                <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                    {categories.map(c => <button key={c} className={`btn btn-sm ${filter === c ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setFilter(c)} style={{ fontSize: 11 }}>{c}</button>)}
                </div>
                <button className="btn btn-sm btn-secondary" onClick={() => refresh(n => n + 1)}><RefreshCw size={14} /> Refresh</button>
            </div>

            <div className="card">
                <div className="card-header">
                    <h3><Shield size={18} style={{ verticalAlign: 'middle', marginRight: 8 }} />Audit Trail ({filtered.length})</h3>
                    <div className="search-bar" style={{ width: 260 }}><Search size={14} style={{ color: 'var(--text-muted)' }} /><input placeholder="Search logs..." value={search} onChange={e => setSearch(e.target.value)} /></div>
                </div>
                <div className="card-body">
                    <table className="data-table">
                        <thead><tr><th>Timestamp</th><th>Action</th><th>Entity</th><th>Description</th><th>User</th></tr></thead>
                        <tbody>
                            {filtered.slice(0, 100).map((log, i) => (
                                <tr key={log.id || i}>
                                    <td style={{ fontSize: 12, whiteSpace: 'nowrap' }}>{new Date(log.createdAt).toLocaleString('en-IN')}</td>
                                    <td><span style={{ display: 'inline-block', padding: '3px 8px', borderRadius: 4, fontSize: 10, fontWeight: 700, background: `${actionColors[log.action] || '#64748b'}18`, color: actionColors[log.action] || '#64748b' }}>{log.action}</span></td>
                                    <td style={{ fontSize: 12 }}>{log.entityType}</td>
                                    <td style={{ color: '#f1f5f9', fontSize: 13 }}>{log.description}</td>
                                    <td>{log.performedBy}</td>
                                </tr>
                            ))}
                            {filtered.length === 0 && <tr><td colSpan={5} style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>No audit logs found</td></tr>}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}
