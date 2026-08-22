import { useEffect, useState } from "react";

import { getBackendHealth } from "./api/health";

type ConnectionState = "checking" | "available" | "unavailable";

const modules = [
  {
    name: "Identity",
    description: "Sesiones, roles y propiedad de recursos.",
    state: "Siguiente incremento",
  },
  {
    name: "Accounts",
    description: "Cuentas sintéticas en PEN y USD.",
    state: "Planificado",
  },
  {
    name: "Ledger",
    description: "Partida doble, append-only y conciliación.",
    state: "Planificado",
  },
];

const statusCopy: Record<ConnectionState, { label: string; detail: string }> = {
  checking: {
    label: "Verificando backend",
    detail: "Consultando el health check de Spring Boot…",
  },
  available: {
    label: "Backend conectado",
    detail: "Spring Boot y PostgreSQL responden correctamente.",
  },
  unavailable: {
    label: "Backend no disponible",
    detail: "Inicia PostgreSQL y Spring Boot para completar la conexión.",
  },
};

function FinCoreMark() {
  return (
    <span className="grid size-10 place-items-center rounded-xl bg-[#d8ff72] text-lg font-black text-[#0b2f2a] shadow-[0_8px_24px_rgba(216,255,114,0.18)]">
      F
    </span>
  );
}

export default function App() {
  const [connectionState, setConnectionState] = useState<ConnectionState>("checking");
  const [healthRequest, setHealthRequest] = useState(0);

  useEffect(() => {
    const controller = new AbortController();

    void getBackendHealth(controller.signal)
      .then(({ status }) => {
        setConnectionState(status === "UP" ? "available" : "unavailable");
      })
      .catch((error: unknown) => {
        if (!(error instanceof DOMException && error.name === "AbortError")) {
          setConnectionState("unavailable");
        }
      });

    return () => controller.abort();
  }, [healthRequest]);

  const currentStatus = statusCopy[connectionState];

  return (
    <div className="min-h-screen bg-[#f4f1e8] text-[#102a27]">
      <header className="border-b border-[#153c36]/10 bg-[#f4f1e8]/90 backdrop-blur">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-5 py-5 sm:px-8 lg:px-10">
          <a className="flex items-center gap-3" href="#top" aria-label="Ir al inicio de FinCore">
            <FinCoreMark />
            <span>
              <span className="block text-lg font-black tracking-[-0.04em]">FinCore</span>
              <span className="block text-[10px] font-bold tracking-[0.18em] text-[#47645f] uppercase">
                Financial systems lab
              </span>
            </span>
          </a>
          <span className="rounded-full border border-[#153c36]/15 px-3 py-1.5 text-xs font-bold text-[#47645f]">
            Datos 100% sintéticos
          </span>
        </div>
      </header>

      <main id="top">
        <section className="relative overflow-hidden bg-[#0b2f2a] text-white">
          <div className="signal-grid absolute inset-0 opacity-30" aria-hidden="true" />
          <div className="relative mx-auto grid max-w-7xl gap-12 px-5 py-16 sm:px-8 sm:py-20 lg:grid-cols-[1.15fr_0.85fr] lg:px-10 lg:py-24">
            <div className="max-w-3xl">
              <p className="mb-6 inline-flex items-center gap-2 rounded-full border border-[#d8ff72]/25 bg-[#d8ff72]/10 px-3 py-1.5 text-xs font-bold tracking-[0.14em] text-[#d8ff72] uppercase">
                <span className="size-1.5 rounded-full bg-[#d8ff72]" />
                Walking skeleton operativo
              </p>
              <h1 className="max-w-3xl text-4xl leading-[1.02] font-black tracking-[-0.055em] text-balance sm:text-6xl lg:text-7xl">
                Un core financiero que hace visibles sus garantías.
              </h1>
              <p className="mt-7 max-w-2xl text-base leading-7 text-[#c5d5d1] sm:text-lg">
                FinCore demuestra consistencia transaccional, trazabilidad y evolución
                arquitectónica con un sistema que puede probarse de extremo a extremo.
              </p>
              <div className="mt-9 flex flex-wrap gap-3 text-sm font-bold">
                <span className="rounded-lg bg-white px-4 py-2.5 text-[#0b2f2a]">Spring Boot 4.1</span>
                <span className="rounded-lg border border-white/15 px-4 py-2.5 text-white">React 19</span>
                <span className="rounded-lg border border-white/15 px-4 py-2.5 text-white">PostgreSQL 18</span>
              </div>
            </div>

            <aside className="self-end rounded-3xl border border-white/10 bg-white/[0.07] p-6 shadow-2xl shadow-black/20 backdrop-blur-sm sm:p-8">
              <div className="flex items-start justify-between gap-6">
                <div>
                  <p className="text-xs font-bold tracking-[0.16em] text-[#93aaa5] uppercase">Estado del sistema</p>
                  <h2 className="mt-2 text-2xl font-black tracking-[-0.035em]">Conexión en tiempo real</h2>
                </div>
                <span
                  className={`mt-1 size-3 rounded-full ${
                    connectionState === "available"
                      ? "bg-[#d8ff72] shadow-[0_0_0_6px_rgba(216,255,114,0.12)]"
                      : connectionState === "checking"
                        ? "animate-pulse bg-[#ffc857]"
                        : "bg-[#ff8d80]"
                  }`}
                  aria-hidden="true"
                />
              </div>

              <div className="mt-8 border-t border-white/10 pt-6" role="status" aria-live="polite">
                <p className="font-extrabold text-white">{currentStatus.label}</p>
                <p className="mt-2 text-sm leading-6 text-[#b7cac5]">{currentStatus.detail}</p>
              </div>

              <dl className="mt-7 grid grid-cols-2 gap-3">
                <div className="rounded-2xl bg-black/10 p-4">
                  <dt className="text-xs font-bold text-[#93aaa5]">Esquema</dt>
                  <dd className="mt-1 text-lg font-black">Flyway managed</dd>
                </div>
                <div className="rounded-2xl bg-black/10 p-4">
                  <dt className="text-xs font-bold text-[#93aaa5]">Integración</dt>
                  <dd className="mt-1 text-lg font-black">JUnit + TC</dd>
                </div>
              </dl>

              {connectionState === "unavailable" && (
                <button
                  className="mt-6 w-full rounded-xl bg-[#d8ff72] px-4 py-3 text-sm font-black text-[#0b2f2a] transition hover:bg-white focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#d8ff72]"
                  type="button"
                  onClick={() => {
                    setConnectionState("checking");
                    setHealthRequest((request) => request + 1);
                  }}
                >
                  Reintentar conexión
                </button>
              )}
            </aside>
          </div>
        </section>

        <section className="mx-auto max-w-7xl px-5 py-16 sm:px-8 lg:px-10 lg:py-20">
          <div className="flex flex-col justify-between gap-5 border-b border-[#153c36]/15 pb-8 md:flex-row md:items-end">
            <div>
              <p className="text-xs font-black tracking-[0.15em] text-[#55706b] uppercase">Mapa de capacidades</p>
              <h2 className="mt-3 text-3xl font-black tracking-[-0.04em] sm:text-4xl">Construido por dominios, no por capas.</h2>
            </div>
            <p className="max-w-md text-sm leading-6 text-[#55706b]">
              Cada funcionalidad agrupa sus controladores, servicios, repositorios, entidades y DTO.
            </p>
          </div>

          <div className="mt-8 grid gap-4 md:grid-cols-3">
            {modules.map((module, index) => (
              <article
                className="group rounded-2xl border border-[#153c36]/12 bg-[#faf8f2] p-6 transition hover:-translate-y-1 hover:border-[#153c36]/30 hover:shadow-xl hover:shadow-[#153c36]/8"
                key={module.name}
              >
                <div className="flex items-center justify-between">
                  <span className="font-mono text-xs font-bold text-[#6d827e]">0{index + 1}</span>
                  <span className="rounded-full bg-[#e6e3d9] px-2.5 py-1 text-[10px] font-black tracking-[0.08em] text-[#55706b] uppercase">
                    {module.state}
                  </span>
                </div>
                <h3 className="mt-10 text-2xl font-black tracking-[-0.04em]">{module.name}</h3>
                <p className="mt-3 text-sm leading-6 text-[#55706b]">{module.description}</p>
              </article>
            ))}
          </div>
        </section>
      </main>

      <footer className="border-t border-[#153c36]/10 px-5 py-8 text-center text-xs font-semibold text-[#667b77]">
        FinCore es un simulador educativo. No procesa dinero ni información financiera real.
      </footer>
    </div>
  );
}
