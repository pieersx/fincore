import { Link } from "react-router-dom";

import { Brand } from "../components/ui";

export function NotFoundPage() {
  return (
    <main className="not-found">
      <Brand />
      <p className="error-code">404</p>
      <h1>Esta ruta no existe</h1>
      <p>La dirección puede haber cambiado o no pertenecer a FinCore.</p>
      <Link className="button-primary" to="/app">Volver al resumen</Link>
    </main>
  );
}
