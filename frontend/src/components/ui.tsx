import type { ButtonHTMLAttributes, ReactNode } from "react";

import { errorMessage } from "../api/client";
import type { Theme } from "../theme/ThemeContext";

export function Brand({ compact = false }: { compact?: boolean }) {
  return (
    <span className="inline-flex items-center gap-3">
      <span className="grid size-10 place-items-center rounded-xl bg-lime text-lg font-black text-forest shadow-glow">
        F
      </span>
      {!compact && (
        <span>
          <span className="block text-lg font-black tracking-tight">FinCore</span>
          <span className="block text-[10px] font-bold tracking-[0.18em] text-muted uppercase">
            Financial systems lab
          </span>
        </span>
      )}
    </span>
  );
}

export function Button({ className = "", ...props }: ButtonHTMLAttributes<HTMLButtonElement>) {
  return <button className={`button-primary ${className}`} {...props} />;
}

export function SecondaryButton({ className = "", ...props }: ButtonHTMLAttributes<HTMLButtonElement>) {
  return <button className={`button-secondary ${className}`} {...props} />;
}

export function Card({ children, className = "" }: { children: ReactNode; className?: string }) {
  return <section className={`card ${className}`}>{children}</section>;
}

export function PageHeader({
  eyebrow,
  title,
  description,
  action,
}: {
  eyebrow: string;
  title: string;
  description?: string;
  action?: ReactNode;
}) {
  return (
    <header className="page-header">
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
        {description && <p className="page-description">{description}</p>}
      </div>
      {action}
    </header>
  );
}

export function StatusBadge({ value }: { value: string }) {
  const positive = ["ACTIVE", "CONFIRMED", "SUCCESS", "BALANCED"].includes(value);
  const warning = ["SUSPENDED", "FAILURE", "DENIED", "MISMATCH"].includes(value);
  return (
    <span className={`status-badge ${positive ? "status-positive" : warning ? "status-warning" : ""}`}>
      {value}
    </span>
  );
}

export function LoadingState({ label = "Cargando datos…" }: { label?: string }) {
  return (
    <div className="state-panel" role="status">
      <span className="spinner" aria-hidden="true" />
      <p>{label}</p>
    </div>
  );
}

export function ErrorState({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  return (
    <div className="state-panel state-error" role="alert">
      <p className="font-black">No pudimos completar la solicitud</p>
      <p>{errorMessage(error)}</p>
      {onRetry && (
        <SecondaryButton type="button" onClick={onRetry}>
          Reintentar
        </SecondaryButton>
      )}
    </div>
  );
}

export function EmptyState({ title, detail }: { title: string; detail: string }) {
  return (
    <div className="state-panel">
      <p className="font-black">{title}</p>
      <p>{detail}</p>
    </div>
  );
}

export interface ThemeToggleProps {
  theme: Theme;
  onToggle: () => void;
}

export function ThemeToggle({ theme, onToggle }: ThemeToggleProps) {
  const isDark = theme === "dark";
  return (
    <button
      type="button"
      className="button-secondary theme-toggle"
      role="switch"
      aria-checked={isDark}
      aria-label={isDark ? "Cambiar a modo claro" : "Cambiar a modo oscuro"}
      onClick={onToggle}
    >
      {isDark ? "Modo oscuro" : "Modo claro"}
    </button>
  );
}

export function Pagination({
  page,
  totalPages,
  onChange,
}: {
  page: number;
  totalPages: number;
  onChange: (page: number) => void;
}) {
  if (totalPages <= 1) return null;
  return (
    <nav className="pagination" aria-label="Paginación">
      <SecondaryButton type="button" disabled={page === 0} onClick={() => onChange(page - 1)}>
        Anterior
      </SecondaryButton>
      <span>
        Página {page + 1} de {totalPages}
      </span>
      <SecondaryButton type="button" disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)}>
        Siguiente
      </SecondaryButton>
    </nav>
  );
}
