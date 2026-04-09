# Arquitectura de KeyGo Server

> Documento resumido. La fuente de verdad arquitectónica es [`docs/design/ARCHITECTURE.md`](docs/design/ARCHITECTURE.md).

## Resumen

- Monorepo Maven multi-módulo.
- Java 21 + Spring Boot 4.x.
- Arquitectura hexagonal / ports and adapters.
- `context-path` activo: `/keygo-server`.
- Persistencia principal: PostgreSQL + JPA + Flyway en `keygo-supabase`.
- Migraciones activas en el repo: `V1` a `V33`. La siguiente debe ser `V34__...`.

## Modulos

| Modulo | Rol |
|---|---|
| `keygo-domain` | Dominio puro, sin Spring ni dependencias internas |
| `keygo-app` | Use cases y puertos de salida |
| `keygo-infra` | Adaptadores transversales: JWT, JWKS, PKCE |
| `keygo-api` | Controllers REST, DTOs, errores y OpenAPI |
| `keygo-supabase` | JPA, Flyway, repositorios y PostgreSQL |
| `keygo-run` | Main, wiring, seguridad y configuración |
| `keygo-bom` | Gestión de versiones |
| `keygo-common` | Utilidades compartidas |

## Flujo de alto nivel

```text
Client
  -> keygo-run filtros/config
  -> keygo-api controller
  -> keygo-app use case
  -> port OUT
  -> keygo-supabase / keygo-infra adapter
  <- BaseResponse<T> o respuesta RFC
```

## Seguridad vigente

- `/api/**` protegido con `Authorization: Bearer <jwt>` según categoría de ruta.
- Autorizacion por endpoint con `@PreAuthorize`.
- Aislamiento tenant para roles acotados mediante evaluacion de `tenant_slug`.
- Rutas públicas y excepciones documentadas en [`docs/api/BOOTSTRAP_FILTER.md`](docs/api/BOOTSTRAP_FILTER.md).

## Referencias

- [`docs/design/ARCHITECTURE.md`](docs/design/ARCHITECTURE.md)
- [`docs/api/AUTH_FLOW.md`](docs/api/AUTH_FLOW.md)
- [`docs/data/MIGRATIONS.md`](docs/data/MIGRATIONS.md)
- [`AGENTS.md`](AGENTS.md)
