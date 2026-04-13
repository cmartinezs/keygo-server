# Lecciones — Seguridad

Autenticación, autorización, aislamiento tenant y patrones anti-abuso.

---

### [2026-04-07] Cascada multi-capa en login: PlatformUser → TenantUser → Membership

**Contexto:** Validación de status en flujo de login no incluía todos los niveles (plataforma, tenant, membership).

**Problema:** `AuthenticatePlatformUserUseCase` no validaba `RESET_PASSWORD`; `ValidateUserCredentialsUseCase` no validaba el status del `PlatformUser` vinculado; `IssueAuthorizationCodeUseCase` no validaba `Membership.PENDING`.

**Solución / Buena práctica:** Cada punto de validación verifica su capa Y cascada hacia abajo. Patrón: platform login → check platform status; tenant login → check platform cascade + tenant status; auth code → check membership status. Excepciones específicas por capa (`PlatformUserSuspendedException`, `MembershipPendingException`) para que el API consumer sepa qué capa rechazó.

**Archivos clave:** `AuthenticatePlatformUserUseCase.java`, `ValidateUserCredentialsUseCase.java`, `IssueAuthorizationCodeUseCase.java`.

---

### [2026-04-04] Flujo reset-password: usar `requestId` en lugar de email — evitar enumeración y ordering correcto

**Contexto:** Implementación del endpoint público `POST /account/reset-password` para usuarios en estado `RESET_PASSWORD` que no pueden autenticarse con Bearer token.

**Problema:** El flujo anterior identificaba al usuario por email (campo en el request body), lo cual: 1) podría revelar qué emails existen en el sistema; 2) requería una llamada DB para buscar al usuario antes de validar el código de verificación.

**Solución / Buena práctica:**
- `SendPasswordResetCodeUseCase.execute()` retorna `SendPasswordResetCodeResult(requestId)` con el UUID de la fila persistida en `password_reset_codes`.
- El controller incluye `reset_code_id` en el body del 401 `RESET_PASSWORD_REQUIRED`.
- `ResetPasswordCommand` usa `requestId` (UUID) en lugar de `email`.
- `ResetPasswordUseCase` busca primero por `requestId` → valida el código → luego busca al usuario con `findByIdAndTenantId` (protección cross-tenant) → verifica estado → valida contraseña.
- **El orden es importante:** el código se valida **antes** de buscar al usuario para evitar revelar información de existencia del usuario cuando el código es inválido.

**Archivos clave:**
- `SendPasswordResetCodeUseCase.java`
- `ResetPasswordUseCase.java`
- `PasswordResetCodeRepositoryPort.java`
- `AccountResetPasswordRequest.java` (campo `request_id`)

---

### [2026-04-02] Anti-enumeración en `ForgotPasswordUseCase` — siempre retornar `sent=true`

**Causa:** Un endpoint de recuperación que devuelve error diferente cuando el email no existe permite a un atacante enumerar cuentas registradas.

**Solución:** `ForgotPasswordUseCase` siempre retorna `ForgotPasswordResult(true)` sin importar si el email existe. El email solo se envía si el usuario se encuentra. No revelar la existencia de la cuenta.

---

### [2026-04-02] Timing oracle en `ValidateUserCredentialsUseCase` — verificar estado DESPUÉS del password

**Causa:** Verificar `status == RESET_PASSWORD` ANTES de validar la contraseña permite a un atacante distinguir usuarios `RESET_PASSWORD` vs `ACTIVE` sin conocer la contraseña (el error es diferente).

**Solución:** Validar contraseña primero (`passwordHasher.matches`); si es incorrecta, lanzar siempre `InvalidCredentialsException`. Solo si la contraseña es **correcta** verificar si el estado es `RESET_PASSWORD` y lanzar `UserPasswordResetRequiredException`.

---

### [2026-03-27] Use cases sin scope de tenant permiten acceso cross-tenant

**Síntoma:** `listByUserId(userId)` devolvía memberships de cualquier tenant.

**Causa:** Controller pasaba `{tenantSlug}` en path pero use cases filtraban solo por ID.

**Solución:** Agregar métodos tenant-scoped al repositorio: `findByUserIdAndTenantSlug`. JPQL: `m.user.tenant.slug`. `@PreAuthorize` controla QUIÉN; repositorio controla QUÉ datos devuelve.

**Archivos clave:** `MembershipRepositoryPort.java`, `ListMembershipsUseCase.java`, `MembershipJpaRepository.java`

---

### [2026-03-25] Bearer-only admin auth: autenticación en filtro + autorización en endpoint

**Síntoma:** JWT válido en filtro pero sin control por tenant ni endpoint — acceso cross-tenant posible.

**Causa:** Autenticación centralizada en filtro sin autorización fina por recurso.

**Solución:** Filtro autentica. `@PreAuthorize` por endpoint con evaluador SpEL que compara `tenantSlug` del path con claim `tenant_slug` del JWT.

**Archivos clave:** `BootstrapAdminKeyFilter.java`, `TenantAuthorizationEvaluator.java`

---

### [2026-03-24] Filtro solo aceptaba `X-KEYGO-ADMIN` — círculo vicioso en rutas OAuth2

**Síntoma:** Frontend no podía obtener JWT porque las rutas OAuth2 también requerían `X-KEYGO-ADMIN`.

**Causa:** Filtro incompleto; rutas de autorización protegidas igual que el resto.

**Solución:** Marcar sufijos públicos (`authorizePathSuffix`, `loginPathSuffix`, `tokenPathSuffix`). Agregar `validateBearerAdminToken()` que acepta Bearer JWT con rol admin. Probar primero `X-KEYGO-ADMIN`, luego Bearer.

**Archivos clave:** `BootstrapAdminKeyFilter.java`, `KeyGoBootstrapProperties.java`

---

### [2026-03-24] `SigningKeyInitializer`: auto-generar clave RSA en startup si DB vacía

**Síntoma:** DB vacía sin clave RSA → arranque fallaba.

**Causa:** Sin mecanismo de inicialización automática.

**Solución:** Crear `SigningKeyInitializer implements ApplicationRunner` con `@Profile("supabase")`. Si no hay ACTIVE, genera RSA-2048, codifica a PEM y persiste. Idempotente.

**Archivos clave:** `SigningKeyInitializer.java`

---

### [2026-03-23] Grant `client_credentials`: sin `id_token` ni `refresh_token`

**Síntoma:** Use case M2M emitía campos propios de `authorization_code`.

**Causa:** Diferencias entre grants no aplicadas en implementación.

**Solución:** `client_credentials`: `sub` = `clientId` (string, no UUID). Sin `id_token`. Sin `refresh_token`. Solo apps `CONFIDENTIAL`. Campos excluidos con `@JsonInclude(NON_NULL)`.

**Archivos clave:** `IssueClientCredentialsTokenUseCase.java`
