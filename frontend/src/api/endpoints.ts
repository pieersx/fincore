import { apiBlob, apiRequest, clearCsrf, refreshCsrf } from "./client";
import type {
  Account,
  AuditEvent,
  Beneficiary,
  CustomerProfile,
  LedgerMovement,
  PageResponse,
  Reconciliation,
  RegistrationResponse,
  SessionUser,
  Transfer,
  UserView,
} from "../types/api";

function pageQuery(page: number, size = 20): string {
  return `?page=${page}&size=${size}`;
}

export async function getSession(signal?: AbortSignal): Promise<SessionUser> {
  return apiRequest<SessionUser>("/auth/me", { signal });
}

export async function login(username: string, password: string): Promise<SessionUser> {
  const form = new URLSearchParams({ username, password });
  await apiRequest<void>("/auth/login", {
    method: "POST",
    body: form,
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
  });
  // Spring migra la sesión al autenticar; el token anterior deja de ser confiable.
  clearCsrf();
  await refreshCsrf();
  return getSession();
}

export async function logout(): Promise<void> {
  await apiRequest<void>("/auth/logout", { method: "POST" });
  clearCsrf();
}

export function registerCustomer(input: {
  username: string;
  password: string;
  displayName: string;
}): Promise<RegistrationResponse> {
  return apiRequest<RegistrationResponse>("/auth/register", { method: "POST", body: input });
}

export const customerApi = {
  profile: () => apiRequest<CustomerProfile>("/customers/me"),
  accounts: () => apiRequest<Account[]>("/accounts"),
  movements: (accountId: string, page: number) =>
    apiRequest<PageResponse<LedgerMovement>>(`/accounts/${accountId}/movements${pageQuery(page)}`),
  beneficiaries: () => apiRequest<Beneficiary[]>("/beneficiaries"),
  createBeneficiary: (input: { destinationAccountNumber: string; alias: string }) =>
    apiRequest<Beneficiary>("/beneficiaries", { method: "POST", body: input }),
  deleteBeneficiary: (id: string) =>
    apiRequest<void>(`/beneficiaries/${id}`, { method: "DELETE" }),
  transfers: (page: number, size = 20) =>
    apiRequest<PageResponse<Transfer>>(`/transfers${pageQuery(page, size)}`),
  transfer: (id: string) => apiRequest<Transfer>(`/transfers/${id}`),
  createTransfer: (
    input: { sourceAccountId: string; beneficiaryId: string; amount: number; description: string },
    idempotencyKey: string,
  ) =>
    apiRequest<Transfer>("/transfers", {
      method: "POST",
      body: input,
      headers: { "Idempotency-Key": idempotencyKey },
    }),
  receipt: (id: string) => apiBlob(`/transfers/${id}/receipt`),
};

export const operationsApi = {
  transfers: (page: number, size = 20) =>
    apiRequest<PageResponse<Transfer>>(`/operations/transfers${pageQuery(page, size)}`),
  transfer: (id: string) => apiRequest<Transfer>(`/operations/transfers/${id}`),
  reconciliation: () => apiRequest<Reconciliation>("/operations/reconciliation"),
  audit: (page: number, size = 20) =>
    apiRequest<PageResponse<AuditEvent>>(`/audit-events${pageQuery(page, size)}`),
};

export const adminApi = {
  users: (page: number) => apiRequest<PageResponse<UserView>>(`/admin/users${pageQuery(page)}`),
  updateUser: (id: string, status: "ACTIVE" | "SUSPENDED") =>
    apiRequest<UserView>(`/admin/users/${id}/status`, { method: "PATCH", body: { status } }),
  customers: (page: number) =>
    apiRequest<PageResponse<CustomerProfile>>(`/admin/customers${pageQuery(page)}`),
  updateCustomer: (id: string, status: "ACTIVE" | "SUSPENDED") =>
    apiRequest<CustomerProfile>(`/admin/customers/${id}/status`, {
      method: "PATCH",
      body: { status },
    }),
  accounts: (page: number) => apiRequest<PageResponse<Account>>(`/admin/accounts${pageQuery(page)}`),
  updateAccount: (id: string, status: "ACTIVE" | "SUSPENDED") =>
    apiRequest<Account>(`/admin/accounts/${id}/status`, { method: "PATCH", body: { status } }),
};
