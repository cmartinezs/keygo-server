# RFC T-108 — Geolocalización de Sesiones por IP

> **Estado:** 📋 Propuesta
> **ID:** T-108
> **Módulos afectados:** `keygo-app`, `keygo-infra`, `keygo-api`, `keygo-run`
> **Migración Flyway:** No requerida

## Objetivo

Enriquecer el campo `location` del endpoint `GET /api/v1/tenants/{slug}/account/sessions`
resolviendo geolocalización (`ciudad, país`) a partir del `ip_address` ya almacenado en
la tabla `sessions`, en tiempo de consulta (no de escritura).

Actualmente `AccountSessionData.location` no existe (campo pendiente). Con este RFC, el
usuario puede identificar sesiones sospechosas por su ubicación geográfica.

## Documentos

1. `01-contexto-y-estado-actual.md` — Estado actual del endpoint + campo faltante
2. `02-diseno-tecnico.md` — Proveedor, arquitectura, caching, IPs privadas, feature flag
3. `03-plan-implementacion.md` — Fases A–F, archivos a crear/modificar, orden de trabajo
4. `04-contrato-api.md` — Request/Response con y sin geoip habilitado

## Decisiones clave

| Decisión | Elección | Justificación |
|---|---|---|
| Proveedor | ip-api.com | Sin API key, JSON simple, detecta IPs privadas, gratuito < 45 req/min |
| HTTP client | `java.net.http.HttpClient` | Sin nuevas dependencias, Java 11+ built-in |
| Persistencia | No — solo en consulta | `ip_address` ya está almacenado; evita migración y contención de escritura |
| Cache | `ConcurrentHashMap` in-memory | IPs son finitas por deployment; evita llamadas repetidas por sesión |
| Feature flag | `keygo.session.geoip.enabled: false` | Default OFF para no introducir latencia en producción sin configuración |
| IPs privadas | Detección local sin HTTP call | Evita latencia/error en ambientes dev/localhost |

## Respuesta esperada (con geoip habilitado)

```json
{
  "sessionId": "...",
  "status": "ACTIVE",
  "browser": "Chrome 123",
  "os": "macOS",
  "deviceType": "desktop",
  "ipAddress": "187.243.1.1",
  "location": "Mexico City, Mexico",
  "createdAt": "...",
  "isCurrent": true
}
```

Con IPs privadas o geoip deshabilitado: `"location": null`.
