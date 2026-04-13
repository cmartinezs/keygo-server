# INC-H03 — Flujo login multi-tenant ambiguo en AUTH_FLOW y FRONTEND_DEVELOPER_GUIDE

**Categoría:** docs
**Criticidad:** 🟡 No crítica
**Estado:** ✅ Resuelta
**Detectada:** 2026-03-26
**Resuelta:** 2026-03-26
**Tarea relacionada:** —

## Descripción

`AUTH_FLOW.md` y `FRONTEND_DEVELOPER_GUIDE.md` describían el flujo de login central
de forma ambigua, generando dos interpretaciones incorrectas: (1) que siempre se
autenticaba contra el tenant `keygo`, y (2) que la UI central almacenaba la sesión
y los tokens OAuth.

## Esperado vs. Real

| | Documentado (ambiguo) | Real / patrón correcto |
|---|---|---|
| Contexto del login | Podía leerse como sesión propia de la UI central | La UI central es solo *hosted login*; el cliente OAuth real es la app origen |
| Tenant del flujo | Ambiguo — podía inferirse siempre `keygo` | El flujo pertenece al `tenantSlug` + `client_id` de la app que inició el authorize |
| Canje del code | Ambiguo sobre quién almacena los tokens | La app origen canjea el `code` en `/oauth2/token` y conserva sus propios tokens |

## Corrección

`AUTH_FLOW.md` y `frontend-developer-guide.md` actualizados para dejar explícito
que la UI central actúa como proveedor de interfaz, no como cliente OAuth efectivo.
El contexto del tenant deriva siempre del `client_id` de la app origen.
