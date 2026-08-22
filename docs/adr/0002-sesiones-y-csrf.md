# ADR 0002: autenticación mediante sesiones y protección CSRF

- Estado: aceptada
- Fecha: 2026-08-21

## Contexto

FinCore necesita autenticar una SPA y demostrar autorización por propiedad y
roles. Al ser inicialmente un monolito desplegado como una unidad, no
existe todavía una necesidad de compartir tokens entre servicios independientes.

Guardar tokens de acceso en almacenamiento del navegador aumentaría la
exposición ante JavaScript malicioso. Implementar manualmente JWT, rotación o
revocación también agregaría complejidad que no resuelve un requisito actual.

## Decisión

La primera versión utilizará:

- sesiones administradas por Spring Security;
- cookie `JSESSIONID` con `HttpOnly` y `SameSite=Strict`;
- `Secure=true` en entornos HTTPS;
- migración del identificador de sesión después del login;
- token CSRF almacenado en la sesión y enviado mediante `X-CSRF-TOKEN`;
- BCrypt con costo 12 para hashes de contraseña;
- autorización cerrada por defecto mediante roles.

Login y logout también requieren CSRF. El frontend obtiene el token desde
`GET /api/v1/auth/csrf` y nunca recibe el hash de una contraseña.

## Consecuencias

- Una sesión puede invalidarse inmediatamente desde el servidor.
- El navegador debe enviar cookies y renovar el token CSRF después de cambios de
  autenticación.
- El despliegue HTTPS debe configurar `SESSION_COOKIE_SECURE=true`.
- Si posteriormente existen clientes móviles o servicios independientes, se
  evaluará OAuth 2.1 y OpenID Connect mediante un proveedor probado.
- Esta decisión no impide incorporar Keycloak más adelante, pero evita diseñar
  un protocolo de autenticación propio.
