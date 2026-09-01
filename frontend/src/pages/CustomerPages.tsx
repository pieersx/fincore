import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import type { FormEvent } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";

import { errorMessage } from "../api/client";
import { customerApi } from "../api/endpoints";
import { Button, Card, EmptyState, ErrorState, LoadingState, PageHeader, Pagination, SecondaryButton, StatusBadge } from "../components/ui";
import { formatDate, formatMoney, maskAccount } from "../utils/format";

export function AccountsPage() {
  const [selectedAccountId, setSelectedAccountId] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const accounts = useQuery({ queryKey: ["accounts"], queryFn: customerApi.accounts });
  const effectiveAccountId = selectedAccountId ?? accounts.data?.[0]?.id ?? null;
  const movements = useQuery({
    queryKey: ["movements", effectiveAccountId, page],
    queryFn: () => customerApi.movements(effectiveAccountId!, page),
    enabled: effectiveAccountId !== null,
  });

  if (accounts.isLoading) return <LoadingState label="Cargando tus cuentas…" />;
  if (accounts.error) return <ErrorState error={accounts.error} onRetry={() => void accounts.refetch()} />;

  return (
    <>
      <PageHeader eyebrow="Productos" title="Mis cuentas" description="El saldo mostrado se respalda con asientos contables de partida doble." />
      <div className="account-selector" role="list" aria-label="Seleccionar cuenta">
        {accounts.data?.map((account) => (
          <button
            className={`account-card ${effectiveAccountId === account.id ? "account-card-active" : ""}`}
            key={account.id}
            onClick={() => { setSelectedAccountId(account.id); setPage(0); }}
            type="button"
          >
            <span className="card-row"><span className="currency-mark">{account.currency}</span><StatusBadge value={account.status} /></span>
            <strong>{formatMoney(account.balance, account.currency)}</strong>
            <span>{maskAccount(account.accountNumber)}</span>
          </button>
        ))}
      </div>
      <Card>
        <div className="section-heading"><div><p className="eyebrow">Libro mayor</p><h2>Movimientos</h2></div></div>
        {movements.isLoading && <LoadingState label="Consultando movimientos…" />}
        {movements.error && <ErrorState error={movements.error} onRetry={() => void movements.refetch()} />}
        {movements.data && movements.data.content.length === 0 && <EmptyState title="Sin movimientos" detail="Esta cuenta todavía no registra actividad." />}
        {movements.data && movements.data.content.length > 0 && (
          <>
            <div className="data-table-wrap">
              <table className="data-table">
                <thead><tr><th>Fecha</th><th>Concepto</th><th>Referencia</th><th>Tipo</th><th className="text-right">Importe</th></tr></thead>
                <tbody>
                  {movements.data.content.map((movement) => (
                    <tr key={movement.entryId}>
                      <td>{formatDate(movement.occurredAt)}</td>
                      <td>{movement.description}</td>
                      <td className="font-mono">{movement.referenceType}</td>
                      <td><StatusBadge value={movement.type} /></td>
                      <td className={`text-right font-black ${movement.type === "CREDIT" ? "amount-positive" : ""}`}>
                        {movement.type === "CREDIT" ? "+" : "-"}{formatMoney(movement.amount, movement.currency)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination page={page} totalPages={movements.data.totalPages} onChange={setPage} />
          </>
        )}
      </Card>
    </>
  );
}

export function BeneficiariesPage() {
  const queryClient = useQueryClient();
  const [form, setForm] = useState({ destinationAccountNumber: "", alias: "" });
  const [formError, setFormError] = useState<string | null>(null);
  const beneficiaries = useQuery({ queryKey: ["beneficiaries"], queryFn: customerApi.beneficiaries });
  const create = useMutation({
    mutationFn: customerApi.createBeneficiary,
    onSuccess: async () => {
      setForm({ destinationAccountNumber: "", alias: "" });
      setFormError(null);
      await queryClient.invalidateQueries({ queryKey: ["beneficiaries"] });
    },
    onError: (error) => setFormError(errorMessage(error)),
  });
  const remove = useMutation({
    mutationFn: customerApi.deleteBeneficiary,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["beneficiaries"] }),
  });

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    create.mutate({ destinationAccountNumber: form.destinationAccountNumber.trim(), alias: form.alias.trim() });
  }

  return (
    <>
      <PageHeader eyebrow="Directorio" title="Beneficiarios" description="Guarda cuentas de destino válidas para transferir sin volver a escribirlas." />
      <div className="two-column-layout">
        <Card>
          <p className="eyebrow">Nuevo registro</p><h2>Agregar beneficiario</h2>
          {formError && <div className="notice notice-error" role="alert">{formError}</div>}
          <form className="form-stack compact-form" onSubmit={handleSubmit}>
            <label>Alias<input required maxLength={80} placeholder="Ej. Ahorros de Ana" value={form.alias} onChange={(event) => setForm({ ...form, alias: event.target.value })} /></label>
            <label>Número de cuenta<input required maxLength={30} placeholder="FC-…" value={form.destinationAccountNumber} onChange={(event) => setForm({ ...form, destinationAccountNumber: event.target.value })} /></label>
            <Button disabled={create.isPending} type="submit">{create.isPending ? "Guardando…" : "Guardar beneficiario"}</Button>
          </form>
        </Card>
        <Card>
          <div className="section-heading"><div><p className="eyebrow">Guardados</p><h2>Mis contactos</h2></div></div>
          {beneficiaries.isLoading && <LoadingState />}
          {beneficiaries.error && <ErrorState error={beneficiaries.error} onRetry={() => void beneficiaries.refetch()} />}
          {remove.error && <div className="notice notice-error" role="alert">{errorMessage(remove.error)}</div>}
          {beneficiaries.data?.length === 0 && <EmptyState title="Sin beneficiarios" detail="Agrega el primero con el formulario." />}
          <div className="beneficiary-list">
            {beneficiaries.data?.map((beneficiary) => (
              <article className="beneficiary-item" key={beneficiary.id}>
                <span className="avatar">{beneficiary.alias.slice(0, 2).toUpperCase()}</span>
                <div><p className="font-black">{beneficiary.alias}</p><p>{maskAccount(beneficiary.destinationAccountNumber)} · {beneficiary.currency}</p></div>
                <SecondaryButton disabled={remove.isPending} type="button" onClick={() => remove.mutate(beneficiary.id)}>Eliminar</SecondaryButton>
              </article>
            ))}
          </div>
        </Card>
      </div>
    </>
  );
}

export function TransfersPage() {
  const [page, setPage] = useState(0);
  const location = useLocation();
  const transfers = useQuery({ queryKey: ["transfers", page, 20], queryFn: () => customerApi.transfers(page) });
  const [downloadError, setDownloadError] = useState<string | null>(null);

  async function downloadReceipt(id: string, reference: string) {
    setDownloadError(null);
    try {
      const blob = await customerApi.receipt(id);
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement("a");
      anchor.href = url;
      anchor.download = `comprobante-${reference}.pdf`;
      anchor.click();
      URL.revokeObjectURL(url);
    } catch (error) {
      setDownloadError(errorMessage(error));
    }
  }

  return (
    <>
      <PageHeader eyebrow="Pagos" title="Transferencias" description="Historial de operaciones confirmadas con comprobante descargable." action={<Link className="button-primary" to="/app/transfers/new">Nueva transferencia</Link>} />
      <Card>
        {typeof location.state === "object" && location.state !== null && "createdReference" in location.state && (
          <div className="notice notice-success" role="status">Transferencia {String(location.state.createdReference)} confirmada.</div>
        )}
        {downloadError && <div className="notice notice-error" role="alert">{downloadError}</div>}
        {transfers.isLoading && <LoadingState />}
        {transfers.error && <ErrorState error={transfers.error} onRetry={() => void transfers.refetch()} />}
        {transfers.data?.content.length === 0 && <EmptyState title="Sin transferencias" detail="Cuando realices una operación aparecerá aquí." />}
        {transfers.data && transfers.data.content.length > 0 && (
          <>
            <div className="data-table-wrap">
              <table className="data-table">
                <thead><tr><th>Fecha</th><th>Referencia</th><th>Destino</th><th>Estado</th><th className="text-right">Importe</th><th /></tr></thead>
                <tbody>{transfers.data.content.map((transfer) => (
                  <tr key={transfer.id}>
                    <td>{formatDate(transfer.createdAt)}</td><td className="font-black">{transfer.reference}</td><td>{maskAccount(transfer.destinationAccountNumber)}</td><td><StatusBadge value={transfer.status} /></td><td className="text-right font-black">{formatMoney(transfer.amount, transfer.currency)}</td>
                    <td className="text-right"><button className="table-action" type="button" onClick={() => void downloadReceipt(transfer.id, transfer.reference)}>PDF</button></td>
                  </tr>
                ))}</tbody>
              </table>
            </div>
            <Pagination page={page} totalPages={transfers.data.totalPages} onChange={setPage} />
          </>
        )}
      </Card>
    </>
  );
}

export function NewTransferPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const accounts = useQuery({ queryKey: ["accounts"], queryFn: customerApi.accounts });
  const beneficiaries = useQuery({ queryKey: ["beneficiaries"], queryFn: customerApi.beneficiaries });
  const [form, setForm] = useState({ sourceAccountId: "", beneficiaryId: "", amount: "", description: "" });
  const activeAccounts = useMemo(() => accounts.data?.filter((account) => account.status === "ACTIVE") ?? [], [accounts.data]);
  const selectedAccount = activeAccounts.find((account) => account.id === form.sourceAccountId);
  const compatibleBeneficiaries = beneficiaries.data?.filter((beneficiary) => !selectedAccount || beneficiary.currency === selectedAccount.currency) ?? [];

  const create = useMutation({
    mutationFn: () => customerApi.createTransfer({
      sourceAccountId: form.sourceAccountId,
      beneficiaryId: form.beneficiaryId,
      amount: Number(form.amount),
      description: form.description.trim(),
    }, `web-${crypto.randomUUID()}`),
    onSuccess: async (transfer) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ["accounts"] }),
        queryClient.invalidateQueries({ queryKey: ["transfers"] }),
      ]);
      navigate("/app/transfers", { replace: true, state: { createdReference: transfer.reference } });
    },
  });

  if (accounts.isLoading || beneficiaries.isLoading) return <LoadingState label="Preparando la transferencia…" />;
  if (accounts.error || beneficiaries.error) return <ErrorState error={accounts.error ?? beneficiaries.error} />;

  return (
    <>
      <PageHeader eyebrow="Nueva operación" title="Realizar transferencia" description="La misma clave de idempotencia evita duplicar el débito ante reintentos de red." />
      <Card className="transfer-form-card">
        {create.error && <div className="notice notice-error" role="alert">{errorMessage(create.error)}</div>}
        {compatibleBeneficiaries.length === 0 && <div className="notice">Necesitas un beneficiario activo en la misma moneda. <Link to="/app/beneficiaries">Agregar beneficiario</Link>.</div>}
        <form className="form-stack" onSubmit={(event) => { event.preventDefault(); create.mutate(); }}>
          <label>Cuenta de origen
            <select required value={form.sourceAccountId} onChange={(event) => setForm({ ...form, sourceAccountId: event.target.value, beneficiaryId: "" })}>
              <option value="">Selecciona una cuenta</option>
              {activeAccounts.map((account) => <option key={account.id} value={account.id}>{account.currency} · {maskAccount(account.accountNumber)} · {formatMoney(account.balance, account.currency)}</option>)}
            </select>
          </label>
          <label>Beneficiario
            <select required disabled={!form.sourceAccountId} value={form.beneficiaryId} onChange={(event) => setForm({ ...form, beneficiaryId: event.target.value })}>
              <option value="">Selecciona un beneficiario</option>
              {compatibleBeneficiaries.map((beneficiary) => <option key={beneficiary.id} value={beneficiary.id}>{beneficiary.alias} · {maskAccount(beneficiary.destinationAccountNumber)}</option>)}
            </select>
          </label>
          <label>Importe ({selectedAccount?.currency ?? "moneda"})<input required min="0.01" step="0.01" inputMode="decimal" type="number" value={form.amount} onChange={(event) => setForm({ ...form, amount: event.target.value })} /></label>
          <label>Descripción <input required maxLength={140} placeholder="Motivo de la transferencia" value={form.description} onChange={(event) => setForm({ ...form, description: event.target.value })} /></label>
          <div className="form-actions"><SecondaryButton type="button" onClick={() => navigate(-1)}>Cancelar</SecondaryButton><Button disabled={create.isPending || compatibleBeneficiaries.length === 0} type="submit">{create.isPending ? "Procesando…" : "Confirmar transferencia"}</Button></div>
        </form>
      </Card>
    </>
  );
}
