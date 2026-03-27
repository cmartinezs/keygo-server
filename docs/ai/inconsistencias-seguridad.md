# INCONSISTENCIAS — Seguridad / Autenticación

> Sub-documento de [`inconsistencias.md`](inconsistencias.md).
>
> Registra **inconsistencias encontradas entre la documentación de seguridad/autenticación y el comportamiento real del backend**.
>
> Fecha de detección: **2026-03-26** | Revisión: flujo OAuth2/OIDC y filtro `BootstrapAdminKeyFilter`

---

## Estado: 🟡 Corrección parcial aplicada (1 corregida, 3 pendientes)

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

### 3. `FRONTEND_DEVELOPER_GUIDE.md` y `AUTH_FLOW.md` podían inducir a usar siempre el tenant `keygo` en logins compartidos

| Campo | Documentado antes | Real / patrón correcto |
|---|---|---|
| Reutilizar login de `keygo-ui` | Podía interpretarse como autenticar siempre contra tenant `keygo` | La UI puede ser compartida, pero el flujo OAuth sigue perteneciendo al `tenantSlug` + `client_id` de la app origen |
| Canje de tokens | Ambiguo; podía inferirse que la UI central almacenaba la sesión final | La app origen debe canjear `code` en `/oauth2/token` y conservar sus propios tokens |
| Contexto OAuth2 | Mezcla entre "app visual" y "cliente OAuth final" | La UI central es solo hosted login; el cliente OAuth efectivo sigue siendo la app origen |

**Impacto:** Una implementación frontend multi-tenant podía terminar solicitando tokens para el tenant equivocado o guardando tokens en una UI que no es la consumidora final.
**Archivos afectados:** `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`, `docs/api/AUTH_FLOW.md`
**Estado:** ✅ Corregida el 2026-03-26.

---

### 4. Dos inicializadores de signing key activos en el perfil `supabase` — duplicación redundante

| Campo | `SigningKeyBootstrapService` | `SigningKeyInitializer` |
|---|---|---|
| Paquete | `keygo-run/config/auth/` | `keygo-run/startup/` |
| Perfil | `@Profile("supabase")` | `@Profile({"supabase", "local"})` |
| Mecanismo | `@EventListener(ApplicationReadyEvent)` | `ApplicationRunner` |
| Estado | 🔴 Redundante / candidato a eliminar | ✅ Correcto — cubre ambos perfiles |

**Impacto:** En el perfil `supabase`, ambas clases se instancian y ambas corren al startup. La segunda encuentra la clave generada por la primera y hace no-op. Es inofensivo pero confunde a quien mantiene el código y viola el principio de responsabilidad única en bootstrap.  
**Causa real del bug:** Con perfil `local`, `SigningKeyBootstrapService` no corre (solo `supabase`), y `SigningKeyInitializer` tenía `@Profile("supabase")` — ambas fallaban en `local` → error `No active signing key found` al emitir cualquier JWT.  
**Fix aplicado:** `SigningKeyInitializer` ahora declara `@Profile({"supabase", "local"})`.  
**Acción pendiente:** Eliminar `SigningKeyBootstrapService` (ver T-067 en ROADMAP).  
**Archivos afectados:**
- `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/auth/SigningKeyBootstrapService.java`
- `keygo-run/src/main/java/io/cmartinezs/keygo/run/startup/SigningKeyInitializer.java`

---

**Última actualización:** 2026-03-27  **Responsable:** AI Agent

