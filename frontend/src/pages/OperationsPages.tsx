import { useQuery } from "@tanstack/react-query";
import { useState } from "react";

import { operationsApi } from "../api/endpoints";
import { Card, EmptyState, ErrorState, LoadingState, PageHeader, Pagination, StatusBadge } from "../components/ui";
import { formatDate, formatMoney, maskAccount, shortId } from "../utils/format";

export function OperationsPage() {
  const [page, setPage] = useState(0);
  const transfers = useQuery({ queryKey: ["operations", "transfers", page], queryFn: () => operationsApi.transfers(page) });

  return (
    <>
      <PageHeader eyebrow="Operaciones" title="Transferencias globales" description="Consulta transversal para control operativo; no expone acciones que alteren el ledger." />
      <Card>
        {transfers.isLoading && <LoadingState />}
        {transfers.error && <ErrorState error={transfers.error} onRetry={() => void transfers.refetch()} />}
        {transfers.data?.content.length === 0 && <EmptyState title="Sin operaciones" detail="No hay transferencias registradas." />}
        {transfers.data && transfers.data.content.length > 0 && (
          <>
            <div className="data-table-wrap"><table className="data-table">
              <thead><tr><th>Fecha</th><th>Referencia</th><th>Origen</th><th>Destino</th><th>Estado</th><th className="text-right">Importe</th></tr></thead>
              <tbody>{transfers.data.content.map((transfer) => <tr key={transfer.id}>
                <td>{formatDate(transfer.createdAt)}</td><td className="font-black">{transfer.reference}</td><td>{maskAccount(transfer.sourceAccountNumber)}</td><td>{maskAccount(transfer.destinationAccountNumber)}</td><td><StatusBadge value={transfer.status} /></td><td className="text-right font-black">{formatMoney(transfer.amount, transfer.currency)}</td>
              </tr>)}</tbody>
            </table></div>
            <Pagination page={page} totalPages={transfers.data.totalPages} onChange={setPage} />
          </>
        )}
      </Card>
    </>
  );
}

export function ReconciliationPage() {
  const reconciliation = useQuery({ queryKey: ["reconciliation"], queryFn: operationsApi.reconciliation });
  if (reconciliation.isLoading) return <LoadingState label="Conciliando saldos…" />;
  if (reconciliation.error) return <ErrorState error={reconciliation.error} onRetry={() => void reconciliation.refetch()} />;

  const data = reconciliation.data!;
  return (
    <>
      <PageHeader eyebrow="Integridad contable" title="Conciliación" description={`Comparación entre saldo materializado y ledger generada el ${formatDate(data.generatedAt)}.`} />
      <div className="metric-grid">
        <Card><p className="metric-label">Estado general</p><div className="metric-badge"><StatusBadge value={data.balanced ? "BALANCED" : "MISMATCH"} /></div></Card>
        <Card><p className="metric-label">Diferencias detectadas</p><p className="metric-value">{data.mismatchCount}</p></Card>
      </div>
      <Card>
        <div className="data-table-wrap"><table className="data-table">
          <thead><tr><th>Cuenta</th><th>Tipo</th><th>Moneda</th><th className="text-right">Saldo almacenado</th><th className="text-right">Saldo ledger</th><th className="text-right">Diferencia</th><th>Resultado</th></tr></thead>
          <tbody>{data.accounts.map((item) => <tr key={item.accountId}>
            <td>{maskAccount(item.accountNumber)}</td><td>{item.kind}</td><td>{item.currency}</td><td className="text-right">{formatMoney(item.storedBalance, item.currency)}</td><td className="text-right">{formatMoney(item.ledgerBalance, item.currency)}</td><td className="text-right font-black">{formatMoney(item.difference, item.currency)}</td><td><StatusBadge value={item.balanced ? "BALANCED" : "MISMATCH"} /></td>
          </tr>)}</tbody>
        </table></div>
      </Card>
    </>
  );
}

export function AuditPage() {
  const [page, setPage] = useState(0);
  const audit = useQuery({ queryKey: ["audit", page, 20], queryFn: () => operationsApi.audit(page) });
  return (
    <>
      <PageHeader eyebrow="Seguridad" title="Auditoría" description="Registro de quién hizo qué, sobre qué recurso y con qué resultado." />
      <Card>
        {audit.isLoading && <LoadingState />}
        {audit.error && <ErrorState error={audit.error} onRetry={() => void audit.refetch()} />}
        {audit.data?.content.length === 0 && <EmptyState title="Sin eventos" detail="No se encontraron registros de auditoría." />}
        {audit.data && audit.data.content.length > 0 && (
          <>
            <div className="data-table-wrap"><table className="data-table">
              <thead><tr><th>Fecha</th><th>Actor</th><th>Acción</th><th>Recurso</th><th>Resultado</th><th>Correlación</th></tr></thead>
              <tbody>{audit.data.content.map((event) => <tr key={event.id}>
                <td>{formatDate(event.occurredAt)}</td><td className="font-black">{event.actorUsername}</td><td>{event.action}</td><td>{event.resourceType}{event.resourceId ? ` · ${shortId(event.resourceId)}` : ""}</td><td><StatusBadge value={event.outcome} /></td><td className="font-mono">{shortId(event.correlationId)}</td>
              </tr>)}</tbody>
            </table></div>
            <Pagination page={page} totalPages={audit.data.totalPages} onChange={setPage} />
          </>
        )}
      </Card>
    </>
  );
}
