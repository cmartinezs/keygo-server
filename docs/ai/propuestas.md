# AI Context — Propuestas de Mejoras Futuras

> Sub-documento de [`AI_CONTEXT.md`](../../AI_CONTEXT.md).
>
> Registra **propuestas técnicas y funcionales** organizadas por horizonte temporal.
> El registro primario con IDs (`T-NNN`, `F-NNN`) está en [`ROADMAP.md`](../../ROADMAP.md).
> Esta sección es un **resumen de estado rápido** para el agente.
>
> **⚠️ Regla de actualización:** Al concluir cualquier tarea, evaluar si hay propuestas
> nuevas o propuestas completadas y actualizar este archivo + `ROADMAP.md`.

---

## Corto plazo

| ID | Propuesta | Estado |
|---|---|---|
| T-002 | Agregar mapper en `keygo-api/platform/` para descargar mapeo `ServiceInfoProvider → ServiceInfoData` al controller | 🔲 Pendiente |
| T-023 | Configurar lint/formato automático (Checkstyle / Spotless). Convención ya en `docs/development/CODE_STYLE.md` | 🔲 Pendiente |
| ~~T-024~~ | ~~Implementar `TenantResolutionStrategy` por path variable `/{tenantSlug}/`~~ | ✅ Completada (Fases 5/6) |
| ~~T-027~~ | ~~Integrar Swagger / OpenAPI con SpringDoc 3.0.1~~ | ✅ Completada 2026-03-21 |
| ~~T-027~~ | ~~Refresh token grant + revocación RFC 7009 + userinfo OIDC~~ | ✅ Completada 2026-03-22 (Fase 7) |
| T-026 | Mantener colecciones Postman actualizadas; crear environment `KeyGo-Server-Docker` | 🔲 Pendiente |
| T-028 | Migrar gestión de clave privada RSA a KMS externo (AWS KMS, Azure Key Vault, HashiCorp Vault) | 🔲 Pendiente |
| T-030 | Agregar verificación de referencias Markdown rotas post-reorganización `docs/ai/` | 🔲 Pendiente |
| T-033 | Endpoints `PUT /api/v1/tenants/{slug}/users/{userId}/suspend` y `/activate` | 🔲 Pendiente |
| T-034 | Tests de regresión en `BootstrapAdminKeyFilterTest` para los nuevos sufijos `/userinfo` y `/oauth2/revoke` como rutas públicas | 🔲 Pendiente |
| T-035 | Detección de replay attack: al recibir un refresh token en estado `USED`, revocar toda la cadena de sesión automáticamente | 🔲 Pendiente |

---

## Mediano plazo

| ID | Propuesta | Estado |
|---|---|---|
| T-009 | Poblar `keygo-domain` con entidades puras: `Tenant`, `User`, `ClientApp`, `Membership` | 🟡 Parcial (Tenant, User, ClientApp ✅; Membership pendiente) |
| T-010 | Poblar `keygo-infra` con puertos: `PasswordHasherPort`, `TokenSignerPort`, `ClockProvider` | 🟡 Parcial (`PasswordHasherPort`, `TokenSignerPort`, `ClockProvider` ✅) |
| T-013 | Tests de integración con Testcontainers para `keygo-supabase` | 🔲 Pendiente |
| T-025 | Tests de integración con Testcontainers para flujo completo de Tenant | 🔲 Pendiente |
| T-031 | Automatizar verificación de links Markdown rotos en CI (p. ej. `markdown-link-check` o `lychee`) | 🔲 Pendiente |
| T-036 | TTL configurable para refresh tokens y sesiones vía `application.yml` (actualmente fijo a 30 días en `AuthorizationController`) | 🔲 Pendiente |
| T-037 | Endpoint `GET /api/v1/tenants/{slug}/sessions` + `DELETE /…/{sessionId}` para que el administrador pueda listar y terminar sesiones activas de usuarios | 🔲 Pendiente |

---

## Largo plazo

| ID | Propuesta | Estado |
|---|---|---|
| T-017 | Renombrar `keygo-supabase` → `keygo-adapter-persistence-postgres` | 🔲 Pendiente |
| T-020 | Observabilidad avanzada: OpenTelemetry + Prometheus + Grafana | 🔲 Pendiente |
| T-032 | Evaluar generador de site estático (MkDocs / Docusaurus) que consolide `docs/` + archivos raíz en un portal navegable unificado con búsqueda | 🔲 Pendiente |
| T-038 | Lista negra de JTI de access tokens revocados con TTL en Redis para revocación inmediata sin esperar expiración natural | 🔲 Pendiente |
| ~~T-039~~ | ~~Soporte de `client_credentials` grant (Fase 8) — emite access token sin usuario para comunicación M2M~~ | ✅ Completada 2026-03-23 (Fase 8) |
| F-010–F-016 | Core OAuth2/OIDC: authorize, token, JWKS, Auth Code + PKCE | ✅ Fases 5 y 6 completadas |
| ~~F-025~~ | ~~`client_credentials` grant M2M sin usuario final~~ | ✅ Completada 2026-03-23 (Fase 8) |
| ~~F-027~~ | ~~Refresh tokens con rotación + SHA-256 hash~~ | ✅ Completada 2026-03-22 (Fase 7) |
| ~~F-028~~ | ~~Endpoint `/userinfo` OIDC §5.3~~ | ✅ Completada 2026-03-22 (Fase 7) |

---

## Referencias

- **Registro primario con IDs y detalle:** [`ROADMAP.md`](../../ROADMAP.md)
- **Historial de completadas:** ver tabla "Historial de propuestas completadas" en `ROADMAP.md`

---

**Última actualización:** 2026-03-23 (Fase 8 completada) | **Responsable:** AI Agent

