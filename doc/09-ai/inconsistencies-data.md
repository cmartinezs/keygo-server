# INCONSISTENCIAS — Datos / Data Model

> Sub-documento de [`inconsistencias.md`](inconsistencias.md).
>
> Registra **inconsistencias encontradas entre la documentación del modelo de datos y el schema
> real de la base de datos** (migraciones Flyway). Cada entrada incluye lo que estaba documentado,
> lo que es real y cómo se corrigió.
>
> Fecha de detección: **2026-03-22** | Revisión: migraciones V1–V9 vs documentos `DATA_MODEL.md`,
> `ENTITY_RELATIONSHIPS.md`, `DATA_DICTIONARY.md`, `AUTH_FLOW.md`
> Segunda revisión: **2026-03-22** | Re-auditoría de inconsistencias "resueltas" — corrección de tablas en singular vía `V10__rename_membership_tables_to_plural.sql`

---

## Estado: ✅ Corregido completamente (2026-03-22)

Todas las inconsistencias han sido corregidas:
- **Inconsistencias #2–12**: corregidas en documentos de referencia (la implementación era correcta).
- **Inconsistencia #1** (tablas en singular): corregida **tanto en DB** (migración `V10__rename_membership_tables_to_plural.sql`) **como en documentos** y entidades JPA.

---

## Inconsistencias encontradas

### 1. Tablas en singular — `membership`, `membership_role`, `app_role`

| Campo | Documentado original | Real (migración V7) | Corrección (V10) |
|---|---|---|---|
| Nombre tabla | `memberships` | `membership` (singular) | ✅ `memberships` (V10) |
| Nombre tabla | `membership_roles` | `membership_role` (singular) | ✅ `membership_roles` (V10) |
| Nombre tabla | `app_roles` | `app_role` (singular) | ✅ `app_roles` (V10) |
| Columna `tenant_id` en `membership` | Presente | **NO existe** | ⚠️ No se agrega — es redundante (user_id implica tenant vía tenant_users) |
| Status values de `membership` | `ACTIVE, INVITED, SUSPENDED, REVOKED` | `ACTIVE, SUSPENDED, PENDING` | ⚠️ Se mantiene implementación — más consistente con el resto del sistema |
| Constraint UNIQUE en `membership` | `(tenant_id, user_id, client_app_id)` | `(user_id, client_app_id)` | ⚠️ Se mantiene implementación — correcta sin `tenant_id` |

**Impacto original:** Inconsistencia de nomenclatura; violaba la convención PostgreSQL de tablas en plural.
**Corrección:** `V10__rename_membership_tables_to_plural.sql` renombra las 3 tablas + sus índices y constraints. Entidades JPA (`AppRoleEntity`, `MembershipEntity`) actualizadas. `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md` actualizados.
**Criterio de decisión:** La documentación manda en nombres de tablas (convención plural estándar). La implementación era correcta en los demás aspectos (columnas, constraints, status values).

---

### 2. Tabla `membership_roles` — PK y nombre de FK incorrectos

| Campo | Documentado | Real (migración V7) |
|---|---|---|
| Clave primaria | UUID `id` (columna independiente) | **PK compuesta** `(membership_id, role_id)` — sin columna `id` |
| FK al rol | `app_role_id` | `role_id` |
| FK a membership | `membership_id` → `memberships(id)` | Correcto tras renombrar en V10 |

**Impacto:** JPA entity con mapeo incorrecto; queries con `app_role_id` fallarían en runtime.
**Corrección:** `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md`. PK compuesta mantenida (correcta para join tables). FK renombrada automáticamente al renombrar tablas en V10.

---

### 3. Tabla `app_roles` — columnas inexistentes documentadas

| Campo | Documentado | Real (migración V7) | Decisión |
|---|---|---|---|
| Columna `tenant_id` | Presente | **NO existe** | ⚠️ No se agrega — redundante (client_app_id implica el tenant) |
| Columna `status` | Presente (`ACTIVE`, `DISABLED`) | **NO existe** | ⚠️ No se agrega ahora — mejora futura (ver ROADMAP T-NNN) |
| Columna `name` | Presente | Columna es `display_name` (nullable) | ✅ `display_name` es más descriptivo; implementación correcta |
| Code constraint | No documentado | `code ~ '^[a-z][a-z0-9_-]*$'` | ✅ Correcto; docs actualizados |

**Impacto:** Código que filtre por `app_role.status` o use `app_role.name` fallaría.
**Corrección:** `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md`. Tabla renombrada a `app_roles` vía V10.

---

### 4. Tabla `authorization_codes` — múltiples errores

| Campo | Documentado | Real (migración V8) |
|---|---|---|
| Columna de scopes | `scope_set TEXT` | `requested_scopes TEXT` |
| Columna `code` tamaño | `VARCHAR(500)` | `VARCHAR(256)` |
| Columna `code_challenge` tamaño | `VARCHAR(500)` | `VARCHAR(256)` |
| Columna `redirect_uri` tamaño | `VARCHAR(2000)` | `VARCHAR(2048)` |
| Status values | `ACTIVE, CONSUMED, EXPIRED, REVOKED` (UPPERCASE) | `pending, used, expired, revoked` (**lowercase**) |
| `code_challenge_method` values | `S256, PLAIN` (UPPERCASE) | `plain, S256` (mixto) |
| Columna `used_at` | No documentada | Presente — `TIMESTAMPTZ` nullable |
| Columna `code_challenge` | Nullable (SÍ) | `NOT NULL` |

**Impacto crítico:** Status en minúsculas es una diferencia que afecta a comparaciones Java/SQL.
**Corrección:** `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md`, `AUTH_FLOW.md`.

---

### 5. Tabla `signing_keys` — columna `private_material` mal nombrada

| Campo | Documentado | Real (migración V9) |
|---|---|---|
| Clave privada | `private_material_ref VARCHAR(500)` | `private_material TEXT` (nullable) |
| Columna `created_at` | No documentada | Presente — `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP` |

**Nota:** La documentación decía que la clave privada era solo una "referencia" a KMS. En la implementación real, el PEM completo se almacena en DB (cifrado en reposo recomendado para producción).
**Corrección:** `DATA_MODEL.md`.

---

### 6. Tabla `tenants` — columna `owner_email` no documentada

| Campo | Documentado | Real (migración V4) |
|---|---|---|
| Columna `owner_email` | No documentada | `VARCHAR(255) NOT NULL` |
| Status values | `ACTIVE, SUSPENDED, ARCHIVED` | `ACTIVE, SUSPENDED, PENDING` |

**Corrección:** `DATA_MODEL.md`.

---

### 7. Tabla `client_apps` — nombres de columnas incorrectos

| Campo | Documentado | Real (migración V5) |
|---|---|---|
| Nombre app | `display_name VARCHAR(255)` | `name VARCHAR(255)` |
| Secret del cliente | `client_secret VARCHAR(500)` | `hashed_secret VARCHAR(255)` |
| Tipo de cliente | `client_type ENUM` | `type VARCHAR(20)` |
| Status values | `ACTIVE, DISABLED, ROTATION_REQUIRED` | `ACTIVE, SUSPENDED, PENDING` |

**Corrección:** `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md`.

---

### 8. Tabla `client_redirect_uris` — nombre de columna incorrecto

| Campo | Documentado | Real (migración V5) |
|---|---|---|
| URI de redirección | `redirect_uri VARCHAR(2000)` | `uri VARCHAR(2048)` |

**Corrección:** `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md`.

---

### 9. Tabla `tenant_users` — campos incorrectos

| Campo | Documentado | Real (migración V6) |
|---|---|---|
| Nombre legible | `display_name VARCHAR(255)` | `first_name VARCHAR(100)` + `last_name VARCHAR(100)` |
| Password hash | `password_hash VARCHAR(500)` | `password_hash VARCHAR(255)` |
| Status values | `ACTIVE, INVITED, LOCKED, SUSPENDED` | `ACTIVE, SUSPENDED, PENDING` |
| Username nulable | SÍ | `NOT NULL` |

**Corrección:** `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md`.

---

### 10. Tablas legado (V1/V3) no documentadas

Las siguientes tablas existen en la DB desde las migraciones iniciales pero **no estaban documentadas** en `DATA_MODEL.md`:

| Tabla | Migración | Nota |
|---|---|---|
| `users` | V1 | Usuarios globales (sin tenant) — legado |
| `roles` | V1 | Roles globales — legado |
| `user_roles` | V1 | Join table usuarios↔roles — legado |
| `permissions` | V1 | Permisos globales — legado |
| `role_permissions` | V1 | Join table roles↔permissions — legado |
| `sessions` | V1 | Sesiones de usuario global — legado |
| `audit_logs` | V1 | Log de auditoría global — legado |
| `oauth_providers` | V3 | Proveedores OAuth externos — legado |
| `oauth_tokens` | V3 | Tokens de proveedores externos — legado |

**Corrección:** `DATA_MODEL.md` — sección "Tablas legado (V1/V3)" agregada.

---

### 11. Tablas planificadas documentadas como implementadas

Las siguientes tablas estaban documentadas en `DATA_MODEL.md` como existentes, pero **no tienen migración**:

| Tabla | Estado real | Fase planificada |
|---|---|---|
| `refresh_tokens` | ❌ No existe — planificada | Fase 7 (`V11__add_refresh_tokens.sql`) |
| `sessions` (multi-tenant) | ❌ No existe — planificada | Fase 8 (`V11__add_tenant_sessions.sql`) |

**Corrección:** `DATA_MODEL.md` — sección "Tablas planificadas (fases futuras)" diferenciada de las activas.

---

### 12. AUTH_FLOW.md — estado de Fase 6 incorrecto

| Aspecto | Documentado | Real |
|---|---|---|
| Estado del documento | "Fase 5 implementada (Fase 6 pendiente)" | **Fases 5 y 6 completadas** |
| Respuesta Paso 3 | `authorization_code_id` (solo ID de auditoría) | `access_token` + `id_token` + `token_type` + `expires_in` + `scope` |
| Status en diagramas | `PENDING`/`CONSUMED` (UPPERCASE) | `pending`/`used` (lowercase) |
| Tabla comparativa | Fase 6 "próxima" | Fase 6 ✅ completa |

**Corrección:** `AUTH_FLOW.md` — completamente actualizado para reflejar Fases 5 y 6 implementadas.

---

## Causa raíz

La documentación se generó **en paralelo a la implementación**, sin releer las migraciones SQL reales antes de escribir el diccionario. Se asumieron nombres de columnas basados en los nombres de los campos Java de las entidades JPA y de los modelos de dominio, que difieren de los nombres SQL reales en algunos casos. Las tablas de V7 se crearon en singular, violando la convención estándar PostgreSQL de nombres en plural.

## Criterio de corrección (re-auditoría 2026-03-22)

> **La documentación manda** para convenciones de nomenclatura (singular vs plural).
> **La implementación manda** cuando la razón técnica es clara (normalización, columnas redundantes, estándares RFC).
> Cuando ambos tienen razón parcial → se aplica el criterio de menor impacto y mayor consistencia con el sistema.

## Regla preventiva establecida

> **Al crear cualquier migración Flyway nueva (`V{n}__*.sql`)**, el agente **debe** actualizar
> `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md` y `DATA_DICTIONARY.md` **antes de cerrar la tarea**.
> Esta regla se agregó a todos los archivos de instrucciones de AI.

---

**Detección:** 2026-03-22 | **Corrección docs:** 2026-03-22 | **Corrección DB (V10):** 2026-03-22 | **Revisado por:** AI Agent

