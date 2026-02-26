import { useState } from 'react';
import { Calendar, Clock, UserCheck, UserX } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import store from '../store';

export default function AttendancePage() {
    const { user } = useAuth();
    const { addToast } = useToast();
    const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
    const [, refresh] = useState(0);

    const employees = store.getEmployees().filter(e => e.status === 'ACTIVE');
    const attendance = store.getAttendance();
    const todayRecords = attendance.filter(a => a.date === date);

    const getRecord = (empId) => todayRecords.find(a => a.employeeId === empId);

    const markPresent = (emp) => {
        const now = new Date();
        const time = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
        store.markAttendance({ employeeId: emp.id, employeeName: `${emp.firstName} ${emp.lastName}`, date, checkIn: time, checkOut: null, status: 'PRESENT', overtimeHours: 0, notes: '' }, user.fullName);
        addToast(`${emp.firstName} marked present at ${time}`, 'success');
        refresh(n => n + 1);
    };

    const markCheckOut = (emp) => {
        const now = new Date();
        const time = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
        const record = getRecord(emp.id);
        if (record) {
            const [inH, inM] = record.checkIn.split(':').map(Number);
            const totalHrs = (now.getHours() + now.getMinutes() / 60) - (inH + inM / 60);
            const overtime = Math.max(0, totalHrs - 9);
            store.markAttendance({ ...record, checkOut: time, overtimeHours: Math.round(overtime * 10) / 10 }, user.fullName);
            addToast(`${emp.firstName} checked out at ${time}`, 'success');
            refresh(n => n + 1);
        }
    };

    const markAbsent = (emp) => {
        store.markAttendance({ employeeId: emp.id, employeeName: `${emp.firstName} ${emp.lastName}`, date, checkIn: null, checkOut: null, status: 'ABSENT', overtimeHours: 0, notes: '' }, user.fullName);
        addToast(`${emp.firstName} marked absent`, 'warning');
        refresh(n => n + 1);
    };

    const markLeave = (emp) => {
        store.markAttendance({ employeeId: emp.id, employeeName: `${emp.firstName} ${emp.lastName}`, date, checkIn: null, checkOut: null, status: 'ON_LEAVE', overtimeHours: 0, notes: 'Leave' }, user.fullName);
        addToast(`${emp.firstName} marked on leave`, 'warning');
        refresh(n => n + 1);
    };

    const present = todayRecords.filter(a => a.status === 'PRESENT').length;
    const absent = todayRecords.filter(a => a.status === 'ABSENT').length;
    const onLeave = todayRecords.filter(a => a.status === 'ON_LEAVE').length;
    const unmarked = employees.length - todayRecords.length;

    return (
        <div>
            <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)' }}>
                <div className="stat-card green"><div className="stat-card-header"><div className="stat-icon green"><UserCheck size={22} /></div></div><div className="stat-value">{present}</div><div className="stat-label">Present</div></div>
                <div className="stat-card amber"><div className="stat-card-header"><div className="stat-icon amber"><UserX size={22} /></div></div><div className="stat-value">{absent}</div><div className="stat-label">Absent</div></div>
                <div className="stat-card cyan"><div className="stat-card-header"><div className="stat-icon cyan"><Calendar size={22} /></div></div><div className="stat-value">{onLeave}</div><div className="stat-label">On Leave</div></div>
                <div className="stat-card purple"><div className="stat-card-header"><div className="stat-icon purple"><Clock size={22} /></div></div><div className="stat-value">{unmarked}</div><div className="stat-label">Unmarked</div></div>
            </div>

            <div className="card">
                <div className="card-header">
                    <h3>Attendance — {new Date(date + 'T00:00').toLocaleDateString('en-IN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</h3>
                    <input type="date" className="form-control" style={{ width: 180 }} value={date} onChange={e => setDate(e.target.value)} />
                </div>
                <div className="card-body">
                    <table className="data-table">
                        <thead><tr><th>Employee</th><th>Department</th><th>Check In</th><th>Check Out</th><th>OT (hrs)</th><th>Status</th><th>Actions</th></tr></thead>
                        <tbody>
                            {employees.map(emp => {
                                const rec = getRecord(emp.id);
                                return (
                                    <tr key={emp.id}>
                                        <td><div style={{ color: '#f1f5f9', fontWeight: 500 }}>{emp.firstName} {emp.lastName}</div><div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{emp.code}</div></td>
                                        <td>{emp.department}</td>
                                        <td style={{ fontWeight: 600, color: rec?.checkIn ? '#10b981' : 'var(--text-muted)' }}>{rec?.checkIn || '—'}</td>
                                        <td style={{ fontWeight: 600, color: rec?.checkOut ? '#22d3ee' : 'var(--text-muted)' }}>{rec?.checkOut || '—'}</td>
                                        <td>{rec?.overtimeHours > 0 ? <span style={{ color: '#f59e0b', fontWeight: 700 }}>{rec.overtimeHours}</span> : '—'}</td>
                                        <td>
                                            {rec ? <span className={`badge ${rec.status === 'PRESENT' ? 'active' : rec.status === 'ON_LEAVE' ? 'pending' : 'inactive'}`}>{rec.status}</span> : <span style={{ fontSize: 12, color: 'var(--text-muted)' }}>Not marked</span>}
                                        </td>
                                        <td>
                                            <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
                                                {!rec && <>
                                                    <button className="btn btn-sm btn-primary" onClick={() => markPresent(emp)}>Check In</button>
                                                    <button className="btn btn-sm btn-danger" onClick={() => markAbsent(emp)}>Absent</button>
                                                    <button className="btn btn-sm btn-secondary" onClick={() => markLeave(emp)}>Leave</button>
                                                </>}
                                                {rec?.status === 'PRESENT' && !rec?.checkOut && <button className="btn btn-sm btn-secondary" onClick={() => markCheckOut(emp)}>Check Out</button>}
                                                {rec?.checkOut && <span style={{ fontSize: 12, color: 'var(--text-muted)', padding: '6px 0' }}>Done ✓</span>}
                                                {rec?.status === 'ABSENT' && <span style={{ fontSize: 12, color: '#ef4444', padding: '6px 0' }}>Absent</span>}
                                                {rec?.status === 'ON_LEAVE' && <span style={{ fontSize: 12, color: '#f59e0b', padding: '6px 0' }}>On Leave</span>}
                                            </div>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}
