# Security Guidelines — OWASP, Secrets, Encryption

**Propósito:** Documentar prácticas de seguridad, protección contra vulnerabilidades OWASP, manejo de secretos y compliance requirements.

---

## OWASP Top 10 — KeyGo Defense Strategy

### 1. Broken Authentication

**Risk:** Atacante accede a cuenta de usuario

**Defenses:**
- ✅ OAuth2 PKCE flow (no password en frontend)
- ✅ JWT tokens con expiry corto (1 hora)
- ✅ Refresh token rotation (nuevo token cada refresh)
- ✅ Rate limiting en login (max 5 intentos/10 min)
- ✅ Account lockout (3 failed attempts)

```java
// Rate limiting
@Component
public class LoginRateLimiter {
  private final RateLimiterRegistry registry;
  
  @PostMapping("/oauth/token")
  public ResponseEntity<?> token(@RequestParam String code) {
    String clientIp = getClientIp();
    RateLimiter limiter = registry.rateLimiter("login_" + clientIp);
    
    if (!limiter.acquirePermission()) {
      return ResponseEntity.status(429)
          .body(ErrorResponse.of("RATE_LIMITED", "Too many login attempts"));
    }
    
    // Process login
  }
}
```

### 2. Broken Access Control

**Risk:** Usuario accede a recurso que no le pertenece

**Defenses:**
- ✅ Role-based access control (RBAC)
- ✅ Tenant isolation (users can't access other tenants)
- ✅ Row-level security (users see only their data)

```java
// Tenant isolation in queries
@Component
public class UserRepository {
  
  @PreAuthorize("hasRole('ADMIN_TENANT')")
  public User findById(UUID id, TenantContext context) {
    return repository.findById(id)
        .filter(user -> user.getTenantId().equals(context.getTenantId()))
        .orElseThrow(() -> new UserNotFoundException());
  }
}
```

### 3. Injection (SQL, Command, LDAP)

**Risk:** Attacker injects malicious code via input

**Defenses:**
- ✅ Use parameterized queries (JPA, never string concat)
- ✅ Input validation (whitelist, regex)
- ✅ Escape special characters

```java
// ❌ BAD: SQL injection risk
String query = "SELECT * FROM users WHERE email = '" + email + "'";
List<?> users = entityManager.createNativeQuery(query).getResultList();

// ✅ GOOD: Parameterized
TypedQuery<User> query = entityManager.createQuery(
    "SELECT u FROM User u WHERE u.email = :email", User.class);
query.setParameter("email", email);
List<User> users = query.getResultList();

// ✅ GOOD: JPA
Optional<User> user = userRepository.findByEmail(email, tenantId);
```

### 4. Insecure Design

**Risk:** No security requirements in design phase

**Defenses:**
- ✅ Threat modeling (STRIDE)
- ✅ Security in architecture reviews
- ✅ Principle of least privilege

**Threat Model Example (STRIDE):**
```
Threat: Attacker intercepts OAuth token
├─ Spoofing: Forge token signature? 
│  └─ Mitigation: RS256 signing, key rotation
├─ Tampering: Modify token payload?
│  └─ Mitigation: JWT signature verification
├─ Repudiation: Deny sending token?
│  └─ Mitigation: Audit log with token fingerprint
├─ Information Disclosure: Steal token?
│  └─ Mitigation: HTTPS only, secure storage (sessionStorage)
├─ Denial of Service: Revoke all tokens?
│  └─ Mitigation: Distributed session store (Redis)
└─ Elevation of Privilege: Increase scope?
   └─ Mitigation: Scope in signed JWT, can't modify
```

### 5. Cryptographic Failures

**Risk:** Weak encryption or unencrypted sensitive data

**Defenses:**
- ✅ Encrypt at rest (AES-256)
- ✅ Encrypt in transit (TLS 1.2+)
- ✅ Strong hashing for passwords (bcrypt, argon2)
- ✅ Key rotation policy

```java
// Password hashing
@Component
public class PasswordHasher {
  
  private final PasswordEncoder encoder;  // BCryptPasswordEncoder
  
  public String hashPassword(String plaintext) {
    return encoder.encode(plaintext);
  }
  
  public boolean matches(String plaintext, String hash) {
    return encoder.matches(plaintext, hash);
  }
}

// At-rest encryption for sensitive fields
@Entity
public class OAuthClient {
  
  @Convert(converter = EncryptedStringConverter.class)
  private String clientSecret;  // Encrypted in DB
}

// Custom converter
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
  
  private final Cipher cipher;  // AES/GCM
  
  @Override
  public String convertToDatabaseColumn(String attribute) {
    if (attribute == null) return null;
    return cipher.encrypt(attribute);
  }
  
  @Override
  public String convertToEntityAttribute(String dbData) {
    if (dbData == null) return null;
    return cipher.decrypt(dbData);
  }
}
```

**TLS Configuration:**
```yaml
# application-prod.yml
server:
  ssl:
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: keygo
    enabled: true
    
  http2:
    enabled: true
```

### 6. Vulnerable & Outdated Components

**Risk:** Using libraries with known CVEs

**Defenses:**
- ✅ Dependency scanning (Snyk, Dependabot)
- ✅ Regular updates (monthly patches)
- ✅ Monitoring for CVEs in runtime

```bash
# Scan dependencies
./mvnw dependency-check:check

# Check for vulnerable packages
snyk test --severity-threshold=high

# Update all dependencies (carefully)
./mvnw versions:update-properties
```

### 7. Identification & Authentication Failures

**Risk:** Weak session management, token reuse

**Defenses:**
- ✅ Session timeout (15 min idle, 8 hours max)
- ✅ Token binding (IP address, user agent)
- ✅ Logout revokes token

```java
// Session management
@Component
public class SessionManager {
  
  private static final long IDLE_TIMEOUT = Duration.ofMinutes(15).toMillis();
  private static final long MAX_SESSION = Duration.ofHours(8).toMillis();
  
  public boolean isSessionValid(Session session) {
    long now = System.currentTimeMillis();
    
    // Check max age
    if (now - session.getCreatedAt().getTime() > MAX_SESSION) {
      return false;
    }
    
    // Check idle timeout
    if (now - session.getLastActivityAt().getTime() > IDLE_TIMEOUT) {
      return false;
    }
    
    // Check revoked
    if (session.getRevokedAt() != null) {
      return false;
    }
    
    return true;
  }
}
```

### 8. Software & Data Integrity Failures

**Risk:** Compromise during build/deploy, man-in-the-middle

**Defenses:**
- ✅ Sign artifacts (Cosign)
- ✅ Verify checksums
- ✅ Secure supply chain

```bash
# Sign Docker image
cosign sign --key cosign.key \
  ghcr.io/cmartinezs/keygo-server:v1.0.0

# Verify signature before deployment
cosign verify --key cosign.pub \
  ghcr.io/cmartinezs/keygo-server:v1.0.0
```

### 9. Logging & Monitoring Failures

**Risk:** Can't detect attacks, no audit trail

**Defenses:**
- ✅ Audit logs for sensitive operations
- ✅ Alerting on suspicious activity
- ✅ Immutable logs (SIEM)

```java
// Audit log for sensitive operation
@Aspect
@Component
public class AuditLogging {
  
  @Around("@annotation(Auditable)")
  public Object audit(ProceedingJoinPoint joinPoint) throws Throwable {
    String operation = joinPoint.getSignature().getName();
    Object[] args = joinPoint.getArgs();
    
    try {
      Object result = joinPoint.proceed();
      auditLog("SUCCESS", operation, args, result);
      return result;
    } catch (Exception e) {
      auditLog("FAILURE", operation, args, e.getMessage());
      throw e;
    }
  }
  
  private void auditLog(String status, String op, Object[] args, Object result) {
    // Write to immutable log (not modifiable after written)
    // Alert if: failed auth, role change, client secret access, etc.
  }
}
```

### 10. Server-Side Request Forgery (SSRF)

**Risk:** App makes request to attacker-controlled URL

**Defenses:**
- ✅ Whitelist allowed URLs
- ✅ Disable internal network access
- ✅ Validate redirects

```java
// Validate redirect URL
@Component
public class RedirectValidator {
  
  private static final Set<String> ALLOWED_DOMAINS = Set.of(
      "keygo.io",
      "app.keygo.io",
      "console.keygo.io"
  );
  
  public boolean isValidRedirect(String redirectUrl) {
    try {
      URI uri = new URI(redirectUrl);
      String host = uri.getHost();
      
      return ALLOWED_DOMAINS.stream()
          .anyMatch(domain -> host != null && host.endsWith(domain));
    } catch (URISyntaxException e) {
      return false;
    }
  }
}
```

---

## Secrets Management

### Environment Variables

**Never in code:**
```java
// ❌ BAD
private static final String DATABASE_PASSWORD = "super-secret-123";
private static final String JWT_KEY = "my-jwt-key";

// ✅ GOOD
private final String databasePassword = env.getProperty("DB_PASSWORD");
private final String jwtKey = env.getProperty("JWT_SIGNING_KEY");
```

### Vault Integration (HashiCorp Vault)

```yaml
# application-prod.yml
spring:
  cloud:
    vault:
      host: vault.internal.keygo.io
      port: 8200
      scheme: https
      authentication: TOKEN
      token: ${VAULT_TOKEN}
      kv:
        engine-version: 2
        backend-path: secret
```

```java
@Configuration
public class VaultConfig {
  
  @Bean
  public RestTemplate restTemplate(VaultOperations vault) {
    return new RestTemplate();
  }
  
  @Bean
  public DataSource dataSource(VaultOperations vault) {
    VaultResponseSupport<Map<String, Object>> secrets = vault.read("secret/data/db");
    String password = (String) secrets.getData().get("password");
    
    return DataSourceBuilder.create()
        .url(env.getProperty("DB_URL"))
        .username(env.getProperty("DB_USER"))
        .password(password)
        .build();
  }
}
```

### GitHub Secrets for CI/CD

```yaml
# .github/workflows/deploy.yml
env:
  DB_PASSWORD: ${{ secrets.DB_PASSWORD }}
  JWT_KEY: ${{ secrets.JWT_SIGNING_KEY }}
  SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
```

### Secret Rotation

```bash
#!/bin/bash
# scripts/rotate-secrets.sh

# 1. Generate new secret
NEW_SECRET=$(openssl rand -hex 32)

# 2. Update in Vault
vault kv put secret/db password="$NEW_SECRET"

# 3. Update in GitHub Secrets
gh secret set DB_PASSWORD --body "$NEW_SECRET"

# 4. Restart app (triggers config refresh)
kubectl rollout restart deployment/keygo-server
```

---

## Data Protection

### Personal Data (GDPR/CCPA)

**Collect only what's needed:**
```java
// ✅ Minimal data collection
public record CreateUserRequest(
    String email,          // Required
    String username        // Required
    // ❌ NO: phone, address, birth_date unless necessary
) {}
```

**Right to deletion:**
```java
@Service
public class UserDeletionService {
  
  @Transactional
  public void deleteUser(UUID userId, TenantContext context) {
    User user = userRepository.findById(userId)
        .filter(u -> u.getTenantId().equals(context.getTenantId()))
        .orElseThrow();
    
    // Soft delete (GDPR allows if retention is legitimate)
    user.setRemovedAt(Instant.now());
    userRepository.save(user);
    
    // OR hard delete if no legitimate retention
    // userRepository.delete(user);
    
    // Audit
    auditLog("USER_DELETED", userId);
  }
}
```

**Data export (right to access):**
```java
@GetMapping("/api/v1/users/{userId}/export")
public ResponseEntity<byte[]> exportUserData(@PathVariable UUID userId) {
  User user = userRepository.findById(userId).orElseThrow();
  
  // Collect all user data
  Map<String, Object> data = Map.of(
      "user", user,
      "sessions", sessionRepository.findByUserId(userId),
      "audit_logs", auditLogRepository.findByActorId(userId),
      "tokens", oauthClientRepository.findByUserId(userId)
  );
  
  // Export as JSON
  String json = objectMapper.writeValueAsString(data);
  
  return ResponseEntity.ok()
      .header("Content-Disposition", "attachment; filename=user-data.json")
      .body(json.getBytes());
}
```

---

## API Security

### Authentication Headers

```
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
X-API-Key: deprecated (use OAuth2)
X-Trace-ID: 550e8400-e29b-41d4-a716-446655440000
```

### CORS Configuration

```java
@Configuration
public class CorsConfig {
  
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("https://app.keygo.io", "https://console.keygo.io")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("Authorization", "Content-Type", "X-Trace-ID")
            .exposedHeaders("X-RateLimit-Remaining", "X-Trace-ID")
            .allowCredentials(true)
            .maxAge(3600);
      }
    };
  }
}
```

### Content Security Policy

```yaml
server:
  servlet:
    session:
      http-only: true
      secure: true
      same-site: strict

management:
  endpoints:
    web:
      exposure:
        include: health,prometheus  # ⚠️ Never expose all endpoints
```

### Input Validation

```java
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}/users")
public class UserController {
  
  @PostMapping
  public ResponseEntity<?> createUser(
      @Valid @RequestBody CreateUserRequest request,
      @PathVariable String tenantSlug) {
    
    // @Valid triggers validation
    // - @NotBlank on email
    // - @Email on email
    // - @Length on username
  }
}

@Data
public class CreateUserRequest {
  @NotBlank(message = "Email is required")
  @Email(message = "Email format invalid")
  private String email;
  
  @NotBlank(message = "Username is required")
  @Length(min = 3, max = 50, message = "Username must be 3-50 chars")
  @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Username contains invalid chars")
  private String username;
}
```

---

## Compliance & Standards

### Checklist: Security Review

Before deploying to production:

- [ ] **Authentication:** OAuth2 PKCE implemented, no hardcoded credentials
- [ ] **Authorization:** RBAC configured, tenant isolation verified
- [ ] **Data Encryption:** Secrets in Vault, passwords hashed (bcrypt/argon2)
- [ ] **HTTPS:** TLS 1.2+, valid certificate, HSTS header
- [ ] **Input Validation:** All inputs validated, whitelist approach
- [ ] **Logging:** Audit logs for sensitive operations, no PII in logs
- [ ] **Secrets:** No secrets in code, env vars or Vault
- [ ] **Dependencies:** Snyk scan passed, no high/critical CVEs
- [ ] **CORS:** Configured for specific origins, credentials: false for public
- [ ] **Rate Limiting:** Implemented on auth, API endpoints
- [ ] **CSRF:** CSRF tokens on forms, SameSite=Strict on cookies
- [ ] **Headers:** Security headers set (CSP, X-Frame-Options, etc.)

### Security Headers

```java
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {
  
  @Override
  protected void doFilterInternal(HttpServletRequest request, 
      HttpServletResponse response, FilterChain chain) 
      throws ServletException, IOException {
    
    // Prevent clickjacking
    response.addHeader("X-Frame-Options", "DENY");
    
    // Prevent MIME type sniffing
    response.addHeader("X-Content-Type-Options", "nosniff");
    
    // Enable XSS protection
    response.addHeader("X-XSS-Protection", "1; mode=block");
    
    // HSTS (enforce HTTPS for 1 year)
    response.addHeader("Strict-Transport-Security", 
        "max-age=31536000; includeSubDomains; preload");
    
    // CSP: only allow scripts from same origin
    response.addHeader("Content-Security-Policy", 
        "default-src 'self'; script-src 'self' 'nonce-" + getNonce() + "'");
    
    chain.doFilter(request, response);
  }
}
```

### OWASP SAMM Maturity

| Practice | Level | Target |
|---|---|---|
| **Threat Modeling** | 1 (ad-hoc) | 3 (systematic) |
| **Design Review** | 1 (informal) | 2 (formal) |
| **Code Review** | 2 (peer) | 3 (security-focused) |
| **Security Testing** | 1 (manual) | 3 (automated) |
| **Dependency Management** | 2 (periodic) | 3 (continuous) |

---

## PII — Datos Personales Identificables

### Regla: PII nunca en URLs

Los datos personales identificables (PII) **no deben aparecer en URLs** — ni como path variable ni como query param.

**Por qué:** las URLs se registran automáticamente en logs de acceso (Nginx, CDN, load balancers), historial del navegador, headers `Referer` y herramientas de monitoreo de red. El cifrado TLS no protege la URL en esos contextos.

**Aplica a:** email, nombre, teléfono, dirección, número de documento, cualquier identificador que permita individualizar a una persona.

```java
// ❌ MAL — email en URL queda en logs
GET /api/v1/users/check?email=user@example.com

// ✅ BIEN — email en body cifrado por TLS, fuera de logs
POST /api/v1/platform/account/check-email
{ "email": "user@example.com" }
```

> Catálogo completo de PII manejados en el sistema: ver T-131 (pendiente).

### Checklist PII por endpoint

Al diseñar o revisar un endpoint que recibe o devuelve datos de usuario, verificar:

- [ ] Ningún campo PII aparece en el path o query string.
- [ ] Los campos PII en response son los mínimos necesarios (principio de minimización).
- [ ] Los emails en response están enmascarados cuando no es el propio recurso del usuario (usar `EmailMasker`).
- [ ] Los logs no imprime valores PII — solo IDs o datos enmascarados.

---

## Anti-Patterns: Evitar

### ❌ Storing plaintext passwords

```java
// MAL
user.setPassword(plaintext);  // Store raw password
```

### ✅ Hash passwords with bcrypt

```java
// BIEN
String hashed = passwordEncoder.encode(plaintext);
user.setPassword(hashed);
```

---

### ❌ SQL injection risk

```java
// MAL
String query = "SELECT * FROM users WHERE id = " + userId;
```

### ✅ Parameterized queries

```java
// BIEN
userRepository.findById(userId);
```

---

## References

| Resource | URL |
|---|---|
| **OWASP Top 10** | https://owasp.org/www-project-top-ten/ |
| **OWASP SAMM** | https://owaspsam.maturitymodels.dev |
| **Spring Security** | https://spring.io/projects/spring-security |
| **GDPR Rights** | https://gdpr-info.eu/ |
| **HashiCorp Vault** | https://www.vaultproject.io/ |
| **Cosign (artifact signing)** | https://github.com/sigstore/cosign |

---

**Última actualización:** 2026-04-10  
**Estado:** Completado para Sprint 4  
**Próxima revisión:** Cuando se agreguen nuevas features de seguridad
