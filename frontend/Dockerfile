# syntax=docker/dockerfile:1.7@sha256:a57df69d0ea827fb7266491f2813635de6f17269be881f696fbfdf2d83dda33e

# Vite necesita Node.js y pnpm solamente durante la compilación.
FROM node:24.19.0-alpine3.23@sha256:244cc2b53f46f9e876304391d17682b0ddae9ac33491f4857e25e35a36ba7995 AS build

ENV PNPM_HOME=/pnpm
ENV PATH=$PNPM_HOME:$PATH

RUN corepack enable && corepack prepare pnpm@11.22.0 --activate

WORKDIR /workspace

# Copiar primero los manifiestos permite reutilizar la capa de dependencias.
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY frontend/package.json frontend/package.json
RUN --mount=type=cache,id=pnpm,target=/pnpm/store \
    pnpm install --frozen-lockfile

COPY frontend frontend
RUN pnpm build:frontend

# Nginx sirve los archivos estáticos y actúa como gateway del backend.
FROM ghcr.io/nginx/nginx-unprivileged:1.31.3-alpine3.24@sha256:f972e5322b9797dc2a6b830030094426437b1ae7032e4644496395336ac6fdac AS runtime

# El digest base aún trae OpenSSL 3.5.7; fijamos la revisión que corrige CVE-2026-14456.
USER root
RUN apk add --no-cache --upgrade libcrypto3=3.5.8-r0 libssl3=3.5.8-r0
USER 101

COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /workspace/frontend/dist /usr/share/nginx/html

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --start-period=5s --retries=5 \
    CMD wget --quiet --spider http://127.0.0.1:8080/healthz || exit 1
