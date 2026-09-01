export type Role = "CUSTOMER" | "ANALYST" | "ADMIN";
export type Currency = "PEN" | "USD";
export type EntityStatus = "ACTIVE" | "SUSPENDED";

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  errors?: Record<string, string>;
}

export interface CsrfResponse {
  token: string;
  headerName: string;
  parameterName: string;
}

export interface SessionUser {
  id: string;
  username: string;
  roles: Role[];
}

export interface UserView {
  id: string;
  username: string;
  status: EntityStatus;
  roles: Role[];
  createdAt: string;
  updatedAt: string;
}

export interface CustomerProfile {
  id: string;
  userId: string;
  displayName: string;
  status: EntityStatus;
  createdAt: string;
  updatedAt: string;
}

export interface Account {
  id: string;
  customerId: string;
  accountNumber: string;
  kind: "CUSTOMER" | "SYSTEM";
  currency: Currency;
  status: EntityStatus;
  balance: number;
  createdAt: string;
  updatedAt: string;
}

export interface LedgerMovement {
  entryId: string;
  journalId: string;
  referenceType: "OPENING_BALANCE" | "TRANSFER";
  referenceId: string;
  type: "DEBIT" | "CREDIT";
  amount: number;
  currency: Currency;
  description: string;
  occurredAt: string;
}

export interface Beneficiary {
  id: string;
  destinationAccountId: string;
  destinationAccountNumber: string;
  currency: Currency;
  alias: string;
  createdAt: string;
}

export interface Transfer {
  id: string;
  reference: string;
  createdByUserId: string;
  sourceAccountId: string;
  sourceAccountNumber: string;
  destinationAccountId: string;
  destinationAccountNumber: string;
  beneficiaryId: string;
  currency: Currency;
  amount: number;
  status: "CONFIRMED";
  description: string | null;
  createdAt: string;
  completedAt: string;
}

export interface AuditEvent {
  id: string;
  actorUsername: string;
  action: string;
  outcome: "SUCCESS" | "FAILURE" | "DENIED";
  resourceType: string;
  resourceId: string | null;
  correlationId: string;
  detail: string | null;
  occurredAt: string;
}

export interface ReconciliationItem {
  accountId: string;
  accountNumber: string;
  kind: "CUSTOMER" | "SYSTEM";
  currency: Currency;
  storedBalance: number;
  ledgerBalance: number;
  difference: number;
  balanced: boolean;
}

export interface Reconciliation {
  generatedAt: string;
  balanced: boolean;
  mismatchCount: number;
  accounts: ReconciliationItem[];
}

export interface RegistrationResponse {
  user: UserView;
  customer: CustomerProfile;
  accounts: Account[];
}
