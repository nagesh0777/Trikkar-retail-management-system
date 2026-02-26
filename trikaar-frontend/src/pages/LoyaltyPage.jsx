import { Heart, Star, TrendingUp } from 'lucide-react';
import store from '../store';

const tierColors = { PLATINUM: '#a78bfa', GOLD: '#f59e0b', SILVER: '#94a3b8', BRONZE: '#d97706' };

export default function LoyaltyPage() {
    const customers = store.getCustomers().filter(c => c.active);
    const config = store.getConfig();
    const getVal = (k) => config.find(c => c.key === k)?.value || '—';

    const tiers = { PLATINUM: 0, GOLD: 0, SILVER: 0, BRONZE: 0 };
    customers.forEach(c => { tiers[c.loyaltyTier] = (tiers[c.loyaltyTier] || 0) + 1; });
    const totalPoints = customers.reduce((s, c) => s + c.loyaltyPoints, 0);

    return (
        <div>
            <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
                <div className="stat-card purple"><div className="stat-card-header"><div className="stat-icon purple"><Heart size={22} /></div></div><div className="stat-value">{customers.length}</div><div className="stat-label">Loyalty Members</div></div>
                <div className="stat-card cyan"><div className="stat-card-header"><div className="stat-icon cyan"><Star size={22} /></div></div><div className="stat-value">{totalPoints.toLocaleString()}</div><div className="stat-label">Total Points Issued</div></div>
                <div className="stat-card green"><div className="stat-card-header"><div className="stat-icon green"><TrendingUp size={22} /></div></div><div className="stat-value">{getVal('loyalty.points_per_rupee')}</div><div className="stat-label">Points per ₹1</div></div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20, marginBottom: 24 }}>
                <div className="card">
                    <div className="card-header"><h3>Tier Distribution</h3></div>
                    <div className="card-body padded">
                        {Object.entries(tiers).map(([tier, count]) => (
                            <div key={tier} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 0', borderBottom: '1px solid var(--border-color)' }}>
                                <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6, padding: '4px 12px', borderRadius: 20, fontSize: 12, fontWeight: 700, background: `${tierColors[tier]}18`, color: tierColors[tier] }}><Star size={12} /> {tier}</span>
                                <span style={{ fontWeight: 700, fontSize: 18 }}>{count}</span>
                            </div>
                        ))}
                    </div>
                </div>
                <div className="card">
                    <div className="card-header"><h3>Tier Thresholds</h3></div>
                    <div className="card-body padded">
                        {[{ tier: 'BRONZE', min: '₹0' }, { tier: 'SILVER', min: `₹${Number(getVal('loyalty.silver_threshold')).toLocaleString()}` }, { tier: 'GOLD', min: `₹${Number(getVal('loyalty.gold_threshold')).toLocaleString()}` }, { tier: 'PLATINUM', min: `₹${Number(getVal('loyalty.platinum_threshold')).toLocaleString()}` }].map(t => (
                            <div key={t.tier} style={{ display: 'flex', justifyContent: 'space-between', padding: '12px 0', borderBottom: '1px solid var(--border-color)' }}>
                                <span style={{ color: tierColors[t.tier], fontWeight: 600 }}>{t.tier}</span>
                                <span style={{ fontWeight: 500 }}>Min Spend: {t.min}</span>
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            <div className="card">
                <div className="card-header"><h3>All Loyalty Members</h3></div>
                <div className="card-body">
                    <table className="data-table">
                        <thead><tr><th>Customer</th><th>Tier</th><th>Points</th><th>Total Spent</th><th>Visits</th></tr></thead>
                        <tbody>
                            {[...customers].sort((a, b) => b.loyaltyPoints - a.loyaltyPoints).map(c => (
                                <tr key={c.id}>
                                    <td style={{ color: '#f1f5f9', fontWeight: 500 }}>{c.firstName} {c.lastName}</td>
                                    <td><span style={{ display: 'inline-flex', alignItems: 'center', gap: 4, padding: '4px 10px', borderRadius: 20, fontSize: 11, fontWeight: 600, background: `${tierColors[c.loyaltyTier]}18`, color: tierColors[c.loyaltyTier] }}><Star size={11} /> {c.loyaltyTier}</span></td>
                                    <td style={{ fontWeight: 700 }}>{c.loyaltyPoints.toLocaleString()}</td>
                                    <td style={{ fontWeight: 600 }}>₹{c.totalSpent.toLocaleString()}</td>
                                    <td>{c.totalVisits}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}
