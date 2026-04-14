# T-139 — Corregir uso de `Object` y `Object[]` en firmas y genéricos

**Estado:** ⬜ Registrada
**Horizonte:** corto plazo
**Módulos afectados:** `keygo-domain`, `keygo-app`, `keygo-api`, `keygo-infra`, `keygo-supabase`, docs

## Requisito

Revisar y corregir el uso de `Object`, `Object[]` y genéricos parametrizados con `Object`
cuando aparezcan como:

1. parámetros de métodos
2. tipos de retorno
3. parametría de colecciones, maps, wrappers o utilidades genéricas

La meta es reemplazar esas firmas por contratos tipados y explícitos (`DTO`, `result`, `record`,
value object, proyección o interfaz dedicada) para mejorar seguridad de tipos, legibilidad y
mantención.

## Relaciones

| Artefacto relacionado | Tipo de relación | Descripción |
|---|---|---|
| `T-138` | `complementaria` | Ambas tareas endurecen la calidad del código y de los tests evitando contratos ambiguos y fallas tardías. |

## Decisiones a tomar antes de activar

Antes de planificar la implementación, resolver:

1. Si habrá excepciones acotadas para APIs de muy bajo nivel donde `Object` sea técnicamente
   inevitable y cómo documentarlas.
2. Si el barrido incluirá también tests, fixtures y utilidades internas además del código productivo.
3. Si se incorporará una regla automática (Checkstyle, ArchUnit, inspección IDE o revisión CI)
   para evitar reintroducir firmas crudas.
4. Cómo modelar consultas nativas/proyecciones JPA que hoy dependan de `Object[]`.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
