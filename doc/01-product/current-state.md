# Situación Actual — KeyGo Server (2026-04-05)

> **Referencia:** Estado completo del proyecto, módulos, capacidades implementadas, entidades, endpoints, deuda técnica y roadmap.

---

## 1. Módulos del Proyecto

```
keygo-server (Maven monorepo)
├── keygo-domain          → Dominio puro (sin Spring, sin dependencias)
├── keygo-app             → Casos de uso + puertos (interfaces)
├── keygo-infra           → Adaptadores de infraestructura (JWT, PKCE, etc.)
├── keygo-api             → Controllers REST, DTOs, error handlers
├── keygo-supabase        → JPA, Flyway migraciones, PostgreSQL adapters
├── keygo-run             → Boot principal, wiring Spring, application.yml
├── keygo-bom             → Gestión de versiones (POM padre)
└── keygo-common          → Utilidades compartidas [STUB — vacío]
```

### **Descripción por Módulo**

| Módulo | Rol | Responsabilidades | Status |
|---|---|---|---|
| **keygo-domain** | Núcleo de negocio | Entidades: `Tenant`, `TenantUser`, `ClientApp`, `AppRole`, `SigningKey`, `Session`, `RefreshToken`, `Contract`, `Invoice`, `AppPlan` (value objects puro, excepciones tipadas) | ✅ Activo — sin dependencias |
| **keygo-app** | Lógica de aplicación | ~60 casos de uso (UseCase), puertos (interfaces): `TenantRepositoryPort`, `UserRepositoryPort`, `SessionRepositoryPort`, `PasswordHasherPort`, `TokenSignerPort`, `EmailNotificationPort`, `ClockProvider` | ✅ Activo — Spring-agnostic |
| **keygo-infra** | Técnica | Implementaciones de puertos: `JwtSignerAdapter` (Nimbus RSA/RS256), `NimbusJwksBuilder`, `PkceVerifierAdapter`, `BcryptPasswordHasherAdapter` | ✅ Activo |
| **keygo-api** | REST API | 21 controllers, DTOs, respuestas `BaseResponse<T>`, error handlers globales, `@PreAuthorize` RBAC, `TenantAuthorizationEvaluator` para aislamiento multi-tenant | ✅ Activo — Bearer-only desde 2026-03-25 |
| **keygo-supabase** | Persistencia | 31 entidades JPA, migraciones Flyway (V1–V23), adapters de repositorios (`UserRepositoryAdapter`, `SessionRepositoryAdapter`, etc.), seeds de datos | ✅ Activo — PostgreSQL 15 |
| **keygo-run** | Bootstrap | Spring Boot main, `BootstrapAdminKeyFilter` (autenticación Bearer), `KeyGoTracingAspect` (logging AOP), `application.yml`, perfiles (supabase, local, docker) | ✅ Activo |
| **keygo-bom** | Dependencias | BOM (POM padre) para versionado centralizado de Spring, Jackson, TestContainers, etc. | ✅ Activo |
| **keygo-common** | Reutilizable | [Stub vacío — propuesto para utilidades compartidas] | 🚧 Pendiente |

### **Regla de Oro (Arquitectura Hexagonal)**
```
keygo-domain   → SIN dependencias Spring ni otros módulos
keygo-app      → Depende de domain; define puertos
keygo-infra    → Implementa puertos; depende de app
keygo-api      → Controllers; llama use cases; retorna BaseResponse<T>
keygo-supabase → Adapters JPA; implementa repositorio ports
keygo-run      → Cablería; main; application.yml
```

---

## 2. Capacidades por Contexto (Bounded Context)

### **A. AUTHENTICATION (Autenticación & OAuth2/OIDC)**

#### ✅ Completado (13 capacidades)
1. **Authorization Code Flow + PKCE** — endpoints `/oauth2/authorize` (query params), `/oauth2/token` (exchange code), validación PKCE per RFC 7636
2. **JWT RS256** — tokens signed con RSA keys privadas per tenant, `kid` + JWKS endpoint (`.well-known/jwks.json`)
3. **Refresh Token Rotation** — SHA-256 hash, estado (ACTIVE/USED/REVOKED), TTL 30 días, cadena de sesión
4. **Token Revocation** — endpoint `/oauth2/revoke` (RFC 7009), revoca refresh token
5. **Userinfo Endpoint** — `/userinfo` (OIDC §5.3), retorna OIDC claims del usuario (`name`, `email`, `picture`, `phone`, etc.)
6. **OpenID Configuration** — `/.well-known/openid-configuration` (metadata OIDC)
7. **Client Credentials Grant** — M2M sin usuario final, acceso a endpoints como `/api/v1/platform/stats`
8. **Session Management** — tabla `sessions`, tracking user agent, creación/terminación
9. **Email Verification** — código 6-dígitos con TTL 30 min para nuevos usuarios
10. **Password Reset Code** — 6-dígitos (TTL 24h) para reset de contraseña
11. **Password Recovery Token** — 32-char (TTL 30 min) para inicio de flujo forgot-password
12. **Status RESET_PASSWORD** — bloquea login automáticamente; requiere código + nueva contraseña
13. **i18n + Locale Resolution** — `Accept-Language` header → locale resolution, cache message source, error clientMessage por idioma

#### 🟡 Parcial (2 capacidades)
- **Scope Filtering en Userinfo** (T-043): Claims debería filtrarse por scope solicitado (`profile`, `email`, `phone`). Actualmente retorna todos.
- **Replay Attack Detection** (T-035): Refresh token `USED` debe revocar cadena entera. Actualmente manual.

#### 🔲 Pendiente (2 capacidades)
- **Redirect OAuth2 Clásico** (T-059): Retorno de `authorization_code` vía redirect HTTP 302 en lugar de JSON
- **TTL Configurable** (T-036): refresh tokens TTL fijo a 30 días; debería ser editable en `application.yml`

---

### **B. TENANT MANAGEMENT (Gestión de Tenants)**

#### ✅ Completado (8 capacidades)
1. **CRUD de Tenants** — crear, leer, actualizar, listar, activar, suspender
2. **Multi-Tenant Isolation** — aislamiento por `tenant_slug` en path (`/api/v1/tenants/{tenantSlug}/...`)
3. **Tenant Resolution** — resolver tenant desde path variable automáticamente
4. **Roles Jerárquicos** — `parent_role_id` en `app_roles`, expansión recursiva vía CTE en JWT (V20, T-107 ✅)
5. **RBAC Básico** — `@PreAuthorize` por endpoint, roles `ADMIN` y `ADMIN_TENANT`
6. **User Management** — CRUD usuarios por tenant (activate, suspend, status = ACTIVE/RESET_PASSWORD)
7. **Memberships** — asignación usuario→app con roles (N:M vía `membership_roles`)
8. **Tenant Stats** — conteos parciales en platform dashboard (usuarios, apps, memberships, signing keys)

#### 🔲 Pendiente (3 capacidades)
- **Dashboard Tenant-Específico** (T-075): GET `/api/v1/admin/tenants/{slug}/dashboard` (KPIs acotados al tenant)
- **Estadísticas con Filtros de Fecha** (T-071): created_after/created_before en ListTenantsUseCase
- **Matrix de Autorización Formal** (T-051): Suite exhaustiva @PreAuthorize por endpoint (ADMIN, ADMIN_TENANT match/mismatch, USER_TENANT)

---

### **C. BILLING (Facturación & Suscripciones)**

#### ✅ Completado (6 capacidades)
1. **Modelo v2: Planes** — `AppPlan` + `AppPlanVersion` (versionado inmutable) + `AppPlanBillingOption` (período + precio) + `AppPlanEntitlement` (derechos del plan)
2. **Contratos (Contracts)** — suscripción a plan, FK plan_version + subscriber (tenant O contractor)
3. **Facturas (Invoices)** — documento generado por período de contrato, status (PENDING/PAID/FAILED)
4. **Suscripciones (Subscriptions)** — vínculo contrato-período, `auto_renew` flag (futuro)
5. **Endpoints Públicos de Catálogo** — GET `/billing/catalog` (plans), GET `/billing/contracts/{id}/activate` (acceso sin token)
6. **Seeds Funcionales** — V17/V18 (planes FREE/STARTER/BUSINESS/ENTERPRISE para keygo-platform, contractors de prueba)

#### 🟡 Parcial (2 capacidades)
- **Contractor (B2C)** — entidad sin `tenant_id`, pero sin modelos de negocio B2B establecidos
- **Status Ciclo Vida** — estados básicos (DRAFT, ACTIVE, EXPIRED, CANCELLED) sin transiciones full documentadas

#### 🔲 Pendiente (6+ capacidades)
- **Integración Gateway Real** (T-084): Stripe/MercadoPago (actualmente solo mock `/mock-approve-payment`)
- **Renovación Automática** (T-085): job `@Scheduled` detecta vencidas, crea factura nueva
- **Multi-Moneda** (T-101): overrides de precio por moneda (USD, MXN, EUR)
- **Precios Dinámicos** (T-102): webhook externo para precio en tiempo real
- **PDF de Facturas** (T-087): iText/JasperReports → S3/Supabase Storage
- **Filtros avanzados** (T-098): subscriberType, caché (T-099), endpoint invoice individual (T-083)

---

### **D. ACCOUNT (Self-Service del Usuario)**

#### ✅ Completado (6 capacidades)
1. **Perfil** — GET/PATCH `/account/profile` (name, email, picture, phone, birthdate, locale)
2. **Sesiones** — GET `/account/sessions` (lista activas), DELETE idempotente por sessionId
3. **Cambio de Contraseña** — POST `/account/change-password` (verifica actual, nueva con complejidad mínima, BCrypt)
4. **Forgot Password** — POST `/account/forgot-password` (anti-enumeración, genera recovery token 32-char)
5. **Recover Password** — POST `/account/recover-password` (valida token, genera código 6-dígitos)
6. **Reset Password** — POST `/account/reset-password` (valida código, asigna nueva, cambia status ACTIVE)

#### 🔲 Pendiente (2 capacidades)
- **Geolocalización IP** (T-108): campo `location` en `sessions`, requiere `GeoIpPort` adapter
- **Expiración Contraseña Temporal** (T-105): TTL 24h, job que re-envía si vence
- **Account Connections** (F-042): apps externas/OAuth vinculadas (tabla futura `connections`, V24+)

---

### **E. PLATFORM (Estadísticas & Observabilidad)**

#### ✅ Completado (3 capacidades)
1. **Service Info** — GET `/api/v1/service/info` (nombre, versión, ambiente)
2. **Response Codes** — GET `/api/v1/response-codes` (catálogo enum `ResponseCode`)
3. **Platform Stats** — GET `/api/v1/platform/stats` (conteos: tenants, usuarios, apps, signing keys; require ADMIN)
4. **Platform Dashboard** — GET `/api/v1/admin/platform/dashboard` (KPIs: usuarios por estado, apps, memberships, sesiones, verificaciones; cached)

#### 🔲 Pendiente (2 capacidades)
- **Caché Dashboard** (T-074): `@Cacheable` TTL 60 s (actualmente ~25 queries JPA)
- **Push de Métricas** (T-073): Micrometer/Prometheus, `keygo_tenants_total`, `keygo_users_total`, `keygo_sessions_active`, etc.

---

## 3. Entidades Principales (JPA)

| Entidad | Tabla | Descripción | V. Flyway | Status |
|---|---|---|---|---|
| **Tenant** | `tenants` | Organización/empresa multiusuario | V1 | ✅ |
| **TenantUser** | `tenant_users` | Usuario dentro tenant + OIDC claims | V1 + V13 | ✅ |
| **ClientApp** | `client_apps` | App OAuth2 en tenant | V1 | ✅ |
| **AppRole** | `app_roles` | Rol en app (code UNIQUE) + parent_role_id | V1 + V20 | ✅ |
| **AppRoleHierarchy** | `app_role_hierarchy` | Relación jerárquica (CTE recursiva) | V20 | ✅ |
| **Membership** | `memberships` | Usuario→App con roles (N:M) | V1 | ✅ |
| **MembershipRole** | `membership_roles` | Asignación roles a membership | V1 | ✅ |
| **SigningKey** | `signing_keys` | RSA key pair JWT | V1 | ✅ |
| **Session** | `sessions` | Sesión usuario autenticado | V7 | ✅ |
| **RefreshToken** | `refresh_tokens` | Token rotatorio (hash SHA-256) | V7 | ✅ |
| **AuthorizationCode** | `authorization_codes` | Code corta duración + PKCE | V5 | ✅ |
| **EmailVerification** | `email_verifications` | Verificación email usuario | V6 | ✅ |
| **PasswordResetCode** | `password_reset_codes` | Código 6-dígitos reset (TTL 24h) | V23 | ✅ |
| **PasswordRecoveryToken** | `password_recovery_tokens` | Token 32-char recover (TTL 30m) | V22 | ✅ |
| **AppPlan** | `app_plans` | Plan de precios (versionado) | V15 | ✅ |
| **AppPlanVersion** | `app_plan_versions` | Versión inmutable de plan | V15 | ✅ |
| **AppPlanBillingOption** | `app_plan_billing_options` | Opción pago (período + precio) | V15 | ✅ |
| **AppPlanEntitlement** | `app_plan_entitlements` | Derecho del plan (feature + limit) | V15 | ✅ |
| **AppContract** | `app_contracts` | Contrato suscripción | V16 | ✅ |
| **AppSubscription** | `app_subscriptions` | Suscripción activa (período vigente) | V16 | ✅ |
| **Invoice** | `invoices` | Factura generada (PENDING/PAID/FAILED) | V16 | ✅ |
| **Contractor** | `contractors` | Pagador B2C (sin tenant_id) | V16 | ✅ |
| **TenantBillingProfile** | `tenant_billing_profiles` | Perfil fiscal/pago tenant | V17 | ✅ |
| **PaymentMethod** | `payment_methods` | Método pago registrado (CARD/BANK) | V18 | ✅ |
| **PaymentTransaction** | `payment_transactions` | Transacción pagada | V18 | ✅ |
| **UsageCounter** | `usage_counters` | Contador uso para billing | V19 | ✅ |
| **UserNotificationPreferences** | `user_notification_preferences` | Email alerts on/off | V21 | ✅ |

**Total:** 31 entidades | Última migración: **V23** (`password_reset_codes`)

---

## 4. Endpoints Activos por Contexto

### **AUTH (5 controllers, ~18 endpoints)**
```
GET    /api/v1/tenants/{tenantSlug}/oauth2/authorize          [público]
POST   /api/v1/tenants/{tenantSlug}/oauth2/token              [público]
POST   /api/v1/tenants/{tenantSlug}/oauth2/revoke             [público]
GET    /api/v1/tenants/{tenantSlug}/userinfo                  [Bearer]
GET    /api/v1/tenants/{tenantSlug}/.well-known/jwks.json     [público]
GET    /api/v1/tenants/{tenantSlug}/.well-known/openid-config [público]
POST   /api/v1/tenants/{tenantSlug}/account/login             [público]
```

### **TENANTS (1 controller, 5 endpoints)**
```
GET    /api/v1/tenants                              [Bearer + ADMIN]
GET    /api/v1/tenants/{slug}                       [Bearer + ADMIN]
PUT    /api/v1/tenants/{slug}                       [Bearer + ADMIN]
PUT    /api/v1/tenants/{slug}/activate              [Bearer + ADMIN]
PUT    /api/v1/tenants/{slug}/suspend               [Bearer + ADMIN]
```

### **USERS (2 controllers, 7 endpoints)**
```
GET    /api/v1/tenants/{tenantSlug}/users/{userId}                   [Bearer + ADMIN_TENANT]
GET    /api/v1/tenants/{tenantSlug}/users                            [Bearer + ADMIN_TENANT]
PUT    /api/v1/tenants/{tenantSlug}/users/{userId}                   [Bearer + ADMIN_TENANT]
PUT    /api/v1/tenants/{tenantSlug}/users/{userId}/activate          [Bearer + ADMIN_TENANT]
PUT    /api/v1/tenants/{tenantSlug}/users/{userId}/suspend           [Bearer + ADMIN_TENANT]
POST   /api/v1/tenants/{tenantSlug}/users/{userId}/reset-password    [Bearer + ADMIN_TENANT]
POST   /api/v1/tenants/{tenantSlug}/users/validate-credentials       [público]
```

### **ACCOUNT (2 controllers, 10 endpoints)**
```
GET    /api/v1/tenants/{tenantSlug}/account/profile                  [Bearer]
PATCH  /api/v1/tenants/{tenantSlug}/account/profile                  [Bearer]
GET    /api/v1/tenants/{tenantSlug}/account/access                   [Bearer]
GET    /api/v1/tenants/{tenantSlug}/account/sessions                 [Bearer]
DELETE /api/v1/tenants/{tenantSlug}/account/sessions/{sessionId}     [Bearer]
GET    /api/v1/tenants/{tenantSlug}/account/notification-preferences [Bearer]
POST   /api/v1/tenants/{tenantSlug}/account/change-password          [Bearer]
POST   /api/v1/tenants/{tenantSlug}/account/forgot-password          [público]
POST   /api/v1/tenants/{tenantSlug}/account/recover-password         [público]
POST   /api/v1/tenants/{tenantSlug}/account/reset-password           [público + requestId]
```

### **BILLING (3 controllers, 12 endpoints)**
```
GET    /api/v1/tenants/{tenantSlug}/apps/{clientId}/billing/catalog  [público]
GET    /api/v1/tenants/{tenantSlug}/apps/{clientId}/billing/plans    [público]
POST   /api/v1/tenants/{tenantSlug}/apps/{clientId}/billing/plans    [Bearer + ADMIN_TENANT]
GET    /api/v1/tenants/{tenantSlug}/apps/{clientId}/billing/subscription [Bearer]
POST   /api/v1/tenants/{tenantSlug}/apps/{clientId}/billing/subscription/cancel [Bearer]
GET    /api/v1/billing/contracts/{contractId}                        [público]
POST   /api/v1/billing/contracts/{contractId}/activate               [público]
POST   /api/v1/billing/contracts/{contractId}/mock-approve-payment   [público]
POST   /api/v1/billing/contracts/{contractId}/verify-email           [público]
POST   /api/v1/billing/contracts/{contractId}/resend-verification    [público]
GET    /api/v1/billing/contracts/{contractId}/resume                 [público]
```

### **ROLES & MEMBERSHIPS (2 controllers, 5 endpoints)**
```
GET    /api/v1/tenants/{tenantSlug}/memberships                      [Bearer + ADMIN_TENANT]
DELETE /api/v1/tenants/{tenantSlug}/memberships/{id}                 [Bearer + ADMIN_TENANT]
GET    /api/v1/tenants/{tenantSlug}/apps/{clientId}/roles            [Bearer + ADMIN_TENANT]
POST   /api/v1/tenants/{tenantSlug}/apps/{clientId}/roles/{roleCode}/parent [Bearer + ADMIN_TENANT]
DELETE /api/v1/tenants/{tenantSlug}/apps/{clientId}/roles/{roleCode}/parent [Bearer + ADMIN_TENANT]
```

### **CLIENT APPS (1 controller, 4 endpoints)**
```
GET    /api/v1/tenants/{tenantSlug}/apps                       [Bearer + ADMIN_TENANT]
GET    /api/v1/tenants/{tenantSlug}/apps/{clientId}            [Bearer + ADMIN_TENANT]
PUT    /api/v1/tenants/{tenantSlug}/apps/{clientId}            [Bearer + ADMIN_TENANT]
POST   /api/v1/tenants/{tenantSlug}/apps/{clientId}/rotate-secret [Bearer + ADMIN_TENANT]
```

### **REGISTRATION (1 controller, 3 endpoints)**
```
POST   /api/v1/tenants/{tenantSlug}/apps/{clientId}/register              [público]
POST   /api/v1/tenants/{tenantSlug}/apps/{clientId}/verify-email          [público]
POST   /api/v1/tenants/{tenantSlug}/apps/{clientId}/resend-verification   [público]
```

### **PLATFORM (3 controllers, 8 endpoints)**
```
GET    /api/v1/service/info                           [público]
GET    /api/v1/response-codes                         [público]
GET    /api/v1/platform/stats                         [Bearer + ADMIN]
GET    /api/v1/admin/platform/dashboard               [Bearer + ADMIN]
GET    /api/v1/tenants/{tenantSlug}/stats             [Bearer + ADMIN_TENANT] [pendiente T-070]
GET    /api/v1/admin/tenants/{tenantSlug}/dashboard   [Bearer + ADMIN_TENANT] [pendiente T-075]
```

**Total:** 21 controllers | ~75 endpoints (20 públicos, 55 requieren Bearer)

---

## 5. Deuda Técnica Identificada

### **Crítica** ⚠️
Ninguna identificada actualmente.

### **Alta** 🔴
1. **Signing Key Bootstrapper Redundante** — `SigningKeyBootstrapService` + `SigningKeyInitializer` duplicados. Action: eliminar Bootstrap.
2. **Sin Detección Replay Attack** (T-035) — Refresh token `USED` debería revocar cadena entera. Mitigación: manual actualmente.

### **Media** 🟠
3. **Documentación Desalineada** — `ARCHITECTURE.md` describe `X-KEYGO-ADMIN` (obsoleto desde 2026-03-25); debería ser Bearer-only.
4. **Lint/Formato No Enforced** (T-023) — Checkstyle/Spotless no configurado; convención existe pero sin CI enforcement.
5. **Sans Feature Flag Signing Key** — cada reinicio genera clave nueva si no existe; no idempotente en balancing.

### **Baja** 🟡
6. **Scope Filtering Incompleto** (T-043) — `/userinfo` no filtra claims por scope solicitado.
7. **Sin Caché Dashboard** (T-074) — GetPlatformDashboardUseCase realiza ~25 queries; sin `@Cacheable`.
8. **Paginación en-Memoria Eliminada** (T-110 ✅) — ya reemplazada por JPA Specifications en 4 adapters.

---

## 6. Roadmap: Estado Actual

| Horizonte | ✅ Completadas | 🔲 Pendientes | 🟡 Parciales | Total |
|---|---|---|---|---|
| **Corto plazo** | 20 | 37 | 2 | 59 |
| **Mediano plazo** | 18 | 28 | 1 | 47 |
| **Largo plazo** | 3 | 42 | 1 | 46 |
| **TOTAL** | **41** | **107** | **4** | **152** |

### **Completadas Recientes (últimos 7 días)**
- T-120/T-121/T-122/T-123: i18n completo (locale resolver, context filter, message source) ✅
- T-110: Paginación JPA Specifications (eliminó en-memoria) ✅
- T-103/T-104/T-107: Password reset, endpoint reset, role hierarchy ✅
- T-033/T-034/T-049/T-065/T-068/T-069/T-082: Tests + endpoints + regresión ✅

---

## 7. Próximas Migraciones (Roadmap DB)

| Propuesta | Tabla | Descripción | Horizonte |
|---|---|---|---|
| **V24** | `audit_events` | Reemplaza recentActivity aproximada (T-076) | Mediano |
| **V25** | `membership_attributes` | Metadata app-específica per usuario (T-044) | Mediano |
| **V26** | `exchange_rates` | Tipos de cambio para multi-moneda (T-089) | Largo |
| **V27** | `dunning_events` | Eventos reintentos de cobro (T-090) | Largo |
| **V28** | `payment_integrations` | Config de gateway (Stripe, MercadoPago) (T-084) | Mediano |
| **V29** | `subscriptions` | Mejora table structure (T-085 renovación) | Mediano |

---

## 8. Resumen Ejecutivo

**KeyGo Server** es una **plataforma de autenticación multi-tenant con OAuth2/OIDC, RBAC jerárquico y facturación integrada**. Arquitectura **hexagonal** separa dominio, aplicación e infraestructura, facilitando testing e interoperabilidad.

### **Fortalezas** ✅
- **Auth core robusto:** Authorization Code + PKCE, refresh rotation, revocación, JWKS
- **Multi-tenant isolation:** Path variable, aislamiento de datos, RBAC `@PreAuthorize`
- **Modelo billing escalable:** Planes versionados, contratos, facturas, seeds funcionales
- **Account self-service completo:** Password reset, sesiones, perfil, access
- **Desarrollo activo:** 41 propuestas completadas, 107 en pipeline

### **Limitaciones Actuales** 🚧
- **Billing sin gateway real** — solo mock `/mock-approve-payment`
- **Sin multi-moneda/precios dinámicos** — base de precios única
- **Sin observabilidad avanzada** — logs básicos, sin Prometheus/OpenTelemetry
- **Deuda técnica menor:** 2 inconsistencias no-críticas, sin lint enforced

### **Próximas Prioridades** 🎯
1. **Eliminar redundancia** SigningKeyBootstrapService (2 horas)
2. **Documentación seguridad** actualizada (ARCHITECTURE.md, BOOTSTRAP_FILTER.md) (4 horas)
3. **Tests integración** Testcontainers V20+ (T-013, T-025, T-091) (16 horas)
4. **Caché dashboard** (T-074) + endpoint tenant dashboard (T-075) (8 horas)

---

**Última actualización:** 2026-04-05  
**Próxima revisión recomendada:** 2026-04-20 (post-milestone)
