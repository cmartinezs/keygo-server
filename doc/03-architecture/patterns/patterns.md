# Patterns — Patrones Adoptados en KeyGo

**Propósito:** Guía de patrones arquitectónicos y de dominio usados en el codebase.

---

## 🏛️ Arquitectura: Hexagonal (Ports & Adapters)

**Descripción:** La lógica de dominio vive en `keygo-domain` completamente desacoplada de frameworks. Los puertos (interfaces) definen contratos, y los adaptadores (implementaciones) quedan en `keygo-supabase`.

**Estructura:**
```
keygo-app/
├── {entity}/
│   ├── port/
│   │   ├── {Entity}RepositoryPort.java          ← Interface
│   │   ├── {Entity}ServicePort.java             ← Interface
│   │   └── ...
│   └── usecase/
│       ├── Create{Entity}UseCase.java           ← Business logic
│       ├── List{Entity}UseCase.java
│       └── ...

keygo-supabase/
├── {entity}/
│   ├── adapter/
│   │   ├── {Entity}RepositoryAdapter.java       ← Implementation (JPA)
│   │   └── {Entity}ServiceAdapter.java
│   ├── repository/
│   │   └── {Entity}JpaRepository.java           ← Spring Data interface
│   └── entity/
│       └── {Entity}Entity.java                  ← JPA @Entity

keygo-domain/
├── {entity}/
│   ├── model/
│   │   ├── {Entity}.java                        ← Domain aggregate
│   │   ├── {Entity}Id.java                      ← Value object (ID)
│   │   └── ...
│   └── exception/
│       ├── {Entity}NotFoundException.java
│       └── ...
```

**Ventajas:**
- Domain es testeable sin Spring/BD
- Adaptadores intercambiables (BD, mensaje, HTTP)
- Inversión de dependencias clara

**Ejemplo:**
```java
// Port (keygo-app)
public interface MembershipRepositoryPort {
  Optional<Membership> findById(MembershipId id);
  Membership save(Membership membership);
  PagedResult<Membership> findAllPaged(String tenantSlug, MembershipFilter filter);
}

// Adapter (keygo-supabase)
@Repository
public class MembershipRepositoryAdapter implements MembershipRepositoryPort {
  private final MembershipJpaRepository jpaRepository;
  
  @Override
  public Optional<Membership> findById(MembershipId id) {
    return jpaRepository.findById(id.value())
        .map(MembershipPersistenceMapper::toDomain);
  }
  
  @Override
  public PagedResult<Membership> findAllPaged(String tenantSlug, MembershipFilter filter) {
    Specification<MembershipEntity> spec = buildSpec(tenantSlug, filter);
    Page<MembershipEntity> page = jpaRepository.findAll(spec, buildPageRequest(filter));
    return PagedResult.of(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
  }
}

// Use case (keygo-app)
public class CreateMembershipUseCase {
  private final MembershipRepositoryPort repository;
  
  public Membership execute(CreateMembershipCommand cmd) {
    // Validaciones de dominio
    if (repository.existsByUserAndClientApp(cmd.userId(), cmd.appId())) {
      throw new DuplicateMembershipException();
    }
    // Crear y persistir
    Membership m = Membership.builder()...build();
    return repository.save(m);
  }
}
```

---

## 📄 Paginación: DB-Side con JPA Specifications

**Descripción:** En lugar de traer todos los resultados y paginar en memoria, la paginación ocurre en la BD usando `JpaSpecificationExecutor` y `PageRequest`.

**Estructura:**
```java
// 1. Definir filter con parámetros de búsqueda
public class MembershipFilter {
  private int page = 0;
  private int size = 20;
  private String sortBy = "createdAt";
  private String sortOrder = "DESC";
  private String nameLike;  // Opcional: búsqueda
  
  public boolean hasFilters() { return nameLike != null; }
}

// 2. Adapter construye Specification (dynamic WHERE)
private Specification<MembershipEntity> buildSpecification(String tenantSlug, MembershipFilter filter) {
  return (root, query, cb) -> {
    List<Predicate> predicates = new ArrayList<>();
    
    // Base filter: scope by tenant
    predicates.add(cb.equal(root.get("tenant").get("slug"), tenantSlug));
    
    // Optional: search by name
    if (filter.hasNameLike()) {
      predicates.add(cb.like(
          cb.lower(root.get("displayName")),
          "%" + filter.getNameLike().toLowerCase() + "%"
      ));
    }
    
    return cb.and(predicates.toArray(new Predicate[0]));
  };
}

// 3. Adapter construye PageRequest (pagination + sort)
private PageRequest buildPageRequest(MembershipFilter filter) {
  Sort.Direction direction = Sort.Direction.fromString(filter.getSortOrder());
  return PageRequest.of(filter.getPage(), filter.getSize(), 
      Sort.by(direction, filter.getSortBy()));
}

// 4. Ejecutar con Spring Data
Page<MembershipEntity> page = jpaRepository.findAll(spec, pageRequest);
List<Membership> results = page.getContent().stream()
    .map(MembershipPersistenceMapper::toDomain)
    .toList();

return PagedResult.of(results, page.getNumber(), page.getSize(), page.getTotalElements());
```

**Ventajas:**
- BD optimiza: índices, LIMIT/OFFSET
- Bajo overhead de memoria (no traer 10k records)
- Filtros dinámicos sin custom SQL

**Anti-pattern:**
```java
// ❌ MAL: Todo en memoria
List<MembershipEntity> all = jpaRepository.findAll();
List<Membership> filtered = all.stream()
    .filter(m -> m.getName().contains(search))
    .skip((page - 1) * size)
    .limit(size)
    .map(MembershipPersistenceMapper::toDomain)
    .toList();
// → Trae 1 millón de registros, filtra después, lento
```

---

## 🗑️ Soft-Delete: removed_at con Índice Parcial

**Descripción:** En lugar de eliminar físicamente, marcamos con un timestamp `removed_at` para preservar auditoría. Los índices parciales aseguran que queries sobre datos activos (removed_at IS NULL) sean rápidas.

**Estructura:**
```java
// 1. Entidad con removed_at
@Entity
@Table(name = "tenant_user_roles", indexes = {
    @Index(name = "idx_removed_at", columnList = "removed_at")
})
public class TenantUserRoleEntity {
  @Id private UUID id;
  @Column(name = "removed_at")
  private OffsetDateTime removedAt;  // null = activo
  
  public boolean isActive() {
    return removedAt == null;
  }
}

// 2. En base de datos: índice parcial para queries activos
CREATE INDEX idx_tenant_user_roles_active 
ON tenant_user_roles (tenant_user_id) 
WHERE removed_at IS NULL;

// 3. En repository, siempre filtrar por removed_at IS NULL
public List<TenantUserRole> findByUserId(UUID userId) {
  return jpaRepository.findByUserIdAndRemovedAtIsNull(userId);
}

// 4. Para "eliminar", solo actualizar removed_at
public void revoke(TenantUserRoleId id) {
  TenantUserRoleEntity entity = jpaRepository.findById(id.value())
      .orElseThrow();
  entity.setRemovedAt(OffsetDateTime.now());
  jpaRepository.save(entity);
}
```

**Beneficios:**
- Auditoría: ¿Quién tenía qué roles cuando?
- Recuperación: `removed_at IS NOT NULL` para restore
- Índice parcial: queries en datos activos rápidas (~same cost como deleted = false)

**SQL:**
```sql
-- Datos activos (rápido, usa índice parcial)
SELECT * FROM tenant_user_roles 
WHERE user_id = 'abc-123' AND removed_at IS NULL;

-- Auditoría (lento, intentional)
SELECT * FROM tenant_user_roles 
WHERE user_id = 'abc-123' AND removed_at IS NOT NULL;

-- Recuperar una revocación
UPDATE tenant_user_roles SET removed_at = NULL 
WHERE id = 'xyz-789' AND user_id = 'abc-123';
```

---

## 🏭 Domain Services: Generación y Derivación de Valores

**Descripción:** Cuando la lógica de generación es compleja o reutilizable, creamos helpers o static methods en value objects que actúan como "factories de dominio".

**Patrones:**

### A. Value Object con Static Factory
```java
// Derivar slug a partir de nombre (dominio)
public record TenantSlug(String value) {
  private static final Pattern VALID_SLUG = 
      Pattern.compile("^[a-z0-9]([a-z0-9\\-]*[a-z0-9])?$");
  
  public TenantSlug {
    if (!VALID_SLUG.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid slug format");
    }
  }
  
  // Static factory: genera slug a partir de nombre
  public static TenantSlug fromName(String name) {
    return new TenantSlug(SlugUtils.toSlug(name));
  }
}

// Uso en use case
public Tenant execute(CreateTenantCommand cmd) {
  TenantSlug slug = TenantSlug.fromName(cmd.name());  // ← Genera + valida
  Tenant tenant = Tenant.builder().slug(slug)...build();
  return repository.save(tenant);
}
```

### B. Helper Estático de Dominio
```java
// Validar contraseña según contexto
public final class PasswordValidationHelper {
  public static void validate(String password, boolean isTemporary) {
    if (isTemporary) {
      validateTemporary(password);  // Min 8 chars
    } else {
      validatePermanent(password);  // Min 12 chars + 4 clases
    }
  }
}

// Uso en use case
public User execute(CreateUserCommand cmd) {
  PasswordValidationHelper.validate(cmd.password(), false);  // ← Valida
  User user = User.builder()...build();
  return repository.save(user);
}
```

**Ventaja:** Lógica testeable sin mocks, reutilizable en CLI/eventos/etc.

---

## 📦 Agregados: Raíces y Límites

**Descripción:** Un agregado es un grupo de entidades mantenido como unidad de consistencia. La raíz del agregado expone una interfaz simplificada.

**Ejemplo: Tenant es raíz de agregado**
```java
@Builder
@Getter
public class Tenant {  // ← Agregado raíz
  private TenantId id;
  private TenantSlug slug;
  private String name;
  private TenantStatus status;  // ACTIVE, SUSPENDED
  
  // Métodos de dominio
  public boolean isActive() {
    return status == TenantStatus.ACTIVE;
  }
  
  public boolean isSuspended() {
    return status == TenantStatus.SUSPENDED;
  }
  
  public void suspend() {
    this.status = TenantStatus.SUSPENDED;
  }
}

// Use case: interactúa solo con raíz
public User execute(CreateUserCommand cmd) {
  Tenant tenant = tenantRepo.findBySlug(TenantSlug.of(cmd.tenantSlug()))
      .orElseThrow(() -> new TenantNotFoundException());
  
  if (tenant.isSuspended()) {  // ← Método de dominio
    throw new TenantSuspendedException();
  }
  
  // Crear usuario...
}
```

---

## 🔄 Mappers: Persistencia ↔ Dominio

**Descripción:** Separar capas requiere traducir entre JPA entities y domain models. Los mappers centralizan esta lógica.

**Patrón:**
```java
public class MembershipPersistenceMapper {
  
  // Entity → Domain
  public static Membership toDomain(MembershipEntity entity) {
    return Membership.builder()
        .id(new MembershipId(entity.getId()))
        .userId(entity.getUser().getId())
        .clientAppId(entity.getApp().getId())
        .status(MembershipStatus.valueOf(entity.getStatus()))
        .createdAt(entity.getCreatedAt())
        .build();
  }
  
  // Domain → Entity
  public static MembershipEntity toEntity(Membership domain) {
    MembershipEntity entity = new MembershipEntity();
    entity.setId(domain.getId().value());
    entity.setStatus(domain.getStatus().name());
    // ...
    return entity;
  }
}

// Uso
List<Membership> memberships = page.getContent()
    .stream()
    .map(MembershipPersistenceMapper::toDomain)
    .toList();
```

---

## 🎯 Value Objects: Validación Implicit

**Descripción:** Valores inmutables que encapsulan validación en el constructor. Garantizan que si tienes una instancia, el valor es válido.

**Ejemplos:**

### EmailAddress
```java
public record EmailAddress(String value) {
  public EmailAddress {
    if (!value.matches("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")) {
      throw new IllegalArgumentException("Invalid email: " + value);
    }
  }
  
  public static EmailAddress of(String value) {
    return new EmailAddress(value);
  }
}

// Uso: sempre válido
EmailAddress email = EmailAddress.of("user@example.com");  // OK
EmailAddress invalid = EmailAddress.of("not-an-email");    // Excepción immediata
```

**Beneficio:** Type-safety. `EmailAddress email` garantiza validez sin revisar.

---

## 🚫 Anti-Patterns Evitar

### ❌ Lógica de negocio en Controllers
```java
@PostMapping("/users")
public void create(@RequestBody CreateUserRequest req) {
  // MALO: lógica aquí
  if (userRepository.existsByEmail(req.email())) {
    throw new BadRequestException();
  }
  User user = new User(req.email(), req.name());
  userRepository.save(user);
}
```
→ No reutilizable, acoplado a HTTP

### ✅ Lógica en Use Case
```java
public class CreateUserUseCase {
  public User execute(CreateUserCommand cmd) {
    // Validación de dominio
    EmailAddress email = EmailAddress.of(cmd.email());
    if (userRepository.existsByEmail(email)) {
      throw new DuplicateUserException();
    }
    User user = User.builder().email(email)...build();
    return userRepository.save(user);
  }
}
```

---

### ❌ N+1 Queries en Memory
```java
List<User> users = userRepository.findAll();
for (User user : users) {
  List<Membership> memberships = membershipRepository.findByUserId(user.getId());
  // BD ejecuta query por cada user → 1 + N queries
}
```

### ✅ Eager Loading o Join Explícito
```java
// Opción 1: Especificación con join
Specification<UserEntity> spec = (root, query, cb) -> {
  root.fetch("memberships", JoinType.LEFT);
  return cb.conjunction();
};
List<User> users = userRepository.findAll(spec);

// Opción 2: Query con JOIN FETCH
@Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.memberships")
List<UserEntity> findAllWithMemberships();
```

---

### ❌ Entidades Enormes sin Límites
```java
@Entity
public class User {
  // ... 50 campos
  // ... 20 relaciones
  // BD trae todo aunque solo necesites email
}

User user = userRepository.findById(id);  // SELECT * → overhead
```

### ✅ DTOs Específicos por Caso de Uso
```java
// Entidad pequeña
@Entity
public class User {
  private String username;
  private String email;
  @OneToMany private List<Membership> memberships;
}

// DTO específico si necesitas
public record UserSummaryDto(String username, String email) {}

// Query selectiva
@Query("SELECT new dto.UserSummaryDto(u.username, u.email) FROM User u WHERE u.id = :id")
UserSummaryDto findSummary(@Param("id") UUID id);
```

---

### ❌ Transacciones muy largas
```java
@Transactional  // ← Desde inicio hasta fin
public void processLargeBatch() {
  List<User> users = userRepository.findAll();  // BD abierta
  for (User user : users) {
    emailService.send(user.getEmail());  // HTTP call dentro de TX
    externalApi.notify(user.getId());    // Más HTTP
  }
  // 30 segundos con BD open → bloquea otros
}
```

### ✅ Transacciones acotadas
```java
@Transactional(readOnly = true)
public List<User> getUsers() {
  return userRepository.findAll();
}

public void processLargeBatch() {
  List<User> users = getUsers();  // TX termina
  for (User user : users) {
    emailService.send(user.getEmail());  // Sin TX
    externalApi.notify(user.getId());    // Sin TX
  }
}
```

---

## 📋 Checklist: Nuevo Patrón

Si identificas un patrón reutilizable en el code:

- [ ] ¿Es específico del dominio o framework-agnostic?
- [ ] ¿Se repite en 3+ lugares?
- [ ] ¿Lo pueden usar use cases sin cambio?
- [ ] ¿Está documentado dónde y cómo se usa?
- [ ] ¿Hay ejemplo de código?

Si sí a todos → considerar agregar a esta guía.

---

## Referencias

| Patrón | Ubicación |
|---|---|
| Puertos | `keygo-app/{entity}/port/` |
| Adaptadores | `keygo-supabase/{entity}/adapter/` |
| Use Cases | `keygo-app/{entity}/usecase/` |
| Dominio | `keygo-domain/{entity}/model/` |
| Excepciones | `keygo-domain/{entity}/exception/` |
| Mappers | `keygo-supabase/{entity}/mapper/` |

---

**Última actualización:** 2026-04-10  
**Estado:** Completado para Sprint 1

