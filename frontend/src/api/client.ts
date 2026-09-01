import type { CsrfResponse, ProblemDetail } from "../types/api";

const API_PREFIX = "/api/v1";
const UNSAFE_METHODS = new Set(["POST", "PUT", "PATCH", "DELETE"]);
export const UNAUTHORIZED_EVENT = "fincore:unauthorized";

let csrf: CsrfResponse | null = null;

export class ApiError extends Error {
  readonly status: number;
  readonly problem: ProblemDetail;

  constructor(status: number, problem: ProblemDetail) {
    super(problem.detail ?? problem.title ?? `La solicitud falló con HTTP ${status}`);
    this.name = "ApiError";
    this.status = status;
    this.problem = problem;
  }
}

interface ApiRequestOptions {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  headers?: Record<string, string>;
  signal?: AbortSignal;
  retryCsrf?: boolean;
}

function isBodyInit(value: unknown): value is BodyInit {
  return (
    typeof value === "string" ||
    value instanceof URLSearchParams ||
    value instanceof FormData ||
    value instanceof Blob ||
    value instanceof ArrayBuffer
  );
}

async function parseProblem(response: Response): Promise<ProblemDetail> {
  try {
    const body: unknown = await response.json();
    if (typeof body === "object" && body !== null) {
      return body as ProblemDetail;
    }
  } catch {
    // Una respuesta sin JSON conserva un mensaje HTTP entendible para el usuario.
  }
  return { status: response.status, title: response.statusText };
}

function notifyExpiredSession(status: number): void {
  if (status === 401 && typeof window !== "undefined") {
    window.dispatchEvent(new Event(UNAUTHORIZED_EVENT));
  }
}

/** Obtiene el token ligado a la cookie de sesión y lo conserva solo en memoria. */
export async function refreshCsrf(signal?: AbortSignal): Promise<CsrfResponse> {
  const response = await fetch(`${API_PREFIX}/auth/csrf`, {
    credentials: "include",
    headers: { Accept: "application/json" },
    signal,
  });
  if (!response.ok) {
    // Avisa al proveedor de sesión para abandonar pantallas privadas si la cookie expiró.
    notifyExpiredSession(response.status);
    throw new ApiError(response.status, await parseProblem(response));
  }
  csrf = (await response.json()) as CsrfResponse;
  return csrf;
}

export function clearCsrf(): void {
  csrf = null;
}

/**
 * Centraliza cookies, CSRF, correlación y Problem Details para que las páginas
 * no repitan reglas de infraestructura.
 */
export async function apiRequest<T>(path: string, options: ApiRequestOptions = {}): Promise<T> {
  const method = options.method ?? "GET";
  const headers = new Headers(options.headers);
  headers.set("Accept", "application/json");
  headers.set("X-Correlation-ID", crypto.randomUUID());

  if (UNSAFE_METHODS.has(method)) {
    const token = csrf ?? (await refreshCsrf(options.signal));
    headers.set(token.headerName, token.token);
  }

  let body: BodyInit | undefined;
  if (options.body !== undefined) {
    if (isBodyInit(options.body)) {
      body = options.body;
    } else {
      headers.set("Content-Type", "application/json");
      body = JSON.stringify(options.body);
    }
  }

  const response = await fetch(`${API_PREFIX}${path}`, {
    method,
    body,
    credentials: "include",
    headers,
    signal: options.signal,
  });

  if (response.status === 403 && UNSAFE_METHODS.has(method) && options.retryCsrf !== false) {
    await refreshCsrf(options.signal);
    return apiRequest<T>(path, { ...options, retryCsrf: false });
  }

  if (!response.ok) {
    throw new ApiError(response.status, await parseProblem(response));
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
}

/** Descarga binarios autenticados, por ejemplo el comprobante PDF. */
export async function apiBlob(path: string): Promise<Blob> {
  const response = await fetch(`${API_PREFIX}${path}`, {
    credentials: "include",
    headers: {
      Accept: "application/pdf",
      "X-Correlation-ID": crypto.randomUUID(),
    },
  });
  if (!response.ok) {
    notifyExpiredSession(response.status);
    throw new ApiError(response.status, await parseProblem(response));
  }
  return response.blob();
}

export function errorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    const fields = error.problem.errors ? Object.values(error.problem.errors) : [];
    return fields.length > 0 ? fields.join(" · ") : error.message;
  }
  return error instanceof Error ? error.message : "Ocurrió un error inesperado.";
}
