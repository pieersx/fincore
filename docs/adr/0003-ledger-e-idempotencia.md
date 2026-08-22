# ADR 0003: ledger de doble entrada e idempotencia en PostgreSQL

- Estado: aceptada
- Fecha: 2026-08-21

## Contexto

Una transferencia puede ser reenviada por problemas de red o llegar dos veces
en paralelo. Además, modificar saldos sin conservar una explicación contable
haría imposible reconstruir y conciliar las operaciones.

## Decisión

Cada transferencia confirmada se ejecutará dentro de una transacción ACID que
incluye:

- reserva persistente de `Idempotency-Key` por usuario y hash del payload;
- bloqueo pesimista de las cuentas en orden estable;
- actualización de ambos saldos;
- una transferencia confirmada;
- un journal con un débito y un crédito iguales;
- auditoría del resultado.

PostgreSQL impondrá la unicidad idempotente, la inmutabilidad del ledger y el
balance de cada journal mediante constraints y triggers diferidos.

## Consecuencias

- Un reintento seguro devuelve el resultado original.
- Dos solicitudes concurrentes no producen un doble débito.
- Los saldos pueden conciliarse contra registros append-only.
- El diseño depende conscientemente de garantías de PostgreSQL.
- Los flujos distribuidos y el transactional outbox quedan fuera de v1.0.0; se
  evaluarán únicamente cuando exista un servicio independiente real.
