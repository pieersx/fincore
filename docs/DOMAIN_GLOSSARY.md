# Glosario del dominio de FinCore

## Usuario

Identidad que puede autenticarse en FinCore.

Un usuario tiene credenciales, estado y roles, pero no necesariamente representa
por sí mismo una cuenta financiera.

## Cliente

Persona ficticia que mantiene una relación financiera simulada con FinCore.

Un cliente puede estar asociado con un usuario y tener una o más cuentas.

## Cuenta de cliente

Producto financiero ficticio que pertenece a un cliente.

Una cuenta de cliente:

- Tiene una moneda.
- Tiene un estado.
- Expone un saldo.
- Recibe movimientos.
- Puede participar en transferencias.

En el código se utilizará el nombre `CustomerAccount` para evitar confundirla con
una cuenta contable.

## Cuenta contable

Cuenta interna utilizada por el ledger para clasificar débitos y créditos.

En el código se utilizará el nombre `LedgerAccount`.

Una cuenta contable no es necesariamente una cuenta visible para el cliente.

## Beneficiario

Referencia guardada por un cliente hacia una cuenta de destino.

Un beneficiario no posee dinero y no reemplaza a la cuenta de destino. Solamente
facilita y controla la selección del destino durante una transferencia.

## Transferencia

Proceso de negocio que intenta mover un importe entre dos cuentas de cliente.

Una transferencia tiene:

- Identificador.
- Cuenta de origen.
- Cuenta de destino.
- Importe.
- Moneda.
- Estado.
- Fecha y hora.
- Clave de idempotencia.
- Referencia al asiento contable generado.

Una transferencia y un asiento contable no son el mismo concepto.

## Asiento contable

Registro completo de un hecho financiero.

En el código se utilizará el nombre `JournalEntry`.

Un asiento contiene dos o más movimientos contables. Dentro de una misma moneda,
el total de débitos debe ser igual al total de créditos.

## Movimiento contable

Una línea individual de un asiento contable.

En el código se utilizará el nombre `Posting`.

Cada movimiento contiene:

- Cuenta contable.
- Lado `DEBIT` o `CREDIT`.
- Importe positivo.
- Moneda.
- Referencia al asiento.

Un movimiento contable nunca utiliza un importe negativo para reemplazar el lado
débito o crédito.

## Débito y crédito

Son lados contables, no sinónimos universales de restar y sumar.

Su efecto depende del tipo de cuenta contable.

Para una transferencia interna entre depósitos de clientes, vistos como pasivos
del sistema financiero:

- Un débito reduce el pasivo asociado con el cliente de origen.
- Un crédito aumenta el pasivo asociado con el cliente de destino.

## Ledger

Registro contable formado por cuentas, asientos y movimientos.

El ledger será la fuente financiera de verdad y tendrá comportamiento
append-only. Una corrección se realiza mediante un nuevo asiento compensatorio,
no modificando movimientos históricos.

## Saldo contable

Saldo calculado a partir de los movimientos confirmados del ledger.

Puede materializarse como una proyección para mejorar el rendimiento, pero siempre
debe poder conciliarse contra el ledger.

## Saldo disponible

Importe que el cliente puede utilizar en una nueva operación.

En la primera versión será igual al saldo contable porque todavía no existirán
retenciones. En una versión futura podría calcularse como:

```text
saldo disponible = saldo contable - retenciones
```

## Estado de transferencia

Representa el avance de una transferencia.

Estados iniciales previstos:

- `PENDING`
- `COMPLETED`
- `REJECTED`

Los estados definitivos se validarán durante el modelado del dominio. Una
transferencia completada no puede regresar a pendiente.

## Idempotencia

Propiedad que permite repetir una solicitud sin duplicar su efecto.

Una clave de idempotencia se relaciona con:

- Usuario o cliente.
- Operación.
- Payload normalizado.
- Resultado obtenido.

La misma clave y el mismo payload devuelven el resultado original. La misma clave
con un payload diferente produce un conflicto.

## Auditoría

Registro de quién realizó una operación, qué ocurrió y cuándo ocurrió.

La auditoría no reemplaza al ledger:

- El ledger representa efectos financieros.
- La auditoría representa acciones y decisiones del sistema.

## Conciliación

Proceso que compara dos representaciones relacionadas para detectar diferencias.

FinCore conciliará la proyección de saldos con los movimientos del ledger.

Una diferencia debe generar una alerta; no debe corregirse silenciosamente.

## Correlation ID

Identificador que permite seguir una solicitud a través de logs, operaciones y
eventos relacionados.

## Evento de dominio

Representación de un hecho que ya ocurrió dentro del dominio.

Ejemplo:

```text
TransferCompleted
```

El nombre utiliza pasado porque comunica un hecho confirmado, no una orden.

## Transactional outbox

Patrón que guarda un evento pendiente en PostgreSQL dentro de la misma transacción
que confirma el cambio de negocio.

Posteriormente, otro proceso publica ese evento en Kafka. Esto evita confirmar
una transferencia y perder su evento si Kafka está temporalmente fuera de
servicio.

## Ejemplo contable

El cliente A transfiere PEN 100.00 al cliente B.

Desde la perspectiva contable del sistema:

| Cuenta contable | Lado | Importe | Moneda |
| --- | --- | ---: | --- |
| Depósitos del cliente A | `DEBIT` | 100.00 | PEN |
| Depósitos del cliente B | `CREDIT` | 100.00 | PEN |

La validación del asiento es:

```text
Débitos:  PEN 100.00
Créditos: PEN 100.00
```

Desde la perspectiva del estado de cuenta del cliente:

```text
Cliente A: -PEN 100.00
Cliente B: +PEN 100.00
```

La representación del cliente utiliza signos para mostrar el efecto económico,
pero el ledger utiliza lados contables explícitos.
