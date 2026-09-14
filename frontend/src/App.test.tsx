import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it, vi } from "vitest";

import App from "./App";
import { AuthProvider } from "./auth/AuthProvider";
import { ThemeProvider } from "./theme/ThemeProvider";

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });
}

function stubMatchMedia(prefersDark: boolean) {
  vi.stubGlobal(
    "matchMedia",
    vi.fn().mockImplementation((query: string) => ({
      matches: query === "(prefers-color-scheme: dark)" ? prefersDark : false,
      media: query,
      onchange: null,
      addListener: vi.fn(),
      removeListener: vi.fn(),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn(),
    })),
  );
}

function renderApplication(initialPath: string) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={[initialPath]}>
        <AuthProvider>
          <ThemeProvider>
            <App />
          </ThemeProvider>
        </AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
  localStorage.clear();
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
    stubMatchMedia(false);

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
    stubMatchMedia(false);

    renderApplication("/login");
    await screen.findByRole("heading", { name: "Iniciar sesión" });
    fireEvent.click(screen.getByRole("button", { name: "Cliente" }));
    fireEvent.click(screen.getByRole("button", { name: "Ingresar" }));

    expect(await screen.findByRole("heading", { name: "Hola, Cliente Uno" })).toBeInTheDocument();
    expect(screen.getByText(/4,750\.00/)).toBeInTheDocument();
    await waitFor(() => expect(fetchMock).toHaveBeenCalledWith("/api/v1/auth/login", expect.objectContaining({ method: "POST" })));
  });

  it("clicking the sidebar theme control switches theme immediately, without navigation or reload", async () => {
    const fetchMock = vi.fn<typeof fetch>(async (input, init) => {
      const url = String(input);
      if (url.endsWith("/auth/csrf")) return jsonResponse({ token: "csrf-demo", headerName: "X-CSRF-TOKEN", parameterName: "_csrf" });
      if (url.endsWith("/auth/login") && init?.method === "POST") return new Response(null, { status: 204 });
      if (url.endsWith("/auth/me")) {
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
    stubMatchMedia(false);

    renderApplication("/login");
    await screen.findByRole("heading", { name: "Iniciar sesión" });
    fireEvent.click(screen.getByRole("button", { name: "Cliente" }));
    fireEvent.click(screen.getByRole("button", { name: "Ingresar" }));

    await screen.findByRole("heading", { name: "Hola, Cliente Uno" });

    const themeSwitch = screen.getByRole("switch");
    const appFrame = document.querySelector(".app-frame");
    expect(appFrame).not.toBeNull();
    expect(appFrame).not.toHaveAttribute("data-theme", "dark");

    fireEvent.click(themeSwitch);

    expect(appFrame).toHaveAttribute("data-theme", "dark");
    expect(themeSwitch).toHaveAttribute("aria-checked", "true");

    fireEvent.click(themeSwitch);

    expect(appFrame).not.toHaveAttribute("data-theme", "dark");
    expect(themeSwitch).toHaveAttribute("aria-checked", "false");
  });

  it("theme toggle is visible and functional when the mobile menu is open", async () => {
    const fetchMock = vi.fn<typeof fetch>(async (input, init) => {
      const url = String(input);
      if (url.endsWith("/auth/csrf")) return jsonResponse({ token: "csrf-demo", headerName: "X-CSRF-TOKEN", parameterName: "_csrf" });
      if (url.endsWith("/auth/login") && init?.method === "POST") return new Response(null, { status: 204 });
      if (url.endsWith("/auth/me")) {
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
    stubMatchMedia(false);

    renderApplication("/login");
    await screen.findByRole("heading", { name: "Iniciar sesión" });
    fireEvent.click(screen.getByRole("button", { name: "Cliente" }));
    fireEvent.click(screen.getByRole("button", { name: "Ingresar" }));

    await screen.findByRole("heading", { name: "Hola, Cliente Uno" });

    // Open the mobile menu via the hamburger trigger in the mobile header, which
    // sets the same `menuOpen` state that adds the `sidebar-open` class to the
    // single shared <aside>. There is only ever one ThemeToggle in the DOM
    // (no separate desktop/mobile copies), so proving it works here proves
    // parity by construction rather than by asserting a jsdom-unobservable
    // CSS media query.
    fireEvent.click(screen.getByRole("button", { name: "Abrir menú" }));

    const sidebar = document.querySelector(".sidebar");
    expect(sidebar).toHaveClass("sidebar-open");

    const themeSwitch = screen.getByRole("switch");
    expect(sidebar).toContainElement(themeSwitch);
    expect(themeSwitch).toBeVisible();

    const appFrame = document.querySelector(".app-frame");
    expect(appFrame).not.toHaveAttribute("data-theme", "dark");

    fireEvent.click(themeSwitch);

    expect(appFrame).toHaveAttribute("data-theme", "dark");
    expect(themeSwitch).toHaveAttribute("aria-checked", "true");
  });

  it("login page renders with light styling regardless of a stored dark theme preference", async () => {
    localStorage.setItem("fincore-theme", "dark");
    const fetchMock = vi.fn<typeof fetch>(async (input) => {
      const url = String(input);
      if (url.endsWith("/auth/csrf")) return jsonResponse({ token: "csrf-guest", headerName: "X-CSRF-TOKEN", parameterName: "_csrf" });
      if (url.endsWith("/auth/me")) return jsonResponse({ title: "Unauthorized", status: 401 }, 401);
      throw new Error(`Solicitud inesperada: ${url}`);
    });
    vi.stubGlobal("fetch", fetchMock);
    stubMatchMedia(false);

    renderApplication("/login");

    expect(await screen.findByRole("heading", { name: "Iniciar sesión" })).toBeInTheDocument();
    expect(document.querySelector(".app-frame")).toBeNull();
  });
});
