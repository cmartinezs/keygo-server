# Estrategia de Testing — KeyGo Server

> **Última actualización:** 2026-03-22  
> Fusiona: `TESTING_GUIDE.md` (guía Postman + configuración) y `TEST_DEPENDENCIES_STRATEGY.md` (estrategia Maven)

---

## 1. Tipos de tests y herramientas

| Tipo | Módulos | Herramientas | Notas |
|---|---|---|---|
| **Unit** | domain, app, api, run, infra | JUnit 5 + AssertJ + Mockito | Sin Spring context |
| **Integration** | supabase | Testcontainers PostgreSQL | TC JDBC URL: `jdbc:tc:postgresql:15-alpine:///testdb` |
| **API/smoke** | postman/ | Postman collection + pm.test() | Ver colección en `postman/` |

### Convención: Given / When / Then

Todo método de test debe tener comentarios `// Given`, `// When`, `// Then`:

```java
@Test
void shouldReturnUnauthorized_whenAdminKeyIsMissing() {
    // Given
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setServletPath("/api/v1/tenants");
    // adminKey header NOT set

    // When
    boolean result = filter.shouldBlock(request);

    // Then
    assertThat(result).isTrue();
}
```

### No usar Spring context en tests unitarios

```java
// ✅ Correcto — sin Spring
@ExtendWith(MockitoExtension.class)
class MyUseCaseTest { ... }

// ❌ Evitar en unit tests
@SpringBootTest
class MyUseCaseTest { ... }
```

---

## 2. Comandos

```bash
# Todos los módulos
./mvnw test

# Módulo específico
./mvnw -pl keygo-api test
./mvnw -pl keygo-supabase test
./mvnw -pl keygo-run test

# Con reporte de cobertura
./mvnw verify               # ejecuta tests + JaCoCo

# Build completo (sin tests)
./mvnw clean package -DskipTests
```

---

## 3. Cobertura de tests actual

| Clase | Módulo | Tests |
|---|---|---|
| `BootstrapAdminKeyFilterTest` | keygo-run | 13 |
| `KeyGoBootstrapPropertiesTest` | keygo-run | 18 |
| `ApplicationConfigTest` | keygo-run | 4 |
| `ServiceInfoPropertiesTest` | keygo-run | 8 |
| `GlobalExceptionHandlerTest` | keygo-api | 6 |
| `ResponseCodeControllerTest` | keygo-api | 7 |
| `ServiceInfoControllerTest` | keygo-api | 4 |
| `ResponseCodeTest` | keygo-api | 7 |
| `ResponseHelperTest` | keygo-api | 5 |
| `UnauthorizedExceptionTest` | keygo-api | 4 |

> Subtotal: **76 tests** — keygo-api (33) + keygo-run (43)

---

## 4. Tests de integración con Testcontainers

Configuración en `keygo-supabase/src/test/resources/application-test.yml`:

```yaml
spring:
  datasource:
    url: "jdbc:tc:postgresql:15-alpine:///testdb"
  flyway:
    enabled: true
```

> ⚠️ **Nota:** Los tests de integración con Testcontainers están configurados pero aún no hay
> casos escritos. `UserRepositoryTest` es un test unitario puro que usa el builder de Lombok.

### Activar perfil de test

```java
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class MyIntegrationTest { ... }
```

---

## 5. Tests con Postman

La colección `docs/postman/KeyGo-Server.postman_collection.json` contiene requests con `pm.test()` para:
- Smoke test del servidor (`GET /actuator/health`)
- Endpoints de plataforma (`GET /api/v1/service/info`, `GET /api/v1/response-codes`)
- Flujo completo de Tenant: crear → obtener → suspender
- Flujo de ClientApp: crear → obtener → actualizar → rotar secret
- Escenarios de error (401, 404, 400)

```bash
# Importar en Postman:
# Collection: docs/postman/KeyGo-Server.postman_collection.json
# Environment: docs/postman/KeyGo-Server-Local.postman_environment.json
```

**Variables del entorno Postman:**

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `baseUrl` | `http://localhost:8080` | URL base del servidor |
| `contextPath` | `/keygo-server` | Context path activo |
| `adminKey` | `changeMe` | Valor del header `X-KEYGO-ADMIN` |

---

## 6. Estrategia de dependencias Maven para tests

### Regla clave: `test` scope NO es transitivo

```
keygo-common
  └─ JUnit (scope=test)    ← NO se propaga a módulos que dependan de keygo-common
```

**Por eso cada módulo declara sus propias dependencias de test** (sin versión, que la gestiona `spring-boot-starter-parent`):

```xml
<!-- En cualquier módulo que necesite tests -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
    <!-- Sin <version> — gestionada por spring-boot-starter-parent -->
</dependency>
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
```

### Tabla de propagación de scopes Maven

| Scope | Compilación | Runtime | Test | Transitivo |
|---|---|---|---|---|
| `compile` | ✅ | ✅ | ✅ | ✅ Sí |
| `provided` | ✅ | ❌ | ✅ | ❌ No |
| `runtime` | ❌ | ✅ | ✅ | ✅ Sí |
| **`test`** | ❌ | ❌ | ✅ | **❌ No** |

### Versiones gestionadas por Spring Boot parent

Como el proyecto hereda de `spring-boot-starter-parent`, ya existe `<dependencyManagement>` con:
- JUnit Jupiter
- Mockito
- AssertJ
- Testcontainers

**No es necesario declarar versiones en el POM padre ni en `keygo-bom`** para estas dependencias.

---

## 7. Configuración de tests en keygo-run (filtro)

Para tests del `BootstrapAdminKeyFilter`, desactivar el filtro en `application-test.yml`:

```yaml
keygo:
  bootstrap:
    enabled: false
```

Esto desactiva tanto el filtro como la validación de `@AssertTrue` (que fallaría si `adminKey` es null y `enabled=true`).

### Usar `setServletPath()` (no `setRequestURI()`)

```java
// ✅ Correcto — simula comportamiento real con context-path
MockHttpServletRequest request = new MockHttpServletRequest();
request.setServletPath("/api/v1/tenants");   // → sin "/keygo-server/"

// ❌ Incorrecto — incluye context-path, no coincide con prefijos del filtro
request.setRequestURI("/keygo-server/api/v1/tenants");
```

---

## Referencias

- [JUnit 5](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ](https://assertj.github.io/doc/)
- [Mockito](https://site.mockito.org/)
- [Testcontainers PostgreSQL](https://java.testcontainers.org/modules/databases/postgres/)
- Colección Postman: `docs/postman/KeyGo-Server.postman_collection.json`

