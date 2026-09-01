import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";

import { customerApi, operationsApi } from "../api/endpoints";
import { useAuth } from "../auth/AuthContext";
import { Card, ErrorState, LoadingState, PageHeader, StatusBadge } from "../components/ui";
import { formatDate, formatMoney, maskAccount } from "../utils/format";

export function DashboardPage() {
  const { hasRole, user } = useAuth();
  return hasRole("CUSTOMER") ? <CustomerDashboard /> : <OperationsDashboard username={user?.username ?? "equipo"} />;
}

function CustomerDashboard() {
  const profile = useQuery({ queryKey: ["profile"], queryFn: customerApi.profile });
  const accounts = useQuery({ queryKey: ["accounts"], queryFn: customerApi.accounts });
  const transfers = useQuery({ queryKey: ["transfers", 0, 5], queryFn: () => customerApi.transfers(0, 5) });
  const firstError = profile.error ?? accounts.error ?? transfers.error;

  if (profile.isLoading || accounts.isLoading || transfers.isLoading) return <LoadingState label="Preparando tu resumen…" />;
  if (firstError) return <ErrorState error={firstError} onRetry={() => void Promise.all([profile.refetch(), accounts.refetch(), transfers.refetch()])} />;

  return (
    <>
      <PageHeader eyebrow="Banca personal" title={`Hola, ${profile.data?.displayName ?? "cliente"}`} description="Consulta tus saldos y la actividad reciente de tus cuentas sintéticas." action={<Link className="button-primary" to="/app/transfers/new">Nueva transferencia</Link>} />
      <div className="metric-grid">
        {accounts.data?.map((account) => (
          <Card className="account-summary" key={account.id}>
            <div className="card-row"><span className="currency-mark">{account.currency}</span><StatusBadge value={account.status} /></div>
            <p className="metric-value">{formatMoney(account.balance, account.currency)}</p>
            <p className="metric-label">{maskAccount(account.accountNumber)}</p>
          </Card>
        ))}
      </div>
      <Card>
        <div className="section-heading"><div><p className="eyebrow">Actividad reciente</p><h2>Últimas transferencias</h2></div><Link to="/app/transfers">Ver todas</Link></div>
        {transfers.data?.content.length ? (
          <div className="activity-list">
            {transfers.data.content.map((transfer) => (
              <article className="activity-item" key={transfer.id}>
                <div><p className="font-black">{transfer.reference}</p><p>{maskAccount(transfer.destinationAccountNumber)} · {formatDate(transfer.createdAt)}</p></div>
                <div className="activity-amount"><strong>-{formatMoney(transfer.amount, transfer.currency)}</strong><StatusBadge value={transfer.status} /></div>
              </article>
            ))}
          </div>
        ) : <p className="empty-inline">Todavía no tienes transferencias.</p>}
      </Card>
    </>
  );
}

function OperationsDashboard({ username }: { username: string }) {
  const transfers = useQuery({ queryKey: ["operations", "transfers", 0, 5], queryFn: () => operationsApi.transfers(0, 5) });
  const reconciliation = useQuery({ queryKey: ["reconciliation"], queryFn: operationsApi.reconciliation });
  const audit = useQuery({ queryKey: ["audit", 0, 5], queryFn: () => operationsApi.audit(0, 5) });
  const firstError = transfers.error ?? reconciliation.error ?? audit.error;

  if (transfers.isLoading || reconciliation.isLoading || audit.isLoading) return <LoadingState label="Cargando el panel operativo…" />;
  if (firstError) return <ErrorState error={firstError} onRetry={() => void Promise.all([transfers.refetch(), reconciliation.refetch(), audit.refetch()])} />;

  return (
    <>
      <PageHeader eyebrow="Control operativo" title={`Bienvenido, ${username}`} description="Visión consolidada de transferencias, conciliación y eventos auditables." />
      <div className="metric-grid metric-grid-three">
        <Card><p className="metric-label">Transferencias registradas</p><p className="metric-value">{transfers.data?.totalElements ?? 0}</p><Link to="/app/operations">Explorar operaciones</Link></Card>
        <Card><p className="metric-label">Diferencias contables</p><p className="metric-value">{reconciliation.data?.mismatchCount ?? 0}</p><StatusBadge value={reconciliation.data?.balanced ? "BALANCED" : "MISMATCH"} /></Card>
        <Card><p className="metric-label">Eventos auditados</p><p className="metric-value">{audit.data?.totalElements ?? 0}</p><Link to="/app/audit">Abrir auditoría</Link></Card>
      </div>
      <Card>
        <div className="section-heading"><div><p className="eyebrow">Últimos eventos</p><h2>Trazabilidad</h2></div><Link to="/app/audit">Ver auditoría</Link></div>
        <div className="activity-list">
          {audit.data?.content.map((event) => (
            <article className="activity-item" key={event.id}>
              <div><p className="font-black">{event.action}</p><p>{event.actorUsername} · {formatDate(event.occurredAt)}</p></div>
              <StatusBadge value={event.outcome} />
            </article>
          ))}
        </div>
      </Card>
    </>
  );
}
