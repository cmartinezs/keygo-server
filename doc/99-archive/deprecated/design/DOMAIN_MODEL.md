# DOMAIN_MODEL (Archived)

⚠️ **This document is archived and no longer maintained.**

Domain model is now documented in:
- [`./patterns/PATTERNS.md`](./patterns/PATTERNS.md) — Domain-driven design patterns
- [`./DATABASE_SCHEMA.md`](./DATABASE_SCHEMA.md) — Entity definitions and relationships
- [`./ARCHITECTURE.md`](./ARCHITECTURE.md) — System design and bounded contexts

Archived in: [`../archive/deprecated/`](../archive/deprecated/)

---

## 2. Contexto de dominio

Key-go Server es un **Identity Provider SaaS multi-tenant**.

Cada empresa cliente opera como un **tenant** dentro del sistema. Dentro de ese tenant:

- existen usuarios,
- existen aplicaciones cliente,
- y existen relaciones de acceso entre usuarios y aplicaciones.

La idea central del dominio es:

> un usuario existe una sola vez por tenant, pero puede acceder a múltiples aplicaciones del mismo tenant mediante memberships independientes.

Esto evita duplicación de cuentas y permite autorización contextual por aplicación.

---

## 3. Bounded Contexts sugeridos

Aunque el sistema nazca como modular monolith, el dominio conviene organizarlo en contextos lógicos.

### 3.1. Tenant Management
Responsable de:
- tenants,
- estado del tenant,
- identidad organizacional básica,
- políticas generales del tenant.

### 3.2. Client Application Management
Responsable de:
- registro de apps cliente,
- redirect URIs,
- grants permitidos,
- secrets,
- tipo de aplicación.

### 3.3. User Identity
Responsable de:
- usuario,
- credenciales locales,
- estado de cuenta,
- recuperación de acceso.

### 3.4. Membership & App Authorization
Responsable de:
- relación usuario ↔ aplicación,
- roles por app,
- políticas de acceso a apps.

### 3.5. Authorization Server
Responsable de:
- authorization code,
- emisión de tokens,
- refresh,
- revoke,
- claims,
- metadata OIDC.

### 3.6. Platform Administration
Responsable de:
- administración global del SaaS,
- operación de tenants,
- soporte,
- privilegios de plataforma.

### 3.7. Audit & Security Events
Responsable de:
- trazabilidad,
- eventos de seguridad,
- observabilidad del dominio.

---

## 4. Entidades principales del dominio

## 4.1. Tenant

### Descripción
Representa una organización cliente dentro de Key-go.

### Responsabilidades
- delimitar el espacio de identidad y administración,
- agrupar usuarios y aplicaciones,
- definir políticas generales,
- actuar como frontera de aislamiento lógico.

### Atributos conceptuales
- TenantId
- Slug
- Name
- Status
- Branding básico *(opcional en MVP)*

### Estados posibles
- ACTIVE
- SUSPENDED
- ARCHIVED *(opcional, más adelante)*

### Invariantes
- el slug debe ser único globalmente,
- un tenant suspendido no debe permitir autenticación operativa normal,
- toda entidad dependiente debe pertenecer exactamente a un tenant.

---

## 4.2. ClientApp

### Descripción
Representa una aplicación registrada por un tenant para usar Key-go como proveedor de identidad.

### Responsabilidades
- identificarse como client OAuth/OIDC,
- declarar cómo inicia autenticación,
- definir redirect URIs,
- exponer su tipo de uso: pública o confidencial.

### Atributos conceptuales
- ClientAppId
- TenantId
- ClientId
- ClientType
- DisplayName
- Status
- AllowedGrants
- AllowedScopes
- RedirectUris
- AccessPolicy

### Tipos de client
- PUBLIC
- CONFIDENTIAL

### Estados posibles
- ACTIVE
- DISABLED
- ROTATION_REQUIRED *(opcional, más adelante)*

### Invariantes
- `ClientId` debe ser único dentro del tenant,
- una app public no debe requerir client secret para Authorization Code + PKCE,
- una app confidential sí puede usar secret y client credentials,
- una redirect URI utilizada en authorize debe coincidir exactamente con una URI registrada,
- una app solo puede usar grants explícitamente habilitados.

---

## 4.3. User

### Descripción
Representa la identidad humana de un usuario dentro de un tenant.

### Responsabilidades
- autenticarse,
- mantener credenciales propias,
- participar en una o varias aplicaciones del tenant,
- mantener estado de cuenta.

### Atributos conceptuales
- UserId
- TenantId
- Email
- Username *(opcional según estrategia)*
- DisplayName
- PasswordHash
- Status
- RecoveryState *(opcional)*

### Estados posibles
- ACTIVE
- INVITED
- LOCKED *(opcional en MVP)*
- SUSPENDED
- DELETED *(lógico, si se usa soft delete)*

### Invariantes
- un usuario existe una sola vez por tenant,
- email o username deben ser únicos dentro del tenant según la política elegida,
- un usuario suspendido no puede autenticarse,
- la credencial local nunca se almacena en texto plano.

---

## 4.4. Membership

### Descripción
Representa la relación entre un User y una ClientApp dentro del mismo tenant.

### Responsabilidades
- indicar si el usuario puede acceder a esa app,
- almacenar el estado de participación del usuario en la app,
- servir como ancla para roles por app.

### Atributos conceptuales
- MembershipId
- TenantId
- UserId
- ClientAppId
- Status
- CreatedAt

### Estados posibles
- ACTIVE
- INVITED
- SUSPENDED
- REVOKED

### Invariantes
- un usuario no puede tener dos memberships activas para la misma app,
- user y app deben pertenecer al mismo tenant,
- una membership suspendida o revocada no debe habilitar login en esa app,
- el acceso a una app debe evaluarse siempre contra su membership o policy.

---

## 4.5. AppRole

### Descripción
Representa un rol definido dentro de una aplicación específica.

### Responsabilidades
- modelar autorización contextual por app,
- agrupar capacidades funcionales reconocidas por la app consumidora.

### Atributos conceptuales
- AppRoleId
- TenantId
- ClientAppId
- Code
- Name
- Status

### Invariantes
- el código del rol debe ser único dentro de la app,
- un rol siempre pertenece a una app específica,
- no debe existir AppRole fuera del contexto de un tenant y app.

---

## 4.6. MembershipRole

### Descripción
Relación entre Membership y AppRole.

### Responsabilidades
- asignar roles específicos a una membership,
- soportar múltiples roles por usuario dentro de una misma app.

### Invariantes
- una membership solo puede recibir roles de su propia app,
- no debe haber duplicados exactos membership-role.

---

## 4.7. AuthorizationCode

### Descripción
Artefacto temporal emitido durante Authorization Code Flow.

### Responsabilidades
- encapsular el resultado de una autenticación exitosa previa al token exchange,
- amarrar la transacción a tenant, app, user y PKCE.

### Atributos conceptuales
- AuthorizationCodeId
- TenantId
- ClientAppId
- UserId
- RedirectUri
- ScopeSet
- CodeChallenge
- CodeChallengeMethod
- ExpiresAt
- Status

### Estados posibles
- ACTIVE
- CONSUMED
- EXPIRED
- REVOKED

### Invariantes
- solo puede consumirse una vez,
- debe expirar rápidamente,
- debe corresponder al mismo client que canjea el token,
- debe validarse contra PKCE cuando aplique.

---

## 4.8. RefreshToken

### Descripción
Artefacto para prolongar una sesión sin reingresar credenciales.

### Responsabilidades
- renovar access tokens,
- soportar rotación y revocación,
- representar continuidad de sesión.

### Atributos conceptuales
- RefreshTokenId
- TenantId
- ClientAppId
- UserId *(nullable en flujos M2M si decides soportarlo así; para MVP mejor solo user-bound)*
- TokenHash
- ExpiresAt
- Status
- RotatedFrom *(opcional)*

### Estados posibles
- ACTIVE
- USED
- REVOKED
- EXPIRED

### Invariantes
- nunca se almacena en claro,
- debe poder revocarse,
- al rotarse, el token anterior deja de ser válido,
- debe estar amarrado al contexto correcto de tenant y client.

---

## 4.9. Session

### Descripción
Representa una sesión activa o histórica de un usuario autenticado.

### Responsabilidades
- trazabilidad de sesiones,
- soporte a logout y cierre de sesiones,
- base para gestión de sesiones por usuario.

### Atributos conceptuales
- SessionId
- TenantId
- UserId
- ClientAppId
- CreatedAt
- LastSeenAt
- Status
- DeviceInfo *(opcional)*
- IpAddress *(opcional)*

### Estados posibles
- ACTIVE
- TERMINATED
- EXPIRED

### Invariantes
- una sesión terminada no puede emitir nuevos tokens,
- la sesión debe ser consistente con user, app y tenant.

---

## 4.10. SigningKey

### Descripción
Representa una clave usada para firmar tokens.

### Responsabilidades
- permitir firma JWT,
- permitir rotación de claves,
- sostener exposición JWKS.

### Atributos conceptuales
- SigningKeyId
- KeyId (`kid`)
- Status
- Algorithm
- PublicMaterial
- PrivateMaterialReference
- ActivatedAt
- RetiredAt

### Estados posibles
- ACTIVE
- RETIRED
- REVOKED

### Invariantes
- debe existir a lo menos una clave activa para emitir tokens,
- una clave retirada puede permanecer publicada en JWKS mientras existan tokens válidos firmados con ella,
- el material privado no debe exponerse fuera de infraestructura segura.

---

## 4.11. PlatformActor

### Descripción
Representa a un operador de plataforma Key-go con privilegios globales.

### Nota
En implementación práctica, este actor puede seguir siendo un `User` dentro del tenant interno de Key-go, complementado con roles de plataforma.

### Responsabilidades
- operar el SaaS,
- administrar tenants,
- ejecutar soporte,
- acceder a control plane.

### Invariantes
- los privilegios de plataforma no deben derivarse de hardcodear un tenant especial,
- el acceso de plataforma debe ser auditable.

---

## 5. Value Objects sugeridos

## 5.1. TenantSlug
- representación segura del slug del tenant
- formato válido para subdominio

## 5.2. EmailAddress
- validación semántica básica
- normalización si se define política

## 5.3. Username
- opcional según estrategia de login

## 5.4. ClientId
- identificador público del client

## 5.5. RedirectUri
- encapsula validación y comparación exacta

## 5.6. ScopeSet
- conjunto de scopes solicitados o permitidos

## 5.7. RoleCode
- código estable del rol

## 5.8. SecretHash
- representación del hash de secret

## 5.9. PasswordHash
- representación del hash de contraseña

## 5.10. AccessPolicy
- CLOSED_APP
- OPEN_JOIN
- SELF_SIGNUP

---

## 6. Relaciones principales del dominio

### Tenant → ClientApp
Un tenant tiene muchas apps.

### Tenant → User
Un tenant tiene muchos usuarios.

### Tenant → Membership
Toda membership pertenece a un tenant.

### User ↔ ClientApp
La relación es **muchos a muchos** y se materializa mediante **Membership**.

### ClientApp → AppRole
Una app puede tener múltiples roles.

### Membership ↔ AppRole
Una membership puede tener múltiples roles por medio de MembershipRole.

### User / ClientApp / Tenant → AuthorizationCode / RefreshToken / Session
Los artefactos de autenticación siempre deben poder trazarse hacia tenant, app y, cuando corresponda, usuario.

---

## 7. Reglas de negocio centrales

## 7.1. Regla de unicidad de identidad
Un usuario no debe duplicarse dentro del mismo tenant si ya existe la identidad base reconocida por la política del sistema.

## 7.2. Regla de acceso por app
Autenticarse correctamente no implica automáticamente poder entrar a cualquier app del tenant.

El acceso final depende de:
- existencia de membership,
- estado de membership,
- y política de acceso de la app.

## 7.3. Regla de pertenencia consistente
User, ClientApp, Membership, Roles y Tokens deben pertenecer al mismo tenant contextual.

## 7.4. Regla de credenciales seguras
Ni passwords ni refresh tokens ni client secrets deben persistirse en texto plano.

## 7.5. Regla de redirect URI estricta
Una redirect URI usada en authorize o token exchange debe coincidir exactamente con una URI registrada para el client.

## 7.6. Regla de consumo único
AuthorizationCode solo puede consumirse una vez.

## 7.7. Regla de rotación de refresh token
Si se aplica rotación, un refresh token usado correctamente debe invalidar el anterior y generar uno nuevo.

## 7.8. Regla de suspensión
Si el tenant está suspendido, no deben completarse flujos operativos normales de autenticación.

## 7.9. Regla de separación de identidades
ClientApp y User son conceptos de dominio distintos y nunca deben colapsarse en la misma entidad conceptual.

---

## 8. Políticas de acceso a aplicación

## 8.1. Closed App
Solo usuarios con membership existente pueden entrar.

### Implicación
La autenticación puede ser exitosa pero la autorización de acceso a la app puede fallar.

## 8.2. Open Join
Si el usuario pertenece al tenant y la app lo permite, se crea membership automáticamente al primer acceso.

## 8.3. Self Signup
Si la app lo permite y el tenant también, el flujo puede crear nuevo usuario + membership.

### Recomendación MVP
- policy default: Closed App
- policy opcional por app: Open Join

---

## 9. Reglas de emisión de claims

Cuando se emite un token para un usuario autenticado en una app, las claims deben reflejar el contexto de esa app.

### Claims mínimas conceptuales
- issuer
- subject
- tenant id
- client id
- roles de membership
- scopes efectivos

### Regla importante
Los roles deben derivarse de la membership del usuario respecto de la app actual, no de roles globales ambiguos.

---

## 10. Roles administrativos

## 10.1. Platform Roles
Representan permisos sobre la plataforma completa.

Ejemplos:
- PLATFORM_OWNER
- PLATFORM_ADMIN
- PLATFORM_SUPPORT
- PLATFORM_READONLY

## 10.2. Tenant Roles
Representan permisos administrativos dentro de un tenant.

Ejemplos:
- TENANT_OWNER
- TENANT_ADMIN
- TENANT_MANAGER
- TENANT_READONLY

## 10.3. App Roles
Representan permisos funcionales dentro de una app específica.

Ejemplos:
- ADMIN
- USER
- VIEWER

### Regla conceptual
No deben mezclarse roles de plataforma, tenant y app como si fueran el mismo nivel de responsabilidad.

---

## 11. Soporte y acceso operacional

## 11.1. Dogfooding
El equipo de Key-go debe poder ser un tenant real del sistema.

## 11.2. Soporte
El acceso de soporte debe ser:
- explícito,
- temporal,
- auditable,
- y restringido por rol.

## 11.3. Invariante de soporte
Nunca debe existir acceso silencioso a tenants de clientes sin trazabilidad verificable.

---

## 12. Agregados recomendados

Aunque la implementación puede variar, conceptualmente estos agregados tienen sentido:

### Agregado Tenant
Raíz:
- Tenant

### Agregado ClientApp
Raíz:
- ClientApp
Incluye:
- RedirectUris
- AllowedGrants
- AllowedScopes
- AccessPolicy

### Agregado User
Raíz:
- User

### Agregado Membership
Raíz:
- Membership
Incluye:
- MembershipRoles

### Agregado Authorization Transaction
Raíz:
- AuthorizationCode

### Agregado Session
Raíz:
- Session
Incluye:
- RefreshTokens asociados, si decides modelarlo así

---

## 13. Eventos de dominio sugeridos

### Tenant
- TenantCreated
- TenantSuspended
- TenantReactivated

### ClientApp
- ClientAppCreated
- ClientSecretRotated
- RedirectUriAdded
- RedirectUriRemoved

### User
- UserCreated
- UserInvited
- UserSuspended
- PasswordChanged
- PasswordResetRequested
- PasswordResetCompleted

### Membership
- MembershipCreated
- MembershipActivated
- MembershipSuspended
- MembershipRevoked
- AppRoleAssignedToMembership
- AppRoleRemovedFromMembership

### Auth
- LoginSucceeded
- LoginFailed
- AuthorizationCodeIssued
- AuthorizationCodeConsumed
- TokenIssued
- RefreshTokenRotated
- SessionTerminated

### Platform
- SupportAccessGranted
- SupportAccessRevoked

---

## 14. Invariantes críticas para tests

Estas invariantes deberían convertirse en tests de dominio y de integración:

1. no se puede crear membership entre user y app de tenants distintos,
2. no se puede consumir un authorization code dos veces,
3. no se puede usar redirect URI no registrada,
4. un usuario suspendido no puede autenticarse,
5. un tenant suspendido no puede autenticar usuarios normalmente,
6. una membership suspendida no habilita acceso a la app,
7. una app public no usa secret en flujos donde PKCE resuelve autenticidad del cliente,
8. un refresh token rotado deja inutilizable al anterior,
9. los roles emitidos en el token corresponden a la app actual,
10. client apps y users no comparten el mismo modelo conceptual.

---

## 15. Decisiones de modelado ya consolidadas

A partir de las decisiones previas del proyecto, el dominio de Key-go ya da por sentado lo siguiente:

- Key-go es un SaaS IAM multi-tenant.
- El modelo correcto es estándar OAuth2/OIDC.
- El tenant se resuelve por subdominio.
- Un usuario existe una sola vez por tenant.
- El acceso a apps se modela por membership.
- Los roles son contextuales por app.
- User y ClientApp son entidades separadas.
- El equipo de Key-go también opera como tenant interno del sistema.
- Existe control plane y tenant plane como niveles distintos.

---

## 16. Próximo artefacto recomendado

Con este modelo de dominio cerrado, el siguiente documento lógico es:

# `KEYGO_SERVER_API_SURFACE.md`

Debe incluir:
- endpoints definitivos,
- payloads request/response,
- errores esperados,
- seguridad por endpoint,
- headers/contexto de tenant,
- y separación entre auth plane, tenant plane y control plane.

La secuencia correcta queda así:

**Architecture → Backlog → Domain Model → API Surface → implementación.**

