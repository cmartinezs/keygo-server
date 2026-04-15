# Plantilla de RFCs — RFC-NNN

## Reglas de nombrado e identificación

### Identificador correlativo

- Formato: `RFC-NNN` donde `NNN` es un entero secuencial de **al menos 3 dígitos** con cero a la
  izquierda (001, 008, 012…).
- El número es **único y permanente**: nunca se reutiliza aunque el RFC sea rechazado o archivado.
- Para determinar el próximo correlativo: leer el mayor `RFC-NNN` registrado en
  `rfcs/README.md` y sumar 1.

### Correlativos retroactivos (RFCs anteriores a este sistema)

Los RFCs creados antes de establecer este correlativo reciben un número retroactivo asignado
por orden de creación aproximado. No se renombran sus carpetas; solo se agrega `RFC-NNN` en
sus `README.md`.

| RFC-NNN | Carpeta |
|---|---|
| RFC-001 | `restructure-multitenant/` |
| RFC-002 | `restructure-implementation/` |
| RFC-003 | `billing-contractor-refactor/` |
| RFC-004 | `ddl-full-refactor/` |
| RFC-005 | `account-ui-proposal/` |
| RFC-006 | `t108-geoip-sessions/` |
| RFC-007 | `incomplete-sections/` |
| RFC-008 | `rbac-multi-scope-alignment/` |

> El próximo RFC nuevo es **RFC-010**.

### Nombre de carpeta

```
rfc-NNN-<slug>/
```

- `<slug>`: kebab-case, en inglés, 2–5 palabras que describen el dominio o cambio del RFC.
- Ejemplos válidos: `rfc-009-tenant-rbac-api/`, `rfc-010-platform-auth-refactor/`.
- Los RFCs anteriores a este sistema mantienen su carpeta original; los nuevos usan el prefijo.

### Archivos dentro de la carpeta

| Archivo | Obligatorio | Contenido |
|---|---|---|
| `README.md` | Sí | Encabezado, objetivo, índice de documentos, decisiones clave, relaciones y transiciones |
| `01-context.md` | Sí | Estado actual, gap detectado, motivación del RFC |
| `02-design.md` | Sí | Propuesta objetivo: qué cambia, cómo y dónde |
| `03-implementation-plan.md` | Sí | Fases, subtareas por fase y estado interno |
| `04-impact-and-acceptance.md` | Sí | Impacto por módulo, criterios de aceptación, migraciones |

Se pueden agregar archivos adicionales (`05-`, `06-`, …) si el RFC es suficientemente complejo.

### Actualización del índice

Al crear un RFC, agregar inmediatamente una entrada en `rfcs/README.md`:

```markdown
- RFC-NNN — [<Título>](rfc-NNN-<slug>/README.md)
```

---

## Plantilla — `README.md` del RFC

> Copiar desde aquí. Reemplazar cada `{{ ... }}`. Eliminar instrucciones entre `<!-- ... -->`.

---

```markdown
# RFC-NNN — {{ Título descriptivo del RFC }}

> **Estado:** DRAFT
> **Origen:** <!-- tarea T-NNN, propuesta directa o iniciativa técnica -->
> **Módulos afectados:** <!-- keygo-domain, keygo-app, keygo-api, keygo-supabase, keygo-run, docs -->
> **Migración Flyway:** <!-- Probable VNN si aplica; "No aplica" si no toca datos -->

## Objetivo

<!-- Qué brecha o necesidad resuelve este RFC. Máximo 3–4 líneas.
     No describir la solución aquí; eso va en 02-design.md. -->

## Documentos

1. [01-context.md](01-context.md) — estado actual y motivación
2. [02-design.md](02-design.md) — propuesta objetivo
3. [03-implementation-plan.md](03-implementation-plan.md) — fases y subtareas
4. [04-impact-and-acceptance.md](04-impact-and-acceptance.md) — impacto y criterios de aceptación

## Decisiones clave propuestas

<!-- Tabla resumen de las decisiones de diseño más importantes del RFC.
     Completar durante el análisis; puede estar vacía al momento de registrar. -->

| Tema | Decisión propuesta | Motivo |
|---|---|---|
| <!-- área --> | <!-- qué se decide --> | <!-- por qué --> |

## Relaciones

<!-- Relaciones con tareas, otros RFCs o inconsistencias.
     Usar los tipos definidos en doc/09-ai/workflow.md#relaciones-entre-tareas. -->

| Artefacto | Tipo de relación | Descripción |
|---|---|---|
| <!-- T-NNN / RFC-NNN / INC-NNN --> | <!-- derivada de · habilita · relacionada con --> | <!-- breve descripción --> |

## Tareas derivadas anticipadas

<!-- RFC grande que habilita trabajo parcial antes de su aprobación formal.
     Omitir si no aplica. -->

| Tarea | Fase RFC relacionada | Estado |
|---|---|---|
| <!-- [T-NNN](../../../09-ai/tasks/T-NNN-slug.md) --> | <!-- Pre-Fase X --> | ⬜ Registrada |

---

## Historial de transiciones

<!-- Sección acumulativa. Agregar al final en cada transición.
     Formato: ### YYYY-MM-DD — {{ emoji estado }} {{ Nombre del estado }} -->
```

---

## Plantilla — `01-context.md`

```markdown
# 01. Contexto y estado actual

## Situación actual

<!-- Qué existe hoy: tablas, use cases, controladores, documentación. -->

## Gap o problema detectado

<!-- Qué falta, está roto, desalineado o incompleto. Ser específico: nombrar archivos, clases,
     migraciones, contratos. Puede usar subsecciones numeradas (2.1, 2.2…) para cada gap. -->

## Lectura sintética

<!-- 3–5 bullets que resumen qué debe cambiar y por qué. -->
```

---

## Plantilla — `02-design.md`

```markdown
# 02. Diseño objetivo

## Decisiones de diseño

<!-- Para cada decisión relevante: qué se propone y por qué se descartaron alternativas. -->

## Componentes afectados

<!-- Por módulo: qué se crea, modifica o elimina. -->

| Módulo | Cambio | Descripción |
|---|---|---|
| `keygo-domain` | <!-- crear / modificar / eliminar --> | <!-- descripción --> |
| `keygo-app` | <!-- ... --> | <!-- ... --> |
| `keygo-api` | <!-- ... --> | <!-- ... --> |
| `keygo-supabase` | <!-- ... --> | <!-- ... --> |
| `keygo-run` | <!-- ... --> | <!-- ... --> |

## Contrato HTTP (si aplica)

<!-- Nuevos endpoints o cambios a contratos existentes.
     Formato: método + path + request/response shape + códigos. -->
```

---

## Plantilla — `03-implementation-plan.md`

```markdown
# 03. Plan de implementación

<!-- Por cada fase: objetivo, listado de subtareas con estado PENDING / APPLIED. -->

## Fase A — {{ Nombre de la fase }}

### Objetivo

<!-- Qué resuelve esta fase. -->

### Subtareas

| # | Subtarea | Archivo clave | Estado |
|---|---|---|---|
| 1 | <!-- descripción --> | <!-- ruta --> | PENDING |

<!-- Repetir bloque de fase para B, C, D… según corresponda. -->

## Orden sugerido

```text
Fase A → Fase B → Fase C
```

Razón: <!-- por qué este orden y no otro. -->

## Tareas derivadas anticipadas

<!-- Partes implementables antes de la aprobación formal del RFC.
     Omitir si no aplica. -->

| Tarea | Fase relacionada | Estado |
|---|---|---|
| <!-- [T-NNN](../../../09-ai/tasks/T-NNN-slug.md) --> | <!-- Pre-Fase A --> | ⬜ Registrada |
```

---

## Plantilla — `04-impact-and-acceptance.md`

```markdown
# 04. Impacto y criterios de aceptación

## Impacto por módulo

| Módulo | Impacto | Detalle |
|---|---|---|
| `keygo-domain` | <!-- Alto / Medio / Bajo / Ninguno --> | <!-- qué cambia --> |
| `keygo-app` | <!-- ... --> | <!-- ... --> |
| `keygo-api` | <!-- ... --> | <!-- ... --> |
| `keygo-supabase` | <!-- ... --> | <!-- ... --> |
| `keygo-run` | <!-- ... --> | <!-- ... --> |
| `docs` | <!-- ... --> | <!-- ... --> |

## Migraciones Flyway

<!-- VNN: descripción de los cambios DDL.
     "No aplica" si el RFC no toca el modelo de datos. -->

## Documentación afectada

<!-- Lista de documentos canónicos que deben actualizarse al cerrar el RFC. -->

- [ ] <!-- doc/02-functional/... -->
- [ ] <!-- doc/03-architecture/... -->
- [ ] <!-- doc/08-reference/data/... -->

## Feedback de salida a UI (si aplica)

<!-- Si el RFC introduce o modifica endpoints consumibles por UI, crear:
     doc/02-functional/frontend/feedback/BE-NNN-<slug>.md
     y actualizar feedback/README.md. -->

- [ ] `BE-NNN-<slug>.md` pendiente de crear al implementar la fase correspondiente.

## Criterios de aceptación

<!-- Lista verificable de condiciones que deben cumplirse para cerrar el RFC. -->

- [ ] <!-- criterio 1 -->
- [ ] <!-- criterio 2 -->
- [ ] <!-- criterio 3 -->
```
