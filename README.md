# keygo-server

Backend IAM multi-tenant para autenticacion, autorizacion y gestion de identidades sobre Java 21 + Spring Boot 4.x.

> Source of truth para documentacion: [`docs/README.md`](docs/README.md)

## Estado actual

- Monorepo Maven multi-modulo con arquitectura hexagonal.
- Seguridad admin actual: `Authorization: Bearer <jwt>` en rutas protegidas.
- Modulos activos: `keygo-domain`, `keygo-app`, `keygo-infra`, `keygo-api`, `keygo-supabase`, `keygo-run`, `keygo-bom`.
- Capacidades implementadas a nivel repo: OAuth2/OIDC base, tenants, apps, usuarios, account self-service, RBAC platform/tenant y billing de plataforma.
- Migraciones Flyway actuales: `V1` a `V33`. La siguiente migracion debe ser `V34__...`.

## Estructura

```text
keygo-server/
├── keygo-domain/     # Dominio puro, sin Spring
├── keygo-app/        # Use cases + puertos
├── keygo-infra/      # Adaptadores transversales
├── keygo-api/        # Controllers REST + DTOs
├── keygo-supabase/   # JPA, Flyway y PostgreSQL
├── keygo-run/        # Main, wiring y application.yml
├── keygo-bom/        # Gestion de versiones
├── docs/             # Documentacion canonica
└── pom.xml           # Parent POM
```

## Requisitos

- Java 21
- Maven Wrapper incluido en el repo (`./mvnw`)
- Docker opcional para DB local

## Quick Start

### 1. Build

```bash
./mvnw clean package
```

### 2. Levantar DB local

```bash
./docs/scripts/db/start.sh
```

### 3. Activar ambiente local

```bash
./docs/scripts/switch-env.sh local
```

### 4. Ejecutar la app

```bash
./mvnw spring-boot:run -pl keygo-run
```

### 5. Verificar

```bash
curl http://localhost:8080/keygo-server/actuator/health
curl http://localhost:8080/keygo-server/swagger-ui/index.html
```

## URLs locales

- Base URL: `http://localhost:8080/keygo-server`
- Swagger UI: `http://localhost:8080/keygo-server/swagger-ui/index.html`
- OpenAPI: `http://localhost:8080/keygo-server/v3/api-docs`
- Health: `http://localhost:8080/keygo-server/actuator/health`

## Seguridad y API

- El `context-path` activo es `/keygo-server`.
- Las rutas protegidas usan `Authorization: Bearer <jwt>`.
- Los detalles de rutas publicas/protegidas viven en [`docs/api/BOOTSTRAP_FILTER.md`](docs/api/BOOTSTRAP_FILTER.md).
- La referencia humana de endpoints vive en [`docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`](docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md).
- El contrato runtime lo expone OpenAPI en `/v3/api-docs`.

## Documentacion clave

- [`docs/README.md`](docs/README.md): indice documental canonico y politica de ubicacion.
- [`ARCHITECTURE.md`](ARCHITECTURE.md): resumen arquitectonico.
- [`AI_CONTEXT.md`](AI_CONTEXT.md): snapshot operativo para agentes.
- [`AGENTS.md`](AGENTS.md): quick-start tecnico para agentes AI.
- [`ROADMAP.md`](ROADMAP.md): propuestas activas y completadas.
- [`docs/development/ENVIRONMENT_SETUP.md`](docs/development/ENVIRONMENT_SETUP.md): setup local y variables de entorno.
- [`docs/operations/DOCKER.md`](docs/operations/DOCKER.md): Docker y runtime local.

## Comandos utiles

```bash
./mvnw test
./mvnw verify
./mvnw -pl keygo-api test
./docs/scripts/quick-start.sh
./docs/scripts/keygo.sh
```

## Contribucion

Las politicas publicas del repositorio viven en:

- [`CONTRIBUTING.md`](CONTRIBUTING.md)
- [`SECURITY.md`](SECURITY.md)
- [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md)
- [`CHANGELOG.md`](CHANGELOG.md)

## Nota para agentes AI

Los wrappers de herramienta permanecen en raiz por compatibilidad:

- [`CLAUDE.md`](CLAUDE.md)
- [`.github/copilot-instructions.md`](.github/copilot-instructions.md)

La politica operativa compartida queda centralizada en [`docs/ai/AGENT_OPERATIONS.md`](docs/ai/AGENT_OPERATIONS.md).
