import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";

import { adminApi } from "../api/endpoints";
import { Card, EmptyState, ErrorState, LoadingState, PageHeader, Pagination, StatusBadge } from "../components/ui";
import type { EntityStatus } from "../types/api";
import { formatDate, formatMoney, maskAccount, shortId } from "../utils/format";

function nextStatus(status: EntityStatus): EntityStatus {
  return status === "ACTIVE" ? "SUSPENDED" : "ACTIVE";
}

function StatusButton({ status, pending, onClick }: { status: EntityStatus; pending: boolean; onClick: () => void }) {
  return <button className="table-action" disabled={pending} type="button" onClick={onClick}>{status === "ACTIVE" ? "Suspender" : "Activar"}</button>;
}

export function AdminUsersPage() {
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();
  const users = useQuery({ queryKey: ["admin", "users", page], queryFn: () => adminApi.users(page) });
  const update = useMutation({ mutationFn: ({ id, status }: { id: string; status: EntityStatus }) => adminApi.updateUser(id, status), onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin", "users"] }) });
  return (
    <AdminPage title="Usuarios" description="Administra el acceso lógico y los roles asignados." loading={users.isLoading} error={users.error ?? update.error} retry={() => void users.refetch()} empty={users.data?.content.length === 0}>
      {users.data && <><div className="data-table-wrap"><table className="data-table"><thead><tr><th>Usuario</th><th>Roles</th><th>Alta</th><th>Estado</th><th /></tr></thead><tbody>{users.data.content.map((user) => <tr key={user.id}><td><strong>{user.username}</strong><br /><span className="secondary-cell">{shortId(user.id)}</span></td><td>{user.roles.join(", ")}</td><td>{formatDate(user.createdAt)}</td><td><StatusBadge value={user.status} /></td><td className="text-right"><StatusButton status={user.status} pending={update.isPending} onClick={() => update.mutate({ id: user.id, status: nextStatus(user.status) })} /></td></tr>)}</tbody></table></div><Pagination page={page} totalPages={users.data.totalPages} onChange={setPage} /></>}
    </AdminPage>
  );
}

export function AdminCustomersPage() {
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();
  const customers = useQuery({ queryKey: ["admin", "customers", page], queryFn: () => adminApi.customers(page) });
  const update = useMutation({ mutationFn: ({ id, status }: { id: string; status: EntityStatus }) => adminApi.updateCustomer(id, status), onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin", "customers"] }) });
  return (
    <AdminPage title="Clientes" description="Controla el estado comercial del perfil, independiente del usuario." loading={customers.isLoading} error={customers.error ?? update.error} retry={() => void customers.refetch()} empty={customers.data?.content.length === 0}>
      {customers.data && <><div className="data-table-wrap"><table className="data-table"><thead><tr><th>Nombre</th><th>Usuario asociado</th><th>Actualización</th><th>Estado</th><th /></tr></thead><tbody>{customers.data.content.map((customer) => <tr key={customer.id}><td className="font-black">{customer.displayName}</td><td className="font-mono">{shortId(customer.userId)}</td><td>{formatDate(customer.updatedAt)}</td><td><StatusBadge value={customer.status} /></td><td className="text-right"><StatusButton status={customer.status} pending={update.isPending} onClick={() => update.mutate({ id: customer.id, status: nextStatus(customer.status) })} /></td></tr>)}</tbody></table></div><Pagination page={page} totalPages={customers.data.totalPages} onChange={setPage} /></>}
    </AdminPage>
  );
}

export function AdminAccountsPage() {
  const [page, setPage] = useState(0);
  const queryClient = useQueryClient();
  const accounts = useQuery({ queryKey: ["admin", "accounts", page], queryFn: () => adminApi.accounts(page) });
  const update = useMutation({ mutationFn: ({ id, status }: { id: string; status: EntityStatus }) => adminApi.updateAccount(id, status), onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin", "accounts"] }) });
  return (
    <AdminPage title="Todas las cuentas" description="Vista administrativa de cuentas de clientes y cuentas del sistema." loading={accounts.isLoading} error={accounts.error ?? update.error} retry={() => void accounts.refetch()} empty={accounts.data?.content.length === 0}>
      {accounts.data && <><div className="data-table-wrap"><table className="data-table"><thead><tr><th>Cuenta</th><th>Tipo</th><th>Moneda</th><th className="text-right">Saldo</th><th>Estado</th><th /></tr></thead><tbody>{accounts.data.content.map((account) => <tr key={account.id}><td className="font-black">{maskAccount(account.accountNumber)}</td><td>{account.kind}</td><td>{account.currency}</td><td className="text-right">{formatMoney(account.balance, account.currency)}</td><td><StatusBadge value={account.status} /></td><td className="text-right"><StatusButton status={account.status} pending={update.isPending} onClick={() => update.mutate({ id: account.id, status: nextStatus(account.status) })} /></td></tr>)}</tbody></table></div><Pagination page={page} totalPages={accounts.data.totalPages} onChange={setPage} /></>}
    </AdminPage>
  );
}

function AdminPage({ title, description, loading, error, retry, empty, children }: { title: string; description: string; loading: boolean; error: unknown; retry: () => void; empty: boolean; children: React.ReactNode }) {
  return <><PageHeader eyebrow="Administración" title={title} description={description} /><Card>{loading && <LoadingState />}{error != null && <ErrorState error={error} onRetry={retry} />}{empty && <EmptyState title="Sin resultados" detail="No hay registros para mostrar." />}{!loading && error == null && !empty && children}</Card></>;
}
