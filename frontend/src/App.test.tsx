import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";

import App from "./App";
import { AuthProvider } from "./auth/AuthProvider";

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });
}

function renderApplication(initialPath: string) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[initialPath]}>
        <AuthProvider><App /></AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});

describe("rutas y sesión de FinCore", () => {
  it("redirige a login cuando no existe una sesión autenticada", async () => {
    const fetchMock = vi.fn<typeof fetch>(async (input) => {
      const url = String(input);
      if (url.endsWith("/auth/csrf")) return jsonResponse({ token: "csrf-guest", headerName: "X-CSRF-TOKEN", parameterName: "_csrf" });
      if (url.endsWith("/auth/me")) return jsonResponse({ title: "Unauthorized", status: 401 }, 401);
      throw new Error(`Solicitud inesperada: ${url}`);
    });
    vi.stubGlobal("fetch", fetchMock);

    renderApplication("/app");

    expect(await screen.findByRole("heading", { name: "Iniciar sesión" })).toBeInTheDocument();
    expect(screen.getByText("Accesos de demostración")).toBeInTheDocument();
  });

  it("autentica al cliente demo y carga su resumen desde la API", async () => {
    const fetchMock = vi.fn<typeof fetch>(async (input, init) => {
      const url = String(input);
      if (url.endsWith("/auth/csrf")) return jsonResponse({ token: "csrf-demo", headerName: "X-CSRF-TOKEN", parameterName: "_csrf" });
      if (url.endsWith("/auth/login") && init?.method === "POST") return new Response(null, { status: 204 });
      if (url.endsWith("/auth/me")) {
        // La primera consulta inicia como invitado; la segunda ocurre después del login.
        const meCalls = fetchMock.mock.calls.filter(([requested]) => String(requested).endsWith("/auth/me"));
        if (meCalls.length === 1) return jsonResponse({ title: "Unauthorized", status: 401 }, 401);
        return jsonResponse({ id: "user-1", username: "customer.one", roles: ["CUSTOMER"] });
      }
      if (url.endsWith("/customers/me")) return jsonResponse({ id: "customer-1", userId: "user-1", displayName: "Cliente Uno", status: "ACTIVE", createdAt: "2026-08-20T10:00:00Z", updatedAt: "2026-08-20T10:00:00Z" });
      if (url.endsWith("/accounts")) return jsonResponse([{ id: "account-1", customerId: "customer-1", accountNumber: "FCPEN0000000001", kind: "CUSTOMER", currency: "PEN", status: "ACTIVE", balance: 4750, createdAt: "2026-08-20T10:00:00Z", updatedAt: "2026-08-20T10:00:00Z" }]);
      if (url.includes("/transfers?page=0&size=5")) return jsonResponse({ content: [], page: 0, size: 5, totalElements: 0, totalPages: 0 });
      throw new Error(`Solicitud inesperada: ${url}`);
    });
    vi.stubGlobal("fetch", fetchMock);

    renderApplication("/login");
    await screen.findByRole("heading", { name: "Iniciar sesión" });
    fireEvent.click(screen.getByRole("button", { name: "Cliente" }));
    fireEvent.click(screen.getByRole("button", { name: "Ingresar" }));

    expect(await screen.findByRole("heading", { name: "Hola, Cliente Uno" })).toBeInTheDocument();
    expect(screen.getByText(/4,750\.00/)).toBeInTheDocument();
    await waitFor(() => expect(fetchMock).toHaveBeenCalledWith("/api/v1/auth/login", expect.objectContaining({ method: "POST" })));
  });
});
