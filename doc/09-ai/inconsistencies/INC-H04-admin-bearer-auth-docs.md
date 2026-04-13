# INC-H04 — Documentación operativa admin describe X-KEYGO-ADMIN como mecanismo vigente

**Categoría:** seguridad
**Criticidad:** 🟡 No crítica
**Estado:** ✅ Resuelta
**Detectada:** 2026-03-26
**Resuelta:** 2026-04-09
**Tarea relacionada:** —

## Descripción

Dos documentos operativos (`ARCHITECTURE.md` y `docs/api/BOOTSTRAP_FILTER.md`) describían
el header `X-KEYGO-ADMIN` como el mecanismo de autenticación para rutas admin, cuando el
sistema ya operaba con `Authorization: Bearer <jwt>`. El código era correcto; los docs, no.

Cubre los ítems #1 (`ARCHITECTURE.md`) y #2 (`BOOTSTRAP_FILTER.md`) de la auditoría de seguridad.

## Esperado vs. Real

| Documento | Documentado | Real |
|---|---|---|
| `ARCHITECTURE.md` | Protección `/api/**` via `X-KEYGO-ADMIN` | `Authorization: Bearer <jwt>` + `@PreAuthorize` |
| `BOOTSTRAP_FILTER.md` | Diagrama y ejemplos curl con `X-KEYGO-ADMIN` | Bearer JWT + authorities validation |
| Rutas públicas OAuth2 | Lista parcial / antigua | Incluye `/oauth2/authorize`, `/account/login`, `/oauth2/token`, `/oauth2/revoke`, `/userinfo`, `/.well-known/*` |

## Corrección

`doc/03-architecture/architecture.md` y `doc/03-architecture/security/bootstrap-filter.md`
actualizados para reflejar el mecanismo Bearer JWT real, incluyendo diagrama de flujo del
filtro y lista completa de rutas públicas. Corrección aplicada durante el refactoring
documental canónico del 2026-04-09.
