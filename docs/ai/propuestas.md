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

> **Prioridad para lanzamiento de KeyGo:** endurecer primero el hosted login actual y eliminar ambigüedad en el handoff entre app origen y `keygo-ui` central.

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
| ~~T-041~~ | ~~Agregar V13 y extender `TenantUserEntity` con 6 campos OIDC estándar~~ | ✅ Completada 2026-03-24 (Fase 9b) |
| ~~T-042~~ | ~~Implementar endpoints self-service de perfil: GET y PATCH `/account/profile` con Bearer token~~ | ✅ Completada 2026-03-24 (Fase 9b) |
| T-043 | Extender `GetUserInfoUseCase` para filtrar claims por scope solicitado (`profile`, `email`, `phone`) | 🔲 Pendiente |
| T-049 | Agregar request Postman `GET /api/v1/tenants/{slug}/apps/{clientId}/roles` con `pm.test()` de status 200, estructura `BaseResponse` y validación de lista | 🔲 Pendiente |
| T-051 | Suite de autorización por endpoint (`@PreAuthorize`) con matriz rol/tenant (ADMIN, ADMIN_TENANT match/mismatch, USER_TENANT) | 🔲 Pendiente |
| T-053 | Script SQL de verificación post-seed V14 (conteos por tenant/app/roles/memberships) para validación rápida local/CI | 🔲 Pendiente |
| T-061 | Externalizar lista de orígenes CORS por ambiente: documentar `KEYGO_CORS_ALLOWED_ORIGINS_0` en `.env.example` y `ENVIRONMENT_SETUP.md`; perfil `prod` con lista vacía (denegación por defecto) | 🔲 Pendiente |
| T-062 | Agregar handler específico para `MissingServletRequestParameterException` y responder `400 INVALID_INPUT` (evitar `500 OPERATION_FAILED` en casos de parámetro faltante) | 🔲 Pendiente |
| T-065 | Agregar `fieldErrors` (lista de campos inválidos) cuando `origin=CLIENT_REQUEST` y `clientRequestCause=USER_INPUT` | 🔲 Pendiente |
| T-068 | Agregar test unitario de `PlatformStatsController`: mockar `GetPlatformStatsUseCase`, verificar status 200, `PLATFORM_STATS_RETRIEVED` y estructura anidada `tenants`/`users`/`apps`/`signingKeys` | 🔲 Pendiente |
| T-069 | Extender `ServiceInfoPropertiesTest` para cubrir `getEnvironment()` (sin perfil → `"default"`; con perfil → nombre del perfil) y `getStatus()` (siempre `"UP"`) | 🔲 Pendiente |
| ~~T-052~~ | ~~Hardening seguridad admin Bearer-only (sin `X-KEYGO-ADMIN`, `@PreAuthorize` + tenant match)~~ | ✅ Completada 2026-03-25 |
| ~~T-056~~ | ~~**Lanzamiento P0 — Hosted login seguro en `keygo-ui`:** contrato tipado `HostedLoginParams`, guard de runtime para query params obligatorios, ejemplo completo de login-handoff con parámetros firmados/validados y componente reutilizable `HostedLoginBoundary`~~ | ✅ Completada 2026-03-26 |

---

## Mediano plazo

> **Prioridad para lanzamiento de KeyGo:** dejar documentado el camino de endurecimiento multi-dominio y una opción BFF para equipos que no quieran exponer tokens en SPA pura.

| ID | Propuesta | Estado |
|---|---|---|
| T-009 | Poblar `keygo-domain` con entidades puras: `Tenant`, `User`, `ClientApp`, `Membership` | 🟡 Parcial (Tenant, User, ClientApp ✅; Membership pendiente) |
| T-010 | Poblar `keygo-infra` con puertos: `PasswordHasherPort`, `TokenSignerPort`, `ClockProvider` | 🟡 Parcial (`PasswordHasherPort`, `TokenSignerPort`, `ClockProvider` ✅) |
| T-013 | Tests de integración con Testcontainers para `keygo-supabase` | 🔲 Pendiente |
| T-025 | Tests de integración con Testcontainers para flujo completo de Tenant | 🔲 Pendiente |
| T-031 | Automatizar verificación de links Markdown rotos en CI (p. ej. `markdown-link-check` o `lychee`) | 🔲 Pendiente |
| T-036 | TTL configurable para refresh tokens y sesiones vía `application.yml` (actualmente fijo a 30 días en `AuthorizationController`) | 🔲 Pendiente |
| T-037 | Endpoint `GET /api/v1/tenants/{slug}/sessions` + `DELETE /…/{sessionId}` para que el administrador pueda listar y terminar sesiones activas de usuarios | 🔲 Pendiente |
| T-044 | Crear tabla `membership_attributes` (V14) + `MembershipAttributeEntity` + port + use cases para leer/escribir metadata app-específica del usuario | 🔲 Pendiente |
| T-045 | Implementar claim mappers por `ClientApp`: configurar qué claims incluir en `id_token` y `access_token` desde `membership_attributes` | 🔲 Pendiente |
| T-046 | Agregar scope `profile:write` explícito y validarlo en PATCH `/account/profile` contra los scopes del access token | 🔲 Pendiente |
| T-050 | Reemplazar validación en `CreateAppRoleUseCase` basada en `findAllByTenantId(...).stream().anyMatch(...)` por lookup directo app+tenant (p. ej. `findByIdAndTenantId`) | 🔲 Pendiente |
| T-054 | Separar seeds funcionales del schema con estrategia de `reference data` por ambiente (dev/demo) fuera de migraciones estructurales | 🔲 Pendiente |
| T-057 | **Lanzamiento P1 — Contrato formal de handoff multi-dominio:** definir y documentar el contrato entre app origen y `keygo-ui` central, incluyendo validación/firmado del contexto OAuth y ejemplos de `withCredentials`, CORS y cookies `SameSite=None; Secure` | 🔲 Pendiente |
| T-058 | **Lanzamiento P1 — Patrón BFF para login central:** documentar un ejemplo de canje de `authorization_code` en backend (BFF) para reducir exposición de tokens en SPA pura y simplificar refresh/logout | 🔲 Pendiente |
| T-063 | Incorporar `traceId/requestId` en `ErrorData` para trazabilidad entre logs y cliente | 🔲 Pendiente |
| T-066 | Agregar `endpointHint`/`actionHint` para errores `CLIENT_TECHNICAL` (ej. `enviar credentials include`) | 🔲 Pendiente |
| T-070 | `GET /api/v1/tenants/{slug}/stats` — estadísticas del tenant: usuarios (por estado), apps (total), memberships (total/activas), sesiones activas; para rol `ADMIN_TENANT` | 🔲 Pendiente |
| T-071 | Agregar filtros `created_after` y `created_before` al endpoint `GET /api/v1/tenants` y al use case `ListTenantsUseCase` / `TenantFilter` para análisis de crecimiento temporal | 🔲 Pendiente |

---

## Largo plazo

> **Prioridad post-lanzamiento:** converger a interoperabilidad OAuth estándar y evaluar sesión compartida real entre múltiples UIs.

| ID | Propuesta | Estado |
|---|---|---|
| T-017 | Renombrar `keygo-supabase` → `keygo-adapter-persistence-postgres` | 🔲 Pendiente |
| T-020 | Observabilidad avanzada: OpenTelemetry + Prometheus + Grafana | 🔲 Pendiente |
| T-032 | Evaluar generador de site estático (MkDocs / Docusaurus) que consolide `docs/` + archivos raíz en un portal navegable unificado con búsqueda | 🔲 Pendiente |
| T-038 | Lista negra de JTI de access tokens revocados con TTL en Redis para revocación inmediata sin esperar expiración natural | 🔲 Pendiente |
| ~~T-039~~ | ~~Soporte de `client_credentials` grant (Fase 8) — emite access token sin usuario para comunicación M2M~~ | ✅ Completada 2026-03-23 (Fase 8) |
| T-047 | Implementar SCIM 2.0 endpoint `/api/v1/tenants/{slug}/scim/v2/Users` para aprovisionamiento de perfiles desde sistemas HR externos | 🔲 Pendiente |
| T-048 | Soporte a esquemas de atributos personalizados por tenant — el admin define campos adicionales del perfil (análogo a Keycloak declarativeUserProfile) | 🔲 Pendiente |
| T-055 | Bootstrap programático de tenants/apps/roles vía control-plane admin (sin dependencia de seeds SQL en producción) | 🔲 Pendiente |
| T-059 | **Post-lanzamiento P2 — Redirect OAuth2 clásico:** evolucionar el backend para entregar `authorization_code` mediante redirect HTTP `302` hacia `redirect_uri` en lugar de retornarlo en JSON, reduciendo lógica de orquestación frontend y mejorando interoperabilidad con terceros | 🔲 Pendiente |
| T-060 | **Post-lanzamiento P3 — Gateway de federación / sesión compartida:** evaluar un patrón BFF/gateway para que el login central pueda administrar sesión entre múltiples UIs sin mezclarlo con el hosted login actual | 🔲 Pendiente |
| T-064 | Estandarizar catálogo i18n de errores por dominio (`auth`, `tenant`, `membership`) combinando `origin` + `clientRequestCause` para resolver `clientMessage` por locale | 🔲 Pendiente |
| T-072 | Dashboard de sesiones activas: `GET /api/v1/platform/sessions` con totales por estado; dependiente de T-037 (endpoints de gestión de sesiones) | 🔲 Pendiente |
| T-073 | Integrar Micrometer + Prometheus para exportar métricas en tiempo real: `keygo_tenants_total`, `keygo_users_total`, `keygo_sessions_active`, `keygo_tokens_issued_total`; complementa `/platform/stats` con push de métricas hacia Grafana | 🔲 Pendiente |
| F-041 | **Épica futura — SSO multi-app para ecosistema KeyGo:** diseñar sesión compartida explícita entre múltiples UIs/apps con contrato formal distinto al hosted login actual | 🔲 Pendiente |
| F-040 | RBAC granular para control-plane: autorización por permiso/acción en endpoints admin (más fino que rol global `ADMIN`) | 🔲 Pendiente |
| F-010–F-016 | Core OAuth2/OIDC: authorize, token, JWKS, Auth Code + PKCE | ✅ Fases 5 y 6 completadas |
| ~~F-025~~ | ~~`client_credentials` grant M2M sin usuario final~~ | ✅ Completada 2026-03-23 (Fase 8) |
| ~~F-027~~ | ~~Refresh tokens con rotación + SHA-256 hash~~ | ✅ Completada 2026-03-22 (Fase 7) |
| ~~F-028~~ | ~~Endpoint `/userinfo` OIDC §5.3~~ | ✅ Completada 2026-03-22 (Fase 7) |

---

## Referencias

- **Registro primario con IDs y detalle:** [`ROADMAP.md`](../../ROADMAP.md)
- **Historial de completadas:** ver tabla "Historial de propuestas completadas" en `ROADMAP.md`

---

**Última actualización:** 2026-03-28 (nuevas propuestas T-068/T-069 corto plazo; T-070/T-071 mediano plazo; T-072/T-073 largo plazo — generadas al implementar dashboard endpoints) | **Responsable:** AI Agent
