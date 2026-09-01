# Guía del frontend de FinCore

El frontend es una aplicación React de una sola página creada con Vite. Consume
la API REST de Spring Boot y presenta experiencias diferentes para los roles
`CUSTOMER`, `ANALYST` y `ADMIN`.

## Estructura

```text
frontend/src
|-- api/          Cliente HTTP, CSRF y funciones por endpoint
|-- auth/         Estado de sesión y contexto de autenticación
|-- components/   Componentes reutilizables y layout autenticado
|-- pages/        Pantallas agrupadas por flujo de usuario
|-- types/        Contratos TypeScript equivalentes a los DTO del backend
|-- utils/        Formato de moneda, fecha, cuenta e identificadores
|-- App.tsx       Rutas públicas, protegidas y restringidas por rol
|-- main.tsx      Proveedores globales y punto de entrada de React
`-- styles.css    Sistema visual adaptable de FinCore
```

Las carpetas del frontend no copian las capas del backend. En React resulta más
útil separar infraestructura de API, sesión, componentes y pantallas. Los
contratos del negocio siguen representados explícitamente en `types/api.ts`.

## Flujo de una solicitud

```text
Página -> función de endpoints.ts -> cliente HTTP -> proxy de Vite -> Spring Boot
                                      |                               |
                                      `-- cookie + CSRF + correlación -'
```

`api/client.ts` centraliza responsabilidades que no deben repetirse en cada
pantalla:

- agrega el prefijo `/api/v1`;
- conserva la cookie `JSESSIONID` mediante `credentials: "include"`;
- obtiene y envía el token CSRF para métodos que modifican datos;
- agrega un identificador de correlación;
- convierte respuestas `Problem Details` en errores entendibles;
- permite descargar el comprobante PDF autenticado.

El token CSRF se conserva únicamente en memoria. La contraseña solo se utiliza
en la solicitud de login y no se guarda en `localStorage`.

## Estado remoto y sesión

TanStack Query administra consultas, caché, estados de carga y revalidación. El
`AuthProvider` consulta `/auth/me`, conserva el usuario autenticado en memoria y
limpia el caché cuando se cierra la sesión.

El enrutador evita que un usuario navegue desde la interfaz a una pantalla que
no corresponde a su rol. Esta protección mejora la experiencia, pero la
seguridad real continúa en Spring Security: cualquier cliente podría construir
una petición manual y el backend debe rechazarla.

## Pantallas implementadas

### Públicas

- Inicio y cierre de sesión con sesión de Spring Security.
- Registro de un cliente sintético en el perfil local.
- Selección rápida de usuarios demo.

### Cliente

- Resumen de saldos y transferencias recientes.
- Cuentas y movimientos paginados del ledger.
- Alta y borrado lógico de beneficiarios.
- Transferencia con una clave de idempotencia por intento.
- Historial y descarga de comprobantes PDF.

### Analista

- Transferencias globales.
- Conciliación entre saldos operativos y ledger.
- Eventos de auditoría paginados.

### Administrador

- Todas las capacidades del analista.
- Activación y suspensión de usuarios, clientes y cuentas.

## Ejecutar en desarrollo

Desde la raíz del repositorio:

```bash
docker compose up -d postgres
SPRING_PROFILES_ACTIVE=local mise exec -- zsh -c 'cd backend && ./mvnw spring-boot:run'
```

En otra terminal:

```bash
mise exec -- pnpm dev:frontend
```

Abre `http://localhost:5173`. El proxy definido en `vite.config.ts` dirige
`/api` y `/actuator` a `http://localhost:8080`; por eso no se necesita CORS en
el flujo local.

## Verificaciones

```bash
mise exec -- pnpm lint:frontend
mise exec -- pnpm test:frontend
mise exec -- pnpm build:frontend
```

Las pruebas actuales verifican la redirección sin sesión, el login del cliente,
la carga del resumen y el envío del token CSRF en escrituras JSON. El build
también ejecuta TypeScript antes de generar los archivos de producción.

## Decisiones importantes

- La API es la fuente de verdad; React no calcula ni modifica saldos.
- Los importes viajan como números del contrato actual y se formatean según PEN
  o USD para mostrarlos.
- Las mutaciones invalidan las consultas relacionadas para no dejar datos
  obsoletos en la pantalla.
- El diseño es adaptable y las tablas permiten desplazamiento horizontal en
  pantallas pequeñas.
- Los datos y credenciales demo solo se usan con el perfil local.
