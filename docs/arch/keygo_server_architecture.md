# KEYGO Server Architecture

## 1. Objetivo

**Key-go Server** es el núcleo backend de un SaaS de autenticación e identidad para pymes y aplicaciones de terceros.

Su propósito es:

- autenticar usuarios finales,
- autenticar aplicaciones cliente,
- emitir y validar tokens,
- centralizar identidad por tenant,
- y administrar acceso por aplicación.

Key-go debe operar como un **Authorization Server OAuth 2.0 / OpenID Connect (OIDC)** con **Hosted Login**, no como un login custom embebido dentro de cada app.

---

## 2. Alcance MVP

El MVP debe enfocarse en resolver de forma sólida y vendible los siguientes puntos:

- multi-tenant desde el inicio,
- login centralizado para usuarios finales,
- registro y administración de aplicaciones cliente,
- emisión de tokens estándar,
- acceso machine-to-machine,
- administración básica de usuarios,
- memberships por aplicación,
- y una base de autorización por roles por app.

### Incluye en MVP

- Authorization Code + PKCE
- Client Credentials
- Refresh Tokens con rotación
- JWT asimétrico + JWKS
- Tenant Console
- Hosted Login
- Self-service básico de usuario
- Control plane lógico para operación de plataforma

### No incluye inicialmente

- MFA
- SAML / SCIM
- ABAC / policy engine avanzado
- billing completo
- workflows complejos de aprobación

---

## 3. Principios de diseño

### 3.1. SaaS real, no framework local
Key-go debe ser consumible por apps externas como un servicio de identidad central.

### 3.2. Multi-tenant desde MVP
El multitenancy no es una mejora futura, es parte del corazón del diseño.

### 3.3. Estándares primero
Se debe usar OAuth2/OIDC como base de interoperabilidad.

### 3.4. Identidades separadas
**User** y **ClientApp** no deben modelarse como la misma entidad con un flag.

### 3.5. Clean Architecture
El dominio no debe depender de Spring, JPA ni detalles de infraestructura.

### 3.6. Modular Monolith primero
Partir con un backend único, modular internamente, y extraer componentes más adelante si el crecimiento lo justifica.

---

## 4. Modelo conceptual

### 4.1. Tenant
Empresa cliente que usa Key-go.

### 4.2. ClientApp
Aplicación registrada dentro de un tenant. Puede ser web, SPA, mobile o backend.

### 4.3. User
Identidad única del usuario dentro del tenant.

### 4.4. Membership
Relación entre un usuario y una app específica del tenant.

### 4.5. AppRole / MembershipRole
Roles que el usuario tiene dentro de una app concreta.

### 4.6. Artefactos de seguridad
- AuthorizationCode
- RefreshToken
- Session
- SigningKey

---

## 5. Regla central del sistema

Un usuario existe **una sola vez por tenant**.

Si ese usuario quiere usar varias apps del mismo tenant:

- no se crea una nueva cuenta,
- no se duplican credenciales,
- solo se crea o activa una **membership** para la nueva app.

Esto permite identidad centralizada y acceso distribuido por aplicación.

---

## 6. Estrategia multi-tenant

## 6.1. Estrategia recomendada para MVP

**Single database + shared schema + `tenant_id` en todas las tablas relevantes**.

### Beneficios

- menor complejidad operativa,
- más rapidez para salir a mercado,
- esquema simple de migraciones,
- menor costo de infraestructura.

### Reglas

- unicidad por tenant,
- índices compuestos por tenant,
- validación de tenant en todos los casos de uso,
- separación lógica obligatoria.

### Evolución futura opcional

- RLS en PostgreSQL,
- schema por tenant,
- o database por tenant si algún cliente enterprise lo exige.

---

## 6.2. Resolución de tenant

La resolución de tenant recomendada es por **subdominio**.

Ejemplos:

- `acme.keygo.com`
- `empresa-x.keygo.com`

Esto simplifica:

- branding,
- UX,
- contexto del login,
- y seguridad contextual.

---

## 7. Modelo de datos mínimo

## 7.1. Tenant

Campos sugeridos:

- `tenant_id`
- `slug`
- `name`
- `status`
- `created_at`
- `updated_at`

Restricción principal:

- `slug` único

---

## 7.2. ClientApp

Campos sugeridos:

- `client_app_id`
- `tenant_id`
- `client_id`
- `client_secret_hash` *(solo para confidential clients)*
- `client_type` (`PUBLIC` | `CONFIDENTIAL`)
- `name`
- `status`
- `created_at`
- `updated_at`

Tablas asociadas:

- `client_redirect_uri`
- `client_allowed_grant`
- `client_allowed_scope`

Restricción principal:

- `(tenant_id, client_id)` único

---

## 7.3. User

Campos sugeridos:

- `user_id`
- `tenant_id`
- `email`
- `username` *(opcional si usarás ambos)*
- `password_hash`
- `status`
- `display_name`
- `created_at`
- `updated_at`

Restricciones sugeridas:

- `(tenant_id, email)` único
- `(tenant_id, username)` único si se usa username

---

## 7.4. Membership

Campos sugeridos:

- `membership_id`
- `tenant_id`
- `client_app_id`
- `user_id`
- `status` (`ACTIVE`, `INVITED`, `SUSPENDED`)
- `created_at`

Restricción principal:

- `(tenant_id, client_app_id, user_id)` único

---

## 7.5. AppRole

Campos sugeridos:

- `app_role_id`
- `tenant_id`
- `client_app_id`
- `code`
- `name`
- `status`

Restricción principal:

- `(tenant_id, client_app_id, code)` único

---

## 7.6. MembershipRole

Campos sugeridos:

- `membership_id`
- `app_role_id`

---

## 7.7. Artefactos OAuth/OIDC

### AuthorizationCode
- código temporal con expiración corta
- asociado a tenant, client, usuario, scopes y challenge PKCE

### RefreshToken
- persistido con hash
- rotación obligatoria
- revocable

### Session
- seguimiento de sesión de usuario
- opcionalmente user-agent, ip, timestamps y estado

### SigningKey
- clave activa
- historial de claves previas
- soporte a JWKS

---

## 8. Políticas de acceso a apps

Cuando un usuario ya pertenece al tenant pero intenta acceder a una nueva app, Key-go debe evaluar la policy de acceso de esa app.

### Políticas posibles

#### A. Closed app
Solo pueden entrar usuarios con membership preexistente.

#### B. Open join
Si el usuario pertenece al tenant, se crea membership automáticamente en el primer login.

#### C. Self-signup
La app permite crear usuario + membership al primer acceso.

### Recomendación MVP

- default: **Closed app**
- opcional por app: **Open join**

---

## 9. Flujos de autenticación

## 9.1. Login de usuario final

### Flujo principal recomendado
**Authorization Code + PKCE**

### Secuencia
1. La app inicia `/oauth2/authorize`
2. Key-go valida `client_id`, `redirect_uri`, tenant y scopes
3. Key-go muestra Hosted Login
4. El usuario ingresa credenciales en Key-go
5. Key-go autentica
6. Key-go emite `authorization_code`
7. La app canjea el code en `/oauth2/token`
8. Key-go responde con tokens

### Tokens esperados
- `access_token`
- `id_token` *(si OIDC está habilitado)*
- `refresh_token`

---

## 9.2. Machine-to-machine

### Flujo
**Client Credentials**

### Uso
- integraciones backend-backend
- jobs programados
- automatizaciones sin usuario final

---

## 9.3. Refresh token

Debe existir soporte para:

- sesiones largas,
- renovación de access token,
- revocación,
- rotación de refresh token.

---

## 10. Claims de tokens

El `access_token` JWT debería incluir al menos:

- `iss`: issuer
- `sub`: `user_id`
- `tid`: `tenant_id`
- `cid`: `client_id`
- `roles`: roles del usuario en la app
- `scp`: scopes

Beneficio:

La app consumidora puede resolver autorización local sin consultar a Key-go en cada request.

---

## 11. Casos de uso principales

## 11.1. Casos de uso de plataforma

- Crear tenant
- Suspender tenant
- Reactivar tenant
- Auditar eventos globales
- Gestionar acceso de soporte
- Gestionar configuración global de seguridad

---

## 11.2. Casos de uso de tenant admin

- Crear app cliente
- Editar app cliente
- Configurar redirect URIs
- Rotar secret
- Configurar grants permitidos
- Crear usuario
- Invitar usuario
- Desactivar usuario
- Resetear contraseña
- Asignar usuario a app
- Quitar usuario de app
- Asignar roles a membership
- Consultar auditoría del tenant

---

## 11.3. Casos de uso de usuario final

- Iniciar sesión
- Recuperar contraseña
- Restablecer contraseña
- Cambiar contraseña
- Ver sesiones activas *(opcional MVP)*
- Cerrar sesiones *(opcional MVP)*

---

## 12. Endpoints sugeridos

## 12.1. OAuth2 / OIDC

- `GET /{tenant}/.well-known/openid-configuration`
- `GET /{tenant}/.well-known/jwks.json`
- `GET /{tenant}/oauth2/authorize`
- `POST /{tenant}/oauth2/token`
- `POST /{tenant}/oauth2/revoke`
- `GET /{tenant}/userinfo`

---

## 12.2. Tenant admin

### Apps
- `POST /{tenant}/admin/apps`
- `GET /{tenant}/admin/apps`
- `GET /{tenant}/admin/apps/{appId}`
- `PATCH /{tenant}/admin/apps/{appId}`
- `POST /{tenant}/admin/apps/{appId}/rotate-secret`

### Usuarios
- `POST /{tenant}/admin/users`
- `GET /{tenant}/admin/users`
- `GET /{tenant}/admin/users/{userId}`
- `PATCH /{tenant}/admin/users/{userId}`
- `POST /{tenant}/admin/users/{userId}/reset-password`

### Memberships
- `POST /{tenant}/admin/memberships`
- `GET /{tenant}/admin/memberships`
- `DELETE /{tenant}/admin/memberships/{membershipId}`
- `POST /{tenant}/admin/memberships/{membershipId}/roles`

---

## 12.3. Platform / Control Plane

- `POST /platform/tenants`
- `GET /platform/tenants`
- `GET /platform/tenants/{tenantId}`
- `PATCH /platform/tenants/{tenantId}`
- `GET /platform/audit`
- `POST /platform/support-access`

---

## 13. Arquitectura backend recomendada

## 13.1. Estilo general

**Clean Architecture + Modular Monolith**

### Capas

#### Domain
- entidades
- value objects
- reglas de negocio
- contratos de dominio

#### Application
- casos de uso
- DTOs de entrada/salida
- orquestación de negocio

#### Adapters / Infrastructure
- JPA / persistencia
- hashing
- JWT / signing
- envío de emails
- integración con cache o mensajería si se agrega después

#### Interfaces
- REST controllers
- filtros
- validadores
- mapeos HTTP ↔ aplicación
- OpenAPI

Regla principal:

**El dominio no depende de Spring, JPA ni detalles externos.**

---

## 13.2. Módulos lógicos sugeridos

- `tenant`
- `client-app`
- `user-identity`
- `membership`
- `authorization`
- `token-service`
- `platform-admin`
- `audit`
- `support-access`

---

## 14. Planos del sistema

## 14.1. Tenant Plane
Usado por el admin del cliente.

Responsabilidades:
- apps
- usuarios
- memberships
- roles
- auditoría del tenant

---

## 14.2. Auth Plane
Usado por usuarios finales y por integraciones OAuth/OIDC.

Responsabilidades:
- login
- token exchange
- userinfo
- refresh / revoke

---

## 14.3. Control Plane
Usado por el equipo de Key-go.

Responsabilidades:
- tenants
- soporte
- configuración global
- auditoría global

---

## 15. Roles y administración

## 15.1. Platform roles
- `PLATFORM_OWNER`
- `PLATFORM_ADMIN`
- `PLATFORM_SUPPORT`
- `PLATFORM_READONLY`

---

## 15.2. Tenant roles
- `TENANT_OWNER`
- `TENANT_ADMIN`
- `TENANT_MANAGER`
- `TENANT_READONLY`

---

## 15.3. Roles por app
Modelados vía membership + approle.

Esto permite que el mismo usuario tenga distintos roles según la app.

---

## 16. Soporte y dogfooding

Key-go debe soportar:

### 16.1. Dogfooding
El equipo de Key-go debe ser también un tenant real dentro del sistema.

### 16.2. Soporte seguro
El acceso de soporte debe ser:

- explícito,
- auditado,
- con motivo,
- con expiración,
- y preferentemente read-only por defecto.

No debe existir impersonation silenciosa sin trazabilidad.

---

## 17. Superficies frontend asociadas

## 17.1. Tenant Console
Para admins del tenant:
- apps
- usuarios
- memberships
- roles
- auditoría básica

## 17.2. Auth UI
Para usuarios finales:
- login
- forgot password
- reset password
- change password

## 17.3. Ops UI
Para plataforma:
- tenants
- auditoría global
- soporte

---

## 18. Repositorios sugeridos

### Backend
- `keygo-auth-server`

### Frontend
Opción MVP simple:
- `keygo-web`

Con apps internas:
- `apps/console`
- `apps/auth-ui`
- `apps/ops` *(si decides incluirlo desde temprano)*

Alternativamente, más adelante:
- `keygo-console`
- `keygo-auth-ui`
- `keygo-ops`

---

## 19. Backlog técnico MVP

### Backend core
- tenant resolution por subdominio
- CRUD tenant
- CRUD client apps
- CRUD users
- CRUD memberships
- roles por app
- `/authorize`
- `/token`
- `/jwks`
- `/userinfo`
- password reset
- emisión JWT asimétrica
- refresh token con rotación

### Seguridad
- hashing de passwords
- hashing de refresh tokens
- rate limiting
- auditoría de eventos críticos

### Front
- hosted login
- console admin del tenant
- self-service básico de usuario

---

## 20. Decisiones ejecutivas finales

Key-go Server debe nacer como:

- un Authorization Server OAuth2/OIDC,
- multi-tenant desde MVP,
- con separación entre User y ClientApp,
- con Membership para relacionar usuario y aplicación,
- con roles por app,
- con Hosted Login,
- con Tenant Console,
- con self-service básico,
- y con control plane lógico separado.

Este enfoque permite salir rápido, mantener el diseño limpio y evolucionar sin rehacer el núcleo del sistema.

