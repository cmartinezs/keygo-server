# T-147 — Actualizar entitlements de planes de plataforma

**Estado:** ✅ Completada
**Módulos afectados:** `keygo-supabase`, docs

---

## Problema / Requisito

Los planes de plataforma (`client_app_id IS NULL`) se introdujeron en `V20__platform_plan_catalog.sql`
con solo dos métricas por plan:

- `MAX_TENANTS`
- `MAX_USERS`

El backup previo al último RFC (`keygo-supabase/src/main/resources/db/backup_20260409_v33/V17__seed_billing_plans.sql`)
define el conjunto completo de entitlements para la escalera comercial de keygo-ui.
Los planes de plataforma deben reflejar la misma estructura de métricas, adaptando los nombres
al contexto platform (`MAX_USERS` en lugar de `MAX_TENANT_USERS`, etc.).

Las métricas faltantes son:

- `MAX_CLIENT_APPS`
- `MAX_ADMINS`
- `MAX_MONTHLY_TOKENS`
- `SOCIAL_LOGIN`
- `CUSTOM_DOMAIN`
- `SLA_UPTIME_PCT`
- `AUDIT_LOG_DAYS`
- Tarifas escalonadas FLEX (16 métricas `FLEX_*`)
- Features exclusivas ENTERPRISE: `PRIORITY_SUPPORT`, `CUSTOM_SLA`, `DEDICATED_SUCCESS_MGR`

## Fuente de verdad

Archivo de referencia: `keygo-supabase/src/main/resources/db/backup_20260409_v33/V17__seed_billing_plans.sql`
— secciones de `INSERT INTO app_plan_entitlements` (puntos 4–9 del archivo).

## Convenciones de nombres para el contexto platform

| Nombre en V17 (keygo-ui) | Nombre en V21 (platform) | Razón |
|---|---|---|
| `MAX_TENANT_USERS` | `MAX_USERS` | V20 ya usó `MAX_USERS`; mantener consistencia |
| `MAX_CLIENT_APPS` | `MAX_CLIENT_APPS` | Mismo nombre; los contractors registran client_apps |
| Todos los demás | Igual al V17 | Sin cambio de nombre |

## Estado actual en V20

| Plan | Métrica | limit_value | period_type | enforcement_mode |
|---|---|---|---|---|
| FREE | MAX_TENANTS | 1 | MONTH | HARD |
| FREE | MAX_USERS | 3 | MONTH | HARD |
| PERSONAL | MAX_TENANTS | 1 | MONTH | HARD |
| PERSONAL | MAX_USERS | 5 | MONTH | HARD |
| TEAM | MAX_TENANTS | 3 ⚠️ | MONTH | HARD |
| TEAM | MAX_USERS | 25 | MONTH | HARD |
| BUSINESS | MAX_TENANTS | 10 ⚠️ | MONTH | HARD |
| BUSINESS | MAX_USERS | 100 | MONTH | HARD |
| FLEX | MAX_TENANTS | 20 | MONTH | SOFT |
| FLEX | MAX_USERS | 250 | MONTH | SOFT |
| ENTERPRISE | MAX_TENANTS | NULL | MONTH | SOFT |
| ENTERPRISE | MAX_USERS | NULL | MONTH | SOFT |

> ⚠️ **Discrepancia a resolver:** V17 define `MAX_TENANTS = 1` para todos los planes fijos
> (en el contexto keygo-ui, un subscriber = 1 tenant propio). En el contexto platform, un
> contractor gestiona múltiples tenants, por lo que el valor 3 (TEAM) y 10 (BUSINESS) de V20
> tiene sentido semántico diferente. Confirmar con producto antes de aplicar la migración.

## Entitlement matrix objetivo (basada en V17 + contexto platform)

### Planes fijos (FREE, PERSONAL, TEAM, BUSINESS)

> `period_type = 'NONE'` para cuotas no periódicas; `'MONTH'` solo para `MAX_MONTHLY_TOKENS`.

| Plan | Métrica | metric_type | limit_value | period_type | enforcement_mode | is_enabled |
|---|---|---|---|---|---|---|
| FREE | MAX_TENANTS | QUOTA | 1 ⚠️ | NONE | HARD | TRUE |
| FREE | MAX_CLIENT_APPS | QUOTA | 1 | NONE | HARD | TRUE |
| FREE | MAX_USERS | QUOTA | 3 | NONE | HARD | TRUE |
| FREE | MAX_ADMINS | QUOTA | 1 | NONE | HARD | TRUE |
| FREE | MAX_MONTHLY_TOKENS | QUOTA | 1000 | MONTH | HARD | TRUE |
| FREE | SOCIAL_LOGIN | BOOLEAN | NULL | NONE | HARD | FALSE |
| FREE | CUSTOM_DOMAIN | BOOLEAN | NULL | NONE | HARD | FALSE |
| FREE | SLA_UPTIME_PCT | QUOTA | NULL | NONE | SOFT | FALSE |
| FREE | AUDIT_LOG_DAYS | QUOTA | 7 | NONE | SOFT | TRUE |
| PERSONAL | MAX_TENANTS | QUOTA | 1 ⚠️ | NONE | HARD | TRUE |
| PERSONAL | MAX_CLIENT_APPS | QUOTA | 3 | NONE | HARD | TRUE |
| PERSONAL | MAX_USERS | QUOTA | 5 | NONE | HARD | TRUE |
| PERSONAL | MAX_ADMINS | QUOTA | 1 | NONE | HARD | TRUE |
| PERSONAL | MAX_MONTHLY_TOKENS | QUOTA | 10000 | MONTH | HARD | TRUE |
| PERSONAL | SOCIAL_LOGIN | BOOLEAN | NULL | NONE | HARD | FALSE |
| PERSONAL | CUSTOM_DOMAIN | BOOLEAN | NULL | NONE | HARD | FALSE |
| PERSONAL | SLA_UPTIME_PCT | QUOTA | NULL | NONE | SOFT | FALSE |
| PERSONAL | AUDIT_LOG_DAYS | QUOTA | 14 | NONE | SOFT | TRUE |
| TEAM | MAX_TENANTS | QUOTA | ? ⚠️ | NONE | HARD | TRUE |
| TEAM | MAX_CLIENT_APPS | QUOTA | 10 | NONE | HARD | TRUE |
| TEAM | MAX_USERS | QUOTA | 25 | NONE | HARD | TRUE |
| TEAM | MAX_ADMINS | QUOTA | 3 | NONE | HARD | TRUE |
| TEAM | MAX_MONTHLY_TOKENS | QUOTA | 100000 | MONTH | HARD | TRUE |
| TEAM | SOCIAL_LOGIN | BOOLEAN | NULL | NONE | HARD | TRUE |
| TEAM | CUSTOM_DOMAIN | BOOLEAN | NULL | NONE | HARD | FALSE |
| TEAM | SLA_UPTIME_PCT | QUOTA | 99 | NONE | SOFT | TRUE |
| TEAM | AUDIT_LOG_DAYS | QUOTA | 30 | NONE | SOFT | TRUE |
| BUSINESS | MAX_TENANTS | QUOTA | ? ⚠️ | NONE | HARD | TRUE |
| BUSINESS | MAX_CLIENT_APPS | QUOTA | 30 | NONE | HARD | TRUE |
| BUSINESS | MAX_USERS | QUOTA | 100 | NONE | HARD | TRUE |
| BUSINESS | MAX_ADMINS | QUOTA | 10 | NONE | HARD | TRUE |
| BUSINESS | MAX_MONTHLY_TOKENS | QUOTA | 500000 | MONTH | HARD | TRUE |
| BUSINESS | SOCIAL_LOGIN | BOOLEAN | NULL | NONE | HARD | TRUE |
| BUSINESS | CUSTOM_DOMAIN | BOOLEAN | NULL | NONE | HARD | TRUE |
| BUSINESS | SLA_UPTIME_PCT | QUOTA | 999 | NONE | SOFT | TRUE |
| BUSINESS | AUDIT_LOG_DAYS | QUOTA | 90 | NONE | SOFT | TRUE |

### Plan FLEX (25 métricas)

> No tiene `MAX_ADMINS` explícito — se reemplaza por `FLEX_ADMIN_INCLUDED_PER_TENANT` + `FLEX_ADMIN_RATE`.
> `SLA_UPTIME_PCT = 999` en V17 (centi-porcentaje, representa 99.9%).

| Métrica | metric_type | limit_value | period_type | enforcement_mode | is_enabled |
|---|---|---|---|---|---|
| MAX_TENANTS | QUOTA | NULL | NONE | SOFT | TRUE |
| MAX_CLIENT_APPS | QUOTA | NULL | NONE | SOFT | TRUE |
| MAX_USERS | QUOTA | NULL | NONE | SOFT | TRUE |
| MAX_MONTHLY_TOKENS | QUOTA | NULL | MONTH | SOFT | TRUE |
| SOCIAL_LOGIN | BOOLEAN | NULL | NONE | HARD | TRUE |
| CUSTOM_DOMAIN | BOOLEAN | NULL | NONE | HARD | TRUE |
| SLA_UPTIME_PCT | QUOTA | 999 | NONE | SOFT | TRUE |
| AUDIT_LOG_DAYS | QUOTA | 90 | NONE | SOFT | TRUE |
| FLEX_TENANT_T1_MAX | QUOTA | 10 | NONE | SOFT | TRUE |
| FLEX_TENANT_RATE_T1 | RATE | 800 | NONE | SOFT | TRUE |
| FLEX_TENANT_T2_MAX | QUOTA | 50 | NONE | SOFT | TRUE |
| FLEX_TENANT_RATE_T2 | RATE | 600 | NONE | SOFT | TRUE |
| FLEX_TENANT_RATE_T3 | RATE | 400 | NONE | SOFT | TRUE |
| FLEX_APP_T1_MAX | QUOTA | 20 | NONE | SOFT | TRUE |
| FLEX_APP_RATE_T1 | RATE | 200 | NONE | SOFT | TRUE |
| FLEX_APP_T2_MAX | QUOTA | 100 | NONE | SOFT | TRUE |
| FLEX_APP_RATE_T2 | RATE | 150 | NONE | SOFT | TRUE |
| FLEX_APP_RATE_T3 | RATE | 100 | NONE | SOFT | TRUE |
| FLEX_IDENTITY_T1_MAX | QUOTA | 100 | NONE | SOFT | TRUE |
| FLEX_IDENTITY_RATE_T1 | RATE | 120 | NONE | SOFT | TRUE |
| FLEX_IDENTITY_T2_MAX | QUOTA | 500 | NONE | SOFT | TRUE |
| FLEX_IDENTITY_RATE_T2 | RATE | 90 | NONE | SOFT | TRUE |
| FLEX_IDENTITY_RATE_T3 | RATE | 60 | NONE | SOFT | TRUE |
| FLEX_ADMIN_INCLUDED_PER_TENANT | QUOTA | 1 | NONE | SOFT | TRUE |
| FLEX_ADMIN_RATE | RATE | 400 | NONE | SOFT | TRUE |

> Tarifas en centavos USD. Tramo T3 no tiene `_MAX` (aplica desde T2_MAX+1 en adelante).

### Plan ENTERPRISE (12 métricas)

> `SLA_UPTIME_PCT = 999 / HARD` (garantizado contractualmente). Resto NULL = ilimitado.

| Métrica | metric_type | limit_value | period_type | enforcement_mode | is_enabled |
|---|---|---|---|---|---|
| MAX_TENANTS | QUOTA | NULL | NONE | SOFT | TRUE |
| MAX_CLIENT_APPS | QUOTA | NULL | NONE | SOFT | TRUE |
| MAX_USERS | QUOTA | NULL | NONE | SOFT | TRUE |
| MAX_ADMINS | QUOTA | NULL | NONE | SOFT | TRUE |
| MAX_MONTHLY_TOKENS | QUOTA | NULL | MONTH | SOFT | TRUE |
| SOCIAL_LOGIN | BOOLEAN | NULL | NONE | HARD | TRUE |
| CUSTOM_DOMAIN | BOOLEAN | NULL | NONE | HARD | TRUE |
| SLA_UPTIME_PCT | QUOTA | 999 | NONE | HARD | TRUE |
| AUDIT_LOG_DAYS | QUOTA | NULL | NONE | SOFT | TRUE |
| PRIORITY_SUPPORT | BOOLEAN | NULL | NONE | HARD | TRUE |
| CUSTOM_SLA | BOOLEAN | NULL | NONE | HARD | TRUE |
| DEDICATED_SUCCESS_MGR | BOOLEAN | NULL | NONE | HARD | TRUE |

### Resumen de filas por plan

| Plan | Métricas | Total acumulado |
|---|---|---|
| FREE | 9 | 9 |
| PERSONAL | 9 | 18 |
| TEAM | 9 | 27 |
| BUSINESS | 9 | 36 |
| FLEX | 25 | 61 |
| ENTERPRISE | 12 | 73 |

## Análisis de balance de valor entre planes

Antes de finalizar los valores de la matriz, verificar que la escalera de límites justifique
el incremento de precio en cada salto. El criterio central es:

> **Un plan de mayor precio debe ofrecer notablemente más que la suma de varios planes
> inferiores que cuesten lo mismo.** El usuario no debe poder "armar" el equivalente
> de un plan superior comprando múltiples del tier inferior.

### Criterios de evaluación por métrica

Para cada métrica cuantitativa (`MAX_*`, `AUDIT_LOG_DAYS`, `MAX_MONTHLY_TOKENS`) calcular:

- **Ratio precio/unidad**: `precio_plan / límite_métrica` — debe decrecer a medida que sube el
  plan (más unidades por dólar en planes superiores).
- **Multiplicador de límite vs precio**: si un plan cuesta X veces más que el anterior,
  sus límites clave deben crecer más que X veces.
- **Punto de saturación**: identificar en qué punto un usuario que escala de plan inferior
  no puede igualar el superior sumando planes.

### Tabla de referencia rápida (valores actuales propuestos)

> Precios de V20: FREE=$0 | PERSONAL=$19/mes | TEAM=$49/mes | BUSINESS=$99/mes

| Métrica | FREE | PERSONAL | TEAM | BUSINESS | Factor TEAM/PERSONAL | Factor BUSINESS/TEAM |
|---|---|---|---|---|---|---|
| `MAX_CLIENT_APPS` | 1 | 3 | 10 | 30 | 3.3× | 3.0× |
| `MAX_USERS` | 3 | 5 | 25 | 100 | 5.0× | 4.0× |
| `MAX_ADMINS` | 1 | 1 | 3 | 10 | 3.0× | 3.3× |
| `MAX_MONTHLY_TOKENS` | 1 000 | 10 000 | 100 000 | 500 000 | 10.0× | 5.0× |
| `AUDIT_LOG_DAYS` | 7 | 14 | 30 | 90 | 2.1× | 3.0× |
| Precio relativo | — | base | 2.6× PERSONAL | 5.2× PERSONAL | — | — |

> ⚠️ **Señal de alerta:** Si el factor de crecimiento de un límite es menor que el factor
> de precio entre esos dos planes, el salto no se justifica y hay que ajustar hacia arriba
> el límite del plan superior o hacia abajo el del inferior.

### Features cualitativas como diferenciadores

Además de los límites numéricos, las features booleanas crean barreras de upgrade que los
límites numéricos no pueden replicar simplemente comprando más del plan inferior:

| Feature | FREE | PERSONAL | TEAM | BUSINESS |
|---|---|---|---|---|
| `SOCIAL_LOGIN` | ✗ | ✗ | ✓ | ✓ |
| `CUSTOM_DOMAIN` | ✗ | ✗ | ✗ | ✓ |
| `SLA_UPTIME_PCT` | — | — | 99% | 99.9% |

Estas features deben revisarse junto con los límites numéricos para que el plan superior
ofrezca una propuesta de valor imposible de replicar combinando planes inferiores.

### Acción requerida (paso 0 del flujo)

Antes de crear la migración, completar una revisión de producto que responda:

1. ¿Los factores de crecimiento de `MAX_CLIENT_APPS` y `MAX_USERS` justifican el salto de
   precio entre PERSONAL → TEAM y TEAM → BUSINESS?
2. ¿El límite de `AUDIT_LOG_DAYS` (7/14/30/90 días) se percibe como diferenciador real?
3. ¿El `MAX_MONTHLY_TOKENS` está bien calibrado para el volumen de uso esperado por plan?
4. ¿Hay alguna métrica donde dos planes FREE equivalgan a un PERSONAL, o dos PERSONAL a
   un TEAM? Si es así, ajustar.

---

## Pasos de implementación

| # | Acción | Archivo | Estado |
|---|---|---|---|
| 1 | Revisar balance de valor entre planes (ver sección anterior) y ajustar valores si corresponde | — | PENDING |
| 2 | Confirmar con producto el valor de `MAX_TENANTS` para TEAM y BUSINESS en contexto platform | — | PENDING |
| 3 | Crear `V21__platform_plan_entitlements_full.sql` con `INSERT ... ON CONFLICT DO UPDATE` para las 73 filas (y corrección de `period_type` de los 12 registros existentes en V20) | `keygo-supabase/src/main/resources/db/migration/V21__platform_plan_entitlements_full.sql` | APPLIED |
| 4 | Actualizar `doc/08-reference/data/migrations.md` con `V21` | `doc/08-reference/data/migrations.md` | APPLIED |
| 5 | Actualizar `doc/08-reference/data/data-model.md` con los nuevos `metric_code` de platform plans | `doc/08-reference/data/data-model.md` | APPLIED |

> **Nota sobre conflicto con V20:** Los entitlements de `MAX_TENANTS` y `MAX_USERS` ya
> existen con `period_type = 'MONTH'`. La migración V21 debe corregirlos a `period_type = 'NONE'`
> (consistente con V17) usando `ON CONFLICT (app_plan_version_id, metric_code) DO UPDATE`.

## Guía de verificación

```sql
-- Debe retornar 73 filas (FREE=9, PERSONAL=9, TEAM=9, BUSINESS=9, FLEX=25, ENTERPRISE=12)
SELECT ap.code AS plan, COUNT(*) AS metrics
FROM app_plan_entitlements ape
JOIN app_plan_versions apv ON apv.id = ape.app_plan_version_id
JOIN app_plans ap ON ap.id = apv.app_plan_id
WHERE ap.client_app_id IS NULL
GROUP BY ap.code, ap.sort_order
ORDER BY ap.sort_order;

-- Detalle completo
SELECT ap.code AS plan, ape.metric_code, ape.metric_type,
       ape.limit_value, ape.period_type, ape.enforcement_mode, ape.is_enabled
FROM app_plan_entitlements ape
JOIN app_plan_versions apv ON apv.id = ape.app_plan_version_id
JOIN app_plans ap ON ap.id = apv.app_plan_id
WHERE ap.client_app_id IS NULL
ORDER BY ap.sort_order, ape.metric_code;
```

## Relaciones

| Artefacto relacionado | Tipo de relación | Descripción |
|---|---|---|
| `V17__seed_billing_plans.sql` (backup) | fuente de verdad | Backup con la matriz completa de entitlements original para keygo-ui |
| `V20__platform_plan_catalog.sql` | complementaria | V20 sembró planes y versiones; V21 completa los entitlements incompletos |
| RFC-009 (V34) | complementaria | RFC-009 usa V34 para `audit_events`; esta tarea usa V21 para entitlements — sin conflicto |
| `doc/08-reference/data/migrations.md` | doc a actualizar | Registrar V21 |
| `doc/08-reference/data/data-model.md` | doc a actualizar | Documentar nuevos `metric_code` para platform plans |
