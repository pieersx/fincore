# ADR 0001: iniciar FinCore como monolito modular

- Estado: Aceptado
- Fecha: 2026-08-15
- Responsable: pieersx

## Contexto

FinCore modelará operaciones financieras que requieren consistencia fuerte,
trazabilidad y transacciones ACID.

Durante la primera etapa será desarrollado por una sola persona dentro de un plan
de 10 semanas. Los límites del dominio todavía deben validarse mediante
implementación y pruebas.

Comenzar con microservicios introduciría desde el primer momento:

- Comunicación por red.
- Fallos parciales.
- Despliegues múltiples.
- Observabilidad distribuida.
- Autenticación entre servicios.
- Consistencia eventual.
- Posibles transacciones distribuidas.
- Mayor costo local y cloud.

Esa complejidad no resolvería todavía un requisito demostrado del producto.

Un monolito tradicional organizado mediante carpetas globales como
`controllers`, `services` y `repositories` tampoco protegería adecuadamente los
límites del dominio.

## Decisión

FinCore comenzará como una aplicación Spring Boot desplegable como una sola
unidad y organizada como monolito modular.

El backend inicial será un único módulo Maven. Los módulos de negocio serán
paquetes directos bajo el paquete principal:

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

Cada módulo podrá organizarse internamente mediante componentes de dominio,
aplicación e infraestructura.

La estructura podrá simplificarse cuando un módulo todavía sea pequeño. No se
crearán paquetes vacíos únicamente para completar una plantilla.

## Propiedad de los módulos

Cada módulo será propietario de:

- Sus entidades y objetos de valor.
- Sus casos de uso.
- Sus repositorios.
- Sus tablas.
- Sus migraciones.
- Sus eventos.
- Sus pruebas.

Un módulo no podrá acceder directamente a los repositorios, entidades o tablas
propiedad de otro módulo.

## Comunicación entre módulos

La comunicación síncrona utilizará interfaces públicas de aplicación.

La comunicación desacoplada utilizará eventos de dominio cuando el consumidor no
deba formar parte de la operación principal.

El paquete `shared` contendrá únicamente conceptos verdaderamente transversales.
No se utilizará como depósito de clases que no tienen un propietario claro.

## Persistencia y transacciones

La primera versión utilizará una sola instancia de PostgreSQL.

Los módulos controlarán conceptualmente sus propias tablas, aunque compartan la
misma base de datos física.

Las operaciones que involucren cuentas, transferencias y ledger podrán utilizar
una única transacción local de base de datos.

Esto permite mantener juntas las invariantes financieras antes de introducir
consistencia distribuida.

## Verificación de límites

Los límites modulares se verificarán mediante:

- Spring Modulith.
- Pruebas de arquitectura.
- Visibilidad de paquetes.
- Revisión de dependencias.
- Reglas que impidan ciclos entre módulos.

Una convención escrita sin pruebas automatizadas no se considerará una frontera
suficiente.

## Alternativas consideradas

### Microservicios desde el inicio

Rechazado porque agrega complejidad distribuida antes de validar el dominio.

### Monolito organizado por capas globales

Rechazado porque facilita dependencias entre cualquier controlador, servicio,
repositorio o entidad del sistema.

### Proyecto Maven multi-módulo desde el inicio

Pospuesto porque los límites pueden verificarse primero mediante paquetes y
Spring Modulith sin introducir complejidad adicional en el build.

Podrá reconsiderarse si existen razones para separar ciclos de compilación o
artefactos.

## Consecuencias positivas

- Las operaciones financieras pueden utilizar transacciones ACID locales.
- El sistema completo puede ejecutarse y probarse como una unidad.
- El desarrollo local necesita menos infraestructura.
- Los límites pueden evolucionar a partir de evidencia.
- La extracción futura de módulos puede justificarse y documentarse.

## Consecuencias negativas

- Todos los módulos comparten proceso y ciclo de despliegue.
- Un fallo no aislado puede afectar a toda la aplicación.
- No existe escalamiento independiente por módulo.
- Los límites dependen de disciplina y pruebas automatizadas.
- La base de datos física es compartida inicialmente.

## Criterios para reconsiderar la decisión

Un módulo podrá evaluarse como servicio independiente cuando exista evidencia de:

- Necesidad de escalamiento independiente.
- Ciclo de despliegue diferente.
- Requisitos de disponibilidad diferentes.
- Necesidad real de aislamiento de fallos.
- Propiedad por un equipo independiente.
- Tecnología o modelo de datos justificadamente diferente.

La moda tecnológica o el deseo de mencionar microservicios en el CV no serán
razones suficientes.

El primer candidato previsto para una posible extracción es `notifications`,
porque sus fallos no deben revertir transferencias y su comunicación es
naturalmente asíncrona.

## Resultado esperado

FinCore conservará la simplicidad operativa de un monolito mientras aplica
límites internos suficientemente fuertes para permitir una evolución futura.
