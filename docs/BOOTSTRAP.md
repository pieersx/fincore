# Creación inicial de los proyectos

Este documento registra cómo se generaron las bases oficiales del frontend y
del backend. Los generadores crean el punto de partida; la arquitectura, la
configuración, las pruebas y la interfaz de FinCore se incorporan después.

> No vuelvas a ejecutar estos comandos sobre las carpetas existentes. Podrías
> sobrescribir código del proyecto. Se conservan aquí para que el origen del
> repositorio sea reproducible y comprensible.

## Frontend: Vite

Desde la raíz del repositorio se ejecutó:

```bash
mise exec -- pnpm create vite frontend --template react-ts --no-interactive
```

El generador oficial creó la plantilla React con TypeScript. Después se:

- integró la carpeta en el workspace de pnpm;
- fijaron versiones exactas de las dependencias;
- añadió Tailwind CSS;
- configuró el proxy de `/actuator` hacia Spring Boot;
- reemplazó la pantalla de ejemplo por la interfaz de FinCore;
- añadió Vitest y React Testing Library;
- mantuvo Oxlint, incluido por la plantilla actual de Vite;
- creó el workflow de integración continua.

Instalación y verificación:

```bash
mise exec -- pnpm install
pnpm lint:frontend
pnpm test:frontend
pnpm build:frontend
```

Ejecución en desarrollo:

```bash
pnpm dev:frontend
```

## Backend: Spring Initializr

La plantilla se descargó desde Spring Initializr con estas opciones:

| Opción | Valor |
| --- | --- |
| Project | Maven |
| Language | Java |
| Spring Boot | 4.1.0 |
| Group | `com.fincore` |
| Artifact | `fincore-backend` |
| Package | `com.fincore` |
| Packaging | Jar |
| Java | 21 |

Dependencias seleccionadas:

- Spring Web;
- Validation;
- Actuator;
- Spring Data JPA;
- Flyway Migration;
- PostgreSQL Driver;
- Testcontainers.

El equivalente reproducible desde la terminal es:

```bash
curl -fsSLG https://start.spring.io/starter.tgz \
  --data-urlencode type=maven-project \
  --data-urlencode language=java \
  --data-urlencode bootVersion=4.1.0 \
  --data-urlencode baseDir=backend \
  --data-urlencode groupId=com.fincore \
  --data-urlencode artifactId=fincore-backend \
  --data-urlencode name=FinCore \
  --data-urlencode packageName=com.fincore \
  --data-urlencode packaging=jar \
  --data-urlencode javaVersion=21 \
  --data-urlencode dependencies=web,validation,actuator,data-jpa,flyway,postgresql,testcontainers \
  --output backend-initializr.tgz
```

Después de generar la plantilla se:

- conservó el Maven Wrapper oficial;
- eliminó la ayuda y configuración de IDE redundantes con la raíz;
- configuró Spring mediante `application.yml`;
- organizó el código mediante Feature + Layers;
- fijó PostgreSQL 18.6 para que las pruebas sean reproducibles;
- añadió la migración inicial de Flyway;
- amplió el test de contexto con Actuator y base de datos.

Verificación:

```bash
mise exec -- zsh -c 'cd backend && ./mvnw verify'
```

Las pruebas usan Testcontainers, por lo que Docker debe estar iniciado. No es
necesario levantar PostgreSQL manualmente para ejecutar `./mvnw verify`.

## Ejecutar todo localmente

Primero inicia la base de datos de desarrollo:

```bash
docker compose up -d postgres
```

Luego ejecuta el backend y el frontend en terminales separadas:

```bash
SPRING_PROFILES_ACTIVE=local mise exec -- zsh -c 'cd backend && ./mvnw spring-boot:run'
```

```bash
pnpm dev:frontend
```

Abre `http://localhost:5173`. El frontend consultará el endpoint de salud del
backend mediante el proxy de Vite.

Fuentes oficiales: [Vite](https://vite.dev/guide/) y
[Spring Initializr](https://start.spring.io/).
