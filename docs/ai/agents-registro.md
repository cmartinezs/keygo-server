# AGENTS — Registro de Cambios

> Sub-documento de [`AGENTS.md`](../../AGENTS.md).
>
> Historial cronológico de actualizaciones al quick-start: módulos, comandos, patrones y URLs.
> Entradas anteriores a 2026-03-25 en [`agents-registro-historico.md`](agents-registro-historico.md).
>
> **Regla:** Agregar entrada cada vez que cambie estructura de módulos, comandos, patrones o URLs.

---

## Formato de entrada

```markdown
### [YYYY-MM-DD] Descripción del cambio
- Bullet con detalle
```

---

## Registro de cambios

### [2026-03-31] Reorganización de /docs — compactación y limpieza

- `docs/ai/lecciones.md`: 1.745 → 514 líneas. Nuevo formato compacto: **Síntoma / Causa / Solución**.
- `docs/ai/agents-registro.md`: 781 → 129 líneas. Entradas pre-2026-03-25 archivadas en `agents-registro-historico.md`.
- `docs/plan_reestructuracion.md`: eliminado (ya ejecutado).
- `docs/research/` → `docs/archive/research/` (5 archivos de investigación históricos).
- `AI_CONTEXT.md`: tabla de retroalimentación duplicada eliminada — apunta a `CLAUDE.md`.
- `ARCHITECTURE.md` (raíz): sección Comandos eliminada; seguridad corregida (Bearer, no X-KEYGO-ADMIN); migraciones actualizadas a V1–V18.
- `docs/README.md`: estructura actualizada con `archive/` y correcciones de versiones.

---

### [2026-03-31] Endpoints billing onboarding: resume + resend-verification

- **Dominio:** `AppContract` — `isVerificationCodeExpired()`, `renewVerificationCode()`
- **App:** `ResumeContractOnboardingUseCase`, `ResendContractVerificationUseCase`
- **API:** `AppContractResumeData` (DTO), 2 nuevos métodos en `AppBillingContractController`, 2 nuevos `ResponseCode`
- **Run:** 2 nuevos `@Bean` en `ApplicationConfig`
- **URLs nuevas:**
  - `GET /keygo-server/api/v1/billing/contracts/{contractId}/resume` — Público
  - `POST /keygo-server/api/v1/billing/contracts/{contractId}/resend-verification` — Público
- **Nota:** Rutas cubiertas por `hasSegment("/billing/contracts")` en filtro. Sin cambios en filtro ni `application.yml`.

---

### [2026-03-30] Reestructuración Flyway — Modelo v2 Contractors integrado desde V1

- Backup de V1–V17 en `backup_20260330/`
- Nuevas migraciones: V11 (contractors), V12 (billing_contracts v2), V13 (billing_subscriptions), V14 (billing_invoices_and_usage), V15 (billing_support_tables), V16 (seed_foundation), V17 (seed_billing_plans), V18 (seed_contractors)
- Cambio de modelo: `subscriber_tenant_id`/`subscriber_tenant_user_id` → `contractor_id` en contratos, suscripciones y usage
- **Próxima migración:** `V19__...`
- Credenciales seed: `contractor@keygo.local` / `Admin1234!`

---

### [2026-03-29] Escalera de planes de billing v2 (USD)

- `V17__seed_keygo_billing_plans_v2.sql`: versiones v2.0 en USD (FREE/BUSINESS/ENTERPRISE) + v1.0 nuevos (PERSONAL/TEAM/FLEX)
- Escalera: FREE $0 | PERSONAL $5 | TEAM $49 | BUSINESS $149 | FLEX pay-per-use | ENTERPRISE custom/año
- ENTERPRISE: `billing_period = YEARLY`, `base_price = 0`

---

### [2026-03-29] Reestructuración Flyway V1–V26 → V1–V17

- Eliminadas V11–V26 (parches y seeds fragmentados). Una migración por dominio.
- V11: billing_contracts, V12: billing_subscriptions, V13: invoices+usage, V14: billing_support_tables
- V15: seed foundation, V16: seed billing_platform_app, V16/V17: seed billing_plans
- `docs/data/MIGRATIONS.md` actualizado. **Próxima (en ese momento):** `V18__...`

---

### [2026-03-28] Endpoint `GET /api/v1/admin/platform/dashboard` + refactorización GROUP BY

- **App:** `PlatformDashboardPort` (9 métodos `Map<K,Long>`), `GetPlatformDashboardUseCase`, `PlatformDashboardResult`
- **Supabase:** `PlatformDashboardAdapter` con helpers `toCountMap()`/`toStringCountMap()`
- **API:** `PlatformDashboardController` (`@PreAuthorize("hasRole('ADMIN')")`), `PlatformDashboardData` (12 sub-DTOs)
- **Reducción:** ~25 queries independientes → ~9 queries GROUP BY por petición
- **URL:** `GET /keygo-server/api/v1/admin/platform/dashboard` — requiere Bearer ADMIN

---

### [2026-03-28] Corrección de documentación: endpoint `GET /api/v1/tenants`

- `ROADMAP.md`: estado actualizado (endpoints 26→27, tests 338+→527+, Postman 40→42)
- `FRONTEND_DEVELOPER_GUIDE.md` §8.2: marcado ✅, `nameLike`→`name_like` corregido
- `AGENTS.md`: endpoint `GET /api/v1/tenants` agregado a lista de URLs

---

### [2026-03-27] Endpoint `GET /api/v1/tenants` — listado paginado

- **App:** `TenantFilter`, `PagedResult<T>`, `ListTenantsUseCase`, nuevo método en `TenantRepositoryPort`
- **Supabase:** `TenantJpaRepository` con `JpaSpecificationExecutor`, `findAll()` con Specification ILIKE
- **API:** `PagedData<T>`, nuevo `@GetMapping listTenants()`, `ResponseCode.TENANT_LIST_RETRIEVED`
- **URL:** `GET /keygo-server/api/v1/tenants?status=ACTIVE&name_like=...&page=0&size=20` — Bearer ADMIN

---

### [2026-03-27] Reorganización: `scripts/` → `docs/scripts/`, `postman/` → `docs/postman/`

- `scripts/` renombrado a `docs/scripts/`; `postman/` renombrado a `docs/postman/`
- `PROJECT_ROOT` corregido en `keygo.sh`, `switch-env.sh`, `check-ai-docs.sh`, `quick-start.sh`
- `data-local.sql`: `ON CONFLICT DO NOTHING` → `INSERT ... WHERE NOT EXISTS` (H2 compatible)
- `AGENTS.md`, `CLAUDE.md`, `AI_CONTEXT.md`, `copilot-instructions.md`: todas las referencias actualizadas

---

### [2026-03-26] Refinación: `envs/` a raíz del proyecto, `.env` activo en raíz

- `scripts/envs/` → `envs/` (raíz del proyecto)
- `.env` activo: se copia a `$PROJECT_ROOT/.env` (antes en `keygo-supabase/.env`)
- `_load-env.sh`: apunta a `$PROJECT_ROOT/.env`
- `keygo-supabase/scripts/`: vaciada

---

### [2026-03-26] Script `keygo.sh` — menú principal + centralización `scripts/db/`

- Nuevo `docs/scripts/keygo.sh`: 20 opciones en 5 categorías (Ambiente / BD / App / Tests / Setup)
- Stubs de compatibilidad en `keygo-supabase/scripts/` delegan con `exec` a `docs/scripts/db/`
- `AGENTS.md` sección "Essential commands" actualizada con rutas centralizadas

---

### [2026-03-26] CORS habilitado en SecurityFilterChain

- Nuevo: `KeyGoCorsProperties` (`keygo.cors.*`), `CorsConfigurationSource` en `SecurityConfig`
- `application.yml`: `keygo.cors.allowed-origins: http://localhost:5173`
- Spring Security aplica CORS antes del `BootstrapAdminKeyFilter`; preflight OPTIONS resuelto

---

### [2026-03-25] Seguridad admin Bearer-only + RBAC por endpoint

- `BootstrapAdminKeyFilter`: autenticación solo por `Authorization: Bearer` (eliminado `X-KEYGO-ADMIN`)
- `@PreAuthorize` en todos los controllers admin; `TenantAuthorizationEvaluator` para aislamiento por tenant
- `OpenApiConfig`: esquema migrado a `BearerAuth`
- Claim `tenant_slug` emitido en access tokens (auth code, refresh, client_credentials)
