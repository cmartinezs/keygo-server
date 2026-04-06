# T-108 — Contrato API

## Endpoint

```
GET /keygo-server/api/v1/tenants/{slug}/account/sessions
Authorization: Bearer <access_token>
```

---

## Respuesta con geoip habilitado (`keygo.session.geoip.enabled: true`)

```json
{
  "date": "2026-04-07T17:00:00Z",
  "success": {
    "code": "SESSIONS_RETRIEVED",
    "message": "Sessions retrieved successfully"
  },
  "data": {
    "sessions": [
      {
        "sessionId": "550e8400-e29b-41d4-a716-446655440000",
        "status": "ACTIVE",
        "browser": "Chrome 123",
        "os": "macOS 14",
        "deviceType": "desktop",
        "ipAddress": "187.243.1.1",
        "location": "Mexico City, Mexico",
        "createdAt": "2026-04-01T10:00:00Z",
        "lastAccessedAt": "2026-04-06T18:00:00Z",
        "expiresAt": "2026-05-01T10:00:00Z",
        "isCurrent": true
      },
      {
        "sessionId": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
        "status": "ACTIVE",
        "browser": "Safari 17",
        "os": "iOS 17",
        "deviceType": "mobile",
        "ipAddress": "8.8.8.8",
        "location": "Mountain View, United States",
        "createdAt": "2026-04-05T08:00:00Z",
        "lastAccessedAt": "2026-04-06T12:00:00Z",
        "expiresAt": "2026-05-05T08:00:00Z",
        "isCurrent": false
      }
    ]
  }
}
```

---

## Respuesta con geoip deshabilitado (`keygo.session.geoip.enabled: false`, default)

```json
{
  "data": {
    "sessions": [
      {
        "sessionId": "...",
        "status": "ACTIVE",
        "browser": "Chrome 123",
        "os": "macOS 14",
        "deviceType": "desktop",
        "ipAddress": "187.243.1.1",
        "location": null,
        "isCurrent": true
      }
    ]
  }
}
```

---

## Respuesta con IP privada (siempre `null`, independiente de la config)

```json
{
  "ipAddress": "192.168.1.100",
  "location": null
}
```

---

## Campos de `AccountSessionData`

| Campo | Tipo | Descripción | Nullable |
|---|---|---|---|
| `sessionId` | UUID | Identificador de la sesión | No |
| `status` | String | `ACTIVE` \| `TERMINATED` \| `EXPIRED` | No |
| `browser` | String | Navegador inferido del User-Agent | Sí |
| `os` | String | SO inferido del User-Agent | Sí |
| `deviceType` | String | `desktop` \| `mobile` \| `tablet` | Sí |
| `ipAddress` | String | IP del cliente al crear la sesión | Sí |
| `location` | String | Ubicación formateada (`"Ciudad, País"`) | **Sí** |
| `createdAt` | Instant | Timestamp de creación | No |
| `lastAccessedAt` | Instant | Último acceso | Sí |
| `expiresAt` | Instant | Expiración de la sesión | No |
| `isCurrent` | boolean | `true` si UA+IP coinciden con la petición actual | No |

---

## Comportamiento del campo `location`

| Situación | Valor de `location` |
|---|---|
| geoip deshabilitado (`enabled: false`) | `null` |
| IP privada (`192.168.x.x`, `127.x.x.x`, etc.) | `null` |
| ip-api.com no disponible / timeout | `null` (graceful degradation) |
| Respuesta `status: fail` de ip-api | `null` |
| Resolución exitosa con city | `"Ciudad, País"` (ej. `"Mexico City, Mexico"`) |
| Resolución exitosa sin city | `"País"` (ej. `"Mexico"`) |

---

## Notas de integración frontend

- Tratar `location: null` mostrando `—` o nada en la UI
- El campo siempre estará presente en la respuesta JSON (como `null` o string)
- No existe un campo separado `city`/`country` — solo el formato unificado `location`
- La UI puede combinar `ipAddress` (para debug técnico) con `location` (para UX)
