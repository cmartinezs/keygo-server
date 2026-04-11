# Test Integration Strategy — Cómo Testear KeyGo

**Propósito:** Documentar estrategia de testing a todos los niveles (unit, integration, contract) para mantener confianza en cambios y evitar regresiones.

---

## Filosofía: Test Trophy

```
         🏆 E2E (UI)
       ↙           ↖
    Integration    Contract
       ↖           ↙
       Unit Tests
```

**Distribución recomendada:**
- **Unit:** 60% (rápidos, determinísticos, domain logic)
- **Integration:** 30% (con BD real, flujos complejos)
- **Contract/E2E:** 10% (API, flujos críticos)

---

## Nivel 1: Unit Tests

### Patrón: Arrange-Act-Assert

```java
@Test
void createUserWithValidEmailSucceeds() {
  // Arrange
  EmailAddress email = EmailAddress.of("john@example.com");
  Username username = Username.of("john_doe");
  
  // Act
  User user = User.builder()
      .email(email)
      .username(username)
      .build();
  
  // Assert
  assertThat(user.getEmail()).isEqualTo(email);
  assertThat(user.getUsername()).isEqualTo(username);
}
```

### Value Objects

Testear inmutabilidad, validación, igualdad:

```java
@Test
void emailAddressRejectsInvalidFormat() {
  // Assert
  assertThatThrownBy(() -> EmailAddress.of("invalid-email"))
      .isInstanceOf(DomainException.class)
      .hasMessageContaining("EMAIL_INVALID_FORMAT");
}

@Test
void emailAddressesWithSameValueAreEqual() {
  EmailAddress email1 = EmailAddress.of("john@example.com");
  EmailAddress email2 = EmailAddress.of("john@example.com");
  
  assertThat(email1).isEqualTo(email2);
  assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
}
```

### Domain Services

Testear lógica de negocio sin BD:

```java
class PasswordValidationHelperTest {
  
  private PasswordValidationHelper validator;
  
  @Before
  void setup() {
    validator = new PasswordValidationHelper();
  }
  
  @Test
  void validatePasswordWithWeakStrengthFails() {
    ValidationResult result = validator.validate("123");
    assertThat(result.isValid()).isFalse();
    assertThat(result.getErrors())
        .contains("PASSWORD_TOO_SHORT", "PASSWORD_WEAK_ENTROPY");
  }
  
  @Test
  void validatePasswordWithStrongStrengthSucceeds() {
    ValidationResult result = validator.validate("SecureP@ss123!");
    assertThat(result.isValid()).isTrue();
  }
}
```

### Mocking & Spying

Mock solo dependencias externas:

```java
@Test
void createUserSendsWelcomeEmail() {
  // Arrange
  EmailSender emailSender = mock(EmailSender.class);
  CreateUserUseCase useCase = new CreateUserUseCase(
      userRepository,
      emailSender  // ← Mock
  );
  
  CreateUserRequest request = CreateUserRequest.builder()
      .email("john@example.com")
      .build();
  
  // Act
  useCase.execute(request, tenantContext);
  
  // Assert
  verify(emailSender, times(1))
      .send(argThat(email -> email.getTo().equals("john@example.com")));
}
```

**Regla:** No mockear repositorio, BD, o domain objects. Testear el flujo real.

---

## Nivel 2: Integration Tests

### BD Real con Testcontainers

```java
@SpringBootTest
@Testcontainers
class UserRepositoryIntegrationTest {
  
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
      .withDatabaseName("keygo_test")
      .withUsername("keygo")
      .withPassword("test-password");
  
  @Autowired
  private UserRepository userRepository;
  
  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }
  
  @Test
  void createAndFindUserByEmail() {
    // Arrange
    User user = User.builder()
        .email(EmailAddress.of("john@example.com"))
        .username(Username.of("john_doe"))
        .tenantId(TenantId.random())
        .build();
    
    // Act
    User saved = userRepository.save(user);
    Optional<User> found = userRepository.findByEmail(
        EmailAddress.of("john@example.com"),
        saved.getTenantId()
    );
    
    // Assert
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
  }
}
```

### Use Case Integration Tests

Flujo completo: Request → UseCase → Repository → Response

```java
@SpringBootTest
@Testcontainers
class CreateUserUseCaseIntegrationTest {
  
  @Container
  static PostgreSQLContainer<?> postgres = ...;
  
  @Autowired
  private CreateUserUseCase createUserUseCase;
  
  @Autowired
  private UserRepository userRepository;
  
  @Autowired
  private TenantRepository tenantRepository;
  
  @Test
  void createUserFlowSuccess() {
    // Arrange
    Tenant tenant = tenantRepository.save(
        Tenant.builder().slug(TenantSlug.of("acme")).build()
    );
    TenantContext context = TenantContext.of(tenant.getId());
    
    CreateUserRequest request = CreateUserRequest.builder()
        .email("john@example.com")
        .username("john_doe")
        .build();
    
    // Act
    CreateUserResponse response = createUserUseCase.execute(request, context);
    
    // Assert
    assertThat(response.getUserId()).isNotNull();
    
    User saved = userRepository.findById(response.getUserId())
        .orElseThrow();
    assertThat(saved.getEmail().value()).isEqualTo("john@example.com");
  }
  
  @Test
  void createUserFailsWithDuplicateEmail() {
    // Arrange
    Tenant tenant = tenantRepository.save(...);
    userRepository.save(User.builder()
        .email(EmailAddress.of("john@example.com"))
        .tenantId(tenant.getId())
        .build()
    );
    
    CreateUserRequest request = CreateUserRequest.builder()
        .email("john@example.com")  // Duplicate
        .build();
    
    // Act & Assert
    assertThatThrownBy(() -> 
        createUserUseCase.execute(request, TenantContext.of(tenant.getId()))
    ).isInstanceOf(DomainException.class)
     .hasMessageContaining("USER_ALREADY_EXISTS");
  }
}
```

### Database Fixtures

Usar `@Sql` para data setup:

```java
@SpringBootTest
@Sql("/fixtures/tenants.sql")
@Sql("/fixtures/users.sql")
class TenantRepositoryIntegrationTest {
  
  @Test
  void findTenantBySlugReturnsExisting() {
    Optional<Tenant> tenant = tenantRepository.findBySlug(
        TenantSlug.of("acme")
    );
    
    assertThat(tenant).isPresent();
    assertThat(tenant.get().getName()).isEqualTo("Acme Inc");
  }
}
```

**File: `/test/resources/fixtures/tenants.sql`**
```sql
INSERT INTO tenants (id, slug, name, created_at)
VALUES (
  '550e8400-e29b-41d4-a716-446655440000',
  'acme',
  'Acme Inc',
  NOW()
);
```

---

## Nivel 3: Contract / API Tests

### Spring Boot Test + MockMvc

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
  
  @Autowired
  private MockMvc mockMvc;
  
  @Autowired
  private UserRepository userRepository;
  
  @MockBean
  private EmailSender emailSender;
  
  @Test
  void createUserReturns201() throws Exception {
    // Arrange
    CreateUserRequest request = CreateUserRequest.builder()
        .email("john@example.com")
        .username("john_doe")
        .build();
    
    String tenantSlug = "acme";
    
    // Act & Assert
    mockMvc.perform(
        post("/api/v1/tenants/{tenantSlug}/users", tenantSlug)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(jwt().jwt(jwt -> jwt.subject("admin-user")))
    )
    .andExpect(status().isCreated())
    .andExpect(jsonPath("$.data.userId").isNotEmpty())
    .andExpect(jsonPath("$.data.email").value("john@example.com"));
  }
  
  @Test
  void createUserWith403IfNoPermission() throws Exception {
    CreateUserRequest request = CreateUserRequest.builder()
        .email("john@example.com")
        .build();
    
    mockMvc.perform(
        post("/api/v1/tenants/{tenantSlug}/users", "acme")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(jwt().jwt(jwt -> jwt.subject("regular-user")))
    )
    .andExpect(status().isForbidden())
    .andExpect(jsonPath("$.error.code").value("INSUFFICIENT_PERMISSIONS"));
  }
}
```

### JWT Mock Helper

```java
@Configuration
public class TestJwtConfig {
  
  @Bean
  public JwtBuilder platformAdminJwt() {
    return jwt()
        .subject("platform-admin-id")
        .claim("roles", List.of("keygo_admin"))
        .claim("aud", "keygo-platform");
  }
  
  @Bean
  public JwtBuilder tenantAdminJwt(String tenantSlug) {
    return jwt()
        .subject("tenant-admin-id")
        .claim("tenant_slug", tenantSlug)
        .claim("roles", List.of("admin"))
        .claim("aud", "keygo-tenant");
  }
}
```

Usage:
```java
mockMvc.perform(
    post("/api/v1/tenants/{tenantSlug}/users", "acme")
        .with(platformAdminJwt())  // ← Helper
)
```

---

## Test Fixtures & Builders

### Data Builders

```java
public class UserTestBuilder {
  
  private UUID id = UUID.randomUUID();
  private EmailAddress email = EmailAddress.of("default@example.com");
  private Username username = Username.of("test_user");
  private UUID tenantId = UUID.randomUUID();
  private Instant createdAt = Instant.now();
  
  public UserTestBuilder withEmail(String emailStr) {
    this.email = EmailAddress.of(emailStr);
    return this;
  }
  
  public UserTestBuilder withTenantId(UUID tenantId) {
    this.tenantId = tenantId;
    return this;
  }
  
  public User build() {
    return User.builder()
        .id(id)
        .email(email)
        .username(username)
        .tenantId(tenantId)
        .createdAt(createdAt)
        .build();
  }
}
```

Usage:
```java
User user = new UserTestBuilder()
    .withEmail("john@example.com")
    .withTenantId(tenantId)
    .build();
```

---

## Test Structure & Organization

```
keygo-app/src/test/java/io/cmartinezs/keygo/
├── domain/
│   ├── user/
│   │   ├── value/
│   │   │   ├── EmailAddressTest.java
│   │   │   └── UsernameTest.java
│   │   ├── service/
│   │   │   └── PasswordValidationHelperTest.java
│   │   └── entity/
│   │       └── UserTest.java
│
├── app/
│   ├── user/
│   │   └── usecase/
│   │       └── CreateUserUseCaseIntegrationTest.java
│
├── adapter/
│   ├── repository/
│   │   └── UserRepositoryAdapterIntegrationTest.java
│   └── controller/
│       └── UserControllerIntegrationTest.java
│
├── fixtures/
│   ├── builders/
│   │   ├── UserTestBuilder.java
│   │   ├── TenantTestBuilder.java
│   │   └── ...
│   └── database/
│       ├── tenants.sql
│       └── users.sql
```

---

## Coverage Goals

| Layer | Minimum | Target |
|---|---|---|
| **Domain (VO, Entities, Services)** | 85% | 95% |
| **Use Cases** | 75% | 90% |
| **Adapters (Controllers, Repos)** | 70% | 85% |
| **Overall** | 75% | 85% |

**Comando para verificar:**
```bash
./mvnw clean verify jacoco:report
# → target/site/jacoco/index.html
```

---

## CI/CD Integration

### GitHub Actions

```yaml
name: Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: keygo_test
          POSTGRES_USER: keygo
          POSTGRES_PASSWORD: test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
      - uses: actions/checkout@v3
      
      - uses: actions/setup-java@v3
        with:
          java-version: 21
          distribution: temurin
      
      - name: Run tests
        run: ./mvnw clean verify
      
      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./target/site/jacoco/jacoco.xml
```

### Pre-commit Hook

```bash
#!/bin/bash
# .git/hooks/pre-commit

echo "Running unit tests..."
./mvnw clean test -DskipITs

if [ $? -ne 0 ]; then
  echo "❌ Tests failed. Commit aborted."
  exit 1
fi

echo "✅ Tests passed."
```

---

## Anti-Patterns: Evitar

### ❌ Test Logic en Application Code

```java
// MAL
public class User {
  public boolean isValidEmail(String email) {
    // Test logic aquí
    if (email == null) return false;
    return email.contains("@");
  }
}
```

### ✅ Validación en Value Object

```java
// BIEN
public class EmailAddress {
  private final String value;
  
  private EmailAddress(String value) {
    if (!isValidFormat(value)) {
      throw new DomainException("EMAIL_INVALID_FORMAT");
    }
    this.value = value;
  }
  
  // Test esto en EmailAddressTest
}
```

---

### ❌ Testear Implementación en vez de Comportamiento

```java
// MAL
@Test
void userHasIdField() {
  User user = ...;
  assertThat(user).hasFieldOrPropertyWithValue("id", expectedId);
}
```

### ✅ Testear Comportamiento

```java
// BIEN
@Test
void userIdIsImmutable() {
  User user = User.builder().id(id1).build();
  // No hay setter para id
  assertThat(user.getId()).isEqualTo(id1);
}
```

---

### ❌ Flaky Tests (No determinísticos)

```java
// MAL: Depende de timing
@Test
void asyncOperationCompletes() {
  userService.createAsync(request);
  Thread.sleep(1000);  // ← Flaky
  assertThat(userRepository.findAll()).hasSize(1);
}
```

### ✅ Sincronismo y Awaits

```java
// BIEN
@Test
void asyncOperationCompletes() {
  CompletableFuture<User> future = userService.createAsync(request);
  
  User user = future.get(5, TimeUnit.SECONDS);
  assertThat(user).isNotNull();
}
```

---

## Checklist: Nuevo Test

- [ ] **¿Qué comportamiento testeo?** (no implementación)
- [ ] **¿Es determinístico?** (sin timing, sin random)
- [ ] **¿Está aislado?** (mockear solo dependencias externas)
- [ ] **¿Nombre claro?** (describe qué y por qué)
  - ✅ `createUserWithValidEmailSucceeds`
  - ❌ `test1`
- [ ] **¿AAA estructura?** (Arrange, Act, Assert)
- [ ] **¿Un assertion principal?** (múltiples OK, pero clara la intent)
- [ ] **¿Usa fixtures?** (builders en vez de hardcoding)
- [ ] **¿Coverage aumentó?** (revisar línea de cobertura)

---

## Referencias

| Aspecto | Ubicación |
|---|---|
| **Domain Models** | `keygo-domain/src/main/java/io/cmartinezs/keygo/domain/` |
| **Use Cases** | `keygo-app/src/main/java/io/cmartinezs/keygo/app/` |
| **Controllers** | `keygo-api/src/main/java/io/cmartinezs/keygo/api/` |
| **Fixtures** | `src/test/java/fixtures/` |
| **JaCoCo Reports** | `target/site/jacoco/index.html` |
| **Pattern Design** | `docs/design/PATTERNS.md` |
| **Validation** | `docs/design/VALIDATION_STRATEGY.md` |

---

**Última actualización:** 2026-04-10  
**Estado:** Completado para Sprint 3  
**Próxima:** OBSERVABILITY.md
