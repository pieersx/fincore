# Plan – 001 Modo oscuro en la aplicación autenticada

Spec: `docs/specs/001-dark-mode-dashboard/spec.md` · Mode: light · Status: proposed

## Approach

Add a `ThemeContext`/`ThemeProvider` pair mirroring the existing `AuthContext`/`AuthProvider` pattern (named exports, `createContext<T|null>`, a `useTheme` hook that throws outside the provider, `useMemo` value). Initial theme resolves once, in a `useState` initializer: read `localStorage` first, else fall back to `window.matchMedia('(prefers-color-scheme: dark)').matches`. Manual toggles write to `localStorage`. Mount `ThemeProvider` in `main.tsx` around `App`. In `AppShell.tsx`, apply the resolved theme as `data-theme="dark"` on the existing `.app-frame` div — not on `<html>`/`<body>` — which scopes dark styling to the authenticated shell (sidebar + `<Outlet/>`, same DOM node for desktop and the mobile drawer) and structurally excludes `AuthPages` and `NotFoundPage`, both outside `AppShell`. Convert the hardcoded hex/rgba colors in `styles.css` for in-scope selectors into CSS custom properties defined at `:root` (light defaults) with a `.app-frame[data-theme="dark"] { --color-*: … }` override block. Add a `ThemeToggle` switch to `components/ui.tsx`, rendered in `.sidebar-user` in `AppShell.tsx`.

## Alternatives considered

| Option | Pros | Cons | Verdict |
|---|---|---|---|
| CSS custom properties scoped to `.app-frame[data-theme]` | No new deps; fits Tailwind v4's CSS-first model; structurally excludes auth pages (confirmed `.form-stack`/`.notice` are shared with `AuthPages`, so global scoping would leak) | Touches most of `styles.css` (~90+ literal colors) | **Chosen** |
| Tailwind v4 `@custom-variant dark` + rewrite JSX to `dark:` utilities | Idiomatic Tailwind v4; no CSS var plumbing | Requires rewriting nearly every component-scoped CSS rule into utility classes across all pages — much larger diff, higher regression risk for AC-6 | Rejected: effort/risk disproportionate to the spec |
| Global `<html class="dark">` + manual exclusions for auth selectors | Simple, matches common Tailwind v3 docs/tutorials | Requires manually excluding every auth-only selector from the dark cascade — easy to miss one and violate the spec's explicit non-goal | Rejected: fragile, error-prone |

State/persistence alternatives: a bare custom hook writing a DOM attribute directly (rejected — `AppShell` and the sidebar toggle need one shared instance, Context is safer); a third-party hook library like `usehooks-ts` (rejected — AGENTS.md requires asking before adding dependencies, and no dependency is needed here).

## Existing code to reuse

- `frontend/src/auth/AuthContext.ts` / `AuthProvider.tsx` — exact pattern for `ThemeContext`/`ThemeProvider`.
- `frontend/src/components/ui.tsx` — existing home for small presentational primitives (`Brand`, `Button`, `StatusBadge`); `ThemeToggle` joins this file.
- `frontend/src/App.test.tsx` — `vi.stubGlobal` pattern (already used for `fetch`) reused to mock `window.matchMedia` in tests.

## Affected components

- **Theme state** (new): `ThemeContext`/`ThemeProvider` under `frontend/src/theme/`.
- **App shell** (`AppShell.tsx`): renders `ThemeToggle`, applies `data-theme` to `.app-frame`.
- **Shared styles** (`styles.css`): color literals become CSS variables with a dark override block, scoped to `.app-frame[data-theme="dark"]`.
- **UI primitives** (`ui.tsx`): new `ThemeToggle` component.
- **App entry** (`main.tsx`): wraps `App` with `ThemeProvider`.
- Explicitly unaffected: `AuthPages`, `NotFoundPage` (both rendered outside `AppShell`/`.app-frame`) — per spec non-goals.

## Data & API changes

None. Client-side only; one `localStorage` key (e.g. `fincore-theme`). No backend, schema or contract changes.

## Files to create / modify

| File | Change |
|---|---|
| `frontend/src/theme/ThemeContext.ts` | New — context object + `useTheme` hook |
| `frontend/src/theme/ThemeProvider.tsx` | New — initial resolution, toggle, localStorage persistence |
| `frontend/src/theme/ThemeProvider.test.tsx` | New — unit tests (mocked `matchMedia`/`localStorage`) |
| `frontend/src/components/ui.tsx` | Add `ThemeToggle` |
| `frontend/src/components/ui.test.tsx` | New — `ThemeToggle` behavior/a11y tests |
| `frontend/src/components/AppShell.tsx` | Apply `data-theme` to `.app-frame`; render `ThemeToggle` in `.sidebar-user` |
| `frontend/src/main.tsx` | Wrap `App` with `ThemeProvider` |
| `frontend/src/styles.css` | Introduce `--color-*` tokens at `:root`; add `.app-frame[data-theme="dark"]` override block; rewrite in-scope selectors to reference variables |
| `frontend/src/App.test.tsx` | Extend — instant toggle, mobile menu parity, auth-page non-regression |

## Testing strategy

Unit tests (Vitest + Testing Library) drive every behavioral AC:
- AC-1/AC-2 (initial theme from OS preference): `ThemeProvider.test.tsx`, mocking `matchMedia` with dark/light `matches` and empty `localStorage`.
- AC-3/AC-4 (manual toggle + persistence): `ThemeProvider.test.tsx` for the toggle/persist logic; `App.test.tsx` for the end-to-end click-toggles-instantly behavior through `AppShell`.
- AC-5 (mobile menu parity): `App.test.tsx`, rendering with the mobile drawer open and asserting the toggle is present and functional.
- AC-6 (contrast across shared components): no automated pixel/contrast test — covered by a manual visual checklist over `card`, `metric-*`, `status-badge`, `data-table`, `nav-link`, and form controls, documented at verification time (disproportionate to add automated contrast tooling for this change).
- AC-7 (keyboard reachability + state announcement): `ui.test.tsx`, asserting `role="switch"`/`aria-pressed` and toggling via `Enter`/`Space` as well as click.
- Non-goal guard: `App.test.tsx` asserts that a stored dark preference does not affect `/login` (`AuthPages`) rendering.
- No I/O boundaries beyond `localStorage`/`matchMedia`, both mocked per the testing rule (mock only I/O boundaries).

## Risks & mitigations

| Risk | Likelihood | Mitigation |
|---|---|---|
| Missing a hardcoded color during the `styles.css` conversion (visual regression, AC-6) | Medium | Grep for remaining hex/`rgba(` literals in every selector reachable from `.app-frame` after the edit; manual pass per page |
| Dark override accidentally applied at `:root`/`<html>` scope instead of `.app-frame[data-theme]`, leaking into `AuthPages` | Medium | Explicit non-regression test (T06); called out in `/flow:review` as a specific check |
| `window.matchMedia` undefined in jsdom by default, causing test failures | High (certain without mocking) | Stub via `vi.stubGlobal`, matching the existing `fetch`-mocking pattern in `App.test.tsx`; ensure `vi.unstubAllGlobals()` runs in `afterEach` |
| Flash of incorrect theme on load | Low | Not applicable: `ProtectedRoute` already blocks on a `LoadingState` before `AppShell`/`.app-frame` mounts, and initial theme is computed synchronously in the `useState` initializer |

## Decisions requiring an ADR

None (light mode). If the app grows more themed surfaces later, the choice to scope theming to `.app-frame` rather than `<html>` would be worth recording as an ADR.

## Rollout & rollback

No feature flag — this is a pure frontend addition with no server dependency and no data migration. Ships as a normal PR behind the existing frontend CI (`pnpm lint:frontend`, `pnpm test:frontend`, `pnpm build:frontend`). Rollback is a straight revert of the PR: no persisted server state, and the only client-side artifact is the `fincore-theme` localStorage key, which is inert if the code reading it is removed.
