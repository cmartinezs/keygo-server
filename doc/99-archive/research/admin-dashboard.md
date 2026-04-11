# Admin Dashboard — Read Model & Contrato API

> **Contexto:** `GET /api/v1/platform/stats` existe pero solo cubre conteos agregados de tenants,
> usuarios, apps y claves activas. El modelo ya dispone de piezas más ricas: `memberships`,
> `authorization_codes`, `signing_keys`, `sessions`, `refresh_tokens` y `email_verifications`.
> El siguiente paso lógico es definir el read model del dashboard: un endpoint agregado, el contrato
> JSON que consumirá el front, y la estructura visual del panel.

---

## 1. Endpoint recomendado

Agregar un endpoint nuevo y dejar `platform/stats` como resumen liviano.

### Opción recomendada

```
GET /api/v1/admin/platform/dashboard
```

### ¿Por qué un endpoint separado?

En la spec actual, la categoría **Platform** mezcla endpoints públicos con protegidos:
`service/info` y `response-codes` son públicos, mientras `platform/stats` requiere Bearer/Admin.
Separar la superficie pública de la operativa deja la API más limpia y coherente.

### Objetivo del endpoint

Entregar en una sola llamada:

- Estado del servicio
- Métricas agregadas
- Seguridad operativa
- Pendientes
- Actividad reciente
- Rankings / topología
- Metadata de _quick actions_

---

## 2. Contrato JSON propuesto

```json
{
  "date": "2026-03-28T13:15:30Z",
  "success": {
    "code": "KEYGO-2000",
    "message": "Platform dashboard retrieved successfully"
  },
  "data": {
    "service": {
      "title": "KeyGo Server",
      "name": "keygo-server",
      "version": "1.0-SNAPSHOT",
      "environment": "local",
      "status": "UP"
    },
    "security": {
      "activeSigningKey": {
        "kid": "kg-2026-01",
        "algorithm": "RS256",
        "activatedAt": "2026-03-20T10:00:00Z",
        "ageDays": 8
      },
      "counts": {
        "activeSigningKeys": 1,
        "retiredSigningKeys": 2,
        "revokedSigningKeys": 0,
        "activeSessions": 91,
        "expiredSessions": 17,
        "terminatedSessions": 6,
        "activeRefreshTokens": 90,
        "usedRefreshTokens": 210,
        "expiredRefreshTokens": 14,
        "revokedRefreshTokens": 3,
        "pendingAuthorizationCodes": 2,
        "usedAuthorizationCodes": 150,
        "expiredAuthorizationCodes": 7,
        "revokedAuthorizationCodes": 0
      },
      "alerts": [
        {
          "level": "warning",
          "code": "SIGNING_KEY_AGE_HIGH",
          "message": "Active signing key is older than 30 days"
        }
      ]
    },
    "tenants": {
      "total": 52,
      "active": 42,
      "pending": 5,
      "suspended": 5,
      "recentlyCreated": 4
    },
    "users": {
      "total": 105,
      "active": 100,
      "pending": 5,
      "suspended": 0,
      "recentlyCreated": 8
    },
    "apps": {
      "total": 52,
      "active": 48,
      "pending": 2,
      "suspended": 2,
      "public": 29,
      "confidential": 23,
      "withoutRedirectUris": 3
    },
    "memberships": {
      "total": 140,
      "active": 131,
      "pending": 4,
      "suspended": 5,
      "usersWithoutMembership": 6
    },
    "registration": {
      "pendingEmailVerifications": 5,
      "expiredPendingVerifications": 2,
      "recentRegistrations": 7,
      "recentVerifications": 5
    },
    "topology": {
      "avgUsersPerTenant": 2.02,
      "avgAppsPerTenant": 1.00,
      "avgMembershipsPerApp": 2.69,
      "tenantsWithoutApps": 3,
      "tenantsWithoutUsers": 1
    },
    "rankings": {
      "topTenantsByUsers": [
        { "tenantSlug": "keygo", "tenantName": "KeyGo", "count": 25 }
      ],
      "topAppsByMemberships": [
        { "clientId": "key-go-ui", "appName": "KeyGo UI", "tenantSlug": "keygo", "count": 25 }
      ]
    },
    "pendingActions": [
      { "type": "TENANT_APPROVAL",        "count": 5, "route": "/tenants?status=PENDING" },
      { "type": "EMAIL_VERIFICATION",     "count": 5, "route": "/registration/verifications?status=PENDING" },
      { "type": "USER_WITHOUT_MEMBERSHIP","count": 6, "route": "/access/users-without-membership" }
    ],
    "recentActivity": [
      {
        "type": "TENANT_CREATED",
        "label": "Tenant demo created",
        "occurredAt": "2026-03-28T12:40:00Z",
        "route": "/tenants/demo"
      },
      {
        "type": "CLIENT_SECRET_ROTATED",
        "label": "Client secret rotated for demo-ui",
        "occurredAt": "2026-03-28T12:10:00Z",
        "route": "/apps/demo-ui"
      }
    ],
    "quickActions": [
      { "code": "CREATE_TENANT", "label": "Crear tenant",     "route": "/tenants/new" },
      { "code": "CREATE_APP",    "label": "Registrar app",    "route": "/apps/new" },
      { "code": "INVITE_USER",   "label": "Invitar usuario",  "route": "/users/new" }
    ]
  }
}
```

---

## 3. Qué sale del modelo actual y qué no

### ✅ Sale directo de lo que ya existe

Esto se puede calcular hoy mismo con queries sobre las tablas actuales:

- Tenants por estado
- Users por estado
- Apps por `type` y `status`
- Memberships por estado
- Signing keys por estado
- Sessions por estado
- Refresh tokens por estado
- Authorization codes por estado
- Email verifications pendientes / expiradas
- Rankings por tenant / app
- Relaciones derivadas: "usuarios sin membership", "tenants sin apps" (el modelo ya amarra `tenant → users/apps` y `user ↔ app` mediante `membership`)

### ⚠️ Sale, pero con aproximación

| Campo | Limitación |
|---|---|
| `recentActivity` | Mientras no exista auditoría formal, se construye con `created_at` + eventos puntuales de negocio |
| `alerts` | Se derivan por reglas calculadas en el use case (ej. clave activa > 30 días) |

### ❌ No sale bien todavía

Los siguientes datos requieren una tabla de auditoría formal (`audit_events`), proyectada para fases futuras:

- Trazabilidad real de eventos
- Intentos fallidos de login
- Anomalías de seguridad
- Cambios administrativos detallados

---

## 4. DTOs backend sugeridos

Usar un DTO agregado específico para el dashboard; **no reutilizar** `platform/stats`.

```java
public record PlatformDashboardResponse(
    Instant           generatedAt,
    ServiceSummary    service,
    SecuritySummary   security,
    CountSummary      tenants,
    CountSummary      users,
    AppSummary        apps,
    MembershipSummary memberships,
    RegistrationSummary registration,
    TopologySummary   topology,
    RankingSummary    rankings,
    List<PendingActionItem> pendingActions,
    List<ActivityItem>      recentActivity,
    List<QuickActionItem>   quickActions
) {}

public record ServiceSummary(
    String title,
    String name,
    String version,
    String environment,
    String status
) {}

public record CountSummary(
    long total,
    long active,
    long pending,
    long suspended,
    long recentlyCreated
) {}

public record AppSummary(
    long total,
    long active,
    long pending,
    long suspended,
    long publicCount,
    long confidentialCount,
    long withoutRedirectUris
) {}

public record MembershipSummary(
    long total,
    long active,
    long pending,
    long suspended,
    long usersWithoutMembership
) {}

public record SecuritySummary(
    SigningKeySummary    activeSigningKey,
    SecurityCounts      counts,
    List<DashboardAlert> alerts
) {}
```

---

## 5. Estructura de servicios backend recomendada

No mezclar con el controller actual de stats. Estructura sugerida:

```
PlatformDashboardController
└── PlatformDashboardUseCase (read use case)
    ├── PlatformDashboardMetricsPort   → impl: PlatformDashboardMetricsAdapter
    ├── PlatformDashboardSecurityPort  → impl: PlatformDashboardSecurityAdapter
    ├── PlatformDashboardRankingPort   → impl: PlatformDashboardRankingAdapter
    └── PlatformDashboardActivityPort  → impl: PlatformDashboardActivityAdapter
```

> El dashboard debe ser un **read use case específico**, no una suma improvisada de endpoints CRUD.

---

## 6. Queries útiles para la implementación

### 6.1 Conteos base

`COUNT(*) GROUP BY status` sobre cada tabla:

```
tenants · tenant_users · client_apps · memberships
signing_keys · sessions · refresh_tokens · authorization_codes
```

> ⚠️ **Atención:** en `authorization_codes` los valores de `status` están en **minúscula**
> (`pending`, `used`, `expired`, `revoked`), a diferencia del resto de tablas que usan
> `UPPER_CASE` (`ACTIVE`, `SUSPENDED`, etc.).

### 6.2 Derivadas de negocio

**Usuarios sin membership:**

```sql
SELECT COUNT(*)
FROM tenant_users u
LEFT JOIN memberships m ON m.user_id = u.id
WHERE m.user_id IS NULL;
```

**Tenants sin apps:**

```sql
SELECT COUNT(*)
FROM tenants t
LEFT JOIN client_apps a ON a.tenant_id = t.id
WHERE a.id IS NULL;
```

**Apps sin redirect URIs:**

```sql
SELECT COUNT(*)
FROM client_apps a
LEFT JOIN client_redirect_uris r ON r.client_app_id = a.id
WHERE r.id IS NULL;
```

**Top apps por memberships:**

```sql
SELECT a.client_id, a.name, t.slug, COUNT(m.id) AS total
FROM client_apps a
JOIN tenants t ON t.id = a.tenant_id
LEFT JOIN memberships m ON m.client_app_id = a.id
GROUP BY a.client_id, a.name, t.slug
ORDER BY total DESC
LIMIT 5;
```
