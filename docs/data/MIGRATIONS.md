# Migraciones Flyway — KeyGo Server

> **Última actualización:** 2026-03-30  
> **Reestructuración total (2026-03-30):** V1–V17 reemplazadas por **V1–V18** con modelo v2 de billing integrado desde el origen. Backup en `backup_20260330/`.  
> **Próxima migración:** `V19__...`

---

## 1. Estrategia

- Herramienta: **Flyway** (perfil `supabase`)
- Ubicación: `keygo-supabase/src/main/resources/db/migration/`
- `ddl-auto: validate` — Flyway gestiona el schema; Hibernate solo valida
- **Regla de oro:** nunca editar ni reusar un archivo de migración ya aplicado

### Convención de nombres

```
V{numero}__{descripcion_con_underscores}.sql
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

> `contractor_id` se declara sin FK en V3 (la tabla `contractors` aún no existe). La FK se agrega via `ALTER TABLE` en V11.

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

## 3. Historial de reestructuraciones

| Fecha | Acción |
|---|---|
| 2026-03-29 | Reescritura completa V1–V26 → **V1–V17** por dominio. Elimina parches acumulativos. |
| 2026-03-30 | Reestructuración V1–V17 → **V1–V18** con modelo v2 de billing integrado desde V1. Nueva entidad `contractors` en V11. Backup en `backup_20260330/`. |

---

## 4. Workflow para crear una nueva migración

```bash
# 1. Crear el archivo (próxima es V19)
touch keygo-supabase/src/main/resources/db/migration/V19__nombre_descriptivo.sql

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
