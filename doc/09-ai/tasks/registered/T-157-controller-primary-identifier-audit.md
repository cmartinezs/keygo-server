# T-157 — Auditar y corregir uso de identificadores primarios en controllers

**Estado:** 🔲 Registrada  
**Prioridad:** 🟡 Media  
**Esfuerzo estimado:** 🕐 M-L (auditoría + correcciones por módulo)

---

## Problema / Requisito

Nueva regla de codificación establecida:

> **Identificadores primarios en parámetros:** controllers y orchestradores **nunca** reciben nombres, códigos u otras referencias (slug, código, email, etc.) como parámetros para relacionar entidades. El controller obtiene el `id` primario (UUID/Long/UserId/etc) consultando el puerto correspondiente **antes** de pasar al use case/orquestador. Esto mantiene la separación entre presentación y aplicación clara.

**Situación actual:** muchos endpoints aún reciben referencias indirectas (tenantSlug, clientId, email, etc.) que los use cases resuelven internamente. Esto viola la regla: la presentación debe ya haber resuelto la identidad antes de llegar a la aplicación.

**Objetivo:** auditar todos los controllers, identificar violaciones y planificar correcciones por módulo/contexto.

---

## Especificación

### Patrón correcto (después de esta tarea)

```
[HTTP Request]
    ↓
[RegistrationController] ← recibe: email (from body), clientId (path param)
    ↓
    ├─→ [RegistrationController] consulta port → resuelve: UserId, ClientAppId
    ↓
[SelfRegistrationOrchestrator.execute(userId, clientAppId)]
    ↓
[Use cases]
```

### Patrón incorrecto (actual, a corregir)

```
[HTTP Request]
    ↓
[RegistrationController] ← recibe: email (from body), clientId (path param)
    ↓
[SelfRegistrationOrchestrator.execute(tenantSlug, clientId, email, code)]
    ↓
[Orchestrator/UseCase] ← aquí resuelve UserId, ClientAppId internamente
    ↗ [Puerto] ← consulta indirecta dentro de aplicación
```

---

## Pasos de análisis y ejecución

| # | Acción | Notas | Estado |
|---|--------|-------|--------|
| 1 | **Auditoría: listar todos los controllers** | Revisar `keygo-api/**/controller/*.java` y clasificar por contexto (auth, user, tenant, billing, platform, etc.) | PENDING |
| 2 | **Identificar violaciones por controller** | Para cada controller, marcar qué parámetros de entrada (path, body) se pasan directamente al use case/orquestador sin resolución de id | PENDING |
| 3 | **Clasificar violaciones por severidad/impacto** | Alto: > 50% de endpoints usan refs indirectas; Medio: 25-50%; Bajo: < 25% | PENDING |
| 4 | **Crear subtareas por módulo** | Una subtarea por contexto (auth, user, tenant, etc.) con lista exacta de endpoints a corregir | PENDING |
| 5 | **Ejecutar correcciones por módulo** | Aplicar el patrón correcto: controller resuelve id, pasa solo identificadores primarios | PENDING |
| 6 | **Tests de integración** | Verificar que cada endpoint corregido sigue funcionando correctamente | PENDING |
| 7 | **Documentar excepciones** | Si algún caso **no puede** aplicar la regla, documentar por qué (ej: flujos públicos anónimos) | PENDING |

---

## Ejemplos de violaciones detectadas

### Actual (incorrecto)
```java
// RegistrationController
@PostMapping("/verify-email")
public ResponseEntity<BaseResponse<Void>> verifyEmail(
    @PathVariable String tenantSlug,
    @PathVariable String clientId,
    @Valid @RequestBody VerifyEmailRequest request) {
  
  // 🔴 Problema: pasa tenantSlug, clientId (referencias) al orquestador
  // que internamente resuelve a IDs
  selfRegistrationOrchestrator.execute(
      tenantSlug, clientId, request.email(), request.code());
  ...
}
```

### Corregido (correcto)
```java
// RegistrationController
@PostMapping("/verify-email")
public ResponseEntity<BaseResponse<Void>> verifyEmail(
    @PathVariable String tenantSlug,
    @PathVariable String clientId,
    @Valid @RequestBody VerifyEmailRequest request) {
  
  // 🟢 Correcto: resolver IDs en el controller ANTES de pasar al orquestador
  Tenant tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(tenantSlug))
      .orElseThrow(...);
  ClientApp app = clientAppRepositoryPort
      .findByClientIdAndTenantId(ClientId.of(clientId), tenant.getId())
      .orElseThrow(...);
  
  // Ahora pasa solo IDs primarios
  selfRegistrationOrchestrator.execute(
      tenant.getId(), app.getId(), request.email(), request.code());
  ...
}
```

---

## Contextos a auditar

- **auth:** Endpoints de autenticación, tokens, JWKS, OIDC
- **user:** Registration, password reset, profile, preferences
- **tenant:** Create, list, suspend, activate
- **membership:** Assign roles, revoke, list
- **clientapp:** Create, update, rotate secret
- **billing:** Contracts, subscriptions, invoices
- **platform:** Platform users, roles, dashboard

---

## Guía de verificación

```bash
# Después de completar correcciones
./mvnw clean package -DskipTests -pl keygo-api
./mvnw checkstyle:check -pl keygo-api

# Tests por módulo
./mvnw test -pl keygo-api -Dtest="*Controller*"

# Verificar que los cambios cumplen la regla
# (manual code review contra patrón correcto)
```

---

## Impacto

- **Beneficio:** Controllers más claros; separación de concerns entre presentación y aplicación; menos resoluciones indirectas dentro de use cases.
- **Riesgo:** Cambios en firmas de métodos públicos; requiere actualizar todos los callers (incluyendo tests).
- **Complejidad:** Media — cambios mecánicos pero amplios.

---

## Relaciones

| Artefacto | Tipo | Descripción |
|-----------|------|-------------|
| T-154 | derivadora | T-154 introdujo `SelfRegistrationOrchestrator` con patrón incorrecto; esta tarea lo corrige y establece la regla global |
| agents.md | referencia | Nueva regla documentada en Reglas críticas |

---

## Notas

- Esta tarea es una **auditoría + correcciones graduales**. Se puede ejecutar por contexto (primero `user`, luego `auth`, etc.).
- No bloquea desarrollo de nuevas features, pero nuevos endpoints deben cumplir desde el inicio.
- Considerar crear un linter custom o pattern check si el volumen justifica automatización.

---

## Historial de transiciones

- 2026-04-15 → 🔲 Registrada (derivada de regla establecida en revisión T-154)
