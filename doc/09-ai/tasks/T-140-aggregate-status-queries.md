# T-140 — Preferir queries agregadas por estado en vez de consultas unitarias por estado

**Estado:** ⬜ Registrada
**Horizonte:** corto plazo
**Módulos afectados:** `keygo-app`, `keygo-supabase`, potencialmente `keygo-api`, docs

## Requisito

Revisar los casos donde hoy existen queries o puertos del tipo `getXByStatus(status)`,
`countXByStatus(status)` o variantes equivalentes invocadas repetidamente para cada estado, y
reemplazarlos cuando corresponda por consultas agregadas en base de datos, por ejemplo:

- `getTenantGroupByStatus()`
- `countTenantsGroupByStatus()`
- `Map<Status, Long> countByStatus()`

La preferencia es resolver el agrupamiento en la BD con `GROUP BY`, retornando estructuras
tipadas y listas para consumo del caso de uso, en vez de disparar una query por cada estado.

## Relaciones

| Artefacto relacionado | Tipo de relación | Descripción |
|---|---|---|
| `T-074` | `complementaria` | Reducir queries repetidas y cachear dashboards apuntan al mismo objetivo de eficiencia de lectura. |

## Decisiones a tomar antes de activar

Antes de planificar la implementación, resolver:

1. Qué repositorios/puertos tienen hoy mayor impacto por patrón N-consultas-por-estado.
2. Qué forma contractual se usará en app/domain (`Map`, DTO agregado, proyección o `result` dedicado).
3. En qué casos `getByStatus(status)` debe mantenerse por necesidades funcionales legítimas y no
   por simple conteo agregado.
4. Si conviene acompañar el cambio con benchmarks o métricas de query count para los flujos críticos.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
