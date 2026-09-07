# keygo-server

Backend IAM multi-tenant para autenticación, autorización y gestión de identidades sobre Java 21 + Spring Boot 4.x.

> Target de producto vigente: `INIT-KEYGO-001 / CAP-IAM-001 — Identity, Tenancy & Access Management`. Las capacidades comerciales existentes en este repositorio están en proceso de separación gobernada hacia `CAP-SUB-001` y `CAP-PAY-001`; su presencia actual no implica ownership objetivo de KeyGo.

## Desarrollo local — camino canónico

KeyGo adopta el contrato local-first de ADÜMÜN. El flujo recomendado es:

```bash
make bootstrap
make deps
make up
make test
make doctor
```

- `make bootstrap` restaura `.env` desde `ADUMUN_ENV_HOME` cuando existe o lo inicializa desde `envs/.env.example` sin sobrescribir uno existente.
- `make up` levanta PostgreSQL + MailHog mediante `compose.yml`.
- La aplicación Spring Boot puede ejecutarse en el host para mantener un loop de desarrollo rápido.
- `make validate` ejecuta `mvn verify` y el doctor del repositorio.
- El ambiente compartido `develop` es para integración; no reemplaza este camino local.

Usa `make help` para ver todos los comandos disponibles.

## Estado arquitectónico

- Maven multi-módulo con DDD + arquitectura hexagonal como baseline.
- Seguridad con `Authorization: Bearer <jwt>` para rutas protegidas.
- Núcleo IAM: identidades, tenants, memberships, aplicaciones cliente, roles/permisos, OAuth2/OIDC, sesiones, tokens, JWKS y administración.
- Billing/subscription/payment permanecen temporalmente en el runtime como implementación legacy/transicional mientras se introducen puertos y límites de capability explícitos.

## Estructura principal

```text
keygo-server/
├── keygo-domain
├── keygo-app
├── keygo-infra
├── keygo-api
├── keygo-supabase
├── keygo-run
├── keygo-bom
├── docs
├── scripts
└── compose.yml
```

## Documentación disponible en este repositorio

- Portal técnico: [`docs/index.html`](docs/index.html)
- Quickstart: [`docs/quickstart.html`](docs/quickstart.html)
- Overview: [`docs/overview.html`](docs/overview.html)
- Guías para integradores: [`docs/integrators/`](docs/integrators/)
- Extensiones/referencias: [`docs/extenders/`](docs/extenders/)

La documentación de producto/SDLC de mayor amplitud se mantiene además en `cmartinezs/keygo-docs` durante la reconciliación documental.

## URLs locales

- Base URL: `http://localhost:8080/keygo-server`
- Swagger UI: `http://localhost:8080/keygo-server/swagger-ui/index.html`
- OpenAPI: `http://localhost:8080/keygo-server/v3/api-docs`
- Health: `http://localhost:8080/keygo-server/actuator/health`
- MailHog UI: `http://localhost:8025`

## Ambientes

Vocabulario ADÜMÜN objetivo:

```text
local
 develop
 test
 prod
```

El repositorio todavía contiene referencias legacy a `desa`; su convergencia a `develop` se realizará de forma incremental para no romper scripts/configuración existentes.

## Contribución y seguridad

- [`CONTRIBUTING.md`](CONTRIBUTING.md)
- [`SECURITY.md`](SECURITY.md)
- [`CODE_OF_CONDUCT.md`](CODE_OF_CONDUCT.md)
- [`CHANGELOG.md`](CHANGELOG.md)
