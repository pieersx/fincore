# Guía DevOps de FinCore

Esta guía explica cómo construir y ejecutar FinCore como tres contenedores:
frontend, backend y PostgreSQL. Es un entorno reproducible para desarrollo y CI;
no es todavía una configuración de producción pública.

## Arquitectura local

```text
Navegador
    |
    | http://localhost:8080
    v
Nginx sin privilegios
    |-- archivos React
    `-- /api y /actuator ---> Spring Boot sin privilegios
                                      |
                                      `-- red interna ---> PostgreSQL
```

El frontend y el backend comparten la red `app`. Solo el backend puede entrar en
la red interna `data`, donde se encuentra PostgreSQL. Los puertos se publican
únicamente en `127.0.0.1`, por lo que no quedan expuestos a toda la red local.

## Archivos y responsabilidad

- `backend/Dockerfile`: compila el JAR con un JDK y lo ejecuta con una imagen JRE más pequeña.
- `frontend/Dockerfile`: compila React con Node.js y sirve `dist` con Nginx.
- `frontend/nginx.conf`: resuelve rutas SPA y redirige API/Actuator al backend.
- `compose.yaml`: conecta servicios, volúmenes, redes y comprobaciones de salud.
- `.env.example`: documenta variables locales sin guardar secretos reales.
- `.github/workflows/container-ci.yml`: construye, escanea y prueba el stack.

Las imágenes base se fijan con versión y digest. La versión comunica qué se usa;
el digest evita que una etiqueta cambie silenciosamente. Renovar esos valores es
una tarea de mantenimiento que debe acompañarse de build, pruebas y escaneo.

## Iniciar todo con Docker Compose

Desde la raíz del repositorio:

```bash
cp .env.example .env
docker compose config --quiet
docker compose up --build --detach --wait
docker compose ps
```

Copiar `.env.example` es opcional si sirven los valores locales por defecto. El
archivo `.env` está ignorado por Git y no debe contener credenciales reales.

Direcciones disponibles:

- Aplicación completa: `http://localhost:8080`.
- Salud a través del gateway: `http://localhost:8080/actuator/health/readiness`.
- Backend directo para Swagger: `http://localhost:8081/swagger-ui.html`.

## Verificar la ejecución

```bash
curl --fail http://localhost:8080/healthz
curl --fail http://localhost:8080/actuator/health/readiness
docker compose exec backend id
docker compose exec frontend id
```

Los dos últimos comandos permiten comprobar que backend y frontend no se
ejecutan como `root`.

## Diagnóstico

```bash
docker compose ps
docker compose logs --follow backend
docker compose logs --follow frontend
docker compose logs --follow postgres
```

Si un puerto ya está ocupado, cambia `APP_PORT`, `BACKEND_PORT` o
`POSTGRES_PORT` en el `.env` local y vuelve a iniciar el stack.

## Detener o reiniciar

Detener sin borrar la base de datos:

```bash
docker compose down
```

Reconstruir después de cambiar dependencias o código:

```bash
docker compose up --build --detach --wait
```

Eliminar también la base local es una acción destructiva y solo debe realizarse
cuando se desea empezar desde cero:

```bash
docker compose down --volumes
```

## Controles de seguridad incluidos

- Etapas de build separadas de las imágenes de ejecución.
- Procesos de aplicación sin usuario root.
- Filesystems de frontend y backend en modo solo lectura, con `/tmp` temporal.
- Eliminación de capabilities Linux y `no-new-privileges`.
- Red de datos interna y puertos enlazados a localhost.
- Healthchecks para ordenar el arranque y detectar fallos.
- Cabeceras HTTP defensivas en Nginx.
- Escaneo de vulnerabilidades altas y críticas con Trivy en cada Pull Request.

Para un despliegue real aún faltan TLS, un gestor de secretos, backups probados,
registro privado de imágenes, observabilidad, infraestructura como código y una
estrategia de rollback. Esos elementos se incorporan después de consolidar este
incremento local y CI.
