# Migraciones Flyway — KeyGo Server

> **Última actualización:** 2026-03-23  
> Reemplaza `docs/keygo-supabase/MIGRATIONS.md` (que solo cubría V1–V3).  
> **Próxima migración:** `V13__...`

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

## 4. Workflow para crear una nueva migración

```bash
# 1. Crear el archivo (próxima es V13)
touch keygo-supabase/src/main/resources/db/migration/V13__descripcion_del_cambio.sql

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

