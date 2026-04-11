# Provisioning Strategy — Aprovisionamiento de Usuarios y Roles

**Propósito:** Documentar estrategias de provisioning (manual, SCIM, directorios) y cómo KeyGo escala desde manual a automatizado.

---

## Estado Actual: Manual API

**Hoy:** Usuarios se crean vía API REST con credenciales.

```
PUT /api/v1/tenants/{tenantSlug}/users
POST /api/v1/tenants/{tenantSlug}/users/{userId}/reset-password
```

**Flujo:**
1. Admin crea usuario en consola
2. Sistema genera contraseña temporal
3. Admin envía email con link (sin automatizar)
4. Usuario cambia contraseña en login

**Limitaciones:**
- No hay integración con directorios (Okta, Azure AD, Google Workspace)
- No hay SCIM 2.0 (estándar empresarial)
- Escalabilidad manual: 1000 usuarios = 1000 requests

---

## Niveles de Provisioning

### Nivel 1: Manual API (Actual)

**Caso de uso:** Startups, equipos pequeños.

**Flujo:**
```
Admin consola
  ↓
POST /api/v1/tenants/{slug}/users (nombre, email)
  ↓
Sistema genera ID, contraseña temporal
  ↓
Admin comparte credenciales
  ↓
Usuario autentica, cambia contraseña
```

**Endpoints:**
- `POST /api/v1/tenants/{tenantSlug}/users` — Crear usuario
- `POST /api/v1/tenants/{tenantSlug}/users/{userId}/reset-password` — Reset password
- `POST /api/v1/tenants/{tenantSlug}/memberships` — Asignar roles

---

### Nivel 2: SCIM 2.0 (Roadmap)

**Caso de uso:** Empresas con Active Directory, Okta, Azure AD.

**Estándar:** System for Cross-domain Identity Management (RFC 7643/7644)

**Flujo:**
```
Directory (Okta/Azure)
  ↓ (webhooks/polling)
SCIM Provider (KeyGo)
  ↓
POST /scim/v2/Users (standard SCIM request)
  ↓
Sistema crea/actualiza/suspende usuario automáticamente
```

**Endpoints SCIM (futuros):**
```
GET    /scim/v2/Users              — List users
GET    /scim/v2/Users/{id}         — Get user
POST   /scim/v2/Users              — Create user
PUT    /scim/v2/Users/{id}         — Update user
DELETE /scim/v2/Users/{id}         — Delete (soft-delete)
GET    /scim/v2/Groups             — List groups (roles)
POST   /scim/v2/Groups             — Create group
```

**SCIM Request Example:**
```json
POST /scim/v2/Users
Authorization: Bearer scim-token

{
  "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
  "userName": "john.doe@example.com",
  "name": {
    "givenName": "John",
    "familyName": "Doe"
  },
  "emails": [
    {
      "value": "john.doe@example.com",
      "type": "work",
      "primary": true
    }
  ],
  "active": true,
  "externalId": "okta-user-123"
}
```

**SCIM Response:**
```json
{
  "schemas": ["urn:ietf:params:scim:schemas:core:2.0:User"],
  "id": "user-uuid",
  "userName": "john.doe@example.com",
  "name": {
    "givenName": "John",
    "familyName": "Doe"
  },
  "active": true,
  "externalId": "okta-user-123",
  "meta": {
    "resourceType": "User",
    "created": "2026-04-10T10:00:00Z",
    "lastModified": "2026-04-10T10:00:00Z"
  }
}
```

---

### Nivel 3: Directory Integration (Future)

**Caso de uso:** Sincronizar automáticamente con directorio empresarial.

**Opciones:**
- **Okta** — Webhooks + SCIM
- **Azure AD** — Webhooks + Graph API + SCIM
- **Google Workspace** — Directory API + SCIM
- **Custom LDAP** — Polling + sync

**Flujo:**
```
Directory (Okta)
  ↓ (webhook: user.created)
KeyGo API
  ↓
Provision user (SCIM)
  ↓
Assign default roles (tenant config)
  ↓
Send welcome email
```

---

## Arquitectura Extensible

### Entidad: ExternalIdentity

Para soportar provisioning automático, agregar:

```java
@Entity
public class UserExternalIdentity {
  private UUID userId;
  private String provider;           // "okta", "azure", "google", etc.
  private String externalId;         // "okta-user-123"
  private String externalEmail;      // "john@company.okta.com"
  private Instant lastSyncAt;        // Cuándo fue sincronizado por última vez
  private Map<String, String> metadata;  // Metadata del directorio
}
```

**Propósito:**
- Mapear usuarios internos ↔ externos
- Evitar duplicados (idempotencia)
- Auditar sincronizaciones

### Port: ProvisioningPort

```java
public interface ProvisioningPort {
  
  /**
   * Provision user from SCIM request.
   * @param scimUser SCIM User object
   * @param tenantId tenant context
   * @return provisioned User
   */
  User provisionUser(SCIMUser scimUser, TenantId tenantId);
  
  /**
   * Update user from directory change.
   * @param externalId directory ID
   * @param changes delta (name, status, etc.)
   */
  void syncUserChanges(String externalId, UserChanges changes);
  
  /**
   * Deprovision user (soft-delete or suspend).
   * @param externalId directory ID
   */
  void deprovisionUser(String externalId);
}
```

---

## Estrategia de Asignación de Roles

### Opción A: Default Roles (Actual)

Al crear usuario:
```java
User user = User.builder()
    .username(...)
    .email(...)
    .build();

// Asignar rol default al tenant
TenantUserRole role = TenantUserRole.builder()
    .tenantUser(tenantUser)
    .tenantRole(defaultRole)  // "USER" o similar
    .build();
```

### Opción B: Mapping desde Directory

```json
// Okta profile → KeyGo roles
{
  "mapping": {
    "okta:group:Engineering": "DEVELOPER",
    "okta:group:Management": "ADMIN_ORG",
    "okta:group:Everyone": "USER"
  }
}
```

**Flujo:**
1. SCIM request llega con `groups: ["Engineering", "Management"]`
2. Sistema mapea → roles: ["DEVELOPER", "ADMIN_ORG"]
3. Crea memberships automáticamente

### Opción C: Reconciliation

Periodically:
1. Consultar directorio (webhook fallback)
2. Comparar con BD
3. Sincronizar cambios

---

## Seguridad: SCIM Access Control

### Authentication

```
Authorization: Bearer scim-token
X-SCIM-Version: 2.0
```

**Token:**
- Específico del tenant
- Generado por tenant admin
- Rotable, revocable
- Auditable (quién, cuándo)

### Validation

```java
@PreAuthorize("hasRole('scim-provider')")
@PostMapping("/scim/v2/Users")
public ResponseEntity<SCIMUser> createUser(@Valid @RequestBody SCIMUser scimUser) {
  // Validate:
  // 1. ¿Token válido?
  // 2. ¿Tenant matches?
  // 3. ¿SCIM schema válido?
  // 4. ¿Email único en tenant?
  // 5. ¿Tenant activo?
}
```

### Audit Log

```
{
  "event": "USER_PROVISIONED",
  "provider": "okta",
  "externalId": "okta-user-123",
  "email": "john@company.com",
  "timestamp": "2026-04-10T10:00:00Z",
  "actor": "scim-token-xyz",
  "result": "SUCCESS"
}
```

---

## Roadmap de Implementación

### Sprint 1: Foundation (Current)

✅ Manual API (ya existe)
- `POST /api/v1/tenants/{slug}/users`
- `POST /api/v1/tenants/{slug}/memberships`

### Sprint 2: SCIM Framework (Future)

- [ ] Entidad `UserExternalIdentity`
- [ ] Port `ProvisioningPort`
- [ ] SCIM v2.0 endpoints (`/scim/v2/Users`, etc.)
- [ ] SCIM Request/Response DTO
- [ ] Mapeo: SCIM ↔ Domain Model

### Sprint 3: Directory Connectors (Future)

- [ ] Okta connector (webhook handler)
- [ ] Azure AD connector (Graph API poller)
- [ ] Role mapping engine
- [ ] Reconciliation job

### Sprint 4: Advanced (Future)

- [ ] Just-in-time (JIT) provisioning
- [ ] Group-based role assignment
- [ ] Custom attribute mapping
- [ ] Directory sync status dashboard

---

## Checklist: Nuevo Provisioning Provider

Cuando agregues un nuevo provider de directorio:

- [ ] **¿Qué protocol soporta?** (SCIM, API custom, LDAP, etc.)
- [ ] **¿Webhooks o polling?**
  - Webhook → handler que entienda eventos del provider
  - Polling → job que sincronice periódicamente
- [ ] **¿Qué datos traes?** (nombre, email, groups, custom attrs)
- [ ] **¿Cómo mapeas a roles KeyGo?** (mapping config, default role, etc.)
- [ ] **¿Qué hacer con usuario suspendido?** (soft-delete, suspend status, etc.)
- [ ] **¿Auditoría?** (log provisioning events)
- [ ] **¿Validación de token?** (signature, expiry, revocation)

---

## Anti-Patterns: Evitar

### ❌ Crear usuario duplicado por falta de idempotencia

```java
// MAL: Cada SCIM request = nuevo usuario
POST /scim/v2/Users (externalId: "okta-123")
  → Crea user-1

POST /scim/v2/Users (externalId: "okta-123")  // Retry
  → Crea user-2  ← Duplicado!
```

### ✅ Guardar externalId, validar antes de crear

```java
// BIEN
if (userRepository.findByExternalId("okta-123").isPresent()) {
  // Actualizar existente
  updateUser(...);
} else {
  // Crear nuevo
  createUser(...);
}
```

---

### ❌ Provisionar sin validación

```java
// MAL
@PostMapping("/scim/v2/Users")
public void createUser(@RequestBody SCIMUser user) {
  // Directamente crear sin validar schema, tenant, etc.
}
```

### ✅ Validación exhaustiva

```java
// BIEN
@PostMapping("/scim/v2/Users")
@PreAuthorize("hasRole('scim-provider')")
public ResponseEntity<SCIMUser> createUser(
    @Valid @RequestBody SCIMUser scimUser) {
  
  // 1. Validar SCIM schema
  if (!scimUser.isValid()) {
    return badRequest(INVALID_SCIM_SCHEMA);
  }
  
  // 2. Validar tenant
  Tenant tenant = getTenantFromContext();
  if (!tenant.isActive()) {
    return forbidden(TENANT_SUSPENDED);
  }
  
  // 3. Validar email
  if (userRepository.existsByEmail(scimUser.getEmail())) {
    return conflict(DUPLICATE_RESOURCE);
  }
  
  // 4. Crear user
  User user = provisioningUseCase.execute(scimUser, tenant);
  return ok(toSCIMResponse(user));
}
```

---

## Referencias

| Aspecto | Ubicación |
|---|---|
| **SCIM RFC** | https://tools.ietf.org/html/rfc7643 (spec), https://tools.ietf.org/html/rfc7644 (protocol) |
| **Okta SCIM** | https://developer.okta.com/docs/guides/scim-provisioning-integration/ |
| **Azure AD SCIM** | https://learn.microsoft.com/en-us/azure/active-directory/app-provisioning/use-scim-to-provision-users-and-groups |
| **User Creation** | `keygo-app/src/main/java/io/cmartinezs/keygo/app/user/usecase/CreateUserUseCase.java` |
| **Membership Creation** | `keygo-app/src/main/java/io/cmartinezs/keygo/app/membership/usecase/CreateMembershipUseCase.java` |
| **Endpoints** | `docs/design/api/ENDPOINT_CATALOG.md` |
| **Authorization** | `docs/design/AUTHORIZATION_PATTERNS.md` |

---

**Última actualización:** 2026-04-10  
**Estado:** Completado para Sprint 2  
**Roadmap:** 4 sprints (Actual + SCIM + Connectors + Advanced)

