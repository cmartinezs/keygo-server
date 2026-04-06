# T-108 — Contexto y Estado Actual

## Endpoint afectado

```
GET /keygo-server/api/v1/tenants/{slug}/account/sessions
Authorization: Bearer <access_token>
```

Implementado en: `AccountSettingsController.getSessions()` → `ListUserSessionsUseCase`

## Flujo actual

```
ListUserSessionsCommand { tenantSlug, bearerToken, requestUserAgent, requestIpAddress }
          ↓
ListUserSessionsUseCase.execute()
  1. Verifica JWT → extrae sub (userId)
  2. Resuelve tenant por slug
  3. sessionRepository.findAllByUserIdAndTenantId()
  4. Filtra ACTIVE
  5. toResult() → SessionInfoResult (sin location)
          ↓
AccountSessionData.from(result)  →  JSON response (sin location)
```

## Cadena de tipos actual

```java
// SessionInfoResult (keygo-app)
public record SessionInfoResult(
    UUID sessionId,
    String status,
    String userAgent,
    String ipAddress,
    Instant createdAt,
    Instant lastAccessedAt,
    Instant expiresAt,
    boolean isCurrent) {}  // ← NO tiene location

// AccountSessionData (keygo-api)
public record AccountSessionData(
    UUID sessionId, String status,
    String browser, String os, String deviceType,
    String ipAddress,
    Instant createdAt, Instant lastAccessedAt, Instant expiresAt,
    boolean isCurrent) {}  // ← NO tiene location
```

## Dato disponible en BD

`sessions.ip_address` (VARCHAR 64) — almacenado en `SessionEntity.ipAddress`.  
Se persiste durante `OpenSessionUseCase` desde el request HTTP.

## Gap identificado

El campo `location` mencionado en el RFC de account-ui nunca fue implementado.
`ip_address` está disponible en BD pero no se convierte a ubicación legible.

## Impacto de la ausencia

- Usuario no puede distinguir sesiones de distintos países
- Sesiones sospechosas (login desde otra región) son indistinguibles por UI
- El RFC account-ui esperaba `location` pero el backend retorna null/ausente
