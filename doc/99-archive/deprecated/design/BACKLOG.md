# BACKLOG (Archived)

⚠️ **This document is archived and no longer maintained.**

This was an early roadmap document (pre-Sprint structure). For current planning, see:
- [`../../ROADMAP.md`](../../ROADMAP.md) — Current feature roadmap
- [`../../design/RFC_CLOSURE_PROCESS.md`](../../design/RFC_CLOSURE_PROCESS.md) — How decisions are made
- Sprint completion documents: SPRINT_1_KICKOFF.md, SPRINT_3_COMPLETION.md, SPRINT_4_COMPLETION.md

Archived in: [`../../archive/deprecated/`](../../archive/deprecated/)

Las prioridades del backlog siguen estas reglas:

- **P0**: bloquea el producto; sin esto no hay MVP usable.
- **P1**: hace vendible el MVP.
- **P2**: mejora operación, seguridad o experiencia, pero puede esperar.

---

## 3. Épicas del MVP

## ÉPICA 1. Fundación del proyecto

### Objetivo
Dejar una base técnica limpia, testeable y preparada para crecer.

### Historias / tareas

#### P0 — E1-H1: Crear estructura base del repositorio
- Definir repo `keygo-auth-server`
- Crear estructura modular inicial
- Configurar build
- Definir convenciones de paquetes

**DoD**
- El proyecto compila
- Existe estructura base alineada a Clean Architecture
- Se puede ejecutar localmente

#### P0 — E1-H2: Configurar estándares de calidad
- Lint / format
- Convenciones de commits
- Reglas básicas de calidad
- Pipeline CI inicial

**DoD**
- Cada PR ejecuta build y tests
- Formato homogéneo

#### P0 — E1-H3: Configurar persistencia base
- PostgreSQL
- migraciones versionadas
- configuración por ambiente
- Testcontainers para integración

**DoD**
- La app levanta contra Postgres
- Las migraciones corren correctamente
- Existen tests de integración básicos

---

## ÉPICA 2. Multitenancy base

### Objetivo
Incorporar tenant como concepto transversal desde el inicio.

### Historias / tareas

#### P0 — E2-H1: Implementar modelo `Tenant`
- entidad de dominio
- persistencia
- estado del tenant
- unicidad por slug

**DoD**
- Se puede crear y consultar tenants
- `slug` único validado

#### P0 — E2-H2: Implementar resolución de tenant
- resolver tenant por subdominio o estrategia equivalente de desarrollo
- propagar contexto de tenant a la request
- validar tenant activo

**DoD**
- Toda request operativa queda ligada a un tenant
- Requests con tenant inválido son rechazadas

#### P0 — E2-H3: Asegurar aislamiento lógico
- agregar `tenant_id` en entidades relevantes
- índices y restricciones por tenant
- validaciones de consistencia

**DoD**
- No existe lectura/escritura cruzada entre tenants
- Tests cubren aislamiento básico

---

## ÉPICA 3. Gestión de aplicaciones cliente

### Objetivo
Permitir que un tenant registre apps que usarán Key-go.

### Historias / tareas

#### P0 — E3-H1: Implementar modelo `ClientApp`
- client app de dominio
- `client_id`
- `client_secret_hash`
- tipo `PUBLIC` o `CONFIDENTIAL`
- estado

**DoD**
- Se pueden crear apps por tenant
- Se respeta unicidad `(tenant_id, client_id)`

#### P0 — E3-H2: Gestionar redirect URIs
- alta y baja de URIs
- validaciones de formato
- coincidencia exacta en authorize

**DoD**
- Una app puede registrar varias redirect URIs
- Sólo URIs válidas pueden usarse

#### P1 — E3-H3: Gestionar grants y scopes permitidos
- Auth Code + PKCE
- Client Credentials
- scopes configurables

**DoD**
- Cada client conoce qué grants/scopes puede usar

#### P1 — E3-H4: Rotación de client secret
- generación de nuevo secret
- mostrar secret una sola vez
- invalidación del anterior según política

**DoD**
- El tenant admin puede rotar el secret
- El secret nunca queda expuesto en texto plano persistido

---

## ÉPICA 4. Identidad de usuarios

### Objetivo
Gestionar usuarios únicos por tenant.

### Historias / tareas

#### P0 — E4-H1: Implementar modelo `User`
- email y/o username
- password hash
- status
- datos básicos

**DoD**
- Se pueden crear usuarios por tenant
- Se valida unicidad por tenant

#### P1 — E4-H2: Crear usuario desde admin
- endpoint admin para alta de usuario
- validaciones mínimas

**DoD**
- Tenant admin crea usuarios correctamente

#### P1 — E4-H3: Desactivar usuario
- suspensión lógica
- impedir login si está desactivado

**DoD**
- Usuario desactivado no puede autenticarse

#### P1 — E4-H4: Reset de contraseña
- generación de token o mecanismo equivalente
- endpoint para reset

**DoD**
- Se puede resetear contraseña de forma segura

---

## ÉPICA 5. Memberships y roles por app

### Objetivo
Permitir que un mismo usuario use múltiples apps del tenant con acceso controlado por app.

### Historias / tareas

#### P0 — E5-H1: Implementar modelo `Membership`
- relación user ↔ app
- estado de membership
- unicidad por app y usuario

**DoD**
- Un usuario puede pertenecer a varias apps del tenant
- No se duplica membership para la misma app

#### P1 — E5-H2: Asignar usuario a app
- endpoint admin para crear membership
- validaciones por tenant

**DoD**
- Tenant admin puede suscribir usuarios a apps

#### P1 — E5-H3: Quitar usuario de app
- baja lógica o eliminación según política

**DoD**
- El usuario deja de poder autenticarse para esa app

#### P1 — E5-H4: Implementar `AppRole` y `MembershipRole`
- roles locales por app
- asignación de roles a membership

**DoD**
- Una membership puede tener uno o varios roles
- Roles se emiten correctamente en tokens

#### P1 — E5-H5: Política de acceso por app
- closed app
- open join
- soporte mínimo configurable

**DoD**
- Al autenticarse contra una app, Key-go aplica la policy definida

---

## ÉPICA 6. Hosted Login y autenticación de usuario

### Objetivo
Implementar el flujo principal de login de usuario final usando OAuth2/OIDC.

### Historias / tareas

#### P0 — E6-H1: Endpoint `/oauth2/authorize`
- validar tenant
- validar client
- validar redirect URI
- validar response type y scopes
- persistir contexto de autorización

**DoD**
- Una app puede iniciar el flujo correctamente
- Requests inválidas son rechazadas con error consistente

#### P0 — E6-H2: Pantalla Hosted Login integrada al flujo
- render login page
- conservar contexto de autorización
- manejo de errores de credenciales

**DoD**
- El usuario puede autenticarse desde la UI central de Key-go

#### P0 — E6-H3: Autenticación de credenciales
- validar usuario
- validar password hash
- validar estado del usuario
- validar acceso a la app según membership/policy

**DoD**
- Sólo usuarios válidos y autorizados completan login

#### P0 — E6-H4: Emisión de authorization code
- code temporal con expiración corta
- amarrado a tenant, user, client y PKCE challenge

**DoD**
- El code se genera y redirige correctamente a la app

#### P0 — E6-H5: Endpoint `/oauth2/token`
- canje de authorization code
- validación PKCE
- emisión de access token / id token / refresh token

**DoD**
- El token endpoint funciona para Auth Code + PKCE

---

## ÉPICA 7. Machine-to-machine

### Objetivo
Permitir autenticación de aplicaciones sin usuario final.

### Historias / tareas

#### P1 — E7-H1: Soporte a `client_credentials`
- autenticación de client confidential
- emisión de access token técnico

**DoD**
- Integraciones M2M pueden obtener token válido

#### P1 — E7-H2: Claims técnicas por client
- `iss`, `tid`, `cid`, scopes técnicos

**DoD**
- Los tokens M2M incluyen claims consistentes

---

## ÉPICA 8. Tokens, claves y seguridad criptográfica

### Objetivo
Emitir tokens seguros y verificables externamente.

### Historias / tareas

#### P0 — E8-H1: Implementar signing keys
- clave activa
- historial de claves
- soporte a rotación futura

**DoD**
- El sistema firma JWT con clave activa

#### P0 — E8-H2: Exponer JWKS
- endpoint `/.well-known/jwks.json`

**DoD**
- Un consumidor externo puede validar firmas con JWKS

#### P1 — E8-H3: Implementar `openid-configuration`
- issuer
- authorize endpoint
- token endpoint
- jwks uri
- userinfo endpoint

**DoD**
- Se expone metadata mínima OIDC por tenant

#### P1 — E8-H4: Refresh tokens con hash y rotación
- hash persistido
- renovación segura
- revocación

**DoD**
- Refresh token nunca se guarda en plano
- Se invalida/reemplaza correctamente

---

## ÉPICA 9. UserInfo y claims

### Objetivo
Permitir que las apps recuperen información básica del usuario autenticado.

### Historias / tareas

#### P1 — E9-H1: Endpoint `/userinfo`
- validar access token
- devolver claims básicas

**DoD**
- El endpoint responde con datos coherentes del usuario

#### P1 — E9-H2: Claims por app
- `roles`
- `scp`
- `cid`
- `tid`

**DoD**
- Las claims reflejan correctamente membership y roles de la app actual

---

## ÉPICA 10. Tenant Admin API

### Objetivo
Dar al admin del tenant capacidad real de gestionar su identidad y sus apps.

### Historias / tareas

#### P1 — E10-H1: CRUD de apps
#### P1 — E10-H2: CRUD de usuarios
#### P1 — E10-H3: CRUD de memberships
#### P1 — E10-H4: Asignación de roles por app
#### P1 — E10-H5: Auditoría básica del tenant

**DoD global**
- El tenant admin puede operar su ecosistema sin intervención manual del equipo Key-go

---

## ÉPICA 11. Control plane / platform admin

### Objetivo
Permitir operación del SaaS por parte del equipo Key-go.

### Historias / tareas

#### P1 — E11-H1: Crear y suspender tenants
#### P1 — E11-H2: Consultar tenants
#### P1 — E11-H3: Auditoría global
#### P2 — E11-H4: Soporte read-only a tenants

**DoD global**
- La plataforma puede administrar el SaaS sin hardcodear excepciones

---

## ÉPICA 12. Self-service del usuario final

### Objetivo
Reducir dependencia de cada app cliente para funciones básicas de identidad.

### Historias / tareas

#### P1 — E12-H1: Forgot password
#### P1 — E12-H2: Reset password
#### P1 — E12-H3: Change password
#### P2 — E12-H4: Ver sesiones activas
#### P2 — E12-H5: Cerrar sesiones activas

**DoD global**
- El usuario final puede gestionar lo esencial de su identidad sin recurrir al tenant admin

---

## ÉPICA 13. Auditoría y observabilidad

### Objetivo
Dejar trazabilidad suficiente para seguridad, soporte y operación.

### Historias / tareas

#### P0 — E13-H1: Auditoría de eventos críticos
Eventos mínimos:
- login exitoso
- login fallido
- token emitido
- refresh realizado
- usuario desactivado
- app creada
- secret rotado
- membership creada/eliminada

**DoD**
- Existe registro consistente por evento crítico

#### P1 — E13-H2: Logs estructurados
#### P1 — E13-H3: Métricas básicas
#### P2 — E13-H4: Alertas operativas

---

## ÉPICA 14. Seguridad operacional

### Objetivo
Evitar que el MVP nazca débil.

### Historias / tareas

#### P0 — E14-H1: Hashing seguro de passwords
#### P0 — E14-H2: Hashing seguro de refresh tokens
#### P1 — E14-H3: Rate limiting para login y token endpoint
#### P1 — E14-H4: Protección contra replay básico en flujos críticos
#### P1 — E14-H5: Validación estricta de redirect URIs

**DoD global**
- Los principales vectores obvios quedan cubiertos desde v1

---

## 4. Orden recomendado de implementación

## Fase 0 — Fundación
- Épica 1
- Épica 2

## Fase 1 — Core IAM usable
- Épica 3
- Épica 4
- Épica 5
- Épica 6
- Épica 8

## Fase 2 — Vendible para terceros
- Épica 7
- Épica 9
- Épica 10
- Épica 12

## Fase 3 — Operación real del SaaS
- Épica 11
- Épica 13
- Épica 14

---

## 5. Corte recomendado de MVP real

Si necesitas definir un primer corte vendible y no sobredimensionado, el MVP mínimo debería incluir:

### Obligatorio
- tenants
- client apps
- users
- memberships
- Auth Code + PKCE
- token endpoint
- JWT + JWKS
- refresh token
- tenant admin básico
- hosted login
- password reset
- auditoría mínima

### Puede quedar para siguiente iteración
- userinfo completo
- client credentials
- support access
- sesiones visibles al usuario
- auditoría avanzada

---

## 6. Riesgos principales del proyecto

### Riesgo 1: contaminar el dominio con Spring/JPA
Mitigación: mantener casos de uso y entidades fuera de infraestructura.

### Riesgo 2: romper aislamiento entre tenants
Mitigación: tenant context obligatorio + tests de aislamiento.

### Riesgo 3: sobrecomplicar el MVP con features enterprise
Mitigación: dejar MFA, SCIM, SAML y ABAC fuera de v1.

### Riesgo 4: construir login fuera del estándar
Mitigación: usar OAuth2/OIDC desde el inicio.

### Riesgo 5: mezclar app y usuario como misma entidad
Mitigación: mantener `User` y `ClientApp` separados.

---

## 7. Definition of Done global del MVP

El MVP de Key-go Server estará realmente terminado cuando:

- un tenant pueda registrarse o ser creado,
- ese tenant pueda crear una app cliente,
- esa app pueda iniciar un login OIDC/OAuth2,
- un usuario del tenant pueda autenticarse,
- el sistema emita tokens válidos,
- el acceso a una app se controle por membership,
- los roles por app viajen en claims,
- el tenant admin pueda administrar usuarios y apps,
- y exista trazabilidad mínima de seguridad y operación.

---

## 8. Siguiente entregable recomendado

Una vez aprobado este backlog, el siguiente artefacto lógico es uno de estos dos:

1. **`KEYGO_SERVER_DOMAIN_MODEL.md`**
   - entidades
   - relaciones
   - invariantes
   - reglas de negocio

2. **`KEYGO_SERVER_API_SURFACE.md`**
   - contratos REST
   - payloads
   - errores
   - seguridad por endpoint

La mejor secuencia técnica sería:

**Backlog → Domain Model → API Surface → implementación.**

