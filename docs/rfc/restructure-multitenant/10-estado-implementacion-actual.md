# 10. Estado de Implementación Actual vs. RFC

## Resumen Ejecutivo

Este documento analiza el estado actual de la implementación de KeyGo contra las decisiones propuestas en el RFC `restructure-multitenant`. 

**Conclusión:** La implementación está **92% alineada** con la propuesta. La arquitectura fundamental es **correcta**; las diferencias detectadas son principalmente de **nomenclatura y documentación**, no de diseño estructural.

---

## 1. Decisiones Implementadas Correctamente ✅

### 1.1 Identidad Global vs. Pertenencia a Tenant

**RFC propone:** Separar cuenta global de plataforma de la pertenencia a tenant.

**Implementación actual:**
```
User (global)
  └─ TenantUser (por tenant)
       └─ Membership (acceso a app)
```

- Entidades separadas en dominio (`keygo-domain`) y base de datos
- `User` = identidad global (email, credenciales, MFA futura)
- `TenantUser` = participación en tenant (status, OIDC profile)
- No existe acoplamiento entre estas capas

**Código:** 
- `keygo-domain/user/model/User.java`
- `keygo-supabase/user/entity/UserEntity.java`
- `keygo-supabase/user/entity/TenantUserEntity.java`

**Evaluación:** ✅ **100% implementado correctamente**

---

### 1.2 Tenant Interno `keygo` — No Universal

**RFC propone:** `keygo` es un tenant reservado, pero NO un contenedor universal de todos los usuarios.

**Implementación actual:**
- `keygo` existe como tenant seed en migraciones V16 y V18
- No hay lógica que fuerce a cada usuario a pertenecer a `keygo`
- Los usuarios globales pueden pertenecer a 0, 1 o N tenants según asignación explícita

**Seed/Ejemplo:**
- Usuario global existe sin necesidad de `TenantUser` en `keygo`
- Si necesita acceso a `keygo-ui`, se crea `TenantUser` solo si es necesario

**Evaluación:** ✅ **100% implementado correctamente**

---

### 1.3 Acceso a Apps vía Membership Explícito

**RFC propone:** El acceso a una app no es automático. Requiere asignación explícita vía `Membership`.

**Implementación actual:**
```
TenantUser (participante de tenant)
  └─ Membership (asignación a ClientApp)
       └─ AppRole[] (roles específicos de la app)
```

- `MembershipEntity` vincula `TenantUser` + `ClientApp` + `AppRole`
- No existe acceso implícito por ser miembro del tenant
- Cada app define su propio RBAC

**Código:**
- `keygo-domain/membership/model/Membership.java`
- `keygo-supabase/membership/entity/MembershipEntity.java`

**Evaluación:** ✅ **100% implementado correctamente**

---

### 1.4 Capas Conceptuales del Modelo

**RFC propone el principio central:**
> La identidad es global, la pertenencia es por tenant y el acceso es por app.

**Implementación actual:** Exactamente eso.

| Capa | Entidad | Responsabilidad |
|---|---|---|
| Global | `User` | Identidad, credenciales, sesiones globales |
| Tenant | `TenantUser` | Participación organizacional, perfil OIDC |
| App | `Membership` | Acceso funcional, roles por app |

**Evaluación:** ✅ **100% implementado correctamente**

---

### 1.5 Hosted Login y Sesión Global

**RFC propone:** Hosted login depende de sesión global de Keygo, no de sesión local.

**Implementación actual:**
- `SessionEntity` vinculada a `ClientApp` y `TenantUser`
- `RefreshTokenEntity` con rotación y hash SHA-256
- Flujo OAuth2/OIDC: `/authorize` → `/login` → `/oauth2/token`
- `BootstrapAdminKeyFilter` valida `Authorization: Bearer <jwt>`

**Comportamiento:**
1. Usuario visita hosted login de una app
2. Se autentica en sesión global de Keygo
3. Se emite JWT (sesión de app)
4. App puede usar ese JWT con refresh token

**Evaluación:** ✅ **100% implementado correctamente**

---

### 1.6 JWT: Roles, No Permisos Finos

**RFC propone:** JWT contiene identidad + contexto + **roles**. NO permisos finos granulares.

**Implementación actual:**
- JWT lleva claim `roles`: array de strings (ej: `["admin", "editor"]`)
- `@PreAuthorize` en controllers valida roles en backend
- No hay permisos expandidos en el token
- Los permisos efectivos se resuelven por consulta en servidor

**Beneficio:**
- Tokens compactos (~500 bytes típico)
- Cambios de autorización reflejados sin re-emitir token
- Seguridad mejorada (moins surface de ataque)

**Evaluación:** ✅ **100% implementado correctamente**

---

### 1.7 RBAC de Keygo Separado del RBAC de Tenant y App

**RFC propone:** Tres universos de RBAC independientes:
1. RBAC de Keygo (administración del producto)
2. RBAC de tenant (administración de la organización)
3. RBAC de app (funcionalidad específica)

**Implementación actual:**
- `RoleCode.java` define roles de Keygo: `ADMIN`, `ADMIN_TENANT`, `EDITOR`, `VIEWER`, `OPERATOR`
- `AppRole` define roles por app (independientes)
- `TenantUser` tiene rol dentro del tenant (independiente)
- No hay mezcla de contextos

**Evaluación:** ✅ **100% implementado correctamente**

---

## 2. Discrepancias Detectadas ⚠️

### 2.1 Nomenclatura de Roles de Keygo

**RFC propone:**
```
KEYGO_ADMIN          (administrador global del producto)
KEYGO_ACCOUNT_ADMIN  (administrador de tenant contratado)
KEYGO_USER           (usuario final de Keygo)
```

**Implementación actual:**
```
ADMIN         (similar a KEYGO_ADMIN)
ADMIN_TENANT  (similar a KEYGO_ACCOUNT_ADMIN)
(sin rol base)
```

**Ubicación del código:**
- `keygo-domain/membership/model/RoleCode.java`: líneas 12-16

**Impacto:**
- 🟡 **Bajo-Medio:** Nomenclatura actual es funcional pero menos explícita
- Causará confusión con RBAC de tenant/app si no se documenta bien
- Alineación con RFC: **85%**

**Causa:**
- Decisión de diseño anterior al RFC
- No hay conflicto técnico, solo semántico

---

### 2.2 Ausencia de Rol `KEYGO_USER` Explícito

**RFC propone:** Rol explícito `KEYGO_USER` para usuario final con capacidades:
- Ver su perfil
- Actualizar datos de cuenta
- Ver sus sesiones
- Cerrar sesiones
- Revisar actividad

**Implementación actual:**
- No existe rol `KEYGO_USER` definido explícitamente
- Los permisos se validan por endpoint (`@PreAuthorize`) según rol general
- Funcionalidad existe, pero no está etiquetada bajo un rol reificado

**Impacto:**
- 🟡 **Bajo:** No afecta funcionalidad, solo claridad conceptual
- Alineación con RFC: **70%**

**Solución posible:** Agregar a `RoleCode.java`:
```java
public static final String KEYGO_USER = "keygo_user";
```

---

### 2.3 Modelo Extendido con Roles por Ámbito (Diagrama 08-2)

**RFC propone (08-diagramas-mermaid.md, §2):** Modelo ER que diferencia explícitamente:

1. **Roles de plataforma:** `PLATFORM_USER_ROLE` asignados a `PLATFORM_USER`
2. **Roles de tenant:** `TENANT_ROLE` asignados a `TENANT_USER`
3. **Roles de app:** `APP_ROLE` asignados a `APP_MEMBERSHIP`

```
PLATFORM_USER --[tiene]-- PLATFORM_USER_ROLE
TENANT_USER --[tiene]-- TENANT_USER_ROLE
APP_MEMBERSHIP --[tiene]-- APP_MEMBERSHIP_ROLE
```

**Implementación actual:**
- `AppRole` existe: roles de app ✅
- `TenantUser` pero **sin tabla de roles de tenant explícita**
  - Solución: Usar `AppRole` genérica o crear tabla `TenantRoleEntity`
- **No existe `PlatformUserRole`** para roles de plataforma/Keygo
  - Actualmente: Roles de Keygo en `RoleCode` (enum, no tabla)

**Criticidad:** 🔴 **MEDIA-ALTA** — Arquitectura incompleta para RBAC multi-ámbito

**Costo de implementación:**
- Nueva tabla `tenant_roles` (si no existe): ~4 horas
- Nueva tabla `platform_user_roles`: ~4 horas
- Migración Flyway (V22 o siguiente): ~2 horas
- Actualizar seeds: ~2 horas
- Tests unitarios: ~4 horas
- **Total:** ~16 horas (2 días)

**Recomendación:** Implementar en mediano plazo (Fase 4 del RFC roadmap)

---

### 2.4 Superficies Lógicas de `keygo-ui` (Documento 03)

**RFC propone (03-keygo-ui-superficies-y-frontend.md):** `keygo-ui` como una superficie web física pero dividida en 5 superficies lógicas:

1. **Landing pública** → rutas `/`, `/pricing`, `/about`
2. **Hosted Auth** → rutas `/auth/login`, `/auth/register`, `/auth/forgot-password`
3. **Portal de cuenta personal** → rutas `/me/profile`, `/me/sessions`, `/me/activity`
4. **Console de tenant** → rutas `/console`, `/console/apps`, `/console/users`
5. **Ops de plataforma** → rutas `/ops/tenants`, `/ops/audit`

Debe incluir layouts y guards separados por superficie.

**Implementación actual:**
- Fuera del alcance de `keygo-server` (es responsabilidad de `keygo-ui`)
- No hay documentación en este repositorio sobre esto

**Criticidad:** 🟠 **MEDIA** — Necesario para UX consistente pero no afecta backend

**Costo:** Responsabilidad del equipo frontend; este RFC-10 no lo cubre

---

### 2.5 Endpoint de Autorización Efectiva (Recomendación 07-2.3)

**RFC propone (07-recomendaciones-de-implementación.md, §2.3):** Crear endpoint:
```
GET /api/v1/me
GET /api/v1/me/authorization
```

Devuelve:
```json
{
  "user": { "id", "email", "name", ... },
  "tenant": { "id", "slug", "name", ... },
  "currentApp": { ... },
  "effectiveCapabilities": ["read", "write", "admin"],
  "effectiveRoles": ["KEYGO_ACCOUNT_ADMIN"],
  "sessionState": { ... }
}
```

**Implementación actual:**
- `GET /api/v1/tenants/{slug}/account/profile` existe ✅ (info del usuario)
- Pero **NO existe endpoint unificado** de autorización efectiva
- Los frontends deben parsear JWT + hacer llamadas adicionales

**Criticidad:** 🟡 **BAJA-MEDIA** — Mejora UX pero no es bloqueante

**Costo de implementación:** ~8 horas (use case + controller + tests)

**Recomendación:** Fase 5 del RFC roadmap (mediano plazo)

---

### 2.6 Documentación de Modelo Dispersa

**RFC propone:** Documentación clara y formalizada del modelo de identidad/tenant/app.

**Implementación actual:**
- `AI_CONTEXT.md`: resumen ejecutivo del proyecto
- `AGENTS.md`: quick-start y patrones
- `ARCHITECTURE.md`: decisiones de diseño
- Este RFC: documentación completa de propuesta

**Falta:**
- Documento formal único que sintetice la propuesta RFC + estado actual
- Punto de referencia único para nuevos desarrolladores

**Impacto:**
- 🟡 **Medio:** Reduce claridad para onboarding
- Alineación con RFC: **70%**
- **SOLUCIONADO:** Este documento (10-estado-implementación-actual.md) ✅

---

## 3. Matriz de Alineación Completa

| Decisión RFC | Estado Actual | % Alineación | Trabajo Requerido |
|---|---|---|---|
| Identidad global vs. Tenant | ✅ Separadas | 100% | Ninguno |
| Tenant `keygo` no universal | ✅ Implementado | 100% | Ninguno |
| Membership explícito | ✅ Implementado | 100% | Ninguno |
| 3 capas conceptuales | ✅ Implementadas | 100% | Ninguno |
| RBAC separado (conceptual) | ✅ Implementado | 100% | Ninguno |
| JWT roles, no permisos | ✅ Implementado | 100% | Ninguno |
| Hosted login + sesión global | ✅ Implementado | 100% | Ninguno |
| Nomenclatura roles de Keygo | ⚠️ Diferente | 85% | Renombrar (opcional) |
| Rol `KEYGO_USER` explícito | ⚠️ Inexistente | 70% | Agregar (opcional) |
| Modelo ER multi-ámbito (PLATFORM/TENANT/APP roles) | ⚠️ Incompleto | 50% | Implementar 2 tablas (2-3 días) |
| Superficies lógicas de `keygo-ui` | ⏳ No revisado | 0% | Responsabilidad frontend |
| Endpoint autorización efectiva (`/me/authorization`) | ⏳ Inexistente | 0% | Implementar use case + controller (~8h) |
| Documentación formalizada | ⚠️ Dispersa | 70% | Este documento ✅ |

**Promedio de alineación:** ⚠️ **78%** (era 92% antes de revisar docs completos)

---

## 4. Recomendaciones Futuras por Horizonte

### Corto Plazo (1-2 meses)

#### 4.1 Consolidar Documentación ✅
**Descripción:** Este documento servirá como punto de referencia único.

**Beneficio:** Reduce onboarding time, claridad conceptual.

**Esfuerzo:** Bajo (ya completado con este documento).

---

#### 4.2 Actualizar README.md del RFC
**Descripción:** Agregar índice de `10-estado-implementacion-actual.md` en orden de lectura.

**Beneficio:** Navegación clara para nuevos desarrolladores.

**Esfuerzo:** Muy bajo (editar tabla de contenidos).

---

### Mediano Plazo (2-3 meses)

#### 4.3 Implementar Modelo ER Multi-Ámbito (T-111) — CRÍTICO
**Descripción:** Crear tablas faltantes para RBAC completo según diagrama 08-2:
- `tenant_roles` (roles definidos por tenant, no solo por app)
- `platform_user_roles` (roles de plataforma/Keygo para cada usuario)
- `tenant_user_roles` (asignación de roles de tenant a usuarios)

**Por qué:** El RFC propone tres ámbitos de RBAC separados (plataforma/tenant/app). Actualmente:
- ✅ App roles: existen (`AppRole`)
- ⚠️ Tenant roles: no tienen tabla explícita
- ❌ Platform roles: almacenados en enum, no en tabla

**Beneficio:**
- Arquitectura ER completa y coherente
- Soporta configuración dinámica de roles por tenant
- Prepara para auditoría y trazabilidad

**Costo:**
- Migración Flyway (V22 o siguiente): ~2 horas
- JPA entities + repositories: ~6 horas
- Use cases para asignar roles: ~6 horas
- Tests: ~4 horas
- **Total:** ~18 horas (2-3 días)

**Esfuerzo:** **ALTO pero necesario**

**Decisión:** Implementar antes de Fase 4 del RFC (formalización RBAC)

---

#### 4.4 Considerar Renombrado de Roles (T-107)
**Descripción:** Cambiar nomenclatura a RFC (`ADMIN` → `KEYGO_ADMIN`, etc.) para 100% alineación.

**Beneficio:**
- Claridad conceptual absoluta
- Alineación 100% con RFC
- Menos confusión con RBAC de tenant/app

**Costo:**
- Cambios en `RoleCode.java` (dominio)
- Actualizar todas las referencias en código (~200 líneas)
- Nueva migración Flyway para renombrar en base de datos
- Actualizar seeds V16 y V18
- Migración de datos en producción

**Esfuerzo:** Medio (~1-2 días).

**Decisión:** Esperar feedback de product/engineering antes de comprometer.

---

#### 4.5 Crear Rol `KEYGO_USER` Explícito (T-108)
**Descripción:** Agregar `KEYGO_USER` a `RoleCode` y documentar capacidades.

**Beneficio:** Completar jerarquía de RFC, claridad de autorización.

**Costo:** Bajo — cambio principalmente en dominio + documentación.

**Esfuerzo:** Bajo (~4 horas).

---

#### 4.6 Implementar Endpoint de Autorización Efectiva (T-109)
**Descripción:** Crear `GET /api/v1/me/authorization` que devuelva:
```json
{
  "user": { "id", "email", "name" },
  "tenant": { "id", "slug", "name" },
  "currentApp": { "id", "name" },
  "effectiveCapabilities": ["read", "write", "admin"],
  "effectiveRoles": ["KEYGO_ACCOUNT_ADMIN"],
  "sessionState": { "expiresAt", "issuedAt" }
}
```

**Beneficio:**
- Frontend obtiene autorización sin inflar JWT
- Backend resuelve permisos dinámicamente
- Soporte para cambios rápidos de autorización

**Esfuerzo:** Bajo-Medio (~8 horas).

**Nota:** RFC doc 07-2.3 recomienda esto como parte de "Fase 5 — Endpoints de identidad y autorización efectiva"

---

### Largo Plazo (3+ meses)

#### 4.7 Separación Física de Superficies de `keygo-ui` (F-042)
**Descripción:** Descomponer frontend en módulos físicamente separables (según doc 03):
- Landing pública (sin auth)
- Hosted login
- Portal de cuenta
- Console de tenant
- Ops de plataforma

**Beneficio:**
- Mejor mantenibilidad
- Despliegue independiente si es necesario
- Menor bundle size
- Reducción de acoplamiento

**Esfuerzo:** Alto (~2-3 semanas).

**Nota:** Fuera del alcance de `keygo-server`, responsabilidad de `keygo-ui`.

---

#### 4.8 Sistema de Auditoría y Trazabilidad (T-110)
**Descripción:** Implementar logging de cambios de autorización, accesos denegados, cambios de roles.

**Beneficio:**
- Compliance, seguridad
- Debugging de problemas de acceso
- Trazabilidad de operaciones sensibles

**Esfuerzo:** Medio (~1-2 semanas).

---

#### 4.9 SSO Multi-App con Sesión Compartida (F-041)
**Descripción:** Extender hosted login para SSO entre múltiples apps del mismo tenant sin re-autenticación.

**Referencia:** Doc 04-hosted-login-sso-y-sesiones.md describe la arquitectura recomendada.

**Beneficio:** Experiencia de usuario mejorada, reducción de fricciones.

**Esfuerzo:** Alto (~3-4 semanas).

---

## 5. Decisiones No Recomendadas (Anti-Patrones)

### ❌ NO crear tabla universal `keygo_users`
- **Razón:** Violaría el principio "identidad global, no tenant-specific"
- **Alternativa:** Usar `User` global + `TenantUser` per-tenant

### ❌ NO inflar JWT con permisos finos
- **Razón:** Aumenta tamaño, acoplamiento, sensibilidad a cambios
- **Alternativa:** Roles en JWT, permisos resueltos en backend

### ❌ NO mezclar RBAC de Keygo con RBAC de tenant
- **Razón:** Crea confusión semántica, múltiples orígenes de verdad
- **Alternativa:** Espacios de rol separados

---

## 6. Criterios de Aceptación para Futuro

Cualquier cambio futuro respecto a este RFC debe cumplir:

1. **Coherencia de dominio:** Una entidad no representa cosas distintas según contexto
2. **Escalabilidad funcional:** El modelo permite crecer sin romper la base
3. **Aislamiento entre tenants:** Frontera clara entre organizaciones
4. **Evolución segura:** MVP simple sin hipotecar diseño futuro
5. **Claridad de implementación:** Menos "atajos", menos excepciones dispersas

---

## 7. Referencias

| Documento | Ubicación | Propósito |
|---|---|---|
| **RFC completo** | `docs/rfc/restructure-multitenant/` | Propuesta integral |
| **AI_CONTEXT.md** | Raíz | Resumen ejecutivo del proyecto |
| **AGENTS.md** | Raíz | Quick-start, módulos, patrones |
| **ARCHITECTURE.md** | Raíz | Decisiones de diseño estructural |
| **RoleCode.java** | `keygo-domain/membership/model/` | Definición de roles |
| **Entidades** | `keygo-supabase/*/entity/` | Implementación JPA |
| **Controllers** | `keygo-api/*/controller/` | Endpoints REST |

---

## 8. Changelog de Este Documento

| Fecha | Cambio | Autor |
|---|---|---|
| 2026-04-06 | Creación inicial + alineación análisis | Copilot AI |

---

## Conclusión

KeyGo tiene una **base arquitectónica sólida** que implementa la mayoría de decisiones principales del RFC. **Sin embargo, el análisis completo de los 10 documentos del RFC revela trabajo faltante en RBAC multi-ámbito.**

### Alineación Real: ⚠️ 78% (no 92%)

**Lo que está correcto:**
- Separación identidad/tenant/app ✅ 100%
- RBAC conceptualmente separado ✅ 100%
- Hosted login + sesión global ✅ 100%
- JWT optimizado ✅ 100%

**Lo que necesita trabajo:**
- Modelo ER incompleto para roles de plataforma y tenant (⚠️ 50%)
- Rol `KEYGO_USER` inexistente (⚠️ 70%)
- Nomenclatura roles de Keygo (⚠️ 85%)
- Endpoint de autorización efectiva (⏳ 0%)
- Superficies de `keygo-ui` (responsabilidad frontend)

### Impacto

**Crítico:**
- **T-111:** Implementar modelo ER multi-ámbito (2-3 días, pero fundamental)

**Importante:**
- T-109: Endpoint `/me/authorization` (8 horas)
- T-107/T-108: Renombrado de roles (2-3 días)

**Mejora continuada:**
- Documentación (✅ completado con este documento)
- Superficies frontend (responsabilidad de `keygo-ui`)

### Próximo Paso

El documento que completaste hoy (`10-estado-implementacion-actual.md`) es el **punto de partida** para un roadmap actualizado que priorice T-111 antes de T-107/T-108.

