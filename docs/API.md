# API de FinCore v1.0.0

Esta guía resume los contratos implementados por el backend. La especificación
OpenAPI generada desde el código está disponible en:

- `http://localhost:8080/v3/api-docs`
- `http://localhost:8080/swagger-ui.html`

Todos los endpoints funcionales utilizan el prefijo `/api/v1`.

## Sesión y CSRF

FinCore usa sesiones de Spring Security. El navegador conserva la cookie
`JSESSIONID`; la contraseña nunca se almacena en React. Todo `POST`, `PATCH` o
`DELETE` requiere el token CSRF de la misma sesión:

1. Ejecutar `GET /api/v1/auth/csrf` y conservar la cookie.
2. Leer `token` de la respuesta.
3. Enviar el valor mediante el header `X-CSRF-TOKEN`.
4. Solicitar un token nuevo después de iniciar o cerrar sesión.

El login recibe `application/x-www-form-urlencoded`; las demás operaciones
reciben JSON, salvo la descarga del comprobante PDF.

## Endpoints

### Públicos e identidad

| Método | Ruta | Acceso | Resultado |
| --- | --- | --- | --- |
| `GET` | `/api/v1/auth/csrf` | Público | Token CSRF de la sesión |
| `POST` | `/api/v1/auth/register` | Público + CSRF | Usuario, cliente y cuentas PEN/USD |
| `POST` | `/api/v1/auth/login` | Público + CSRF | Crea sesión; responde `204` |
| `POST` | `/api/v1/auth/logout` | CSRF | Invalida la sesión; responde `204` |
| `GET` | `/api/v1/auth/me` | Autenticado | Usuario y roles autenticados |
| `GET` | `/actuator/health` | Público | Salud técnica |
| `GET` | `/actuator/info` | Público | Información de la aplicación |

### Cliente

| Método | Ruta | Resultado |
| --- | --- | --- |
| `GET` | `/api/v1/customers/me` | Perfil propio |
| `GET` | `/api/v1/accounts` | Cuentas propias PEN y USD |
| `GET` | `/api/v1/accounts/{accountId}` | Cuenta propia |
| `GET` | `/api/v1/accounts/{accountId}/movements` | Movimientos paginados del ledger |
| `GET` | `/api/v1/beneficiaries` | Beneficiarios activos propios |
| `POST` | `/api/v1/beneficiaries` | Crea un beneficiario |
| `DELETE` | `/api/v1/beneficiaries/{beneficiaryId}` | Borrado lógico del beneficiario |
| `POST` | `/api/v1/transfers` | Confirma una transferencia idempotente |
| `GET` | `/api/v1/transfers` | Transferencias propias paginadas |
| `GET` | `/api/v1/transfers/{transferId}` | Transferencia propia |
| `GET` | `/api/v1/transfers/{transferId}/receipt` | Comprobante `application/pdf` |

### Analista y administrador

| Método | Ruta | Rol | Resultado |
| --- | --- | --- | --- |
| `GET` | `/api/v1/audit-events` | `ANALYST`, `ADMIN` | Auditorías paginadas |
| `GET` | `/api/v1/operations/transfers` | `ANALYST`, `ADMIN` | Transferencias globales |
| `GET` | `/api/v1/operations/transfers/{id}` | `ANALYST`, `ADMIN` | Investigación de una transferencia |
| `GET` | `/api/v1/operations/reconciliation` | `ANALYST`, `ADMIN` | Compara saldos con el ledger |
| `GET` | `/api/v1/admin/users` | `ADMIN` | Usuarios paginados |
| `PATCH` | `/api/v1/admin/users/{id}/status` | `ADMIN` + CSRF | Activa o suspende usuario |
| `GET` | `/api/v1/admin/customers` | `ADMIN` | Clientes paginados |
| `GET` | `/api/v1/admin/customers/{id}` | `ADMIN` | Cliente por ID |
| `PATCH` | `/api/v1/admin/customers/{id}/status` | `ADMIN` + CSRF | Activa o suspende cliente |
| `GET` | `/api/v1/admin/accounts` | `ADMIN` | Cuentas paginadas |
| `GET` | `/api/v1/admin/accounts/{id}` | `ADMIN` | Cuenta por ID |
| `PATCH` | `/api/v1/admin/accounts/{id}/status` | `ADMIN` + CSRF | Activa o suspende cuenta de cliente |

Los listados paginados aceptan `page` desde cero y `size` entre 1 y 100.

## Prueba rápida con curl

Inicia PostgreSQL y el backend desde la raíz:

```bash
docker compose up -d postgres
SPRING_PROFILES_ACTIVE=local mise exec -- zsh -c 'cd backend && ./mvnw spring-boot:run'
```

Obtén el token y guarda la cookie:

```bash
curl -sS --cookie-jar /tmp/fincore-cookies.txt \
  http://localhost:8080/api/v1/auth/csrf
```

Copia el campo `token` y autentica a `customer.one`:

```bash
curl -i \
  --cookie /tmp/fincore-cookies.txt \
  --cookie-jar /tmp/fincore-cookies.txt \
  --request POST \
  --header 'X-CSRF-TOKEN: REEMPLAZAR_CON_TOKEN' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'username=customer.one' \
  --data-urlencode 'password=FincoreDemo!2026' \
  http://localhost:8080/api/v1/auth/login
```

Solicita un token nuevo con la cookie autenticada y úsalo en las escrituras:

```bash
curl -sS \
  --cookie /tmp/fincore-cookies.txt \
  --cookie-jar /tmp/fincore-cookies.txt \
  http://localhost:8080/api/v1/auth/csrf
```

## Crear un beneficiario

`destinationAccountNumber` debe existir, estar activo y pertenecer a otro
cliente. Una cuenta propia o un duplicado es rechazado.

```bash
curl -i \
  --cookie /tmp/fincore-cookies.txt \
  --request POST \
  --header 'X-CSRF-TOKEN: REEMPLAZAR_CON_TOKEN_NUEVO' \
  --header 'Content-Type: application/json' \
  --data '{
    "destinationAccountNumber": "FCPEN0000000002",
    "alias": "Cliente Dos PEN"
  }' \
  http://localhost:8080/api/v1/beneficiaries
```

El escenario demo ya contiene este beneficiario con ID
`60000000-0000-0000-0000-000000000001`.

## Crear una transferencia

Cada intento debe llevar una clave propia de 8 a 100 caracteres. Repetir la
misma clave y el mismo cuerpo devuelve la transferencia original. Usar esa clave
con otro cuerpo responde `409 Conflict`.

```bash
curl -i \
  --cookie /tmp/fincore-cookies.txt \
  --request POST \
  --header 'X-CSRF-TOKEN: REEMPLAZAR_CON_TOKEN_NUEVO' \
  --header 'Idempotency-Key: demo-manual-0001' \
  --header 'Content-Type: application/json' \
  --data '{
    "sourceAccountId": "30000000-0000-0000-0000-000000000001",
    "beneficiaryId": "60000000-0000-0000-0000-000000000001",
    "amount": 25.50,
    "description": "Transferencia manual sintética"
  }' \
  http://localhost:8080/api/v1/transfers
```

Una respuesta confirmada incluye `id`, `reference`, cuentas enmascarables,
moneda, monto, estado y timestamps UTC. Para descargar su comprobante:

```bash
curl -sS \
  --cookie /tmp/fincore-cookies.txt \
  --output comprobante.pdf \
  http://localhost:8080/api/v1/transfers/REEMPLAZAR_CON_ID/receipt
```

## Datos sintéticos locales

| Usuario | Rol | Contraseña |
| --- | --- | --- |
| `customer.one` | `CUSTOMER` | `FincoreDemo!2026` |
| `customer.two` | `CUSTOMER` | `FincoreDemo!2026` |
| `analyst.demo` | `ANALYST` | `FincoreDemo!2026` |
| `admin.demo` | `ADMIN` | `FincoreDemo!2026` |

| Usuario | Cuenta PEN | Saldo inicial | Cuenta USD | Saldo inicial |
| --- | --- | ---: | --- | ---: |
| `customer.one` | `FCPEN0000000001` | 4 750.00 | `FCUSD0000000001` | 1 200.00 |
| `customer.two` | `FCPEN0000000002` | 2 750.00 | `FCUSD0000000002` | 800.00 |

Los datos demo solo se cargan con los perfiles `local` y `test`. La
configuración base mantiene el registro deshabilitado y no crea usuarios.

## Errores y trazabilidad

Los errores usan `application/problem+json`:

| Estado | Significado |
| --- | --- |
| `400` | Cuerpo, header o parámetro inválido |
| `401` | No existe una sesión autenticada |
| `403` | Falta el rol o un CSRF válido |
| `404` | El recurso no existe o no pertenece al cliente |
| `409` | Duplicado o clave idempotente reutilizada con otro cuerpo |
| `422` | Regla de negocio: saldo, estado, moneda o propiedad |

Cada respuesta incluye `X-Correlation-ID`. El backend conserva un valor válido
enviado por el cliente o genera uno nuevo y lo asocia con la auditoría.
