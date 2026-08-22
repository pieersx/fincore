# Contribuir a FinCore

Esta guía define un flujo de trabajo pequeño y verificable para mantener `main`
estable y un historial fácil de revisar.

## Estrategia de ramas

- `main`: código estable e integrado.
- `feat/<nombre>`: funcionalidad nueva, por ejemplo `feat/accounts-module`.
- `fix/<nombre>`: corrección de un defecto.
- `test/<nombre>`: pruebas sin cambio funcional.
- `docs/<nombre>`: documentación.
- `chore/<nombre>`: herramientas, dependencias o mantenimiento.

No se utiliza una rama permanente `develop`. FinCore es desarrollado inicialmente
por una persona y las ramas cortas reducen divergencia y conflictos innecesarios.

## Flujo para cada cambio

1. Actualizar `main` y crear una rama con un objetivo concreto.
2. Implementar el incremento junto con sus pruebas y documentación necesaria.
3. Ejecutar las verificaciones locales.
4. Crear commits pequeños con Conventional Commits.
5. Abrir un pull request hacia `main` y completar su auto-revisión.
6. Integrar únicamente cuando las comprobaciones automáticas estén en verde.
7. Eliminar la rama después del merge.

Ejemplo:

```bash
git switch main
git pull --ff-only
git switch -c feat/accounts-module

mise exec -- zsh -c 'cd backend && ./mvnw test'

git add backend
git commit -m "feat(accounts): add account balance query"
git push -u origin feat/accounts-module
```

## Commits

El formato es `<tipo>(<alcance>): <descripción>`.

Tipos principales: `feat`, `fix`, `test`, `docs`, `refactor`, `perf`, `build`,
`ci` y `chore`.

El mensaje explica el resultado del cambio. No se crean commits por cada archivo
ni se agrupan funcionalidades no relacionadas en un mismo commit.

## Criterio de terminado

Antes de solicitar revisión, el cambio debe:

- Compilar desde un clon limpio usando herramientas versionadas.
- Incluir pruebas del comportamiento relevante.
- Mantener la separación Feature + Layers y sus dependencias entre capas.
- No contener secretos ni datos financieros o personales reales.
- Actualizar README, OpenAPI o ADR cuando el comportamiento o una decisión cambien.
- Evitar código muerto, paquetes vacíos y comentarios que repitan el código.

Los comentarios se reservan para explicar decisiones, invariantes y motivos que el
código por sí solo no comunica. Los nombres claros y las pruebas describen el
funcionamiento ordinario.
