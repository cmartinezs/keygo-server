# Arquitectura de KeyGo Server

> 📖 **Documento canónico:** [`docs/design/ARCHITECTURE.md`](docs/design/ARCHITECTURE.md)
> Este archivo es un resumen de referencia rápida. Para el detalle completo, ver el enlace anterior.

---

## Resumen técnico

- Build: Maven multi-módulo (monorepo) — Java 21, Spring Boot 4.x.
- Arquitectura: **Hexagonal / Ports & Adapters**.
- Módulo ejecutable: `keygo-run`.
- `context-path`: `/keygo-server`.
- Persistencia opcional: `keygo-supabase` (perfil `supabase`) — JPA + Flyway + PostgreSQL.

## Módulos

| Módulo | Rol | Estado |
|---|---|---|
| `keygo-domain` | Dominio puro. Sin Spring. | ✅ Activo |
| `keygo-app` | Usecases + puertos (interfaces OUT). | ✅ Activo |
| `keygo-infra` | JWT signer (RSA/Nimbus), JWKS builder, PkceVerifier. | ✅ Activo |
| `keygo-api` | REST controllers + DTOs + error handlers. | ✅ Activo |
| `keygo-supabase` | JPA/Flyway + entidades + repos. Migraciones V1–V18. | ✅ Activo |
| `keygo-run` | Main + wiring + `application.yml`. | ✅ Activo |
| `keygo-bom` | Gestión de versiones de dependencias. | ✅ Activo |
| `keygo-common` | Utilidades compartidas. | 🚧 Stub |

> **Regla de oro:** `keygo-domain` no puede depender de Spring ni de ningún otro módulo del proyecto.

## Flujo general

```
Client → BootstrapAdminKeyFilter → keygo-api (Controller)
                                       → keygo-app (UseCase)
                                           → Port OUT (interface)
                                               → keygo-supabase / keygo-infra (Adapter)
                                       ← BaseResponse<T>
```

## Seguridad

- `/api/**` protegido con `Authorization: Bearer <jwt>` (rol `ADMIN` o `ADMIN_TENANT`).
- `/actuator/**`, `/.well-known/**`, `/swagger-ui/**`, `/v3/api-docs` son públicos.
- `@PreAuthorize` por endpoint + `TenantAuthorizationEvaluator` para aislamiento por tenant.

---

> Ver [`docs/design/ARCHITECTURE.md`](docs/design/ARCHITECTURE.md) para modelo conceptual,
> estrategia multi-tenant, flujos OAuth2/OIDC y más.
