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
| T-026 | Mantener colecciones Postman actualizadas; crear environment `KeyGo-Server-Docker` | 🔲 Pendiente |
| ~~T-027~~ | ~~Integrar Swagger / OpenAPI con SpringDoc 3.0.1~~ | ✅ Completada 2026-03-21 |
| ~~T-027~~ | ~~Refresh token grant + revocación RFC 7009 + userinfo OIDC~~ | ✅ Completada 2026-03-22 (Fase 7) |
| T-028 | Migrar gestión de clave privada RSA a KMS externo (AWS KMS, Azure Key Vault, HashiCorp Vault) | 🔲 Pendiente |
| T-030 | Agregar verificación de referencias Markdown rotas post-reorganización `docs/ai/` | 🔲 Pendiente |
| ~~T-033~~ | ~~Endpoints `PUT /api/v1/tenants/{slug}/users/{userId}/suspend` y `/activate`~~ | ✅ Completada 2026-04-03 |
| ~~T-034~~ | ~~Tests de regresión en `BootstrapAdminKeyFilterTest` para los nuevos sufijos `/userinfo` y `/oauth2/revoke` como rutas públicas~~ | ✅ Completada 2026-04-03 |
| T-035 | Detección de replay attack: al recibir un refresh token en estado `USED`, revocar toda la cadena de sesión automáticamente | 🔲 Pendiente |
| ~~T-041~~ | ~~Agregar V13 y extender `TenantUserEntity` con 6 campos OIDC estándar~~ | ✅ Completada 2026-03-24 (Fase 9b) |
| ~~T-042~~ | ~~Implementar endpoints self-service de perfil: GET y PATCH `/account/profile` con Bearer token~~ | ✅ Completada 2026-03-24 (Fase 9b) |
| T-043 | Extender `GetUserInfoUseCase` para filtrar claims por scope solicitado (`profile`, `email`, `phone`) | 🔲 Pendiente |
| ~~T-049~~ | ~~Agregar request Postman `GET /api/v1/tenants/{slug}/apps/{clientId}/roles` con `pm.test()` de status 200, estructura `BaseResponse` y validación de lista~~ | ✅ Completada 2026-04-03 |
| T-051 | Suite de autorización por endpoint (`@PreAuthorize`) con matriz rol/tenant (ADMIN, ADMIN_TENANT match/mismatch, USER_TENANT) | 🔲 Pendiente |
| ~~T-052~~ | ~~Hardening seguridad admin Bearer-only (sin `X-KEYGO-ADMIN`, `@PreAuthorize` + tenant match)~~ | ✅ Completada 2026-03-25 |
| T-053 | Script SQL de verificación post-seed V14 (conteos por tenant/app/roles/memberships) para validación rápida local/CI | 🔲 Pendiente |
| ~~T-056~~ | ~~**Lanzamiento P0 — Hosted login seguro en `keygo-ui`:** contrato tipado `HostedLoginParams`, guard de runtime para query params obligatorios, ejemplo completo de login-handoff con parámetros firmados/validados y componente reutilizable `HostedLoginBoundary`~~ | ✅ Completada 2026-03-26 |
| T-061 | Externalizar lista de orígenes CORS por ambiente: documentar `KEYGO_CORS_ALLOWED_ORIGINS_0` en `.env.example` y `ENVIRONMENT_SETUP.md`; perfil `prod` con lista vacía (denegación por defecto) | 🔲 Pendiente |
| T-062 | Agregar handler específico para `MissingServletRequestParameterException` y responder `400 INVALID_INPUT` (evitar `500 OPERATION_FAILED` en casos de parámetro faltante) | 🔲 Pendiente |
| ~~T-065~~ | ~~Agregar `fieldErrors` (lista de campos inválidos) cuando `origin=CLIENT_REQUEST` y `clientRequestCause=USER_INPUT`~~ | ✅ Completada 2026-04-03 (tests mejorados) |
| ~~T-068~~ | ~~Agregar test unitario de `PlatformStatsController`: mockar `GetPlatformStatsUseCase`, verificar status 200, `PLATFORM_STATS_RETRIEVED` y estructura anidada `tenants`/`users`/`apps`/`signingKeys`~~ | ✅ Completada 2026-04-03 |
| ~~T-069~~ | ~~Extender `ServiceInfoPropertiesTest` para cubrir `getEnvironment()` (sin perfil → `"default"`; con perfil → nombre del perfil) y `getStatus()` (siempre `"UP"`)~~ | ✅ Completada 2026-04-03 |
| T-074 | Agregar caché `@Cacheable` en `GetPlatformDashboardUseCase` con TTL 60 s (Spring Cache + Caffeine) — el use case realiza ~25 queries JPA por llamada | 🔲 Pendiente |
| T-075 | `GET /api/v1/admin/tenants/{slug}/dashboard` — dashboard de métricas específicas del tenant para rol `ADMIN_TENANT` (usuarios/apps/memberships/sesiones/verificaciones acotados al slug) | 🔲 Pendiente |
| ~~T-080~~ | ~~`V21__seed_billing_keygo_plans.sql` — planes FREE/STARTER/BUSINESS/ENTERPRISE para keygo-platform con entitlements reales~~ | ✅ Completada 2026-03-28 |
| ~~T-081~~ | ~~Tests de controller billing (`AppBillingPlanControllerTest`, `AppBillingContractControllerTest`, `AppBillingSubscriptionControllerTest`) + `CreateAppPlanUseCaseTest`~~ | ✅ Completada 2026-03-28 |
| ~~T-082~~ | ~~Tests de regresión en `BootstrapAdminKeyFilterTest` para sufijos `/billing/catalog` y `/billing/contracts` como rutas públicas~~ | ✅ Completada 2026-04-03 |
| T-083 | Endpoint `GET /billing/invoices/{invoiceId}` — detalle de factura individual (requiere nueva ruta en `AppBillingSubscriptionController`) | 🔲 Pendiente |
| T-091 | Test de integración Testcontainers: validar coherencia JPA ↔ Flyway con `ddl-auto: validate` contra DB limpia con todas las migraciones aplicadas | 🔲 Pendiente |
| T-094 | Agregar test unitario para `AppPlanBillingOptionRepositoryAdapter`: `findByAppPlanVersionId`, `findByAppPlanVersionIdAndBillingPeriod` y `saveAll` con Mockito | 🔲 Pendiente |
| T-095 | Validar en `CreateAppPlanCommand` que si `billingOptions` no está vacía, al menos una opción tenga `isDefault=true`; lanzar `IllegalArgumentException` si ninguna es default | 🔲 Pendiente |
| T-096 | Añadir `@NotNull` y `@Valid` en `CreateAppPlanRequest.billingOptions`, `@NotNull` en `billingPeriod` y `basePrice` de `BillingOptionRequest`; agregar test de validación Bean Validation | 🔲 Pendiente |
| ~~T-103~~ | ~~Bloquear login cuando `status = RESET_PASSWORD` en `ValidateUserCredentialsUseCase` → `UserPasswordResetRequiredException`; `GlobalExceptionHandler` responde `403 RESET_PASSWORD_REQUIRED`; el frontend redirige al flujo de cambio de contraseña~~ | ✅ Completada 2026-04-02 |
| ~~T-106~~ | ~~Jerarquía de excepciones tipadas por capa + `ErrorData.layer`: `KeyGoException` → `DomainException` / `UseCaseException` / `PortException`; mensajes en la clase con constructores de valores. Ver [`docs/design/EXCEPTION_HIERARCHY.md`](../../docs/design/EXCEPTION_HIERARCHY.md)~~ | ✅ Completada 2026-04-01 |
| ~~T-110~~ | ~~**Estandarizar paginación, filtrado y ordenamiento con JPA Specifications (DB-side):** Fase 1: infraestructura compartida (`PageFilter` base class, `InvalidPaginationParamException` en shared), 4 filter objects por entidad (`UserFilter`, `ClientAppFilter`, `MembershipFilter`, `AppRoleFilter`), 5 endpoints actualizados (tenants + sort/order, users, apps, roles, memberships) con `PagedData<T>`. Fase 2: refactorizar `TenantFilter` para extender `PageFilter` + sorting dinámico. Fase 3 (✅ Completada): **Eliminar paginación en-memoria** — reemplazar 4 adapters (User, ClientApp, Membership, AppRole) con JPA Specifications + `JpaSpecificationExecutor`. Cada adapter ahora construye dinámicamente predicados SQL reales (WHERE), sorting (ORDER BY), y paginación (LIMIT/OFFSET). Corregir TenantRepositoryAdapter para sorting dinámico. Documentación en FRONTEND_DEVELOPER_GUIDE.md § 14, Postman, y `docs/ai/lecciones.md` (regla: NUNCA paginar en aplicación).~~ | ✅ Completada 2026-04-03 |

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
| ~~T-037~~ | ~~Endpoints self-service de sesiones: `GET /account/sessions` + `DELETE /account/sessions/{id}`, idempotente, `is_current` por UA+IP~~ | ✅ Completada 2026-04-02 |
| ~~F-030~~ | ~~RFC Account & Settings: 6 endpoints self-service (change-password, sessions, notification-preferences, access)~~ | ✅ Completada 2026-04-02 |
| T-108 | Enriquecer sesiones con geolocalización por IP: `GeoIpPort` + adapter (MaxMind/ip-api); campo `location` en `AccountSessionData`; feature flag `keygo.session.geoip.enabled=false` | 🔲 Pendiente |
| T-109 | Job `@Scheduled` de limpieza de sesiones expiradas/terminadas: TTL configurable, lock transaccional, métrica `keygo_sessions_cleaned_total` | 🔲 Pendiente |
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
| T-076 | Reemplazar `recentActivity` aproximada del dashboard (basada en `created_at`) por tabla de auditoría formal `audit_events` (`V16__add_audit_events.sql`) — habilita consultas eficientes y filtrables por tipo y tenant | 🔲 Pendiente |
| T-077 | `GET /api/v1/admin/alerts` — feed de alertas activas paginado con filtros por `level` y `category`; desacopla alertas del dashboard principal | 🔲 Pendiente |
| T-084 | Integración con gateway de pago real (MercadoPago / Stripe) que reemplaza el endpoint `mock-approve-payment`; adapter configurable por `keygo.billing.payment-provider` | 🔲 Pendiente |
| T-085 | Renovación automática de suscripciones via `@Scheduled` job: detectar suscripciones en `currentPeriodEnd < now()` + `autoRenew=true`, generar nueva factura, actualizar período | 🔲 Pendiente |
| T-086 | Soporte Bearer TENANT_USER en `GET /billing/subscription`: resolver `subscriberId` desde JWT claim `sub` en lugar de asumir siempre TENANT | 🔲 Pendiente |
| T-092 | Script CI que compare columnas `NOT NULL` de todas las entidades JPA con las definiciones de las migraciones correspondientes; detecta desincronías antes del runtime | 🔲 Pendiente |
| T-097 | `PUT /billing/plans/{planCode}/billing-options` — añadir/actualizar opciones de pago de la versión activa sin crear nueva versión; valida que no se duplique `billing_period` | 🔲 Pendiente |
| T-098 | Filtro `?subscriberType=TENANT\|TENANT_USER` en `GET /billing/catalog`: la tabla `app_plans` ya tiene la columna; filtrar por ella si se especifica, retornar todos si no | 🔲 Pendiente |
| T-099 | Caché `@Cacheable` + Caffeine TTL 5 min en `GetAppPlanCatalogUseCase` y `GetAppPlanUseCase`; invalidar al crear plan o actualizar billing options (T-097) | 🔲 Pendiente |
| ~~T-104~~ | ~~Endpoint `POST /account/reset-password` — recibe `email` + `temporaryPassword` + `newPassword`; `ResetPasswordUseCase` verifica hash temporal (BCrypt), actualiza hash con nueva contraseña y cambia `status → ACTIVE`; validación de complejidad mínima; depende de T-103~~ | ✅ Completada 2026-04-02 |
| ~~T-107~~ | ~~**Jerarquía de roles en apps de tenant:** tabla `app_role_hierarchy` (V20), restricciones de ciclo y profundidad ≤5, CTE recursiva para expansión en JWT, use cases `AssignRoleParentUseCase`/`RemoveRoleParentUseCase`, endpoints `POST/DELETE …/roles/{roleCode}/parent`~~ | ✅ Completada 2026-04-02 |

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
| T-078 | WebSocket o SSE `GET /api/v1/admin/platform/dashboard/stream` — push de snapshots del dashboard cada 30 s sin polling; requiere Spring WebFlux o `SseEmitter` + scheduler; dependiente de T-074 (caché) | 🔲 Pendiente |
| T-079 | `GET /api/v1/admin/platform/dashboard/histogram?days=30` — series de tiempo diarias de registros, sesiones y logins para gráficas de tendencia en el UI; primera aproximación con `GROUP BY DATE(created_at)`; mejora con T-076 (`audit_events`) | 🔲 Pendiente |
| T-087 | Generación de PDF de facturas: `InvoicePdfPort` + adapter con iText/JasperReports; PDF almacenado en S3/Supabase Storage, URL en campo `pdf_url` de `invoices` | 🔲 Pendiente |
| T-088 | Factura electrónica CFDI México: integración con PAC (Proveedor Autorizado de Certificación); emit XML CFDI 4.0 post-pago | 🔲 Pendiente |
| T-089 | Billing multi-currency: almacenar tipo de cambio en tabla `exchange_rates` (V22), convertir `base_price` al momento de crear contrato; campo `exchange_rate_snapshot` en `invoices` | 🔲 Pendiente |
| T-090 | Motor de dunning: tabla `dunning_events`, job que detecta facturas vencidas, reintenta cobro en D+1/D+3/D+7 con notificación email por evento; requiere gateway real (T-084) | 🔲 Pendiente |
| T-093 | Evaluar migración a Liquibase o jOOQ code generation para mantener schema y código en sincronía automática; elimina estructuralmente errores tipo `missing column` | 🔲 Pendiente |
| T-100 | Modelo de precios escalonado (tiers) por billing option: tabla `app_plan_billing_tiers`; cálculo de precio en `ActivateAppContractUseCase`; necesario para plan FLEX con precios por rangos de uso | 🔲 Pendiente |
| T-101 | Soporte de múltiples monedas por opción de billing: tabla `app_plan_billing_option_prices` con overrides por moneda (`USD`, `MXN`, `EUR`); resolver moneda del suscriptor desde el contrato | 🔲 Pendiente |
| T-102 | Precios dinámicos vía webhook externo: `DynamicPricingPort` + adapter configurable; precio base en `app_plan_billing_options` como fallback; integración con Stripe Price API | 🔲 Pendiente |
| T-105 | Política de expiración de contraseñas temporales (TTL 24 h): campo `temp_password_expires_at` en `tenant_users`; job `@Scheduled` que detecta usuarios `RESET_PASSWORD` con TTL vencido, genera nueva contraseña y la reenvía por email; config `keygo.security.temp-password-ttl-hours` | 🔲 Pendiente |
| T-115 | Incrementar cobertura JaCoCo en `keygo-supabase` desde 0.15 hasta 0.60: añadir tests unitarios para `UserRepositoryAdapter`, `EmailVerificationRepositoryAdapter`, `SessionRepositoryAdapter`, `MembershipRepositoryAdapter` y adapters de billing | 🔲 Pendiente |
| T-120 | **Diseño de catálogo i18n para respuestas API** — crear estructura i18n/messages_XX.properties (es, es-CL, en-US fallback, pt_BR, fr); ver [`docs/design/I18N_STRATEGY.md`](../../docs/design/I18N_STRATEGY.md) | 🟡 Parcial (archivos creados; faltan T-122, T-123) |
| T-121 | **LocaleResolver + LocaleContextFilter** — resolver locale desde `Accept-Language` header; propagar vía `LocaleContextHolder`; fallback en-US | ✅ Completada 2026-04-03 (20 tests, 100% coverage) |
| T-122 | **Refactorizar `ApiErrorDataFactory.clientMessage()`** — integrar `MessageSource`; cache `ReloadableResourceBundleMessageSource` con TTL 3600 s en prod | ✅ Completada 2026-04-03 |
| T-123 | **Tests de i18n** — unitarios de `LocaleResolver`, `LocaleContextFilter`, `ApiErrorDataFactory` con múltiples locales × ResponseCode | ✅ Completada 2026-04-03 |
| F-041 | **Épica futura — SSO multi-app para ecosistema KeyGo:** diseñar sesión compartida explícita entre múltiples UIs/apps con contrato formal distinto al hosted login actual | 🔲 Pendiente |
| F-040 | RBAC granular para control-plane: autorización por permiso/acción en endpoints admin (más fino que rol global `ADMIN`) | 🔲 Pendiente |
| ~~F-043~~ | ~~**Flujo forgot/recover-password:** `POST /account/forgot-password` (anti-enumeración) + `POST /account/recover-password` (token 32-hex, TTL 30 min, upsert), tabla `V22__password_recovery_tokens.sql`, `PasswordRecoveryToken` domain model, `ForgotPasswordUseCase`, `RecoverPasswordUseCase`, email HTML~~ | ✅ Completada 2026-04-02 |
| F-042 | **Account connections (RFC §5.5)** — `GET /account/connections`: lista de apps externas/integraciones vinculadas; modelo `UserConnection`; tabla `connections` (V22+) | 🔲 Pendiente |
| F-010–F-016 | Core OAuth2/OIDC: authorize, token, JWKS, Auth Code + PKCE | ✅ Fases 5 y 6 completadas |
| ~~F-025~~ | ~~`client_credentials` grant M2M sin usuario final~~ | ✅ Completada 2026-03-23 (Fase 8) |
| ~~F-027~~ | ~~Refresh tokens con rotación + SHA-256 hash~~ | ✅ Completada 2026-03-22 (Fase 7) |
| ~~F-028~~ | ~~Endpoint `/userinfo` OIDC §5.3~~ | ✅ Completada 2026-03-22 (Fase 7) |

---

## Referencias

- **Registro primario con IDs y detalle:** [`ROADMAP.md`](../../ROADMAP.md)
- **Historial de completadas:** ver tabla "Historial de propuestas completadas" en `ROADMAP.md`

---

**Última actualización:** 2026-04-03 (T-120 🟡 Parcial, T-121 ✅, T-122 ✅, T-123 ✅ — i18n completo: locale resolver + context filter + message source) | **Responsable:** AI Agent
