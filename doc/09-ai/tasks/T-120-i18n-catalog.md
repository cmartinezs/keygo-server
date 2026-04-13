# T-120 — Diseño de catálogo i18n para respuestas API

**Estado:** 🟡 PARTIAL
**Horizonte:** largo plazo
**Módulos afectados:** `keygo-api`, `keygo-run`, docs

## Requisito

Crear estructura `i18n/messages_XX.properties` con locales: `es`, `es-CL`, `en-US` (fallback),
`pt_BR`, `fr`. Ver diseño previo en `doc/99-archive/deprecated/design/I18N_STRATEGY.md`.

## Estado actual

- Archivos de properties creados ✅.
- T-122 (integración `MessageSource` en `ApiErrorDataFactory`) ✅ completada.
- T-123 (tests de i18n) ✅ completada.
- Pendiente: completar cobertura de locales y mensajes faltantes.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
