# Migraciones Flyway — KeyGo Server

> **Última actualización:** 2026-03-29  
> Reemplaza `docs/keygo-supabase/MIGRATIONS.md` (que solo cubría V1–V3).  
> **Próxima migración:** `V20__...`

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

### V1 — `V1__initial_schema.sql`

**Tablas creadas:** `users`, `roles`, `user_roles`, `permissions`, `role_permissions`

| Tabla | Descripción |
|---|---|
| `users` | Usuarios del sistema (legacy, pre-multitenancy) |
| `roles` | Roles globales |
| `user_roles` | Relación N:M usuarios ↔ roles |
| `permissions` | Permisos con enum `action` (CREATE/READ/UPDATE/DELETE/EXECUTE) |
| `role_permissions` | Relación N:M roles ↔ permisos |

---

### V2 — `V2__seed_data.sql`

**Propósito:** Datos iniciales — roles por defecto, permisos básicos y usuario administrador inicial.

---

### V3 — `V3__add_oauth_support.sql`

**Tablas creadas:** `oauth_providers`, `oauth_tokens`

| Tabla | Descripción |
|---|---|
| `oauth_providers` | Configuración de proveedores OAuth externos |
| `oauth_tokens` | Tokens de OAuth almacenados por usuario y proveedor |

---

### V4 — `V4__add_tenants.sql`

**Tablas creadas:** `tenants`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | `@GeneratedValue(UUID)` |
| `slug` | VARCHAR UNIQUE | Identificador URL-friendly del tenant |
| `name` | VARCHAR | Nombre legible |
| `status` | VARCHAR CHECK | `ACTIVE \| SUSPENDED \| PENDING` |
| `created_at` | TIMESTAMPTZ | `@CreationTimestamp` |
| `updated_at` | TIMESTAMPTZ | `@UpdateTimestamp` |

**Índices:** `idx_tenants_slug` (UNIQUE), `idx_tenants_status`

---

### V5 — `V5__add_client_apps.sql`

**Tablas creadas:** `client_apps`, `client_redirect_uris`, `client_allowed_grants`, `client_allowed_scopes`

| Tabla | Descripción |
|---|---|
| `client_apps` | Aplicaciones OAuth2 registradas por tenant |
| `client_redirect_uris` | URIs de redirección permitidas por app |
| `client_allowed_grants` | Grant types permitidos (`authorization_code`, `client_credentials`, etc.) |
| `client_allowed_scopes` | Scopes permitidos por app |

**Columnas clave de `client_apps`:**

| Columna | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `tenant_id` | UUID FK → `tenants` | Cascade no propagado; ON DELETE RESTRICT |
| `client_id` | VARCHAR UNIQUE | Identificador público del cliente OAuth2 |
| `hashed_secret` | VARCHAR | Solo para CONFIDENTIAL clients |
| `type` | VARCHAR | `PUBLIC \| CONFIDENTIAL` |
| `name` | VARCHAR | |
| `status` | VARCHAR | `ACTIVE \| SUSPENDED` |

---

### V6 — `V6__add_tenant_users.sql`

**Tablas creadas:** `tenant_users`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `tenant_id` | UUID FK → `tenants` | ON DELETE CASCADE |
| `email` | VARCHAR | UNIQUE(tenant_id, email) |
| `username` | VARCHAR | UNIQUE(tenant_id, username) |
| `hashed_password` | VARCHAR | bcrypt hash |
| `display_name` | VARCHAR | |
| `status` | VARCHAR | `ACTIVE \| SUSPENDED \| PENDING` |
| `created_at` | TIMESTAMPTZ | |
| `updated_at` | TIMESTAMPTZ | |

---

### V7 — `V7__add_memberships.sql`

**Tablas creadas (en singular, renombradas en V10):** `app_role`, `membership`, `membership_role`

> ⚠️ Los nombres originales de V7 eran en **singular**. V10 los renombra al plural canónico.
> Las entidades JPA y el código fuente usan ya los nombres con plural.

---

### V8 — `V8__add_oauth_authorization_codes.sql`

**Tablas creadas:** `authorization_codes`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `code` | VARCHAR UNIQUE | Código temporal de autorización |
| `client_app_id` | UUID FK → `client_apps` | |
| `tenant_id` | UUID FK → `tenants` | |
| `tenant_user_id` | UUID FK → `tenant_users` | |
| `redirect_uri` | VARCHAR | URI a la que redirigir tras el canje |
| `scope` | VARCHAR | Scopes autorizados (espacio-separados) |
| `code_challenge` | VARCHAR | Valor del challenge PKCE |
| `code_challenge_method` | VARCHAR | `plain` o `S256` |
| `nonce` | VARCHAR | Nonce OIDC (opcional) |
| `status` | VARCHAR | `PENDING \| CONSUMED \| EXPIRED \| REVOKED` |
| `expires_at` | TIMESTAMPTZ | TTL corto (típicamente 5 min) |
| `created_at` | TIMESTAMPTZ | |

**Índices:** `idx_authorization_codes_code` (UNIQUE), `idx_authorization_codes_client_app_id`, `idx_authorization_codes_tenant_user_id`

---

### V9 — `V9__add_signing_keys.sql`

**Tablas creadas:** `signing_keys`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | UUID PK | |
| `kid` | VARCHAR UNIQUE | Key ID para el header JWT y JWKS |
| `algorithm` | VARCHAR | `RS256 \| RS384 \| RS512` |
| `status` | VARCHAR CHECK | `ACTIVE \| RETIRED \| REVOKED` |
| `public_material` | TEXT | PEM X.509/SPKI (clave pública) |
| `private_material` | TEXT | PEM PKCS#8 (clave privada, cifrar en reposo) |
| `activated_at` | TIMESTAMPTZ | |
| `retired_at` | TIMESTAMPTZ | Nullable |
| `created_at` | TIMESTAMPTZ | |
| `updated_at` | TIMESTAMPTZ | |

**Restricción:** solo puede haber **una** clave con status `ACTIVE` por algoritmo (enforced en negocio).

---

### V10 — `V10__rename_membership_tables_to_plural.sql`

**Propósito:** Renombrar las tablas de V7 de singular a plural.

| Nombre original (V7) | Nombre actual |
|---|---|
| `app_role` | `app_roles` |
| `membership` | `memberships` |
| `membership_role` | `membership_roles` |

Esto alinea los nombres de tabla con la convención del resto del schema.

---

## 3. Tabla resumen de entidades JPA ↔ migraciones

| Entidad JPA | Tabla | Migración |
|---|---|---|
| `UserEntity` | `users` | V1 |
| `RoleEntity` | `roles` | V1 |
| `PermissionEntity` | `permissions` | V1 |
| `TenantEntity` | `tenants` | V4 |
| `ClientAppEntity` | `client_apps` | V5 |
| `ClientRedirectUriEntity` | `client_redirect_uris` | V5 |
| `ClientAllowedGrantEntity` | `client_allowed_grants` | V5 |
| `ClientAllowedScopeEntity` | `client_allowed_scopes` | V5 |
| `TenantUserEntity` | `tenant_users` | V6 |
| `AppRoleEntity` | `app_roles` | V7 + V10 |
| `MembershipEntity` | `memberships` | V7 + V10 |
| `AuthorizationCodeEntity` | `authorization_codes` | V8 |
| `SigningKeyEntity` | `signing_keys` | V9 |
| `SessionEntity` | `sessions` | V11 |
| `RefreshTokenEntity` | `refresh_tokens` | V11 |
| `EmailVerificationEntity` | `email_verifications` | V12 |

---

### V11 — `V11__add_refresh_tokens_and_sessions.sql`

**Propósito:** Soporte para Refresh Tokens (rotación SHA-256) y Sesiones de usuario (Fase 7).

**Tablas creadas:**

| Tabla | Descripción |
|---|---|
| `sessions` | Sesión OAuth2 de usuario; agrupa todos los refresh tokens emitidos en ese contexto |
| `refresh_tokens` | Hash SHA-256 del refresh token plano; nunca almacena el token en claro |

**Columnas clave `sessions`:** `id UUID PK`, `tenant_id FK`, `client_app_id FK`, `user_id FK`, `status VARCHAR(20) CHECK(ACTIVE|TERMINATED|EXPIRED)`, `expires_at`, `last_accessed_at`, `user_agent TEXT`, `ip_address VARCHAR(64)`, `created_at`.

**Columnas clave `refresh_tokens`:** `id UUID PK`, `token_hash VARCHAR(64) UNIQUE` (SHA-256 hex), `session_id FK`, `tenant_id FK`, `client_app_id FK`, `user_id FK`, `requested_scopes TEXT`, `status VARCHAR(20) CHECK(ACTIVE|USED|EXPIRED|REVOKED)`, `expires_at`, `used_at`, `replaced_by_id FK self-ref`, `created_at`.

**Índices:** `idx_sessions_user_tenant`, `idx_sessions_status`, `idx_refresh_tokens_hash` (para búsqueda por token), `idx_refresh_tokens_session`, `idx_refresh_tokens_user_tenant`, `idx_refresh_tokens_status`.

**Decisión de diseño:** El campo `token_hash` usa SHA-256 (determinista, 64 hex chars) en lugar de BCrypt para permitir búsqueda directa en DB. El token plano se genera con `SecureRandom` (256 bits) y se entrega al cliente una sola vez.

---

### V12 — `V12__add_email_verifications.sql`

**Propósito:** Soporte para verificación de email en el flujo de auto-registro de usuarios. Cada fila representa un intento de verificación; la fila más reciente por usuario es el código activo.

**Tablas creadas:**

| Tabla | Descripción |
|---|---|
| `email_verifications` | Código de verificación de email generado al registrar un usuario con estado PENDING |

**Columnas clave `email_verifications`:**

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | `UUID PK` | Identificador único generado con `gen_random_uuid()` |
| `tenant_user_id` | `UUID FK NOT NULL` | Referencia a `tenant_users(id)` con `ON DELETE CASCADE` |
| `code` | `VARCHAR(10) NOT NULL` | Código numérico de 6 dígitos generado con `SecureRandom` |
| `expires_at` | `TIMESTAMPTZ NOT NULL` | Expiración del código (30 minutos desde la creación) |
| `used_at` | `TIMESTAMPTZ` | Nullable — se llena cuando el usuario lo usa exitosamente |
| `created_at` | `TIMESTAMPTZ NOT NULL DEFAULT NOW()` | Timestamp de creación |

**Índices:** `idx_email_verifications_tenant_user_id`, `idx_email_verifications_code`.

**Reglas de negocio:**
- Solo el código más reciente (último `created_at`) es el activo para un usuario
- El código expira 30 minutos después de su creación
- Un nuevo código solo puede solicitarse cuando el anterior ha expirado (`expires_at < NOW()`)
- Una vez usado (`used_at IS NOT NULL`), el código no puede reutilizarse
- Al verificar exitosamente, el campo `used_at` se actualiza y el usuario pasa de `PENDING` a `ACTIVE` en `tenant_users`

---

### V13 — `V13__extend_tenant_user_profile.sql`

**Tablas modificadas:** `tenant_users`

**Propósito:** Extender la tabla `tenant_users` con los campos de perfil estándar OIDC §5.3.

**Decisión de diseño:** El perfil canónico del usuario vive en `tenant_users` (nivel tenant), no en `memberships` (nivel app). Esto sigue el patrón OIDC §5.3, Auth0 y Keycloak: "el usuario tiene un perfil, las apps tienen atributos de membresía".

**Columnas agregadas:**

| Columna | Tipo | Nullable | Scope OIDC | Descripción |
|---|---|---|---|---|
| `phone_number` | `VARCHAR(30)` | ✅ | `phone` | Número de teléfono en formato E.164 |
| `locale` | `VARCHAR(10)` | ✅ | `profile` | Locale BCP47, e.g. `es-MX` |
| `zoneinfo` | `VARCHAR(50)` | ✅ | `profile` | tz database, e.g. `America/Mexico_City` |
| `profile_picture_url` | `TEXT` | ✅ | `profile` | URL externa de foto de perfil |
| `birthdate` | `DATE` | ✅ | `profile` | Fecha de nacimiento ISO 8601 |
| `website` | `VARCHAR(2048)` | ✅ | `profile` | URL del sitio web personal |

**Nuevos endpoints habilitados por esta migración:**
- `GET /api/v1/tenants/{slug}/account/profile` — perfil propio (Bearer token)
- `PATCH /api/v1/tenants/{slug}/account/profile` — editar perfil propio (Bearer token)
- `/userinfo` ahora retorna los campos extendidos (`given_name`, `family_name`, `picture`, `locale`, `zoneinfo`, `birthdate`, `website`, `phone_number`)

---

### V14 — `V14__seed_initial_ui_tenants.sql`

**Tipo:** Seed de datos base para desarrollo de UI (sin cambios de schema).

**Objetivo:** Poblar dos tenants operativos (`keygo`, `demo`) con apps, usuarios, roles y memberships mínimas para acelerar pruebas del frontend y flujos OAuth2.

**Datos sembrados:**

| Contexto | Registros |
|---|---|
| `tenants` | `keygo`, `demo` |
| `client_apps` | `key-go-ui` (tenant `keygo`), `demo-ui` (tenant `demo`) |
| `tenant_users` | 5 usuarios seed (3 en `keygo`, 2 en `demo`) |
| `app_roles` | `key-go-ui`: `admin`, `admin_tenant`, `user_tenant`; `demo-ui`: `demo_admin`, `demo_user` |
| `memberships` + `membership_roles` | Asignación 1:1 user→app con rol por defecto según perfil |

**Decisión de diseño importante:** No se insertan datos en tablas legacy (`users`, `user_roles`) porque están en proceso de salida. El seed se concentra en el modelo tenant-scoped (`tenant_users`, `memberships`, `app_roles`).

**Idempotencia aplicada:**
- `ON CONFLICT` para claves naturales (`tenants.slug`, `client_apps.client_id`, `(tenant_id, username)`, `(client_app_id, code)`, `(user_id, client_app_id)`).
- `WHERE NOT EXISTS` para tablas de relación (`client_redirect_uris`, `client_allowed_grants`, `client_allowed_scopes`, `membership_roles`).

---

### V15 — `V15__reset_seed_user_passwords.sql`

**Tipo:** Corrección de datos seed (sin cambios de schema).

**Objetivo:** Reemplazar el hash BCrypt de los usuarios seed de V2 y V14 cuya contraseña en texto plano no fue documentada, por hashes de contraseñas conocidas y verificadas.

**Credenciales de desarrollo (solo local/dev):**

| Tabla | Usuario | Email | Contraseña | Tenant | Rol |
|---|---|---|---|---|---|
| `users` (legacy) | `admin` | `admin@keygo.local` | `Admin1234!` | — | ADMIN legacy |
| `tenant_users` | `keygo_admin` | `admin@keygo.local` | `Admin1234!` | `keygo` | `admin` |
| `tenant_users` | `keygo_tenant_admin` | `tenant-admin@keygo.local` | `Admin1234!` | `keygo` | `admin_tenant` |
| `tenant_users` | `keygo_user` | `user@keygo.local` | `Admin1234!` | `keygo` | `user_tenant` |
| `tenant_users` | `demo_admin` | `admin@demo.local` | `DevAdmin1!` | `demo` | `demo_admin` |
| `tenant_users` | `demo_user` | `user@demo.local` | `DevUser1!` | `demo` | `demo_user` |

> ⚠️ **Nunca usar estas credenciales en producción.** Son exclusivamente para entornos de desarrollo local.

---

### V16 — `V16__add_billing_catalog.sql`

**Tablas creadas:** `app_plans`, `app_plan_versions`, `app_plan_entitlements`

**Propósito:** Catálogo de planes de billing por `ClientApp`. Cada app puede definir su propio catálogo de planes con versiones inmutables y entitlements (límites y feature flags).

**Decisión de diseño:** Los planes son `app-scoped` (pertenecen a una `ClientApp`, no al tenant globalmente). El campo `subscriber_type` determina si el plan es para suscriptores tipo `TENANT` (B2B, empresa) o `TENANT_USER` (B2C, individuo). Un app puede tener planes de ambos tipos simultáneamente.

#### Tabla `app_plans`

| Columna | Tipo | Nulable | Descripción |
|---|---|---|---|
| `id` | UUID PK | NO | Identificador único (`gen_random_uuid()`) |
| `client_app_id` | UUID FK → `client_apps.id` ON DELETE CASCADE | NO | App propietaria del plan |
| `code` | VARCHAR(50) | NO | Código único del plan dentro de la app (e.g. `STARTER`, `PRO`) |
| `name` | VARCHAR(100) | NO | Nombre legible del plan |
| `description` | TEXT | SÍ | Descripción opcional |
| `subscriber_type` | VARCHAR(20) CHECK (`TENANT`, `TENANT_USER`) | NO | Tipo de suscriptor objetivo |
| `status` | VARCHAR(20) CHECK (`ACTIVE`, `INACTIVE`) | NO | Estado del plan |
| `is_public` | BOOLEAN | NO | Si aparece en el catálogo público |
| `created_at` | TIMESTAMPTZ | NO | Timestamp de creación |
| `updated_at` | TIMESTAMPTZ | NO | Timestamp de última actualización |

**Constraints:** `UNIQUE(client_app_id, code)` | Índice parcial en `(client_app_id, subscriber_type, status) WHERE is_public=TRUE`

#### Tabla `app_plan_versions`

Snapshots inmutables de precio y período. Las suscripciones siempre apuntan a una versión específica; las versiones existentes no se modifican al publicar nuevas.

| Columna | Tipo | Nulable | Descripción |
|---|---|---|---|
| `id` | UUID PK | NO | Identificador único |
| `app_plan_id` | UUID FK → `app_plans.id` ON DELETE RESTRICT | NO | Plan padre |
| `version` | VARCHAR(20) | NO | Etiqueta de versión (e.g. `1.0`, `2.0`) |
| `currency` | VARCHAR(3) | NO | Moneda ISO-4217 (default `MXN`) |
| `billing_period` | VARCHAR(20) CHECK (`MONTHLY`, `YEARLY`, `ONE_TIME`) | NO | Período de facturación |
| `base_price` | NUMERIC(12,2) | NO | Precio base del período |
| `setup_fee` | NUMERIC(12,2) | NO | Tarifa única de activación |
| `trial_days` | INT | NO | Días de prueba gratuita (0 = sin prueba) |
| `effective_from` | DATE | NO | Fecha de inicio de vigencia |
| `effective_to` | DATE | SÍ | Fecha de fin de vigencia (`NULL` = sin vencimiento) |
| `status` | VARCHAR(20) CHECK (`ACTIVE`, `INACTIVE`, `DEPRECATED`) | NO | Estado de la versión |
| `created_at` | TIMESTAMPTZ | NO | Timestamp de creación |

**Constraints:** `UNIQUE(app_plan_id, version)`

#### Tabla `app_plan_entitlements`

Límites y feature flags por versión de plan.

| Columna | Tipo | Nulable | Descripción |
|---|---|---|---|
| `id` | UUID PK | NO | Identificador único |
| `app_plan_version_id` | UUID FK → `app_plan_versions.id` ON DELETE CASCADE | NO | Versión a la que pertenece |
| `metric_code` | VARCHAR(100) | NO | Código de métrica de negocio (e.g. `MAX_USERS`, `ALLOW_SSO`) |
| `metric_type` | VARCHAR(20) CHECK (`QUOTA`, `BOOLEAN`, `RATE`) | NO | Tipo de métrica |
| `limit_value` | BIGINT | SÍ | Valor límite (`NULL` = ilimitado para QUOTA/RATE) |
| `period_type` | VARCHAR(20) CHECK (`NONE`, `DAY`, `MONTH`) | NO | Período de la cuota (default `NONE`) |
| `enforcement_mode` | VARCHAR(20) CHECK (`HARD`, `SOFT`) | NO | Modo de enforcement (default `HARD`) |
| `is_enabled` | BOOLEAN | NO | Si el entitlement está habilitado |

**Constraints:** `UNIQUE(app_plan_version_id, metric_code)`

---

### V17 — `V17__add_billing_contracts.sql`

**Tablas creadas:** `app_contracts`

**Propósito:** Flujo de onboarding/checkout antes de que se cree una `AppSubscription`. Permite verificar email y confirmar pago antes de activar la suscripción.

**Decisión de diseño:**
- `subscriber_type = TENANT` → onboarding de empresa: crea un nuevo `Tenant` al activar.
- `subscriber_type = TENANT_USER` → signup individual: crea o resuelve un `TenantUser`.
- `company_slug` es globalmente único en `app_contracts` (se convierte en el slug del nuevo tenant al activar).

#### Tabla `app_contracts`

| Columna | Tipo | Nulable | Descripción |
|---|---|---|---|
| `id` | UUID PK | NO | Identificador único del contrato |
| `client_app_id` | UUID FK → `client_apps.id` ON DELETE RESTRICT | NO | App a la que pertenece el contrato |
| `selected_plan_version_id` | UUID FK → `app_plan_versions.id` ON DELETE RESTRICT | NO | Versión del plan seleccionada |
| `billing_period` | VARCHAR(20) CHECK (`MONTHLY`, `YEARLY`, `ONE_TIME`) | NO | Período de facturación elegido |
| `subscriber_type` | VARCHAR(20) CHECK (`TENANT`, `TENANT_USER`) | NO | Tipo de suscriptor |
| `subscriber_tenant_id` | UUID FK → `tenants.id` ON DELETE SET NULL | SÍ | FK al tenant creado (se rellena al activar, B2B) |
| `subscriber_tenant_user_id` | UUID FK → `tenant_users.id` ON DELETE SET NULL | SÍ | FK al usuario creado (se rellena al activar, B2C) |
| `status` | VARCHAR(40) CHECK (7 valores) | NO | Estado del contrato (ver máquina de estados) |
| `contractor_email` | VARCHAR(255) | NO | Email del contratante |
| `contractor_first_name` | VARCHAR(100) | NO | Nombre del contratante |
| `contractor_last_name` | VARCHAR(100) | NO | Apellido del contratante |
| `company_name` | VARCHAR(200) | SÍ | Nombre de empresa (solo B2B) |
| `company_slug` | VARCHAR(100) | SÍ | Slug de empresa — se convierte en `tenant.slug` al activar |
| `company_tax_id` | VARCHAR(100) | SÍ | RFC / Tax ID (solo B2B) |
| `company_address` | TEXT | SÍ | Dirección fiscal (solo B2B) |
| `email_verified_at` | TIMESTAMPTZ | SÍ | Timestamp de verificación de email |
| `payment_verified_at` | TIMESTAMPTZ | SÍ | Timestamp de confirmación de pago |
| `expires_at` | TIMESTAMPTZ | NO | Expiración del contrato (TTL configurable, default 24h) |
| `created_at` | TIMESTAMPTZ | NO | Timestamp de creación |
| `updated_at` | TIMESTAMPTZ | NO | Timestamp de última actualización |

**Constraints:**
- `UNIQUE(company_slug)` — slug único globalmente (B2B)
- `CHECK(NOT (subscriber_tenant_id IS NOT NULL AND subscriber_tenant_user_id IS NOT NULL))` — solo un suscriptor por contrato

**Estados válidos:** `PENDING_EMAIL_VERIFICATION`, `PENDING_PAYMENT`, `READY_TO_ACTIVATE`, `ACTIVATED`, `CANCELLED`, `EXPIRED`, `FAILED`

**Índices:** `idx_app_contracts_client_app`, `idx_app_contracts_status`, `idx_app_contracts_email`, `idx_app_contracts_sub_tenant`, `idx_app_contracts_sub_user`

---

### V18 — `V18__add_billing_subscriptions.sql`

**Tablas creadas:** `app_subscriptions`, `payment_transactions`

**Propósito:** Relación de suscripción activa entre un suscriptor y una versión de plan. Incluye la tabla de transacciones de pago (una por evento de facturación).

#### Tabla `app_subscriptions`

| Columna | Tipo | Nulable | Descripción |
|---|---|---|---|
| `id` | UUID PK | NO | Identificador único |
| `client_app_id` | UUID FK → `client_apps.id` ON DELETE RESTRICT | NO | App propietaria |
| `app_plan_version_id` | UUID FK → `app_plan_versions.id` ON DELETE RESTRICT | NO | Versión del plan activo |
| `contract_id` | UUID FK → `app_contracts.id` ON DELETE SET NULL | SÍ | Contrato origen (puede ser null si creado manualmente) |
| `subscriber_tenant_id` | UUID FK → `tenants.id` ON DELETE RESTRICT | SÍ | Suscriptor B2B (exactamente uno de los dos es no-null) |
| `subscriber_tenant_user_id` | UUID FK → `tenant_users.id` ON DELETE RESTRICT | SÍ | Suscriptor B2C |
| `status` | VARCHAR(20) CHECK (`PENDING`, `ACTIVE`, `PAST_DUE`, `SUSPENDED`, `CANCELLED`, `EXPIRED`) | NO | Estado de la suscripción |
| `current_period_start` | TIMESTAMPTZ | NO | Inicio del período actual |
| `current_period_end` | TIMESTAMPTZ | NO | Fin del período actual |
| `cancel_at_period_end` | BOOLEAN | NO | Si se cancelará al fin del período |
| `cancelled_at` | TIMESTAMPTZ | SÍ | Timestamp de cancelación |
| `next_billing_at` | TIMESTAMPTZ | SÍ | Próxima fecha de renovación |
| `auto_renew` | BOOLEAN | NO | Si renueva automáticamente |
| `created_at` | TIMESTAMPTZ | NO | Timestamp de creación |
| `updated_at` | TIMESTAMPTZ | NO | Timestamp de última actualización |

**Constraints:**
- `UNIQUE(client_app_id, subscriber_tenant_id)` — una suscripción B2B por app
- `UNIQUE(client_app_id, subscriber_tenant_user_id)` — una suscripción B2C por app
- `CHECK(NOT (subscriber_tenant_id IS NOT NULL AND subscriber_tenant_user_id IS NOT NULL))`

#### Tabla `payment_transactions`

| Columna | Tipo | Nulable | Descripción |
|---|---|---|---|
| `id` | UUID PK | NO | Identificador único |
| `contract_id` | UUID FK → `app_contracts.id` ON DELETE SET NULL | SÍ | Contrato origen |
| `subscription_id` | UUID FK → `app_subscriptions.id` ON DELETE SET NULL | SÍ | Suscripción asociada |
| `provider` | VARCHAR(50) CHECK (`MANUAL`, `MOCK`, `MERCADOPAGO`, `STRIPE`, `OTHER`) | NO | Proveedor de pago |
| `provider_reference` | VARCHAR(255) | SÍ | ID de referencia del PSP externo |
| `amount` | NUMERIC(12,2) | NO | Monto cobrado |
| `currency` | VARCHAR(3) | NO | Moneda ISO-4217 (default `MXN`) |
| `status` | VARCHAR(20) CHECK (`PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`, `EXPIRED`) | NO | Estado de la transacción |
| `paid_at` | TIMESTAMPTZ | SÍ | Timestamp de pago exitoso |
| `raw_response` | JSONB | SÍ | Respuesta raw del PSP (para auditoría) |
| `created_at` | TIMESTAMPTZ | NO | Timestamp de creación |

---

### V19 — `V19__add_billing_invoices_and_usage.sql`

**Tablas creadas:** `invoices`, `usage_counters`

**Propósito:** Facturas históricas por período de suscripción y contadores de uso atómicos por métrica.

**Decisión de diseño:**
- Los campos `*_snapshot` en `invoices` capturan el estado en el momento de emisión y son inmutables retroactivamente.
- Los incrementos de `usage_counters` se hacen con `UPDATE ... SET used_value = used_value + delta` para atomicidad a nivel PostgreSQL sin locks de aplicación.

#### Tabla `invoices`

| Columna | Tipo | Nulable | Descripción |
|---|---|---|---|
| `id` | UUID PK | NO | Identificador único |
| `subscription_id` | UUID FK → `app_subscriptions.id` ON DELETE RESTRICT | NO | Suscripción a la que pertenece |
| `invoice_number` | VARCHAR(50) UNIQUE | NO | Número de factura (e.g. `INV-A1B2C3D4`) |
| `status` | VARCHAR(20) CHECK (`DRAFT`, `ISSUED`, `PAID`, `VOID`, `OVERDUE`) | NO | Estado de la factura |
| `issue_date` | DATE | NO | Fecha de emisión |
| `due_date` | DATE | NO | Fecha de vencimiento |
| `period_start` | DATE | NO | Inicio del período facturado |
| `period_end` | DATE | NO | Fin del período facturado |
| `currency` | VARCHAR(3) | NO | Moneda |
| `subtotal` | NUMERIC(12,2) | NO | Subtotal sin impuestos |
| `tax_amount` | NUMERIC(12,2) | NO | Monto de impuestos |
| `total` | NUMERIC(12,2) | NO | Total a pagar |
| `billing_name_snapshot` | VARCHAR(300) | SÍ | Nombre del titular al momento de emisión |
| `billing_tax_id_snapshot` | VARCHAR(100) | SÍ | RFC/Tax ID al momento de emisión |
| `billing_address_snapshot` | TEXT | SÍ | Dirección al momento de emisión |
| `plan_name_snapshot` | VARCHAR(100) | SÍ | Nombre del plan al momento de emisión |
| `plan_version_snapshot` | VARCHAR(20) | SÍ | Versión del plan al momento de emisión |
| `pdf_url` | TEXT | SÍ | URL del PDF de la factura (si generado) |
| `created_at` | TIMESTAMPTZ | NO | Timestamp de creación |

**Índices:** `idx_invoices_subscription`, `idx_invoices_status`

#### Tabla `usage_counters`

| Columna | Tipo | Nulable | Descripción |
|---|---|---|---|
| `id` | UUID PK | NO | Identificador único |
| `client_app_id` | UUID FK → `client_apps.id` ON DELETE CASCADE | NO | App propietaria |
| `subscriber_tenant_id` | UUID FK → `tenants.id` ON DELETE CASCADE | SÍ | Suscriptor B2B |
| `subscriber_tenant_user_id` | UUID FK → `tenant_users.id` ON DELETE CASCADE | SÍ | Suscriptor B2C |
| `metric_code` | VARCHAR(100) | NO | Código de la métrica (e.g. `MAX_USERS`, `EVALUACIONES_POR_MES`) |
| `period_start` | TIMESTAMPTZ | NO | Inicio del período de la cuota |
| `period_end` | TIMESTAMPTZ | NO | Fin del período de la cuota |
| `used_value` | BIGINT | NO | Valor acumulado en el período |
| `updated_at` | TIMESTAMPTZ | NO | Timestamp de última actualización |

**Constraints:**
- `UNIQUE(client_app_id, subscriber_tenant_id, metric_code, period_start, period_end)` (B2B)
- `UNIQUE(client_app_id, subscriber_tenant_user_id, metric_code, period_start, period_end)` (B2C)
- `CHECK(NOT (subscriber_tenant_id IS NOT NULL AND subscriber_tenant_user_id IS NOT NULL))`

**Índices:** `idx_usage_app_tenant`, `idx_usage_app_user`

---

## 4. Workflow para crear una nueva migración

```bash
# 1. Crear el archivo (próxima es V20)
touch keygo-supabase/src/main/resources/db/migration/V20__descripcion_del_cambio.sql

# 2. Escribir el SQL de manera idempotente cuando sea posible
# 3. Levantar DB local
cd keygo-supabase && ./scripts/dev-start.sh

# 4. Aplicar (Flyway corre automáticamente al arrancar la app)
export SPRING_PROFILES_ACTIVE="supabase,local"
./mvnw spring-boot:run -pl keygo-run

# 5. Verificar
./mvnw -pl keygo-supabase test
```

### Reglas de escritura

- Usar `IF NOT EXISTS` / `IF EXISTS` cuando corresponda para mayor seguridad
- Definir constraints con nombre explícito (`CONSTRAINT pk_... PRIMARY KEY`, `CONSTRAINT fk_...`)
- Agregar índices relevantes en la misma migración
- No usar `DROP TABLE` sin `IF EXISTS`
- **Nunca** modificar migraciones ya aplicadas


# 2. Escribir el SQL de manera idempotente cuando sea posible
# 3. Levantar DB local
cd keygo-supabase && ./scripts/dev-start.sh

# 4. Aplicar (Flyway corre automáticamente al arrancar la app)
export SPRING_PROFILES_ACTIVE="supabase,local"
./mvnw spring-boot:run -pl keygo-run

# 5. Verificar
./mvnw -pl keygo-supabase test
```

### Reglas de escritura

- Usar `IF NOT EXISTS` / `IF EXISTS` cuando corresponda para mayor seguridad
- Definir constraints con nombre explícito (`CONSTRAINT pk_... PRIMARY KEY`, `CONSTRAINT fk_...`)
- Agregar índices relevantes en la misma migración
- No usar `DROP TABLE` sin `IF EXISTS`
- **Nunca** modificar migraciones ya aplicadas

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
# Info — estado de todas las migraciones
cd keygo-supabase && ./scripts/info.sh

# Migrate — aplicar migraciones pendientes
cd keygo-supabase && ./scripts/migrate.sh

# Validate — verificar integridad del schema
cd keygo-supabase && ./scripts/validate.sh
```

---

## Referencias

- [Flyway Docs](https://flywaydb.org/documentation/)
- [`docs/data/DATA_MODEL.md`](DATA_MODEL.md) — Diccionario completo de tablas
- [`docs/data/ENTITY_RELATIONSHIPS.md`](ENTITY_RELATIONSHIPS.md) — Diagramas E/R
- [`docs/development/ENVIRONMENT_SETUP.md`](../development/ENVIRONMENT_SETUP.md) — Variables de entorno

