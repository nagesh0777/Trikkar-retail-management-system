import { useState } from 'react';
import { Settings, Save } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import store from '../store';

export default function ConfigPage() {
    const { user } = useAuth();
    const { addToast } = useToast();
    const [configs, setConfigs] = useState(store.getConfig());
    const [cat, setCat] = useState('Business');
    const cats = [...new Set(configs.map(c => c.category))];

    const handleChange = (id, value) => { setConfigs(prev => prev.map(c => c.id === id ? { ...c, value } : c)); };
    const handleSave = () => {
        configs.forEach(c => { const orig = store.getConfig().find(x => x.id === c.id); if (orig && orig.value !== c.value && c.editable) store.updateConfig(c.id, c.value, user.fullName); });
        addToast('Configuration saved', 'success');
    };

    return (
        <div>
            <div style={{ display: 'flex', gap: 12, marginBottom: 24 }}>
                {cats.map(c => <button key={c} className={`btn btn-sm ${cat === c ? 'btn-primary' : 'btn-secondary'}`} onClick={() => setCat(c)}>{c}</button>)}
            </div>
            <div className="card">
                <div className="card-header"><h3><Settings size={18} style={{ verticalAlign: 'middle', marginRight: 8 }} />{cat} Configuration</h3><button className="btn btn-primary btn-sm" onClick={handleSave}><Save size={14} /> Save Changes</button></div>
                <div className="card-body padded">
                    {configs.filter(c => c.category === cat).map(cfg => (
                        <div key={cfg.id} className="form-group">
                            <label style={{ display: 'flex', justifyContent: 'space-between' }}>
                                <span>{cfg.key}</span>
                                {!cfg.editable && <span style={{ fontSize: 10, color: 'var(--text-muted)' }}>READ ONLY</span>}
                            </label>
                            <input className="form-control" value={cfg.value} onChange={e => handleChange(cfg.id, e.target.value)} disabled={!cfg.editable} style={{ opacity: cfg.editable ? 1 : 0.5 }} />
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}
