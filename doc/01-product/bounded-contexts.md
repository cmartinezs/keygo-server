# Bounded Contexts — KeyGo Server

> **Descripción:** Definición de 4 dominios independientes con responsabilidades, entidades, puertos, casos de uso y dependencias explícitas. Facilita descomposición arquitectónica y asignación de responsabilidades.

**Fecha:** 2026-04-05

---

## 🎯 Visión General

KeyGo Server se divide en **4 Bounded Contexts principales** que alinean con los casos de negocio y arquitectura hexagonal:

```
┌─────────────────────────────────────────────────────────────────┐
│                        KeyGo Platform                           │
├──────────────────┬──────────────────┬──────────────┬────────────┤
│  AUTH Context    │  TENANTS Context │ BILLING Ctx  │ ACCOUNT Ctx│
│  (OAuth2/OIDC)   │ (RBAC, Isolation)│ (Suscripción)│ (Self-Srv) │
└──────────────────┴──────────────────┴──────────────┴────────────┘
         ↓                   ↓                ↓               ↓
    keygo-api        keygo-api         keygo-api        keygo-api
       ↓                   ↓                ↓               ↓
    keygo-app         keygo-app        keygo-app       keygo-app
       ↓                   ↓                ↓               ↓
 [Use Cases]        [Use Cases]      [Use Cases]    [Use Cases]
       ↓                   ↓                ↓               ↓
  Ports ↔ Infra   Ports ↔ Infra   Ports ↔ Infra  Ports ↔ Infra
```

---

## 1. BOUNDED CONTEXT: Authentication (Autenticación & OAuth2/OIDC)

### **Responsabilidad**
Emitir, validar, revocar y gestionar tokens JWT (access, refresh, ID) siguiendo OAuth2/OIDC estándares. Sesiones de usuario, verificación de email y reset de contraseña.

### **Entidades (Dominio)**
- `SigningKey` — Par RSA para firmar JWTs (por tenant)
- `Session` — Sesión de usuario autenticado (tracking UA, IP, created/terminated_at)
- `RefreshToken` — Token rotatorio (hash SHA-256, estado ACTIVE/USED/REVOKED, TTL 30d)
- `AuthorizationCode` — Code corta duración + PKCE challenge (TTL 10m)
- `EmailVerification` — Código 6-dígitos verificación email (TTL 30m)
- `PasswordResetCode` — Código 6-dígitos reset password (TTL 24h)
- `PasswordRecoveryToken` — Token 32-char para inicio forgot-password (TTL 30m)

### **Entidades JPA (Persistencia)**
- `SigningKeyEntity` → tabla `signing_keys`
- `SessionEntity` → tabla `sessions`
- `RefreshTokenEntity` → tabla `refresh_tokens`
- `AuthorizationCodeEntity` → tabla `authorization_codes`
- `EmailVerificationEntity` → tabla `email_verifications`
- `PasswordResetCodeEntity` → tabla `password_reset_codes` (V23)
- `PasswordRecoveryTokenEntity` → tabla `password_recovery_tokens` (V22)

### **Puertos (Interfaces OUT)**
```java
// Adaptado por keygo-infra / keygo-supabase
TokenSignerPort              // Firmar JWT (Nimbus RSA)
PasswordHasherPort           // Hash/verify password (BCrypt)
TokenVerifierPort            // Validar JWT signature + claims
PkceVerifierPort             // Validar PKCE challenge/verifier
EmailNotificationPort        // Enviar códigos por email (SMTP/SES)
RefreshTokenRepositoryPort   // CRUD refresh tokens
SessionRepositoryPort        // CRUD sesiones
AuthorizationCodeRepositoryPort  // CRUD auth codes
EmailVerificationRepositoryPort  // CRUD email verifications
PasswordRecoveryRepositoryPort   // CRUD recovery tokens + reset codes
```

### **Casos de Uso (keygo-app/auth)**
```
UC-A1:  AuthorizeWithCodeUseCase
        → Validar client_id, redirect_uri, scope, PKCE challenge
        → Generar authorization code (TTL 10m)
        → Retornar code al cliente

UC-A2:  ExchangeCodeForTokenUseCase
        → Validar code, code_verifier, client_id, client_secret
        → Generar access_token + id_token + refresh_token
        → Crear sesión

UC-A3:  ExchangeRefreshTokenUseCase
        → Validar refresh_token (hash, not REVOKED, not USED)
        → Detectar replay si USED (revocar cadena entera, T-035)
        → Emitir nuevo access_token + nuevo refresh_token
        → Marcar anterior refresh_token como USED

UC-A4:  RevokeTokenUseCase
        → Validar refresh_token ownership
        → Marcar refresh_token status=REVOKED
        → Idempotente

UC-A5:  GetUserInfoUseCase
        → Validar access_token válido y no expirado
        → Retornar OIDC claims (sub, name, email, picture, phone, etc.)
        → [Futuro T-043] Filtrar claims por scope solicitado

UC-A6:  ValidateUserCredentialsUseCase
        → Email existe, password matches (BCrypt), not RESET_PASSWORD
        → Retornar valid=true/false (anti-enumeration)

UC-A7:  RegisterUserUseCase
        → Crear usuario, email no existe, generar EmailVerificationCode
        → Enviar email verificación
        → Usuario status=UNVERIFIED hasta verificar código

UC-A8:  VerifyEmailUseCase
        → Validar código 6-dígitos (TTL 30m)
        → Cambiar status UNVERIFIED → ACTIVE
        → Usuario puede hacer login

UC-A9:  ForgotPasswordUseCase
        → Email existe (no revela si existe)
        → Generar PasswordRecoveryToken 32-char (TTL 30m)
        → Enviar link con token por email

UC-A10: RecoverPasswordUseCase
        → Validar recovery token
        → Generar PasswordResetCode 6-dígitos (TTL 24h)
        → Retornar requestId (UUID) para siguiente paso

UC-A11: ResetPasswordUseCase
        → Validar reset code, requestId, nueva contraseña
        → Hash nueva contraseña (BCrypt)
        → Cambiar status RESET_PASSWORD → ACTIVE

UC-A12: ChangePasswordUseCase
        → Usuario autenticado, validar contraseña actual
        → Nueva contraseña ≠ anterior, cumple complejidad
        → Hash e ir actualizando
        → Sesión sigue válida (no force logout)
```

### **Controllers (keygo-api/auth)**
```
AuthorizationController
  GET  /api/v1/tenants/{slug}/oauth2/authorize          [público]
  POST /api/v1/tenants/{slug}/oauth2/token              [público]
  POST /api/v1/tenants/{slug}/oauth2/revoke             [público]
  GET  /api/v1/tenants/{slug}/userinfo                  [Bearer]
  
UserAuthController
  POST /api/v1/tenants/{slug}/account/login             [público]
  POST /api/v1/tenants/{slug}/account/change-password   [Bearer]
  POST /api/v1/tenants/{slug}/account/forgot-password   [público]
  POST /api/v1/tenants/{slug}/account/recover-password  [público]
  POST /api/v1/tenants/{slug}/account/reset-password    [público]

DiscoveryController
  GET  /.well-known/jwks.json                           [público]
  GET  /.well-known/openid-configuration                [público]

RegistrationController
  POST /api/v1/tenants/{slug}/apps/{clientId}/register  [público]
  POST /api/v1/tenants/{slug}/apps/{clientId}/verify-email [público]
```

### **Propuestas Asociadas**
| T/F | Descripción | Horizonte |
|---|---|---|
| T-035 | Replay attack detection (USED token → revocar cadena) | Corto |
| T-036 | TTL configurable refresh tokens | Corto |
| T-043 | Scope filtering en /userinfo | Corto |
| T-063 | TraceId/requestId en ErrorData | Mediano |
| T-064 | Catálogo i18n avanzado | Mediano |
| T-059 | Redirect OAuth2 clásico (HTTP 302) | Largo |
| T-038 | Lista negra JTI revocación inmediata | Largo |
| F-043 | Forgot/recover password flow | ✅ Completado |

### **Dependencias Externas**
- `keygo-domain` — Value objects (Token, Claims, etc.)
- `keygo-infra` — TokenSignerAdapter (Nimbus RSA), PasswordHasherAdapter (BCrypt), PkceVerifierAdapter
- `keygo-supabase` — JPA Repositories para todas entidades

### **Flujos Críticos**
```
1. Authorization Code Flow:
   Client → GET /authorize (PKCE challenge)
         → Redirect to /oauth2/token (code + verifier)
         → JWT access_token + id_token + refresh_token
         → Client almacena en localStorage/sessionStorage

2. Refresh Token Rotation:
   Client token expiring → POST /oauth2/token (refresh_token)
         → Server: validar hash, check estado ≠ REVOKED/USED
         → Emitir nuevo pair: access_token + refresh_token (nuevo)
         → Cliente actualiza localStorage
         → Anterior refresh_token = USED

3. Logout / Revocation:
   Client → POST /oauth2/revoke (refresh_token, Bearer access)
         → Server: marcar status=REVOKED
         → Refresh futuros rechazados (401)
         → Access token sigue válido hasta expiración (diseño stateless)
```

---

## 2. BOUNDED CONTEXT: Tenant Management (Gestión de Tenants)

### **Responsabilidad**
Gestionar ciclo de vida de tenants (organizaciones multiusuario), aislamiento de datos por tenant, RBAC con roles jerárquicos, memberships usuario-app-roles.

### **Entidades (Dominio)**
- `Tenant` — Organización multiusuario (slug UNIQUE, status ACTIVE/SUSPENDED)
- `TenantUser` — Usuario dentro tenant (OIDC claims: name, email, picture, phone, etc.)
- `ClientApp` — Aplicación OAuth2 dentro tenant (clientId, scopes, redirect_uris, grant_types)
- `AppRole` — Rol dentro app (code UNIQUE, parent_role_id para jerarquía)
- `AppRoleHierarchy` — Relación padre-hijo roles (expansión recursiva vía CTE)
- `Membership` — Asignación usuario → app con N roles

### **Entidades JPA (Persistencia)**
- `TenantEntity` → tabla `tenants`
- `TenantUserEntity` → tabla `tenant_users`
- `ClientAppEntity` → tabla `client_apps`
- `AppRoleEntity` → tabla `app_roles`
- `AppRoleHierarchyEntity` → tabla `app_role_hierarchy` (V20)
- `MembershipEntity` → tabla `memberships`
- `MembershipRoleEntity` → tabla `membership_roles`

### **Puertos (Interfaces OUT)**
```java
TenantRepositoryPort              // CRUD tenants
TenantUserRepositoryPort          // CRUD usuarios por tenant
ClientAppRepositoryPort           // CRUD apps por tenant
AppRoleRepositoryPort             // CRUD roles por app
MembershipRepositoryPort          // CRUD memberships + roles
TenantAuthorizationEvaluator      // Verificar tenant del token vs. path
```

### **Casos de Uso (keygo-app/tenant)**
```
UC-T1:  CreateTenantUseCase
        → Admin solo, validar slug UNIQUE
        → Crear tenant status=ACTIVE
        → Seed inicial: default roles, signing key, etc.
        → Retornar tenant + admin user

UC-T2:  ListTenantsUseCase
        → Admin solo, paginación + filtros (nombre, status, created_after/before, T-071)
        → Retornar TenantData[] con paginación

UC-T3:  GetTenantUseCase
        → Admin solo
        → Retornar detalles tenant + stats

UC-T4:  UpdateTenantUseCase
        → Admin solo
        → Actualizar nombre, settings

UC-T5:  ActivateTenantUseCase
        → Admin solo
        → status SUSPENDED → ACTIVE

UC-T6:  SuspendTenantUseCase
        → Admin solo
        → status ACTIVE → SUSPENDED (usuarios no pueden login, pero data persiste)

UC-T7:  CreateTenantUserUseCase
        → ADMIN_TENANT solo, crear usuario con email único por tenant
        → status=UNVERIFIED (requiere email verification)
        → Enviar email con verify code

UC-T8:  ListTenantUsersUseCase
        → ADMIN_TENANT, paginación + filtros (nombre, status, email)
        → Retornar usuarios del tenant

UC-T9:  UpdateTenantUserUseCase
        → ADMIN_TENANT, actualizar nombre/phone/locale

UC-T10: ActivateTenantUserUseCase
        → ADMIN_TENANT, status SUSPENDED → ACTIVE

UC-T11: SuspendTenantUserUseCase
        → ADMIN_TENANT, status ACTIVE → SUSPENDED

UC-T12: ResetTenantUserPasswordUseCase
        → ADMIN_TENANT, generar PasswordResetCode, enviar email

UC-T13: CreateClientAppUseCase
        → ADMIN_TENANT, validar redirect_uris públicos, generar clientSecret
        → Crear app status=DRAFT (no usable hasta ACTIVE)

UC-T14: ListClientAppsUseCase
        → ADMIN_TENANT, paginación

UC-T15: RotateClientSecretUseCase
        → ADMIN_TENANT, generar nuevo clientSecret (anterior sigue válido 24h, T-XXX grace period)

UC-T16: CreateAppRoleUseCase
        → ADMIN_TENANT, código UNIQUE por app, nombre, descripción

UC-T17: AssignRoleParentUseCase
        → ADMIN_TENANT, establecer parent_role_id
        → Validar ciclos (no permitir A→B→A), profundidad ≤5
        → Actualizar app_role_hierarchy

UC-T18: CreateMembershipUseCase
        → ADMIN_TENANT, asignar usuario → app + roles
        → Validar roles existen en app

UC-T19: ListMembershipsUseCase
        → ADMIN_TENANT, paginación

UC-T20: RemoveMembershipUseCase
        → ADMIN_TENANT, revocar todos roles del usuario en app

UC-T21: GetTenantStatsUseCase
        → ADMIN_TENANT (T-070), retornar usuarios (por estado), apps, memberships

UC-T22: GetTenantDashboardUseCase
        → ADMIN_TENANT (T-075), KPIs detalladas, cacheadas (T-074)
```

### **Controllers (keygo-api/tenant)**
```
TenantController
  GET    /api/v1/tenants                     [Bearer + ADMIN]
  GET    /api/v1/tenants/{slug}              [Bearer + ADMIN]
  PUT    /api/v1/tenants/{slug}              [Bearer + ADMIN]
  PUT    /api/v1/tenants/{slug}/activate     [Bearer + ADMIN]
  PUT    /api/v1/tenants/{slug}/suspend      [Bearer + ADMIN]

TenantUserController
  GET    /api/v1/tenants/{slug}/users        [Bearer + ADMIN_TENANT]
  GET    /api/v1/tenants/{slug}/users/{id}   [Bearer + ADMIN_TENANT]
  PUT    /api/v1/tenants/{slug}/users/{id}   [Bearer + ADMIN_TENANT]
  PUT    /api/v1/tenants/{slug}/users/{id}/activate [Bearer + ADMIN_TENANT]
  PUT    /api/v1/tenants/{slug}/users/{id}/suspend  [Bearer + ADMIN_TENANT]
  POST   /api/v1/tenants/{slug}/users/{id}/reset-password [Bearer + ADMIN_TENANT]
  GET    /api/v1/tenants/{slug}/stats        [Bearer + ADMIN_TENANT] (T-070)
  GET    /api/v1/admin/tenants/{slug}/dashboard [Bearer + ADMIN_TENANT] (T-075)

ClientAppController
  GET    /api/v1/tenants/{slug}/apps         [Bearer + ADMIN_TENANT]
  GET    /api/v1/tenants/{slug}/apps/{id}    [Bearer + ADMIN_TENANT]
  PUT    /api/v1/tenants/{slug}/apps/{id}    [Bearer + ADMIN_TENANT]
  POST   /api/v1/tenants/{slug}/apps/{id}/rotate-secret [Bearer + ADMIN_TENANT]

AppRoleController
  GET    /api/v1/tenants/{slug}/apps/{appId}/roles [Bearer + ADMIN_TENANT]
  POST   /api/v1/tenants/{slug}/apps/{appId}/roles [Bearer + ADMIN_TENANT]
  POST   /api/v1/tenants/{slug}/apps/{appId}/roles/{roleCode}/parent [Bearer + ADMIN_TENANT]
  DELETE /api/v1/tenants/{slug}/apps/{appId}/roles/{roleCode}/parent [Bearer + ADMIN_TENANT]

MembershipController
  GET    /api/v1/tenants/{slug}/memberships  [Bearer + ADMIN_TENANT]
  DELETE /api/v1/tenants/{slug}/memberships/{id} [Bearer + ADMIN_TENANT]
```

### **Propuestas Asociadas**
| T/F | Descripción | Horizonte |
|---|---|---|
| T-024 | Tenant resolution por path {slug} | ✅ Completada |
| T-051 | Matriz @PreAuthorize exhaustiva | Corto |
| T-070 | Stats por tenant con filtros fecha | Mediano |
| T-075 | Dashboard tenant-específico | Mediano |
| T-074 | Caché dashboard (TTL 60s) | Mediano |
| T-044 | Membership attributes metadata | Mediano |
| T-045 | Claim mappers por app | Mediano |
| T-057 | Contrato multi-dominio | Mediano |
| T-055 | Bootstrap programático (control-plane) | Largo |

### **Dependencias Externas**
- `keygo-domain` — Entidades Tenant, TenantUser, etc.
- `keygo-infra` — Adapters (TenantAuthorizationEvaluator)
- `keygo-supabase` — JPA Repositories
- **Auth Context** — Sesiones usuario, roles en JWT

### **Flujo Crítico: Multi-Tenant Isolation**
```
Request: GET /api/v1/tenants/{slug}/users
         Header: Authorization: Bearer {jwt con iss="https://keygo.local/tenants/acme"}

1. BootstrapAdminKeyFilter:
   → Extraer iss del token = "https://keygo.local/tenants/acme"
   → Resolver slug = "acme" (remove "https://keygo.local/tenants/" prefix)

2. TenantAuthorizationEvaluator:
   → Validar path {slug} = "acme"
   → Validar token iss matches path slug
   → Si mismatch → 403 Forbidden

3. ListTenantUsersUseCase:
   → Query: SELECT * FROM tenant_users WHERE tenant_id = {tenantId} AND ...
   → Nunca retorna usuarios de otro tenant

4. Respuesta: Solo usuarios del tenant "acme"
```

---

## 3. BOUNDED CONTEXT: Billing (Facturación & Suscripciones)

### **Responsabilidad**
Gestionar planes de precios, contratos de suscripción, facturas, pagos e integración con gateways. Soporte para renovación automática y multi-moneda (futuro).

### **Entidades (Dominio)**
- `AppPlan` — Plan de precios para app (versionado)
- `AppPlanVersion` — Versión inmutable de plan + billing options + entitlements
- `AppPlanBillingOption` — Opción de pago (período: MONTHLY/ANNUAL, base_price)
- `AppPlanEntitlement` — Derecho del plan (feature_name, limit)
- `AppContract` — Contrato suscripción (FK plan_version + subscriber: tenant OR contractor)
- `AppSubscription` — Suscripción activa (FK contract, período actual, auto_renew)
- `Invoice` — Factura (status: PENDING/PAID/FAILED, total, período)
- `Contractor` — Pagador B2C (sin tenant_id)
- `PaymentMethod` — Método pago registrado (CARD, BANK, etc.)
- `PaymentTransaction` — Transacción pagada (status, amount, timestamp)

### **Entidades JPA (Persistencia)**
- `AppPlanEntity` → tabla `app_plans`
- `AppPlanVersionEntity` → tabla `app_plan_versions`
- `AppPlanBillingOptionEntity` → tabla `app_plan_billing_options`
- `AppPlanEntitlementEntity` → tabla `app_plan_entitlements`
- `AppContractEntity` → tabla `app_contracts`
- `AppSubscriptionEntity` → tabla `app_subscriptions`
- `InvoiceEntity` → tabla `invoices`
- `ContractorEntity` → tabla `contractors`
- `PaymentMethodEntity` → tabla `payment_methods`
- `PaymentTransactionEntity` → tabla `payment_transactions`
- `UsageCounterEntity` → tabla `usage_counters` (para billing por uso)
- `TenantBillingProfileEntity` → tabla `tenant_billing_profiles`

### **Puertos (Interfaces OUT)**
```java
AppPlanRepositoryPort              // CRUD plans + versions
BillingOptionRepositoryPort        // CRUD billing options
ContractRepositoryPort             // CRUD contracts
SubscriptionRepositoryPort         // CRUD subscriptions
InvoiceRepositoryPort              // CRUD facturas
PaymentGatewayPort                 // Integración gateway (Stripe/MercadoPago, T-084)
EmailNotificationPort              // Enviar facturas por email
```

### **Casos de Uso (keygo-app/billing)**
```
UC-B1:  ListAppPlansUseCase
        → Público (sin auth), retornar planes ACTIVE
        → Incluir billing options + entitlements
        → [T-099] Cacheado (TTL 5m)

UC-B2:  GetAppPlanDetailUseCase
        → Público, retornar plan + versión actual + options

UC-B3:  CreateAppPlanUseCase
        → ADMIN_TENANT, crear plan DRAFT (no público aún)
        → Versión inicial + billing options + entitlements
        → Validación (T-095/T-096): ≥1 option con isDefault=true

UC-B4:  PublishAppPlanVersionUseCase
        → ADMIN_TENANT, versión DRAFT → ACTIVE
        → Anteriores → INACTIVE (grandfathering)

UC-B5:  CreateContractUseCase
        → Público, especificar plan + subscriber (tenant OR contractor)
        → Contract status=DRAFT, awaiting activation

UC-B6:  ActivateContractUseCase
        → Público, cambiar DRAFT → ACTIVE
        → Crear AppSubscription (período actual)
        → Generar primera Invoice (PENDING)
        → Enviar email confirmación

UC-B7:  GenerateInvoiceUseCase
        → Cron job (@Scheduled), crear invoice por period_end vencido
        → Calcular total = base_price × factor (ej. 1.0 MONTHLY, 10.0 ANNUAL)
        → Marcar status=PENDING (awaiting payment)

UC-B8:  ProcessPaymentUseCase
        → Gateway webhook: pago aprobado/rechazado
        → Si aprobado: invoice status=PAID + subscription active
        → Si rechazado: invoice status=FAILED + retry (dunning, T-090)

UC-B9:  RenewSubscriptionUseCase
        → Cron job (@Scheduled, T-085), detectar suscripciones auto_renew
        → Crear nuevo subscription con período siguiente
        → Generar factura nueva
        → Mantener contractor/tenant

UC-B10: CancelSubscriptionUseCase
        → Usuario o admin, status ACTIVE → CANCELLED
        → Factura actual → CANCELLED (reembolso?, futura)
        → Siguiente período no renovado

UC-B11: UpdateBillingOptionUseCase
        → [T-097] ADMIN_TENANT, actualizar opciones pago sin crear versión
        → Validar no duplicar billing_period

UC-B12: ListInvoicesUseCase
        → [T-083] Usuario/admin, retornar facturas con pagination
        → Filtros: status, período

UC-B13: GetInvoiceDetailUseCase
        → [T-083] Retornar PDF [T-087] o HTML con detalles
```

### **Controllers (keygo-api/billing)**
```
AppPlanController
  GET  /api/v1/tenants/{slug}/apps/{appId}/billing/catalog [público]
  GET  /api/v1/tenants/{slug}/apps/{appId}/billing/plans    [público]
  POST /api/v1/tenants/{slug}/apps/{appId}/billing/plans    [Bearer + ADMIN_TENANT]
  PUT  /api/v1/tenants/{slug}/apps/{appId}/billing/plans/{planCode}/options [Bearer + ADMIN_TENANT] (T-097)

BillingContractController
  GET  /api/v1/billing/contracts/{contractId}               [público]
  POST /api/v1/billing/contracts/{contractId}/activate      [público]
  POST /api/v1/billing/contracts/{contractId}/mock-approve-payment [público]
  POST /api/v1/billing/contracts/{contractId}/verify-email   [público]
  POST /api/v1/billing/contracts/{contractId}/resend-verification [público]
  GET  /api/v1/billing/contracts/{contractId}/resume        [público]

AppSubscriptionController
  GET  /api/v1/tenants/{slug}/apps/{appId}/billing/subscription [Bearer]
  POST /api/v1/tenants/{slug}/apps/{appId}/billing/subscription/cancel [Bearer]

InvoiceController
  GET  /api/v1/billing/invoices/{invoiceId}                 [Bearer] (T-083)
```

### **Propuestas Asociadas**
| T/F | Descripción | Horizonte |
|---|---|---|
| T-083 | Endpoint GET /invoices/{id} | Mediano |
| T-084 | Integración gateway real (Stripe/MercadoPago) | Mediano |
| T-085 | Renovación automática @Scheduled | Mediano |
| T-086 | Bearer TENANT_USER en /subscription | Mediano |
| T-087 | PDF de facturas | Mediano |
| T-088 | CFDI México (factura electrónica) | Largo |
| T-089 | Multi-moneda | Largo |
| T-090 | Dunning (reintentos) | Mediano |
| T-097 | Actualizar billing options sin versión | Mediano |
| T-098 | Filtro subscriberType en catalog | Mediano |
| T-099 | Caché catalog (TTL 5m) | Mediano |
| T-100 | Precios tiers/escalonado | Largo |
| T-101 | Overrides precio por moneda | Largo |
| T-102 | Precios dinámicos webhook | Largo |

### **Dependencias Externas**
- `keygo-domain` — Value objects Plan, Contract, Invoice
- `keygo-supabase` — JPA Repositories + V15-V18 migraciones
- **Auth Context** — Validación usuario (Bearer token)
- **Tenant Context** — Resolver subscriber (tenant_id)
- **Gateway Externo** — Stripe/MercadoPago (T-084)
- **Email** — Notificaciones factura

---

## 4. BOUNDED CONTEXT: Account (Self-Service del Usuario)

### **Responsabilidad**
Gestionar perfil del usuario, sesiones activas, cambio de contraseña, y preferencias de notificación. Operaciones self-service del usuario autenticado.

### **Entidades (Dominio)**
- `AccountProfile` — Perfil usuario (name, email, picture, phone, birthdate, locale)
- `Session` — Sesión activa (compartida con Auth context)
- `UserNotificationPreferences` — Flags email_alerts_enabled, etc.

### **Entidades JPA (Persistencia)**
- `TenantUserEntity` → tabla `tenant_users` (campos OIDC claims)
- `SessionEntity` → tabla `sessions` (compartida)
- `UserNotificationPreferencesEntity` → tabla `user_notification_preferences` (V21)

### **Puertos (Interfaces OUT)**
```java
TenantUserRepositoryPort           // Lectura/actualización profile
SessionRepositoryPort              // CRUD sesiones (compartida)
UserNotificationRepositoryPort     // CRUD preferences
```

### **Casos de Uso (keygo-app/account)**
```
UC-AC1: GetAccountProfileUseCase
        → Usuario autenticado, retornar su profile (OIDC claims)
        → Incluye sub, name, email, picture, phone_number, birthdate, locale, updated_at

UC-AC2: UpdateAccountProfileUseCase
        → Usuario autenticado, actualizar name, picture, phone, birthdate, locale
        → Email read-only (requiere verif separada, futuro)
        → Validar locale es BCP 47 válido (es-MX, en-US, etc.)

UC-AC3: ListAccountSessionsUseCase
        → Usuario autenticado, retornar sesiones activas
        → Incluir user_agent, created_at, is_current (detectado por UA+IP)
        → Ordenar por created_at DESC

UC-AC4: RevokeSessionUseCase
        → Usuario autenticado, revocar session específica
        → Idempotente (no error si ya revocada)
        → Refresh tokens de esa sesión se invalidan

UC-AC5: GetAccountAccessUseCase
        → Usuario autenticado, retornar roles + permisos en la app
        → Desglose por app: roles, scopes, entitlements del plan
        → [Futuro T-048] Custom attributes del usuario

UC-AC6: GetNotificationPreferencesUseCase
        → Usuario autenticado, retornar flags
        → email_alerts_enabled, sms_alerts_enabled (futuro)

UC-AC7: UpdateNotificationPreferencesUseCase
        → [Futuro] Usuario configura preferencias
        → PATCH con nuevos valores
```

### **Controllers (keygo-api/account)**
```
AccountController
  GET    /api/v1/tenants/{slug}/account/profile              [Bearer]
  PATCH  /api/v1/tenants/{slug}/account/profile              [Bearer]
  GET    /api/v1/tenants/{slug}/account/access               [Bearer]
  GET    /api/v1/tenants/{slug}/account/sessions             [Bearer]
  DELETE /api/v1/tenants/{slug}/account/sessions/{id}        [Bearer]
  GET    /api/v1/tenants/{slug}/account/notification-preferences [Bearer]
  POST   /api/v1/tenants/{slug}/account/change-password      [Bearer]
  POST   /api/v1/tenants/{slug}/account/forgot-password      [público]
  POST   /api/v1/tenants/{slug}/account/recover-password     [público]
  POST   /api/v1/tenants/{slug}/account/reset-password       [público]
```

### **Propuestas Asociadas**
| T/F | Descripción | Horizonte |
|---|---|---|
| T-037 | Endpoints self-service sesiones | ✅ Completada |
| T-042 | Endpoints self-service perfil | ✅ Completada |
| T-046 | Scope profile:write explícito | Mediano |
| T-048 | Custom attributes por tenant | Largo |
| T-105 | Expiración automática temp password | Mediano |
| T-108 | Geolocalización IP sesiones | Mediano |
| T-109 | Job cleanup sesiones | Mediano |
| F-042 | Account connections | Largo |
| F-030 | RFC Account self-service | ✅ Completada |

### **Dependencias Externas**
- `keygo-domain` — Value objects UserProfile
- `keygo-supabase` — JPA Repositories
- **Auth Context** — Password change uses BCrypt hashing

---

## 5. Matriz de Dependencias Entre Contextos

```
┌──────────────────────────────────────────────────────────────┐
│                       Auth Context                           │
│  (OAuth2, tokens, sesiones, email verification, password)   │
└────────────┬──────────────────────────────────────────────┬──┘
             │                                              │
             ↓ (usuario autenticado con roles)             ↓ (token)
┌────────────────────────────────────────────────────────────────┐
│                    Tenant Context                              │
│  (tenants, usuarios, apps, roles, memberships)                │
│                                                                │
│  * Lee Session de Auth (crear sesión al login)               │
│  * Valida usuario status ≠ RESET_PASSWORD                    │
│  * Expande roles jerárquicos en JWT (claims)                 │
└────┬──────────────────────────────────────────┬───────────────┘
     │                                          │
     ↓ (tenantId, userId, roles)               ↓ (ADMIN_TENANT)
┌────────────────────────────────────────────────────────────────┐
│                    Billing Context                             │
│  (planes, contratos, facturas, pagos)                         │
│                                                                │
│  * Valida subscriber = tenant (aislamiento)                  │
│  * Respeta roles ADMIN_TENANT para modificar planes          │
└────────────────────────────────────────────────────────────────┘

     ↓ (userId + roles)
┌────────────────────────────────────────────────────────────────┐
│                    Account Context                             │
│  (perfil, sesiones, preferences)                              │
│                                                                │
│  * Usuario solo ve su propia sesión/perfil                   │
│  * Cambio password valida contraseña actual (Auth)           │
└────────────────────────────────────────────────────────────────┘
```

---

## 6. Resumen: Responsabilidades Claras

| Contexto | Responsable De | NO Responsable De |
|---|---|---|
| **Auth** | Tokens, sesiones, email/password verification | Gestión usuarios multitenants |
| **Tenants** | Usuarios, apps, roles, RBAC, aislamiento | Tokens, facturas |
| **Billing** | Planes, contratos, facturas, renovación | Usuarios, autenticación |
| **Account** | Perfil, sesiones propias, change password | Crear usuarios, validar token |

---

**Última actualización:** 2026-04-05  
**Propietario:** Arquitectura KeyGo  
**Próxima revisión:** 2026-04-20 (post-HITO-1)
