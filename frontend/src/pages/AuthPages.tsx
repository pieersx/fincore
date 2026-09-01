import { useState } from "react";
import type { FormEvent } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";

import { errorMessage } from "../api/client";
import { registerCustomer } from "../api/endpoints";
import { useAuth } from "../auth/AuthContext";
import { Brand, Button, SecondaryButton } from "../components/ui";

const demoUsers = [
  { label: "Cliente", username: "customer.one" },
  { label: "Analista", username: "analyst.demo" },
  { label: "Administrador", username: "admin.demo" },
];

function AuthLayout({ children }: { children: React.ReactNode }) {
  return (
    <main className="auth-layout">
      <section className="auth-story">
        <Brand />
        <div>
          <p className="eyebrow text-lime">Simulador financiero educativo</p>
          <h1>Opera, controla y audita un core financiero de extremo a extremo.</h1>
          <p>
            Todos los usuarios, saldos y movimientos son sintéticos. FinCore no procesa dinero real.
          </p>
        </div>
        <p className="auth-footnote">React · Spring Boot · PostgreSQL · Docker</p>
      </section>
      <section className="auth-panel">{children}</section>
    </main>
  );
}

export function LoginPage() {
  const { login, status } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (status === "authenticated") return <Navigate to="/app" replace />;

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await login(username.trim(), password);
      navigate("/app", { replace: true });
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthLayout>
      <div className="auth-card">
        <p className="eyebrow">Acceso seguro</p>
        <h2>Iniciar sesión</h2>
        <p className="form-intro">Ingresa con una cuenta demo o con el cliente que registraste.</p>

        {typeof location.state === "object" && location.state !== null && "message" in location.state && (
          <div className="notice notice-success" role="status">{String(location.state.message)}</div>
        )}
        {error && <div className="notice notice-error" role="alert">{error}</div>}

        <form className="form-stack" onSubmit={(event) => void handleSubmit(event)}>
          <label>
            Usuario
            <input autoComplete="username" required value={username} onChange={(event) => setUsername(event.target.value)} />
          </label>
          <label>
            Contraseña
            <input
              autoComplete="current-password"
              minLength={12}
              required
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
            />
          </label>
          <Button disabled={submitting || status === "loading"} type="submit">
            {submitting ? "Ingresando…" : "Ingresar"}
          </Button>
        </form>

        <div className="demo-access">
          <p>Accesos de demostración</p>
          <div className="demo-buttons">
            {demoUsers.map((demo) => (
              <SecondaryButton
                key={demo.username}
                type="button"
                onClick={() => {
                  setUsername(demo.username);
                  setPassword("FincoreDemo!2026");
                }}
              >
                {demo.label}
              </SecondaryButton>
            ))}
          </div>
        </div>

        <p className="auth-switch">¿Necesitas una cuenta cliente? <Link to="/register">Crear cuenta</Link></p>
      </div>
    </AuthLayout>
  );
}

export function RegisterPage() {
  const { status } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: "", displayName: "", password: "" });
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  if (status === "authenticated") return <Navigate to="/app" replace />;

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      await registerCustomer({
        username: form.username.trim(),
        displayName: form.displayName.trim(),
        password: form.password,
      });
      navigate("/login", {
        replace: true,
        state: { message: "Cuenta creada. Ya puedes iniciar sesión." },
      });
    } catch (requestError) {
      setError(errorMessage(requestError));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <AuthLayout>
      <div className="auth-card">
        <p className="eyebrow">Alta de cliente</p>
        <h2>Crear cuenta sintética</h2>
        <p className="form-intro">El backend creará el perfil y sus cuentas iniciales en PEN y USD.</p>
        {error && <div className="notice notice-error" role="alert">{error}</div>}
        <form className="form-stack" onSubmit={(event) => void handleSubmit(event)}>
          <label>
            Nombre visible
            <input required maxLength={100} value={form.displayName} onChange={(event) => setForm({ ...form, displayName: event.target.value })} />
          </label>
          <label>
            Usuario
            <input autoComplete="username" required minLength={4} maxLength={50} value={form.username} onChange={(event) => setForm({ ...form, username: event.target.value })} />
          </label>
          <label>
            Contraseña
            <input autoComplete="new-password" required minLength={12} type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} />
            <span className="field-hint">Mínimo 12 caracteres.</span>
          </label>
          <Button disabled={submitting} type="submit">{submitting ? "Creando…" : "Crear cuenta"}</Button>
        </form>
        <p className="auth-switch">¿Ya tienes una cuenta? <Link to="/login">Iniciar sesión</Link></p>
      </div>
    </AuthLayout>
  );
}
