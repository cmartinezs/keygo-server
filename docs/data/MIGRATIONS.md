# Migraciones Flyway — KeyGo Server

> **Última actualización:** 2026-04-07  
> **Reestructuración total (2026-03-30):** V1–V17 reemplazadas por **V1–V18** con modelo v2 de billing integrado desde el origen. Backup en `backup_20260330/`.  
> **Próxima migración:** `V32__...`

---

## 1. Estrategia

- Herramienta: **Flyway** (perfil `supabase`)
- Ubicación: `keygo-supabase/src/main/resources/db/migration/`
- `ddl-auto: validate` — Flyway gestiona el schema; Hibernate solo valida
- **Regla de oro:** nunca editar ni reusar un archivo de migración ya aplicado

### Convención de nombres

```
V{número}__{descripción_con_underscores}.sql
```

- Número: incremental, entero, sin ceros a la izquierda
- Doble guion bajo entre versión y descripción
- Extensión: `.sql`

---

## 2. Migraciones aplicadas

> ⚠️ **Reestructuración 2026-03-30:** Modelo v2 billing integrado. La entidad `contractors` existe desde V11. Las tablas de billing ya no usan `subscriber_*` sino `contractor_id`. V16 seed foundation incluye `keygo_contractor`. V18 seed contractors completo.

### Resumen

| N° | Archivo | Dominio | Contenido |
|---|---|---|---|
| V1  | `V1__drop_all.sql`                          | Bootstrap | Drop ALL (incluye `contractors`) |
| V2  | `V2__foundation.sql`                        | Bootstrap | Extensión `uuid-ossp` + función `update_updated_at_column()` |
| V3  | `V3__tenants.sql`                           | Core      | Tabla `tenants` (`contractor_id` sin FK, estado `DELETED`) |
| V4  | `V4__client_apps.sql`                       | Core      | Tablas `client_apps`, `client_redirect_uris`, `client_allowed_grants`, `client_allowed_scopes` |
| V5  | `V5__tenant_users.sql`                      | Core      | Tabla `tenant_users` (con campos OIDC 5.3) |
| V6  | `V6__memberships.sql`                       | Core      | Tablas `app_roles`, `memberships`, `membership_roles` |
| V7  | `V7__auth_codes_and_signing_keys.sql`       | Auth      | Tablas `authorization_codes`, `signing_keys` |
| V8  | `V8__sessions_and_refresh_tokens.sql`       | Auth      | Tablas `sessions`, `refresh_tokens` |
| V9  | `V9__email_verifications.sql`               | Auth      | Tabla `email_verifications` |
| V10 | `V10__billing_catalog.sql`                  | Billing   | Tablas `app_plans` (con `subscriber_type`), `app_plan_versions`, `app_plan_billing_options`, `app_plan_entitlements` |
| V11 | `V11__contractors.sql`                      | Billing   | **NUEVA** tabla `contractors` + FK `tenants.contractor_id → contractors(id)` |
| V12 | `V12__billing_contracts.sql`                | Billing   | Tabla `app_contracts` (modelo v2: `contractor_id`, sin `subscriber_*`, estados `SUPERSEDED`+`FINALIZED`) |
| V13 | `V13__billing_subscriptions.sql`            | Billing   | Tablas `app_subscriptions` (`contractor_id`), `payment_transactions` |
| V14 | `V14__billing_invoices_and_usage.sql`       | Billing   | Tablas `invoices`, `usage_counters` (`contractor_id`) |
| V15 | `V15__billing_support_tables.sql`           | Billing   | Tablas `payment_methods`, `tenant_billing_profiles` |
| V16 | `V16__seed_foundation.sql`                  | Seed      | Tenants `keygo`+`demo`, apps, usuarios (incluye `keygo_contractor`), roles, memberships |
| V17 | `V17__seed_billing_plans.sql`               | Seed      | Planes FREE/PERSONAL/TEAM/BUSINESS/FLEX/ENTERPRISE + versiones v1.0 + billing options + entitlements (USD) |
| V18 | `V18__seed_contractors.sql`                 | Seed      | `keygo_contractor`, `contractors` ACTIVE, contrato ACTIVE plan PERSONAL, suscripción ACTIVE, tenant `acme` |
| V19 | `V19__user_status_reset_password.sql`       | Auth      | Columna `status=RESET_PASSWORD` en `tenant_users`; tabla `password_reset_tokens` |
| V20 | `V20__add_app_role_hierarchy.sql`           | Core      | Tabla `app_role_hierarchy` (parent/child, restricciones de ciclo, profundidad ≤5), índices, CTE recursiva |
| V21 | `V21__user_notification_preferences.sql`   | Core      | Tabla `user_notification_preferences` (5 flags boolean, UNIQUE `user_id+tenant_id`) |
| V22 | `V22__signing_key_tenant_scope_and_audit_refs.sql` | Auth | `tenant_id` en `signing_keys` (nullable, FK → tenants); `signing_key_id` en `sessions` y `refresh_tokens` (nullable, FK → signing_keys) |
| V23 | `V23__password_reset_codes.sql`             | Auth      | Tabla `password_reset_codes` (código 6 dígitos, TTL 15 min, UNIQUE por usuario) |
| V24 | `V24__platform_roles_and_user_roles.sql`    | RBAC      | Tablas `platform_roles` (UNIQUE code), `platform_user_roles` (UNIQUE user+role) |
| V25 | `V25__tenant_roles_and_user_roles.sql`      | RBAC      | Tablas `tenant_roles` (UNIQUE tenant_id+code), `tenant_user_roles` (partial UNIQUE, soft-delete) |
| V26 | `V26__seed_platform_and_tenant_roles.sql`   | Seed      | 3 platform_roles, 3 platform_user_role assignments, 5 tenant_roles (keygo+demo), 2 tenant_user_roles |
| V27 | `V27__platform_users.sql`                   | Identity  | Tabla `platform_users` (email+username UNIQUE, status CHECK, perfil OIDC) |
| V28 | `V28__sessions_platform_refactor.sql`       | Identity  | `platform_user_id` en `tenant_users`; refactor `platform_user_roles` FK; `sessions`+`refresh_tokens` refactorizados con `platform_user_id` |
| V29 | `V29__platform_users_seed_and_role_rename.sql` | Seed   | 4 platform_users (keygo), role rename `keygo_account_admin`→`keygo_tenant_admin`, vinculación `tenant_users.platform_user_id` |
| V30 | `V30__billing_contractor_to_platform_user.sql` | Billing | `contractors.tenant_user_id`→`platform_user_id`; `app_plans`/`app_contracts`/`app_subscriptions` nullable `client_app_id` + `subscriber_type=PLATFORM` |
| V31 | `V31__verification_codes.sql`               | Auth      | Tabla unificada `verification_codes` (purpose discriminator); drop `email_verifications`+`password_reset_codes` |

---

### V1 — Bootstrap: Drop ALL

**Propósito:** Pizarrón limpio. Elimina todas las tablas (CASCADE) y la función `update_updated_at_column()`. Idempotente con `DROP ... IF EXISTS`.  
**Prerequisito:** `./docs/scripts/db/clean.sh` (limpia `flyway_schema_history`).

---

### V2 — Bootstrap: Foundation

**Propósito:** Instala extensión `uuid-ossp` y crea la función trigger `update_updated_at_column()` usada por todas las tablas con `updated_at`.

---

### V3 — Tenants

**Tablas creadas:** `tenants`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | `gen_random_uuid()` |
| `slug` | VARCHAR(100) UNIQUE | URL-friendly, lowercase, min 3 chars |
| `owner_email` | VARCHAR(255) | Email del administrador propietario |
| `status` | VARCHAR(20) | `ACTIVE \| SUSPENDED \| PENDING \| DELETED` |
| `contractor_id` | UUID | FK → `contractors(id)` agregada en V11; NULL = tenant de sistema |
| `created_at` / `updated_at` | TIMESTAMPTZ | Trigger auto-actualiza `updated_at` |

> `contractor_id` se declara sin FK en V3 (la tabla `contractors` aún no existe). La FK se agrega vía `ALTER TABLE` en V11.

---

### V4 — Client Apps

**Tablas creadas:** `client_apps`, `client_redirect_uris`, `client_allowed_grants`, `client_allowed_scopes`

| Tabla | Descripción |
|---|---|
| `client_apps` | Apps OAuth2 registradas. `type = PUBLIC \| CONFIDENTIAL`, `status = ACTIVE \| SUSPENDED \| PENDING` |
| `client_redirect_uris` | URIs de redirección permitidas (sin wildcards) |
| `client_allowed_grants` | Grant types por app (ej. `AUTHORIZATION_CODE`, `CLIENT_CREDENTIALS`) |
| `client_allowed_scopes` | Scopes por app (ej. `openid`, `profile`, `email`) |

---

### V5 — Tenant Users

**Tablas creadas:** `tenant_users`

Incluye los 6 campos de perfil OIDC 5.3 desde el inicio:

| Campo OIDC | Columna | Tipo | Scope |
|---|---|---|---|
| `phone_number` | `phone_number` | VARCHAR(30) | `phone` |
| `locale` | `locale` | VARCHAR(10) | `profile` (BCP47) |
| `zoneinfo` | `zoneinfo` | VARCHAR(50) | `profile` |
| `picture` | `profile_picture_url` | TEXT | `profile` |
| `birthdate` | `birthdate` | DATE | `profile` |
| `website` | `website` | VARCHAR(2048) | `profile` |

**UNIQUE:** `(tenant_id, email)` y `(tenant_id, username)` — unicidad por tenant, no global.

---

### V6 — Memberships

**Tablas creadas:** `app_roles`, `memberships`, `membership_roles`

| Tabla | Descripción |
|---|---|
| `app_roles` | Roles por ClientApp (no globales). UNIQUE `(client_app_id, code)`. `code` lowercase `[a-z0-9_-]` |
| `memberships` | Acceso de TenantUser a ClientApp. UNIQUE `(user_id, client_app_id)` |
| `membership_roles` | N:M `memberships ↔ app_roles`. PK compuesta `(membership_id, role_id)` |

---

### V7 — Auth Codes + Signing Keys

**Tablas creadas:** `authorization_codes`, `signing_keys`

| Tabla | Descripción |
|---|---|
| `authorization_codes` | Códigos PKCE del flujo Authorization Code. TTL 10 min, single-use. `status` lowercase |
| `signing_keys` | Pares RSA para firma JWT (RS256/RS384/RS512). `ACTIVE` = en uso; `RETIRED` = solo validación |

---

### V8 — Sessions + Refresh Tokens

**Tablas creadas:** `sessions`, `refresh_tokens`

| Tabla | Descripción |
|---|---|
| `sessions` | Sesión `(tenant, app, user)`. Agrupa refresh tokens. `status = ACTIVE \| TERMINATED \| EXPIRED` |
| `refresh_tokens` | Hash SHA-256 hex (64 chars). **El token plano NUNCA se persiste**. `replaced_by_id` apunta al nuevo tras rotación |

---

### V9 — Email Verifications

**Tablas creadas:** `email_verifications`

| Columna | Tipo | Descripción |
|---|---|---|
| `tenant_user_id` | UUID FK | FK → `tenant_users(id)` ON DELETE CASCADE |
| `code` | VARCHAR(10) | 6 dígitos, `SecureRandom` |
| `expires_at` | TIMESTAMPTZ | TTL 30 minutos |
| `used_at` | TIMESTAMPTZ | No-nulo = ya usado |

---

### V10 — Billing Catalog

**Tablas creadas:** `app_plans`, `app_plan_versions`, `app_plan_billing_options`, `app_plan_entitlements`

| Tabla | Descripción |
|---|---|
| `app_plans` | Planes por ClientApp. `subscriber_type = TENANT \| TENANT_USER`. `sort_order` controla el orden en UI. UNIQUE `(client_app_id, code)` |
| `app_plan_versions` | Snapshots inmutables de configuración (sin precio). Suscripciones existentes no se afectan |
| `app_plan_billing_options` | Períodos de facturación disponibles por versión (0 filas = plan gratuito). `billing_period = MONTHLY \| YEARLY \| ONE_TIME` |
| `app_plan_entitlements` | Límites y feature flags. `metric_type = QUOTA \| BOOLEAN \| RATE`. `limit_value = NULL` = ilimitado |

---

### V11 — Contractors (NUEVA)

**Tablas creadas:** `contractors`  
**Modificaciones:** FK `tenants.contractor_id → contractors(id)`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | `gen_random_uuid()` |
| `tenant_user_id` | UUID UNIQUE FK | FK → `tenant_users(id)` ON DELETE RESTRICT. Relación 1:1 |
| `status` | VARCHAR(20) | `PENDING \| ACTIVE \| SUSPENDED` |
| `created_at` / `updated_at` | TIMESTAMPTZ | Trigger auto-actualiza `updated_at` |

**Lógica de ciclo de vida:**
- `PENDING`: email verificado en contrato, esperando primer pago
- `ACTIVE`: primer contrato activado exitosamente
- `SUSPENDED`: suspendido por impago u otra razón administrativa

**ALTER TABLE en esta misma migración:** `ALTER TABLE tenants ADD CONSTRAINT fk_tenants_contractor_id FOREIGN KEY (contractor_id) REFERENCES contractors(id) ON DELETE SET NULL`

---

### V12 — Billing Contracts (modelo v2)

**Tablas creadas:** `app_contracts`

| Columna clave | Tipo | Notas |
|---|---|---|
| `contractor_id` | UUID FK → `contractors(id)` | NULL hasta verificar email; NOT NULL desde `PENDING_PAYMENT` |
| `verification_code` | VARCHAR(10) | Código 6 dígitos para verificar email del contrato |
| `verification_code_expires_at` | TIMESTAMPTZ | TTL 30 min |
| `status` | VARCHAR(40) | `PENDING_EMAIL_VERIFICATION → PENDING_PAYMENT → READY_TO_ACTIVATE → ACTIVE → SUPERSEDED \| FINALIZED \| CANCELLED \| EXPIRED \| FAILED` |

**Índice único parcial:** `UNIQUE(contractor_id) WHERE status = 'ACTIVE'` — solo 1 contrato vigente por contratante.

**Campos eliminados vs modelo v1:** `subscriber_type`, `subscriber_tenant_id`, `subscriber_tenant_user_id`, `company_slug`

---

### V13 — Billing Subscriptions (modelo v2)

**Tablas creadas:** `app_subscriptions`, `payment_transactions`

| Tabla | Descripción |
|---|---|
| `app_subscriptions` | Suscripción activa `contractor ↔ versión de plan`. `contractor_id NOT NULL`. UNIQUE `(client_app_id, contractor_id)`. `status = PENDING \| ACTIVE \| PAST_DUE \| SUSPENDED \| CANCELLED \| EXPIRED` |
| `payment_transactions` | Evento de pago. `provider = MOCK \| MANUAL \| MERCADOPAGO \| STRIPE \| OTHER` |

**Campos eliminados vs modelo v1:** `subscriber_tenant_id`, `subscriber_tenant_user_id` (reemplazados por `contractor_id`)

---

### V14 — Invoices + Usage Counters (modelo v2)

**Tablas creadas:** `invoices`, `usage_counters`

| Tabla | Descripción |
|---|---|
| `invoices` | Snapshot histórico por período. Campos `*_snapshot` inmutables. `status = DRAFT \| ISSUED \| PAID \| VOID \| OVERDUE` |
| `usage_counters` | Contadores atómicos por `(app, contractor, métrica, período)`. `contractor_id NOT NULL`. UNIQUE `(client_app_id, contractor_id, metric_code, period_start, period_end)`. Incrementar con `UPDATE ... SET used_value = used_value + delta` |

**Campos eliminados vs modelo v1 en `usage_counters`:** `subscriber_tenant_id`, `subscriber_tenant_user_id`

---

### V15 — Billing Support Tables

**Tablas creadas:** `tenant_billing_profiles`, `payment_methods`

| Tabla | Descripción |
|---|---|
| `tenant_billing_profiles` | Datos fiscales por Tenant. `billing_type = PERSONAL \| COMPANY`. Índice único parcial para `is_default` |
| `payment_methods` | Tokens PSP por Tenant. **NUNCA almacena PAN ni CVV**. `provider = STRIPE \| MERCADOPAGO \| PAYPAL \| MANUAL \| MOCK`. Índice único parcial para `is_default` |

---

### V16 — Seed: Foundation

**Propósito:** Datos base para desarrollo. Tenants + apps + usuarios + roles + memberships con contraseñas correctas. Incluye `keygo_contractor` (usuario contratante del modelo billing v2).

| Usuario | Email | Contraseña | Tenant | Rol |
|---|---|---|---|---|
| `keygo_admin` | `admin@keygo.local` | `Admin1234!` | keygo | admin |
| `keygo_tenant_admin` | `tenant-admin@keygo.local` | `Admin1234!` | keygo | admin_tenant |
| `keygo_user` | `user@keygo.local` | `Admin1234!` | keygo | user_tenant |
| `demo_admin` | `admin@demo.local` | `DevAdmin1!` | demo | demo_admin |
| `demo_user` | `user@demo.local` | `DevUser1!` | demo | demo_user |

---

### V17 — Seed: Billing Plans

**Propósito:** Catálogo completo para `keygo-ui`. Escalera completa en USD. 6 planes, versiones v1.0 únicas por plan, billing options y entitlements completos.

| Plan | Versión | Precio/mes | Trial | MAX_TENANTS | MAX_APPS | MAX_USERS | MAX_ADMINS |
|---|---|---|---|---|---|---|---|
| FREE | v1.0 | $0 | 0 días | 1 | 1 | 3 | 1 |
| PERSONAL | v1.0 | $5 | 14 días | 1 | 3 | 5 | 1 |
| TEAM | v1.0 | $49 | 14 días | 1 | 10 | 25 | 3 |
| BUSINESS | v1.0 | $149 | 14 días | 1 | 30 | 100 | 10 |
| FLEX | v1.0 | Por uso | 0 días | ∞ (SOFT) | ∞ (SOFT) | ∞ (SOFT) | 1/tenant+tarifa |
| ENTERPRISE | v1.0 | Custom | 30 días | ∞ (SOFT) | ∞ (SOFT) | ∞ (SOFT) | ∞ (SOFT) |

---

### V18 — Seed: Contractors

**Propósito:** Datos de ejemplo del modelo billing v2. Demuestra el ciclo completo.

| Entidad | Detalle |
|---|---|
| `tenant_users` | `keygo_contractor` / `contractor@keygo.local` (Admin1234!) en tenant `keygo` |
| `memberships` | membership en `keygo-ui` con rol `user_tenant` |
| `contractors` | ID `88888888-8888-8888-8888-000000000001`, status `ACTIVE` |
| `app_contracts` | ID `99999999-9999-9999-9999-000000000001`, plan PERSONAL v1.0, status `ACTIVE` |
| `app_subscriptions` | ID `99999999-9999-9999-9999-000000000002`, plan PERSONAL v1.0, período 2026-03-30→2026-04-30 |
| `tenants` | Tenant `acme` (ID `aaaaaaaa-aaaa-aaaa-aaaa-000000000001`) con `contractor_id` → acme |
| `tenants` | Tenant `demo` actualizado con `contractor_id` → acme |

---

### V19 — User Status: Reset Password + Password Reset Tokens

**Tablas creadas/modificadas:** `tenant_users` (nuevo estado), `password_reset_tokens`

| Elemento | Descripción |
|---|---|
| `tenant_users.status` | Nuevo valor `RESET_PASSWORD` — bloquea login, obliga a flujo de cambio de contraseña |
| `password_reset_tokens` | Tabla para tokens temporales de reset; `user_id → tenant_users(id)`, `expires_at`, `used_at` |

---

### V20 — App Role Hierarchy

**Tablas creadas:** `app_role_hierarchy`

| Tabla | Descripción |
|---|---|
| `app_role_hierarchy` | Relación parent/child entre roles de la misma app. Restricción de ciclo y profundidad ≤ 5. PK compuesta `(parent_role_id, child_role_id)`. Índice `idx_role_hierarchy_child`. |

**CTE recursiva** — usada en `MembershipRepositoryAdapter.findEffectiveRoleCodesByUserAndClientApp()` para expandir roles heredados al emitir JWT.

---

### V21 — User Notification Preferences

**Tablas creadas:** `user_notification_preferences`

| Columna | Tipo | Default | Descripción |
|---|---|---|---|
| `id` | UUID PK | `gen_random_uuid()` | Identificador único |
| `user_id` | UUID FK → `tenant_users(id)` ON DELETE CASCADE | — | Usuario propietario |
| `tenant_id` | UUID FK → `tenants(id)` ON DELETE CASCADE | — | Tenant de contexto |
| `security_alerts_email` | BOOLEAN NOT NULL | `TRUE` | Alertas de seguridad por email |
| `security_alerts_in_app` | BOOLEAN NOT NULL | `TRUE` | Alertas de seguridad in-app |
| `billing_alerts_email` | BOOLEAN NOT NULL | `TRUE` | Alertas de billing por email |
| `product_updates_email` | BOOLEAN NOT NULL | `FALSE` | Actualizaciones de producto por email |
| `weekly_digest` | BOOLEAN NOT NULL | `FALSE` | Resumen semanal por email |
| `created_at` | TIMESTAMPTZ NOT NULL | `NOW()` | Timestamp de creación |
| `updated_at` | TIMESTAMPTZ NOT NULL | `NOW()` | Timestamp de última modificación |

- UNIQUE `(user_id, tenant_id)` — un registro por par usuario+tenant
- Índice `idx_notif_prefs_user_tenant` sobre `(user_id, tenant_id)`

---

### V23 — `password_reset_codes`

**Propósito:** Tabla para códigos de verificación en el flujo de cambio de contraseña forzado (`status=RESET_PASSWORD`).

| Campo | Tipo | Default | Descripción |
|---|---|---|---|
| `id` | UUID PK | `gen_random_uuid()` | Identificador único |
| `tenant_user_id` | UUID FK NOT NULL | — | Usuario al que pertenece el código → `tenant_users(id)` ON DELETE CASCADE |
| `code` | VARCHAR(6) NOT NULL | — | Código numérico de 6 dígitos |
| `expires_at` | TIMESTAMPTZ NOT NULL | — | Expiración (creación + 15 minutos) |
| `used_at` | TIMESTAMPTZ | — | `NULL` = activo; timestamp cuando se verificó |
| `created_at` | TIMESTAMPTZ NOT NULL | `NOW()` | Timestamp de creación |

- UNIQUE `(tenant_user_id)` — un código activo por usuario
- Índice `idx_password_reset_codes_user` sobre `password_reset_codes(tenant_user_id)`

> ⚠️ **Nota:** Esta tabla fue consolidada en V31 dentro de `verification_codes` con `purpose='PASSWORD_RESET'`.

---

### V24 — `platform_roles` + `platform_user_roles`

**Propósito:** Primer nivel de RBAC multi-ámbito — roles a nivel de plataforma.

#### Tabla `platform_roles`

| Campo | Tipo | Default | Descripción |
|---|---|---|---|
| `id` | UUID PK | `gen_random_uuid()` | Identificador único |
| `code` | VARCHAR(50) NOT NULL UNIQUE | — | Código del rol (ej. `keygo_admin`) |
| `name` | VARCHAR(255) NOT NULL | — | Nombre legible |
| `description` | TEXT | — | Descripción del rol |
| `created_at` | TIMESTAMPTZ NOT NULL | `NOW()` | Timestamp de creación |
| `updated_at` | TIMESTAMPTZ NOT NULL | `NOW()` | Última modificación |

- UNIQUE `(code)` — `uq_platform_roles_code`
- Índice `idx_platform_roles_code`
- Trigger `platform_roles_updated_at`

#### Tabla `platform_user_roles`

| Campo | Tipo | Default | Descripción |
|---|---|---|---|
| `id` | UUID PK | `gen_random_uuid()` | Identificador único |
| `tenant_user_id` | UUID FK NOT NULL | — | → `tenant_users(id)` ON DELETE CASCADE |
| `platform_role_id` | UUID FK NOT NULL | — | → `platform_roles(id)` ON DELETE CASCADE |
| `assigned_at` | TIMESTAMPTZ NOT NULL | `NOW()` | Fecha de asignación |
| `created_at` | TIMESTAMPTZ NOT NULL | `NOW()` | Timestamp de creación |
| `updated_at` | TIMESTAMPTZ NOT NULL | `NOW()` | Última modificación |

- UNIQUE `(tenant_user_id, platform_role_id)` — `uq_platform_user_roles_user_role`
- Índices `idx_platform_user_roles_tenant_user_id`, `idx_platform_user_roles_platform_role_id`
- Trigger `platform_user_roles_updated_at`

> ⚠️ **Nota:** En V28, la FK `tenant_user_id` se migró a `platform_user_id` → `platform_users(id)`.

---

### V25 — `tenant_roles` + `tenant_user_roles`

**Propósito:** Segundo nivel de RBAC multi-ámbito — roles definidos por cada tenant con soporte de soft-delete para auditoría.

#### Tabla `tenant_roles`

| Campo | Tipo | Default | Descripción |
|---|---|---|---|
| `id` | UUID PK | `gen_random_uuid()` | Identificador único |
| `tenant_id` | UUID FK NOT NULL | — | → `tenants(id)` ON DELETE CASCADE |
| `code` | VARCHAR(50) NOT NULL | — | Código del rol (UPPERCASE por convención) |
| `name` | VARCHAR(255) NOT NULL | — | Nombre legible |
| `description` | TEXT | — | Descripción del rol |
| `active` | BOOLEAN NOT NULL | `true` | Roles inactivos no se pueden asignar, pero asignaciones existentes permanecen |
| `created_at` | TIMESTAMPTZ NOT NULL | `NOW()` | Timestamp de creación |
| `updated_at` | TIMESTAMPTZ NOT NULL | `NOW()` | Última modificación |

- UNIQUE `(tenant_id, code)` — `uq_tenant_roles_tenant_code`
- Índices `idx_tenant_roles_tenant_id`, `idx_tenant_roles_code`, `idx_tenant_roles_active`
- Trigger `tenant_roles_updated_at`

#### Tabla `tenant_user_roles`

| Campo | Tipo | Default | Descripción |
|---|---|---|---|
| `id` | UUID PK | `gen_random_uuid()` | Identificador único |
| `tenant_user_id` | UUID FK NOT NULL | — | → `tenant_users(id)` ON DELETE CASCADE |
| `tenant_role_id` | UUID FK NOT NULL | — | → `tenant_roles(id)` ON DELETE CASCADE |
| `assigned_at` | TIMESTAMPTZ NOT NULL | `NOW()` | Fecha de asignación |
| `removed_at` | TIMESTAMPTZ | — | `NULL` = activo; timestamp de revocación (soft-delete) |
| `created_at` | TIMESTAMPTZ NOT NULL | `NOW()` | Timestamp de creación |
| `updated_at` | TIMESTAMPTZ NOT NULL | `NOW()` | Última modificación |

- UNIQUE parcial `(tenant_user_id, tenant_role_id)` WHERE `removed_at IS NULL` — `uq_tenant_user_roles_active`
- Índices `idx_tenant_user_roles_tenant_user_id`, `idx_tenant_user_roles_tenant_role_id`, `idx_tenant_user_roles_removed_at`
- Trigger `tenant_user_roles_updated_at`

---

### V26 — Seed: Platform & Tenant Roles

**Propósito:** Datos de desarrollo — roles de plataforma, roles de tenant, y asignaciones de usuarios.

#### Datos insertados

| Tabla | Registros | Detalle |
|---|---|---|
| `platform_roles` | 3 | `keygo_admin`, `keygo_account_admin`, `keygo_user` |
| `platform_user_roles` | 3 | `keygo_admin`→KEYGO_ADMIN, `keygo_tenant_admin`→KEYGO_ACCOUNT_ADMIN, `keygo_contractor`→KEYGO_USER |
| `tenant_roles` (keygo) | 3 | `KEYGO_ADMIN_INTERNAL`, `KEYGO_EDITOR`, `KEYGO_VIEWER` |
| `tenant_roles` (demo) | 2 | `DEMO_ADMIN`, `DEMO_USER` |
| `tenant_user_roles` | 2 | `keygo_admin`→KEYGO_ADMIN_INTERNAL, `demo_admin`→DEMO_ADMIN |

> ⚠️ **Nota:** En V29, el rol `keygo_account_admin` se renombró a `keygo_tenant_admin`.

---

### V27 — `platform_users`

**Propósito:** Tabla de identidad global de plataforma, separada de `tenant_users`. Un `platform_user` puede estar vinculado a múltiples `tenant_users` en distintos tenants.

| Campo | Tipo | Default | Descripción |
|---|---|---|---|
| `id` | UUID PK | `gen_random_uuid()` | Identificador único |
| `email` | VARCHAR(255) NOT NULL UNIQUE | — | Email global único |
| `username` | VARCHAR(100) NOT NULL UNIQUE | — | Username global único |
| `password_hash` | VARCHAR(255) NOT NULL | — | Hash BCrypt |
| `first_name` | VARCHAR(100) | — | Nombre |
| `last_name` | VARCHAR(100) | — | Apellido |
| `status` | VARCHAR(30) NOT NULL | `'ACTIVE'` | CHECK: `ACTIVE`, `SUSPENDED`, `PENDING`, `RESET_PASSWORD` |
| `email_verified_at` | TIMESTAMPTZ | — | Timestamp de verificación de email |
| `phone_number` | VARCHAR(30) | — | Teléfono |
| `locale` | VARCHAR(10) | — | Locale (ej. `es-CL`) |
| `zoneinfo` | VARCHAR(50) | — | Zona horaria (ej. `America/Santiago`) |
| `profile_picture_url` | TEXT | — | URL de avatar |
| `created_at` | TIMESTAMPTZ NOT NULL | `NOW()` | Timestamp de creación |
| `updated_at` | TIMESTAMPTZ NOT NULL | `NOW()` | Última modificación |

- UNIQUE `(email)`, UNIQUE `(username)`
- CHECK `status IN ('ACTIVE', 'SUSPENDED', 'PENDING', 'RESET_PASSWORD')`
- Índices `idx_platform_users_email`, `idx_platform_users_username`, `idx_platform_users_status`
- Trigger `trg_platform_users_updated_at`

---

### V28 — Refactor: Sessions + Platform Identity

**Propósito:** Refactorizar sesiones y tokens para soportar identidad de plataforma.

#### Cambios por tabla

| Tabla | Cambio | Detalle |
|---|---|---|
| `tenant_users` | ADD COLUMN | `platform_user_id UUID` → FK `platform_users(id)` ON DELETE SET NULL |
| `platform_user_roles` | RENAME + RE-FK | `tenant_user_id` → `platform_user_id` → FK `platform_users(id)` |
| `sessions` | DROP + ADD | DROP `user_id`, `tenant_id`; ADD `platform_user_id` (nullable → `platform_users`); `client_app_id` → nullable |
| `refresh_tokens` | DROP + ADD | DROP `user_id`, `tenant_id`; ADD `tenant_user_id` (nullable → `tenant_users`); `client_app_id` → nullable |

**Índices parciales:**
- `idx_sessions_platform_user` ON `sessions(platform_user_id)` WHERE `platform_user_id IS NOT NULL`
- `idx_sessions_client_app` ON `sessions(client_app_id)` WHERE `client_app_id IS NOT NULL`
- `idx_refresh_tokens_tenant_user` ON `refresh_tokens(tenant_user_id)` WHERE `tenant_user_id IS NOT NULL`
- `idx_tenant_users_platform_user` ON `tenant_users(platform_user_id)` WHERE `platform_user_id IS NOT NULL`

---

### V29 — Seed: Platform Users + Role Rename

**Propósito:** Datos iniciales de `platform_users`, vinculación a `tenant_users`, y corrección de nombre de rol.

#### Cambios

| Acción | Detalle |
|---|---|
| RENAME rol | `keygo_account_admin` → `keygo_tenant_admin` (code + name) |
| INSERT `platform_users` | 4 usuarios: `keygo_admin`, `keygo_tenant_admin`, `keygo_user`, `keygo_contractor` (contraseña: `Admin1234!`) |
| INSERT `platform_user_roles` | Todos→`keygo_user`; `keygo_admin`→`keygo_admin`; `keygo_tenant_admin`+`keygo_contractor`→`keygo_tenant_admin` |
| UPDATE `tenant_users` | Vincular `platform_user_id` para usuarios keygo donde email coincide |

---

### V30 — Refactor: Billing Contractor → Platform User

**Propósito:** Migración del modelo de billing para usar `platform_users` en lugar de `tenant_users`. Planes, contratos y suscripciones ahora pueden ser de plataforma (sin `client_app_id`).

#### Cambios por tabla

| Tabla | Cambio | Detalle |
|---|---|---|
| `contractors` | MIGRATE FK | `tenant_user_id` → `platform_user_id` (NOT NULL, UNIQUE, FK → `platform_users`) |
| `app_plans` | EXTEND | `subscriber_type` CHECK agrega `'PLATFORM'`; `client_app_id` → nullable; índices parciales |
| `app_contracts` | MODIFY | `client_app_id` → nullable; índices parciales por tipo |
| `app_subscriptions` | MODIFY | `client_app_id` → nullable; UNIQUE parciales por tipo; índices parciales |

**Migración de datos:**
- Planes de `keygo-ui` migrados a plataforma (`client_app_id=NULL`, `subscriber_type='PLATFORM'`)
- Contratos y suscripciones de `keygo-ui` migrados a plataforma
- Contractor recibe rol `keygo_tenant_admin` en `platform_user_roles`

---

### V31 — `verification_codes` (Consolidada)

**Propósito:** Tabla unificada con discriminador `purpose` que reemplaza 3 tablas específicas de códigos de verificación.

| Campo | Tipo | Default | Descripción |
|---|---|---|---|
| `id` | UUID PK | `gen_random_uuid()` | Identificador único |
| `tenant_user_id` | UUID FK NOT NULL | — | → `tenant_users(id)` ON DELETE CASCADE |
| `purpose` | VARCHAR(30) NOT NULL | — | CHECK: `EMAIL_VERIFICATION`, `PASSWORD_RESET`, `PASSWORD_RECOVERY` |
| `code` | VARCHAR(64) NOT NULL | — | Código de verificación |
| `expires_at` | TIMESTAMPTZ NOT NULL | — | Expiración del código |
| `used_at` | TIMESTAMPTZ | — | `NULL` = activo; timestamp cuando se verificó |
| `metadata` | JSONB | — | Datos adicionales (extensible) |
| `created_at` | TIMESTAMPTZ NOT NULL | `NOW()` | Timestamp de creación |

- CHECK `purpose IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET', 'PASSWORD_RECOVERY')`
- CHECK `tenant_user_id IS NOT NULL`
- UNIQUE parcial `(tenant_user_id, purpose)` WHERE `used_at IS NULL` — un código activo por propósito
- Índices `idx_vc_tenant_user`, `idx_vc_code`, `idx_vc_purpose`

**Tablas eliminadas:** `email_verifications`, `password_reset_codes`, `password_recovery_tokens`

---

## 3. Historial de reestructuraciones

| Fecha | Acción |
|---|---|
| 2026-03-29 | Reescritura completa V1–V26 → **V1–V17** por dominio. Elimina parches acumulativos. |
| 2026-03-30 | Reestructuración V1–V17 → **V1–V18** con modelo v2 de billing integrado desde V1. Nueva entidad `contractors` en V11. Backup en `backup_20260330/`. |
| 2026-04-07 | Identidad de plataforma (V23–V31): RBAC multi-ámbito, `platform_users`, refactor sessions/billing, tabla unificada `verification_codes`. |

---

## 4. Workflow para crear una nueva migración

```bash
# 1. Crear el archivo (próxima es V32)
touch keygo-supabase/src/main/resources/db/migration/V32__nombre_descriptivo.sql

# 2. Escribir SQL limpio (estado final, no parches)
# 3. Levantar DB local
./docs/scripts/db/start.sh

# 4. Aplicar (Flyway corre automáticamente al arrancar la app)
./mvnw spring-boot:run -pl keygo-run
# O directamente con el script
./docs/scripts/db/migrate.sh

# 5. Verificar
./docs/scripts/db/info.sh
./docs/scripts/db/validate.sh
```

### Reglas de escritura

- Usar `IF NOT EXISTS` / `IF EXISTS` cuando corresponda
- Definir constraints con nombre explícito (`CONSTRAINT chk_...`, `CONSTRAINT uq_...`)
- Agregar índices relevantes en la misma migración
- Agregar `COMMENT ON TABLE` y `COMMENT ON COLUMN` para columnas clave
- **Nunca** editar ni reusar archivos ya aplicados
- **FK en seeds:** siempre subquery por campo semántico — nunca hardcodear UUIDs de FK

---

## 5. Documentación obligatoria al crear una migración

Al crear `V{n}__*.sql`, actualizar **antes de cerrar la tarea**:

| Documento | Sección |
|---|---|
| `docs/data/MIGRATIONS.md` (este archivo) | Agregar sección `V{n}` |
| `docs/data/DATA_MODEL.md` | Agregar diccionario de la(s) nueva(s) tabla(s) |
| `docs/data/ENTITY_RELATIONSHIPS.md` | Actualizar diagramas de relaciones |

---

## 6. Comandos Flyway

```bash
./docs/scripts/db/info.sh      # estado de todas las migraciones
./docs/scripts/db/migrate.sh   # aplicar pendientes
./docs/scripts/db/validate.sh  # verificar integridad del schema
./docs/scripts/db/repair.sh    # reparar metadatos (si es necesario)
./docs/scripts/db/clean.sh     # ⚠️ borrar schema completo (pide confirmación)
```

---

## Referencias

- [Flyway Docs](https://flywaydb.org/documentation/)
- [`docs/data/DATA_MODEL.md`](DATA_MODEL.md) — Diccionario completo de tablas
- [`docs/data/ENTITY_RELATIONSHIPS.md`](ENTITY_RELATIONSHIPS.md) — Diagramas E/R
- [`docs/development/ENVIRONMENT_SETUP.md`](../development/ENVIRONMENT_SETUP.md) — Variables de entorno
