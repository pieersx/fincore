# Plan del proyecto FinCore

- Estado: Aprobado
- Fecha de inicio: 2026-08-15
- Duración: 10 semanas
- Enfoque principal: Ingeniería backend
- Enfoque secundario: DevOps e ingeniería cloud
- Enfoque complementario: Ingeniería full-stack

## 1. Propósito

FinCore es un simulador educativo de core financiero creado para demostrar
ingeniería backend, arquitectura de software, pruebas, seguridad, DevOps y
prácticas cloud.

El proyecto comenzará como un monolito modular. Evolucionará hacia eventos
asíncronos, un servicio de notificaciones desplegable independientemente,
infraestructura en AWS y Kubernetes únicamente después de completar la
arquitectura más sencilla y poder demostrar sus limitaciones.

FinCore servirá como evidencia para postular a prácticas preprofesionales y
puestos junior de ingeniería de software, especialmente en backend Java y
DevOps en Lima, Perú.

## 2. Seguridad y autenticidad

FinCore utilizará exclusivamente datos sintéticos. Nunca debe contener datos
reales como:

- Identidades de clientes o documentos oficiales de identidad.
- Cuentas bancarias, tarjetas, credenciales o tokens de autenticación.
- Transacciones financieras o información confidencial de empresas.
- Credenciales cloud, claves privadas o secretos de producción.

El realismo provendrá de los controles de ingeniería y no de utilizar datos
reales. Estos controles incluyen:

- Registros inmutables de contabilidad de doble entrada.
- Transacciones ACID.
- Operaciones idempotentes.
- Control de concurrencia.
- Restricciones e índices en la base de datos.
- Autorización y trazabilidad de auditoría.
- Conciliación.
- Gestión segura de secretos.
- Pruebas de invariantes financieras y escenarios de fallo.
- Logs, métricas, trazas, alertas y manuales operativos.

FinCore se describirá como un simulador educativo inspirado en prácticas de
producción. No se presentará como banco, procesador de pagos, software que cumple
regulaciones financieras ni sistema apto para administrar dinero real.

## 3. Estrategia de arquitectura

El sistema inicial será un único backend desplegable dividido en módulos de
negocio explícitos:

```text
React y TypeScript
        |
        | HTTPS y REST
        v
Monolito modular Spring Boot
|-- identity
|-- customers
|-- accounts
|-- beneficiaries
|-- transfers
|-- ledger
|-- audit
`-- shared
        |
        v
PostgreSQL
```

Los módulos se organizarán por capacidad de negocio y no mediante capas técnicas
globales. Un módulo puede exponer interfaces de aplicación y eventos, pero los
detalles internos de su dominio e infraestructura permanecerán privados.

Spring Modulith y las pruebas de arquitectura verificarán estos límites.

## 4. Stack tecnológico

### 4.1 Backend

| Tecnología | Política de versión | Propósito |
| --- | --- | --- |
| Java | 21 LTS, fijado con mise | Lenguaje principal |
| Spring Boot | 4.1.x | Plataforma de la aplicación |
| Spring Modulith | 2.1.x | Verificación y documentación modular |
| Maven Wrapper | 3.9.x | Builds reproducibles |
| Spring Web MVC | Administrado por Spring Boot | API REST |
| Spring Data JPA | Administrado por Spring Boot | Persistencia |
| Hibernate | Administrado por Spring Boot | ORM |
| Spring Security | Administrado por Spring Boot | Autenticación y autorización |
| Bean Validation | Administrado por Spring Boot | Validación de entradas |
| Flyway | Versión compatible con Boot | Migraciones SQL |
| PostgreSQL JDBC | Administrado por Spring Boot | Conexión con la base de datos |
| Spring Boot Actuator | Administrado por Spring Boot | Salud y métricas |
| springdoc-openapi | Versión compatible con Boot 4 | Documentación OpenAPI |

Spring Web MVC será el stack web inicial. No se combinará WebFlux con JPA
bloqueante solamente para afirmar que el sistema es reactivo. Posteriormente
podrá añadirse un ejercicio reactivo independiente si existe un caso de uso
medible.

Las dependencias administradas por los BOM de Spring Boot y Spring Modulith no
recibirán versiones independientes sin una razón documentada.

### 4.2 Base de datos y representación financiera

| Tecnología | Política de versión | Propósito |
| --- | --- | --- |
| PostgreSQL | 18.x | Base de datos transaccional |
| Flyway | Versión compatible con Boot | Historial del esquema |
| Testcontainers PostgreSQL | Versión estable compatible | Pruebas de integración |

Reglas financieras:

- Los importes monetarios en Java utilizarán `BigDecimal`, nunca `float` ni
  `double`.
- Los importes monetarios en PostgreSQL utilizarán una definición explícita de
  `numeric`.
- Cada importe tendrá un código de moneda ISO 4217.
- Las monedas iniciales serán PEN y USD.
- La primera versión no realizará cambio de divisas.
- Los movimientos del ledger serán append-only: solo se agregarán registros.
- Cada asiento contable deberá cuadrar independientemente dentro de una moneda.
- Las restricciones de base de datos protegerán invariantes además de las
  validaciones de la aplicación.

### 4.3 Frontend

| Tecnología | Política de versión | Propósito |
| --- | --- | --- |
| Node.js | 24 LTS, fijado con mise | Runtime de herramientas |
| pnpm | 11.22.0, fijado en `package.json` | Gestión de dependencias |
| React | 19.2.x | Interfaz de usuario |
| TypeScript | 7.x | Tipado estático |
| Vite | 8.x | Desarrollo y build de producción |
| React Router | Versión estable compatible | Navegación |
| TanStack Query | Versión estable compatible | Estado del servidor |
| React Hook Form | Versión estable compatible | Estado de formularios |
| Zod | Versión estable compatible | Esquemas del cliente |
| Tailwind CSS | 4.x | Estilos |

Redux no se incorporará inicialmente. Los datos remotos pertenecerán a TanStack
Query y el estado pequeño de la interfaz permanecerá local a React.

### 4.4 Pruebas

Pruebas del backend:

- JUnit.
- AssertJ.
- Mockito cuando se justifique utilizar un test double.
- ArchUnit.
- Spring Modulith Test.
- Testcontainers.
- REST Assured.
- Pruebas de concurrencia e idempotencia.
- Pruebas de invariantes financieras y conciliación.

Pruebas del frontend:

- Vitest.
- React Testing Library.
- Mock Service Worker.
- Playwright.
- Comprobaciones automáticas de accesibilidad con axe.

El porcentaje de cobertura será una señal de diagnóstico, no el objetivo. Los
comportamientos críticos y las rutas de fallo deberán estar cubiertos sin
importar el porcentaje global.

### 4.5 Seguridad

La primera versión utilizará:

- Spring Security.
- Sesiones administradas por el servidor.
- Cookies con atributos `HttpOnly`, `Secure` y un valor `SameSite` apropiado.
- Protección CSRF.
- Argon2 o bcrypt para el hash de contraseñas.
- Roles `CUSTOMER`, `ANALYST` y `ADMIN`.
- Verificación de propiedad de recursos.
- Rate limiting en endpoints sensibles cuando se justifique.
- Auditoría de operaciones financieras y sensibles para la seguridad.
- Secretos fuera del control de versiones.

OAuth 2.1, OpenID Connect y Keycloak se introducirán después como evolución de
identidad. FinCore no implementará un protocolo propio de autenticación ni
diseñará manualmente la criptografía de tokens.

### 4.6 Calidad y documentación

- Conventional Commits.
- Maven Enforcer.
- Spotless.
- ArchUnit.
- ESLint y Prettier.
- Architecture Decision Records (ADR).
- OpenAPI.
- Diagramas de arquitectura C4.
- Modelo de amenazas.
- Manuales operativos o runbooks.
- Tags de versión semántica y changelog.

### 4.7 DevOps y cadena de suministro

- Dockerfiles multi-stage.
- Docker Compose para desarrollo local.
- GitHub Actions.
- Renovate para actualizar dependencias.
- CodeQL para análisis estático.
- Trivy para dependencias, imágenes e infraestructura como código.
- Generación de Software Bill of Materials (SBOM).
- Firma de imágenes como ejercicio posterior de hardening.
- Comprobaciones de health, readiness y liveness.
- Procedimientos documentados de despliegue y rollback.

### 4.8 Observabilidad

- Logs estructurados en JSON.
- Identificadores de correlación y causación.
- Micrometer.
- OpenTelemetry.
- Prometheus.
- Grafana.
- Tempo.
- Loki.
- Alertas técnicas y de negocio.

Las métricas iniciales de negocio incluirán:

- Transferencias iniciadas, completadas y rechazadas.
- Repeticiones de solicitudes idempotentes.
- Diferencias detectadas durante la conciliación.
- Latencia de transferencias.
- Eventos de outbox pendientes, publicados, reintentados y fallidos.

### 4.9 Eventos

La etapa orientada a eventos incorporará:

- Primero, eventos de dominio dentro del proceso.
- Transactional outbox en PostgreSQL.
- Apache Kafka en modo KRaft.
- Consumidores idempotentes.
- Reintentos con exponential backoff limitado.
- Dead-letter topics.
- Versionado de esquemas de eventos.
- Identificadores de correlación y causación.

### 4.10 AWS

La arquitectura objetivo de referencia incluye:

- Amazon S3 y CloudFront para el frontend.
- Amazon ECR para imágenes de contenedores.
- Amazon ECS Fargate para el backend.
- Application Load Balancer.
- Amazon RDS para PostgreSQL.
- Amazon S3 para documentos generados.
- AWS Secrets Manager o Systems Manager Parameter Store.
- CloudWatch.
- AWS Certificate Manager.
- Route 53 únicamente si se compra un dominio.
- Políticas IAM de mínimo privilegio.
- AWS Budgets y alertas de costos.
- Terraform para crear infraestructura reproducible.

La arquitectura de referencia y el entorno económico de demostración pueden ser
diferentes. No se creará ningún recurso en AWS antes de configurar controles de
facturación. Los recursos costosos se crearán temporalmente y se destruirán
después de recopilar la evidencia necesaria.

### 4.11 Kubernetes

- Minikube para aprendizaje local.
- Deployments y Services.
- Ingress.
- ConfigMaps y Secrets.
- Probes de startup, readiness y liveness.
- Requests y limits de recursos.
- Horizontal Pod Autoscaler.
- PodDisruptionBudget.
- NetworkPolicy.
- Helm.
- Rolling updates y rollbacks.
- Despliegue temporal en EKS solamente si el presupuesto lo permite.

## 5. Plan de ejecución de 10 semanas

### Semana 1: fundamentos y walking skeleton

Entregables:

- Estándares del repositorio y documentación inicial.
- Alcance del producto y glosario.
- ADR iniciales.
- Backend generado con Spring Boot.
- Frontend generado con React y TypeScript.
- PostgreSQL ejecutándose mediante Docker Compose.
- Primera migración de Flyway.
- Endpoint de salud del backend.
- Primera prueba del backend y del frontend.
- Procedimiento documentado para iniciar el entorno local.

Criterios de salida:

- Un nuevo colaborador puede clonar el repositorio e iniciar todo el entorno
  local.
- React, Spring Boot y PostgreSQL se comunican correctamente.
- Todas las pruebas guardadas en el repositorio pasan.

### Semana 2: identidad, clientes y autorización

Entregables:

- Registro de cuentas de demostración.
- Inicio y cierre de sesión.
- Manejo seguro de sesiones.
- Roles y permisos.
- Perfiles sintéticos de clientes.
- Verificación de propiedad de recursos.
- Registros de auditoría de autenticación y autorización.
- Pruebas de integración de seguridad.

Criterios de salida:

- Un cliente no puede acceder a los recursos de otro cliente.
- Una solicitud no autenticada no puede acceder a operaciones protegidas.
- Las restricciones por roles se demuestran mediante pruebas automatizadas.

### Semana 3: cuentas y ledger de doble entrada

Entregables:

- Cuentas en PEN y USD.
- Estados del ciclo de vida de una cuenta.
- Modelo de asientos y movimientos del ledger.
- Comportamiento append-only del ledger.
- Cálculo de saldo o proyección de saldo verificable.
- Restricciones e índices de base de datos.
- Cuentas y saldos sintéticos reproducibles.
- Servicio de conciliación y sus pruebas.

Criterios de salida:

- Cada asiento contable cuadra dentro de su moneda.
- El historial del ledger no puede modificarse ni eliminarse mediante la
  aplicación.
- El saldo proyectado de una cuenta coincide con el ledger.

### Semana 4: beneficiarios, transferencias y flujo de producto

Entregables:

- Gestión de beneficiarios.
- Transferencias internas en la misma moneda.
- Manejo de `Idempotency-Key` persistido en PostgreSQL.
- Bloqueo de cuentas y control de concurrencia.
- Reglas de saldo insuficiente y límites operativos.
- Historial paginado de movimientos.
- Flujo web de transferencias y movimientos para el cliente.
- Pruebas de concurrencia y solicitudes duplicadas.

Criterios de salida:

- Las solicitudes concurrentes no pueden producir un saldo negativo.
- Reutilizar una clave de idempotencia no puede duplicar una transferencia.
- Una transferencia completada genera asientos balanceados atómicamente.

### Semana 5: producto completo, accesibilidad y documentos

Entregables:

- Dashboard del cliente.
- Vistas para analista y administrador.
- Búsqueda y filtros.
- Comprobante PDF de transferencia.
- Estados de carga, vacío, éxito y error.
- Navegación accesible mediante teclado y lector de pantalla.
- Escenario sintético completo para demostraciones.
- Pruebas end-to-end con Playwright.

Criterios de salida:

- El recorrido principal de negocio pasa de extremo a extremo.
- La aplicación puede demostrarse sin modificar manualmente la base de datos.

### Semana 6: calidad, seguridad, CI/CD y `v1.0.0`

Entregables:

- Modelo de amenazas.
- Headers de seguridad y revisión de configuración segura.
- Suites de pruebas unitarias, modulares, de integración, arquitectura y E2E.
- Jobs de CodeQL, Trivy, formato, pruebas y build en GitHub Actions.
- Imágenes multi-stage reproducibles.
- Documentación OpenAPI.
- Changelog y proceso de releases.
- Tag `v1.0.0` después de superar todos los controles de calidad.

Criterios de salida:

- CI pasa desde un clon limpio.
- No permanece ninguna vulnerabilidad crítica conocida sin explicación.
- El monolito modular está completo y puede desplegarse.

Hito profesional:

- Comenzar las postulaciones activas a prácticas y puestos junior como máximo en
  esta etapa.

### Semana 7: observabilidad, Terraform y AWS

Entregables:

- Logs estructurados, correlation IDs, métricas y trazas.
- Stack local de observabilidad.
- Presupuesto y alertas de facturación de AWS.
- Módulos de Terraform y decisión sobre el estado remoto.
- Despliegue económico en AWS.
- TLS, secretos, dashboards, runbook de despliegue y prueba de rollback.

Criterios de salida:

- La infraestructura puede reconstruirse mediante comandos documentados.
- Un despliegue fallido puede revertirse.
- Los costos y procedimientos de limpieza están documentados.

### Semana 8: eventos de dominio, outbox y Kafka

Entregables:

- Eventos internos de dominio.
- Transactional outbox en PostgreSQL.
- Kafka ejecutándose localmente.
- Publicador de eventos.
- Consumidores idempotentes.
- Reintentos y dead-letter topics.
- Esquemas y observabilidad de eventos.

Criterios de salida:

- Confirmar una transferencia y su registro de outbox es una operación atómica.
- Una caída temporal de Kafka no pierde eventos confirmados.
- Una entrega duplicada no duplica los efectos del consumidor.

### Semana 9: extracción del servicio de notificaciones

Entregables:

- ADR que justifique la extracción.
- Servicio de notificaciones desplegable independientemente.
- Datos controlados por el servicio.
- Contrato Kafka entre el core y notificaciones.
- Aislamiento de fallos y pruebas de contrato del consumidor.
- CI e imagen de contenedor independientes.

Criterios de salida:

- Un fallo de notificaciones nunca revierte una transferencia.
- El servicio no consulta tablas de la base de datos del core.
- El servicio puede desplegarse y revertirse independientemente.

### Semana 10: Kubernetes y presentación del portafolio

Entregables:

- Despliegue en Minikube.
- Helm chart.
- Probes, controles de recursos, escalamiento y políticas de red.
- Demostración de rolling update y rollback.
- Despliegue temporal en EKS solo si se justifica económicamente.
- Documentación final de arquitectura.
- Instrucciones para la demo pública.
- Caso de estudio para el portafolio.
- Preguntas de entrevista y defensa de arquitectura.
- Puntos para el CV basados en resultados verificables.

Criterios de salida:

- El sistema sobrevive al reinicio controlado de un pod.
- El despliegue y el rollback están demostrados.
- Cada afirmación del portafolio enlaza código, pruebas, documentación o
  mediciones.

## 6. Definition of Done de cada incremento

Un incremento se considera terminado solamente cuando:

- Su comportamiento y criterios de aceptación están documentados.
- El código de producción está implementado.
- Las pruebas automatizadas pertinentes pasan.
- Los cambios de base de datos utilizan una migración forward-only.
- Se consideraron la seguridad y los escenarios de fallo.
- Los logs no exponen secretos ni información sensible.
- La documentación representa el comportamiento actual.
- La aplicación continúa ejecutándose desde un clon limpio.
- El commit tiene un propósito concreto y puede explicarse.

## 7. Estrategia de Git y releases

- `main` debe permanecer compilable y ejecutable.
- Se usarán ramas de corta duración cuando la protección de GitHub esté activa.
- Los pull requests serán suficientemente pequeños para revisarlos de manera
  significativa.
- Los commits seguirán Conventional Commits.
- No se versionarán archivos generados, secretos, resultados del build ni estado
  local.
- Las versiones se etiquetarán únicamente después de superar sus controles.

Hitos previstos:

- `v0.1.0`: fundamentos del repositorio y arquitectura.
- `v0.2.0`: walking skeleton.
- `v0.3.0`: identidad y clientes.
- `v0.4.0`: cuentas y ledger.
- `v0.5.0`: transferencias.
- `v1.0.0`: producto completo como monolito modular.
- `v1.1.0`: AWS y observabilidad.
- `v2.0.0`: outbox y Kafka.
- `v2.1.0`: servicio de notificaciones.
- `v3.0.0`: despliegue en Kubernetes.

## 8. Tecnologías pospuestas deliberadamente

Las siguientes tecnologías no forman parte del producto inicial:

- Microservicios.
- Kubernetes.
- Kafka.
- Redis.
- MongoDB.
- GraphQL.
- gRPC.
- API Gateway.
- WebFlux.
- Sagas distribuidas.
- Machine learning.
- Sistemas de pago reales.
- Integraciones con Visa, Mastercard o redes interbancarias.

Solo podrán incorporarse cuando un requisito documentado justifique su costo y
complejidad.

## 9. Política de aprendizaje y evidencia

El objetivo no es acumular tecnologías. Para cada implementación importante, el
desarrollador deberá poder explicar:

- Qué problema resuelve.
- Por qué fue elegida.
- Qué alternativas más sencillas se consideraron.
- Cuáles son sus modos de fallo.
- Cómo se prueba.
- Cómo se observa en producción.
- Cómo se despliega y se revierte.

El progreso se evaluará mediante software funcional, pruebas, documentación y
resultados medidos, no mediante niveles de habilidad autoasignados.

## 10. Referencias

- [Proyecto Spring Boot](https://spring.io/projects/spring-boot)
- [Referencia de Spring Modulith](https://docs.spring.io/spring-modulith/reference/)
- [Versiones de React](https://react.dev/versions)
- [Versiones de Vite](https://vite.dev/releases)
- [Versiones de Node.js](https://nodejs.org/en/about/previous-releases)
- [Versiones de PostgreSQL](https://www.postgresql.org/docs/release/)
- Documentación de AWS Free Tier:
  <https://docs.aws.amazon.com/awsaccountbilling/latest/aboutv2/free-tier.html>
