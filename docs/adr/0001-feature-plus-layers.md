# ADR 0001: iniciar FinCore con Feature + Layers

- Estado: Aceptado
- Fecha: 2026-08-15
- Actualizado: 2026-08-21
- Responsable: pieersx

## Contexto

FinCore modela operaciones financieras que requieren consistencia fuerte,
trazabilidad y transacciones ACID. Durante la primera etapa será desarrollado
por una sola persona y desplegado como una única aplicación Spring Boot.

Comenzar con microservicios agregaría comunicación por red, fallos parciales,
despliegues múltiples y consistencia distribuida sin resolver todavía un
requisito demostrado del producto.

Una estructura formada por paquetes globales como `controllers`, `services` y
`repositories` también dificultaría localizar todos los elementos de una misma
funcionalidad a medida que crezca el código.

## Decisión

FinCore utilizará un monolito organizado mediante **Feature + Layers**. El
primer nivel separa las funcionalidades de negocio y el segundo nivel separa
las responsabilidades técnicas:

```text
com.fincore
|-- identity
|   |-- controller
|   |-- dto
|   |-- entity
|   |-- mapper
|   |-- repository
|   `-- service
|-- customers
|-- accounts
|-- beneficiaries
|-- transfers
|-- ledger
|-- audit
|-- onboarding
`-- shared
    |-- config
    |-- dto
    |-- exception
    |-- model
    `-- security
```

No se crearán carpetas vacías solo para completar la plantilla. Una feature
tendrá `mapper`, `repository` o `entity` únicamente cuando exista esa
responsabilidad.

La dirección habitual de dependencias será:

```text
Controller -> Service -> Repository -> PostgreSQL
      |           |
      v           v
     DTO       Entity + Mapper
```

Los controllers no accederán a repositories. Las entidades JPA no se devolverán
directamente desde la API. Los services serán responsables de los casos de uso,
las reglas y los límites transaccionales.

## Persistencia y transacciones

La primera versión utilizará una instancia de PostgreSQL y transacciones locales.
Las operaciones que involucren cuentas, transferencias y ledger podrán confirmar
o revertir todos sus cambios de forma atómica.

## Alternativas consideradas

### Capas globales

Rechazada porque un paquete global por capa mezcla funcionalidades diferentes y
se vuelve difícil de navegar cuando crece el proyecto.

### Microservicios desde el inicio

Rechazados porque introducen complejidad distribuida antes de validar el dominio
y completar una aplicación desplegable.

## Consecuencias

- La estructura resulta familiar para equipos que desarrollan con Spring Boot.
- Cada funcionalidad puede recorrerse desde HTTP hasta la base de datos.
- Las transacciones financieras permanecen locales y consistentes.
- La separación depende de convenciones, revisión y pruebas.
- Una futura extracción a microservicios requerirá analizar dependencias y datos,
  no solamente mover carpetas.

## Criterios para evolucionar

Una funcionalidad podrá evaluarse como servicio independiente cuando exista una
necesidad demostrada de escalamiento, despliegue, disponibilidad, aislamiento de
fallos o propiedad por un equipo diferente. La moda tecnológica no será una
razón suficiente.
