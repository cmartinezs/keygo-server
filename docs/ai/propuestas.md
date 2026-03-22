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
| ~~T-001~~ | ~~Corregir bug `BootstrapAdminKeyFilter` (`getRequestURI()` → `getServletPath()`)~~ | ✅ Completada 2026-03-21 |
| T-002 | Agregar mapper en `keygo-api/platform/` para descargar mapeo `ServiceInfoProvider → ServiceInfoData` al controller | 🔲 Pendiente |
| T-023 | Configurar lint/formato automático (Checkstyle / Spotless). Convención ya en `docs/development/CODE_STYLE.md` | 🔲 Pendiente |
| T-024 | Implementar `TenantResolutionStrategy` por path variable `/{tenantSlug}/` | ✅ Completada (Fases 5/6) |
| T-026 | Mantener colecciones Postman actualizadas; crear environment `KeyGo-Server-Docker` | 🔲 Pendiente |
| T-027 | Endpoints `PUT /tenants/{slug}/users/{userId}/suspend` y `/activate` | 🔲 Pendiente |
| T-028 | Tests de integración con Testcontainers para `UserRepositoryAdapter` | 🔲 Pendiente |
| T-030 | Agregar verificación de referencias Markdown rotas post-reorganización `docs/ai/` — script o check de links en `docs/ai/README.md` | 🔲 Pendiente |

---

## Mediano plazo

| ID | Propuesta | Estado |
|---|---|---|
| T-009 | Poblar `keygo-domain` con entidades puras: `Tenant`, `User`, `ClientApp`, `Membership` | 🟡 Parcial (Tenant, User, ClientApp ✅; Membership pendiente) |
| T-010 | Poblar `keygo-infra` con puertos: `PasswordHasherPort`, `TokenSignerPort`, `ClockProvider` | 🟡 Parcial (`PasswordHasherPort`, `TokenSignerPort`, `ClockProvider` ✅) |
| T-013 | Tests de integración con Testcontainers para `keygo-supabase` | 🔲 Pendiente |
| T-025 | Tests de integración con Testcontainers para flujo completo de Tenant | 🔲 Pendiente |
| T-031 | Automatizar verificación de links Markdown rotos en CI (p. ej. `markdown-link-check` o `lychee`) para detectar referencias entre documentos que ya no existen | 🔲 Pendiente |

---

## Largo plazo

| ID | Propuesta | Estado |
|---|---|---|
| T-017 | Renombrar `keygo-supabase` → `keygo-adapter-persistence-postgres` | 🔲 Pendiente |
| T-020 | Observabilidad avanzada: OpenTelemetry + Prometheus + Grafana | 🔲 Pendiente |
| T-032 | Evaluar generador de site estático (MkDocs / Docusaurus) que consolide `docs/` + archivos raíz en un portal navegable unificado con búsqueda | 🔲 Pendiente |
| F-010–F-016 | Core OAuth2/OIDC: authorize, token, JWKS, Auth Code + PKCE | ✅ Fases 5 y 6 completadas |

---

## Referencias

- **Registro primario con IDs y detalle:** [`ROADMAP.md`](../../ROADMAP.md)
- **Historial de completadas:** ver tabla "Historial de propuestas completadas" en `ROADMAP.md`

---

**Última actualización:** 2026-03-22 | **Responsable:** AI Agent

