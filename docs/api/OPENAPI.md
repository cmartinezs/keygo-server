# OpenAPI / Swagger UI — KeyGo Server

> **Última actualización:** 2026-03-22  
> Librería: `springdoc-openapi-starter-webmvc-ui:3.0.1` (compatible Spring Boot 4.x)  
> Configuración: `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/OpenApiConfig.java`

---

## 1. URLs

| Recurso | URL local |
|---|---|
| **Swagger UI** | `http://localhost:8080/keygo-server/swagger-ui/index.html` |
| **OpenAPI JSON spec** | `http://localhost:8080/keygo-server/v3/api-docs` |
| **OpenAPI por grupo** | `http://localhost:8080/keygo-server/v3/api-docs/{group}` |

> Ambas rutas son **públicas** — el `BootstrapAdminKeyFilter` las excluye del header `X-KEYGO-ADMIN`.

---

## 2. Grupos de API disponibles

La UI organiza los endpoints en grupos por funcionalidad:

| Grupo (ID) | Display name | Path pattern |
|---|---|---|
| `1-platform` | 🌐 Platform | `/api/v1/service/**`, `/api/v1/response-codes/**` |
| `2-tenants` | 🏢 Tenants | `/api/v1/tenants/**` (excluye apps y users) |
| `3-client-apps` | 📦 Client Apps | `/api/v1/tenants/*/apps/**` |
| `4-users` | 👤 Users | `/api/v1/tenants/*/users/**` |

Seleccionar el grupo en el dropdown superior derecho de la Swagger UI.

---

## 3. Autenticación en la UI

Los endpoints protegidos requieren el header `X-KEYGO-ADMIN`. Para autenticarse en la UI:

1. Click en el botón **Authorize 🔒** (esquina superior derecha de la UI)
2. En el campo **AdminKeyAuth (apiKey)**, ingresar el valor de `KEYGO_ADMIN_KEY` (default dev: `changeMe`)
3. Click **Authorize** → **Close**

La UI enviará automáticamente `X-KEYGO-ADMIN: <valor>` en todas las requests subsecuentes.

### Endpoints públicos (sin autenticación)

- `GET /actuator/health`
- `GET /api/v1/tenants/{slug}/.well-known/openid-configuration`
- `GET /api/v1/tenants/{slug}/.well-known/jwks.json`

Estos endpoints no tienen `@SecurityRequirement` en su controller — aparecen sin candado en la UI.

---

## 4. Configuración (OpenApiConfig.java)

```java
// keygo-run/src/main/java/io/cmartinezs/keygo/run/config/OpenApiConfig.java

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI keyGoOpenAPI(KeyGoBootstrapProperties bootstrapProperties) {
    // Security scheme: API Key en header X-KEYGO-ADMIN
    SecurityScheme adminKeyScheme = new SecurityScheme()
        .type(SecurityScheme.Type.APIKEY)
        .in(SecurityScheme.In.HEADER)
        .name("X-KEYGO-ADMIN")
        .description("Admin API key — default dev: `changeMe`. Set via KEYGO_ADMIN_KEY.");

    return new OpenAPI()
        .info(new Info()
            .title("KeyGo Server API")
            .version("1.0")
            .description("Enterprise authentication service — IAM open source"))
        .components(new Components()
            .addSecuritySchemes("AdminKeyAuth", adminKeyScheme));
  }

  @Bean public GroupedOpenApi platformGroup()   { /* /api/v1/service/**, /api/v1/response-codes/** */ }
  @Bean public GroupedOpenApi tenantsGroup()    { /* /api/v1/tenants/** excl. apps y users */ }
  @Bean public GroupedOpenApi clientAppsGroup() { /* /api/v1/tenants/*/apps/** */ }
  @Bean public GroupedOpenApi usersGroup()      { /* /api/v1/tenants/*/users/** */ }
}
```

---

## 5. Anotaciones en controllers

### Proteger un controller (requiere X-KEYGO-ADMIN)

```java
@RestController
@RequestMapping("/api/v1/tenants")
@Tag(name = "Tenants", description = "Tenant management — requires X-KEYGO-ADMIN")
@SecurityRequirement(name = "AdminKeyAuth")   // ← aplica a TODOS los métodos del controller
public class TenantController { ... }
```

### Endpoint público (sin autenticación en la UI)

```java
@RestController
@RequestMapping("/api/v1/tenants/{slug}/.well-known")
@Tag(name = "OIDC Discovery", description = "Public OIDC metadata endpoints")
// SIN @SecurityRequirement — aparece sin candado en la UI
public class OidcDiscoveryController { ... }
```

### Documentar un endpoint

```java
@Operation(
    summary = "Get tenant by slug",
    description = "Retrieves a tenant by its unique slug identifier.")
@ApiResponse(responseCode = "200", description = "Tenant found",
    content = @Content(schema = @Schema(implementation = BaseResponse.class)))
@ApiResponse(responseCode = "404", description = "Tenant not found")
@GetMapping("/{slug}")
public ResponseEntity<BaseResponse<TenantData>> getTenant(@PathVariable String slug) { ... }
```

> ⚠️ **Nota:** `@SecurityRequirementsOptional` **no existe** en `swagger-annotations-jakarta`.
> Para marcar un endpoint como público, simplemente no poner `@SecurityRequirement` en ese método/clase.

---

## 6. application.yml — configuración springdoc

```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui/index.html
    display-request-duration: true
    operations-sorter: method
  api-docs:
    path: /v3/api-docs
```

> Si no se configura explícitamente, springdoc usa los paths por defecto (`/swagger-ui/index.html` y `/v3/api-docs`).

---

## 7. Dependencia Maven

```xml
<!-- keygo-api/pom.xml -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <!-- versión gestionada en pom.xml raíz: ${springdoc.version} = 3.0.1 -->
</dependency>
```

> **Importante:** usar `springdoc-openapi-starter-webmvc-ui:3.0.1` para Spring Boot 4.x.
> SpringDoc 2.x es para Spring Boot 3.x y no es compatible con Spring 7 / Jackson 3.

---

## 8. Verificación rápida

```bash
# 1. Arrancar el servidor
./mvnw spring-boot:run -pl keygo-run

# 2. Verificar que la UI responde
curl -s http://localhost:8080/keygo-server/swagger-ui/index.html | grep -o "<title>.*</title>"

# 3. Verificar que el JSON spec responde
curl -s http://localhost:8080/keygo-server/v3/api-docs | python3 -m json.tool | head -20
```

---

## 9. Endpoints documentados (resumen)

| Grupo | Método | Path | Auth |
|---|---|---|---|
| Platform | GET | `/api/v1/service/info` | 🔒 |
| Platform | GET | `/api/v1/response-codes` | 🔒 |
| Tenants | POST | `/api/v1/tenants` | 🔒 |
| Tenants | GET | `/api/v1/tenants/{slug}` | 🔒 |
| Tenants | PUT | `/api/v1/tenants/{slug}/suspend` | 🔒 |
| Tenants | GET | `/api/v1/tenants/{slug}/.well-known/openid-configuration` | 🌐 público |
| Tenants | GET | `/api/v1/tenants/{slug}/.well-known/jwks.json` | 🌐 público |
| Tenants | GET | `/api/v1/tenants/{slug}/oauth2/authorize` | 🌐 público |
| Tenants | POST | `/api/v1/tenants/{slug}/account/login` | 🌐 público |
| Tenants | POST | `/api/v1/tenants/{slug}/oauth2/token` | 🌐 público |
| Client Apps | POST | `/api/v1/tenants/{slug}/apps` | 🔒 |
| Client Apps | GET | `/api/v1/tenants/{slug}/apps` | 🔒 |
| Client Apps | GET | `/api/v1/tenants/{slug}/apps/{clientId}` | 🔒 |
| Client Apps | PUT | `/api/v1/tenants/{slug}/apps/{clientId}` | 🔒 |
| Client Apps | POST | `/api/v1/tenants/{slug}/apps/{clientId}/rotate-secret` | 🔒 |
| Users | POST | `/api/v1/tenants/{slug}/users` | 🔒 |
| Users | GET | `/api/v1/tenants/{slug}/users` | 🔒 |
| Users | GET | `/api/v1/tenants/{slug}/users/{userId}` | 🔒 |
| Users | PUT | `/api/v1/tenants/{slug}/users/{userId}` | 🔒 |
| Users | POST | `/api/v1/tenants/{slug}/users/{userId}/reset-password` | 🔒 |
| Users | POST | `/api/v1/tenants/{slug}/users/validate-credentials` | 🔒 |

---

## Referencias

- [SpringDoc OpenAPI 3](https://springdoc.org/)
- [Swagger UI docs](https://swagger.io/tools/swagger-ui/)
- [`docs/api/RESPONSE_CODES.md`](RESPONSE_CODES.md) — Catálogo de ResponseCode
- [`docs/api/BOOTSTRAP_FILTER.md`](BOOTSTRAP_FILTER.md) — Seguridad de las rutas

