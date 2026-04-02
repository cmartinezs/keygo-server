# Data Model — KeyGo Server

> Documentación del **diccionario de datos** y **modelo de entidades** (E/R) del sistema KeyGo Server.
>
> Fecha de actualización: **2026-03-30** | Estado: ✅ Sincronizado con migraciones V1–V17 + diseño de datos v2

---

## Tabla de contenidos

1. [Tablas activas (multi-tenancy)](#tablas-activas-multi-tenancy)
2. [Tablas legado (V1/V3)](#tablas-legado-v1v3)
3. [Tablas planificadas (fases futuras)](#tablas-planificadas-fases-futuras)
4. [Modelo E/R (Diagrama Mermaid)](#modelo-er-diagrama-mermaid)
5. [Relaciones de dependencia](#relaciones-de-dependencia)
6. [Guías de consulta común](#guías-de-consulta-común)
7. [Notas sobre enumeraciones](#notas-sobre-enumeraciones)
8. [Referencia rápida de constraints únicos](#referencia-rápida-de-constraints-únicos)

---

## Tablas activas (multi-tenancy)

> Estas tablas forman el núcleo del sistema.
> **Identidad y autenticación:** V3–V9 | **Billing:** V10–V14 | **Seeds:** V15–V17.

### Tabla: `tenants` — V3

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único del tenant (`uuid_generate_v4()`) |
| `slug` | VARCHAR(100) | UNIQUE | NO | Identificador URL-friendly (solo minúsculas, números y guiones). Mín. 3 chars. |
| `name` | VARCHAR(255) | | NO | Nombre legal/comercial del tenant |
| `owner_email` | VARCHAR(255) | | NO | Email del propietario/administrador del tenant |
| `status` | VARCHAR(20) | | NO | Estado: `ACTIVE`, `SUSPENDED`, `PENDING`, `DELETED` |
| `contractor_id` | UUID | FK → `contractors.id` | SÍ | Contratante propietario (NULL para tenants de sistema). Se fija al crear el tenant. |
| `created_at` | TIMESTAMPTZ | | NO | Marca de tiempo de creación (UTC) |
| `updated_at` | TIMESTAMPTZ | | NO | Marca de tiempo de última actualización (UTC — auto-actualizado por trigger) |

**Constraints:**
- `UNIQUE(slug)`, `slug ~ '^[a-z0-9][a-z0-9\-]*[a-z0-9]$'`, `char_length(slug) >= 3`
- `status IN ('ACTIVE', 'SUSPENDED', 'PENDING', 'DELETED')`
- FK opcional: `contractor_id` → `contractors(id)` ON DELETE SET NULL

**Reglas de negocio:**
- El `slug` es único globalmente; no puede repetirse entre tenants.
- Un tenant `SUSPENDED` no debe permitir operaciones normales (login, emisión de tokens).
- `contractor_id IS NULL` indica un tenant de sistema/plataforma (p. ej. `keygo`, `demo`).
- `contractor_id IS NOT NULL` indica un tenant creado por un contratante usando el plan activo.
- El límite de tenants por contratante se controla vía el entitlement `MAX_TENANTS` del plan activo: `COUNT(*) FROM tenants WHERE contractor_id = :id AND status != 'DELETED'`.
- Toda entidad con `tenant_id` pertenece lógicamente a este tenant.

---

### Tabla: `client_apps` — V4

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único de la aplicación cliente |
| `tenant_id` | UUID | FK | NO | Referencia al tenant propietario |
| `client_id` | VARCHAR(255) | UNIQUE | NO | ID OAuth2/OIDC único globalmente; usado en requests de autorización |
| `name` | VARCHAR(255) | | NO | Nombre legible de la aplicación |
| `description` | TEXT | | SÍ | Descripción opcional de la app |
| `type` | VARCHAR(20) | | NO | Tipo de cliente: `PUBLIC` (SPA, mobile) o `CONFIDENTIAL` (servidor backend) |
| `hashed_secret` | VARCHAR(255) | | SÍ | Hash del secret (solo si `CONFIDENTIAL`); nunca se expone en claro |
| `status` | VARCHAR(20) | | NO | Estado: `ACTIVE`, `SUSPENDED`, `PENDING` |
| `created_at` | TIMESTAMPTZ | | NO | Marca de tiempo de creación |
| `updated_at` | TIMESTAMPTZ | | NO | Marca de tiempo de última actualización |

**Constraints:**
- `type IN ('PUBLIC', 'CONFIDENTIAL')`
- `status IN ('ACTIVE', 'SUSPENDED', 'PENDING')`

**Reglas de negocio:**
- `type = PUBLIC` no debe tener `hashed_secret` (o ignorarlo en validación).
- `type = CONFIDENTIAL` requiere validar `hashed_secret` en algunos flows.
- Un cliente solo puede usar grants (`client_allowed_grants`) y scopes (`client_allowed_scopes`) registrados.

---

### Tabla: `client_redirect_uris` — V4

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único |
| `client_app_id` | UUID | FK | NO | Referencia al cliente propietario |
| `uri` | VARCHAR(2048) | | NO | URI exacta permitida (sin wildcards) |
| `created_at` | TIMESTAMPTZ | | NO | Marca de tiempo de creación |

**Reglas de negocio:**
- La redirect URI en un `authorize` request debe coincidir exactamente con una entrada aquí.
- No se permiten wildcards; la validación es literal.
- Un cliente puede tener múltiples redirect URIs (dev, staging, producción).

---

### Tabla: `client_allowed_grants` — V4

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único |
| `client_app_id` | UUID | FK | NO | Referencia al cliente propietario |
| `grant_type` | VARCHAR(50) | | NO | Tipo de grant permitido (p. ej. `authorization_code`, `client_credentials`, `refresh_token`) |

**Reglas de negocio:**
- Un cliente solo puede usar grants registrados aquí.
- El servidor debe validar contra esta tabla antes de emitir un token por ese flujo.

---

### Tabla: `client_allowed_scopes` — V4

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único |
| `client_app_id` | UUID | FK | NO | Referencia al cliente propietario |
| `scope` | VARCHAR(100) | | NO | Scope permitido (p. ej. `openid`, `profile`, `email`, `custom:admin`) |

**Reglas de negocio:**
- Un cliente solo puede solicitar scopes registrados aquí.
- El servidor filtra los scopes autorizados contra esta lista.

---

### Tabla: `tenant_users` — V5

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único del usuario |
| `tenant_id` | UUID | FK | NO | Referencia al tenant propietario |
| `username` | VARCHAR(100) | UNIQUE (tenant) | NO | Username único dentro del tenant |
| `email` | VARCHAR(255) | UNIQUE (tenant) | NO | Email único dentro del tenant |
| `password_hash` | VARCHAR(255) | | NO | Hash seguro de contraseña (BCrypt) |
| `first_name` | VARCHAR(100) | | SÍ | Nombre de pila |
| `last_name` | VARCHAR(100) | | SÍ | Apellido |
| `status` | VARCHAR(20) | | NO | Estado: `ACTIVE`, `SUSPENDED`, `PENDING` |
| `created_at` | TIMESTAMPTZ | | NO | Marca de tiempo de creación |
| `updated_at` | TIMESTAMPTZ | | NO | Marca de tiempo de última actualización |

**Constraints:**
- `UNIQUE(tenant_id, email)` — email único por tenant
- `UNIQUE(tenant_id, username)` — username único por tenant
- `status IN ('ACTIVE', 'SUSPENDED', 'PENDING')`
- FK: `tenant_id` → `tenants(id)` ON DELETE CASCADE

**Reglas de negocio:**
- Email y username son únicos por tenant, no globalmente.
- La contraseña nunca se almacena en claro; siempre como hash BCrypt.
- Un usuario `SUSPENDED` no puede autenticarse.

---

### Tabla: `app_roles` — V6

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único del rol |
| `client_app_id` | UUID | FK | NO | Referencia a la aplicación propietaria del rol |
| `code` | VARCHAR(50) | UNIQUE (app) | NO | Código del rol en minúsculas (p. ej. `admin`, `user`, `viewer`). Patrón: `^[a-z][a-z0-9_-]*$` |
| `display_name` | VARCHAR(255) | | SÍ | Nombre legible del rol |
| `description` | TEXT | | SÍ | Descripción de responsabilidades/permisos |
| `created_at` | TIMESTAMPTZ | | NO | Marca de tiempo de creación |
| `updated_at` | TIMESTAMPTZ | | NO | Marca de tiempo de última actualización |

**Constraints:**
- `UNIQUE(client_app_id, code)` — código único dentro de la app
- `code ~ '^[a-z][a-z0-9_-]*$'` — solo minúsculas, números, guiones y underscores
- FK: `client_app_id` → `client_apps(id)` ON DELETE CASCADE

**Reglas de negocio:**
- Un rol siempre pertenece a una app específica; los roles no son globales.
- El `code` es el identificador funcional dentro de la app (p. ej. en JWT claims).
- Diferentes apps pueden tener roles con el mismo código, pero son entidades distintas.

---

### Tabla: `memberships` — V6

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único de la membership |
| `user_id` | UUID | FK | NO | Referencia al usuario (`tenant_users`) |
| `client_app_id` | UUID | FK | NO | Referencia a la aplicación cliente |
| `status` | VARCHAR(20) | | NO | Estado: `ACTIVE`, `SUSPENDED`, `PENDING` |
| `created_at` | TIMESTAMPTZ | | NO | Marca de tiempo de creación |
| `updated_at` | TIMESTAMPTZ | | NO | Marca de tiempo de última actualización |

**Constraints:**
- `UNIQUE(user_id, client_app_id)` — no hay memberships duplicadas
- `status IN ('ACTIVE', 'SUSPENDED', 'PENDING')`
- FK: `user_id` → `tenant_users(id)` ON DELETE CASCADE
- FK: `client_app_id` → `client_apps(id)` ON DELETE CASCADE

**Reglas de negocio:**
- Una membership define si el usuario puede acceder a esa app.
- Un usuario **debe** tener membership `ACTIVE` en una app para autenticarse en ella.
- Estado `SUSPENDED` deniega temporalmente el login.
- `PENDING` = invitación pendiente de aceptación.

---

### Tabla: `membership_roles` — V6

> ⚠️ **PK compuesta** `(membership_id, role_id)` — NO hay columna `id` independiente. La columna FK al rol es `role_id` (no `app_role_id`).

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `membership_id` | UUID | PK (parte) + FK | NO | Referencia a la membership |
| `role_id` | UUID | PK (parte) + FK | NO | Referencia al rol (`app_role`) |
| `assigned_at` | TIMESTAMPTZ | | NO | Marca de tiempo de asignación |

**Constraints:**
- PK compuesta: `(membership_id, role_id)`
- FK: `membership_id` → `memberships(id)` ON DELETE CASCADE
- FK: `role_id` → `app_roles(id)` ON DELETE CASCADE

**Reglas de negocio:**
- Una membership puede tener múltiples roles dentro de la misma app.
- El rol de una membership debe pertenecer a la misma app que la membership.
- Al revocar/eliminar una membership, se eliminan implícitamente todos sus roles (CASCADE).

---

### Tabla: `authorization_codes` — V7

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único del código |
| `code` | VARCHAR(256) | UNIQUE | NO | Valor opaco del código de autorización |
| `client_app_id` | UUID | FK | NO | Referencia al cliente que lo solicitó |
| `tenant_id` | UUID | FK | NO | Referencia al tenant |
| `user_id` | UUID | FK | NO | Referencia al usuario autenticado |
| `code_challenge` | VARCHAR(256) | | NO | Challenge PKCE (valor hash SHA-256 o plain) |
| `code_challenge_method` | VARCHAR(10) | | NO | Método PKCE: `plain` o `S256` |
| `requested_scopes` | TEXT | | NO | Scopes solicitados (serializado) |
| `redirect_uri` | VARCHAR(2048) | | NO | URI de redirección autorizada |
| `status` | VARCHAR(20) | | NO | Estado: `pending`, `used`, `expired`, `revoked` |
| `expires_at` | TIMESTAMPTZ | | NO | Marca de tiempo de expiración (~10 min tras creación) |
| `created_at` | TIMESTAMPTZ | | NO | Marca de tiempo de emisión |
| `used_at` | TIMESTAMPTZ | | SÍ | Marca de tiempo de canje (se llena al marcar `used`) |

**Constraints:**
- `UNIQUE(code)`
- `code_challenge_method IN ('plain', 'S256')`
- `status IN ('pending', 'used', 'expired', 'revoked')`
- FK: `client_app_id` → `client_apps(id)` ON DELETE CASCADE
- FK: `tenant_id` → `tenants(id)` ON DELETE CASCADE
- FK: `user_id` → `tenant_users(id)` ON DELETE CASCADE

**Reglas de negocio:**
- Solo se puede canjear una vez: `pending` → `used` (inmutable después).
- Debe expirar rápidamente (~10 min). `expires_at > NOW()` se valida al canjear.
- El `client_app_id` que canjea debe ser el mismo que lo solicitó.
- Si PKCE fue usado en `/authorize`, se debe validar `code_verifier` contra `code_challenge`.

---

### Tabla: `signing_keys` — V7

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único |
| `kid` | VARCHAR(100) | UNIQUE | NO | Key ID usado en el header `kid` del JWT |
| `algorithm` | VARCHAR(20) | | NO | Algoritmo de firma: `RS256`, `RS384`, `RS512` |
| `status` | VARCHAR(20) | | NO | Estado: `ACTIVE`, `RETIRED`, `REVOKED` |
| `public_material` | TEXT | | NO | Clave pública en formato PEM |
| `private_material` | TEXT | | SÍ | Clave privada en formato PEM (cifrada en reposo recomendada) |
| `activated_at` | TIMESTAMPTZ | | NO | Marca de tiempo de activación |
| `retired_at` | TIMESTAMPTZ | | SÍ | Marca de tiempo de retiro (`null` si sigue activa) |
| `created_at` | TIMESTAMPTZ | | NO | Marca de tiempo de creación |

**Constraints:**
- `UNIQUE(kid)`
- `status IN ('ACTIVE', 'RETIRED', 'REVOKED')`

**Reglas de negocio:**
- Debe existir al menos una clave `ACTIVE` para emitir tokens.
- Las claves `RETIRED` se mantienen para validación de tokens ya emitidos.
- La clave privada se almacena en DB en PEM cifrado — en producción debe migrar a KMS/bóveda.
- Solo una clave `ACTIVE` a la vez; al rotar, la anterior pasa a `RETIRED`.

---

## Tablas legado (V1/V3)

> Estas tablas existen en la DB por las migraciones iniciales pero **no se usan** en el sistema multi-tenancy actual. Se conservan por compatibilidad; no crear nuevos endpoints sobre ellas.

| Tabla | Migración | Descripción | Estado |
|---|---|---|---|
| `users` | V1 | Usuarios globales (sin tenant). Estructura diferente a `tenant_users`. | 🚧 Legado |
| `roles` | V1 | Roles globales del sistema. Sin relación con `app_role`. | 🚧 Legado |
| `user_roles` | V1 | Join table `users ↔ roles`. PK compuesta `(user_id, role_id)`. | 🚧 Legado |
| `permissions` | V1 | Permisos globales con acciones: `CREATE`, `READ`, `UPDATE`, `DELETE`, `EXECUTE`. | 🚧 Legado |
| `role_permissions` | V1 | Join table `roles ↔ permissions`. PK compuesta. | 🚧 Legado |
| `sessions` | V1 | Sesiones de usuario global (token JWT crudo, no multi-tenant). | 🚧 Legado |
| `audit_logs` | V1 | Log de auditoría global por acción + recurso + `user_id`. | 🚧 Legado |
| `oauth_providers` | V3 | Configuración de proveedores OAuth externos (Google, GitHub, etc.). | 🚧 Legado |
| `oauth_tokens` | V3 | Tokens de acceso de proveedores externos por usuario global. | 🚧 Legado |

---

## Tablas planificadas / pendientes

> Estas tablas están en diseño o pendientes de migración. **No tienen migración aplicada aún.**

| Tabla | Descripción | Migración pendiente |
|---|---|---|
| `contractors` | Entidad central de billing — persona/entidad que firma contratos. 1:1 con `tenant_users` en el tenant proveedor | `V19__billing_v2_contractors.sql` |
| `token_blacklist` | Lista negra de JTI de access tokens revocados (opcionalmente en Redis) | futura |
| `audit_events` | Registro de eventos de auditoría por tenant (login, token emitido, revocación) | futura |

> ℹ️ Las tablas `contractors` (billing v2) y modificaciones a `app_contracts`, `app_subscriptions`, `usage_counters` y `tenants` están especificadas en `docs/data/MIGRATIONS.md` §4.

---

## Modelo E/R — Identidad y Autenticación (V3–V9)

> Tablas del núcleo de identidad: tenants, apps, usuarios, auth codes, signing keys, sesiones, refresh tokens y verificación de email.
> Las tablas del modelo legacy anterior (V1: `users`, `roles`, `permissions`) se omiten — ver sección §2.
> Para las tablas de billing ver el diagrama de la sección siguiente (V10–V14).

```mermaid
erDiagram
    TENANTS ||--o{ CLIENT_APPS : "owns (tenant_id)"
    TENANTS ||--o{ TENANT_USERS : "contains (tenant_id)"
    TENANTS ||--o{ AUTHORIZATION_CODES : "issues (tenant_id)"
    TENANTS ||--o{ SESSIONS : "has (tenant_id)"
    TENANTS ||--o{ REFRESH_TOKENS : "owns (tenant_id)"

    CLIENT_APPS ||--o{ CLIENT_REDIRECT_URIS : "registers (client_app_id)"
    CLIENT_APPS ||--o{ CLIENT_ALLOWED_GRANTS : "permits (client_app_id)"
    CLIENT_APPS ||--o{ CLIENT_ALLOWED_SCOPES : "permits (client_app_id)"
    CLIENT_APPS ||--o{ APP_ROLES : "defines (client_app_id)"
    CLIENT_APPS ||--o{ MEMBERSHIPS : "accessed-by (client_app_id)"
    CLIENT_APPS ||--o{ AUTHORIZATION_CODES : "requests (client_app_id)"
    CLIENT_APPS ||--o{ SESSIONS : "used-in (client_app_id)"
    CLIENT_APPS ||--o{ REFRESH_TOKENS : "issued-to (client_app_id)"

    TENANT_USERS ||--o{ MEMBERSHIPS : "has (user_id)"
    TENANT_USERS ||--o{ AUTHORIZATION_CODES : "authenticates (user_id)"
    TENANT_USERS ||--o{ SESSIONS : "owns (user_id)"
    TENANT_USERS ||--o{ REFRESH_TOKENS : "owns (user_id)"
    TENANT_USERS ||--o{ EMAIL_VERIFICATIONS : "has (tenant_user_id)"

    MEMBERSHIPS ||--o{ MEMBERSHIP_ROLES : "assigned (membership_id)"
    APP_ROLES ||--o{ MEMBERSHIP_ROLES : "grants (role_id)"

    SESSIONS ||--o{ REFRESH_TOKENS : "contains (session_id)"
    REFRESH_TOKENS ||--o| REFRESH_TOKENS : "replaced-by (replaced_by_id)"

    TENANTS {
        UUID id PK
        VARCHAR slug UK
        VARCHAR name
        VARCHAR owner_email
        VARCHAR status
        UUID contractor_id FK
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    CLIENT_APPS {
        UUID id PK
        UUID tenant_id FK
        VARCHAR client_id UK
        VARCHAR name
        VARCHAR type
        VARCHAR hashed_secret
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    CLIENT_REDIRECT_URIS {
        UUID id PK
        UUID client_app_id FK
        VARCHAR uri
        TIMESTAMPTZ created_at
    }

    CLIENT_ALLOWED_GRANTS {
        UUID id PK
        UUID client_app_id FK
        VARCHAR grant_type
    }

    CLIENT_ALLOWED_SCOPES {
        UUID id PK
        UUID client_app_id FK
        VARCHAR scope
    }

    TENANT_USERS {
        UUID id PK
        UUID tenant_id FK
        VARCHAR username
        VARCHAR email
        VARCHAR password_hash
        VARCHAR first_name
        VARCHAR last_name
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    APP_ROLES {
        UUID id PK
        UUID client_app_id FK
        VARCHAR code
        VARCHAR display_name
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    MEMBERSHIPS {
        UUID id PK
        UUID user_id FK
        UUID client_app_id FK
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    MEMBERSHIP_ROLES {
        UUID membership_id PK_FK
        UUID role_id PK_FK
        TIMESTAMPTZ assigned_at
    }

    AUTHORIZATION_CODES {
        UUID id PK
        VARCHAR code UK
        UUID client_app_id FK
        UUID tenant_id FK
        UUID user_id FK
        VARCHAR code_challenge
        VARCHAR code_challenge_method
        TEXT requested_scopes
        VARCHAR redirect_uri
        VARCHAR status
        TIMESTAMPTZ expires_at
        TIMESTAMPTZ created_at
        TIMESTAMPTZ used_at
    }

    SIGNING_KEYS {
        UUID id PK
        VARCHAR kid UK
        VARCHAR algorithm
        VARCHAR status
        TEXT public_material
        TEXT private_material
        TIMESTAMPTZ activated_at
        TIMESTAMPTZ retired_at
        TIMESTAMPTZ created_at
    }

    SESSIONS {
        UUID id PK
        UUID tenant_id FK
        UUID client_app_id FK
        UUID user_id FK
        VARCHAR status
        TIMESTAMPTZ expires_at
        TIMESTAMPTZ last_accessed_at
        TEXT user_agent
        VARCHAR ip_address
        TIMESTAMPTZ created_at
    }

    REFRESH_TOKENS {
        UUID id PK
        VARCHAR token_hash UK
        UUID session_id FK
        UUID tenant_id FK
        UUID client_app_id FK
        UUID user_id FK
        TEXT requested_scopes
        VARCHAR status
        TIMESTAMPTZ expires_at
        TIMESTAMPTZ used_at
        UUID replaced_by_id FK
        TIMESTAMPTZ created_at
    }

    EMAIL_VERIFICATIONS {
        UUID id PK
        UUID tenant_user_id FK
        VARCHAR code
        TIMESTAMPTZ expires_at
        TIMESTAMPTZ used_at
        TIMESTAMPTZ created_at
    }
```

---

## Modelo E/R — Billing (V10–V14, modelo v2)

> Tablas de billing: catálogo de planes, contratos, suscripciones, facturas, contadores de uso, perfiles de facturación y métodos de pago.
> **V10:** catálogo (planes, versiones, entitlements) | **V11:** contratos | **V12:** suscripciones + transacciones | **V13:** facturas + contadores de uso | **V14:** perfiles fiscales + métodos de pago.
> **Modelo v2:** el suscriptor es siempre un `CONTRACTOR` — ya no hay columnas polimórficas `subscriber_tenant_id` / `subscriber_tenant_user_id`.
> ⚠️ `CONTRACTORS` es parte del diseño v2 pendiente de implementación en la migración `V19__billing_v2_contractors.sql`. Las columnas `contractor_id` en las tablas actuales serán añadidas en V19.

```mermaid
erDiagram
    TENANT_USERS ||--o| CONTRACTORS : "representa (tenant_user_id UNIQUE)"
    TENANTS ||--o{ TENANTS : "creados por contractor (contractor_id)"
    TENANTS ||--o{ TENANT_BILLING_PROFILES : "perfil fiscal (tenant_id)"
    TENANTS ||--o{ PAYMENT_METHODS : "métodos de pago (tenant_id)"

    CONTRACTORS ||--o{ APP_CONTRACTS : "historial de contratos"
    CONTRACTORS ||--o{ APP_SUBSCRIPTIONS : "suscrito a (contractor_id)"
    CONTRACTORS ||--o{ USAGE_COUNTERS : "acumula uso (contractor_id)"

    CLIENT_APPS ||--o{ APP_PLANS : "owns (client_app_id)"
    CLIENT_APPS ||--o{ APP_CONTRACTS : "scope (client_app_id)"
    CLIENT_APPS ||--o{ APP_SUBSCRIPTIONS : "scope (client_app_id)"
    CLIENT_APPS ||--o{ USAGE_COUNTERS : "scope (client_app_id)"

    APP_PLANS ||--o{ APP_PLAN_VERSIONS : "versiones (app_plan_id)"
    APP_PLAN_VERSIONS ||--o{ APP_PLAN_ENTITLEMENTS : "entitlements (app_plan_version_id)"
    APP_PLAN_VERSIONS ||--o{ APP_CONTRACTS : "seleccionada (selected_plan_version_id)"
    APP_PLAN_VERSIONS ||--o{ APP_SUBSCRIPTIONS : "activa (app_plan_version_id)"

    APP_CONTRACTS ||--o| APP_SUBSCRIPTIONS : "origina (contract_id)"
    APP_CONTRACTS ||--o{ PAYMENT_TRANSACTIONS : "genera (contract_id)"
    APP_SUBSCRIPTIONS ||--o{ PAYMENT_TRANSACTIONS : "tiene (subscription_id)"
    APP_SUBSCRIPTIONS ||--o{ INVOICES : "factura por período (subscription_id)"

    CONTRACTORS {
        UUID id PK
        UUID tenant_user_id FK_UK
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    APP_PLANS {
        UUID id PK
        UUID client_app_id FK
        VARCHAR code
        VARCHAR name
        VARCHAR subscriber_type
        VARCHAR status
        BOOLEAN is_public
        INT sort_order
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    APP_PLAN_VERSIONS {
        UUID id PK
        UUID app_plan_id FK
        VARCHAR version
        VARCHAR currency
        VARCHAR billing_period
        NUMERIC base_price
        NUMERIC setup_fee
        INT trial_days
        DATE effective_from
        DATE effective_to
        VARCHAR status
        TIMESTAMPTZ created_at
    }

    APP_PLAN_ENTITLEMENTS {
        UUID id PK
        UUID app_plan_version_id FK
        VARCHAR metric_code
        VARCHAR metric_type
        BIGINT limit_value
        VARCHAR period_type
        VARCHAR enforcement_mode
        BOOLEAN is_enabled
    }

    APP_CONTRACTS {
        UUID id PK
        UUID client_app_id FK
        UUID contractor_id FK
        UUID selected_plan_version_id FK
        VARCHAR billing_period
        VARCHAR status
        VARCHAR contractor_email
        VARCHAR contractor_first_name
        VARCHAR contractor_last_name
        VARCHAR company_name
        VARCHAR company_tax_id
        TEXT company_address
        VARCHAR verification_code
        TIMESTAMPTZ email_verified_at
        TIMESTAMPTZ payment_verified_at
        TIMESTAMPTZ expires_at
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    APP_SUBSCRIPTIONS {
        UUID id PK
        UUID client_app_id FK
        UUID app_plan_version_id FK
        UUID contract_id FK
        UUID contractor_id FK
        VARCHAR status
        TIMESTAMPTZ current_period_start
        TIMESTAMPTZ current_period_end
        BOOLEAN cancel_at_period_end
        TIMESTAMPTZ cancelled_at
        TIMESTAMPTZ next_billing_at
        BOOLEAN auto_renew
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    PAYMENT_TRANSACTIONS {
        UUID id PK
        UUID contract_id FK
        UUID subscription_id FK
        VARCHAR provider
        VARCHAR provider_reference
        NUMERIC amount
        VARCHAR currency
        VARCHAR status
        TIMESTAMPTZ paid_at
        TIMESTAMPTZ created_at
    }

    INVOICES {
        UUID id PK
        UUID subscription_id FK
        VARCHAR invoice_number UK
        VARCHAR status
        DATE issue_date
        DATE due_date
        DATE period_start
        DATE period_end
        VARCHAR currency
        NUMERIC subtotal
        NUMERIC tax_amount
        NUMERIC total
        VARCHAR billing_name_snapshot
        VARCHAR billing_tax_id_snapshot
        VARCHAR plan_name_snapshot
        VARCHAR plan_version_snapshot
        TEXT pdf_url
        TIMESTAMPTZ created_at
    }

    USAGE_COUNTERS {
        UUID id PK
        UUID client_app_id FK
        UUID contractor_id FK
        VARCHAR metric_code
        TIMESTAMPTZ period_start
        TIMESTAMPTZ period_end
        BIGINT used_value
        TIMESTAMPTZ updated_at
    }

    TENANT_BILLING_PROFILES {
        UUID id PK
        UUID tenant_id FK
        VARCHAR billing_type
        VARCHAR billing_name
        VARCHAR tax_id
        VARCHAR tax_regime
        VARCHAR address_line1
        VARCHAR address_line2
        VARCHAR city
        VARCHAR state
        VARCHAR country
        VARCHAR postal_code
        VARCHAR contact_email
        VARCHAR contact_phone
        BOOLEAN is_default
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    PAYMENT_METHODS {
        UUID id PK
        UUID tenant_id FK
        VARCHAR provider
        VARCHAR method_type
        VARCHAR provider_token
        VARCHAR last_four
        VARCHAR card_brand
        SMALLINT expiry_month
        SMALLINT expiry_year
        VARCHAR paypal_email
        VARCHAR display_label
        BOOLEAN is_default
        VARCHAR status
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }
```

---

## Relaciones de dependencia

### Jerarquía de cascade

```mermaid
graph TD
    subgraph "Identidad (V3–V9)"
        A["🏢 TENANTS"] -->|CASCADE| B["🔐 CLIENT_APPS"]
        A -->|CASCADE| C["👤 TENANT_USERS"]
        A -->|CASCADE| K["🎫 AUTHORIZATION_CODES"]
        A -->|CASCADE| SES["🖥️ SESSIONS"]
        A -->|CASCADE| RT["🔄 REFRESH_TOKENS"]
        A -->|CASCADE| TBP["🧾 TENANT_BILLING_PROFILES"]
        A -->|CASCADE| PM["💳 PAYMENT_METHODS"]

        B -->|CASCADE| F["↩️ CLIENT_REDIRECT_URIS"]
        B -->|CASCADE| G["✅ CLIENT_ALLOWED_GRANTS"]
        B -->|CASCADE| H["📋 CLIENT_ALLOWED_SCOPES"]
        B -->|CASCADE| E["🎭 APP_ROLES"]
        B -->|CASCADE| D["📊 MEMBERSHIPS"]
        B -->|CASCADE| K
        B -->|CASCADE| SES
        B -->|CASCADE| RT

        C -->|CASCADE| D
        C -->|CASCADE| K
        C -->|CASCADE| SES
        C -->|CASCADE| RT
        C -->|CASCADE| EV["📧 EMAIL_VERIFICATIONS"]

        D -->|CASCADE| J["🔗 MEMBERSHIP_ROLES"]
        E -->|CASCADE| J

        SES -->|CASCADE| RT
    end

    subgraph "Billing (V10–V14)"
        B -->|CASCADE| AP["📦 APP_PLANS"]
        B -->|RESTRICT| AC["📄 APP_CONTRACTS"]
        B -->|RESTRICT| AS["🔁 APP_SUBSCRIPTIONS"]
        B -->|CASCADE| UC["📊 USAGE_COUNTERS"]

        AP -->|RESTRICT| APV["📋 APP_PLAN_VERSIONS"]
        APV -->|CASCADE| APE["⚙️ APP_PLAN_ENTITLEMENTS"]
        APV -->|RESTRICT| AC
        APV -->|RESTRICT| AS

        AC -->|SET NULL| AS
        AC -->|SET NULL| PT["💰 PAYMENT_TRANSACTIONS"]
        AS -->|SET NULL| PT
        AS -->|RESTRICT| INV["🧾 INVOICES"]
    end
```

**Implicaciones:**
- Si se elimina un **tenant**: se eliminan en cascada apps, usuarios, auth codes, sesiones, refresh tokens, perfiles de facturación y métodos de pago.
- Si se elimina una **app**: se eliminan redirect URIs, grants, scopes, roles, memberships, auth codes, sesiones y refresh tokens. Las relaciones de billing usan `ON DELETE RESTRICT` para evitar borrar planes/contratos/suscripciones activos.
- Si se elimina un **usuario**: se eliminan sus memberships, auth codes, sesiones, refresh tokens y verificaciones de email.
- Si se elimina una **sesión**: se eliminan en cascada sus refresh tokens.
- `SIGNING_KEYS` **no tiene FK hacia tenants** — son claves globales del servidor.
- Las tablas de billing (`app_contracts`, `app_subscriptions`, `invoices`) usan `ON DELETE RESTRICT` para proteger el historial financiero.

---

## Guías de consulta común

### 1. Obtener todas las apps activas de un tenant

```sql
SELECT ca.* FROM client_apps ca
WHERE ca.tenant_id = :tenantId
  AND ca.status = 'ACTIVE';
```

### 2. Buscar usuario por email en un tenant

```sql
SELECT tu.* FROM tenant_users tu
WHERE tu.tenant_id = :tenantId
  AND tu.email = :email
  AND tu.status = 'ACTIVE';
```

### 3. Verificar si un usuario tiene membership activa en una app

```sql
SELECT COUNT(1) FROM memberships m
WHERE m.user_id = :userId
  AND m.client_app_id = :clientAppId
  AND m.status = 'ACTIVE';
-- Si count = 1, el usuario tiene acceso
```

### 4. Obtener roles asignados a un usuario en una app

```sql
SELECT ar.code, ar.display_name
FROM app_roles ar
JOIN membership_roles mr ON mr.role_id = ar.id
JOIN memberships m ON m.id = mr.membership_id
WHERE m.user_id = :userId
  AND ar.client_app_id = :clientAppId
  AND m.status = 'ACTIVE';
```

### 5. Listar todos los usuarios de un tenant

```sql
SELECT tu.* FROM tenant_users tu
WHERE tu.tenant_id = :tenantId
ORDER BY tu.created_at DESC;
```

### 6. Canjear authorization code (búsqueda + validación)

```sql
SELECT ac.* FROM authorization_codes ac
WHERE ac.code = :code
  AND ac.client_app_id = :clientAppId
  AND ac.status = 'pending'
  AND ac.expires_at > NOW();
```

### 7. Obtener claves de firma activas (JWKS)

```sql
SELECT id, kid, algorithm, public_material
FROM signing_keys
WHERE status = 'ACTIVE'
ORDER BY activated_at DESC;
```

### 8. Marcar authorization code como usado

```sql
UPDATE authorization_codes
SET status = 'used', used_at = NOW()
WHERE id = :id;
```

---

## Notas sobre enumeraciones

> Los valores de `status` en las tablas multi-tenancy siguen la convención que define cada CHECK constraint en la migración SQL correspondiente.

| Tabla | Campo | Valores permitidos | Convención |
|---|---|---|---|
| `tenants` | `status` | `ACTIVE`, `SUSPENDED`, `PENDING`, `DELETED` | UPPERCASE |
| `client_apps` | `type` | `PUBLIC`, `CONFIDENTIAL` | UPPERCASE |
| `client_apps` | `status` | `ACTIVE`, `SUSPENDED`, `PENDING` | UPPERCASE |
| `tenant_users` | `status` | `ACTIVE`, `SUSPENDED`, `PENDING` | UPPERCASE |
| `memberships` | `status` | `ACTIVE`, `SUSPENDED`, `PENDING` | UPPERCASE |
| `authorization_codes` | `status` | `pending`, `used`, `expired`, `revoked` | **lowercase** |
| `authorization_codes` | `code_challenge_method` | `plain`, `S256` | mixto |
| `signing_keys` | `status` | `ACTIVE`, `RETIRED`, `REVOKED` | UPPERCASE |
| `app_roles` | `code` | regex `^[a-z][a-z0-9_-]*$` | solo minúsculas |
| `sessions` | `status` | `ACTIVE`, `TERMINATED`, `EXPIRED` | UPPERCASE |
| `refresh_tokens` | `status` | `ACTIVE`, `USED`, `EXPIRED`, `REVOKED` | UPPERCASE |
| `app_plans` | `subscriber_type` | `TENANT`, `TENANT_USER` | UPPERCASE |
| `app_plans` | `status` | `ACTIVE`, `INACTIVE` | UPPERCASE |
| `app_plan_versions` | `billing_period` | `MONTHLY`, `YEARLY`, `ONE_TIME` | UPPERCASE |
| `app_plan_versions` | `status` | `ACTIVE`, `INACTIVE`, `DEPRECATED` | UPPERCASE |
| `app_plan_entitlements` | `metric_type` | `QUOTA`, `BOOLEAN`, `RATE` | UPPERCASE |
| `app_plan_entitlements` | `enforcement_mode` | `HARD`, `SOFT` | UPPERCASE |
| `app_plan_entitlements` | `period_type` | `NONE`, `DAY`, `MONTH` | UPPERCASE |
| `app_contracts` | `billing_period` | `MONTHLY`, `YEARLY`, `ONE_TIME` | UPPERCASE |
| `app_contracts` | `status` | `PENDING_EMAIL_VERIFICATION`, `PENDING_PAYMENT`, `READY_TO_ACTIVATE`, `ACTIVE`, `SUPERSEDED`, `FINALIZED`, `EXPIRED`, `CANCELLED`, `FAILED` | UPPERCASE |
| `app_subscriptions` | `status` | `PENDING`, `ACTIVE`, `PAST_DUE`, `SUSPENDED`, `CANCELLED`, `EXPIRED` | UPPERCASE |
| `payment_transactions` | `status` | `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`, `EXPIRED` | UPPERCASE |
| `invoices` | `status` | `DRAFT`, `ISSUED`, `PAID`, `VOID`, `OVERDUE` | UPPERCASE |
| `tenant_billing_profiles` | `billing_type` | `PERSONAL`, `COMPANY` | UPPERCASE |
| `payment_methods` | `provider` | `STRIPE`, `MERCADOPAGO`, `PAYPAL`, `MANUAL`, `MOCK` | UPPERCASE |
| `payment_methods` | `method_type` | `CARD`, `PAYPAL`, `BANK_TRANSFER`, `MOCK` | UPPERCASE |
| `payment_methods` | `status` | `ACTIVE`, `EXPIRED`, `REVOKED` | UPPERCASE |

> ⚠️ Los valores de `authorization_codes.status` son **minúsculas** (distinto al resto). Tener en cuenta en comparaciones de código Java.

---

## Referencia rápida de constraints únicos

| Tabla | Constraint | Descripción |
|---|---|---|
| `tenants` | `UNIQUE(slug)` | Slug global único |
| `client_apps` | `UNIQUE(client_id)` | Client ID único globalmente |
| `client_redirect_uris` | — | Sin constraint; múltiples URIs por app |
| `tenant_users` | `UNIQUE(tenant_id, email)` | Email único por tenant |
| `tenant_users` | `UNIQUE(tenant_id, username)` | Username único por tenant |
| `memberships` | `UNIQUE(user_id, client_app_id)` | No hay memberships duplicadas |
| `app_roles` | `UNIQUE(client_app_id, code)` | Código de rol único por app |
| `membership_roles` | PK `(membership_id, role_id)` | PK compuesta; sin columna `id` propia |
| `authorization_codes` | `UNIQUE(code)` | Authorization code único globalmente |
| `signing_keys` | `UNIQUE(kid)` | Key ID único globalmente |
| `refresh_tokens` | `UNIQUE(token_hash)` | Hash SHA-256 único globalmente (64 hex chars) |
| `app_plans` | `UNIQUE(client_app_id, code)` | Código de plan único por app |
| `app_plan_versions` | `UNIQUE(app_plan_id, version)` | Tag de versión único por plan |
| `app_plan_entitlements` | `UNIQUE(app_plan_version_id, metric_code)` | Métrica única por versión de plan |
| `app_contracts` | UNIQUE parcial `(contractor_id) WHERE status='ACTIVE'` | Solo 1 contrato ACTIVE por contratante |
| `app_subscriptions` | `UNIQUE(client_app_id, contractor_id)` | Una suscripción activa por contratante y app |
| `invoices` | `UNIQUE(invoice_number)` | Número de factura único globalmente |
| `usage_counters` | `UNIQUE(client_app_id, contractor_id, metric_code, period_start, period_end)` | Contador único por contratante/métrica/período |
| `tenant_billing_profiles` | UNIQUE parcial `(tenant_id) WHERE is_default=TRUE` | A lo sumo un perfil default por tenant |
| `payment_methods` | UNIQUE parcial `(tenant_id) WHERE is_default=TRUE` | A lo sumo un método de pago default por tenant |

---

## Tabla: `sessions` — V8

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único de la sesión |
| `tenant_id` | UUID | FK → `tenants.id` | NO | Tenant de la sesión |
| `client_app_id` | UUID | FK → `client_apps.id` | NO | App cliente que inició la sesión |
| `user_id` | UUID | FK → `tenant_users.id` | NO | Usuario propietario de la sesión |
| `status` | VARCHAR(20) | — | NO | Estado: `ACTIVE`, `TERMINATED`, `EXPIRED` |
| `expires_at` | TIMESTAMPTZ | — | NO | Expiración de la sesión (configurado en 30 días) |
| `last_accessed_at` | TIMESTAMPTZ | — | NO | Último acceso (se actualiza en cada rotación de RT) |
| `user_agent` | TEXT | — | SÍ | User-agent del cliente (para auditoría) |
| `ip_address` | VARCHAR(64) | — | SÍ | IP de origen (para auditoría) |
| `created_at` | TIMESTAMPTZ | — | NO | Timestamp de creación (auto, `CURRENT_TIMESTAMP`) |

**Índices:** `idx_sessions_user_tenant(user_id, tenant_id)`, `idx_sessions_status(status)`

**Reglas de negocio:**
- Una sesión ACTIVE puede tener múltiples refresh tokens, pero solo uno es válido (ACTIVE) en un momento dado.
- Al terminar la sesión (`TERMINATED`), todos sus refresh tokens se revocan.
- El `last_accessed_at` se actualiza en cada rotación de refresh token exitosa.

---

## Tabla: `refresh_tokens` — V8

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único del refresh token |
| `token_hash` | VARCHAR(64) | UNIQUE | NO | Hash SHA-256 (hex) del token plano — 64 caracteres |
| `session_id` | UUID | FK → `sessions.id` | NO | Sesión a la que pertenece este token |
| `tenant_id` | UUID | FK → `tenants.id` | NO | Tenant propietario |
| `client_app_id` | UUID | FK → `client_apps.id` | NO | App que recibió el token |
| `user_id` | UUID | FK → `tenant_users.id` | NO | Usuario propietario |
| `requested_scopes` | TEXT | — | NO | Scopes otorgados (espacio separado, e.g. `openid profile`) |
| `status` | VARCHAR(20) | — | NO | Estado: `ACTIVE`, `USED`, `EXPIRED`, `REVOKED` |
| `expires_at` | TIMESTAMPTZ | — | NO | Expiración del token (mismo que la sesión, 30 días) |
| `used_at` | TIMESTAMPTZ | — | SÍ | Cuándo fue canjeado (solo para estado `USED`) |
| `replaced_by_id` | UUID | FK → `refresh_tokens.id` | SÍ | Auto-referencia al nuevo RT que lo reemplazó |
| `created_at` | TIMESTAMPTZ | — | NO | Timestamp de creación (auto, `CURRENT_TIMESTAMP`) |

**Índices:** `idx_refresh_tokens_hash(token_hash)`, `idx_refresh_tokens_session(session_id)`, `idx_refresh_tokens_user_tenant(user_id, tenant_id)`, `idx_refresh_tokens_status(status)`

**Reglas de negocio:**
- El token plano (`raw`) **nunca se almacena** en DB; solo el hash SHA-256 determinista.
- El token plano se entrega al cliente una única vez al emitirlo. Si se pierde, debe re-autenticar.
- Al recibir un token en estado `USED` para rotación, se interpreta como posible ataque de replay.
- RFC 7009: la revocación es idempotente — si el token no existe o ya fue revocado, se responde 200.
- `replaced_by_id` permite trazar la cadena completa de rotación para auditoría.

---

## Tabla: `email_verifications` — V9

Almacena códigos de verificación de email generados durante el auto-registro de usuarios. La fila más reciente por usuario (mayor `created_at`) es el código activo.

| Columna | Tipo | FK | Nullable | Descripción |
|---|---|---|---|---|
| `id` | UUID PK | — | NO | Identificador único (`gen_random_uuid()`) |
| `tenant_user_id` | UUID | → `tenant_users.id` ON DELETE CASCADE | NO | Usuario al que pertenece el código |
| `code` | VARCHAR(10) | — | NO | Código numérico de 6 dígitos (generado con `SecureRandom`) |
| `expires_at` | TIMESTAMPTZ | — | NO | Expiración: 30 minutos desde `created_at` |
| `used_at` | TIMESTAMPTZ | — | SÍ | Timestamp de uso exitoso; `NULL` = no utilizado aún |
| `created_at` | TIMESTAMPTZ | — | NO | Timestamp de creación (`DEFAULT NOW()`) |

**Índices:** `idx_email_verifications_tenant_user_id(tenant_user_id)`, `idx_email_verifications_code(code)`

**Reglas de negocio:**
- Solo el registro con el mayor `created_at` es el código activo para un usuario.
- Un nuevo código solo se puede solicitar cuando el anterior ha expirado (`expires_at < NOW()`).
- Al verificar exitosamente: `used_at` se actualiza en `email_verifications` y `status` pasa a `ACTIVE` en `tenant_users`.
- Un código ya usado (`used_at IS NOT NULL`) no puede reutilizarse aunque no haya expirado.
- La fila se elimina en cascada si el `tenant_user` es eliminado.

---

## Tablas de billing — Modelo v2

> **Modelo v2 (2026-03-30):** rediseño estructural del billing. El suscriptor ya no es un tenant o
> tenant_user creado automáticamente al activar el contrato. En su lugar se introduce la entidad
> `contractor`, que representa la persona/entidad que firma contratos y siempre tiene una cuenta
> (`TenantUser`) en el tenant del proveedor. Los tenants propios son creados **manualmente** por el
> contratante después de contratar, dentro del límite `MAX_TENANTS` del plan activo.
>
> Las tablas de billing son `app-scoped`: pertenecen a una `ClientApp`, no directamente a un tenant.

### Tabla: `contractors` — ⚡ NUEVA (modelo v2)

Entidad central del billing. Representa la persona física o legal que firma contratos con la plataforma.
Tiene una relación **1:1** con un `TenantUser` en el tenant del proveedor.

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único del contratante |
| `tenant_user_id` | UUID | FK → `tenant_users.id` UNIQUE | NO | Usuario del contratante en el tenant del proveedor (1:1) |
| `status` | VARCHAR(20) | — | NO | Estado: `PENDING`, `ACTIVE`, `SUSPENDED` |
| `created_at` | TIMESTAMPTZ | — | NO | Timestamp de creación |
| `updated_at` | TIMESTAMPTZ | — | NO | Timestamp de última actualización |

**Constraints:**
- `UNIQUE(tenant_user_id)` — relación 1:1 estricta
- `CHECK(status IN ('PENDING','ACTIVE','SUSPENDED'))`
- FK: `tenant_user_id` → `tenant_users(id)` ON DELETE RESTRICT

**Reglas de negocio:**
- Se crea durante la verificación de email del primer contrato (al confirmar el email, antes del pago).
- Pasa a `ACTIVE` al activar el primer contrato.
- Solo puede tener **1 contrato en estado `ACTIVE`** en cualquier momento (invariante garantizada por índice único parcial en `app_contracts`).
- Un contratante puede tener muchos tenants propios (dentro del límite `MAX_TENANTS` del plan).
- El `TenantUser` asociado vive **siempre** en el tenant del proveedor (nunca es NULL).

---

### Tabla: `app_plans` — V10

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único |
| `client_app_id` | UUID | FK → `client_apps.id` ON DELETE CASCADE | NO | App propietaria del plan |
| `code` | VARCHAR(50) | UNIQUE (app) | NO | Código único del plan dentro de la app (e.g. `STARTER`, `PRO`) |
| `name` | VARCHAR(100) | — | NO | Nombre legible del plan |
| `description` | TEXT | — | SÍ | Descripción opcional |
| `subscriber_type` | VARCHAR(20) | — | NO | Tipo de suscriptor: `TENANT` (B2B) o `TENANT_USER` (B2C) |
| `status` | VARCHAR(20) | — | NO | Estado: `ACTIVE`, `INACTIVE` |
| `is_public` | BOOLEAN | — | NO | Si aparece en el catálogo público |
| `created_at` | TIMESTAMPTZ | — | NO | Timestamp de creación |
| `updated_at` | TIMESTAMPTZ | — | NO | Timestamp de última actualización |

**Constraints:** `UNIQUE(client_app_id, code)` | `CHECK(subscriber_type IN ('TENANT','TENANT_USER'))` | `CHECK(status IN ('ACTIVE','INACTIVE'))`

**Reglas de negocio:**
- Un plan con `is_public=false` no aparece en `GET /billing/catalog` pero puede asignarse manualmente.
- El `code` es el identificador funcional del plan en APIs y mensajes (no el UUID).
- Una app puede tener planes `TENANT` y `TENANT_USER` simultáneamente.

---

### Tabla: `app_plan_versions` — V10

Snapshots inmutables de precio y período. Las suscripciones apuntan a una versión específica; cambiar precios requiere crear una nueva versión.

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único |
| `app_plan_id` | UUID | FK → `app_plans.id` ON DELETE RESTRICT | NO | Plan padre |
| `version` | VARCHAR(20) | UNIQUE (plan) | NO | Etiqueta de versión (e.g. `1.0`, `2.0-beta`) |
| `currency` | VARCHAR(3) | — | NO | Moneda ISO-4217 (default `MXN`) |
| `billing_period` | VARCHAR(20) | — | NO | Período: `MONTHLY`, `YEARLY`, `ONE_TIME` |
| `base_price` | NUMERIC(12,2) | — | NO | Precio base del período |
| `setup_fee` | NUMERIC(12,2) | — | NO | Tarifa de activación única |
| `trial_days` | INT | — | NO | Días de prueba gratuita (0 = sin prueba) |
| `effective_from` | DATE | — | NO | Fecha de inicio de vigencia |
| `effective_to` | DATE | — | SÍ | Fecha de fin de vigencia (`NULL` = sin vencimiento) |
| `status` | VARCHAR(20) | — | NO | Estado: `ACTIVE`, `INACTIVE`, `DEPRECATED` |
| `created_at` | TIMESTAMPTZ | — | NO | Timestamp de creación |

**Constraints:** `UNIQUE(app_plan_id, version)` | `CHECK(billing_period IN ('MONTHLY','YEARLY','ONE_TIME'))`

**Reglas de negocio:**
- Las versiones con `status=DEPRECATED` no pueden ser seleccionadas en nuevos contratos.
- Las suscripciones activas apuntando a una versión `DEPRECATED` no se afectan.
- `ON DELETE RESTRICT` en la FK impide eliminar un plan que tiene versiones.

---

### Tabla: `app_plan_entitlements` — V10

Límites y feature flags por versión de plan. Definen qué puede hacer el suscriptor dentro de los límites del plan.

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único |
| `app_plan_version_id` | UUID | FK → `app_plan_versions.id` ON DELETE CASCADE | NO | Versión a la que pertenece |
| `metric_code` | VARCHAR(100) | UNIQUE (version) | NO | Código de métrica de negocio (e.g. `MAX_USERS`, `ALLOW_SSO`, `EVALUACIONES_POR_MES`) |
| `metric_type` | VARCHAR(20) | — | NO | Tipo: `QUOTA` (cuota numérica), `BOOLEAN` (habilitado/deshabilitado), `RATE` (rate limit) |
| `limit_value` | BIGINT | — | SÍ | Valor límite (`NULL` = ilimitado para QUOTA/RATE) |
| `period_type` | VARCHAR(20) | — | NO | Período de reset: `NONE`, `DAY`, `MONTH` |
| `enforcement_mode` | VARCHAR(20) | — | NO | Modo: `HARD` (bloquea) o `SOFT` (alerta pero permite) |
| `is_enabled` | BOOLEAN | — | NO | Si el entitlement está activo |

**Constraints:** `UNIQUE(app_plan_version_id, metric_code)` | `CHECK(metric_type IN ('QUOTA','BOOLEAN','RATE'))` | `CHECK(enforcement_mode IN ('HARD','SOFT'))`

---

### Tabla: `app_contracts` — ⚡ MODIFICADA (modelo v2)

Representa el proceso de onboarding/checkout previo a una suscripción. Registra el progreso de
verificación de email, pago y activación. A partir del modelo v2 el contrato siempre pertenece a
un `Contractor`, y los tenants **no se crean automáticamente** al activar.

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único del contrato |
| `client_app_id` | UUID | FK → `client_apps.id` ON DELETE RESTRICT | NO | App del **PROVEEDOR** a la que pertenece el contrato |
| `contractor_id` | UUID | FK → `contractors.id` ON DELETE RESTRICT | SÍ | Contratante asociado. NULL hasta que se verifica el email y se crea el Contractor. |
| `selected_plan_version_id` | UUID | FK → `app_plan_versions.id` ON DELETE RESTRICT | NO | Versión del plan seleccionada |
| `billing_period` | VARCHAR(20) | — | NO | Período elegido: `MONTHLY`, `YEARLY`, `ONE_TIME` |
| `status` | VARCHAR(40) | — | NO | Estado del flujo de contratación (ver máquina de estados) |
| `contractor_email` | VARCHAR(255) | — | NO | Email del contratante (capturado en onboarding, antes de que exista Contractor) |
| `contractor_first_name` | VARCHAR(100) | — | NO | Nombre del contratante |
| `contractor_last_name` | VARCHAR(100) | — | NO | Apellido del contratante |
| `company_name` | VARCHAR(200) | — | SÍ | Nombre de empresa (opcional, para facturación) |
| `company_tax_id` | VARCHAR(100) | — | SÍ | RFC / Tax ID (opcional, para facturación) |
| `company_address` | TEXT | — | SÍ | Dirección fiscal (opcional, para facturación) |
| `verification_code` | VARCHAR(10) | — | SÍ | Código numérico de 6 dígitos enviado a `contractor_email` |
| `verification_code_expires_at` | TIMESTAMPTZ | — | SÍ | Expiración del código (configurable, default 30 min) |
| `email_verified_at` | TIMESTAMPTZ | — | SÍ | Timestamp de verificación de email |
| `payment_verified_at` | TIMESTAMPTZ | — | SÍ | Timestamp de confirmación de pago |
| `expires_at` | TIMESTAMPTZ | — | NO | TTL del contrato (configurable, default 48 h) |
| `created_at` | TIMESTAMPTZ | — | NO | Timestamp de creación |
| `updated_at` | TIMESTAMPTZ | — | NO | Timestamp de última actualización |

**Constraints:**
- `CHECK(billing_period IN ('MONTHLY','YEARLY','ONE_TIME'))`
- Índice único parcial: `UNIQUE(contractor_id) WHERE status = 'ACTIVE'` — solo 1 contrato vigente por contratante
- FK opcional: `contractor_id` → `contractors(id)` ON DELETE RESTRICT (NULL hasta verificación de email)

**Columnas eliminadas respecto al modelo v1:**
- ~~`subscriber_type`~~ — eliminado; el tipo de suscriptor se infiere del plan
- ~~`subscriber_tenant_id`~~ — eliminado; los tenants no se crean automáticamente
- ~~`subscriber_tenant_user_id`~~ — eliminado; el TenantUser se crea vía `contractors`
- ~~`company_slug`~~ — eliminado; no se crea un tenant automáticamente al activar

**Estados válidos:**
```
PENDING_EMAIL_VERIFICATION → PENDING_PAYMENT → READY_TO_ACTIVATE → ACTIVE → SUPERSEDED | FINALIZED
                                                                ↓
                                                   CANCELLED | EXPIRED | FAILED
```

| Estado | Significado |
|---|---|
| `PENDING_EMAIL_VERIFICATION` | Estado inicial; esperando verificación de email |
| `PENDING_PAYMENT` | Email verificado; se creó TenantUser + Contractor (si es nuevo); esperando pago |
| `READY_TO_ACTIVATE` | Pago aprobado; listo para activar |
| `ACTIVE` | Contrato vigente; suscripción activa |
| `SUPERSEDED` | Reemplazado por un nuevo contrato (upgrade/downgrade) |
| `FINALIZED` | Terminado al fin del período sin renovación |
| `EXPIRED` | TTL superado antes de activar |
| `CANCELLED` | Cancelado manualmente |
| `FAILED` | Error irrecuperable en activación |

**Reglas de negocio:**
- `contractor_id` es `NULL` hasta que se verifica el email y se crea/identifica el `Contractor`.
- El código de verificación (`verification_code`) es propio del contrato (no usa `email_verifications`), porque el suscriptor aún puede no existir como `TenantUser` en el momento de crear el contrato.
- Solo puede haber **1 contrato `ACTIVE`** por `contractor_id` (invariante DB + aplicación).
- Al activar un upgrade: el contrato anterior pasa a `SUPERSEDED` en la misma transacción.

---

### Tabla: `app_subscriptions` — ⚡ MODIFICADA (modelo v2)

Relación activa entre un contratante y una versión de plan de una app.

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único |
| `client_app_id` | UUID | FK → `client_apps.id` ON DELETE RESTRICT | NO | App propietaria |
| `app_plan_version_id` | UUID | FK → `app_plan_versions.id` ON DELETE RESTRICT | NO | Versión del plan activo |
| `contract_id` | UUID | FK → `app_contracts.id` ON DELETE SET NULL | SÍ | Contrato origen |
| `contractor_id` | UUID | FK → `contractors.id` ON DELETE RESTRICT | NO | Contratante suscrito (**reemplaza** `subscriber_tenant_id` y `subscriber_tenant_user_id`) |
| `status` | VARCHAR(20) | — | NO | Estado: `PENDING`, `ACTIVE`, `PAST_DUE`, `SUSPENDED`, `CANCELLED`, `EXPIRED` |
| `current_period_start` | TIMESTAMPTZ | — | NO | Inicio del período actual |
| `current_period_end` | TIMESTAMPTZ | — | NO | Fin del período actual |
| `cancel_at_period_end` | BOOLEAN | — | NO | Marcar cancelación al fin del período |
| `cancelled_at` | TIMESTAMPTZ | — | SÍ | Timestamp de cancelación efectiva |
| `next_billing_at` | TIMESTAMPTZ | — | SÍ | Próxima fecha de renovación |
| `auto_renew` | BOOLEAN | — | NO | Si renueva automáticamente |
| `created_at` | TIMESTAMPTZ | — | NO | Timestamp de creación |
| `updated_at` | TIMESTAMPTZ | — | NO | Timestamp de última actualización |

**Constraints:**
- `UNIQUE(client_app_id, contractor_id)` — una suscripción activa por contratante y app
- FK: `contractor_id` → `contractors(id)` ON DELETE RESTRICT

**Columnas eliminadas respecto al modelo v1:**
- ~~`subscriber_tenant_id`~~ — reemplazado por `contractor_id`
- ~~`subscriber_tenant_user_id`~~ — reemplazado por `contractor_id`

---

### Tabla: `payment_transactions` — V12

Una transacción de pago por evento de facturación (activación inicial, renovación, etc.).

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único |
| `contract_id` | UUID | FK → `app_contracts.id` ON DELETE SET NULL | SÍ | Contrato asociado |
| `subscription_id` | UUID | FK → `app_subscriptions.id` ON DELETE SET NULL | SÍ | Suscripción asociada |
| `provider` | VARCHAR(50) | — | NO | Proveedor: `MANUAL`, `MOCK`, `MERCADOPAGO`, `STRIPE`, `OTHER` |
| `provider_reference` | VARCHAR(255) | — | SÍ | ID de referencia del PSP externo |
| `amount` | NUMERIC(12,2) | — | NO | Monto cobrado |
| `currency` | VARCHAR(3) | — | NO | Moneda ISO-4217 |
| `status` | VARCHAR(20) | — | NO | Estado: `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`, `EXPIRED` |
| `paid_at` | TIMESTAMPTZ | — | SÍ | Timestamp de pago exitoso |
| `raw_response` | JSONB | — | SÍ | Respuesta raw del PSP (auditoría) |
| `created_at` | TIMESTAMPTZ | — | NO | Timestamp de creación |

---

### Tabla: `invoices` — V13

Snapshot inmutable de una factura por período de suscripción. Los campos `*_snapshot` no se modifican retroactivamente.

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único |
| `subscription_id` | UUID | FK → `app_subscriptions.id` ON DELETE RESTRICT | NO | Suscripción a la que pertenece |
| `invoice_number` | VARCHAR(50) | UNIQUE | NO | Número de factura (e.g. `INV-A1B2C3D4`) |
| `status` | VARCHAR(20) | — | NO | Estado: `DRAFT`, `ISSUED`, `PAID`, `VOID`, `OVERDUE` |
| `issue_date` | DATE | — | NO | Fecha de emisión |
| `due_date` | DATE | — | NO | Fecha de vencimiento |
| `period_start` | DATE | — | NO | Inicio del período facturado |
| `period_end` | DATE | — | NO | Fin del período facturado |
| `currency` | VARCHAR(3) | — | NO | Moneda |
| `subtotal` | NUMERIC(12,2) | — | NO | Subtotal sin impuestos |
| `tax_amount` | NUMERIC(12,2) | — | NO | Monto de impuestos |
| `total` | NUMERIC(12,2) | — | NO | Total a pagar |
| `billing_name_snapshot` | VARCHAR(300) | — | SÍ | Nombre del titular al momento de emisión |
| `billing_tax_id_snapshot` | VARCHAR(100) | — | SÍ | RFC/Tax ID al momento de emisión |
| `billing_address_snapshot` | TEXT | — | SÍ | Dirección al momento de emisión |
| `plan_name_snapshot` | VARCHAR(100) | — | SÍ | Nombre del plan al momento de emisión |
| `plan_version_snapshot` | VARCHAR(20) | — | SÍ | Versión del plan al momento de emisión |
| `pdf_url` | TEXT | — | SÍ | URL del PDF de la factura |
| `created_at` | TIMESTAMPTZ | — | NO | Timestamp de creación |

---

### Tabla: `usage_counters` — ⚡ MODIFICADA (modelo v2)

Contadores atómicos de uso por contratante, métrica y período.

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único |
| `client_app_id` | UUID | FK → `client_apps.id` ON DELETE CASCADE | NO | App propietaria |
| `contractor_id` | UUID | FK → `contractors.id` ON DELETE CASCADE | NO | Contratante al que pertenece el contador (**reemplaza** los campos `subscriber_*`) |
| `metric_code` | VARCHAR(100) | — | NO | Código de métrica (e.g. `MAX_TENANTS`, `MAX_USERS`, `EVALUACIONES_POR_MES`) |
| `period_start` | TIMESTAMPTZ | — | NO | Inicio del período de la cuota |
| `period_end` | TIMESTAMPTZ | — | NO | Fin del período de la cuota |
| `used_value` | BIGINT | — | NO | Valor acumulado en el período (default 0) |
| `updated_at` | TIMESTAMPTZ | — | NO | Timestamp de última actualización |

**Constraints:**
- `UNIQUE(client_app_id, contractor_id, metric_code, period_start, period_end)`
- FK: `contractor_id` → `contractors(id)` ON DELETE CASCADE

**Columnas eliminadas respecto al modelo v1:**
- ~~`subscriber_tenant_id`~~ — reemplazado por `contractor_id`
- ~~`subscriber_tenant_user_id`~~ — reemplazado por `contractor_id`

**Reglas de negocio:**
- Los incrementos se realizan con `UPDATE ... SET used_value = used_value + delta` para atomicidad.
- La métrica `MAX_TENANTS` se evalúa consultando directamente `COUNT(*) FROM tenants WHERE contractor_id = :id AND status != 'DELETED'` (no usa contadores periódicos).

---

### Tabla: `tenant_billing_profiles` — V14

Datos fiscales y de facturación por Tenant. Se usan para generar facturas y CFDI. Un tenant puede tener múltiples perfiles; solo uno puede ser el `is_default`.

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único |
| `tenant_id` | UUID | FK → `tenants.id` ON DELETE CASCADE | NO | Tenant propietario |
| `billing_type` | VARCHAR(20) | — | NO | Tipo: `PERSONAL` (persona física) o `COMPANY` (persona moral) |
| `billing_name` | VARCHAR(300) | — | NO | Nombre completo (persona) o razón social (empresa) |
| `tax_id` | VARCHAR(100) | — | SÍ | RFC / NIT / RUT / VAT / EIN |
| `tax_regime` | VARCHAR(100) | — | SÍ | Código de régimen SAT (ej. `601`, `612`) |
| `address_line1` | VARCHAR(300) | — | SÍ | Primera línea del domicilio fiscal |
| `address_line2` | VARCHAR(300) | — | SÍ | Segunda línea del domicilio fiscal |
| `city` | VARCHAR(100) | — | SÍ | Ciudad |
| `state` | VARCHAR(100) | — | SÍ | Estado / provincia |
| `country` | VARCHAR(2) | — | NO | Código ISO 3166-1 alpha-2 (default `MX`) |
| `postal_code` | VARCHAR(20) | — | SÍ | Código postal |
| `contact_email` | VARCHAR(255) | — | SÍ | Email de contacto fiscal |
| `contact_phone` | VARCHAR(50) | — | SÍ | Teléfono de contacto |
| `is_default` | BOOLEAN | — | NO | Si es el perfil de facturación principal del tenant |
| `created_at` | TIMESTAMPTZ | — | NO | Timestamp de creación |
| `updated_at` | TIMESTAMPTZ | — | NO | Timestamp de última actualización |

**Constraints:**
- `CHECK(billing_type IN ('PERSONAL','COMPANY'))`
- UNIQUE parcial: `UNIQUE(tenant_id) WHERE is_default = TRUE` — a lo sumo un perfil default por tenant
- FK: `tenant_id` → `tenants(id)` ON DELETE CASCADE

**Reglas de negocio:**
- Solo un perfil puede ser `is_default = TRUE` por tenant (índice único parcial).
- Los datos se copian como snapshot en `invoices` al emitir: `billing_name_snapshot`, `billing_tax_id_snapshot`, `billing_address_snapshot`.
- `tax_regime` es opcional pero requerido para CFDI en México.

---

### Tabla: `payment_methods` — V14

Tokens de métodos de pago por Tenant. **Nunca almacena datos crudos de tarjeta (PAN, CVV)** — solo tokens PSP y metadatos de display.

| Campo | Tipo | Clave | Nulable | Descripción |
|---|---|---|---|---|
| `id` | UUID | PK | NO | Identificador único |
| `tenant_id` | UUID | FK → `tenants.id` ON DELETE CASCADE | NO | Tenant propietario |
| `provider` | VARCHAR(50) | — | NO | PSP: `STRIPE`, `MERCADOPAGO`, `PAYPAL`, `MANUAL`, `MOCK` |
| `method_type` | VARCHAR(20) | — | NO | Tipo: `CARD`, `PAYPAL`, `BANK_TRANSFER`, `MOCK` |
| `provider_token` | VARCHAR(500) | — | SÍ | Token opaco del PSP (cifrar en reposo en prod). NULL para `MOCK`/`MANUAL` |
| `last_four` | VARCHAR(4) | — | SÍ | Últimos 4 dígitos (solo display; nunca el PAN completo) |
| `card_brand` | VARCHAR(50) | — | SÍ | Marca: `VISA`, `MASTERCARD`, `AMEX`, `CARNET`, etc. |
| `expiry_month` | SMALLINT | — | SÍ | Mes de expiración (1–12) |
| `expiry_year` | SMALLINT | — | SÍ | Año de expiración (≥ 2020) |
| `paypal_email` | VARCHAR(255) | — | SÍ | Email de la cuenta PayPal (solo para `method_type = PAYPAL`) |
| `display_label` | VARCHAR(100) | — | SÍ | Etiqueta legible para UI (ej. `VISA **** 4242`) |
| `is_default` | BOOLEAN | — | NO | Si es el método de pago principal del tenant |
| `status` | VARCHAR(20) | — | NO | Estado: `ACTIVE`, `EXPIRED`, `REVOKED` |
| `created_at` | TIMESTAMPTZ | — | NO | Timestamp de creación |
| `updated_at` | TIMESTAMPTZ | — | NO | Timestamp de última actualización |

**Constraints:**
- `CHECK(provider IN ('STRIPE','MERCADOPAGO','PAYPAL','MANUAL','MOCK'))`
- `CHECK(method_type IN ('CARD','PAYPAL','BANK_TRANSFER','MOCK'))`
- `CHECK(status IN ('ACTIVE','EXPIRED','REVOKED'))`
- `CHECK(expiry_month BETWEEN 1 AND 12)`, `CHECK(expiry_year >= 2020)`
- UNIQUE parcial: `UNIQUE(tenant_id) WHERE is_default = TRUE` — a lo sumo un método default por tenant
- FK: `tenant_id` → `tenants(id)` ON DELETE CASCADE

**Reglas de negocio:**
- `provider_token` debe cifrarse en reposo en producción.
- Solo un método puede ser `is_default = TRUE` por tenant.
- Los métodos `MOCK` son solo para dev/testing; deben bloquearse en producción vía configuración.

---

---

## Tabla: `user_notification_preferences` (V21)

**Contexto:** Preferencias de notificación self-service. Un registro por par `(user_id, tenant_id)`.  
Si no hay registro para un usuario, el backend retorna valores por defecto (`security_alerts_email=true`, `security_alerts_in_app=true`, `billing_alerts_email=true`, demás `false`).

| Columna | Tipo | Nullable | Default | Descripción |
|---|---|---|---|---|
| `id` | UUID PK | NO | `gen_random_uuid()` | Identificador único |
| `user_id` | UUID FK → `tenant_users(id)` | NO | — | Usuario propietario (ON DELETE CASCADE) |
| `tenant_id` | UUID FK → `tenants(id)` | NO | — | Tenant de contexto (ON DELETE CASCADE) |
| `security_alerts_email` | BOOLEAN | NO | `TRUE` | Alertas de seguridad por email |
| `security_alerts_in_app` | BOOLEAN | NO | `TRUE` | Alertas de seguridad in-app |
| `billing_alerts_email` | BOOLEAN | NO | `TRUE` | Alertas de billing por email |
| `product_updates_email` | BOOLEAN | NO | `FALSE` | Actualizaciones de producto por email |
| `weekly_digest` | BOOLEAN | NO | `FALSE` | Resumen semanal por email |
| `created_at` | TIMESTAMPTZ | NO | `NOW()` | Timestamp de creación |
| `updated_at` | TIMESTAMPTZ | NO | `NOW()` | Timestamp de última modificación |

**Constraints:**
- UNIQUE `(user_id, tenant_id)` — un registro por par usuario+tenant
- Índice `idx_notif_prefs_user_tenant` sobre `(user_id, tenant_id)`

**Entidad JPA:** `UserNotificationPreferencesEntity` (`keygo-supabase/user/entity/`)  
**Puerto:** `NotificationPreferencesRepositoryPort` (`keygo-app/user/port/`)  
**Adaptador:** `NotificationPreferencesRepositoryAdapter` (`keygo-supabase/user/adapter/`)

---

**Última actualización:** 2026-04-02 | **Responsable:** AI Agent | **Sincronizado con:** Migraciones V1–V21

