import { createContext, useContext, useState, useCallback } from 'react';
import store from '../store';

const AuthContext = createContext(null);
export const useAuth = () => useContext(AuthContext);

const ALL_PAGES = ['dashboard', 'employees', 'customers', 'inventory', 'sales', 'salary', 'loyalty', 'reports', 'config', 'audit', 'attendance'];

export function AuthProvider({ children }) {
    store.init();
    const [user, setUser] = useState(() => {
        const stored = localStorage.getItem('trikaar_user');
        return stored ? JSON.parse(stored) : null;
    });

    const login = async (username, password) => {
        const u = store.getUserByUsername(username);
        if (!u || u.password !== password) throw new Error('Invalid username or password');
        const userData = { id: u.id, username: u.username, fullName: u.fullName, role: u.role, employeeId: u.employeeId, access: u.access || ALL_PAGES };
        localStorage.setItem('trikaar_user', JSON.stringify(userData));
        store.logAudit('USER_LOGIN', 'User', `${u.fullName} logged in`, u.fullName);
        setUser(userData);
        return userData;
    };

    const logout = useCallback(() => {
        if (user) store.logAudit('USER_LOGOUT', 'User', `${user.fullName} logged out`, user.fullName);
        localStorage.removeItem('trikaar_user');
        setUser(null);
    }, [user]);

    const hasAccess = useCallback((page) => {
        if (!user) return false;
        if (user.role === 'SUPER_ADMIN') return true;
        return (user.access || []).includes(page);
    }, [user]);

    return (
        <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user, hasAccess }}>
            {children}
        </AuthContext.Provider>
    );
}
