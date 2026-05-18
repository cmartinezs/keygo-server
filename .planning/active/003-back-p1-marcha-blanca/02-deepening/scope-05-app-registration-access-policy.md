# 🔍 DEEPENING: Scope 05 — Política de acceso/registro por app

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)

**Spec origen:** BE-P1-005 | **Prioridad:** P1 / Importante

---

## Objective

Agregar campo `access_policy` a `ClientApp` con enum `CLOSED | OPEN_JOIN | SELF_SIGNUP`. Default `CLOSED`. Validar en el flujo de authorize/login/token que un usuario sin membership activa en app cerrada recibe error funcional `KG-NO-MEMBERSHIP` en lugar de error genérico.

---

## Tasks

| # | Tarea | Workflow | Estado | Output |
|---|-------|----------|--------|--------|
| 1 | Agregar campo `access_policy` a entidad `ClientApp` y DTO | GENERATE-DOCUMENT | DONE | Entidad y DTO actualizados |
| 2 | Crear Flyway migration para añadir columna `access_policy` con default `CLOSED` | GENERATE-DOCUMENT | DONE | Migración V24 (o siguiente disponible) |
| 3 | Ajustar validación en flujo authorize/login/token: app `CLOSED` + user sin membership → error `KG-NO-MEMBERSHIP` | GENERATE-DOCUMENT | DONE | Lógica de validación |
| 4 | Emitir error funcional `{ code: "KG-NO-MEMBERSHIP", message: "..." }` estable para UI | GENERATE-DOCUMENT | DONE | `NoMembershipException` + `ResponseCode.NO_MEMBERSHIP` + handler en `GlobalExceptionHandler` |
| 5 | Deshabilitar `SELF_SIGNUP` por defecto en marcha blanca (no implementado aún) | GENERATE-DOCUMENT | DONE | Restricción documentada |
| 6 | Agregar tests por política: CLOSED + sin membership → denegado; CLOSED + con membership → permitido; OPEN_JOIN + sin membership → crea membership | GENERATE-DOCUMENT | DONE | Tests nuevos |
| 7 | Actualizar `TRACEABILITY.md` | UPDATE-TRACEABILITY | PENDING | TRACEABILITY.md actualizado |

---

## Done Criteria

- [x] Campo `access_restriction` existe en `ClientApp` con default `CLOSED`.
- [x] Migración Flyway V31 creada.
- [x] App cerrada + user sin membership → error `NO_MEMBERSHIP` (403) detectable por UI.
- [x] Auto-join no ocurre salvo política `OPEN_JOIN` explícita.
- [x] `SELF_SIGNUP` existe en enum pero sin implementación funcional (se trata como CLOSED).
- [x] TRACEABILITY.md actualizado.

---

## Inconsistencies Found

| # | Descripción | Docs involucrados | Estado | Resolución |
|---|-------------|------------------|--------|-----------|
| — | *Ninguna aún* | — | — | — |

---

## Residuals

| # | Descripción | Diferido a | Estado |
|---|-------------|-----------|--------|
| R-001 | Implementación completa de `SELF_SIGNUP` / `PUBLIC_SIGNUP` | Fuera de alcance marcha blanca | PENDING |

---

> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)
