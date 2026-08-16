# Alcance del producto FinCore v1.0.0

## 1. Problema

FinCore simula las operaciones fundamentales de un core financiero para demostrar
cómo mantener consistencia, trazabilidad y seguridad durante el movimiento de
dinero ficticio.

## 2. Usuarios

### Cliente

Puede:

- Iniciar y cerrar sesión.
- Consultar sus cuentas.
- Consultar sus saldos y movimientos.
- Administrar sus beneficiarios.
- Realizar transferencias internas.
- Descargar comprobantes.

No puede consultar ni modificar recursos pertenecientes a otro cliente.

### Analista

Puede:

- Consultar transferencias.
- Consultar eventos de auditoría autorizados.
- Investigar operaciones rechazadas o inusuales.

No puede modificar saldos ni registros del ledger.

### Administrador

Puede:

- Consultar usuarios y cuentas.
- Activar o suspender cuentas según las reglas definidas.
- Consultar auditorías administrativas.
- Administrar datos sintéticos de demostración.

No puede editar ni eliminar movimientos contables confirmados.

## 3. Funcionalidades incluidas

### Identidad

- Registro local de usuarios ficticios.
- Inicio y cierre de sesión.
- Roles `CUSTOMER`, `ANALYST` y `ADMIN`.
- Sesiones seguras.
- Datos de demostración reproducibles.

El registro podrá desactivarse en la demo pública.

### Clientes y cuentas

- Perfil sintético de cliente.
- Cuentas en PEN y USD.
- Consulta de saldo.
- Consulta paginada de movimientos.
- Estados de cuenta activa y suspendida.

### Beneficiarios

- Registro de beneficiarios.
- Consulta y eliminación de beneficiarios.
- Validación de que la cuenta de destino existe.
- Prevención de duplicados.

### Transferencias

- Transferencias entre cuentas internas de la misma moneda.
- Validación de propiedad de la cuenta de origen.
- Validación del beneficiario.
- Validación de saldo.
- Control de concurrencia.
- Idempotencia persistida.
- Estados de transferencia.
- Comprobante PDF.

### Ledger

- Contabilidad de doble entrada.
- Asientos balanceados.
- Registros append-only.
- Conciliación entre ledger y saldos proyectados.
- Trazabilidad desde una transferencia hasta sus asientos.

### Auditoría

- Registro de accesos relevantes.
- Registro de cambios administrativos.
- Registro de operaciones financieras.
- Identificador de correlación por solicitud.

## 4. Reglas financieras iniciales

- Los importes deben ser mayores que cero.
- PEN y USD utilizan dos decimales.
- La cuenta de origen y la cuenta de destino deben ser diferentes.
- Ambas cuentas deben estar activas.
- La cuenta de origen debe pertenecer al cliente autenticado.
- Una transferencia debe utilizar una sola moneda.
- La cuenta de origen debe tener saldo suficiente.
- La primera versión no aplicará comisiones.
- Una transferencia confirmada no puede editarse ni eliminarse.
- En cada asiento contable, el total de débitos debe ser igual al total de créditos
  dentro de una misma moneda.
- Todos los timestamps se almacenan en UTC.
- La interfaz puede mostrar las fechas en `America/Lima`.

## 5. Idempotencia

Cada solicitud de transferencia debe incluir `Idempotency-Key`.

- Repetir la clave con el mismo payload devuelve el resultado original.
- Repetir la clave con un payload diferente produce un conflicto.
- Dos solicitudes concurrentes con la misma clave no crean dos transferencias.
- El resultado idempotente se almacena en PostgreSQL.

## 6. Funcionalidades no incluidas en v1.0.0

- Dinero o clientes reales.
- Transferencias interbancarias reales.
- Cambio de divisas.
- Tarjetas.
- Préstamos.
- Intereses.
- Comisiones.
- Pagos de servicios.
- Integraciones con Visa o Mastercard.
- Integraciones con SBS u otras instituciones.
- Recuperación real de contraseña por correo.
- MFA real.
- Kafka.
- Microservicios.
- Kubernetes.
- Machine learning.

## 7. Criterio principal de aceptación

Dadas dos cuentas activas en la misma moneda y con saldo suficiente, cuando el
propietario autenticado realiza una transferencia válida, entonces FinCore debe:

1. Crear exactamente una transferencia.
2. Registrar sus movimientos contables balanceados.
3. Actualizar o proyectar correctamente ambos saldos.
4. Conservar la trazabilidad completa.
5. Generar un comprobante.
6. Devolver el mismo resultado ante un reintento idempotente.
7. Mantener todas estas acciones dentro de una única transacción de base de datos.

Si falla cualquiera de las operaciones financieras anteriores, ninguna debe
quedar confirmada parcialmente.

## 8. Datos de demostración

Todos los datos serán sintéticos y estarán identificados como datos de prueba.

El entorno local tendrá:

- Un cliente con cuentas PEN y USD.
- Un segundo cliente beneficiario.
- Un usuario analista.
- Un usuario administrador.
- Movimientos históricos reproducibles.

Las credenciales de demostración no serán credenciales de producción.
