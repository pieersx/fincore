import { Navigate, Route, Routes } from "react-router-dom";

import { useAuth } from "./auth/AuthContext";
import { AppShell } from "./components/AppShell";
import { LoadingState } from "./components/ui";
import { AdminAccountsPage, AdminCustomersPage, AdminUsersPage } from "./pages/AdminPages";
import { LoginPage, RegisterPage } from "./pages/AuthPages";
import { AccountsPage, BeneficiariesPage, NewTransferPage, TransfersPage } from "./pages/CustomerPages";
import { DashboardPage } from "./pages/DashboardPage";
import { NotFoundPage } from "./pages/NotFoundPage";
import { AuditPage, OperationsPage, ReconciliationPage } from "./pages/OperationsPages";
import type { Role } from "./types/api";

function ProtectedRoute() {
  const { status } = useAuth();
  if (status === "loading") return <div className="full-page-state"><LoadingState label="Validando la sesión…" /></div>;
  return status === "authenticated" ? <AppShell /> : <Navigate to="/login" replace />;
}

function RoleRoute({ roles, children }: { roles: Role[]; children: React.ReactNode }) {
  const { hasRole } = useAuth();
  return hasRole(...roles) ? children : <Navigate to="/app" replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/app" element={<ProtectedRoute />}>
        <Route index element={<DashboardPage />} />
        <Route path="accounts" element={<RoleRoute roles={["CUSTOMER"]}><AccountsPage /></RoleRoute>} />
        <Route path="beneficiaries" element={<RoleRoute roles={["CUSTOMER"]}><BeneficiariesPage /></RoleRoute>} />
        <Route path="transfers" element={<RoleRoute roles={["CUSTOMER"]}><TransfersPage /></RoleRoute>} />
        <Route path="transfers/new" element={<RoleRoute roles={["CUSTOMER"]}><NewTransferPage /></RoleRoute>} />
        <Route path="operations" element={<RoleRoute roles={["ANALYST", "ADMIN"]}><OperationsPage /></RoleRoute>} />
        <Route path="reconciliation" element={<RoleRoute roles={["ANALYST", "ADMIN"]}><ReconciliationPage /></RoleRoute>} />
        <Route path="audit" element={<RoleRoute roles={["ANALYST", "ADMIN"]}><AuditPage /></RoleRoute>} />
        <Route path="admin/users" element={<RoleRoute roles={["ADMIN"]}><AdminUsersPage /></RoleRoute>} />
        <Route path="admin/customers" element={<RoleRoute roles={["ADMIN"]}><AdminCustomersPage /></RoleRoute>} />
        <Route path="admin/accounts" element={<RoleRoute roles={["ADMIN"]}><AdminAccountsPage /></RoleRoute>} />
      </Route>
      <Route path="/" element={<Navigate to="/app" replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
