# 🔍 DEEPENING: Scope 01 — GET /platform/account/access

> **Status:** APPLIED
> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)

**Spec origen:** Bug reportado en piloto (403 con token de plataforma en endpoints tenant-scoped) | **Prioridad:** P1 / Bloqueante para piloto

---

## Objective

Agregar `GET /api/v1/platform/account/access` que, dado el `sub` del JWT de plataforma (userId), retorna la vista completa de acceso del usuario: todos los tenants donde tiene membresías, con las apps y roles de cada una.

---

## Diseño de contrato

### GET /api/v1/platform/account/access

**Auth:** Bearer platform token (cualquier usuario de plataforma autenticado — `isAuthenticated()`)

**Request:** sin body ni query params — todo se deriva del `sub` del JWT.

**Response:** `BaseResponse<List<TenantAccessData>>`

```json
{
  "success": { "code": "ACCOUNT_ACCESS_RETRIEVED", "message": "..." },
  "data": [
    {
      "tenantId": "uuid",
      "tenantSlug": "keygo",
      "tenantName": "KeyGo Platform",
      "apps": [
        {
          "membershipId": "uuid",
          "membershipStatus": "ACTIVE",
          "clientAppId": "uuid",
          "clientId": "oauth2-client-id-string",
          "appName": "Portal Admin",
          "roles": ["ADMIN_TENANT"]
        }
      ]
    }
  ]
}
```

**Casos de borde:**
- Usuario sin membresías → `data: []` (200 OK)
- App o tenant no encontrado → ignorar esa membresía (log warn) — defensivo ante inconsistencia de datos

---

## Flujo de orquestación (GetAccountAccessUseCase)

```
userId (del sub JWT)
  → MembershipRepositoryPort.findByUserId(userId)          → List<Membership>
  → por cada Membership:
      ClientAppRepositoryPort.findById(clientAppId)        → Optional<ClientApp>  (contiene tenantId)
      TenantRepositoryPort.findById(tenantId)              → Optional<Tenant>     (contiene slug + name)
      GetMembershipRolesUseCase.execute(membershipId)      → List<MembershipRoleResult>
  → agrupar por tenantId
  → construir List<TenantAccessData>
```

---

## Componentes a crear / modificar

### keygo-app

**Nuevo:** `GetAccountAccessUseCase`
- Package: `io.cmartinezs.keygo.app.membership.usecase`
- Dependencias: `MembershipRepositoryPort`, `ClientAppRepositoryPort`, `TenantRepositoryPort`, `GetMembershipRolesUseCase`
- Retorna: `List<TenantAccessResult>` (record interno o clase)

**Nuevo:** result records (en `keygo-app`, package `io.cmartinezs.keygo.app.membership.result`)
- `TenantAccessResult(String tenantId, String tenantSlug, String tenantName, List<AppAccessResult> apps)`
- `AppAccessResult(UUID membershipId, String membershipStatus, UUID clientAppId, String clientId, String appName, List<String> roles)`

### keygo-api

**Nuevo:** DTOs response (package `io.cmartinezs.keygo.api.membership.response` o `platform.response`)
- `TenantAccessData` (builder — campos: `tenantId`, `tenantSlug`, `tenantName`, `apps`)
- `AppAccessData` (builder — campos: `membershipId`, `membershipStatus`, `clientAppId`, `clientId`, `appName`, `roles`)

**Modificado:** `PlatformAccountController`
- Nueva dependencia: `GetAccountAccessUseCase`
- Nuevo método `getAccess()`:
  - Extrae userId del `sub` (reutiliza `extractUserId()` ya existente)
  - Llama `getAccountAccessUseCase.execute(userId)`
  - Construye `BaseResponse<List<TenantAccessData>>`
  - `@PreAuthorize("isAuthenticated()")`

**Nuevo ResponseCode:** `ACCOUNT_ACCESS_RETRIEVED` en `ResponseCode` enum

### keygo-run

- Wiring de `GetAccountAccessUseCase` en la clase de configuración correspondiente

---

## Tasks

| # | Tarea | Workflow | Estado | Output |
|---|-------|----------|--------|--------|
| 1 | `TenantAccessResult` y `AppAccessResult` records en `keygo-app` | GENERATE-DOCUMENT | APPLIED | dos archivos nuevos en `app.membership.result` |
| 2 | `GetAccountAccessUseCase` en `keygo-app` | GENERATE-DOCUMENT | APPLIED | `GetAccountAccessUseCase.java` |
| 3 | `TenantAccessData` y `AppAccessData` DTOs en `keygo-api` | GENERATE-DOCUMENT | APPLIED | dos archivos nuevos en `api.membership.response` |
| 4 | `ACCOUNT_ACCESS_RETRIEVED` en `ResponseCode` enum | GENERATE-DOCUMENT | APPLIED | ya existía en `ResponseCode.java` |
| 5 | `GET /account/access` en `PlatformAccountController` + inyectar `GetAccountAccessUseCase` | GENERATE-DOCUMENT | APPLIED | `PlatformAccountController.java` modificado |
| 6 | Wiring de `GetAccountAccessUseCase` en `keygo-run` | GENERATE-DOCUMENT | APPLIED | `ApplicationConfig.java` modificado |
| 7 | Test de `GetAccountAccessUseCase` | GENERATE-DOCUMENT | APPLIED | `GetAccountAccessUseCaseTest.java` — 4 casos |
| 8 | Test de `PlatformAccountController.getAccess` | GENERATE-DOCUMENT | APPLIED | `PlatformAccountControllerTest.java` — 2 casos |
| 9 | Actualizar `TRACEABILITY.md` | UPDATE-TRACEABILITY | APPLIED | `TRACEABILITY.md` |

---

## Done Criteria

- [x] `GET /platform/account/access` con cualquier token de plataforma válido retorna 200 con la lista de tenants + apps + roles del usuario autenticado.
- [x] Usuario sin membresías → `data: []`.
- [ ] `GET /tenants/keygo/memberships` con token tenant sigue funcionando (no regresión — no modificado).
- [x] `ACCOUNT_ACCESS_RETRIEVED` en `ResponseCode`.
- [x] `GetAccountAccessUseCase` ignora membresías cuya app o tenant no se encuentren.
- [x] Tests: happy path, usuario sin membresías, app no encontrada, tenant no encontrado, dos apps en un tenant.
- [x] `TRACEABILITY.md` actualizado.

---

## Decisiones de diseño

| Decisión | Alternativas consideradas | Elección | Razón |
|---|---|---|---|
| Endpoint único `/access` vs endpoints separados | `GET /memberships` + `GET /apps` | `/access` | El endpoint `/apps` sin tenant no tiene semántica propia; el frontend necesita la vista agrupada; una sola llamada |
| Respuesta sin paginación | Paginado por membresía | Sin paginación (lista completa) | Volumen bajo en el piloto; la agrupación por tenant se pierde con paginación por membresía |
| `GetAccountAccessUseCase` en `keygo-app` vs lógica en controller | Lógica directa en controller | Use case en `keygo-app` | La orquestación de 3–4 ports es lógica de aplicación, no de presentación |
| Manejo de app/tenant no encontrado | Lanzar excepción vs ignorar | Log warn + ignorar | Inconsistencia de datos no debe romper el endpoint; el usuario ve lo que puede ver |

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
