# Lecciones — API

REST controllers, OpenAPI/Swagger, manejo de excepciones, email y Postman.

---

### [2026-04-05] Auditoría completa de excepciones personalizadas sin handler en `GlobalExceptionHandler`

**Contexto:** Tras detectar que `ContractStateViolationException` no tenía handler, se auditaron todas las excepciones personalizadas.

**Problema:** 8 excepciones de dominio no tenían handlers específicos y caían en el catch-all genérico (HTTP 500):
1. `AppRoleNotFoundException` → 404
2. `ClientAppAlreadySuspendedException` → 409
3. `ClientAppSecretRotationException` → 400
4. `ContractVerificationCodeInvalidException` → 400
5. `MembershipAlreadySuspendedException` → 409
6. `RoleHierarchyCycleException` → 400
7. `RoleHierarchyDepthExceededException` → 400
8. `SessionInvalidStateException` → 422

**Solución / Buena práctica:**
- **Criterio HTTP status:**
  - 404 para recursos no encontrados
  - 400 para violaciones de reglas de negocio que son validaciones de entrada
  - 409 para conflictos de estado (intentar hacer algo que ya está hecho)
  - 422 para violaciones de reglas de negocio sobre estado (operación no válida en el estado actual)
- **Metodología de auditoría:** `find` + `grep` para listar todas las excepciones en `keygo-domain` y `keygo-app`, luego comparar con los `@ExceptionHandler` existentes usando `comm -23`.

**Archivos clave:** `GlobalExceptionHandler.java`, `GlobalExceptionHandlerTest.java`

---

### [2026-04-05] `ContractStateViolationException` → HTTP 500 en lugar de 422

**Problema:** `ContractStateViolationException` es una `DomainException` que representa violación de reglas de negocio, pero sin `@ExceptionHandler` específico caía en el catch-all genérico → 500.

**Solución / Buena práctica:** Las excepciones de dominio que representan violaciones de reglas de negocio (no errores de validación de entrada) deben retornar 422, no 400. Siempre agregar tests unitarios para nuevos handlers.

---

### [2026-04-04] Email templates responsive: 4 tipos Thymeleaf con CSS inline

**Problema:** Los templates iniciales usaban HTML simplificado sin estilos CSS inline, no adaptados a las variables reales del sistema. El puerto `EmailNotificationPort` define 4 métodos de conveniencia, pero solo existían 2 templates.

**Solución / Buena práctica:**
1. **Crear 4 templates Thymeleaf** en `keygo-run/src/main/resources/templates/email/`:
   - `email-validation.html` — Registro de usuario (código 6 dígitos, 30 min)
   - `contract-verification.html` — Onboarding de contrato (código + contractId, 30 min)
   - `temporary-password.html` — Contraseña temporal para nuevos usuarios
   - `password-recovery.html` — Recuperación de contraseña (token 32-char, 24h)

2. **Variables esperadas por cada tipo:**
   ```
   email-verification:    "userName", "verificationCode", "expiresInMinutes"
   contract-verification: "userName", "verificationCode", "contractId", "expiresInMinutes"
   temporary-password:    "userName", "temporaryPassword"
   password-recovery:     "userName", "recoveryToken", "tenantSlug"
   ```

3. **Patrón adoptado:**
   - Templates **NUNCA incluyen HTML inline en Java** → siempre en archivos `.html` con Thymeleaf
   - **Variables consistentes:** `userName`, `verificationCode`, `recoveryToken`
   - **CSS inline obligatorio** → compatibilidad con clientes email antiguos
   - **Responsive design obligatorio** → `@media (max-width: 600px)`
   - **Colores de brand únicos por tipo** → morado (verificación), rojo (recuperación), etc.

4. Tests usan `ArgumentCaptor<SendEmailCommand>` para validar que cada método de conveniencia construye el command correcto.

**Archivos afectados:**
- `email-validation.html`, `password-recovery.html` — actualizados
- `contract-verification.html`, `temporary-password.html` — nuevos
- `SmtpEmailNotificationAdapterTest.java` — refactorizado
- `docs/design/email/EMAIL_TEMPLATES_MAPPING.md` — nuevo documento de referencia

---

### [2026-04-03] `PageFilter` como base class eliminó duplicación en `TenantFilter`

**Problema:** `TenantFilter` reimplementaba validación de `page`/`size`/`sortBy` en lugar de extender `PageFilter`. Dos clases con lógica de validación idéntica → riesgo de divergencia.

**Solución:** Refactorizar `TenantFilter` para extender `PageFilter`. Automáticamente agregó compatibilidad de `sort`/`order` a `GET /tenants`. El cambio requirió actualizar 5 tests (4 params → 6 params).

**Patrón confirmado escalable:** 4 filtros nuevos (`UserFilter`, `ClientAppFilter`, `MembershipFilter`, `AppRoleFilter`) implementados sin duplicación gracias a `PageFilter` base.

**Archivos clave:** `PageFilter.java`, `TenantFilter.java`

---

### [2026-04-02] `traceId` en `ErrorData` — leído de MDC en `ApiErrorDataFactory`

**Problema:** Sin `traceId` en el body de error, el frontend no puede correlacionar errores con logs del servidor.

**Solución / Buena práctica:** Agregar campo `traceId` a `ErrorData` (con `@JsonInclude(NON_NULL)` heredado). En `ApiErrorDataFactory.fromDetail()` leer `MDC.get("traceId")` — si el `RequestTracingFilter` corrió antes, siempre estará presente. `traceId` solo en errores + header `X-Trace-ID` en todas las respuestas (no rompe contratos de respuesta exitosa).

**Archivos clave:** `ErrorData.java`, `ApiErrorDataFactory.java`

---

### [2026-04-01] `ErrorData` tiene campo `layer` solo con excepciones tipadas KeyGo

**Problema:** El campo `layer` de `ErrorData` solo se popula cuando la excepción es instancia de `KeyGoException`. Para excepciones de Spring, `layer` estará **ausente** (null → omitido por `@JsonInclude(NON_NULL)`).

**Solución / Buena práctica:** En el frontend, tratar `layer` como **opcional** y usarlo solo para telemetría/diagnóstico. En Postman, no hacer `pm.expect(body.data).to.have.property('layer')` a menos que se esté probando una excepción tipada KeyGo específica.

---

### [2026-04-01] `fieldErrors` en `ErrorData` — solo en validaciones `@Valid` / `@Validated`, no en todos los 400

**Problema:** Se podría asumir que `fieldErrors` aparece en todos los `400 INVALID_INPUT`. En realidad, solo se popula cuando el backend usa `@Valid` (`MethodArgumentNotValidException`) o `@Validated` (`ConstraintViolationException`).

**Solución / Buena práctica:** En el frontend, usar `if (errorData.fieldErrors?.length)` antes de intentar mapear errores por campo.

---

### [2026-04-01] Postman: tests de error desactualizados negaban la existencia de `data`

**Problema:** La carpeta "Escenarios de Error" tenía tests con `pm.expect(body).to.not.have.property('data')`. Desde la implementación de `ErrorData`, **todas** las respuestas de error incluyen `data`.

**Solución / Buena práctica:** Al agregar `ErrorData` al `GlobalExceptionHandler`, actualizar inmediatamente todos los tests de Postman que validen respuestas de error.

---

### [2026-04-01] Swagger: `Content`/`Schema` faltantes al agregar `@ApiResponse` con body de error

**Síntoma:** `mvnw compile` falla con `cannot find symbol: class Content / class Schema`.

**Causa:** Los controllers existentes no tenían `import io.swagger.v3.oas.annotations.media.Content` ni `import io.swagger.v3.oas.annotations.media.Schema` — solo `@ApiResponse` sin body no los requería.

**Solución:** Al agregar cualquier `@ApiResponse` con cuerpo de error, agregar ambos imports al inicio del archivo.

---

### [2026-04-01] Swagger: convención de descripción con código de respuesta

**Solución:** Sufijo `(code: NOMBRE_ENUM)` en cada descripción de `@ApiResponse`. Ejemplo: `"Tenant not found (code: RESOURCE_NOT_FOUND)"`. Para 400 de validación: `"... (code: INVALID_INPUT). data.field_errors lists each invalid field."`.

---

### [2026-03-31] Email HTML: `SimpleMailMessage` no soporta HTML + ambigüedad `setFrom` en Mockito

**Síntoma:** Email enviado como texto plano. En tests, `setFrom(any())` falla en compilación.

**Causa:** `SimpleMailMessage` es solo texto plano. `MimeMessage.setFrom()` tiene dos sobrecargas; `any()` genérico es ambiguo para Mockito.

**Solución:** Usar `MimeMessageHelper(message, true, "UTF-8")` + `helper.setText(html, true)`. Stubear con `doThrow(...).when(mock).setFrom(any(Address.class))`. Agregar `mockito-junit-jupiter` a `keygo-infra/pom.xml`.

**Archivos clave:** `SmtpEmailNotificationAdapter.java`, `keygo-infra/pom.xml`

---

### [2026-03-31] Controller de contratos: mover `clientAppId` del path al body

**Síntoma:** Path requería dos resoluciones innecesarias (`tenantRepo` + `clientAppRepo`).

**Solución:** Mover `clientAppId` al body. Eliminar repositorios innecesarios del controller. `BootstrapAdminKeyFilter` con `hasSegment(contains)` no se rompe con cambio de path.

**Archivos clave:** `AppBillingContractController.java`, `CreateAppContractRequest.java`

---

### [2026-03-31] `FRONTEND_DEVELOPER_GUIDE`: cambios en cascada al renombrar path o campo

**Síntoma:** Un cambio de path propagó inconsistencias a 4+ secciones del documento.

**Solución:** Antes de editar, buscar el path/campo obsoleto con grep en todo el documento y actualizar todas las ocurrencias.

---

### [2026-03-28] Swagger muestra `data: {}` en lugar del tipo real

**Síntoma:** Swagger UI no infiere el tipo de `data` en `BaseResponse<T>`.

**Causa:** springdoc-openapi no resuelve el genérico `T` desde `@Schema(implementation = BaseResponse.class)`.

**Solución:** Agregar inner class estática `Response extends BaseResponse<PropioDTOType>` en cada DTO. springdoc lee la parametrización de la superclase vía reflexión.

---

### [2026-03-28] Swagger muestra camelCase cuando API serializa en snake_case

**Causa:** springdoc genera schemas por reflexión, independiente del `JsonMapperBuilderCustomizer` de runtime.

**Solución:** Agregar `spring.jackson.property-naming-strategy: SNAKE_CASE` en `application.yml`. Crear `SnakeCaseModelConverter` implementando `io.swagger.v3.core.converter.ModelConverter`.

**Archivos clave:** `application.yml`, `SnakeCaseModelConverter.java`

---

### [2026-03-28] `BaseResponse` en sub-paquete `response`, no en `shared` directamente

**Síntoma:** Compilación falla: `cannot find symbol BaseResponse`.

**Causa:** Import incorrecto: `api.shared.BaseResponse` en lugar de `api.shared.response.BaseResponse`.

**Solución:** Verificar ubicación real con grep antes de escribir imports.

---

### [2026-03-27] Colección Postman corrompida por edición parcial

**Síntoma:** JSON inválido: objetos malformados, cierres faltantes, variables no resueltas.

**Causa:** Ediciones parciales sin validación; variables `{{...}}` no declaradas en entorno.

**Solución:** Validar con `python3 -m json.tool` tras cada edición. Variables deben existir en entorno/colección/scripts. Scripts de token exchange deben hacer `pm.environment.set('accessToken', ...)`.

**Archivos clave:** `KeyGo-Server.postman_collection.json`

---

### [2026-03-23] Flujo OAuth2: backend retorna `code` en JSON, no HTTP 302

**Síntoma:** Frontend esperaba redirect 302; backend retorna `data.code` en JSON.

**Causa:** Implementación actual difiere del estándar RFC 6749.

**Solución:** Frontend: lee `code` del JSON, construye URL callback manualmente y navega con `window.location.href`. Diseñar `CallbackPage` preparada para migración futura a 302.

**Archivos clave:** `AUTH_FLOW.md`
