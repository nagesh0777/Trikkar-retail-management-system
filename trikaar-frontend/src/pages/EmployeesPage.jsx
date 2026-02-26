import { useState } from 'react';
import { Plus, Search, Edit2, Trash2, Key, Shield } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import store from '../store';

const ALL_PAGES = ['dashboard', 'employees', 'attendance', 'customers', 'inventory', 'sales', 'suppliers', 'purchases', 'expenses', 'cash_register', 'salary', 'loyalty', 'reports', 'config', 'audit'];
const ROLES = ['WORKER', 'CASHIER', 'STOCK_MANAGER', 'ACCOUNTANT', 'MANAGER', 'SUPER_ADMIN'];

const emptyForm = { firstName: '', lastName: '', email: '', phone: '', department: '', designation: '', employmentType: 'FULL_TIME', wageType: 'MONTHLY', baseSalary: '', hourlyRate: '', dailyRate: '', incentivePercent: '', address: '', bankAccount: '', pan: '', dateOfJoining: new Date().toISOString().split('T')[0], role: 'WORKER', access: ['dashboard', 'attendance'] };

export default function EmployeesPage() {
    const { user } = useAuth();
    const { addToast } = useToast();
    const [search, setSearch] = useState('');
    const [showForm, setShowForm] = useState(false);
    const [showCreds, setShowCreds] = useState(null);
    const [showAccess, setShowAccess] = useState(null);
    const [editId, setEditId] = useState(null);
    const [form, setForm] = useState({ ...emptyForm });
    const [, refresh] = useState(0);

    const employees = store.getEmployees();
    const filtered = employees.filter(e => e.status !== 'TERMINATED' && (`${e.firstName} ${e.lastName} ${e.code}`.toLowerCase().includes(search.toLowerCase())));

    const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

    const openAdd = () => { setEditId(null); setForm({ ...emptyForm }); setShowForm(true); };
    const openEdit = (emp) => { setEditId(emp.id); setForm({ ...emp }); setShowForm(true); };

    const handleSave = () => {
        if (!form.firstName || !form.lastName) { addToast('First & Last name required', 'error'); return; }
        if (editId) {
            store.updateEmployee(editId, form, user.fullName);
            addToast('Employee updated', 'success');
        } else {
            const result = store.addEmployee(form, user.fullName);
            setShowCreds({ ...result.credentials, name: `${form.firstName} ${form.lastName}`, code: result.employee.code });
            addToast('Employee created with login credentials', 'success');
        }
        setShowForm(false);
        refresh(n => n + 1);
    };

    const handleDelete = (emp) => {
        if (confirm(`Terminate ${emp.firstName} ${emp.lastName}?`)) {
            store.deleteEmployee(emp.id, user.fullName);
            addToast(`${emp.firstName} terminated`, 'warning');
            refresh(n => n + 1);
        }
    };

    const handleAccessSave = () => {
        store.updateEmployee(showAccess.id, { access: showAccess.access, role: showAccess.role }, user.fullName);
        addToast('Access updated', 'success');
        setShowAccess(null);
        refresh(n => n + 1);
    };

    const toggleAccess = (page) => {
        setShowAccess(prev => ({
            ...prev,
            access: prev.access.includes(page) ? prev.access.filter(p => p !== page) : [...prev.access, page]
        }));
    };

    return (
        <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
                <p style={{ color: 'var(--text-secondary)', fontSize: 14 }}>Manage workforce • {filtered.length} active employees</p>
                <button className="btn btn-primary" onClick={openAdd}><Plus size={16} /> Add Employee</button>
            </div>

            <div className="card">
                <div className="card-header">
                    <h3>Employees</h3>
                    <div className="search-bar" style={{ width: 260 }}>
                        <Search size={14} style={{ color: 'var(--text-muted)' }} />
                        <input placeholder="Search by name or code..." value={search} onChange={e => setSearch(e.target.value)} />
                    </div>
                </div>
                <div className="card-body">
                    <table className="data-table">
                        <thead><tr><th>Code</th><th>Name</th><th>Department</th><th>Designation</th><th>Wage</th><th>Base Pay</th><th>Status</th><th>Actions</th></tr></thead>
                        <tbody>
                            {filtered.map(emp => (
                                <tr key={emp.id}>
                                    <td style={{ color: '#818cf8', fontWeight: 600 }}>{emp.code}</td>
                                    <td><div style={{ color: '#f1f5f9', fontWeight: 500 }}>{emp.firstName} {emp.lastName}</div><div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{emp.email}</div></td>
                                    <td>{emp.department}</td>
                                    <td>{emp.designation}</td>
                                    <td><span className="badge approved">{emp.wageType}</span></td>
                                    <td style={{ fontWeight: 600 }}>₹{Number(emp.wageType === 'MONTHLY' ? emp.baseSalary : emp.wageType === 'DAILY' ? emp.dailyRate : emp.hourlyRate).toLocaleString()}</td>
                                    <td><span className={`badge ${emp.status.toLowerCase()}`}>{emp.status}</span></td>
                                    <td>
                                        <div style={{ display: 'flex', gap: 4 }}>
                                            <button className="btn btn-sm btn-secondary" onClick={() => openEdit(emp)} title="Edit"><Edit2 size={13} /></button>
                                            <button className="btn btn-sm btn-secondary" onClick={() => { const u = store.getUsers().find(u => u.employeeId === emp.id); setShowAccess({ ...emp, access: u?.access || ['dashboard'], role: u?.role || 'WORKER' }); }} title="Access Control"><Shield size={13} /></button>
                                            <button className="btn btn-sm btn-danger" onClick={() => handleDelete(emp)} title="Terminate"><Trash2 size={13} /></button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Add/Edit Modal */}
            {showForm && (
                <div className="modal-overlay" onClick={() => setShowForm(false)}>
                    <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 650 }}>
                        <div className="modal-header"><h3>{editId ? 'Edit' : 'Add'} Employee</h3><button className="icon-button" onClick={() => setShowForm(false)}>✕</button></div>
                        <div className="modal-body">
                            <div className="form-row">
                                <div className="form-group"><label>First Name *</label><input className="form-control" value={form.firstName} onChange={e => set('firstName', e.target.value)} /></div>
                                <div className="form-group"><label>Last Name *</label><input className="form-control" value={form.lastName} onChange={e => set('lastName', e.target.value)} /></div>
                            </div>
                            <div className="form-row">
                                <div className="form-group"><label>Email</label><input className="form-control" type="email" value={form.email} onChange={e => set('email', e.target.value)} /></div>
                                <div className="form-group"><label>Phone</label><input className="form-control" value={form.phone} onChange={e => set('phone', e.target.value)} /></div>
                            </div>
                            <div className="form-row">
                                <div className="form-group"><label>Department</label><input className="form-control" value={form.department} onChange={e => set('department', e.target.value)} placeholder="e.g. Sales" /></div>
                                <div className="form-group"><label>Designation</label><input className="form-control" value={form.designation} onChange={e => set('designation', e.target.value)} placeholder="e.g. Manager" /></div>
                            </div>
                            <div className="form-row-3">
                                <div className="form-group"><label>Employment Type</label><select className="form-control" value={form.employmentType} onChange={e => set('employmentType', e.target.value)}><option value="FULL_TIME">Full Time</option><option value="PART_TIME">Part Time</option><option value="CONTRACT">Contract</option></select></div>
                                <div className="form-group"><label>Wage Type</label><select className="form-control" value={form.wageType} onChange={e => set('wageType', e.target.value)}><option value="MONTHLY">Monthly</option><option value="DAILY">Daily</option><option value="HOURLY">Hourly</option></select></div>
                                <div className="form-group"><label>{form.wageType === 'MONTHLY' ? 'Monthly Salary' : form.wageType === 'DAILY' ? 'Daily Rate' : 'Hourly Rate'} *</label><input className="form-control" type="number" value={form.wageType === 'MONTHLY' ? form.baseSalary : form.wageType === 'DAILY' ? form.dailyRate : form.hourlyRate} onChange={e => set(form.wageType === 'MONTHLY' ? 'baseSalary' : form.wageType === 'DAILY' ? 'dailyRate' : 'hourlyRate', Number(e.target.value))} /></div>
                            </div>
                            <div className="form-row">
                                <div className="form-group"><label>Date of Joining</label><input className="form-control" type="date" value={form.dateOfJoining} onChange={e => set('dateOfJoining', e.target.value)} /></div>
                                <div className="form-group"><label>Incentive %</label><input className="form-control" type="number" value={form.incentivePercent} onChange={e => set('incentivePercent', Number(e.target.value))} placeholder="0" /></div>
                            </div>
                            <div className="form-group"><label>Address</label><input className="form-control" value={form.address} onChange={e => set('address', e.target.value)} /></div>
                            <div className="form-row">
                                <div className="form-group"><label>Bank Account</label><input className="form-control" value={form.bankAccount} onChange={e => set('bankAccount', e.target.value)} /></div>
                                <div className="form-group"><label>PAN</label><input className="form-control" value={form.pan} onChange={e => set('pan', e.target.value)} /></div>
                            </div>
                            {!editId && (
                                <div className="form-group">
                                    <label>Role (for System Access)</label>
                                    <select className="form-control" value={form.role} onChange={e => set('role', e.target.value)}>
                                        {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
                                    </select>
                                </div>
                            )}
                        </div>
                        <div className="modal-footer">
                            <button className="btn btn-secondary" onClick={() => setShowForm(false)}>Cancel</button>
                            <button className="btn btn-primary" onClick={handleSave}>{editId ? 'Update' : 'Create Employee'}</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Credentials Modal */}
            {showCreds && (
                <div className="modal-overlay" onClick={() => setShowCreds(null)}>
                    <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 440 }}>
                        <div className="modal-header"><h3><Key size={18} style={{ verticalAlign: 'middle', marginRight: 8 }} />Login Credentials Generated</h3></div>
                        <div className="modal-body" style={{ textAlign: 'center' }}>
                            <div style={{ background: 'rgba(16,185,129,0.08)', border: '1px solid rgba(16,185,129,0.2)', borderRadius: 12, padding: 24, marginBottom: 20 }}>
                                <div style={{ fontSize: 14, color: 'var(--text-secondary)', marginBottom: 4 }}>{showCreds.code} — {showCreds.name}</div>
                                <div style={{ marginTop: 16, display: 'flex', flexDirection: 'column', gap: 12 }}>
                                    <div><span style={{ fontSize: 12, color: 'var(--text-muted)' }}>Username</span><div style={{ fontSize: 20, fontWeight: 800, color: '#10b981' }}>{showCreds.username}</div></div>
                                    <div><span style={{ fontSize: 12, color: 'var(--text-muted)' }}>Password</span><div style={{ fontSize: 20, fontWeight: 800, color: '#818cf8' }}>{showCreds.password}</div></div>
                                </div>
                            </div>
                            <p style={{ fontSize: 12, color: 'var(--text-muted)' }}>Share these credentials with the employee. They can log in at the login page.</p>
                        </div>
                        <div className="modal-footer"><button className="btn btn-primary" onClick={() => setShowCreds(null)}>Done</button></div>
                    </div>
                </div>
            )}

            {/* Access Control Modal */}
            {showAccess && (
                <div className="modal-overlay" onClick={() => setShowAccess(null)}>
                    <div className="modal" onClick={e => e.stopPropagation()} style={{ maxWidth: 480 }}>
                        <div className="modal-header"><h3><Shield size={18} style={{ verticalAlign: 'middle', marginRight: 8 }} />Page Access — {showAccess.firstName}</h3></div>
                        <div className="modal-body">
                            <div className="form-group">
                                <label>System Role</label>
                                <select className="form-control" value={showAccess.role} onChange={e => setShowAccess(p => ({ ...p, role: e.target.value }))}>
                                    {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
                                </select>
                            </div>
                            <label style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 10, display: 'block' }}>Page Access</label>
                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
                                {ALL_PAGES.map(page => (
                                    <label key={page} style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '8px 12px', borderRadius: 8, border: `1px solid ${showAccess.access.includes(page) ? '#6366f1' : 'var(--border-color)'}`, background: showAccess.access.includes(page) ? 'rgba(99,102,241,0.08)' : 'transparent', cursor: 'pointer', fontSize: 13, transition: 'all 0.2s' }}>
                                        <input type="checkbox" checked={showAccess.access.includes(page)} onChange={() => toggleAccess(page)} style={{ accentColor: '#6366f1' }} />
                                        <span style={{ textTransform: 'capitalize' }}>{page}</span>
                                    </label>
                                ))}
                            </div>
                        </div>
                        <div className="modal-footer">
                            <button className="btn btn-secondary" onClick={() => setShowAccess(null)}>Cancel</button>
                            <button className="btn btn-primary" onClick={handleAccessSave}>Save Access</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
