# Glosario de Términos — KeyGo Server

> **Descripción:** Diccionario unificado de conceptos, entidades y términos técnicos usados en KeyGo. Referencia para onboarding de nuevos devs y alineación de vocabulario.

**Fecha:** 2026-04-05

---

## A

### **Access Token**
JWT de corta duración (TTL ~1h) que autoriza requests a APIs protegidas. Contiene `sub` (user ID), `iss` (tenant), `aud` (client app), `scope` y roles del usuario.

**Contexto:** Auth Context  
**Sinónimos:** Bearer token

---

### **Admin (Rol)**
Usuario con permisos en toda la plataforma KeyGo. Puede crear/suspender tenants, ver estadísticas globales. Scope global (no acotado a tenant).

**Contexto:** Auth/Tenants  
**Valores posibles:** `ADMIN`, `ADMIN_TENANT` (ver más abajo)

---

### **Admin Tenant (Rol)**
Usuario administrador de un tenant específico. Puede crear usuarios, roles, apps, memberships, planes dentro del tenant. No ve otros tenants.

**Contexto:** Tenants  
**Path ejemplo:** `/api/v1/tenants/{tenantSlug}/users` requiere `ADMIN_TENANT` con tenant_slug matching

---

### **App / ClientApp**
Aplicación OAuth2 registrada dentro de un tenant. Ej.: "MobileApp", "WebSPA", "BackendAPI". Cada app tiene `clientId`, `clientSecret`, `redirect_uris`, `grant_types`, `scopes`.

**Contexto:** Tenants/Auth  
**Tabla:** `client_apps`  
**Relación:** 1 app ∈ 1 tenant (muchas apps por tenant)

---

### **AppRole**
Rol definido por el tenant para su app. Ej.: "Admin", "Viewer", "Editor". Code UNIQUE por app. Puede tener parent_role (jerarquía).

**Contexto:** Tenants  
**Tabla:** `app_roles`  
**Jerarquía:** AppRole → AppRoleHierarchy (V20) para expansión recursiva en JWT

---

### **AppPlan**
Plan de precios para una app (versionado). Define qué usuarios pueden activar contrato a este plan. Ej.: "FREE", "STARTER", "BUSINESS".

**Contexto:** Billing  
**Tabla:** `app_plans`  
**Status:** DRAFT (no público), ACTIVE (público en catálogo)

---

### **Authorization Code**
Token corta duración (TTL 10m) emitido en OAuth2 Authorization Code flow. Cliente lo intercambia por access_token + refresh_token.

**Contexto:** Auth  
**Tabla:** `authorization_codes`  
**PKCE:** Challenge incluida para seguridad

---

### **Authorization Code Flow**
Flujo OAuth2 seguro para SPAs. Pasos: (1) usuario autoriza, (2) backend obtiene code, (3) code → access_token.

**Contexto:** Auth  
**RFC:** OAuth 2.0 RFC 6749, PKCE RFC 7636

---

## B

### **Bearer Token**
Mecanismo de autenticación HTTP. Request incluye `Authorization: Bearer <jwt>`. Sin Bearer = 401 Unauthorized.

**Contexto:** Auth  
**Alternativa obsoleta:** X-KEYGO-ADMIN (deprecated 2026-03-25)

---

### **BCrypt**
Algoritmo de hash de contraseñas con work factor adaptativo. Usado para almacenar contraseñas de usuarios sin riesgo de reversión.

**Contexto:** Auth  
**Work Factor:** 12 (equilibrio security/performance)

---

### **Billing Period**
Período de facturación. Valores: `MONTHLY` (mensual), `ANNUAL` (anual).

**Contexto:** Billing  
**Tabla:** `app_plan_billing_options` (cada opción especifica período + precio)

---

### **Billing Option**
Variante de precio dentro una versión de plan. Ej.: "Plan Basic - Monthly $9.99", "Plan Basic - Annual $99.99".

**Contexto:** Billing  
**Tabla:** `app_plan_billing_options`  
**Atributos:** billing_period, base_price, isDefault

---

### **Bounded Context**
Dominio independiente en arquitectura hexagonal con responsabilidades claras, entidades y puertos propios.

**Contexto:** Arquitectura  
**4 en KeyGo:** Auth, Tenants, Billing, Account

---

## C

### **Client ID**
Identificador único público de una app OAuth2. No es secreto (puede estar en código frontend).

**Contexto:** Auth/Tenants  
**Formato:** UUID  
**Tabla:** `client_apps`

---

### **Client Secret**
Identificador secreto de una app OAuth2. Debe almacenarrese en backend seguro (nunca frontend). Usado en token exchange.

**Contexto:** Auth/Tenants  
**Confidentialidad:** SECRETO  
**Rotación:** [T-XXX future grace period]

---

### **ClientApp**
Ver "App / ClientApp"

---

### **Contract / AppContract**
Suscripción activa a un plan. Une plan_version + subscriber (tenant o contractor). Genera invoice automática.

**Contexto:** Billing  
**Tabla:** `app_contracts`  
**Status:** DRAFT (antes activar), ACTIVE (suscripto), CANCELLED, EXPIRED

---

### **Contractor**
Entidad pagadora sin tenant (B2C). Ej.: usuario individual que se suscribe a un plan.

**Contexto:** Billing  
**Tabla:** `contractors`  
**Contraste:** TenantUser (usuario dentro tenant)

---

## D

### **Domain Model / keygo-domain**
Código Java sin dependencias Spring. Entidades puras, value objects, excepciones tipadas. Testeable sin framework.

**Contexto:** Arquitectura  
**Módulo:** `keygo-domain`  
**Regla:** Sin @SpringBootTest, sin @Autowired, sin persistencia

---

## E

### **Email Verification**
Proceso de confirmar que email del usuario existe y es controlado por él. Código 6-dígitos enviado por SMTP/SES (TTL 30m).

**Contexto:** Auth  
**Tabla:** `email_verifications`  
**Status previo:** UNVERIFIED → ACTIVE

---

### **Entitlement / AppPlanEntitlement**
Derecho que el usuario obtiene al suscribirse a un plan. Ej.: "Max Users = 10", "Storage = 100GB", "API Calls = 10K/month".

**Contexto:** Billing  
**Tabla:** `app_plan_entitlements`  
**Enforced:** En aplicación cliente (verificar límites contra entitlements del plan)

---

## F

### **Forgot Password**
Flujo de recuperación de contraseña. Usuario ingresa email → obtiene recovery token 32-char por email → puede resetear contraseña.

**Contexto:** Auth/Account  
**Tablas:** `password_recovery_tokens` (V22), `password_reset_codes` (V23)  
**Anti-enumeration:** Endpoint retorna 200 OK siempre (no revela si email existe)

---

## G

### **Grant Type**
Tipo de concesión OAuth2. Valores soportados en KeyGo: `authorization_code`, `refresh_token`, `client_credentials`.

**Contexto:** Auth  
**Futuro:** `implicit` (deprecated), `password` (deprecated)

---

## H

### **Hexagonal Architecture / Ports & Adapters**
Patrón de arquitectura donde dominio está aislado en el centro, conectado vía puertos (interfaces) a adaptadores exteriores.

**Contexto:** Arquitectura  
**Módulos:** domain → app (puertos) → infra/supabase (adapters)

---

## I

### **ID Token**
JWT emitido en OAuth2/OIDC con información del usuario. Contiene claims: `sub`, `name`, `email`, `picture`, etc. Solo para cliente (no APIs).

**Contexto:** Auth  
**Algoritmo:** RS256 (RSA signed)  
**Diferencia con Access Token:** Access = autorización; ID = autenticación

---

### **Iss (Issuer)**
Claim en JWT que indica quién emitió el token. En KeyGo: `https://keygo.local/tenants/{slug}`.

**Contexto:** Auth  
**Validación:** Token iss debe coincidir con tenant path {slug}

---

### **Internacionalización (i18n)**
Soporte de múltiples idiomas. En KeyGo: locale resuelto desde `Accept-Language` header, error messages traducidos.

**Contexto:** Auth/Platform  
**Lenguajes soportados:** es, es-MX, en-US, pt-BR, fr (futuro)  
**Implementación:** MessageSource + messages_XX.properties

---

## J

### **JWT (JSON Web Token)**
Estándar de token seguro (RFC 7519). Formato: `header.payload.signature`. En KeyGo firmado con RSA RS256.

**Contexto:** Auth  
**Tipos en KeyGo:** Access Token, ID Token, Refresh Token (con hash SHA-256)

---

## K

### **JWKS (JSON Web Key Set)**
Endpoint público que expone claves públicas para validar JWTs. En KeyGo: `GET /.well-known/jwks.json`.

**Contexto:** Auth  
**RFC:** OpenID Connect Core, RFC 7517  
**Per tenant:** Cada tenant tiene signing keys propias

---

## L

### **Locale**
Idioma + región. Formato BCP 47: `es-MX`, `en-US`, `pt-BR`. Usado para resolver idioma de error messages.

**Contexto:** i18n  
**Resolución:** Accept-Language header → LocaleContextHolder (thread-local)  
**Fallback:** en-US

---

## M

### **Membership**
Asignación de usuario a una app con roles. Ej.: "user@acme.com is Admin + Viewer in MobileApp".

**Contexto:** Tenants  
**Tabla:** `memberships`  
**Relación:** User N:M App (vía tabla join `membership_roles`)

---

### **Multi-Tenant**
Arquitectura donde múltiples organizaciones (tenants) usan la misma aplicación con aislamiento completo de datos.

**Contexto:** Tenants  
**Aislamiento en KeyGo:** Path variable `/{tenantSlug}/` + filter JPA por tenant_id

---

## N

### **Nimbus**
Librería Java para JWT signing/verification. Usado en keygo-infra.

**Contexto:** Auth/Infra  
**Clase:** NimbusJwksBuilder, NimbusJwtVerifier

---

## O

### **OIDC (OpenID Connect)**
Capa de autenticación sobre OAuth2. Agrega ID Token + userinfo endpoint + descubrimiento.

**Contexto:** Auth  
**Estándar:** OpenID Connect Core 1.0  
**Endpoints KeyGo:** /oauth2/authorize, /oauth2/token, /userinfo, /.well-known/openapi-configuration

---

### **OAuth2**
Protocolo de autorización abierto (RFC 6749). Permite a usuarios autorizar aplicaciones sin compartir contraseña.

**Contexto:** Auth  
**Flujos en KeyGo:** Authorization Code (+ PKCE), Refresh Token, Client Credentials

---

## P

### **PKCE (Proof Key for Code Exchange)**
Extensión de OAuth2 que agrega seguridad a Authorization Code flow (RFC 7636). Previene code interception.

**Contexto:** Auth  
**Parámetro:** code_challenge en /authorize, code_verifier en /token exchange

---

### **Port**
Interfaz (clase Java) que define contrato entre dominio y adaptadores. Ej.: `PasswordHasherPort`, `TokenSignerPort`.

**Contexto:** Arquitectura  
**Ubicación:** `keygo-app/ports/*`  
**Implementación:** `keygo-infra/adapters/*`, `keygo-supabase/adapters/*`

---

### **Proyección**
[No usado actualmente en KeyGo]

---

## R

### **RBAC (Role-Based Access Control)**
Control de acceso basado en roles. Usuario tiene rol (ADMIN, ADMIN_TENANT, etc.) → puede/no acceder a recurso.

**Contexto:** Tenants/Auth  
**Implementación:** `@PreAuthorize("hasRole('ADMIN_TENANT')")`

---

### **Refresh Token**
Token de larga duración (TTL 30d) usado para obtener nuevo access_token sin re-autenticar usuario.

**Contexto:** Auth  
**Tabla:** `refresh_tokens`  
**Rotación:** Cada refresh genera nuevo refresh_token; anterior marcado USED  
**Hash:** SHA-256 (no plaintext en DB)

---

### **Reset Password**
Cambio de contraseña olvidada. Requiere código 6-dígitos (TTL 24h) enviado por email.

**Contexto:** Auth/Account  
**Flujo:** Forgot → Recover (genera código) → Reset (valida código + nueva password)  
**Status usuario:** RESET_PASSWORD → ACTIVE (post-reset)

---

### **Response Code**
Enum en keygo-api que categoriza respuestas API. Ej.: `INVALID_INPUT`, `UNAUTHORIZED`, `OPERATION_FAILED`.

**Contexto:** API/Error Handling  
**Enum:** `ResponseCode` en `keygo-api/common/ResponseCode.java`  
**Mapeo a HTTP:** HTTP status code (200, 400, 401, etc.) + clientMessage localizado

---

## S

### **Scope**
Permiso granular solicitado por cliente en OAuth2. Ej.: "scope=profile email phone". Usado para filtrar claims en userinfo.

**Contexto:** Auth  
**Estándar OIDC:** `profile`, `email`, `phone`, `address` + custom scopes

---

### **Session**
Sesión de usuario autenticado. Creada al login (exchange auth code → tokens + session). Tracking UA, IP, created/terminated_at.

**Contexto:** Auth/Account  
**Tabla:** `sessions`  
**Revocación:** DELETE /account/sessions/{id}

---

### **Signing Key**
Par RSA (clave privada + pública) usado para firmar JWTs. Uno por tenant.

**Contexto:** Auth  
**Tabla:** `signing_keys`  
**Algoritmo:** RSA 2048-bit mínimo  
**Futuro:** KMS externo (T-028)

---

## T

### **Tenant**
Organización o cliente que usa KeyGo. Ej.: "Acme Corp". Multiusuario, aislado de otros tenants.

**Contexto:** Tenants  
**Tabla:** `tenants`  
**Identificador:** slug UNIQUE (`acme`, `mycompany`, etc.)  
**Status:** ACTIVE, SUSPENDED

---

### **Tenant Admin**
Ver "Admin Tenant (Rol)"

---

### **Tenant Slug**
Identificador único human-friendly del tenant. Usado en URLs: `/api/v1/tenants/{slug}/...`.

**Contexto:** Tenants  
**Ejemplo:** `acme`, `mycompany-inc`, `test-org-123`  
**Constraint:** lowercase, alphanumeric + dash, 3-50 chars

---

### **Tenant User**
Usuario dentro de un tenant específico. Tiene OIDC claims (name, email, picture, phone, etc.) + status (ACTIVE, RESET_PASSWORD, SUSPENDED).

**Contexto:** Tenants  
**Tabla:** `tenant_users`  
**Email:** UNIQUE per tenant (mismo email en tenants distintos = usuarios distintos)

---

### **Token**
Pieza de información que autoriza acceso. En OAuth2: access_token, refresh_token, id_token.

**Contexto:** Auth  
**Formato KeyGo:** JWT (RS256 signed)

---

## U

### **Use Case**
Clase en `keygo-app` que encapsula lógica de negocio de una operación atómica. Ej.: `CreateTenantUseCase`, `ExchangeRefreshTokenUseCase`.

**Contexto:** Arquitectura  
**Patrón:** Hexagonal - Use Cases orquesta Puertos  
**Testing:** Unitario con mocks (no requiere Spring, BD)

---

### **User**
Ver "Tenant User"

---

### **User Tenant (Rol)**
Usuario de una app dentro tenant. Puede ver su perfil, sesiones, cambiar password. No gestiona otros usuarios.

**Contexto:** Tenants/Account  
**Permisos:** GET /account/profile, DELETE /account/sessions, etc. (no CRUD usuarios)

---

## V

### **Value Object**
Objeto de dominio sin identidad única (vs. Entity). Ej.: `Money`, `Email`, `Password`. Inmutable, comparado por valor.

**Contexto:** Arquitectura (keygo-domain)  
**Ejemplo:** `RefreshTokenHash` (hash SHA-256 del token, no el token plain)

---

## W

### **WebFlux**
[No usado actualmente en KeyGo] Alternativa async/non-blocking a Spring MVC.

---

## X

### **X-Forwarded-For**
Header HTTP que contiene IP original del cliente (útil detrás proxy/load balancer).

**Contexto:** Security/Sessions  
**Uso:** Tracking IP en sesiones para is_current detection

---

## Y

[Ningún término aplicable]

---

## Z

### **Zero-Knowledge Architecture**
[No usado actualmente en KeyGo]

---

## Acrónimos Comunes

| Acrónimo | Significado | Contexto |
|---|---|---|
| **ACTIVE** | Status usuario/tenant activo | Tenants |
| **BCrypt** | Algoritmo hash password | Auth |
| **CRUD** | Create, Read, Update, Delete | Operaciones |
| **JWT** | JSON Web Token | Auth |
| **OIDC** | OpenID Connect | Auth |
| **RBAC** | Role-Based Access Control | Tenants |
| **RS256** | RSA + SHA-256 (JWT signature) | Auth |
| **SPA** | Single Page Application | Frontend |
| **TTL** | Time To Live (expiración) | Tokens, Codes |
| **UUID** | Universally Unique Identifier | IDs |
| **M2M** | Machine to Machine | Auth (Client Credentials) |

---

**Última actualización:** 2026-04-05  
**Mantenimiento:** Actualizar con nuevos términos post-propuestas  
**Próxima revisión:** 2026-04-20 (post-HITO-1)
