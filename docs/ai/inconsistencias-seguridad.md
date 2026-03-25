# INCONSISTENCIAS — Seguridad / Autenticación

> Sub-documento de [`inconsistencias.md`](inconsistencias.md).
>
> Registra **inconsistencias encontradas entre la documentación de seguridad/autenticación y el comportamiento real del backend**.
>
> Fecha de detección: **2026-03-25** | Revisión: flujo OAuth2/OIDC y filtro `BootstrapAdminKeyFilter`

---

## Estado: 🔲 Pendiente de corrección documental

Estas inconsistencias no bloquean runtime, pero sí pueden confundir a quien implemente clientes o revise la seguridad actual del sistema.

---

## Inconsistencias encontradas

### 1. `ARCHITECTURE.md` sigue describiendo autenticación admin con `X-KEYGO-ADMIN`

| Campo | Documentado | Real actual |
|---|---|---|
| Protección de `/api/**` | Header `X-KEYGO-ADMIN` | `Authorization: Bearer <jwt>` |
| Autorización | No detallada / implícita en filtro | `@PreAuthorize` + roles `ADMIN` / `ADMIN_TENANT` |
| Aislamiento tenant | No documentado | validación `tenant_slug` del token vs `tenantSlug` en path |

**Impacto:** La arquitectura rápida de referencia puede inducir a pensar que los endpoints admin todavía aceptan bootstrap key, cuando el comportamiento real ya migró a Bearer-only.
**Archivos afectados:** `ARCHITECTURE.md`, `keygo-run/src/main/java/io/cmartinezs/keygo/run/filter/BootstrapAdminKeyFilter.java`, `keygo-api/src/main/java/io/cmartinezs/keygo/api/security/TenantAuthorizationEvaluator.java`
**Acción sugerida:** Actualizar la sección `## Seguridad` de `ARCHITECTURE.md` para reflejar Bearer-only + RBAC por endpoint.

---

### 2. `docs/api/BOOTSTRAP_FILTER.md` quedó desalineado con el filtro actual

| Campo | Documentado | Real actual |
|---|---|---|
| Mecanismo principal | `X-KEYGO-ADMIN` | `Authorization: Bearer <jwt>` |
| Diagrama de flujo | valida admin key | valida Bearer JWT + authorities |
| Ejemplos curl | usan `X-KEYGO-ADMIN` | deben usar `Authorization: Bearer ...` |
| Rutas públicas OAuth2 | parciales / antiguas | incluyen `/oauth2/authorize`, `/account/login`, `/oauth2/token`, `/oauth2/revoke`, `/userinfo`, `/.well-known/*` |

**Impacto:** La guía específica del filtro ya no representa el comportamiento del código y puede provocar pruebas manuales equivocadas o diagnósticos incorrectos.
**Archivos afectados:** `docs/api/BOOTSTRAP_FILTER.md`, `keygo-run/src/main/java/io/cmartinezs/keygo/run/filter/BootstrapAdminKeyFilter.java`, `keygo-run/src/test/java/io/cmartinezs/keygo/run/filter/BootstrapAdminKeyFilterTest.java`
**Acción sugerida:** Actualizar el documento completo del filtro para reflejar seguridad Bearer-only, categorías de rutas públicas y ejemplos correctos.

---

**Última actualización:** 2026-03-25 | **Responsable:** AI Agent

