# Authorization Patterns — Cómo Controlar Acceso en KeyGo

**Propósito:** Documentar patrones de autorización: JWT claims, roles, @PreAuthorize, validación de tenant.

---

## Principio Fundamental

**Decisión arquitectónica (RFC restructure-multitenant §06):**
> Identidad es global, pertenencia es por tenant, acceso es por app.

**En términos de autorización:**
- **Quién eres:** JWT contiene `sub` (user UUID global)
- **A cuál tenant accedes:** JWT contiene `tenant_slug` (scope)
- **Qué roles tienes:** JWT contiene `roles` (en ese tenant/app)

---

## 1. JWT Claims Structure

### Estructura Canónica

```json
{
  "sub": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",  // User UUID (global)
  "email": "user@example.com",
  "tenant_slug": "my-company",                      // Tenant actual (scope)
  "tenant_id": "t-uuid",
  "roles": [                                        // Roles en ese tenant/app
    "ADMIN_TENANT",
    "USER"
  ],
  "aud": "keygo-console",                          // Target app (audience)
  "iss": "https://keygo.local/api/v1/tenants/my-company",  // Issuer (contiene tenant)
  "exp": 1680000000,
  "iat": 1679996400
}
```

### Ubicación en Token

Para validar claims:
```bash
# 1. Decodificar JWT (sin verificar firma)
echo $token | jq -R 'split(".") | .[1] | @base64d | fromjson'

# 2. Extraer tenant_slug
echo $token | jq -R 'split(".") | .[1] | @base64d | fromjson | .tenant_slug'

# 3. Extraer roles
echo $token | jq -R 'split(".") | .[1] | @base64d | fromjson | .roles'
```

---

## 2. Tres Niveles de RBAC

### Nivel 1: Platform Roles (Global)

**Contexto:** Administración de plataforma, no vinculada a tenant específico.

**Roles (bien-conocidos):**
- `keygo_admin` — Administrador global de plataforma (acceso a todos los tenants)
- `keygo_account_admin` — Admin de cuenta de plataforma (gestión de platform users)
- `keygo_user` — Usuario regular de plataforma (acceso limitado)

**Endpoints:**
```java
@PreAuthorize("hasRole('keygo_admin')")
public ResponseEntity<BaseResponse<DashboardData>> getPlatformDashboard() {
  // Acceso a dashboard global, stats, etc.
}
```

**En JWT (Spring convierte a ROLE_keygo_admin):**
```json
{
  "sub": "...",
  "roles": ["keygo_admin"],
  "aud": "keygo-platform"
}
```

### Nivel 2: Tenant Roles (Organizacional)

**Contexto:** Roles definidos por el tenant, dentro de un tenant específico.

**Características:**
- Cada tenant define sus propios roles (ej: "DEVELOPER", "MANAGER", "VIEWER")
- Código de rol: uppercase, única dentro del tenant (ej: `DEVELOPER`, `ADMIN_ORG`)
- Asignables a usuarios de ese tenant
- Pueden tener jerarquía (rol padre)

**Validación:** Además de validar rol, verificar que `tenant_slug` en JWT coincida con URL.

**Endpoints:**
```java
@PreAuthorize("hasAnyRole('ADMIN_ORG', 'keygo_admin') and " +
    "@tenantAuthorizationEvaluator.hasTenantAccess(authentication)")
public ResponseEntity<BaseResponse<PagedData<UserData>>> listUsers(
    @PathVariable String tenantSlug) {
  // Acceso limitado a usuarios de ese tenant con rol apropiado
}
```

**En JWT:**
```json
{
  "sub": "...",
  "tenant_slug": "my-company",
  "roles": ["ADMIN_ORG"],
  "aud": "keygo-console"
}
```

### Nivel 3: App Roles (Funcional)

**Contexto:** Roles definidos por cada aplicación cliente dentro de un tenant.

**Características:**
- Cada app cliente define sus roles (ej: "EDITOR", "VIEWER", "ADMIN_APP")
- Independientes de otros roles (tenant o platform)
- Usuario puede tener diferentes roles en diferentes apps

**Validación:** El cliente app valida roles en su propio JWT.

**Ejemplo:**
```java
// En cliente app (no en KeyGo)
@PreAuthorize("hasRole('EDITOR')")
public ResponseEntity<PagedData<DocumentData>> listDocuments() {
  // Solo usuarios con rol EDITOR en esta app
}
```

**En JWT (client app específico):**
```json
{
  "sub": "...",
  "tenant_slug": "my-company",
  "app_id": "myapp-uuid",
  "roles": ["EDITOR", "ADMIN_APP"],
  "aud": "myapp"
}
```

---

## 3. Patrón @PreAuthorize: Cómo Escribir Reglas

### Patrón A: Platform Admin (Acceso Global)

```java
@RestController
@RequestMapping("/api/v1/platform")
@PreAuthorize("hasRole('keygo_admin')")  // ← Global, no tenant-scoped
public class PlatformAdminController {
  
  @GetMapping("/dashboard")
  public ResponseEntity<BaseResponse<DashboardData>> getDashboard() {
    // Solo keygo_admin puede acceder
    // No hay validación de tenant_slug
  }
}
```

### Patrón B: Tenant-Scoped Admin

```java
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}/users")
@PreAuthorize("@tenantAuthorizationEvaluator.hasTenantAccess(authentication)")
public class TenantUserController {
  
  @PostMapping
  public ResponseEntity<BaseResponse<UserData>> createUser(
      @PathVariable String tenantSlug,
      @Valid @RequestBody CreateUserRequest request) {
    // 1. ¿keygo_admin? → sí, acceso a cualquier tenant
    // 2. ¿Tenant en token coincide con URL? → sí, acceso
    // Si no → 403 BUSINESS_RULE_VIOLATION
  }
}
```

**Lógica de validación (TenantAuthorizationEvaluator):**
```java
public boolean hasTenantAccess(Authentication authentication) {
  // Paso 1: ¿keygo_admin? → sí, acceso a cualquier tenant
  if (hasAdminRole(authentication)) {
    return true;
  }
  
  // Paso 2: Extraer tenant de URL
  String requestedTenantSlug = resolveTenantSlugFromPath();
  
  // Paso 3: Extraer tenant del token (claim: tenant_slug)
  String tokenTenantSlug = resolveTenantSlugFromClaims(authentication);
  
  // Paso 4: ¿Coinciden?
  return requestedTenantSlug.equalsIgnoreCase(tokenTenantSlug);
}
```

**Nota:** No valida rol específico (ej: "ADMIN_ORG"). Solo valida:
1. ¿keygo_admin? → acceso global
2. ¿Tenant matches? → acceso a ese tenant

El rol específico (ADMIN_ORG, EDITOR, etc.) lo valida la lógica de use case.

### Patrón C: Roles Tenant-Scoped con Validación

```java
@PreAuthorize("@tenantAuthorizationEvaluator.hasTenantAccess(authentication)")
@PostMapping("/{tenantSlug}/users")
public ResponseEntity<BaseResponse<UserData>> createUser(
    @PathVariable String tenantSlug,
    @Valid @RequestBody CreateUserRequest request) {
  
  // Spring Security valida: tenant_slug en token matches URL
  
  // Use case valida rol específico:
  User user = createUserUseCase.execute(
    command, 
    authentication  // ← Incluir auth para validar rol en use case
  );
}

// En use case:
public User execute(CreateUserCommand cmd, Authentication auth) {
  // Validación de dominio: rol específico requerido
  List<String> userRoles = extractRolesFromAuth(auth);
  if (!userRoles.contains("ADMIN_ORG") && 
      !userRoles.contains("keygo_admin")) {
    throw new InsufficientPrivilegesException();
  }
  
  // Crear usuario...
}
```

### Patrón D: Roles Específicos por App

```java
// Usuario con rol "EDITOR" en app "myapp"
// JWT contiene: roles: ["EDITOR"], app_id: "myapp-uuid"

@PreAuthorize("hasRole('EDITOR')")
public ResponseEntity<...> editDocument(...) {
  // Cliente app valida rol específico
}
```

### Patrón E: Dinámico con Bean

```java
@PreAuthorize("@customAuthEvaluator.canManageTenantUsers(authentication, #tenantSlug)")
public ResponseEntity<...> createUser(@PathVariable String tenantSlug) {
  // Delega a bean custom para lógica compleja
}

// Implementación:
@Component
public class CustomAuthEvaluator {
  
  public boolean canManageTenantUsers(Authentication auth, String tenantSlug) {
    // 1. ¿keygo_admin? → sí
    if (hasRole(auth, "keygo_admin")) {
      return true;
    }
    
    // 2. ¿Tenant matches y tiene rol ADMIN_ORG?
    if (tenantMatches(auth, tenantSlug) && 
        hasRole(auth, "ADMIN_ORG")) {
      return true;
    }
    
    return false;
  }
}
```

---

## 4. Validation: Tenant Scope

### Cómo Funciona

**Entrada:**
```
GET /api/v1/tenants/my-company/users
Authorization: Bearer eyJ...  (contiene tenant_slug: "my-company")
```

**TenantAuthorizationEvaluator:**
1. Extrae `tenantSlug` de URL → "my-company"
2. Extrae `tenant_slug` del JWT → "my-company"
3. Compara: "my-company".equalsIgnoreCase("my-company") → ✓

**Salida:** Acceso permitido

---

**Entrada:**
```
GET /api/v1/tenants/other-company/users
Authorization: Bearer eyJ...  (contiene tenant_slug: "my-company")
```

**TenantAuthorizationEvaluator:**
1. Extrae `tenantSlug` de URL → "other-company"
2. Extrae `tenant_slug` del JWT → "my-company"
3. Compara: "other-company".equalsIgnoreCase("my-company") → ✗

**Salida:** 403 BUSINESS_RULE_VIOLATION

---

### Extracción de Tenant del Token

```java
private String resolveTenantSlugFromClaims(Authentication authentication) {
  Object principal = authentication.getPrincipal();
  
  // Estrategia 1: Claim explícito "tenant_slug"
  if (principal contains "tenant_slug") {
    return claims.get("tenant_slug");
  }
  
  // Estrategia 2: Extraer de "iss" (issuer)
  // iss = "https://keygo.local/api/v1/tenants/my-company/..."
  // → extrae "my-company"
  String issuer = claims.get("iss");
  return extractTenantFromIssuer(issuer);
}
```

---

## 5. Anti-Patterns: Evitar

### ❌ No validar tenant en endpoint tenant-scoped

```java
// MAL
@PreAuthorize("hasRole('ADMIN_TENANT')")  // Solo checks role
public ResponseEntity<...> getUser(@PathVariable String tenantSlug) {
  // Usuario de tenant A puede acceder a usuario de tenant B
}

// BIEN
@PreAuthorize("hasRole('ADMIN_TENANT') and " +
    "@tenantAuthorizationEvaluator.hasTenantAccess(authentication)")
public ResponseEntity<...> getUser(@PathVariable String tenantSlug) {
  // Valida tenant
}
```

---

### ❌ Confiar en roles de JWT sin validación

```java
// MAL
if (authentication.getAuthorities().contains("ADMIN")) {
  // Confiar en JWT sin validar firma
}

// BIEN (Spring Security valida firma automáticamente)
@PreAuthorize("hasRole('ADMIN')")
public void method() {
  // Spring ya verificó JWT antes de extraer roles
}
```

---

### ❌ Scoping inconsistente

```java
// MAL: Algunos endpoints validan tenant, otros no
@PreAuthorize("hasRole('ADMIN_TENANT')")
public ResponseEntity<UserData> getUser(@PathVariable String tenantSlug) { ... }

@PreAuthorize("hasRole('ADMIN_TENANT')")  // ← Falta validación de tenant
public ResponseEntity<List<User>> listAll() { ... }

// BIEN: Todos validan si es tenant-scoped
@PreAuthorize("hasRole('ADMIN_TENANT') and @tenantAuthorizationEvaluator.hasTenantAccess(auth)")
public ResponseEntity<List<User>> listByTenant(@PathVariable String tenantSlug) { ... }
```

---

## 6. Checklist: Nuevo Endpoint

Cuando agregues un nuevo endpoint:

- [ ] **¿Es platform-level o tenant-scoped?**
  - Platform → `@PreAuthorize("hasRole('KEYGO_ADMIN')")`
  - Tenant → `@PreAuthorize("... and @tenantAuthorizationEvaluator.hasTenantAccess(...)")`

- [ ] **¿Qué rol es requerido?**
  - KEYGO_ADMIN para global
  - ADMIN_TENANT para tenant
  - Roles específicos de app para funcionalidades

- [ ] **¿Está documentado en ENDPOINT_CATALOG.md?**
  - Columna "Auth" debe listar requerimiento

- [ ] **¿JWT contiene los claims necesarios?**
  - `sub`, `tenant_slug`, `roles`, `aud`

- [ ] **¿Tests validan tanto acceso permitido como denegado?**
  ```java
  @Test
  void testCreateUser_adminAllowed() { ... }
  
  @Test
  void testCreateUser_userDenied() { ... }
  
  @Test
  void testCreateUser_differentTenantDenied() { ... }
  ```

---

## 7. Ejemplo Completo: CreateUser

### Escenario 1: keygo_admin accediendo a cualquier tenant

#### Token (JWT)

```json
{
  "sub": "admin-user-uuid",
  "tenant_slug": "ANY",
  "roles": ["keygo_admin"],
  "aud": "keygo-console",
  "iss": "https://keygo.local/api/v1/platform"
}
```

#### Request

```
POST /api/v1/tenants/my-company/users
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "username": "john",
  "email": "john@example.com",
  "password": "SecurePass123!"
}
```

#### Spring Security Valida

**Step 1:** Verificar JWT firma (automático)

**Step 2:** Extraer tenant de URL → "my-company"

**Step 3:** Extraer roles → ["keygo_admin"]

**Step 4:** Evaluar @PreAuthorize
```
@tenantAuthorizationEvaluator.hasTenantAccess(authentication)
  → ¿keygo_admin? Sí → acceso permitido
```

**Step 5:** Execute método
```
createUser(tenantSlug="my-company", request=...)
```

#### Response

```json
{
  "data": { "id": "user-uuid", "email": "john@example.com", "username": "john" },
  "success": { "code": "USER_CREATED", "message": "User created successfully" }
}
```

---

### Escenario 2: Tenant user con rol ADMIN_ORG accediendo solo a su tenant

#### Token (JWT)

```json
{
  "sub": "tenant-user-uuid",
  "tenant_slug": "my-company",
  "roles": ["ADMIN_ORG"],
  "aud": "keygo-console",
  "iss": "https://keygo.local/api/v1/tenants/my-company"
}
```

#### Request (mismo endpoint)

```
POST /api/v1/tenants/my-company/users
Authorization: Bearer eyJ...
...
```

#### Spring Security Valida

**Step 1:** Verificar JWT firma

**Step 2:** Extraer tenant de URL → "my-company"

**Step 3:** Extraer tenant del token → "my-company"

**Step 4:** Evaluar @PreAuthorize
```
@tenantAuthorizationEvaluator.hasTenantAccess(authentication)
  → ¿keygo_admin? No
  → ¿URL tenant == JWT tenant? "my-company" == "my-company" → Sí → acceso permitido
```

**Step 5:** Execute método

#### Response

```json
{
  "data": { "id": "user-uuid", ... },
  "success": { "code": "USER_CREATED", "message": "..." }
}
```

---

### Escenario 3: User intenta acceder a otro tenant

#### Token (JWT)

```json
{
  "sub": "tenant-user-uuid",
  "tenant_slug": "my-company",
  "roles": ["ADMIN_ORG"],
  "iss": "https://keygo.local/api/v1/tenants/my-company"
}
```

#### Request (intentando acceder a otro tenant)

```
POST /api/v1/tenants/other-company/users
Authorization: Bearer eyJ...
...
```

#### Spring Security Rechaza

**Step 1:** Verificar JWT firma

**Step 2:** Extraer tenant de URL → "other-company"

**Step 3:** Extraer tenant del token → "my-company"

**Step 4:** Evaluar @PreAuthorize
```
@tenantAuthorizationEvaluator.hasTenantAccess(authentication)
  → ¿keygo_admin? No
  → ¿URL tenant == JWT tenant? "other-company" == "my-company" → No → acceso denegado
```

#### Response

```json
{
  "data": null,
  "success": null,
  "error": {
    "code": "BUSINESS_RULE_VIOLATION",
    "message": "Insufficient privileges for this operation",
    "origin": "CLIENT_REQUEST",
    "layer": "SECURITY"
  }
}
```

---

## 8. Referencias

| Aspecto | Ubicación |
|---|---|
| **Roles definidos** | `keygo-domain/{membership,platform}/model/{Platform,Tenant}UserRole.java` |
| **Validación de tenant** | `keygo-api/src/main/java/io/cmartinezs/keygo/api/security/TenantAuthorizationEvaluator.java` |
| **Endpoints** | `keygo-api/src/main/java/io/cmartinezs/keygo/api/{domain}/controller/` |
| **Catálogo** | `docs/design/api/ENDPOINT_CATALOG.md` (columna "Auth") |
| **RFC origen** | `docs/rfc/restructure-multitenant/06-jwt-y-autorizacion.md` |
| **Errores de autorización** | `docs/design/api/ERROR_CATALOG.md` (403 BUSINESS_RULE_VIOLATION) |

---

**Última actualización:** 2026-04-10  
**Estado:** Completado para Sprint 2  
**RFC Closure:** Absorbe decisiones de restructure-multitenant §06
