import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import AppLayout from './components/AppLayout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import EmployeesPage from './pages/EmployeesPage';
import AttendancePage from './pages/AttendancePage';
import CustomersPage from './pages/CustomersPage';
import InventoryPage from './pages/InventoryPage';
import SalesPage from './pages/SalesPage';
import SalaryPage from './pages/SalaryPage';
import LoyaltyPage from './pages/LoyaltyPage';
import ReportsPage from './pages/ReportsPage';
import ConfigPage from './pages/ConfigPage';
import AuditPage from './pages/AuditPage';
import SuppliersPage from './pages/SuppliersPage';
import PurchasesPage from './pages/PurchasesPage';
import ExpensesPage from './pages/ExpensesPage';
import CashRegisterPage from './pages/CashRegisterPage';

function Protected({ children }) { const { isAuthenticated } = useAuth(); return isAuthenticated ? children : <Navigate to="/login" replace />; }

function AppRoutes() {
  const { isAuthenticated } = useAuth();
  return (
    <Routes>
      <Route path="/login" element={isAuthenticated ? <Navigate to="/" replace /> : <LoginPage />} />
      <Route path="/" element={<Protected><AppLayout /></Protected>}>
        <Route index element={<DashboardPage />} />
        <Route path="employees" element={<EmployeesPage />} />
        <Route path="attendance" element={<AttendancePage />} />
        <Route path="customers" element={<CustomersPage />} />
        <Route path="inventory" element={<InventoryPage />} />
        <Route path="sales" element={<SalesPage />} />
        <Route path="salary" element={<SalaryPage />} />
        <Route path="loyalty" element={<LoyaltyPage />} />
        <Route path="reports" element={<ReportsPage />} />
        <Route path="config" element={<ConfigPage />} />
        <Route path="audit" element={<AuditPage />} />
        <Route path="suppliers" element={<SuppliersPage />} />
        <Route path="purchases" element={<PurchasesPage />} />
        <Route path="expenses" element={<ExpensesPage />} />
        <Route path="cash-register" element={<CashRegisterPage />} />
      </Route>
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ToastProvider>
          <AppRoutes />
        </ToastProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}
