# 🔍 DEEPENING: Scope 01 — Platform account: memberships y apps listing

> **Status:** PENDING
> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)

**Spec origen:** Bug reportado en piloto (403 con token de plataforma en endpoints tenant-scoped) | **Prioridad:** P1 / Bloqueante para piloto

---

## Objective

Agregar en `PlatformAccountController` los métodos:

| Método | Path | Descripción |
|---|---|---|
| `GET` | `/api/v1/platform/account/memberships` | Lista membresías de un tenant; mismos filtros que el endpoint tenant-scoped |
| `GET` | `/api/v1/platform/account/apps` | Lista apps de un tenant; mismos filtros que el endpoint tenant-scoped |

Ambos requieren `tenant_slug` como query param obligatorio (sustituye al path variable `{tenantSlug}`).

---

## Diseño de contratos

### GET /api/v1/platform/account/memberships

**Query params:**

| Param | Tipo | Requerido | Descripción |
|---|---|---|---|
| `tenant_slug` | String | ✅ | Slug del tenant a consultar |
| `user_id` | UUID | ☐ | Filtra por usuario |
| `client_app_id` | UUID | ☐ | Filtra por app |
| `status` | MembershipStatus | ☐ | ACTIVE / SUSPENDED / PENDING |
| `page` | int | ☐ | Default 0 |
| `size` | int | ☐ | Default 20 |
| `sort` | String | ☐ | Campo de ordenamiento |
| `order` | String | ☐ | ASC / DESC |

**Response:** `BaseResponse<PagedData<MembershipData>>` — misma estructura que `TenantMembershipController.listMemberships`.

**Authorization:** `@PreAuthorize("hasAnyRole('KEYGO_ADMIN', 'KEYGO_ACCOUNT_ADMIN')")`

---

### GET /api/v1/platform/account/apps

**Query params:**

| Param | Tipo | Requerido | Descripción |
|---|---|---|---|
| `tenant_slug` | String | ✅ | Slug del tenant a consultar |
| `status` | ClientAppStatus | ☐ | ACTIVE / SUSPENDED / PENDING |
| `name_like` | String | ☐ | Match parcial por nombre |
| `q` | String | ☐ | Búsqueda OR sobre nombre y client_id |
| `page` | int | ☐ | Default 0 |
| `size` | int | ☐ | Default 20 |
| `sort` | String | ☐ | Campo de ordenamiento |
| `order` | String | ☐ | ASC / DESC |

**Response:** `BaseResponse<PagedData<ClientAppData>>` — misma estructura que `TenantClientAppController.listClientApps`.

**Authorization:** `@PreAuthorize("hasAnyRole('KEYGO_ADMIN', 'KEYGO_ACCOUNT_ADMIN')")`

---

## Tasks

| # | Tarea | Workflow | Estado | Output |
|---|-------|----------|--------|--------|
| 1 | Inyectar `ListMembershipsUseCase`, `ListClientAppsUseCase`, `GetMembershipRolesUseCase` en `PlatformAccountController` (constructor) | GENERATE-DOCUMENT | PENDING | `PlatformAccountController.java` — constructor actualizado |
| 2 | Agregar `GET /account/memberships` en `PlatformAccountController` con `@PreAuthorize`, validación de `tenant_slug` y delegación a `ListMembershipsUseCase` | GENERATE-DOCUMENT | PENDING | `PlatformAccountController.java` — método nuevo |
| 3 | Agregar `GET /account/apps` en `PlatformAccountController` con `@PreAuthorize`, validación de `tenant_slug` y delegación a `ListClientAppsUseCase` | GENERATE-DOCUMENT | PENDING | `PlatformAccountController.java` — método nuevo |
| 4 | OpenAPI annotations en ambos métodos | GENERATE-DOCUMENT | PENDING | `PlatformAccountController.java` — anotaciones |
| 5 | Tests de controlador: happy path memberships, happy path apps, caso `tenant_slug` vacío | GENERATE-DOCUMENT | PENDING | `PlatformAccountControllerTest.java` (nuevo o extendido) |
| 6 | Actualizar `TRACEABILITY.md` | UPDATE-TRACEABILITY | PENDING | `TRACEABILITY.md` |

---

## Done Criteria

- [ ] `GET /platform/account/memberships?tenant_slug=keygo&user_id=xxx` retorna 200 con datos paginados usando token de plataforma.
- [ ] `GET /platform/account/apps?tenant_slug=keygo` retorna 200 con datos paginados usando token de plataforma.
- [ ] `GET /tenants/keygo/memberships` sigue retornando los mismos resultados con token tenant (no regresión).
- [ ] `GET /platform/account/memberships` sin `tenant_slug` retorna 400.
- [ ] `GET /platform/account/apps` sin `tenant_slug` retorna 400.
- [ ] Sin nuevos use cases, sin cambios en `keygo-app`, `keygo-supabase` ni `keygo-domain`.
- [ ] `TRACEABILITY.md` actualizado.

---

## Decisiones de diseño

| Decisión | Alternativas consideradas | Elección | Razón |
|---|---|---|---|
| `tenant_slug` como query param vs path variable | Path var `/platform/account/{tenantSlug}/memberships` | Query param `?tenant_slug=` | Más consistente con el patrón de "account" — el slug es un filtro, no una dimensión del recurso |
| Agregar en `PlatformAccountController` vs nuevo controlador | Nuevo `PlatformTenantDataController` | En el existente `PlatformAccountController` | El contexto es el mismo (cuenta de plataforma); no justifica nuevo controlador para dos métodos |
| Validación de `tenant_slug` | `@NotBlank` en param vs validación en método | `required = true` en `@RequestParam` + Spring 400 automático | Suficiente; Spring lanza `MissingServletRequestParameterException` (→ 400) automáticamente |

---

## Inconsistencies Found

| # | Descripción | Archivos involucrados | Estado | Resolución |
|---|-------------|----------------------|--------|-----------|
| — | *Ninguna* | — | — | — |

---

## Residuals

| # | Descripción | Diferido a | Estado |
|---|-------------|-----------|--------|
| — | — | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)
