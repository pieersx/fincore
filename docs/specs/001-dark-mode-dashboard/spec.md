---
id: "001"
slug: dark-mode-dashboard
title: "Modo oscuro en la aplicación autenticada"
mode: light
status: shipped
created: 2026-09-13
branch: feat/001-dark-mode-dashboard
---

<!-- mode: light | full · status: draft | approved | implementing | verified | shipped -->

# 001 – Modo oscuro en la aplicación autenticada

## Problem / Context

Hoy toda la interfaz (login, `AppShell` y las páginas bajo `/app`, incluido el Dashboard) usa una única paleta clara con colores fijos en `frontend/src/styles.css`. No existe ningún soporte de tema oscuro ni control para cambiarlo. Usuarios que prefieren o necesitan un tema oscuro (por accesibilidad, uso nocturno o preferencia del sistema operativo) no tienen forma de obtenerlo hoy.

## Goals

- Ofrecer un tema oscuro completo para toda la app autenticada (sidebar, header móvil y todas las páginas bajo `/app`), no solo el Dashboard.
- Detectar `prefers-color-scheme` del sistema operativo como valor inicial cuando el usuario no ha elegido nada todavía.
- Permitir alternar manualmente entre modo claro y oscuro con un control ubicado junto a la información del usuario en el sidebar (y accesible también en el menú móvil).
- Recordar la elección manual del usuario entre sesiones (persistencia local en el navegador).
- Mantener contraste de texto y componentes (cards, badges, tablas, formularios) legible en ambos temas.

## Non-goals

- No se traduce ni cambia el tema de las páginas públicas de autenticación (`AuthPages` / `auth-layout`, `auth-story`, `auth-panel`) en esta iteración — quedan fuera de alcance.
- No se sincroniza la preferencia de tema entre dispositivos ni se persiste en el backend / cuenta del usuario; es una preferencia local del navegador.
- No se añaden temas adicionales más allá de claro/oscuro (p. ej. alto contraste, temas por rol).
- No se cambia la paleta de marca (verde/lima) ni el layout existente; solo se adaptan los valores de color para modo oscuro.

## Scenarios

- Como cliente que usa la app de noche, cuando abro FinCore con el sistema operativo en modo oscuro, quiero que la app cargue en modo oscuro automáticamente para no encandilarme.
- Como cualquier usuario autenticado, cuando hago clic en el control de tema junto a mi usuario en el sidebar, quiero que toda la app (sidebar, header, página actual) cambie de tema al instante sin recargar.
- Como usuario que eligió modo oscuro, cuando cierro sesión y vuelvo a entrar más tarde, quiero que la app recuerde mi elección en lugar de volver a preguntarle al sistema operativo.
- Como usuario en un dispositivo móvil, cuando abro el menú lateral, quiero poder cambiar de tema desde ahí igual que en escritorio.

## Acceptance criteria

- **AC-1** Given un usuario sin preferencia guardada y el sistema operativo en modo oscuro, When carga cualquier página bajo `/app`, Then la app se renderiza en modo oscuro sin acción manual.
- **AC-2** Given un usuario sin preferencia guardada y el sistema operativo en modo claro, When carga cualquier página bajo `/app`, Then la app se renderiza en modo claro.
- **AC-3** Given la app autenticada en cualquier tema, When el usuario hace clic en el control de tema en el sidebar, Then el tema cambia (claro↔oscuro) de inmediato en el sidebar, header móvil y el contenido de la página actual, sin recargar la página.
- **AC-4** Given un usuario que cambió manualmente el tema, When recarga la página o vuelve a iniciar sesión más tarde (mismo navegador), Then la app se abre con el tema elegido manualmente, ignorando la preferencia del sistema operativo en ese momento.
- **AC-5** Given el menú móvil abierto (breakpoint ≤900px), When el usuario busca el control de tema, Then lo encuentra visible y funcional en esa vista, con el mismo comportamiento que en escritorio.
- **AC-6** Given el modo oscuro activo, When se inspeccionan los componentes compartidos (`card`, `metric-*`, `status-badge`, `data-table`, `nav-link`, formularios), Then el texto y los elementos interactivos mantienen contraste legible (sin texto oscuro sobre fondo oscuro ni claro sobre claro).
- **AC-7** Given un lector de pantalla o navegación por teclado, When el usuario activa el control de tema con Enter/Espacio, Then el control es alcanzable por teclado y anuncia su estado (p. ej. `aria-pressed` o rol de switch) indicando el tema actual.

## Risks & open questions

- [NEEDS CLARIFICATION: si el usuario borra el `localStorage` del navegador, la preferencia manual se pierde y la app vuelve a seguir `prefers-color-scheme`; se asume que este comportamiento es aceptable y no requiere aviso al usuario.]
- `styles.css` fija casi todos los colores como valores hexadecimales directos en reglas de componente (no variables), por lo que aplicar un segundo tema implica tocar la mayoría de las reglas de ese archivo — mayor superficie de cambio de la que el nombre "modo oscuro al dashboard" sugiere a primera vista.
- Las páginas de autenticación (`AuthPages`) quedan explícitamente fuera de alcance (Non-goals); si un usuario cambia a oscuro dentro de `/app` y luego cierra sesión, la pantalla de login puede sentirse inconsistente (permanece en claro). Se acepta como conocido para esta iteración.

## Verification
<!-- Filled by /flow:verify. Do not edit by hand. -->
Verified 2026-09-13 · `pnpm test:frontend` (13/13 pass) · `pnpm lint:frontend` (clean) · `pnpm build:frontend` (tsc -b + vite build, exit 0)

| Criterion | Status | Evidence |
|---|---|---|
| AC-1 | PASS | `ThemeProvider.test.tsx` → "resolves dark when OS prefers dark and no stored preference" |
| AC-2 | PASS | `ThemeProvider.test.tsx` → "resolves light when OS prefers light and no stored preference" |
| AC-3 | PASS | `App.test.tsx` → "clicking the sidebar theme control switches theme immediately, without navigation or reload"; `ThemeProvider.test.tsx` → "toggle switches theme value" |
| AC-4 | PASS | `ThemeProvider.test.tsx` → "reads persisted preference on mount, ignoring current OS preference"; "persists a manually toggled theme so a second provider instance picks it up" |
| AC-5 | PASS | `App.test.tsx` → "theme toggle is visible and functional when the mobile menu is open" |
| AC-6 | NOT-VERIFIABLE | Requires visual/browser rendering, not automatable headlessly; covered by manual contrast review during `/flow:review` on `styles.css` (found and fixed one real regression: `.form-stack label` illegible in dark, fixed via `--color-label-text`). Supporting evidence: built CSS bundle (`dist/assets/*.css`) contains the fixed tokens and the `data-theme` selector. |
| AC-7 | PASS | `ui.test.tsx` → "renders with role switch/aria-checked reflecting current theme"; "fires onToggle on click (native button provides Enter/Space activation for free)" |
