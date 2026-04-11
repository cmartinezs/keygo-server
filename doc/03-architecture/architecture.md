# Arquitectura de KeyGo Server

Documento canónico de arquitectura técnica del backend.

## Objetivo

KeyGo Server es el backend IAM del proyecto: autentica usuarios y aplicaciones, emite tokens, resuelve tenancy y expone APIs administrativas y self-service sobre un monolito modular.

## Principios

- Hexagonal / ports and adapters.
- Dominio puro en `keygo-domain`.
- Infraestructura intercambiable detrás de puertos.
- Contratos HTTP explícitos y versionados.
- Multi-tenancy desde el núcleo del modelo.

## Stack

- Java 21
- Spring Boot 4.x
- Jackson 3
- Spring Data JPA
- Flyway
- PostgreSQL

## Módulos

| Módulo | Responsabilidad |
|---|---|
| `keygo-domain` | Dominio, invariantes, entidades y value objects |
| `keygo-app` | Use cases, comandos, queries y puertos OUT |
| `keygo-infra` | JWT signer, JWKS builder, PKCE verifier y adaptadores comunes |
| `keygo-api` | Controllers, DTOs, errores, OpenAPI y envelope REST |
| `keygo-supabase` | Persistencia JPA, entidades, repositorios y migraciones Flyway |
| `keygo-run` | Spring Boot main, wiring, filtros, seguridad y configuración |
| `keygo-bom` | Gestión centralizada de versiones |
| `keygo-common` | Utilidades compartidas |

## Regla de dependencias

```text
keygo-domain
  <- keygo-app
     <- keygo-infra
     <- keygo-api
     <- keygo-supabase
        <- keygo-run
```

Regla crítica: `keygo-domain` no depende de Spring ni de módulos internos.

## Flujo de request

```text
HTTP request
  -> filtros y seguridad en keygo-run
  -> controller en keygo-api
  -> use case en keygo-app
  -> puerto OUT
  -> adapter en keygo-supabase o keygo-infra
  <- resultado de dominio
  <- BaseResponse<T> o respuesta RFC
```

## Seguridad vigente

- `context-path`: `/keygo-server`
- Las rutas administrativas protegidas usan `Authorization: Bearer <jwt>`.
- El claim `roles` alimenta authorities Spring Security.
- `@PreAuthorize` decide acceso por endpoint.
- El aislamiento tenant para roles acotados se valida contra `tenant_slug` o `iss`.
- Las excepciones de rutas públicas están documentadas en [`security/bootstrap-filter.md`](security/bootstrap-filter.md).

## Superficie funcional actual

El backend ya cubre:

- OAuth2/OIDC base para tenants
- login y token exchange de plataforma
- tenants, apps y usuarios
- account self-service
- RBAC platform y tenant
- billing de plataforma y contratos base

## Persistencia

- `keygo-supabase` aloja entidades JPA y migraciones.
- `ddl-auto: validate`; Flyway es la fuente de verdad del schema.
- Migraciones presentes en el repo: `V1` a `V33`.
- La siguiente migración debe ser `V34__...`.

## Convenciones técnicas clave

- Envelope REST: `BaseResponse<T>` salvo endpoints RFC/OIDC nativos.
- Jackson 3: `tools.jackson.databind.*`.
- JSONB JPA: `@JdbcTypeCode(SqlTypes.JSON)` + `@Column(columnDefinition = "jsonb")`.
- Objetos nuevos persistidos no seteados con `id`.
- Campos nullable del dominio expuestos como `Optional<T>`.

## Fuentes de verdad relacionadas

| Tema | Documento |
|---|---|
| Índice documental | [`../README.md`](../README.md) |
| Seguridad HTTP | [`security/bootstrap-filter.md`](security/bootstrap-filter.md) |
| Flujos OAuth2/OIDC | [`../02-functional/authentication-flow.md`](../02-functional/authentication-flow.md) |
| Migraciones | [`../08-reference/data/migrations.md`](../08-reference/data/migrations.md) |
| Setup local | [`../07-operations/environment-setup.md`](../07-operations/environment-setup.md) |
| Política documental | [`../README.md`](../README.md) |
