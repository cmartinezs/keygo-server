# Catálogo de Errores — ResponseCode y ErrorData

**Propósito:** Referencia completa de códigos de error, estructura de respuesta y reglas de uso.

---

## Estructura de Respuesta Error

```json
{
  "code": "INVALID_INPUT",
  "origin": "CLIENT_REQUEST",
  "layer": "api",
  "clientMessage": "El valor del email es inválido",
  "traceId": "abc-123-def",
  "fieldErrors": [
    {
      "field": "email",
      "message": "must be a valid email address",
      "rejectedValue": "notanemail"
    }
  ]
}
```

**Campos:**
- `code` → ResponseCode enum (90 values)
- `origin` → CLIENT_REQUEST | BUSINESS_RULE | SERVER_PROCESSING
- `layer` → "api" | "app" | "domain" | "infra"
- `clientMessage` → Localizado per Accept-Language (i18n)
- `traceId` → MDC, correlaciona con server logs
- `fieldErrors` → Solo si INVALID_INPUT + USER_INPUT validation
- `detail` / `exception` → Solo en dev (nunca en prod)

---

## Mapa de HTTP Status × ResponseCode

| HTTP | Categoría | ResponseCode Típico | Cuándo |
|---|---|---|---|
| **200** | Éxito | RESOURCE_CREATED, OPERATION_COMPLETED, etc. | Operación exitosa |
| **400** | Entrada inválida | INVALID_INPUT | Params/body inválido |
| **401** | Autenticación | AUTHENTICATION_REQUIRED | Token falta/invalido |
| **403** | Autorización | INSUFFICIENT_PERMISSIONS | Token válido, sin permisos |
| **404** | No encontrado | RESOURCE_NOT_FOUND | ID no existe |
| **409** | Conflicto | DUPLICATE_RESOURCE, CONTRACT_INVALID_STATE | Estado/constraint violation |
| **500** | Error interno | OPERATION_FAILED, DATABASE_ERROR | Excepción no mapeada |

---

## Catálogo Completo por Categoría

### ✅ Servicio / Operaciones Generales (HTTP 200)

| Code | Mensaje | Cuándo |
|---|---|---|
| SERVICE_INFO_RETRIEVED | Service info retrieved | GET /service-info |
| RESPONSE_CODES_RETRIEVED | Codes catalog retrieved | GET /response-codes |
| PLATFORM_STATS_RETRIEVED | Platform stats retrieved | GET /admin/stats |
| PLATFORM_DASHBOARD_RETRIEVED | Dashboard retrieved | GET /admin/dashboard |
| OPERATION_COMPLETED | Operation completed | Operación genérica exitosa |
| RESOURCE_CREATED | Resource created | POST exitoso genérico |
| RESOURCE_UPDATED | Resource updated | PATCH/PUT exitoso genérico |
| RESOURCE_DELETED | Resource deleted | DELETE exitoso |
| RESOURCE_RETRIEVED | Resource retrieved | GET exitoso genérico |

### 🔐 Autenticación Plataforma (HTTP 200)

| Code | Mensaje | Cuándo |
|---|---|---|
| PLATFORM_AUTHORIZATION_INITIATED | Authorization initiated | POST /authorize |
| PLATFORM_LOGIN_SUCCESS | Platform login successful | POST /login (platform user) |
| PLATFORM_TOKEN_ISSUED | Platform tokens issued | OAuth2 token grant |
| PLATFORM_TOKEN_ROTATED | Token rotated | Refresh token rotation |
| PLATFORM_TOKEN_REVOKED | Token revoked | POST /revoke |

### 👤 Gestión Usuarios Plataforma (HTTP 200)

| Code | Mensaje | Cuándo |
|---|---|---|
| PLATFORM_USER_CREATED | Platform user created | POST /admin/users |
| PLATFORM_USER_RETRIEVED | User retrieved | GET /admin/users/{id} |
| PLATFORM_USER_UPDATED | User updated | PATCH /admin/users/{id} |
| PLATFORM_USER_SUSPENDED | User suspended | PUT /admin/users/{id}/suspend |
| PLATFORM_USER_ACTIVATED | User activated | PUT /admin/users/{id}/activate |
| PLATFORM_ROLE_ASSIGNED | Role assigned | POST /admin/users/{id}/roles |
| PLATFORM_ROLE_REVOKED | Role revoked | DELETE /admin/users/{id}/roles/{rid} |

### 🏢 Operaciones Tenant (HTTP 200)

| Code | Mensaje | Cuándo |
|---|---|---|
| TENANT_CREATED | Tenant created | POST /tenants |
| TENANT_RETRIEVED | Tenant retrieved | GET /tenants/{slug} |
| TENANT_LIST_RETRIEVED | Tenant list retrieved | GET /tenants |
| TENANT_SUSPENDED | Tenant suspended | PUT /tenants/{slug}/suspend |
| TENANT_ACTIVATED | Tenant activated | PUT /tenants/{slug}/activate |

### 👁️ Cuenta Self-Service (HTTP 200)

| Code | Mensaje | Cuándo |
|---|---|---|
| ACCOUNT_PASSWORD_CHANGED | Password changed | PUT /account/change-password |
| ACCOUNT_PASSWORD_RESET | Password reset | POST /account/reset-password |
| ACCOUNT_PASSWORD_RECOVERY_SENT | Recovery email sent | POST /account/forgot-password |
| ACCOUNT_PASSWORD_RECOVERED | Password recovered | POST /account/recover-password |
| ACCOUNT_SESSIONS_RETRIEVED | Sessions retrieved | GET /account/sessions |
| ACCOUNT_SESSION_REVOKED | Session revoked | DELETE /account/sessions/{id} |
| ACCOUNT_SESSION_ALREADY_CLOSED | Session already closed | Session ya estaba cerrada (idempotent) |
| ACCOUNT_NOTIFICATION_PREFERENCES_RETRIEVED | Prefs retrieved | GET /account/preferences |
| ACCOUNT_NOTIFICATION_PREFERENCES_UPDATED | Prefs updated | PATCH /account/preferences |
| ACCOUNT_ACCESS_RETRIEVED | Access data retrieved | GET /account/access |

### 👥 Usuarios (HTTP 200)

| Code | Mensaje | Cuándo |
|---|---|---|
| USER_CREATED | User created | POST /tenants/{slug}/users |
| USER_REGISTERED | Registered (email sent) | POST /register (self-signup) |
| USER_RETRIEVED | User retrieved | GET /tenants/{slug}/users/{id} |
| USER_LIST_RETRIEVED | User list retrieved | GET /tenants/{slug}/users |
| USER_UPDATED | User updated | PATCH /tenants/{slug}/users/{id} |
| USER_PASSWORD_RESET | Password reset | Admin reset password |
| USER_PROFILE_RETRIEVED | Profile retrieved | GET /account/profile |
| USER_PROFILE_UPDATED | Profile updated | PATCH /account/profile |
| CREDENTIALS_VALID | Credentials valid | Validación exitosa |

### 📧 Email Verification (HTTP 200)

| Code | Mensaje | Cuándo |
|---|---|---|
| EMAIL_VERIFICATION_SENT | Code sent | POST /verify-email/send |
| EMAIL_VERIFIED | Email verified | POST /verify-email/confirm |
| EMAIL_VERIFICATION_RESENT | Code resent | POST /verify-email/resend |
| EMAIL_NOT_VERIFIED | Not verified (error) | User intenta sin verificar email (409) |
| EMAIL_VERIFICATION_EXPIRED | Code expired (error) | Code > TTL (409) |
| EMAIL_VERIFICATION_STILL_ACTIVE | Still active (error) | Re-request antes de expirar (409) |

### 🔑 Reset Password (HTTP 200/409)

| Code | Mensaje | Cuándo |
|---|---|---|
| RESET_PASSWORD_CODE_SENT | Reset code sent | POST /account/forgot-password |
| RESET_PASSWORD_REQUIRED | Must reset (error) | Login con status RESET_PASSWORD (409) |

### 🎫 Client Apps (HTTP 200)

| Code | Mensaje | Cuándo |
|---|---|---|
| CLIENT_APP_CREATED | App created | POST /tenants/{slug}/apps |
| CLIENT_APP_RETRIEVED | App retrieved | GET /tenants/{slug}/apps/{id} |
| CLIENT_APP_LIST_RETRIEVED | Apps retrieved | GET /tenants/{slug}/apps |
| CLIENT_APP_UPDATED | App updated | PATCH /tenants/{slug}/apps/{id} |
| CLIENT_APP_SECRET_ROTATED | Secret rotated | POST /tenants/{slug}/apps/{id}/secret |

### 📋 Memberships (HTTP 200/409)

| Code | Mensaje | Cuándo |
|---|---|---|
| MEMBERSHIP_CREATED | Membership created | POST /tenants/{slug}/memberships (status: PENDING) |
| MEMBERSHIP_RETRIEVED | Membership retrieved | GET /tenants/{slug}/memberships/{id} |
| MEMBERSHIP_LIST_RETRIEVED | Memberships retrieved | GET /tenants/{slug}/memberships |
| MEMBERSHIP_APPROVED | Membership approved | PUT /tenants/{slug}/memberships/{id}/approve |
| MEMBERSHIP_REVOKED | Membership revoked | DELETE /tenants/{slug}/memberships/{id} |
| MEMBERSHIP_SUSPENDED | Membership suspended | PUT /tenants/{slug}/memberships/{id}/suspend |

### 🔐 Roles (HTTP 200)

| Code | Mensaje | Cuándo |
|---|---|---|
| ROLE_CREATED | Role created | POST /tenants/{slug}/roles |
| ROLE_RETRIEVED | Role retrieved | GET /tenants/{slug}/roles/{code} |
| ROLE_LIST_RETRIEVED | Roles retrieved | GET /tenants/{slug}/roles |
| ROLE_UPDATED | Role updated | PATCH /tenants/{slug}/roles/{code} |
| ROLE_DELETED | Role deleted | DELETE /tenants/{slug}/roles/{code} |
| ROLE_ASSIGNED | Role assigned | POST /tenants/{slug}/users/{id}/roles |
| ROLE_PARENT_ASSIGNED | Parent assigned | POST /tenants/{slug}/roles/{code}/parent |
| ROLE_PARENT_REMOVED | Parent removed | DELETE /tenants/{slug}/roles/{code}/parent |

### 🔓 OAuth2 / Authorization (HTTP 200/201)

| Code | Mensaje | Cuándo |
|---|---|---|
| AUTHORIZATION_INITIATED | Authorization initiated | GET /authorize (redirect) |
| AUTHORIZATION_CODE_ISSUED | Auth code issued | Authorization code generado |
| AUTHORIZATION_CODE_EXCHANGED | Code exchanged | POST /oauth2/token (auth_code grant) |
| LOGIN_SUCCESSFUL | Login successful | POST /oauth2/login |
| TOKEN_ISSUED | Tokens issued | Token emitido (genérico) |
| CLIENT_CREDENTIALS_TOKEN_ISSUED | M2M token issued | client_credentials grant |
| REFRESH_TOKEN_ROTATED | Refresh rotated | Refresh token rotation |
| TOKEN_REVOKED | Token revoked | POST /oauth2/revoke |
| USER_INFO_RETRIEVED | Userinfo retrieved | GET /userinfo (OIDC) |
| JWKS_RETRIEVED | JWKS retrieved | GET /.well-known/jwks.json |
| OIDC_CONFIGURATION_RETRIEVED | OIDC config retrieved | GET /.well-known/openid-configuration |

### 💳 Billing — Catálogo (HTTP 200)

| Code | Mensaje | Cuándo |
|---|---|---|
| APP_PLAN_CATALOG_RETRIEVED | Catalog retrieved | GET /billing/catalog |
| APP_PLAN_RETRIEVED | Plan retrieved | GET /billing/plans/{code} |
| APP_PLAN_CREATED | Plan created | POST /billing/plans (admin) |
| PLATFORM_PLAN_CATALOG_RETRIEVED | Platform catalog retrieved | GET /platform/billing/catalog |
| PLATFORM_PLAN_RETRIEVED | Platform plan retrieved | GET /platform/billing/plans/{code} |

### 📝 Billing — Contratos (HTTP 200/404/422)

| Code | Mensaje | Cuándo |
|---|---|---|
| APP_CONTRACT_CREATED | Contract created | POST /billing/contracts |
| APP_CONTRACT_RETRIEVED | Contract retrieved | GET /billing/contracts/{id} |
| APP_CONTRACT_EMAIL_VERIFIED | Email verified | POST /billing/contracts/{id}/verify-email |
| APP_CONTRACT_PAYMENT_APPROVED | Payment approved | POST /billing/contracts/{id}/mock-approve-payment |
| APP_CONTRACT_ACTIVATED | Contract activated | POST /billing/contracts/{id}/activate |
| APP_CONTRACT_ONBOARDING_RESUMED | Onboarding state | GET /billing/contracts/{id}/resume |
| APP_CONTRACT_VERIFICATION_RESENT | Verification resent | POST /billing/contracts/{id}/resend-verification |
| CONTRACT_NOT_FOUND | Not found (error) | Contract ID no existe (404) |
| CONTRACT_INVALID_STATE | Invalid state (error) | Contract status no permite operación (422) |
| CONTRACTOR_EMAIL_ALREADY_EXISTS | Email exists (error) | Email ya en uso (409) |
| PROVIDER_APP_NOT_FOUND | App not found (error) | Provider app no existe (404) |
| PLAN_VERSION_NOT_FOUND | Plan version not found | Plan version no existe (404) |
| APP_INVALID_PLAN_SUBSCRIBER_TYPE | Invalid subscriber type | Plan/contract subscriber type mismatch (409) |

### 💰 Billing — Suscripciones (HTTP 200/409)

| Code | Mensaje | Cuándo |
|---|---|---|
| APP_SUBSCRIPTION_RETRIEVED | Subscription retrieved | GET /subscriptions/{id} |
| APP_SUBSCRIPTION_CANCELLED | Subscription cancelled | PUT /subscriptions/{id}/cancel |
| SUBSCRIPTION_NOT_FOUND | Not found (error) | Subscription no existe (404) |
| SUBSCRIPTION_INVALID_STATE | Invalid state (error) | Status no permite operación (409) |

### 📄 Billing — Invoices (HTTP 200)

| Code | Mensaje | Cuándo |
|---|---|---|
| APP_INVOICE_LIST_RETRIEVED | Invoices retrieved | GET /invoices |
| APP_INVOICE_RETRIEVED | Invoice retrieved | GET /invoices/{id} |
| PLATFORM_INVOICES_RETRIEVED | Platform invoices retrieved | GET /platform/billing/invoices |

### 📊 Billing — Uso y Límites (HTTP 200)

| Code | Mensaje | Cuándo |
|---|---|---|
| APP_USAGE_RETRIEVED | Usage retrieved | GET /usage |
| APP_LIMITS_RETRIEVED | Limits retrieved | GET /limits |
| PLATFORM_SUBSCRIPTION_RETRIEVED | Platform subscription | GET /platform/billing/subscriptions/{id} |
| PLATFORM_SUBSCRIPTION_CANCELLED | Cancelled | PUT /platform/subscriptions/{id}/cancel |

---

## Errores: Validación (HTTP 400)

| Code | Origin | Causa | Ejemplo |
|---|---|---|---|
| **INVALID_INPUT** | CLIENT_REQUEST | Params/body inválido, formato | Body JSON malformado, tipos incorrectos |
| **REQUIRED_FIELD_MISSING** | CLIENT_REQUEST | Campo obligatorio falta | Missing `email` en POST |
| **INVALID_DATA_FORMAT** | CLIENT_REQUEST | Formato específico incorrecto | UUID inválido, email inválido |

**Siempre retornar con `fieldErrors` si aplica.**

---

## Errores: Reglas de Negocio (HTTP 409)

| Code | Causa | Cuándo |
|---|---|---|
| **DUPLICATE_RESOURCE** | Resource unique constraint | `email` ya existe, `slug` duplicado |
| **BUSINESS_RULE_VIOLATION** | Validación lógica | Saldo insuficiente, estado inválido |
| **DUPLICATE_TENANT** | Tenant slug duplicado | POST /tenants con slug existente |

---

## Errores: Recursos (HTTP 404)

| Code | Causa |
|---|---|
| **RESOURCE_NOT_FOUND** | ID no existe |
| **RESOURCE_UNAVAILABLE** | Recurso temporalmente indisponible |

---

## Errores: Autenticación (HTTP 401)

| Code | Causa |
|---|---|
| **AUTHENTICATION_REQUIRED** | Token ausente o inválido |

---

## Errores: Autorización (HTTP 403)

| Code | Causa |
|---|---|
| **INSUFFICIENT_PERMISSIONS** | Token válido, rol/permisos insuficientes |

---

## Errores: Sistema (HTTP 500)

| Code | Causa |
|---|---|
| **OPERATION_FAILED** | Excepción no mapeada, error genérico |
| **DATABASE_ERROR** | Base de datos unavailable, query error |
| **EXTERNAL_SERVICE_ERROR** | Servicio externo error |

---

## Reglas de Cuándo Usar Cada ResponseCode

### INVALID_INPUT (400)

✅ **Usar:**
- Body JSON inválido (deserialization error)
- Parámetro en path/query con tipo incorrecto
- Validación Bean Validation falla (@NotNull, @Email, @Size)
- Formato inválido (UUID, date, regex no match)

❌ **NO usar:**
- Lógica de negocio (ej: "email ya existe" → DUPLICATE_RESOURCE)
- Recurso no encontrado → RESOURCE_NOT_FOUND
- Permiso insuficiente → INSUFFICIENT_PERMISSIONS

### BUSINESS_RULE_VIOLATION (409)

✅ **Usar:**
- Estado inválido para operación (ej: revoke ACTIVE membership)
- Validación de dominio falla (ej: contract.subscriber != plan.subscriber)
- Regla de negocio incumplida

❌ **NO usar:**
- Entrada inválida → INVALID_INPUT
- Recurso no existe → RESOURCE_NOT_FOUND

### OPERATION_FAILED (500)

✅ **Usar:**
- Excepción no mapeada, inesperada
- Error interno del servidor

❌ **NO usar:**
- Errores conocidos/esperados → códigos específicos

---

## Cómo Documentar en OpenAPI

```java
@Operation(summary = "Create tenant")
@ApiResponse(
  responseCode = "201",
  description = "Tenant created",
  content = @Content(
    schema = @Schema(implementation = ApiErrorData.class),
    examples = @ExampleObject(
      name = "success",
      value = """
        {
          "code": "TENANT_CREATED",
          "origin": "BUSINESS_RULE",
          "layer": "api",
          "clientMessage": "Tenant created successfully"
        }
        """
    )
  )
)
@ApiResponse(
  responseCode = "400",
  description = "Invalid input",
  content = @Content(
    schema = @Schema(implementation = ApiErrorData.class),
    examples = @ExampleObject(
      name = "invalid_slug",
      value = """
        {
          "code": "INVALID_INPUT",
          "origin": "CLIENT_REQUEST",
          "layer": "api",
          "clientMessage": "Invalid tenant slug format",
          "fieldErrors": [
            {
              "field": "slug",
              "message": "must match pattern ^[a-z0-9-]+$"
            }
          ]
        }
        """
    )
  )
)
@ApiResponse(
  responseCode = "409",
  description = "Tenant already exists",
  content = @Content(
    schema = @Schema(implementation = ApiErrorData.class),
    examples = @ExampleObject(
      name = "duplicate_slug",
      value = """
        {
          "code": "DUPLICATE_TENANT",
          "origin": "BUSINESS_RULE",
          "layer": "api",
          "clientMessage": "A tenant with this slug already exists"
        }
        """
    )
  )
)
public ResponseEntity<ApiResponse<TenantData>> createTenant(
  @RequestBody @Valid CreateTenantRequest req
) { ... }
```

---

## Cómo Usar i18n (MessageSource)

ErrorData incluye `clientMessage` localizado per Accept-Language header.

```java
// En GlobalExceptionHandler:
@ExceptionHandler(DuplicateTenantException.class)
public ResponseEntity<ApiErrorResponse> handleDuplicate(
  DuplicateTenantException ex,
  HttpServletRequest request
) {
  String message = messageSource.getMessage(
    "error.duplicate_tenant",
    new Object[]{ex.getSlug()},
    request.getLocale()
  );
  return ResponseEntity.status(409)
    .body(ApiErrorResponse.builder()
      .code("DUPLICATE_TENANT")
      .origin(ApiErrorOrigin.BUSINESS_RULE)
      .clientMessage(message)
      .build()
    );
}
```

---

## Checklist para Nuevo Endpoint

1. ✅ ¿Qué ResponseCode retorna en success? (ej. RESOURCE_CREATED)
2. ✅ ¿Qué errores son posibles? (INVALID_INPUT, RESOURCE_NOT_FOUND, etc.)
3. ✅ ¿Cuál es HTTP status de cada error? (400, 404, 409, 500, etc.)
4. ✅ ¿Hay fieldErrors si validación falla?
5. ✅ ¿ClientMessage está localizado?
6. ✅ ¿Documentado en OpenAPI con @ApiResponse?

---

**Última actualización:** 2026-04-10  
**Total ResponseCodes:** 90
