# OpenAPI / Swagger UI — KeyGo Server

> **Última actualización:** 2026-03-26  
> Librería: `springdoc-openapi-starter-webmvc-ui:3.0.1` (compatible Spring Boot 4.x)  
> Configuración: `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/OpenApiConfig.java`

---

## 1. URLs

| Recurso | URL local |
|---|---|
| **Swagger UI** | `http://localhost:8080/keygo-server/swagger-ui/index.html` |
| **OpenAPI JSON spec** | `http://localhost:8080/keygo-server/v3/api-docs` |
| **OpenAPI por grupo** | `http://localhost:8080/keygo-server/v3/api-docs/{group}` |

> Ambas rutas son **públicas** — el `BootstrapAdminKeyFilter` las excluye de la verificación de Bearer token.

---

## 2. Grupos de API disponibles

La UI organiza los endpoints en grupos por funcionalidad:

| Grupo (ID) | Display name | Path pattern incluido | Path pattern excluido |
|---|---|---|---|
| `1-platform` | 🏠 Platform | `/api/v1/service/**`, `/api/v1/response-codes/**` | — |
| `2-tenants` | 🏢 Tenants | `/api/v1/tenants/**` | `apps/**`, `users/**`, `.well-known/**`, `oauth2/**`, `account/**`, `userinfo`, `memberships/**` |
| `3-client-apps` | 📦 Client Apps | `/api/v1/tenants/*/apps/**` | — |
| `4-users` | 👤 Users | `/api/v1/tenants/*/users/**`, `/api/v1/tenants/*/account/profile` | — |
| `5-oauth2` | 🔐 OAuth2 / OIDC | `/api/v1/tenants/*/oauth2/**`, `/api/v1/tenants/*/account/login`, `/api/v1/tenants/*/userinfo`, `/api/v1/tenants/*/.well-known/**` | — |
| `6-memberships` | 🔗 Memberships | `/api/v1/tenants/*/memberships/**` | — |

Seleccionar el grupo en el dropdown superior derecho de la Swagger UI.

> **Nota:** Los endpoints bajo `3-client-apps` incluyen también los de auto-registro (`/register`, `/verify-email`, `/resend-verification`) y los de roles de app (`/apps/{clientAppId}/roles`).

---

## 3. Autenticación en la UI

La mayoría de endpoints protegidos requieren un **Bearer JWT** emitido por el propio KeyGo. Para autenticarse en la UI:

1. Obtener un token llamando a `POST /api/v1/tenants/{slug}/oauth2/token`
   (o vía el flujo completo `GET /oauth2/authorize` → `POST /account/login` → `POST /oauth2/token`).
2. Click en el botón **Authorize 🔒** (esquina superior derecha de la UI).
3. En el campo **BearerAuth**, ingresar el `access_token` obtenido.
4. Click **Authorize** → **Close**.

La UI enviará automáticamente `Authorization: Bearer <token>` en todas las requests subsecuentes.

### Endpoints públicos (sin autenticación)

Los siguientes endpoints **no requieren** ningún token:

| Endpoint | Nota |
|---|---|
| `GET /api/v1/service/info` | Info del servicio |
| `GET /api/v1/response-codes` | Catálogo de códigos |
| `GET /actuator/health` | Health check |
| `GET /api/v1/tenants/{slug}/.well-known/openid-configuration` | OIDC discovery |
| `GET /api/v1/tenants/{slug}/.well-known/jwks.json` | JWKS |
| `GET /api/v1/tenants/{slug}/oauth2/authorize` | Iniciar auth flow |
| `POST /api/v1/tenants/{slug}/account/login` | Enviar credenciales |
| `POST /api/v1/tenants/{slug}/oauth2/token` | Intercambiar código / rotar token |
| `POST /api/v1/tenants/{slug}/oauth2/revoke` | Revocar token |
| `POST /api/v1/tenants/{slug}/apps/{clientId}/register` | Auto-registro |
| `POST /api/v1/tenants/{slug}/apps/{clientId}/verify-email` | Verificar email |
| `POST /api/v1/tenants/{slug}/apps/{clientId}/resend-verification` | Reenviar código |

Los siguientes requieren **Bearer token propio del usuario** (no `X-KEYGO-ADMIN`):

| Endpoint | Nota |
|---|---|
| `GET /api/v1/tenants/{slug}/userinfo` | Claims OIDC del usuario autenticado |
| `GET /api/v1/tenants/{slug}/account/profile` | Perfil propio |
| `PATCH /api/v1/tenants/{slug}/account/profile` | Editar perfil propio |

---

## 4. Configuración (`OpenApiConfig.java`)

```java
// keygo-run/src/main/java/io/cmartinezs/keygo/run/config/OpenApiConfig.java

@Configuration
public class OpenApiConfig {

  private static final String SECURITY_SCHEME_NAME = "BearerAuth";

  @Bean
  public OpenAPI keyGoOpenAPI(KeyGoBootstrapProperties bootstrapProperties) {
    // Security scheme: Bearer JWT
    SecurityScheme bearerScheme = new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .description("Bearer JWT token issued by KeyGo OAuth2/OIDC endpoints.");

    return new OpenAPI()
        .info(new Info()
            .title("KeyGo Server API")
            .version("1.0")
            .description("Enterprise authentication service — IAM open source")
            .contact(new Contact().name("KeyGo Server").url("https://github.com/cmartinezs/keygo-server"))
            .license(new License().name("AGPL-3.0")))
        .components(new Components()
            .addSecuritySchemes(SECURITY_SCHEME_NAME, bearerScheme));
  }

  @Bean public GroupedOpenApi platformGroup()     { /* 1-platform */ }
  @Bean public GroupedOpenApi tenantsGroup()      { /* 2-tenants (CRUD sólo) */ }
  @Bean public GroupedOpenApi clientAppsGroup()   { /* 3-client-apps + registro + roles */ }
  @Bean public GroupedOpenApi usersGroup()        { /* 4-users + account/profile */ }
  @Bean public GroupedOpenApi oauth2Group()       { /* 5-oauth2 + OIDC discovery */ }
  @Bean public GroupedOpenApi membershipsGroup()  { /* 6-memberships */ }
}
```

---

## 5. Tags en controllers (convención)

| Tag name | Controller(s) | Tipo auth |
|---|---|---|
| `Platform` | `ServiceInfoController`, `ResponseCodeController` | 🌐 público |
| `Tenants` | `PlatformTenantController` | 🔒 Bearer `ADMIN` |
| `Client Apps` | `TenantClientAppController` | 🔒 Bearer `ADMIN` / `ADMIN_TENANT` |
| `Registration` | `RegistrationController` | 🌐 público |
| `Users` | `TenantUserController` | 🔒 Bearer `ADMIN` / `ADMIN_TENANT` |
| `Account Profile` | `AccountProfileController` | 🌐 Bearer usuario propio |
| `OAuth2 / Authorization` | `AuthorizationController`, `RevocationController` | 🌐 público |
| `OIDC Discovery` | `JwksController`, `OidcMetadataController`, `UserInfoController` | 🌐 público / Bearer usuario |
| `Memberships` | `TenantMembershipController` | 🔒 Bearer `ADMIN` / `ADMIN_TENANT` |
| `App Roles` | `TenantAppRoleController` | 🔒 Bearer `ADMIN` / `ADMIN_TENANT` |

### Proteger un controller (requiere Bearer JWT)

```java
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}")
@Tag(name = "Tenants", description = "Tenant management — requires Bearer JWT")
@SecurityRequirement(name = "BearerAuth")   // ← aplica a TODOS los métodos del controller
@PreAuthorize("hasRole('ADMIN')")
public class PlatformTenantController { ... }
```

### Endpoint público (sin autenticación en la UI)

```java
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}")
@Tag(name = "OAuth2 / Authorization", description = "Public OAuth2 endpoints")
// SIN @SecurityRequirement — aparece sin candado en la UI
public class AuthorizationController { ... }
```

### Endpoint público con Bearer del usuario (no admin)

```java
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}/account/profile")
@Tag(name = "Account Profile", description = "Self-service profile — requires Bearer token (no X-KEYGO-ADMIN)")
// SIN @SecurityRequirement a nivel de clase — Bearer validado manualmente en el método
public class AccountProfileController { ... }
```

> ⚠️ **Nota:** `@SecurityRequirementsOptional` **no existe** en `swagger-annotations-jakarta`.
> Para marcar un endpoint como público, simplemente no poner `@SecurityRequirement` en ese método/clase.

### Retorno nativo (no `BaseResponse`) — OIDC / JWKS

`JwksController` y `OidcMetadataController` retornan `Map<String, Object>` directamente,
**no** envuelto en `BaseResponse<T>`. Esto es intencional para garantizar interoperabilidad
con librerías OAuth2/OIDC de terceros que consumen estos endpoints directamente.

---

## 6. `application.yml` — configuración springdoc

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: alpha
    tags-sorter: alpha
    display-request-duration: true
  show-actuator: false
```

> La Swagger UI queda accesible en `/keygo-server/swagger-ui/index.html`
> (Spring redirige desde `/swagger-ui.html` al path canónico de la UI).

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

# 4. Verificar grupos disponibles
curl -s http://localhost:8080/keygo-server/v3/api-docs/swagger-config | python3 -m json.tool
```

---

## 9. Endpoints documentados (resumen completo)

> 🔒 = Bearer JWT requerido (emitido por KeyGo) | 🌐 = público | 🌐🔑 = público, pero requiere Bearer del usuario

### Platform

| Método | Path | Auth | ResponseCode |
|---|---|---|---|
| `GET` | `/api/v1/service/info` | 🌐 | `SERVICE_INFO_RETRIEVED` |
| `GET` | `/api/v1/response-codes` | 🌐 | `RESPONSE_CODES_RETRIEVED` |

### Tenants

| Método | Path | Auth | ResponseCode |
|---|---|---|---|
| `POST` | `/api/v1/tenants` | 🔒 `ADMIN` | `TENANT_CREATED` |
| `GET` | `/api/v1/tenants/{slug}` | 🔒 `ADMIN` | `TENANT_RETRIEVED` |
| `PUT` | `/api/v1/tenants/{slug}/suspend` | 🔒 `ADMIN` | `TENANT_SUSPENDED` |

### Client Apps

| Método | Path | Auth | ResponseCode |
|---|---|---|---|
| `POST` | `/api/v1/tenants/{slug}/apps` | 🔒 `ADMIN`/`ADMIN_TENANT` | `CLIENT_APP_CREATED` |
| `GET` | `/api/v1/tenants/{slug}/apps` | 🔒 `ADMIN`/`ADMIN_TENANT` | `CLIENT_APP_LIST_RETRIEVED` |
| `GET` | `/api/v1/tenants/{slug}/apps/{clientId}` | 🔒 `ADMIN`/`ADMIN_TENANT` | `CLIENT_APP_RETRIEVED` |
| `PUT` | `/api/v1/tenants/{slug}/apps/{clientId}` | 🔒 `ADMIN`/`ADMIN_TENANT` | `CLIENT_APP_UPDATED` |
| `POST` | `/api/v1/tenants/{slug}/apps/{clientId}/rotate-secret` | 🔒 `ADMIN`/`ADMIN_TENANT` | `CLIENT_SECRET_ROTATED` |
| `POST` | `/api/v1/tenants/{slug}/apps/{clientId}/register` | 🌐 | `USER_REGISTERED` |
| `POST` | `/api/v1/tenants/{slug}/apps/{clientId}/verify-email` | 🌐 | `EMAIL_VERIFIED` |
| `POST` | `/api/v1/tenants/{slug}/apps/{clientId}/resend-verification` | 🌐 | `EMAIL_VERIFICATION_RESENT` |

### App Roles *(bajo Client Apps)*

| Método | Path | Auth | ResponseCode |
|---|---|---|---|
| `POST` | `/api/v1/tenants/{slug}/apps/{clientAppId}/roles` | 🔒 `ADMIN`/`ADMIN_TENANT` | `ROLE_CREATED` |
| `GET` | `/api/v1/tenants/{slug}/apps/{clientAppId}/roles` | 🔒 `ADMIN`/`ADMIN_TENANT` | `ROLE_LIST_RETRIEVED` |

### Users

| Método | Path | Auth | ResponseCode |
|---|---|---|---|
| `POST` | `/api/v1/tenants/{slug}/users` | 🔒 `ADMIN`/`ADMIN_TENANT` | `USER_CREATED` |
| `GET` | `/api/v1/tenants/{slug}/users` | 🔒 `ADMIN`/`ADMIN_TENANT` | `USER_LIST_RETRIEVED` |
| `GET` | `/api/v1/tenants/{slug}/users/{userId}` | 🔒 `ADMIN`/`ADMIN_TENANT` | `USER_RETRIEVED` |
| `PUT` | `/api/v1/tenants/{slug}/users/{userId}` | 🔒 `ADMIN`/`ADMIN_TENANT` | `USER_UPDATED` |
| `POST` | `/api/v1/tenants/{slug}/users/{userId}/reset-password` | 🔒 `ADMIN`/`ADMIN_TENANT` | `USER_PASSWORD_RESET` |
| `POST` | `/api/v1/tenants/{slug}/users/validate-credentials` | 🔒 `ADMIN`/`ADMIN_TENANT` | `CREDENTIALS_VALID` |
| `GET` | `/api/v1/tenants/{slug}/account/profile` | 🌐🔑 Bearer propio | `USER_PROFILE_RETRIEVED` |
| `PATCH` | `/api/v1/tenants/{slug}/account/profile` | 🌐🔑 Bearer propio | `USER_PROFILE_UPDATED` |

### Memberships

| Método | Path | Auth | ResponseCode |
|---|---|---|---|
| `POST` | `/api/v1/tenants/{slug}/memberships` | 🔒 `ADMIN`/`ADMIN_TENANT` | `MEMBERSHIP_CREATED` |
| `GET` | `/api/v1/tenants/{slug}/memberships` | 🔒 `ADMIN`/`ADMIN_TENANT` | `MEMBERSHIP_LIST_RETRIEVED` |
| `DELETE` | `/api/v1/tenants/{slug}/memberships/{membershipId}` | 🔒 `ADMIN`/`ADMIN_TENANT` | `MEMBERSHIP_REVOKED` |

### OAuth2 / OIDC

| Método | Path | Auth | ResponseCode / Formato |
|---|---|---|---|
| `GET` | `/api/v1/tenants/{slug}/oauth2/authorize` | 🌐 | `AUTHORIZATION_INITIATED` |
| `POST` | `/api/v1/tenants/{slug}/account/login` | 🌐 | `LOGIN_SUCCESSFUL` |
| `POST` | `/api/v1/tenants/{slug}/oauth2/token` (`authorization_code`) | 🌐 | `TOKEN_ISSUED` |
| `POST` | `/api/v1/tenants/{slug}/oauth2/token` (`refresh_token`) | 🌐 | `REFRESH_TOKEN_ROTATED` |
| `POST` | `/api/v1/tenants/{slug}/oauth2/token` (`client_credentials`) | 🌐 | `CLIENT_CREDENTIALS_TOKEN_ISSUED` |
| `POST` | `/api/v1/tenants/{slug}/oauth2/revoke` | 🌐 | `TOKEN_REVOKED` |
| `GET` | `/api/v1/tenants/{slug}/userinfo` | 🌐🔑 Bearer propio | `USER_INFO_RETRIEVED` |
| `GET` | `/api/v1/tenants/{slug}/.well-known/openid-configuration` | 🌐 | Raw JSON (OIDC Discovery 1.0) |
| `GET` | `/api/v1/tenants/{slug}/.well-known/jwks.json` | 🌐 | Raw JSON `{"keys":[...]}` |

---

## Referencias

- [SpringDoc OpenAPI 3](https://springdoc.org/)
- [Swagger UI docs](https://swagger.io/tools/swagger-ui/)
- [`docs/api/RESPONSE_CODES.md`](RESPONSE_CODES.md) — Catálogo de ResponseCode
- [`docs/api/BOOTSTRAP_FILTER.md`](BOOTSTRAP_FILTER.md) — Seguridad de las rutas
- [`docs/api/AUTH_FLOW.md`](AUTH_FLOW.md) — Flujo OAuth2/OIDC detallado
