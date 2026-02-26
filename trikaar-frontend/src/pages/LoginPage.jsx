import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import store from '../store';

export default function LoginPage() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const { login } = useAuth();
    const { addToast } = useToast();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            const u = await login(username, password);
            addToast(`Welcome back, ${u.fullName}!`, 'success');
            navigate('/');
        } catch (err) { setError(err.message); }
    };

    const fill = (u) => { setUsername(u); setPassword(u === 'admin' ? 'admin123' : 'pass123'); };

    const accounts = store.getUsers().map(u => ({ username: u.username, role: u.role, name: u.fullName }));

    return (
        <div className="login-page">
            <div className="login-card">
                <div className="logo-container">
                    <div className="logo-icon" style={{ width: 52, height: 52, fontSize: 22 }}>T</div>
                    <div className="logo-text"><h1 style={{ fontSize: 26 }}>TRIKAAR</h1><span>Retail Management Platform</span></div>
                </div>
                <h2>Welcome Back</h2>
                <p className="subtitle">Sign in to manage your business</p>
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Username</label>
                        <input className="form-control" value={username} onChange={e => setUsername(e.target.value)} placeholder="Enter username" required />
                    </div>
                    <div className="form-group">
                        <label>Password</label>
                        <input className="form-control" type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Enter password" required />
                    </div>
                    {error && <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.2)', borderRadius: 8, padding: '10px 14px', marginBottom: 16, color: '#ef4444', fontSize: 13 }}>{error}</div>}
                    <button className="btn btn-primary" type="submit">Sign In</button>
                </form>
                <div style={{ marginTop: 24, padding: '14px 16px', background: 'rgba(99,102,241,0.06)', border: '1px solid rgba(99,102,241,0.12)', borderRadius: 12 }}>
                    <div style={{ fontSize: 10, fontWeight: 700, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: 1, marginBottom: 10 }}>Quick Login</div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                        {accounts.map(a => (
                            <button key={a.username} type="button" onClick={() => fill(a.username)}
                                style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '8px 12px', borderRadius: 8, border: '1px solid var(--border-color)', background: 'var(--bg-card)', color: 'var(--text-primary)', fontSize: 12, cursor: 'pointer', fontFamily: 'inherit', transition: 'all 0.2s' }}
                                onMouseOver={e => e.currentTarget.style.borderColor = '#6366f1'} onMouseOut={e => e.currentTarget.style.borderColor = 'var(--border-color)'}>
                                <span><strong>{a.username}</strong> — {a.name}</span>
                                <span className={`badge ${a.role === 'SUPER_ADMIN' ? 'admin' : a.role === 'CASHIER' ? 'worker' : 'approved'}`}>{a.role}</span>
                            </button>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
}
