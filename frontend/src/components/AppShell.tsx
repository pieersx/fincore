import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";

import { useAuth } from "../auth/AuthContext";
import type { Role } from "../types/api";
import { Brand } from "./ui";

interface NavItem {
  to: string;
  label: string;
  roles: Role[];
}

const navigation: NavItem[] = [
  { to: "/app", label: "Resumen", roles: ["CUSTOMER", "ANALYST", "ADMIN"] },
  { to: "/app/accounts", label: "Cuentas", roles: ["CUSTOMER"] },
  { to: "/app/beneficiaries", label: "Beneficiarios", roles: ["CUSTOMER"] },
  { to: "/app/transfers", label: "Transferencias", roles: ["CUSTOMER"] },
  { to: "/app/operations", label: "Operaciones", roles: ["ANALYST", "ADMIN"] },
  { to: "/app/reconciliation", label: "Conciliación", roles: ["ANALYST", "ADMIN"] },
  { to: "/app/audit", label: "Auditoría", roles: ["ANALYST", "ADMIN"] },
  { to: "/app/admin/users", label: "Usuarios", roles: ["ADMIN"] },
  { to: "/app/admin/customers", label: "Clientes", roles: ["ADMIN"] },
  { to: "/app/admin/accounts", label: "Todas las cuentas", roles: ["ADMIN"] },
];

export function AppShell() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);
  const [loggingOut, setLoggingOut] = useState(false);

  const items = navigation.filter((item) => item.roles.some((role) => user?.roles.includes(role)));

  async function handleLogout() {
    setLoggingOut(true);
    try {
      await logout();
      navigate("/login", { replace: true });
    } finally {
      setLoggingOut(false);
    }
  }

  return (
    <div className="app-frame">
      <aside className={`sidebar ${menuOpen ? "sidebar-open" : ""}`}>
        <div className="sidebar-brand">
          <Brand />
          <button className="sidebar-close" type="button" onClick={() => setMenuOpen(false)} aria-label="Cerrar menú">
            ×
          </button>
        </div>
        <nav className="sidebar-nav" aria-label="Navegación principal">
          {items.map((item) => (
            <NavLink
              end={item.to === "/app"}
              key={item.to}
              to={item.to}
              onClick={() => setMenuOpen(false)}
              className={({ isActive }) => (isActive ? "nav-link nav-link-active" : "nav-link")}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-user">
          <span className="avatar">{user?.username.slice(0, 2).toUpperCase()}</span>
          <div className="min-w-0 flex-1">
            <p className="truncate font-black">{user?.username}</p>
            <p className="truncate text-xs text-sidebar-muted">{user?.roles.join(" · ")}</p>
          </div>
          <button className="logout-button" type="button" disabled={loggingOut} onClick={() => void handleLogout()}>
            {loggingOut ? "…" : "Salir"}
          </button>
        </div>
      </aside>

      {menuOpen && <button className="sidebar-backdrop" aria-label="Cerrar menú" onClick={() => setMenuOpen(false)} />}

      <div className="app-content">
        <header className="mobile-header">
          <button type="button" className="menu-button" onClick={() => setMenuOpen(true)} aria-label="Abrir menú">
            ☰
          </button>
          <Brand compact />
          <span className="mobile-location">{items.find((item) => item.to === location.pathname)?.label ?? "FinCore"}</span>
        </header>
        <main className="content-container">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
