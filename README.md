# FinCore

FinCore es un simulador educativo de core financiero desarrollado para demostrar
ingeniería backend, arquitectura de software, testing, seguridad, DevOps y cloud.

El sistema comienza como un monolito modular y evoluciona progresivamente hacia
eventos, servicios independientes, AWS y Kubernetes.

> FinCore utiliza exclusivamente datos sintéticos y no procesa dinero real.

![Arquitectura evolutiva de FinCore](docs/architecture/fincore-evolution.png)

## Estado del proyecto

FinCore se encuentra en la fase de *walking skeleton*. El backend ya puede
compilarse, ejecutarse y probarse contra PostgreSQL; los módulos financieros y
el frontend se incorporarán en incrementos posteriores.

## Objetivo

Construir un producto financiero ficticio que permita demostrar:

- APIs REST con Java y Spring Boot.
- Diseño modular orientado al dominio.
- Transacciones financieras consistentes.
- Ledger inmutable de doble entrada.
- Idempotencia y control de concurrencia.
- Seguridad, autorización y auditoría.
- Pruebas automatizadas.
- CI/CD y seguridad de la cadena de suministro.
- Infraestructura reproducible en AWS.
- Observabilidad.
- Evolución justificada hacia eventos y microservicios.

## Funcionalidades previstas para v1.0.0

- Usuarios y roles.
- Clientes ficticios.
- Cuentas en PEN y USD.
- Beneficiarios.
- Transferencias internas.
- Historial de movimientos.
- Ledger de doble entrada.
- Comprobantes PDF.
- Auditoría.
- Paneles para cliente, analista y administrador.

Consulta el [alcance detallado](docs/PRODUCT_SCOPE.md) para conocer las reglas y
los límites de la primera versión.

## Arquitectura

La primera versión será una aplicación Spring Boot desplegable como una sola
unidad y separada internamente en módulos de negocio:

```text
com.fincore
|-- identity
|-- customers
|-- accounts
|-- beneficiaries
|-- transfers
|-- ledger
|-- audit
`-- shared
```

Spring Modulith verificará los límites y dependencias entre módulos.

Los microservicios no se utilizarán hasta que exista una necesidad técnica
demostrable.

## Stack principal

| Área | Tecnologías |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1, Spring Modulith 2.1 y Maven |
| Persistencia | PostgreSQL 18, Spring Data JPA y Flyway |
| Seguridad | Spring Security, sesiones seguras y RBAC |
| Frontend | React 19.2, TypeScript, Vite y Tailwind CSS |
| Pruebas | JUnit, ArchUnit, Testcontainers, Vitest y Playwright |
| DevOps | Docker, GitHub Actions, Trivy, CodeQL y Terraform |
| Cloud | AWS ECS, RDS, S3, CloudFront, ECR y CloudWatch |
| Evolución | OpenTelemetry, Kafka y Kubernetes |

Kafka y Kubernetes pertenecen a etapas posteriores; no son dependencias del
producto inicial.

## Evolución prevista

| Versión | Resultado |
| --- | --- |
| `v0.1.0` | Fundamentos del repositorio y arquitectura |
| `v0.2.0` | Walking skeleton |
| `v0.3.0` | Identidad y clientes |
| `v0.4.0` | Cuentas y ledger |
| `v0.5.0` | Transferencias |
| `v1.0.0` | Monolito modular completo |
| `v1.1.0` | AWS y observabilidad |
| `v2.0.0` | Transactional outbox y Kafka |
| `v2.1.0` | Servicio de notificaciones |
| `v3.0.0` | Kubernetes |

## Documentación

- [Plan de 10 semanas](docs/PROJECT_PLAN.md).
- [Alcance de v1.0.0](docs/PRODUCT_SCOPE.md).
- [Glosario del dominio](docs/DOMAIN_GLOSSARY.md).
- [Diagrama de arquitectura](docs/architecture/fincore-evolution.png).
- [Guía de contribución](CONTRIBUTING.md).

Las decisiones importantes se documentarán mediante Architecture Decision
Records dentro de `docs/adr`.

## Requisitos locales

- WSL2 con Linux.
- mise.
- Java 21.
- Maven 3.9.
- Node.js 24.
- pnpm 11.
- Docker y Docker Compose.
- Git.

Las versiones principales están fijadas en `mise.toml` y `package.json`.

## Ejecutar el backend

```bash
mise install
docker compose up -d postgres
docker compose ps
mise exec -- zsh -c 'cd backend && ./mvnw test'
mise exec -- zsh -c 'cd backend && ./mvnw spring-boot:run'
```

Con la aplicación iniciada, el endpoint de salud estará disponible en
`http://localhost:8080/actuator/health`.

Las pruebas crean una instancia desechable de PostgreSQL mediante Testcontainers.
La aplicación local utiliza el servicio definido en `compose.yaml` y aplica las
migraciones de Flyway durante el arranque.

Para detener los servicios locales sin eliminar los datos:

```bash
docker compose down
```

## Seguridad

No deben guardarse en el repositorio:

- Datos personales, cuentas o transacciones reales.
- Contraseñas o tokens.
- Claves privadas.
- Credenciales de AWS.
- Archivos `.env` con secretos.
- Estado local de Terraform.

FinCore utiliza datos sintéticos y no está diseñado para procesar dinero real.

## Licencia

Este proyecto se distribuye bajo la [licencia MIT](LICENSE).

Copyright © 2026 pieersx.
