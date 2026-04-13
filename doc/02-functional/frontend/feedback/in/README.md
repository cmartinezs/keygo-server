# Feedback IN — UI → Backend

Gaps e inconsistencias detectados por el equipo de UI al consumir la API.

## Índice

| Archivo | Estado | Resumen |
|---|---|---|
| [UI-001-missing-platform-users-list-endpoint.md](UI-001-missing-platform-users-list-endpoint.md) | 🟢 Resuelto | La UI reportó que `GET /platform/users` no estaba implementado; backend lo habilitó con paginación. |
| [UI-002-missing-platform-user-roles-endpoint.md](UI-002-missing-platform-user-roles-endpoint.md) | 🔴 Abierto | La UI reportó que no existe `GET /platform/users/{userId}/platform-roles` para consultar roles asignados. |

## Cómo agregar una entrada

1. Crear `UI-NNN-<slug>.md` en esta carpeta.
2. Usar la plantilla de abajo.
3. Agregar fila en la tabla de índice de este `README.md`.
4. Cuando la entrada quede **🟢 Resuelto**, anotar la tarea, RFC o cambio que la resolvió.

## Plantilla

```markdown
# UI-NNN — <título corto>

**Fecha:** YYYY-MM-DD  
**Estado:** 🔴 Abierto  
**Contexto:** <pantalla o flujo donde se detectó>

## Problema

<descripción clara del gap>

## Comportamiento esperado

<qué debería devolver o exponer el backend>

## Resolución

_Pendiente._

**Tarea/RFC que lo resolvió:** _Pendiente._

<!-- Completar cuando se resuelva: qué se hizo y la referencia T-NNN / RFC-NNN / ADR / artefacto aplicable -->
```
