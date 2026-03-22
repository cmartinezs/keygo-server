# 📊 Data Model Documentation — KeyGo Server

> Documentación centralizada del **modelo de datos**, **diccionario**, **relaciones de entidades** y **flujos de negocio**.

---

## 📑 Documentos principales

### 1. **DATA_MODEL.md** — Diccionario y Entidad-Relación (E/R)

**Ubicación:** `docs/keygo-server/DATA_MODEL.md`

Contiene:
- ✅ **Diccionario completo** — descripción de cada tabla, campos, tipos, constraints
- 📐 **Diagrama E/R (Mermaid)** — visualización de todas las entidades y sus relaciones
- 🔗 **Relaciones de cascade** — jerarquía de eliminaciones en cascada
- 🎯 **Guías de consulta comunes** — SQL de referencia rápida
- 📋 **Enumeraciones (ENUM)** — valores posibles por campo
- 🔑 **Constraints únicos** — tabla de referencia de PRIMARY KEYs, UNIQUE constraints

**Cuándo consultarlo:**
- Necesitas entender la estructura de una tabla específica
- Quieres validar constraints antes de crear migraciones
- Necesitas una consulta SQL de referencia
- Consultas sobre tipos de datos y valores permitidos

---

### 2. **ENTITY_RELATIONSHIPS.md** — Flujos y contextos de negocio

**Ubicación:** `docs/keygo-server/ENTITY_RELATIONSHIPS.md`

Contiene:
- 🏗️ **Relaciones por contexto** — diagramas por bounded context (Tenant Mgmt, Client Apps, User Identity, etc.)
- 🔐 **Flujo OAuth2 Authorization Code** — secuencia completa con validaciones
- 🔄 **Flujos de token** — refresh token, revocation, rotación
- 📊 **Ciclo de vida de memberships** — state machine + transiciones permitidas
- ✅ **Matriz de decisión de acceso** — lógica de autorización paso a paso
- 🚀 **Índices recomendados** — SQL para optimización

**Cuándo consultarlo:**
- Necesitas entender cómo fluyen los datos en un caso de uso
- Trabajas en autenticación/autorización
- Necesitas la lógica de validación de memberships
- Quieres optimizar consultas (índices)
- Necesitas comprender transiciones de estado

---

## 🎯 Mapa rápido de acceso por rol

### Para desarrolladores backend

```
¿Necesito implementar...?

├─ Un nuevo endpoint REST
│  ├─ Leer: DATA_MODEL.md → Diccionario + Guías de consulta
│  ├─ Leer: ENTITY_RELATIONSHIPS.md → Matriz de decisión de acceso
│  └─ Verificar: AGENTS.md § "JPA entities" → repositorios existentes

├─ Autenticación OAuth2
│  ├─ Leer: ENTITY_RELATIONSHIPS.md § "Flujo Authorization Code"
│  ├─ Leer: DATA_MODEL.md → tablas authorization_codes, refresh_tokens, signing_keys
│  └─ Referenciar: docs/arch/keygo_server_domain_model.md

├─ Gestión de memberships/roles
│  ├─ Leer: ENTITY_RELATIONSHIPS.md § "Ciclo de vida de memberships"
│  ├─ Leer: DATA_MODEL.md → tablas memberships, app_roles, membership_roles
│  └─ Referenciar: AGENTS.md § "Fase 4 completada"

└─ Una query SQL o índice
   ├─ Consultar: DATA_MODEL.md § "Guías de consulta común"
   ├─ Consultar: DATA_MODEL.md § "Constraints únicos"
   └─ Verificar: ENTITY_RELATIONSHIPS.md § "Índices recomendados"
```

### Para arquitectos / diseñadores

```
¿Necesito entender...?

├─ La estructura general del dominio
│  └─ Leer: DATA_MODEL.md § "Modelo E/R" + "Relaciones de dependencia"

├─ Cómo está organizado el negocio en contextos
│  └─ Leer: ENTITY_RELATIONSHIPS.md § "Relaciones por contexto"

├─ El flujo de autorización completo
│  ├─ Leer: ENTITY_RELATIONSHIPS.md § "Flujo Authorization Code"
│  ├─ Leer: ENTITY_RELATIONSHIPS.md § "Matriz de decisión"
│  └─ Referenciar: docs/arch/keygo_server_domain_model.md

├─ Decisiones de normalización y cascade
│  ├─ Leer: DATA_MODEL.md § "Relaciones de dependencia"
│  └─ Revisar: migrations en keygo-supabase/db/migration/

└─ Performance y escalabilidad
   ├─ Leer: ENTITY_RELATIONSHIPS.md § "Índices recomendados"
   ├─ Revisar: DATA_MODEL.md § "Constraints únicos"
   └─ Considerar: docs/keygo-supabase/INTEGRATION.md
```

### Para QA / Testing

```
¿Necesito validar...?

├─ Estados y transiciones de entidades
│  └─ Leer: ENTITY_RELATIONSHIPS.md § "Ciclo de vida de memberships"

├─ Flujos de autenticación
│  └─ Leer: ENTITY_RELATIONSHIPS.md § "Flujo Authorization Code" + "Token Refresh"

├─ Reglas de negocio (invariantes)
│  ├─ Leer: DATA_MODEL.md § "Tabla XXX" → "Reglas de negocio"
│  └─ Leer: ENTITY_RELATIONSHIPS.md § "Matriz de decisión"

└─ Datos de prueba y estados válidos
   ├─ Consultar: DATA_MODEL.md § "Enumeraciones (ENUM)"
   ├─ Consultar: DATA_MODEL.md § "Constraints únicos"
   └─ Referenciar: AGENTS.md § "Testing conventions"
```

---

## 🔗 Relación con otros documentos

| Documento | Relación | Cuándo usar en paralelo |
|---|---|---|
| `ARCHITECTURE.md` | Arquitectura módulos | Diseñar capas de la feature |
| `keygo_server_domain_model.md` | Modelo conceptual | Entender bounded contexts |
| `keygo_server_implementation_plan.md` | Roadmap técnico | Planificar fases de implementación |
| `AGENTS.md` | Quick-start | Referencia rápida de repos/entidades |
| `docs/keygo-api/RESPONSE_CODES_GUIDE.md` | Response codes | Mapear errores a responseCodes |
| `postman/KeyGo-Server.postman_collection.json` | Test requests | Validar con ejemplos HTTP |

---

## 📐 Diagramas a nivel de vista

### Vista 1: Estructura general (nivel 30k pies)
```
Locación: DATA_MODEL.md § "Modelo E/R"
Contenido: Todas las entidades + relaciones principales
Herramienta: Mermaid ER diagram
Audiencia: Todos
```

### Vista 2: Contextos de negocio (nivel 10k pies)
```
Locación: ENTITY_RELATIONSHIPS.md § "Relaciones por contexto"
Contenido: Tenant Mgmt, Client Apps, User Identity, Membership & Auth, Token Lifecycle
Herramienta: Mermaid class diagrams
Audiencia: Desarrolladores, Arquitectos
```

### Vista 3: Flujos de datos (nivel 3k pies — de-tail)
```
Locación: ENTITY_RELATIONSHIPS.md § "Flujo Authorization Code", "Token Refresh", "Validación de acceso"
Contenido: Secuencias paso-a-paso con decisiones
Herramienta: Mermaid sequence diagrams, state machines, flowcharts
Audiencia: Desarrolladores
```

### Vista 4: State machines (nivel entidad)
```
Locación: ENTITY_RELATIONSHIPS.md § "Ciclo de vida de memberships", "Token Revocation"
Contenido: Transiciones permitidas, invariantes
Herramienta: Mermaid state diagrams
Audiencia: Developers, QA
```

---

## 🎓 Ejemplos de uso

### Caso 1: Implementar endpoint que lista roles de un usuario en una app

```
Plan:
1. Verificar tabla app_roles → DATA_MODEL.md § "Tabla: app_roles"
2. Verificar relación membership_roles → DATA_MODEL.md § "Tabla: membership_roles"
   ⚠️ PK compuesta (membership_id, role_id) — sin columna id propia; FK al rol es role_id
3. Verificar query SQL → DATA_MODEL.md § "Guía 4: Obtener roles asignados"
4. Entender validaciones → ENTITY_RELATIONSHIPS.md § "Matriz de decisión"
5. Escribir código + tests

Ficheros a consultar:
├─ DATA_MODEL.md (SQL reference)
├─ ENTITY_RELATIONSHIPS.md (access matrix, state machine membership)
├─ AGENTS.md (repositories: AppRoleJpaRepository, MembershipJpaRepository)
└─ keygo-api § TenantAppRoleController.java (ejemplo de controller)
```

### Caso 2: Debuggear bug en token refresh

```
Plan:
1. Entender flujo → ENTITY_RELATIONSHIPS.md § "Refresh Token Flow"
2. Revisar tabla refresh_tokens → DATA_MODEL.md § "Tabla: refresh_tokens"
3. Revisar estados → DATA_MODEL.md § "Enumeraciones" → refresh_token_status
4. Entender validaciones → ENTITY_RELATIONSHIPS.md § "Flujos de token (refresh, revoke)"
5. Revisionar query de repo → DATA_MODEL.md § "Guía 7"

Ficheros a consultar:
├─ ENTITY_RELATIONSHIPS.md (sequence diagram)
├─ DATA_MODEL.md (enum values, table schema)
├─ keygo-supabase § RefreshTokenJpaRepository.java (query methods)
└─ Logs + tests de error
```

### Caso 3: Diseñar una migración Flyway para nueva tabla

```
Plan:
1. Revisar constraints en entidades ya existentes → DATA_MODEL.md § "Constraints únicos"
2. Revisar relaciones → DATA_MODEL.md § "Relaciones de dependencia"
3. Entender cascade rules → DATA_MODEL.md § "Jerarquía de cascade"
4. Escribir migración V<n>__...sql
5. Ejecutar y verificar con tests

Ficheros a consultar:
├─ DATA_MODEL.md (field types, constraints, indexes)
├─ keygo-supabase/src/main/resources/db/migration/ (migración anterior)
├─ AGENTS.md § "Flyway migrations" (convención V<n>)
└─ docs/keygo-supabase/MIGRATIONS.md (guía de migraciones)
```

---

## 📌 Convenciones de nomenclatura

| Elemento | Patrón | Ejemplo |
|---|---|---|
| Tabla | `snake_case` | `client_apps`, `tenant_users` |
| Columna | `snake_case` | `client_id`, `created_at` |
| PK | `id` (UUID) | `id` |
| FK | `{entity}_id` | `tenant_id`, `user_id` |
| Índice | `idx_{table}_{columns}` | `idx_membership_user_app` |
| ENUM Java | `PascalCase` | `ClientType`, `MembershipStatus` |
| ENUM SQL | Mayúsculas (mayoría) / minúsculas (`authorization_codes.status`) | `ACTIVE` / `pending` |
| Entidad JPA | `PascalCase + Entity` | `TenantEntity`, `ClientAppEntity` |
| Índice | `idx_{table}_{columns}` | `idx_memberships_user_id` |

> ⚠️ **Excepción importante:** `authorization_codes.status` usa valores en **minúsculas** (`pending`, `used`, `expired`, `revoked`), a diferencia del resto de tablas que usan UPPERCASE. Tener en cuenta en comparaciones Java y SQL.


---

## ✅ Checklist de validación antes de hacer cambios

- [ ] He leído `DATA_MODEL.md` para entender la tabla afectada
- [ ] He leído `ENTITY_RELATIONSHIPS.md` para entender el flujo de negocio
- [ ] He verificado si hay cambios de estado en `ENTITY_RELATIONSHIPS.md`
- [ ] He consultado las "Guías de consulta" relevantes en `DATA_MODEL.md`
- [ ] He validado constraints únicos contra lo que dice `DATA_MODEL.md`
- [ ] He considerado cascade rules si toco FKs
- [ ] He verificado enumeraciones en `DATA_MODEL.md` si uso ENUMs (¡atención a los lowercase de `authorization_codes.status`!)
- [ ] He revisado migraciones Flyway existentes en `keygo-supabase/src/main/resources/db/migration/`
- [ ] Mi cambio está alineado con el bounded context correspondiente en `ENTITY_RELATIONSHIPS.md`
- [ ] **Si agrego una nueva tabla Flyway**: he actualizado `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md` y `DATA_DICTIONARY.md`

---

## 🚀 Siguientes pasos

**Fases 5 y 6 ✅ — OAuth2/OIDC Authorization Flow completadas:**
- ✅ `AuthorizationCode` lifecycle (V8)
- ✅ `SigningKey` + JWT RS256 firma (V9)
- ✅ JWKS endpoint (`/.well-known/jwks.json`)
- ✅ OIDC Discovery (`/.well-known/openid-configuration`)
- ✅ Tablas `memberships`, `app_roles`, `membership_roles` renombradas a plural (V10)

**Fase 7 ⏳ (planificada) — Refresh Token Flow:**
- Implementar `refresh_tokens` tabla (`V11__add_refresh_tokens.sql` — siguiente migración libre)
- Implementar `POST /oauth2/token` con `grant_type=refresh_token`
- Implementar rotación de tokens

**Consulta:**
- `ENTITY_RELATIONSHIPS.md` § "Flujo de token (refresh, revoke)"
- `DATA_MODEL.md` § "Tablas planificadas (fases futuras)"
- `docs/arch/keygo_server_implementation_plan.md` § Fase 7

---

**Última actualización:** 2026-03-22 | **Responsable:** AI Agent | **Estado:** ✅ Completo  
**Sincronizado con:** Migraciones V1–V10 | **Fases implementadas:** 0–6

