# Tasks — Planes de implementación

Cada archivo es una tarea independiente. Ciclo de vida definido en [workflow.md](../workflow.md).

> Nota: no toda iniciativa nace como tarea. Algunas entran directo por RFC y solo crean tareas
> derivadas cuando conviene para la implementacion o la trazabilidad.

## Convención de relaciones

Si una tarea referencia otra tarea, RFC o inconsistencia, debe incluir una sección
`## Relaciones` en su archivo y declarar el **tipo de relación** (`bloqueante`,
`habilitadora`, `complementaria`, `derivada de`, etc.). La definición canónica de tipos vive
en [workflow.md](../workflow.md#relaciones-entre-tareas).

| Estado | Emoji | Significado |
|---|---|---|
| Registrada | ⬜ | Sin análisis ni plan |
| En análisis | 🔍 | Leyendo código y documentación |
| Planificada | 📋 | Plan completo, esperando aprobación |
| En RFC | 📄 | RFC creado, pendiente de aprobación |
| Aprobada | 🟢 | Lista para implementar |
| En desarrollo | 🔵 | Implementación en curso |
| Bloqueada | 🚫 | Dependencia no resuelta |
| En revisión | 🔄 | Implementación completa, verificando |
| Pendiente integración UI | 🧩 | Backend listo, esperando integración o confirmación desde UI |
| Control de cambio | 🛂 | Ajuste solicitado sobre una entrega ya hecha, pendiente decisión de reabrir o derivar |
| Completada | ✅ | Cerrada y verificada |
| Archivada | ⬛ | Cancelada o descartada |

## Corto plazo

| Archivo | Resumen | Estado |
|---|---|---|
| [T-002-service-info-mapper.md](T-002-service-info-mapper.md) | Mapper `ServiceInfoProvider → ServiceInfoData` en `keygo-api/platform/` | ⬜ Registrada |
| [T-009-domain-entities.md](T-009-domain-entities.md) | Entidades puras en `keygo-domain` — pendiente `Membership` | 📋 Planificada |
| [T-010-infra-ports.md](T-010-infra-ports.md) | Puertos en `keygo-infra` — evaluar si hay pendientes | 📋 Planificada |
| [T-023-lint-format.md](T-023-lint-format.md) | Checkstyle / Spotless automático en el build | ⬜ Registrada |
| [T-026-postman-collections.md](T-026-postman-collections.md) | Colecciones Postman actualizadas + environment Docker | ⬜ Registrada |
| [T-028-kms-rsa-key.md](T-028-kms-rsa-key.md) | Migrar clave RSA a KMS externo (AWS/Azure/Vault) | ⬜ Registrada |
| [T-030-markdown-links.md](T-030-markdown-links.md) | Verificación de links Markdown rotos post-reorganización | ⬜ Registrada |
| [T-035-replay-attack-detection.md](T-035-replay-attack-detection.md) | Revocar cadena de sesión al detectar refresh token `USED` | ⬜ Registrada |
| [T-043-userinfo-scope-filter.md](T-043-userinfo-scope-filter.md) | Filtrar claims de `userinfo` por scope (`profile`, `email`, `phone`) | ⬜ Registrada |
| [T-051-authorization-matrix.md](T-051-authorization-matrix.md) | Suite de tests `@PreAuthorize` con matriz rol/tenant por endpoint | ⬜ Registrada |
| [T-053-seed-verification-script.md](T-053-seed-verification-script.md) | Script SQL de verificación post-seed V14 | ⬜ Registrada |
| [T-061-cors-origins-config.md](T-061-cors-origins-config.md) | Externalizar orígenes CORS por ambiente; `prod` vacío por defecto | ⬜ Registrada |
| [T-062-missing-param-handler.md](T-062-missing-param-handler.md) | Handler `MissingServletRequestParameterException` → `400 INVALID_INPUT` | ⬜ Registrada |
| [T-074-dashboard-cache.md](T-074-dashboard-cache.md) | `@Cacheable` TTL 60 s en `GetPlatformDashboardUseCase` | ⬜ Registrada |
| [T-075-tenant-dashboard.md](T-075-tenant-dashboard.md) | `GET /admin/tenants/{slug}/dashboard` para `ADMIN_TENANT` | ⬜ Registrada |
| [T-083-invoice-detail.md](T-083-invoice-detail.md) | `GET /billing/invoices/{invoiceId}` — detalle de factura | ⬜ Registrada |
| [T-091-testcontainers-flyway.md](T-091-testcontainers-flyway.md) | Testcontainers: coherencia JPA ↔ Flyway con `ddl-auto: validate` | ⬜ Registrada |
| [T-094-billing-option-repo-tests.md](T-094-billing-option-repo-tests.md) | Tests unitarios para `AppPlanBillingOptionRepositoryAdapter` | ⬜ Registrada |
| [T-095-billing-option-default-validation.md](T-095-billing-option-default-validation.md) | Validar `isDefault=true` en `CreateAppPlanCommand` | ⬜ Registrada |
| [T-096-billing-request-validation.md](T-096-billing-request-validation.md) | `@NotNull @Valid` en `CreateAppPlanRequest.billingOptions` | ⬜ Registrada |
| [T-120-i18n-catalog.md](T-120-i18n-catalog.md) | Catálogo i18n: archivos creados; completar cobertura de locales | 📋 Planificada |
| [T-126-platform-user-status-endpoints.md](T-126-platform-user-status-endpoints.md) | Endpoints admin `suspend`/`activate`/`require-reset-password` en `PlatformUser` | ⬜ Registrada |
| [T-128-username-collision.md](T-128-username-collision.md) | Resolver colisión de username generado en contratos | ⬜ Registrada |
| [T-129-domain-services-generation.md](T-129-domain-services-generation.md) | Consolidar 5 factories de generación en `keygo-domain` | ⬜ Registrada |
| [T-130-check-email.md](T-130-check-email.md) | `POST /platform/account/check-email` — valida email antes del ToS | 🧩 Pendiente integración UI |
| [T-131-pii-inventory.md](T-131-pii-inventory.md) | Inventario formal de PII por entidad y superficie de exposición | ⬜ Registrada |
| [T-132-doc-filenames-english.md](T-132-doc-filenames-english.md) | Migrar nombres de archivos y carpetas de `doc/` a inglés | ⬜ Registrada |
| [T-134-application-config-categories.md](T-134-application-config-categories.md) | Separar `ApplicationConfig` por dominios y estereotipos de configuración | ⬜ Registrada |
| [T-135-shell-scripts-review.md](T-135-shell-scripts-review.md) | Revisar scripts `*.sh` con fallas de ejecución y compatibilidad | ⬜ Registrada |
| [T-136-message-contract-redesign.md](T-136-message-contract-redesign.md) | Redefinir contrato de mensajes backend para que UI sepa qué mostrar, cuándo y cómo | ⬜ Registrada |
| [T-137-password-history-reuse-policy.md](T-137-password-history-reuse-policy.md) | Modelar restricción para no reutilizar las últimas N contraseñas | ⬜ Registrada |
| [T-138-controller-tests-response-not-null-guards.md](T-138-controller-tests-response-not-null-guards.md) | Endurecer tests de controller validando `isNotNull()` antes de dereferenciar `BaseResponse` y sus nodos | ⬜ Registrada |
| [T-139-eliminate-raw-object-signatures.md](T-139-eliminate-raw-object-signatures.md) | Corregir uso de `Object` / `Object[]` en parámetros, retornos y genéricos en favor de contratos tipados | ⬜ Registrada |
| [T-140-aggregate-status-queries.md](T-140-aggregate-status-queries.md) | Preferir queries agregadas por estado (`GROUP BY`) en vez de consultas repetidas por cada status | ⬜ Registrada |
| [T-141-platform-user-public-profile.md](T-141-platform-user-public-profile.md) | Endpoint para exponer perfil público de `platform_user` consumible por UI | ⬜ Registrada |
| [T-144-review-json-snake-case-contracts.md](T-144-review-json-snake-case-contracts.md) | Revisar request/response JSON para detectar y planificar corrección de contratos que no estén en `snake_case`. | ⬜ Registrada |
| [T-145-platform-billing-catalog-empty.md](T-145-platform-billing-catalog-empty.md) | Registro retroactivo de la corrección que alinea Flyway y seeds para que `GET /platform/billing/catalog` deje de responder `data: []`. | 🧩 Pendiente integración UI |

## Mediano plazo

| Archivo | Resumen | Estado |
|---|---|---|
| [T-013-testcontainers-supabase.md](T-013-testcontainers-supabase.md) | Testcontainers para adapters de `keygo-supabase` | ⬜ Registrada |
| [T-025-testcontainers-tenant-flow.md](T-025-testcontainers-tenant-flow.md) | Testcontainers: flujo completo de tenant | ⬜ Registrada |
| [T-031-ci-markdown-links.md](T-031-ci-markdown-links.md) | CI: verificación automática de links Markdown | ⬜ Registrada |
| [T-036-token-ttl-config.md](T-036-token-ttl-config.md) | TTL configurable para refresh tokens y sesiones | ⬜ Registrada |
| [T-044-membership-attributes.md](T-044-membership-attributes.md) | Tabla `membership_attributes` + use cases | ⬜ Registrada |
| [T-045-claim-mappers.md](T-045-claim-mappers.md) | Claim mappers por `ClientApp` desde `membership_attributes` | ⬜ Registrada |
| [T-046-profile-write-scope.md](T-046-profile-write-scope.md) | Scope `profile:write` en `PATCH /account/profile` | ⬜ Registrada |
| [T-050-role-lookup-refactor.md](T-050-role-lookup-refactor.md) | Lookup directo en `CreateAppRoleUseCase` | ⬜ Registrada |
| [T-054-seeds-separation.md](T-054-seeds-separation.md) | Separar seeds funcionales del schema Flyway | ⬜ Registrada |
| [T-057-multidomain-handoff-contract.md](T-057-multidomain-handoff-contract.md) | Contrato formal de handoff multi-dominio | ⬜ Registrada |
| [T-058-bff-login-pattern.md](T-058-bff-login-pattern.md) | Patrón BFF para login central | ⬜ Registrada |
| [T-063-trace-id-error-data.md](T-063-trace-id-error-data.md) | `traceId`/`requestId` en `ErrorData` | ⬜ Registrada |
| [T-066-endpoint-hint.md](T-066-endpoint-hint.md) | `endpointHint`/`actionHint` en errores `CLIENT_TECHNICAL` | ⬜ Registrada |
| [T-070-tenant-stats.md](T-070-tenant-stats.md) | `GET /tenants/{slug}/stats` para `ADMIN_TENANT` | ⬜ Registrada |
| [T-071-tenant-date-filters.md](T-071-tenant-date-filters.md) | Filtros `created_after`/`created_before` en `GET /tenants` | ⬜ Registrada |
| [T-072-sessions-dashboard.md](T-072-sessions-dashboard.md) | `GET /platform/sessions` — totales por estado | ⬜ Registrada |
| [T-076-audit-events-table.md](T-076-audit-events-table.md) | Tabla `audit_events` para reemplazar `recentActivity` aproximada | ⬜ Registrada |
| [T-077-alerts-feed.md](T-077-alerts-feed.md) | `GET /admin/alerts` — feed paginado con filtros | ⬜ Registrada |
| [T-084-payment-gateway.md](T-084-payment-gateway.md) | Integración gateway de pago real (MercadoPago / Stripe) | ⬜ Registrada |
| [T-085-subscription-auto-renewal.md](T-085-subscription-auto-renewal.md) | Job de renovación automática de suscripciones | ⬜ Registrada |
| [T-086-billing-bearer-tenant-user.md](T-086-billing-bearer-tenant-user.md) | Bearer `TENANT_USER` en `GET /billing/subscription` | ⬜ Registrada |
| [T-092-ci-not-null-check.md](T-092-ci-not-null-check.md) | Script CI: columnas `NOT NULL` JPA vs migraciones | ⬜ Registrada |
| [T-097-billing-options-update.md](T-097-billing-options-update.md) | `PUT /billing/plans/{planCode}/billing-options` | ⬜ Registrada |
| [T-098-catalog-subscriber-filter.md](T-098-catalog-subscriber-filter.md) | Filtro `subscriberType` en `GET /billing/catalog` | ⬜ Registrada |
| [T-099-plan-catalog-cache.md](T-099-plan-catalog-cache.md) | Caché Caffeine TTL 5 min en `GetAppPlanCatalogUseCase` | ⬜ Registrada |
| [T-105-temp-password-expiry.md](T-105-temp-password-expiry.md) | TTL 24 h para contraseñas temporales + job de reenvío | ⬜ Registrada |
| [T-108-geoip-sessions.md](T-108-geoip-sessions.md) | Geolocalización por IP en sesiones | ⬜ Registrada |
| [T-109-session-cleanup-job.md](T-109-session-cleanup-job.md) | Job de limpieza de sesiones expiradas | ⬜ Registrada |

## Largo plazo

| Archivo | Resumen | Estado |
|---|---|---|
| [T-017-rename-supabase-module.md](T-017-rename-supabase-module.md) | Renombrar `keygo-supabase` → `keygo-adapter-persistence-postgres` | ⬜ Registrada |
| [T-020-observability.md](T-020-observability.md) | OpenTelemetry + Prometheus + Grafana | ⬜ Registrada |
| [T-032-static-site-docs.md](T-032-static-site-docs.md) | Portal de documentación con MkDocs / Docusaurus | ⬜ Registrada |
| [T-038-jti-blacklist-redis.md](T-038-jti-blacklist-redis.md) | Lista negra de JTI con TTL en Redis | ⬜ Registrada |
| [T-047-scim-endpoint.md](T-047-scim-endpoint.md) | SCIM 2.0 endpoint para aprovisionamiento externo | ⬜ Registrada |
| [T-048-custom-attribute-schemas.md](T-048-custom-attribute-schemas.md) | Esquemas de atributos personalizados por tenant | ⬜ Registrada |
| [T-055-programmatic-bootstrap.md](T-055-programmatic-bootstrap.md) | Bootstrap programático de tenants/apps/roles | ⬜ Registrada |
| [T-059-oauth2-redirect.md](T-059-oauth2-redirect.md) | Redirect OAuth2 clásico con HTTP 302 | ⬜ Registrada |
| [T-060-federation-gateway.md](T-060-federation-gateway.md) | Gateway de federación / sesión compartida multi-UI | ⬜ Registrada |
| [T-064-i18n-error-catalog.md](T-064-i18n-error-catalog.md) | Catálogo i18n de errores por dominio | ⬜ Registrada |
| [T-073-micrometer-prometheus.md](T-073-micrometer-prometheus.md) | Micrometer + Prometheus — métricas en tiempo real | ⬜ Registrada |
| [T-078-dashboard-sse.md](T-078-dashboard-sse.md) | SSE push de snapshots del dashboard cada 30 s | ⬜ Registrada |
| [T-079-dashboard-histogram.md](T-079-dashboard-histogram.md) | Histograma temporal de registros/sesiones/logins | ⬜ Registrada |
| [T-087-invoice-pdf.md](T-087-invoice-pdf.md) | Generación de PDF de facturas con iText/JasperReports | ⬜ Registrada |
| [T-088-cfdi-factura-electronica.md](T-088-cfdi-factura-electronica.md) | Factura electrónica CFDI 4.0 México | ⬜ Registrada |
| [T-089-billing-multicurrency.md](T-089-billing-multicurrency.md) | Billing multi-currency con snapshot de tipo de cambio | ⬜ Registrada |
| [T-090-dunning-engine.md](T-090-dunning-engine.md) | Motor de dunning D+1/D+3/D+7 | ⬜ Registrada |
| [T-093-liquibase-joox-eval.md](T-093-liquibase-joox-eval.md) | Evaluar migración a Liquibase o jOOQ | ⬜ Registrada |
| [T-100-tiered-pricing.md](T-100-tiered-pricing.md) | Modelo de precios escalonado (tiers) | ⬜ Registrada |
| [T-101-billing-multicurrency-options.md](T-101-billing-multicurrency-options.md) | Overrides de precio por moneda en billing options | ⬜ Registrada |
| [T-102-dynamic-pricing-webhook.md](T-102-dynamic-pricing-webhook.md) | Precios dinámicos vía webhook externo (Stripe Price API) | ⬜ Registrada |
| [T-115-supabase-test-coverage.md](T-115-supabase-test-coverage.md) | Cobertura JaCoCo `keygo-supabase` de 0.15 → 0.60 | ⬜ Registrada |
| [T-127-status-audit-events.md](T-127-status-audit-events.md) | Event sourcing para auditoría de cambios de status | ⬜ Registrada |
| [F-040-rbac-granular.md](F-040-rbac-granular.md) | RBAC granular por permiso/acción en control-plane | ⬜ Registrada |
| [F-041-sso-multi-app.md](F-041-sso-multi-app.md) | SSO multi-app para ecosistema KeyGo | ⬜ Registrada |
| [F-042-account-connections.md](F-042-account-connections.md) | `GET /account/connections` — apps externas vinculadas | ⬜ Registrada |

---

## Historial — Completadas

Las tareas completadas se mueven aquí. No se eliminan.

| Archivo / ID | Resumen | Fecha |
|---|---|---|
| [T-142-platform-users-list-endpoint.md](T-142-platform-users-list-endpoint.md) | Registro retroactivo de la corrección que habilitó `GET /platform/users` para la UI, con paginación y artefactos sincronizados. | 2026-04-13 |
| [T-143-platform-user-roles-read-endpoint.md](T-143-platform-user-roles-read-endpoint.md) | Endpoint `GET /platform/users/{userId}/platform-roles` completado con metadata de asignación, scope y contractor resumido para la UI. | 2026-04-13 |
