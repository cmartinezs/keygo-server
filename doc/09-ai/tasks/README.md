# Tasks — Planes de implementación

Cada archivo es una tarea independiente. Ciclo de vida definido en [workflow.md](../workflow.md).

> Nota: no toda iniciativa nace como tarea. Algunas entran directo por RFC y solo crean tareas
> derivadas cuando conviene para la implementacion o la trazabilidad.

## Plantilla y reglas de nombrado

Ver [TEMPLATE.md](TEMPLATE.md) para la plantilla completa, reglas de correlativo (`T-NNN` /
`F-NNN`) y convenciones de nombre de archivo.

El próximo correlativo disponible es **T-163** (y **F-043** para features de largo plazo).

## Estructura de carpetas por estado

Cada tarea vive en la carpeta que corresponde a su estado actual. Al cambiar de estado,
el archivo se mueve a la carpeta del nuevo estado.

| Carpeta | Estado | Emoji |
|---|---|---|
| `registered/` | Registrada | ⬜ |
| `in-analysis/` | En análisis | 🔍 |
| `planned/` | Planificada | 📋 |
| `in-rfc/` | En RFC | 📄 |
| `approved/` | Aprobada | 🟢 |
| `in-development/` | En desarrollo | 🔵 |
| `blocked/` | Bloqueada | 🚫 |
| `in-review/` | En revisión | 🔄 |
| `pending-ui/` | Pendiente integración UI | 🧩 |
| `change-control/` | Control de cambio | 🛂 |
| `completed/` | Completada | ✅ |
| `archived/` | Archivada | ⬛ |

## Convención de relaciones

Si una tarea referencia otra tarea, RFC o inconsistencia, debe incluir una sección
`## Relaciones` en su archivo y declarar el **tipo de relación** (`bloqueante`,
`habilitadora`, `complementaria`, `derivada de`, etc.). La definición canónica de tipos vive
en [workflow.md](../workflow.md#relaciones-entre-tareas).

---

## Registrada ⬜

| Archivo | Resumen |
|---|---|
| [T-002](registered/T-002-service-info-mapper.md) | Mapper `ServiceInfoProvider → ServiceInfoData` en `keygo-api/platform/` |
| [T-023](registered/T-023-lint-format.md) | Checkstyle / Spotless automático en el build |
| [T-026](registered/T-026-postman-collections.md) | Colecciones Postman actualizadas + environment Docker |
| [T-028](registered/T-028-kms-rsa-key.md) | Migrar clave RSA a KMS externo (AWS/Azure/Vault) |
| [T-030](registered/T-030-markdown-links.md) | Verificación de links Markdown rotos post-reorganización |
| [T-035](registered/T-035-replay-attack-detection.md) | Revocar cadena de sesión al detectar refresh token `USED` |
| [T-043](registered/T-043-userinfo-scope-filter.md) | Filtrar claims de `userinfo` por scope (`profile`, `email`, `phone`) |
| [T-051](registered/T-051-authorization-matrix.md) | Suite de tests `@PreAuthorize` con matriz rol/tenant por endpoint |
| [T-053](registered/T-053-seed-verification-script.md) | Script SQL de verificación post-seed V14 |
| [T-061](registered/T-061-cors-origins-config.md) | Externalizar orígenes CORS por ambiente; `prod` vacío por defecto |
| [T-062](registered/T-062-missing-param-handler.md) | Handler `MissingServletRequestParameterException` → `400 INVALID_INPUT` |
| [T-074](registered/T-074-dashboard-cache.md) | `@Cacheable` TTL 60 s en `GetPlatformDashboardUseCase` |
| [T-075](registered/T-075-tenant-dashboard.md) | `GET /admin/tenants/{slug}/dashboard` para `ADMIN_TENANT` |
| [T-083](registered/T-083-invoice-detail.md) | `GET /billing/invoices/{invoiceId}` — detalle de factura |
| [T-091](registered/T-091-testcontainers-flyway.md) | Testcontainers: coherencia JPA ↔ Flyway con `ddl-auto: validate` |
| [T-094](registered/T-094-billing-option-repo-tests.md) | Tests unitarios para `AppPlanBillingOptionRepositoryAdapter` |
| [T-095](registered/T-095-billing-option-default-validation.md) | Validar `isDefault=true` en `CreateAppPlanCommand` |
| [T-096](registered/T-096-billing-request-validation.md) | `@NotNull @Valid` en `CreateAppPlanRequest.billingOptions` |
| [T-126](registered/T-126-platform-user-status-endpoints.md) | Endpoints admin `suspend`/`activate`/`require-reset-password` en `PlatformUser` |
| [T-129](registered/T-129-domain-services-generation.md) | Consolidar 5 factories de generación en `keygo-domain` |
| [T-131](registered/T-131-pii-inventory.md) | Inventario formal de PII por entidad y superficie de exposición |
| [T-132](registered/T-132-doc-filenames-english.md) | Migrar nombres de archivos y carpetas de `doc/` a inglés |
| [T-133](registered/T-133-doc-bilingual-content.md) | Revisar contenido bilingüe en documentación |
| [T-134](registered/T-134-application-config-categories.md) | Separar `ApplicationConfig` por dominios y estereotipos de configuración |
| [T-135](registered/T-135-shell-scripts-review.md) | Revisar scripts `*.sh` con fallas de ejecución y compatibilidad |
| [T-136](registered/T-136-message-contract-redesign.md) | Redefinir contrato de mensajes backend para que UI sepa qué mostrar, cuándo y cómo |
| [T-137](registered/T-137-password-history-reuse-policy.md) | Modelar restricción para no reutilizar las últimas N contraseñas |
| [T-138](registered/T-138-controller-tests-response-not-null-guards.md) | Endurecer tests de controller validando `isNotNull()` antes de dereferenciar `BaseResponse` |
| [T-139](registered/T-139-eliminate-raw-object-signatures.md) | Corregir uso de `Object` / `Object[]` en favor de contratos tipados |
| [T-140](registered/T-140-aggregate-status-queries.md) | Preferir queries agregadas por estado (`GROUP BY`) en vez de consultas repetidas |
| [T-141](registered/T-141-platform-user-public-profile.md) | Endpoint para exponer perfil público de `platform_user` consumible por UI |
| [T-144](registered/T-144-review-json-snake-case-contracts.md) | Revisar request/response JSON para detectar contratos que no estén en `snake_case` |
| [T-148](registered/T-148-jpa-entity-ddl-audit.md) | Auditoría sistemática de entidades JPA vs DDL Flyway: `@JoinColumn`/`@Column` names, `nullable`, `optional` |
| [T-149](registered/T-149-docker-native-image.md) | Imagen Docker optimizada con compilación nativa GraalVM (multi-stage, distroless, arranque < 500 ms) |
| [T-151](registered/T-151-deployment-cicd-strategy.md) | Estrategia de despliegue (develop/test/prod), decisión cloud/PaaS y pipeline CI/CD completo |
| [T-152](registered/T-152-prod-safe-application-config.md) | Externalizar config unsafe (`mock-payment`, `method-logging`, actuator) y crear perfiles `develop`/`test`/`prod` |
| [T-156](registered/T-156-app-layer-orchestration-pattern.md) | Documentar patrón Orchestration Use Case introducido en T-154 como guía para flujos futuros |
| [T-157](registered/T-157-controller-primary-identifier-audit.md) | Auditar y corregir uso de identificadores primarios en controllers — cumplir regla: no pasar referencias indirectas a use cases |
| [T-155](registered/T-155-tenant-app-invitation-flow.md) | Flujo de invitación admin a app de tenant — nuevos endpoints, migración V25 y feedback out BE-009 para UI |
| [T-159](registered/T-159-request-attack-surface-hardening.md) | Hardening de superficie de ataque HTTP: rate limit, brute force, enumeración, payload limits, headers de seguridad |
| [T-160](registered/T-160-springboot-startup-optimization.md) | Revisión y optimización de arranque de Spring Boot: lazy init, HikariCP, Flyway, JVM flags de desarrollo |
| [T-200](registered/T-200-jacoco-90-coverage.md) | Mejorar coverage JaCoCo de 60% a 90% — corregir tests fallidos y expandir coverage |
| [T-161](registered/T-161-jacoco-java21-compatibility.md) | Actualizar `jacoco-maven-plugin` a ≥ 0.8.11 para eliminar warning `Unsupported class file major version 69` con Java 21 |
| [T-013](registered/T-013-testcontainers-supabase.md) | Testcontainers para adapters de `keygo-supabase` |
| [T-025](registered/T-025-testcontainers-tenant-flow.md) | Testcontainers: flujo completo de tenant |
| [T-031](registered/T-031-ci-markdown-links.md) | CI: verificación automática de links Markdown |
| [T-036](registered/T-036-token-ttl-config.md) | TTL configurable para refresh tokens y sesiones |
| [T-044](registered/T-044-membership-attributes.md) | Tabla `membership_attributes` + use cases |
| [T-045](registered/T-045-claim-mappers.md) | Claim mappers por `ClientApp` desde `membership_attributes` |
| [T-046](registered/T-046-profile-write-scope.md) | Scope `profile:write` en `PATCH /account/profile` |
| [T-050](registered/T-050-role-lookup-refactor.md) | Lookup directo en `CreateAppRoleUseCase` |
| [T-054](registered/T-054-seeds-separation.md) | Separar seeds funcionales del schema Flyway |
| [T-057](registered/T-057-multidomain-handoff-contract.md) | Contrato formal de handoff multi-dominio |
| [T-058](registered/T-058-bff-login-pattern.md) | Patrón BFF para login central |
| [T-063](registered/T-063-trace-id-error-data.md) | `traceId`/`requestId` en `ErrorData` |
| [T-066](registered/T-066-endpoint-hint.md) | `endpointHint`/`actionHint` en errores `CLIENT_TECHNICAL` |
| [T-070](registered/T-070-tenant-stats.md) | `GET /tenants/{slug}/stats` para `ADMIN_TENANT` |
| [T-071](registered/T-071-tenant-date-filters.md) | Filtros `created_after`/`created_before` en `GET /tenants` |
| [T-072](registered/T-072-sessions-dashboard.md) | `GET /platform/sessions` — totales por estado |
| [T-076](registered/T-076-audit-events-table.md) | Tabla `audit_events` para reemplazar `recentActivity` aproximada |
| [T-077](registered/T-077-alerts-feed.md) | `GET /admin/alerts` — feed paginado con filtros |
| [T-084](registered/T-084-payment-gateway.md) | Integración gateway de pago real (MercadoPago / Stripe) |
| [T-085](registered/T-085-subscription-auto-renewal.md) | Job de renovación automática de suscripciones |
| [T-086](registered/T-086-billing-bearer-tenant-user.md) | Bearer `TENANT_USER` en `GET /billing/subscription` |
| [T-092](registered/T-092-ci-not-null-check.md) | Script CI: columnas `NOT NULL` JPA vs migraciones |
| [T-097](registered/T-097-billing-options-update.md) | `PUT /billing/plans/{planCode}/billing-options` |
| [T-098](registered/T-098-catalog-subscriber-filter.md) | Filtro `subscriberType` en `GET /billing/catalog` |
| [T-099](registered/T-099-plan-catalog-cache.md) | Caché Caffeine TTL 5 min en `GetAppPlanCatalogUseCase` |
| [T-105](registered/T-105-temp-password-expiry.md) | TTL 24 h para contraseñas temporales + job de reenvío |
| [T-108](registered/T-108-geoip-sessions.md) | Geolocalización por IP en sesiones |
| [T-109](registered/T-109-session-cleanup-job.md) | Job de limpieza de sesiones expiradas |
| [T-017](registered/T-017-rename-supabase-module.md) | Renombrar `keygo-supabase` → `keygo-adapter-persistence-postgres` |
| [T-020](registered/T-020-observability.md) | OpenTelemetry + Prometheus + Grafana |
| [T-032](registered/T-032-static-site-docs.md) | Portal de documentación con MkDocs / Docusaurus |
| [T-038](registered/T-038-jti-blacklist-redis.md) | Lista negra de JTI con TTL en Redis |
| [T-047](registered/T-047-scim-endpoint.md) | SCIM 2.0 endpoint para aprovisionamiento externo |
| [T-048](registered/T-048-custom-attribute-schemas.md) | Esquemas de atributos personalizados por tenant |
| [T-055](registered/T-055-programmatic-bootstrap.md) | Bootstrap programático de tenants/apps/roles |
| [T-059](registered/T-059-oauth2-redirect.md) | Redirect OAuth2 clásico con HTTP 302 |
| [T-060](registered/T-060-federation-gateway.md) | Gateway de federación / sesión compartida multi-UI |
| [T-064](registered/T-064-i18n-error-catalog.md) | Catálogo i18n de errores por dominio |
| [T-073](registered/T-073-micrometer-prometheus.md) | Micrometer + Prometheus — métricas en tiempo real |
| [T-078](registered/T-078-dashboard-sse.md) | SSE push de snapshots del dashboard cada 30 s |
| [T-079](registered/T-079-dashboard-histogram.md) | Histograma temporal de registros/sesiones/logins |
| [T-087](registered/T-087-invoice-pdf.md) | Generación de PDF de facturas con iText/JasperReports |
| [T-088](registered/T-088-cfdi-factura-electronica.md) | Factura electrónica CFDI 4.0 México |
| [T-089](registered/T-089-billing-multicurrency.md) | Billing multi-currency con snapshot de tipo de cambio |
| [T-090](registered/T-090-dunning-engine.md) | Motor de dunning D+1/D+3/D+7 |
| [T-093](registered/T-093-liquibase-joox-eval.md) | Evaluar migración a Liquibase o jOOQ |
| [T-100](registered/T-100-tiered-pricing.md) | Modelo de precios escalonado (tiers) |
| [T-101](registered/T-101-billing-multicurrency-options.md) | Overrides de precio por moneda en billing options |
| [T-102](registered/T-102-dynamic-pricing-webhook.md) | Precios dinámicos vía webhook externo (Stripe Price API) |
| [T-115](registered/T-115-supabase-test-coverage.md) | Cobertura JaCoCo `keygo-supabase` de 0.15 → 0.60 |
| [T-127](registered/T-127-status-audit-events.md) | Event sourcing para auditoría de cambios de status |
| [F-040](registered/F-040-rbac-granular.md) | RBAC granular por permiso/acción en control-plane |
| [F-041](registered/F-041-sso-multi-app.md) | SSO multi-app para ecosistema KeyGo |
| [F-042](registered/F-042-account-connections.md) | `GET /account/connections` — apps externas vinculadas |

## Planificada 📋

| Archivo | Resumen |
|---|---|
| [T-009](planned/T-009-domain-entities.md) | Entidades puras en `keygo-domain` — pendiente `Membership` |
| [T-010](planned/T-010-infra-ports.md) | Puertos en `keygo-infra` — evaluar si hay pendientes |
| [T-120](planned/T-120-i18n-catalog.md) | Catálogo i18n: archivos creados; completar cobertura de locales |
| [T-150](planned/T-150-platform-user-username-removal.md) | Eliminar `username` de `PlatformUser`; login siempre por email, renombrar `emailOrUsername` → `email` |

## Pendiente integración UI 🧩

| Archivo | Resumen |
|---|---|
| [T-130](pending-ui/T-130-check-email.md) | `POST /platform/account/check-email` — valida email antes del ToS |
| [T-145](pending-ui/T-145-platform-billing-catalog-empty.md) | Corrección que alinea Flyway y seeds para `GET /platform/billing/catalog` |
| [T-154](pending-ui/T-154-tenant-app-open-registration-flow.md) | Flujo de self-registro abierto: backend completado; bloqueado en UI por T-158 (endpoints públicos de descubrimiento) |
| [T-158](pending-ui/T-158-public-discovery-endpoints.md) | `GET /tenants/public` y `GET /tenants/{slug}/apps/public` — backend completo, 11 tests; pendiente consumo por UI |

## Archivada ⬛

| Archivo | Motivo |
|---|---|
| [T-128](archived/T-128-username-collision.md) | Absorbida por T-150 — la eliminación de `username` resuelve la causa raíz |

## Completada ✅

| Archivo | Resumen | Fecha |
|---|---|---|
| [T-142](completed/T-142-platform-users-list-endpoint.md) | Registro retroactivo de la corrección que habilitó `GET /platform/users` para la UI, con paginación y artefactos sincronizados. | 2026-04-13 |
| [T-143](completed/T-143-platform-user-roles-read-endpoint.md) | Endpoint `GET /platform/users/{userId}/platform-roles` completado con metadata de asignación, scope y contractor resumido para la UI. | 2026-04-13 |
| [T-146](completed/T-146-platform-roles-catalog-endpoint.md) | `GET /platform/roles` — catálogo de roles de plataforma disponibles para asignación desde UI. | 2026-04-13 |
| [T-147](completed/T-147-platform-plan-entitlements-update.md) | Migración V21 con matriz completa de entitlements de planes de plataforma (73 filas): corrección de `period_type` en V20 + 61 nuevas métricas incluyendo FLEX tiers y ENTERPRISE exclusivos. | 2026-04-14 |
| [T-153](completed/T-153-platform-account-profile.md) | `GET` / `PATCH /api/v1/platform/account/profile` — endpoints de perfil self-service para platform users, operando directamente sobre PlatformUserRepositoryPort. | 2026-04-14 |
| [T-162](completed/T-162-app-role-default-self-registration.md) | Rol por defecto de app en self-registration: diseño documentado, decisión registrada (`is_default` en `app_roles` + asignación automática en `SelfRegistrationOrchestrator`). | 2026-04-18 |
