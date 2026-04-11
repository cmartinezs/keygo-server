# Validation Strategy — Dónde Va Cada Validación

**Propósito:** Clarificar en qué capa se valida cada aspecto de un input.

---

## Principio

**Una validación pertenece a una capa específica** según si:
- Es una **restricción de formato/estructura** (HTTP boundary)
- Es una **regla de negocio** (dominio)
- Es **contextual/stateful** (use case)

**Ventaja:** Reaplicabilidad. La lógica de dominio es testeable sin HTTP, y se reutiliza en CLI, eventos, etc.

---

## Las 3 Capas

### 1️⃣ HTTP Layer: Request DTOs

**Qué:** Validaciones de formato y restricciones técnicas.

**Anotaciones:** `@NotBlank`, `@NotNull`, `@Size`, `@Email`, `@Pattern`, `@Digits`

**Cuándo:** 
- Formato básico (no vacío, tamaño límite, patrón)
- Solo lo que Spring puede validar sin BD/contexto

**Ejemplo:**
```java
public record CreateUserRequest(
    @NotBlank(message = "username is required")
    @Size(min = 3, max = 100, message = "username must be between 3 and 100")
    String username,

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    String email,

    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be at least 8 characters")  // ← Mínimo HTTP
    String password
) {}
```

**Flujo:**
1. HTTP request llega
2. DispatcherServlet aplica `@Valid` en parámetro
3. Si falla → `400 INVALID_INPUT` con `fieldErrors`
4. Si pasa → se pasa al use case

**⚠️ Limitación:** No puede validar:
- Unicidad de email/username (requiere BD)
- Política completa de contraseña (regla de dominio)
- Dependencias entre campos

---

### 2️⃣ Domain Layer: Value Objects & Helpers

**Qué:** Invariantes de negocio y lógica de validación compleja.

**Dónde:**
- **Value Objects** (record con validación en `compact constructor`)
- **Helpers estáticos** (utilidad reutilizable)
- **Métodos de dominio** (invariantes de entidad)

**Cuándo:** 
- La validación refleja una regla de negocio
- Se reutiliza en múltiples contextos (CLI, eventos, etc.)
- Requiere lógica compleja (regexes, combinaciones, etc.)

**Ejemplos concretos:**

#### Value Object: Username
```java
// keygo-domain/src/main/java/io/cmartinezs/keygo/domain/user/model/Username.java
public record Username(String value) {
  private static final String USERNAME_REGEX = "^[a-zA-Z0-9_.\\-]{3,100}$";

  public Username {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Username cannot be null or blank");
    }
    if (!value.matches(USERNAME_REGEX)) {
      throw new IllegalArgumentException(
          "Username must be 3-100 chars (letters, digits, '_', '-', '.')");
    }
  }

  public static Username of(String value) {
    return new Username(value);  // Constructor valida automáticamente
  }
}
```

**Ventaja:** `Username.of(string)` siempre devuelve un username válido o lanza excepción.

#### Value Object: EmailAddress
```java
// keygo-domain/src/main/java/io/cmartinezs/keygo/domain/user/model/EmailAddress.java
public record EmailAddress(String value) {
  private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";

  public EmailAddress {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("EmailAddress cannot be null or blank");
    }
    if (!value.matches(EMAIL_REGEX)) {
      throw new IllegalArgumentException("EmailAddress has invalid format");
    }
  }

  public static EmailAddress of(String value) {
    return new EmailAddress(value);
  }
}
```

#### Helper: PasswordValidationHelper
```java
// keygo-domain/src/main/java/io/cmartinezs/keygo/domain/user/model/PasswordValidationHelper.java
public final class PasswordValidationHelper {
  
  /**
   * Valida contraseña según contexto (temporal o permanente).
   * @param password the password to validate
   * @param isTemporary true=generated (relaxed), false=user-defined (strict)
   * @throws InvalidPasswordException if validation fails
   */
  public static void validate(String password, boolean isTemporary) {
    if (isTemporary) {
      validateTemporaryPassword(password);  // Min 8 chars
    } else {
      validatePermanentPassword(password);  // Min 12 chars + 4 clases
    }
  }

  static void validatePermanentPassword(String password) {
    if (password.length() < 12) {
      throw new InvalidPasswordException("must be at least 12 characters");
    }
    if (!password.matches(".*[A-Z].*")) {
      throw new InvalidPasswordException("must contain uppercase letter");
    }
    if (!password.matches(".*[a-z].*")) {
      throw new InvalidPasswordException("must contain lowercase letter");
    }
    if (!password.matches(".*\\d.*")) {
      throw new InvalidPasswordException("must contain digit");
    }
    if (!password.matches(".*[^A-Za-z0-9].*")) {
      throw new InvalidPasswordException("must contain special character");
    }
  }
}
```

**Ventaja:** Lógica reutilizable, testeable sin Spring, sin BD.

---

### 3️⃣ Use Case Layer: Orquestación

**Qué:** Validaciones contextuales y de estado (requieren BD o coordinar múltiples entidades).

**Dónde:** En el método `execute()` del use case.

**Cuándo:**
- Validar unicidad (email/username dentro de tenant)
- Validar estado de precondiciones (tenant está activo?)
- Coordinar entre múltiples agregados

**Ejemplo: CreateUserUseCase**
```java
public class CreateUserUseCase {
  
  public User execute(CreateUserCommand command) {
    // 1. Validación contextual: tenant existe?
    Tenant tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(command.tenantSlug()))
        .orElseThrow(() -> new TenantNotFoundException(command.tenantSlug()));

    // 2. Validación de estado: tenant está activo?
    if (tenant.isSuspended()) {
      throw new TenantSuspendedException(command.tenantSlug());
    }

    // 3. Crear value objects (valida dominio automáticamente)
    EmailAddress email = EmailAddress.of(command.email());
    Username username = Username.of(command.username());

    // 4. Validación de unicidad (require BD query)
    if (userRepositoryPort.existsByTenantIdAndEmail(tenant.getId(), email)) {
      throw new DuplicateUserException("email", command.email());
    }

    if (userRepositoryPort.existsByTenantIdAndUsername(tenant.getId(), username)) {
      throw new DuplicateUserException("username", command.username());
    }

    // 5. Validar política de contraseña (dominio)
    PasswordValidationHelper.validate(command.rawPassword(), false);

    // 6. Crear entidad (siempre válida por ahora)
    String hashedPassword = credentialEncoderPort.encode(command.rawPassword());
    User user = User.builder()
        .tenantId(tenant.getId())
        .username(username)
        .email(email)
        .passwordHash(PasswordHash.of(hashedPassword))
        .firstName(command.firstName())
        .lastName(command.lastName())
        .status(UserStatus.ACTIVE)
        .build();

    return userRepositoryPort.save(user);
  }
}
```

**Flujo de excepciones:**
- `TenantNotFoundException` → 404 (mapeo en GlobalExceptionHandler)
- `TenantSuspendedException` → 409 (BUSINESS_RULE_VIOLATION)
- `DuplicateUserException` → 409 (BUSINESS_RULE_VIOLATION)
- `InvalidPasswordException` → 400 (INVALID_INPUT)

---

## Matriz: Qué Va Dónde

| Validación | Ejemplo | Capa | Anotación | Excepción |
|---|---|---|---|---|
| Formato: no vacío | email required | HTTP | `@NotBlank` | `INVALID_INPUT` |
| Tamaño límite | max 255 chars | HTTP | `@Size` | `INVALID_INPUT` |
| Patrón básico | email format | HTTP | `@Email` | `INVALID_INPUT` |
| Invariante VO | username regex | Domain | Value Object constructor | `IllegalArgumentException` |
| Lógica compleja | password policy | Domain | Helper static | `InvalidPasswordException` |
| Unicidad | email exists? | Use Case | Repository query | `DuplicateUserException` |
| Dependencias entre campos | new password ≠ old | Use Case | Custom logic | Business exception |
| Precondiciones | tenant active? | Use Case | Repository state | Domain exception |

---

## Testing por Capa

### HTTP Layer: Unit Tests
```java
@Test
void testCreateUserRequest_invalidEmail() {
  // Simular: {"username": "john", "email": "invalid", "password": "Temp1234!"}
  CreateUserRequest request = new CreateUserRequest("john", "invalid", "Temp1234!");
  // DispatcherServlet aplicará @Valid → 400
}
```

### Domain Layer: Unit Tests (sin Spring)
```java
@Test
void testPasswordValidation_permanent_tooShort() {
  assertThrows(InvalidPasswordException.class, 
    () -> PasswordValidationHelper.validate("Short1!", false));
}

@Test
void testUsername_validFormat() {
  Username username = Username.of("john_doe");
  assertEquals("john_doe", username.value());
}
```

### Use Case Layer: Integration Tests
```java
@Test
void testCreateUserUseCase_duplicateEmail() {
  // Dado: usuario "john@example.com" ya existe
  // Cuando: crear otro usuario con mismo email
  assertThrows(DuplicateUserException.class,
    () -> createUserUseCase.execute(command));
}

@Test
void testCreateUserUseCase_tenantSuspended() {
  // Dado: tenant está suspended
  assertThrows(TenantSuspendedException.class,
    () -> createUserUseCase.execute(command));
}
```

---

## Ejemplo Completo: CreateUser

### 1. Request llega con JSON
```json
POST /api/v1/tenants/my-tenant/users
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "TempPass123!"
}
```

### 2. HTTP Layer Valida
```java
CreateUserRequest request = ...;  // @Valid applied
// Si falla → 400 INVALID_INPUT con fieldErrors
// Si pasa → continúa
```

### 3. Controller → Use Case
```java
@PostMapping("/api/v1/tenants/{tenantSlug}/users")
public ResponseEntity<BaseResponse<UserData>> createUser(
    @PathVariable String tenantSlug,
    @Valid @RequestBody CreateUserRequest request) {
  
  CreateUserCommand command = new CreateUserCommand(
      tenantSlug, request.username(), request.email(), request.password());
  
  User user = createUserUseCase.execute(command);  // ← Orquestación
  return ResponseEntity.status(CREATED).body(BaseResponse.success(...));
}
```

### 4. Use Case Valida
```java
public User execute(CreateUserCommand command) {
  // Contextual validations:
  Tenant tenant = tenantRepositoryPort.findBySlug(...);  // 404?
  if (tenant.isSuspended()) throw TenantSuspendedException();  // 409
  
  // Domain validations:
  Username username = Username.of(command.username());  // Regex VO
  PasswordValidationHelper.validate(command.rawPassword(), false);  // Policy
  
  // Uniqueness:
  if (userRepositoryPort.existsByTenantIdAndEmail(...)) {
    throw DuplicateUserException();  // 409
  }
  
  // Create + Persist
  User user = User.builder()...build();
  return userRepositoryPort.save(user);
}
```

### 5. Excepciones → HTTP Response
```java
// GlobalExceptionHandler mapea:
TenantNotFoundException → 404 RESOURCE_NOT_FOUND
TenantSuspendedException → 409 BUSINESS_RULE_VIOLATION
InvalidPasswordException → 400 INVALID_INPUT
DuplicateUserException → 409 BUSINESS_RULE_VIOLATION
```

---

## Checklist: Nuevo Endpoint

Cuando agregues un nuevo endpoint con validación:

- [ ] **HTTP:** ¿Qué restricciones de formato? (@NotBlank, @Size, @Email, @Pattern)
- [ ] **Domain:** ¿Qué reglas de negocio? (Value Objects, Helpers, métodos de dominio)
- [ ] **Use Case:** ¿Qué validaciones contextuales? (Unicidad, estado, precondiciones)
- [ ] **Exceptions:** ¿Mapeo a ResponseCode correcto? (4xx vs 5xx, qué origin)
- [ ] **Tests:** ¿Unitarios para dominio, integración para use case?
- [ ] **Documentación:** ¿ERROR_CATALOG.md actualizado con nuevos codes?

---

## Anti-Patterns

❌ **Validar en el controller**
```java
// MAL
if (user.getEmail().contains("@")) {
  createUserUseCase.execute(...);
}
```
→ Se pierde cuando usas use case desde CLI

✅ **Validar en domain + deleguar a use case**
```java
// BIEN
EmailAddress.of(email);  // Valida automáticamente
createUserUseCase.execute(command);  // Simplemente usa
```

❌ **Validación de negocio en HTTP**
```java
// MAL
@PostMapping
public void create(@RequestParam String email) {
  if (userRepository.existsByEmail(email)) {  // ← En controller!
    throw new BadRequestException();
  }
}
```
→ No reutilizable, acoplado a Spring

✅ **Validación de negocio en use case**
```java
// BIEN
public User execute(CreateUserCommand command) {
  if (userRepository.existsByEmail(command.email())) {
    throw DuplicateUserException();  // ← Mejor excepción
  }
}
```

---

## Referencias

| Elemento | Ubicación |
|---|---|
| **Value Objects** | `keygo-domain/src/main/java/io/cmartinezs/keygo/domain/{entity}/model/*.java` |
| **Helpers** | `PasswordValidationHelper.java`, `PasswordHashValidator.java` |
| **Use Cases** | `keygo-app/src/main/java/io/cmartinezs/keygo/app/{entity}/usecase/*.java` |
| **Exceptions** | `keygo-domain/src/main/java/io/cmartinezs/keygo/domain/{entity}/exception/*.java` |
| **DTOs** | `keygo-api/src/main/java/io/cmartinezs/keygo/api/{entity}/request/*.java` |
| **Exception Mapping** | `docs/design/api/ERROR_CATALOG.md` |

---

**Última actualización:** 2026-04-10  
**Estado:** Completado para Sprint 1
