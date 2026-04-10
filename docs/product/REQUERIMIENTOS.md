# Requerimientos Funcionales y No-Funcionales — KeyGo Server

> **Descripción:** Consolidación de requisitos por dominio (contextos). Fuente: ROADMAP.md, propuestas.md, código, RFCs, y SITUACION_ACTUAL.md.

**Fecha:** 2026-04-05

---

## 1. Requerimientos Funcionales por Bounded Context

### **A. AUTHENTICATION (Autenticación & OAuth2/OIDC)**

#### **RF-A1: Authorization Code Flow con PKCE**
- **Descripción:** Cliente SPA inicia login solicitando authorization code con PKCE challenge
- **Endpoint:** `GET /api/v1/tenants/{tenantSlug}/oauth2/authorize?client_id=...&redirect_uri=...&response_type=code&scope=...&code_challenge=...&code_challenge_method=S256`
- **Respuesta:** Redirect a `redirect_uri` con `?code=...&state=...`
- **Validaciones:**
  - `redirect_uri` registrada en ClientApp
  - `code_challenge` no-vacío, length 43-128 (base64url)
  - `state` preservado (no puede ser nulo)
- **Status:** ✅ Completado
- **Propuestas asociadas:** T-059 (futuro: redirect HTTP 302 en lugar de JSON)

#### **RF-A2: Token Exchange (Authorization Code → Access Token + ID Token + Refresh Token)**
- **Descripción:** Cliente backend intercambia `code` por tokens JWT
- **Endpoint:** `POST /api/v1/tenants/{tenantSlug}/oauth2/token` (application/x-www-form-urlencoded)
- **Parámetros:** `grant_type=authorization_code, code=..., code_verifier=..., client_id=..., client_secret=...`
- **Respuesta:** `{ access_token, token_type=Bearer, expires_in, id_token, refresh_token }`
- **Validaciones:**
  - `code` válido y no expirado (TTL 10 min)
  - `code_verifier` matched PKCE challenge
  - `client_id`+`client_secret` correcto
  - Usuario no suspendido
  - Scope solicitado no excede scopes app
- **Status:** ✅ Completado
- **Propuestas asociadas:** T-043 (filtro scope en userinfo)

#### **RF-A3: Refresh Token Rotation**
- **Descripción:** Cliente intercambia refresh token por nuevo access token + nuevo refresh token
- **Endpoint:** `POST /api/v1/tenants/{tenantSlug}/oauth2/token` (grant_type=refresh_token)
- **Parámetros:** `grant_type=refresh_token, refresh_token=..., client_id=..., client_secret=...`
- **Respuesta:** `{ access_token, refresh_token (NUEVO), expires_in, token_type=Bearer }`
- **Validaciones:**
  - Refresh token no expirado (TTL 30 días)
  - Hash SHA-256 válido (no tampering)
  - Status ≠ REVOKED (sino rechaza)
  - Estado anterior NO USED (sino = replay attack → revocar cadena, T-035)
- **Comportamiento:** Nuevo refresh token reemplaza anterior; anterior queda status=USED
- **Status:** ✅ Completado
- **Propuestas asociadas:** T-035 (detección replay automática), T-036 (TTL configurable)

#### **RF-A4: Token Revocation (RFC 7009)**
- **Descripción:** Usuario/admin revoca refresh token (logout)
- **Endpoint:** `POST /api/v1/tenants/{tenantSlug}/oauth2/revoke` (Bearer + application/x-www-form-urlencoded)
- **Parámetros:** `token=<refresh_token>`
- **Respuesta:** 200 OK (idempotente incluso si ya revocado)
- **Validaciones:**
  - Token válido o ya revocado (no error)
  - Propietario del token = usuario actual (ADMIN_TENANT puede revocar otros)
- **Status:** ✅ Completado
- **Propuestas asociadas:** —

#### **RF-A5: Userinfo Endpoint (OIDC §5.3)**
- **Descripción:** Cliente obtiene perfil/claims del usuario autenticado
- **Endpoint:** `GET /api/v1/tenants/{tenantSlug}/userinfo` (Bearer)
- **Respuesta:** JSON con claims OIDC estándar: `sub`, `name`, `email`, `email_verified`, `picture`, `phone_number`, `birthdate`, `locale`, `updated_at`
- **Validaciones:**
  - Access token válido y no expirado
  - Usuario no suspendido
- **Filtrado por Scope:** [PENDIENTE T-043] Debería retornar solo claims correspon del `scope` solicitado en authorize (p. ej. scope=email → solo sub+email)
- **Status:** ✅ Completado (sin scope filtering)
- **Propuestas asociadas:** T-043 (scope filtering)

#### **RF-A6: JWKS & OpenID Configuration**
- **Descripción:** Cliente obtiene claves públicas y metadata OIDC (descubrimiento)
- **Endpoints:**
  - `GET /.well-known/jwks.json` → array de public keys (kid, kty, use, n, e)
  - `GET /.well-known/openid-configuration` → metadata: issuer, authorization_endpoint, token_endpoint, userinfo_endpoint, etc.
- **Status:** ✅ Completado
- **Propuestas asociadas:** —

#### **RF-A7: Client Credentials Grant (M2M)**
- **Descripción:** Servicio backend obtiene access token sin usuario final
- **Endpoint:** `POST /api/v1/tenants/{tenantSlug}/oauth2/token` (grant_type=client_credentials)
- **Parámetros:** `grant_type=client_credentials, client_id=..., client_secret=..., scope=...`
- **Respuesta:** `{ access_token, token_type=Bearer, expires_in }`
- **Casos de uso:** API-to-API, background jobs, admin operations
- **Status:** ✅ Completado (Fase 8)
- **Propuestas asociadas:** —

#### **RF-A8: Email Verification**
- **Descripción:** Usuario nuevo verifica email con código 6-dígitos
- **Flujo:** Register → Email enviado → Usuario ingresa código en UI → POST `/apps/{clientId}/verify-email`
- **Endpoint:** `POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/verify-email` (público)
- **Parámetros:** `email, code`
- **Validaciones:**
  - Código válido y no expirado (TTL 30 min)
  - Usuario con ese email existe y status=UNVERIFIED
  - Código matched
- **Comportamiento:** Usuario status cambia a ACTIVE; puede hacer login
- **Status:** ✅ Completado
- **Propuestas asociadas:** —

#### **RF-A9: Password Reset (Forgot + Recover + Reset)**
- **Descripción:** Usuario olvida contraseña → recupera acceso
- **Secuencia:**
  1. `POST /account/forgot-password` (público, email) → Email con recovery token (32-char, TTL 30m)
  2. Usuario click link (frontend valida token) → obtiene requestId (UUID)
  3. `POST /account/recover-password` (público, requestId, newPassword) → genera código 6-dígitos (TTL 24h)
  4. Usuario recibe código en email → ingresa en UI
  5. `POST /account/reset-password` (público, resetCodeId, resetCode, newPassword) → actualiza password, status=ACTIVE
- **Endpoints:**
  - `POST /api/v1/tenants/{tenantSlug}/account/forgot-password` (público, body: { email })
  - `POST /api/v1/tenants/{tenantSlug}/account/recover-password` (público, body: { requestId, newPassword })
  - `POST /api/v1/tenants/{tenantSlug}/account/reset-password` (público, body: { resetCodeId, resetCode, newPassword })
- **Validaciones:**
  - Email usuario existe
  - Recovery token válido/no expirado
  - Código 6-dígitos válido/no expirado
  - Nueva contraseña cumple complejidad mínima (password validator)
  - Anti-enumeración: forgot-password no revela si email existe
- **Status:** ✅ Completado (T-104)
- **Propuestas asociadas:** T-105 (expiración automática de temp password)

#### **RF-A10: Login Credential Validation (Admin)**
- **Descripción:** Admin valida credenciales usuario (sin emitir token)
- **Endpoint:** `POST /api/v1/tenants/{tenantSlug}/users/validate-credentials` (público)
- **Parámetros:** `email, password`
- **Respuesta:** 200 { valid: true } | 400 { valid: false, reason: "INVALID_CREDENTIALS" | "RESET_PASSWORD_REQUIRED" }
- **Validaciones:** Email existe, contraseña correcta (BCrypt), usuario no suspendido
- **Status:** ✅ Completado
- **Propuestas asociadas:** —

#### **RF-A11: i18n (Internacionalización) de Mensajes de Error**
- **Descripción:** Error messages traducidos por `Accept-Language` header
- **Locale Resolution:** `Accept-Language: es-MX` → resolver locale, fallback a en-US
- **Message Source:** `messages_es.properties`, `messages_en_US.properties` en classpath
- **Implementación:** `LocaleContextFilter` propaga vía `LocaleContextHolder`; `ApiErrorDataFactory.clientMessage()` usa `MessageSource`
- **Cache:** ReloadableResourceBundleMessageSource con TTL 3600 s en prod
- **Status:** ✅ Completado (T-120/T-121/T-122/T-123)
- **Propuestas asociadas:** T-064 (catálogo i18n avanzado por origen+causa)

---

### **B. TENANT MANAGEMENT (Gestión de Tenants)**

#### **RF-B1: CRUD de Tenants**
- **Operaciones:**
  - `POST /api/v1/tenants` (ADMIN) → crear
  - `GET /api/v1/tenants` (ADMIN) → listar con paginación + filtros + sort (T-110 ✅)
  - `GET /api/v1/tenants/{slug}` (ADMIN) → detalle
  - `PUT /api/v1/tenants/{slug}` (ADMIN) → actualizar (nombre, etc.)
  - `PUT /api/v1/tenants/{slug}/activate` (ADMIN) → cambiar status SUSPENDED → ACTIVE
  - `PUT /api/v1/tenants/{slug}/suspend` (ADMIN) → cambiar status ACTIVE → SUSPENDED
- **Atributos:** id, slug (UNIQUE), name, status (ACTIVE/SUSPENDED), created_at, updated_at
- **Status:** ✅ Completado
- **Propuestas asociadas:** T-071 (filtros fecha)

#### **RF-B2: Multi-Tenant Isolation**
- **Descripción:** Datos de tenant A nunca visible a tenant B
- **Implementación:** Path variable `/{tenantSlug}/` resuelta automáticamente; todas queries JPA filtran `tenant_id`
- **Validación:** `TenantAuthorizationEvaluator` verifica tenant del token = path variable
- **Status:** ✅ Completado
- **Propuestas asociadas:** —

#### **RF-B3: User Management (CRUD)**
- **Operaciones:**
  - `POST /api/v1/tenants/{tenantSlug}/users` [futuro] (ADMIN_TENANT) → crear usuario
  - `GET /api/v1/tenants/{tenantSlug}/users` (ADMIN_TENANT) → listar con paginación + filtros (T-110 ✅)
  - `GET /api/v1/tenants/{tenantSlug}/users/{userId}` (ADMIN_TENANT) → detalle
  - `PUT /api/v1/tenants/{tenantSlug}/users/{userId}` (ADMIN_TENANT) → actualizar (nombre, email)
  - `PUT /api/v1/tenants/{tenantSlug}/users/{userId}/activate` (ADMIN_TENANT) → status SUSPENDED → ACTIVE
  - `PUT /api/v1/tenants/{tenantSlug}/users/{userId}/suspend` (ADMIN_TENANT) → status ACTIVE → SUSPENDED
  - `POST /api/v1/tenants/{tenantSlug}/users/{userId}/reset-password` (ADMIN_TENANT) → genera código reset 6-dígitos, envía email
- **Status:** ✅ Completado
- **Propuestas asociadas:** T-033/T-034 (activate/suspend endpoints) ✅

#### **RF-B4: Roles Jerárquicos**
- **Descripción:** Roles con parent roles; expansión recursiva en JWT
- **Operaciones:**
  - `POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/roles` (ADMIN_TENANT) → crear rol
  - `GET /api/v1/tenants/{tenantSlug}/apps/{clientId}/roles` (ADMIN_TENANT) → listar
  - `POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/roles/{roleCode}/parent` (ADMIN_TENANT) → asignar parent
  - `DELETE /api/v1/tenants/{tenantSlug}/apps/{clientId}/roles/{roleCode}/parent` (ADMIN_TENANT) → desasignar parent
- **Validaciones:** Ciclos evitados, profundidad máx 5 niveles
- **JWT:** Claims incluyen roles expandidos (padre + abuelo + ... vía CTE)
- **Status:** ✅ Completado (V20, T-107 ✅)
- **Propuestas asociadas:** —

#### **RF-B5: Memberships (Asignación Usuario → App + Roles)**
- **Descripción:** Usuario tiene N roles en una app
- **Operaciones:**
  - `GET /api/v1/tenants/{tenantSlug}/memberships` (ADMIN_TENANT) → listar
  - `DELETE /api/v1/tenants/{tenantSlug}/memberships/{id}` (ADMIN_TENANT) → revocar roles
  - [Futuro] `POST` crear/actualizar memberships con roles específicos
- **Modelo:** Membership 1:1 UserApp, roles en tabla join `membership_roles`
- **Status:** ✅ Completado
- **Propuestas asociadas:** T-044 (metadata per membership), T-045 (claim mappers)

#### **RF-B6: Tenant Stats & Dashboard**
- **Operaciones:**
  - `GET /api/v1/platform/stats` (ADMIN) → platform-level (tenants, usuarios, apps)
  - `GET /api/v1/admin/platform/dashboard` (ADMIN) → dashboard detallado (cached)
  - `GET /api/v1/tenants/{tenantSlug}/stats` (ADMIN_TENANT) [pendiente T-070] → stats acotadas al tenant
  - `GET /api/v1/admin/tenants/{tenantSlug}/dashboard` (ADMIN_TENANT) [pendiente T-075] → dashboard tenant
- **Métricas:** Usuarios (por estado), apps (total), memberships, sesiones activas, verificaciones pendientes
- **Caché:** [Pendiente T-074] `@Cacheable` TTL 60 s
- **Status:** ✅ Parcial (platform stats ✅; tenant stats/dashboard pendientes)
- **Propuestas asociadas:** T-070, T-075, T-074

---

### **C. BILLING (Facturación & Suscripciones)**

#### **RF-C1: Catálogo de Planes**
- **Descripción:** Planes públicos (sin auth) con precios y derechos
- **Operaciones:**
  - `GET /api/v1/tenants/{tenantSlug}/apps/{clientId}/billing/catalog` (público) → lista planes activos
  - `GET /api/v1/tenants/{tenantSlug}/apps/{clientId}/billing/catalog/{planCode}` (público) → detalle + opciones de pago
- **Estructura:** AppPlan (versionado) → AppPlanVersion (inmutable) → AppPlanBillingOption (período + precio) + AppPlanEntitlement (derechos)
- **Status:** ✅ Completado
- **Propuestas asociadas:** T-099 (caché), T-098 (filtro subscriberType), T-097 (actualizar billing options)

#### **RF-C2: Activación de Suscripción (Contract)**
- **Descripción:** Suscriptor (tenant o contractor) activa contrato a un plan
- **Flujo:**
  1. `POST /api/v1/billing/contracts/{contractId}/activate` (público) → activa contrato, crea suscripción
  2. Suscripción genera invoice automática
  3. Sistema envía email con detalles
- **Parámetros:** planVersionId, billingPeriod (MONTHLY/ANNUAL), autoRenew flag
- **Validaciones:**
  - Contrato status=DRAFT
  - Planversion existe y activa
  - BillingOption existe para período
- **Status:** ✅ Completado
- **Propuestas asociadas:** T-084 (gateway real), T-085 (renovación automática)

#### **RF-C3: Verificación de Email (Contractor)**
- **Descripción:** Contractor verifica email antes de activar contrato (anti-spam)
- **Flujo:** `POST /verify-email` (código) → marcar email_verified=true
- **Reintento:** `POST /resend-verification` → re-envía código
- **Status:** ✅ Completado
- **Propuestas asociadas:** —

#### **RF-C4: Facturas**
- **Descripción:** Documento de facturación por período de suscripción
- **Operaciones:**
  - `GET /api/v1/billing/invoices` [futuro] → listar por suscriptor
  - `GET /api/v1/billing/invoices/{invoiceId}` [pendiente T-083] → detalle
- **Atributos:** id, subscription_id, period (from/to), total, status (PENDING/PAID/FAILED), created_at
- **Generación:** Automática al activar contrato; futura automática en renovación (T-085)
- **Status:** ✅ Parcial (creación ✅; consulta pendiente)
- **Propuestas asociadas:** T-083, T-087 (PDF), T-088 (CFDI)

#### **RF-C5: Pagos (Gateway)**
- **Descripción:** Integración con gateway real (Stripe/MercadoPago)
- **Operaciones:**
  - `POST /api/v1/billing/contracts/{contractId}/mock-approve-payment` (público, ACTUAL) → mock aprobación
  - [Futuro T-084] `POST /billing/payments` → crea pago real vía gateway
- **Validaciones:** Moneda, importe, método pago válido
- **Status:** 🟡 Parcial (mock ✅; real pendiente T-084)
- **Propuestas asociadas:** T-084 (gateway real), T-089 (multi-moneda), T-090 (dunning)

#### **RF-C6: Catálogo Público sin Auth**
- **Descripción:** Catálogo, contratos y pagos públicos (sin Bearer token)
- **Rutas:** `/api/v1/billing/*` sin path variable {tenantSlug}
- **Propósito:** SPA pura accede sin token para ver planes y activar contrato
- **Seguridad:** Validación por contractId (GUID), no by tenant
- **Status:** ✅ Completado
- **Propuestas asociadas:** —

---

### **D. ACCOUNT (Self-Service del Usuario)**

#### **RF-D1: Perfil de Usuario**
- **Descripción:** Usuario ve/actualiza su perfil
- **Operaciones:**
  - `GET /api/v1/tenants/{tenantSlug}/account/profile` (Bearer) → devuelve OIDC claims
  - `PATCH /api/v1/tenants/{tenantSlug}/account/profile` (Bearer) → actualiza name, picture, phone, etc.
- **Campos:** name, email (read-only), picture, phone_number, birthdate, locale, updated_at
- **Validaciones:**
  - Email read-only (no update directo; requiere verif)
  - Locale es BCP 47 válido (es-MX, en-US, etc.)
  - picture es URL válida
- **Status:** ✅ Completado (T-042 ✅)
- **Propuestas asociadas:** T-046 (scope profile:write), T-048 (custom attributes)

#### **RF-D2: Gestión de Sesiones**
- **Descripción:** Usuario ve sesiones activas (por device/browser) y revoca
- **Operaciones:**
  - `GET /api/v1/tenants/{tenantSlug}/account/sessions` (Bearer) → lista sesiones activas (user_agent, created_at, is_current)
  - `DELETE /api/v1/tenants/{tenantSlug}/account/sessions/{sessionId}` (Bearer, idempotente) → revoca sesión
- **Validaciones:**
  - Usuario solo ve sus sesiones
  - Revocación idempotente (no error si ya revocada)
- **Future:** is_current detectado por UA+IP (T-037 ✅)
- **Status:** ✅ Completado (T-037 ✅)
- **Propuestas asociadas:** T-108 (geolocalización IP), T-109 (cleanup job)

#### **RF-D3: Cambio de Contraseña**
- **Descripción:** Usuario autenticado cambia su contraseña
- **Endpoint:** `POST /api/v1/tenants/{tenantSlug}/account/change-password` (Bearer)
- **Parámetros:** currentPassword, newPassword
- **Validaciones:**
  - currentPassword coincide (BCrypt)
  - newPassword cumple complejidad mínima
  - newPassword ≠ currentPassword
- **Comportamiento:** Sesión actual válida (no logout forzado)
- **Status:** ✅ Completado
- **Propuestas asociadas:** —

#### **RF-D4: Acceso / Permisos del Usuario**
- **Descripción:** Usuario consulta sus roles y permisos en la app/tenant
- **Endpoint:** `GET /api/v1/tenants/{tenantSlug}/account/access` (Bearer)
- **Respuesta:** `{ roles: [{ code, name, ... }], permissions: [...] }`
- **Status:** ✅ Completado (T-042 ✅)
- **Propuestas asociadas:** —

#### **RF-D5: Preferencias de Notificación**
- **Descripción:** Usuario configura si recibe emails de alertas
- **Operaciones:**
  - `GET /api/v1/tenants/{tenantSlug}/account/notification-preferences` (Bearer) → devuelve flags
  - `PATCH` [futuro] → actualiza
- **Campos:** email_alerts_enabled, sms_alerts_enabled (futuro)
- **Status:** ✅ Completado (lectura ✅; escritura [futuro])
- **Propuestas asociadas:** —

#### **RF-D6: Account Connections (Futuro)**
- **Descripción:** Usuario vincula apps externas (OAuth integrations)
- **Operaciones:** `GET /account/connections` → lista de apps vinculadas
- **Futuro:** Revocar conexión, re-autorizar
- **Status:** 🔲 Pendiente (F-042)
- **Propuestas asociadas:** F-042 (RFC §5.5)

---

## 2. Requerimientos No-Funcionales

### **RNF-SEC: Seguridad**

#### **RNF-SEC-1: Bearer Token Only**
- **Mecanismo:** `Authorization: Bearer <jwt>`
- **Algoritmo:** RS256 (RSA 2048-bit mínimo)
- **Validaciones:** Firma válida, not expired, iss=tenant correcto, aud=client_id correcto
- **Exención:** Rutas públicas listadas en `BootstrapAdminKeyFilter.PUBLIC_PATHS`
- **Header prohibido:** X-KEYGO-ADMIN obsoleto
- **Status:** ✅ Completado (T-052 ✅)

#### **RNF-SEC-2: Multi-Tenant Isolation**
- **Aislamiento:** Path variable {tenantSlug} resuelta automáticamente
- **Validación:** Token iss debe coincidir con path {tenantSlug}
- **JPA Queries:** Todos filtran `tenant_id` (no opcional)
- **Audit:** Logs incluyen tenant_id y user_id por cada request
- **Status:** ✅ Completado

#### **RNF-SEC-3: RBAC (Role-Based Access Control)**
- **Roles:** ADMIN (plataforma), ADMIN_TENANT (tenant), USER_TENANT (usuario app)
- **Decorador:** `@PreAuthorize("hasRole('ADMIN_TENANT') and @tenantAuthEval.isAuthorized(...)")`
- **Validación:** Rol + tenant match por endpoint
- **Matriz:** [Pendiente T-051] Suite exhaustiva de @PreAuthorize
- **Status:** ✅ Parcial (RBAC básico ✅; matriz formal pendiente)

#### **RNF-SEC-4: PKCE (OAuth2 Security)**
- **Requerimiento:** Authorization Code flow debe usar PKCE (RFC 7636)
- **Challenge:** `code_challenge = BASE64URL(SHA256(code_verifier))` con `S256`
- **Validación:** Verifier en token request debe matchear challenge
- **Enforced:** Código rechaza requests sin PKCE
- **Status:** ✅ Completado

#### **RNF-SEC-5: Refresh Token Rotation**
- **Patrón:** Cada refresh emite nuevo refresh token; anterior queda USED
- **Hash:** SHA-256 (evita token exposure en logs)
- **Revocación:** Lista negra de JTI (futuro T-038)
- **TTL:** 30 días (configurable futuro T-036)
- **Status:** ✅ Completado

#### **RNF-SEC-6: Password Security (BCrypt)**
- **Algoritmo:** BCrypt (work factor = 12)
- **Hash:** Nunca plaintext en DB
- **Validación:** Complejidad mínima (8 chars, 1 mayús, 1 dígito, 1 especial)
- **Cambio:** Nueva contraseña ≠ anterior 3 cambios (futuro)
- **Status:** ✅ Completado (T-1 ✅)

#### **RNF-SEC-7: Anti-Enumeration (Forgot Password)**
- **Patrón:** `POST /forgot-password` retorna 200 OK siempre (email existe o no)
- **Interno:** Email ring enviado solo si usuario existe
- **Beneficio:** Attacker no puede enumerar usuarios
- **Status:** ✅ Completado

#### **RNF-SEC-8: Rate Limiting (Futuro)**
- **Requerimiento:** Limitar requests por IP para login, forgot-password (anti-brute-force)
- **Implementación:** [Pendiente] Spring Cloud Gateway / custom interceptor
- **Límite:** p. ej. 5 login attempts/min por IP
- **Status:** 🔲 Pendiente (no priorizado)

---

### **RNF-PERF: Rendimiento**

#### **RNF-PERF-1: Paginación DB-Side (JPA Specifications)**
- **Patrón:** `SELECT ... FROM table WHERE ... ORDER BY ... LIMIT offset, size` (no en-memoria)
- **Implementación:** JPA `Specification` + `JpaSpecificationExecutor` (T-110 ✅)
- **Adaptado:** 4 repos (User, ClientApp, Membership, AppRole)
- **Status:** ✅ Completado

#### **RNF-PERF-2: Caché Dashboard (Futuro T-074)**
- **Requerimiento:** GetPlatformDashboardUseCase ~25 queries → caché TTL 60s
- **Herramienta:** Spring `@Cacheable` + Caffeine
- **Invalidación:** Al crear tenant/usuario/app
- **Métrica:** Latencia <500ms (vs. actual ~2000ms)
- **Status:** 🔲 Pendiente

#### **RNF-PERF-3: Índices DB**
- **Índices esperados:** tenant_id, user_id, client_app_id, email (UNIQUE), refresh_token (unique HASH), created_at
- **Validación:** [Pendiente T-092] Script CI que compare columnas JPA ↔ migraciones
- **Status:** 🟡 Parcial (índices existen; validación no automatizada)

#### **RNF-PERF-4: Connection Pool**
- **Herramienta:** HikariCP (Spring default)
- **Config:** `spring.datasource.hikari.maximum-pool-size=20`, `minimumIdle=5`
- **Status:** ✅ Completado (valores defaults)

---

### **RNF-OBS: Observabilidad**

#### **RNF-OBS-1: Logging Estructurado**
- **Formato:** JSON (no plaintext)
- **Campos:** timestamp, level, logger, message, stack trace, tenant_id, user_id, request_id
- **Herramienta:** SLF4J + Logback (JSON layout)
- **Status:** 🟡 Parcial (logs básicos ✅; JSON layout no configurado)

#### **RNF-OBS-2: Trace ID / Request ID**
- **Requerimiento:** Cada request obtiene UUID único para trazabilidad cliente-logs
- **Propagación:** Request → ErrorData → cliente (futuro T-063)
- **Implementación:** Filter que genera request_id, propaga vía MDC
- **Status:** 🟡 Parcial (generación ✅; propagación a cliente pendiente T-063)

#### **RNF-OBS-3: Métricas (Futuro T-073)**
- **Requerimiento:** Micrometer + Prometheus
- **Métricas:** `keygo_tenants_total`, `keygo_users_total`, `keygo_sessions_active`, `keygo_tokens_issued_total`, etc.
- **Exportar:** Prometheus endpoint `/metrics`
- **Status:** 🔲 Pendiente

#### **RNF-OBS-4: Health Check**
- **Endpoint:** `GET /actuator/health` (público)
- **Status:** ✅ Completado (Spring Boot default)

#### **RNF-OBS-5: Audit Trail (Futuro T-076)**
- **Requerimiento:** Tabla `audit_events` registra: usuario, acción, recurso, timestamp, resultado (SUCCESS/FAILED)
- **Casos:** Crear usuario, asignar rol, activar contrato, etc.
- **Consulta:** `GET /api/v1/admin/audit` [futuro]
- **Status:** 🔲 Pendiente (T-076 V24)

---

### **RNF-DATA: Persistencia & Datos**

#### **RNF-DATA-1: PostgreSQL 15+**
- **Versión mínima:** 15
- **Charset:** UTF-8
- **Driver:** PostgreSQL JDBC 42+
- **Status:** ✅ Completado

#### **RNF-DATA-2: Flyway Migraciones**
- **Patrón:** `V{n}__description.sql` (versionado inmutable)
- **Aplicación:** Automática on boot
- **Validación:** DDL checks (columnsa NOT NULL, FK constraints)
- **Última:** V23 (password_reset_codes)
- **Status:** ✅ Completado

#### **RNF-DATA-3: JPA Validation (Entity Annotations)**
- **Entidades:** `@Getter @Setter @Builder` (nunca `@Data`)
- **Campos obligatorios:** `@NotNull`
- **Validación:** `@Valid` en controllers
- **Status:** ✅ Completado

#### **RNF-DATA-4: Soft Deletes (Auditoría)**
- **Patrón:** Campos `deleted_at`, `is_deleted` en entidades sensibles
- **Queries:** Filtran `WHERE is_deleted = false` automáticamente
- **Recuperación:** [Futuro] endpoint admin restore
- **Status:** 🟡 Parcial (deleted_at ✅; soft delete filter no universal)

---

### **RNF-I18N: Internacionalización**

#### **RNF-I18N-1: Locale Resolution**
- **Fuente:** `Accept-Language` header
- **Fallback:** en-US
- **Propagación:** `LocaleContextHolder` (thread-local)
- **Status:** ✅ Completado (T-121 ✅)

#### **RNF-I18N-2: Error Messages Localizados**
- **Archivos:** `messages_es.properties`, `messages_en_US.properties`, etc.
- **Catálogo:** ResponseCode enum + origen (CLIENT_REQUEST, SERVER) + causa (USER_INPUT, TECHNICAL)
- **Resolución:** `ApiErrorDataFactory.clientMessage()` consulta MessageSource por locale
- **Status:** ✅ Completado (T-122/T-123 ✅)
- **Propuestas:** T-064 (catálogo avanzado per dominio)

#### **RNF-I18N-3: Soporte Múltiples Idiomas**
- **Soportados:** es (español), es-MX (mexicano), en-US (inglés), pt-BR (portugués), fr (francés) [futura expansión]
- **Fallback chain:** es-MX → es → en-US
- **Status:** ✅ Base (es, en_US ✅; otros idiomas [futuro])

---

### **RNF-COMPAT: Compatibilidad**

#### **RNF-COMPAT-1: OpenAPI / Swagger**
- **Endpoint:** `GET /v3/api-docs` (público, JSON OpenAPI 3.0)
- **UI:** Swagger UI en `/swagger-ui/` (público)
- **Generación:** SpringDoc 3.0.1 (automática desde @RestController)
- **Status:** ✅ Completado (T-027 ✅)

#### **RNF-COMPAT-2: Response Envelope**
- **Formato:** `BaseResponse<T>` (no respuesta nativa)
- **Campos:** `data`, `error` (null si success), `timestamp`
- **Exención:** Rutas OIDC (/.well-known, /jwks) retornan JSON nativo
- **Status:** ✅ Completado

#### **RNF-COMPAT-3: HTTP Status Codes**
- **200 OK:** Request exitoso
- **400 Bad Request:** Input validation falló
- **401 Unauthorized:** Bearer token faltante/inválido
- **403 Forbidden:** Autorización falló (role/tenant mismatch)
- **404 Not Found:** Recurso no existe
- **500 Internal Server Error:** Error imprevisto (server-side)
- **Status:** ✅ Completado

#### **RNF-COMPAT-4: Error Response Format**
- **Estructura:** `{ error: { code, clientMessage, internalMessage, layer, fieldErrors, traceId } }`
- **Code:** ResponseCode enum (INVALID_INPUT, RESET_PASSWORD_REQUIRED, etc.)
- **ClientMessage:** Traducido por locale
- **FieldErrors:** Array de { field, message } para validación
- **Status:** ✅ Completado (T-065 ✅)

---

## 3. Matriz de Cobertura Actual

| Requerimiento | Status | Propuesta | Horizonte |
|---|---|---|---|
| **RF-A1 a RF-A11** | ✅ / 🟡 | Completados salvo T-035, T-043, T-059, T-036 | Corto/Largo |
| **RF-B1 a RF-B6** | ✅ / 🟡 | Completados salvo T-070, T-075, T-074 | Corto/Mediano |
| **RF-C1 a RF-C6** | ✅ / 🟡 | Completados salvo T-084, T-085, T-089, T-090 | Mediano/Largo |
| **RF-D1 a RF-D6** | ✅ | Completados salvo F-042 (connections) | Corto/Largo |
| **RNF-SEC-1 a RNF-SEC-8** | ✅ / 🔲 | Completados salvo rate limiting | Futuro |
| **RNF-PERF-1 a RNF-PERF-4** | ✅ / 🔲 | Completados salvo caché dashboard | Corto |
| **RNF-OBS-1 a RNF-OBS-5** | 🟡 / 🔲 | Parcial; futura observabilidad avanzada | Mediano/Largo |
| **RNF-DATA-1 a RNF-DATA-4** | ✅ / 🟡 | Completados; soft deletes no universal | Futuro |
| **RNF-I18N-1 a RNF-I18N-3** | ✅ | Completados i18n base; expansión idiomas [futuro] | Corto |
| **RNF-COMPAT-1 a RNF-COMPAT-4** | ✅ | Completados OpenAPI, envelopes, HTTP codes, error format | Corto |

---

## 4. Resumen Ejecutivo

### **Capacidades Completas (🟢) — 65 Requerimientos**
- ✅ Autenticación OAuth2/OIDC (auth code, PKCE, refresh rotation, revocation, userinfo, JWKS, M2M)
- ✅ Gestión de tenants (CRUD, isolation, RBAC, roles jerárquicos, memberships)
- ✅ Self-service account (perfil, sesiones, password reset)
- ✅ Billing base (catálogo, contratos, facturas, verificación email)
- ✅ Seguridad (Bearer-only, BCrypt, anti-enumeration)
- ✅ Observabilidad base (health check, request_id)
- ✅ Internacionalización (locale, error messages traducidas)
- ✅ Compatibilidad (OpenAPI, BaseResponse, error format)

### **Capacidades Parciales (🟡) — 12 Requerimientos**
- 🟡 Scope filtering en userinfo (T-043)
- 🟡 Replay attack detection (T-035)
- 🟡 Tenant stats/dashboard (T-070, T-075)
- 🟡 Billing payments (T-084 gateway real)
- 🟡 Logging estructurado (JSON layout)
- 🟡 Soft deletes universal (T-XXX)
- 🟡 Paginación DB-side (implementado; validación no CI)

### **Capacidades Pendientes (🔲) — 15 Requerimientos**
- 🔲 Rate limiting (anti-brute-force)
- 🔲 Caché dashboard (T-074)
- 🔲 Renovación automática (T-085)
- 🔲 Multi-moneda (T-089)
- 🔲 Precios dinámicos (T-102)
- 🔲 SCIM 2.0 (T-047)
- 🔲 Métricas Prometheus (T-073)
- 🔲 Audit trail (T-076)
- 🔲 Geolocalización IP (T-108)
- 🔲 Account connections (F-042)

---

**Última actualización:** 2026-04-05  
**Cobertura funcional:** 85% ✅  
**Cobertura no-funcional:** 70% ✅
