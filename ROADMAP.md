# KeyGo Server — Roadmap de Mejoras

> **Documento vivo.** Los agentes AI deben actualizar este archivo cuando generen nuevas propuestas
> concretas al concluir tareas. Ver instrucciones de mantenimiento al final.

## Estado actual del producto (2026-03-28)

| Dimensión | Estado |
|---|---|
| Arquitectura | Hexagonal definida, módulos activos: `keygo-app`, `keygo-api`, `keygo-infra`, `keygo-run`, `keygo-supabase` |
| Autenticación | **Fase 9b completada**: JWT RS256, JWKS, OIDC discovery, refresh token con rotación, revocación RFC 7009, userinfo OIDC §5.3, client_credentials M2M, registro con verificación email, **perfil de usuario OIDC extendido (V13)** |
| Persistencia | Entidades JPA: User, Role, Permission, Tenant, ClientApp, Membership & AppRole, AuthorizationCode, SigningKey, Session, RefreshToken, EmailVerification. V13: 6 campos OIDC extendidos en `tenant_users` |
| API pública | **27 endpoints**: `GET /service/info`, `GET /response-codes`, Tenants (**4**: POST crear, **GET listar paginado**, GET ver, PUT suspender), Client Apps (5), Users & memberships (6), OAuth2 (5), OIDC (2), UserInfo (1), Revocation (1), Registro (3), Account Profile (2: GET+PATCH) |
| CI/CD | ✅ Pipeline activo en `.github/workflows/ci.yml` (test + package en push/PR a main/develop) |
| Tests | **527+ tests unitarios** — sin integración ni e2e |
| Postman | ✅ Colecciones en `docs/postman/` — **42 requests** con scripts `pm.test()` y entorno local |
| Fase actual | **Fase 9b ✅ Completada** + `GET /api/v1/tenants` (listado paginado) — próxima: Fase 10 (Control plane y soporte) |

---

## Propuestas técnicas

### Corto plazo

> Relacionadas con la base de código actual; bajo esfuerzo; bloquean calidad inmediata.
>
> **Enfoque de lanzamiento KeyGo (2026-03-26):** priorizar endurecimiento del hosted login y del handoff entre app origen y `keygo-ui` central antes de abordar SSO real o cambios profundos de protocolo.

| ID    | Propuesta                                                                                                                                                                                                                                                               | Módulo                                     | Justificación                                                                                                                             |
|-------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| T-002 | Agregar mapper dedicado en `keygo-api/platform/` para transformar `ServiceInfoProvider` → `ServiceInfoData` y descargar al controller de la lógica de mapeo                                                                                                             | `keygo-api`                                | Principio de responsabilidad única en controllers                                                                                         |
| T-003 | Agregar `request/` bajo `keygo-api/platform/` para DTOs de entrada cuando aparezcan endpoints con body o query params                                                                                                                                                   | `keygo-api`                                | Anticipa crecimiento ordenado                                                                                                             |
| T-004 | Crear sub-paquetes `command/`, `query/` y `result/` en `keygo-app/platform/` al implementar el primer use case con entrada/salida propia                                                                                                                                | `keygo-app`                                | Patrón CQRS mínimo para separar intención                                                                                                 |
| T-005 | Restringir `management.endpoints.web.exposure.include` a `health,info` en el perfil `prod`                                                                                                                                                                              | `keygo-run`                                | Actuator actualmente expone todos los endpoints (`"*"`) — riesgo de seguridad                                                             |
| T-007 | Renombrar config de IntelliJ `.run/KeyGo Runner.run.xml` a `.run/KeygoApplication.run.xml` para reflejar el nombre actual de la clase principal                                                                                                                         | Infra dev                                  | Consistencia tras renombrado de `KeyGoRunner` → `KeygoApplication`                                                                        |
| T-023 | Configurar plugin de lint/formato automático (Checkstyle con Google Java Style o Spotless) en el `pom.xml` raíz; integrar como paso en CI                                                                                                                               | `pom.xml` raíz / CI                        | La convención de 2 espacios ya está documentada en `docs/keygo-server/CODE_STYLE.md`; falta enforcement automático                        |
| T-025 | Agregar tests de integración con Testcontainers para el flujo completo de Tenant: crear → consultar → suspender vía `TenantRepositoryAdapter`                                                                                                                           | `keygo-supabase`                           | El adaptador solo tiene tests unitarios con Mockito; la persistencia real no se valida aún                                                |
| T-033 | Agregar endpoints `PUT /api/v1/tenants/{slug}/users/{userId}/suspend` y `PUT /api/v1/tenants/{slug}/users/{userId}/activate` para gestión del estado de usuarios por tenant                                                                                             | `keygo-api`, `keygo-app`, `keygo-supabase` | La entidad `TenantUserEntity` ya soporta estados; faltan los endpoints REST y use cases correspondientes                                  |
| T-026 | Mantener colecciones Postman actualizadas: agregar nuevas requests al crear endpoints; crear un environment adicional `KeyGo-Server-Docker` para pruebas contra imagen Docker                                                                                           | `postman/`                                 | Las colecciones actuales cubren los 7 endpoints existentes; cada nueva feature debe extenderlas                                           |
| T-028 | Migrar gestión de clave privada RSA a KMS externo (AWS KMS, Azure Key Vault, HashiCorp Vault) — eliminar `private_material` de la DB en producción                                                                                                                      | `keygo-infra`, `keygo-supabase`            | La clave privada en DB es práctica aceptable solo en dev/staging; producción requiere HSM o KMS                                           |
| T-034 | Agregar tests de regresión en `BootstrapAdminKeyFilterTest` para los nuevos sufijos `/userinfo` y `/oauth2/revoke` como rutas públicas — verificar que no requieran `X-KEYGO-ADMIN` y que el filtro siga protegiendo el resto de `/api/`                                | `keygo-run`                                | La Fase 7 agregó dos nuevas propiedades (`userInfoPathSuffix`, `revocationPathSuffix`) al filtro sin ampliar los tests existentes         |
| T-061 | Externalizar lista de orígenes CORS (`keygo.cors.allowed-origins`) por ambiente: agregar `KEYGO_CORS_ALLOWED_ORIGINS_0` como variable de entorno documentada en `.env.example` y `ENVIRONMENT_SETUP.md`; agregar perfil `prod` con lista vacía (denegación por defecto) | `keygo-run`, `docs/`                       | El valor actual está fijo en `application.yml`; en producción debe configurarse explícitamente el dominio del frontend y rechazar `*`     |
| T-062 | Agregar handler específico para `MissingServletRequestParameterException` en `GlobalExceptionHandler` y responder `400 INVALID_INPUT` con `BaseResponse.data` consistente                                                                                               | `keygo-api`                                | Evita que parámetros faltantes caigan en el catch-all `500 OPERATION_FAILED` y mejora claridad contractual para frontend/tests standalone |
 T-065  Agregar `fieldErrors` (lista de campos invlidos) en `ErrorData` cuando `origin=CLIENT_REQUEST` y `clientRequestCause=USER_INPUT`  `keygo-api`  Mejora UX de formularios y reduce ambigedad al mostrar validaciones por campo en frontend 
 T-067  Eliminar `SigningKeyBootstrapService` — supersedido por `SigningKeyInitializer` (que cubre `supabase` + `local`); tener ambos activos en `supabase` es redundante y confuso  `keygo-run`  Limpieza técnica: una sola clase de bootstrap (`SigningKeyInitializer`) inicializa la clave de firma en todos los perfiles donde aplica; detectado al depurar error `No active signing key found` en perfil `local` 
| T-035 | Implementar detección de refresh token replay: si se intenta rotar un token en estado `USED`, revocar automáticamente todos los tokens activos de la misma sesión y terminar la sesión | `keygo-app`, `keygo-supabase` | Patrón de seguridad estándar (RFC 6749 §10.4) — detecta posible robo de token y cierra toda la sesión comprometida |
| T-029 | Agregar columna `status VARCHAR(20)` a la tabla `app_roles` con valores `ACTIVE\|DISABLED` y migración `V11__add_app_role_status.sql`; actualizar `AppRoleEntity` y use cases para filtrar roles deshabilitados | `keygo-supabase`, `keygo-app`, `keygo-api` | La documentación original preveía este campo; permite deshabilitar un rol sin eliminarlo, útil para gestión de permisos granular en apps |
| T-030 | Agregar verificación de referencias Markdown rotas tras la reorganización de `docs/ai/` — script o step en CI que detecte links rotos en los docs de la carpeta `docs/ai/` y en los archivos raíz que apuntan a ella | `docs/ai/`, CI | La reorganización eliminó 5 archivos de la raíz; links rotos a rutas antiguas no se detectarían sin un check explícito |
| T-049 | Agregar request Postman `GET /api/v1/tenants/{slug}/apps/{clientId}/roles` con `pm.test()` para status 200, envelope `BaseResponse` y validación de lista | `postman/` | Cierra cobertura funcional de roles en pruebas manuales; hoy solo está documentada la creación |
| T-051 | Agregar suite de autorización por endpoint (`@PreAuthorize`) con matriz rol/tenant (ADMIN, ADMIN_TENANT tenant-match, ADMIN_TENANT tenant-mismatch, USER_TENANT) usando MockMvc + JWT de prueba | `keygo-api`, `keygo-run` | Evita regresiones de seguridad tras migrar a Bearer-only y documenta comportamiento esperado (401/403/200) por endpoint |
| T-053 | Agregar script SQL de verificación post-seed para V14 (conteos esperados por tenant/app/roles/memberships) y validación rápida en local/CI | `keygo-supabase`, `scripts/` | Permite comprobar integridad del dataset semilla sin inspección manual y reduce errores al preparar entorno de UI |
| T-068 | Agregar test unitario de `PlatformStatsController` (similar a `ServiceInfoControllerTest`): mockar `GetPlatformStatsUseCase`, verificar status 200, `ResponseCode.PLATFORM_STATS_RETRIEVED` y estructura anidada `tenants`/`users`/`apps`/`signingKeys` | `keygo-api` | El controller se creó sin test unitario en la misma sesión de implementación; cierra la cobertura del nuevo endpoint de estadísticas |
| T-069 | Extender `ServiceInfoPropertiesTest` para cubrir `getEnvironment()` (devuelve `"default"` sin perfil activo, devuelve el nombre del perfil con perfil activo) y `getStatus()` (siempre `"UP"`) | `keygo-run` | Los nuevos métodos añadidos a `ServiceInfoProperties` no tienen tests explícitos; el test existente solo cubre `title`, `name` y `version` |
| T-074 | Agregar caché `@Cacheable` en `GetPlatformDashboardUseCase` con TTL 60 s usando Spring Cache + Caffeine — el use case actual realiza ~25 queries JPA por llamada; el dashboard es de solo lectura y puede tolerar datos con hasta 60 s de desfase | `keygo-app`, `keygo-run` | Reduce carga en DB y latencia percibida en el frontend; el dashboard es el endpoint más pesado de la plataforma |
| T-075 | Implementar `GET /api/v1/admin/tenants/{slug}/dashboard` — dashboard de métricas específicas de un tenant: usuarios (total/por estado), apps (total/por tipo), memberships (total/activas), sesiones activas del tenant, verificaciones pendientes y actividad reciente acotada al tenant | `keygo-api`, `keygo-app`, `keygo-supabase` | Permite al rol `ADMIN_TENANT` ver su propio dashboard sin acceso a datos globales; complementa el dashboard global de `ADMIN` |
| T-082 | Agregar tests de regresión en `BootstrapAdminKeyFilterTest` para los sufijos `/billing/catalog` y `/billing/contracts` como rutas públicas — verificar que no requieran Bearer token y que el resto de rutas de billing sí lo requieran | `keygo-run` | Los nuevos sufijos de billing se agregaron al filtro sin ampliar los tests existentes; cubre regresión de seguridad en rutas de billing |
| T-083 | Agregar endpoint `GET /billing/invoices/{invoiceId}` para detalle de factura individual — new route in `AppBillingSubscriptionController` + `GetInvoiceUseCase` + `InvoiceRepositoryPort.findById` ya existe | `keygo-api`, `keygo-app` | El catálogo de invoices solo lista; el detalle individual falta y es necesario para mostrar facturas al usuario |
| T-091 | Agregar test de integración con Testcontainers que valide la coherencia entre las entidades JPA y las migraciones Flyway usando `ddl-auto: validate` contra una DB limpia con todas las migraciones aplicadas | `keygo-supabase` | Previene en CI la clase de error `SchemaManagementException: missing column` detectada en V23 (`subscriber_type`); detecta desincronías entidad/schema antes del runtime sin necesidad de arrancar la app completa |

---

### Mediano plazo

> Evoluciones naturales de la arquitectura actual; esfuerzo moderado; habilitan features reales.
>
> **Prioridad de pre-escalamiento:** formalizar el contrato multi-dominio y ofrecer una variante BFF para equipos con mayores requisitos de seguridad frontend.

| ID | Propuesta | Módulo | Justificación |
|---|---|---|---|
| T-008 | Definir interfaz `BootstrapFilterProperties` en `keygo-api` que `KeyGoBootstrapProperties` implemente; mover `BootstrapAdminKeyFilter` a `keygo-api/security/filter/` | `keygo-api` / `keygo-run` | Elimina dependencia circular que hoy fuerza el filtro a vivir en `keygo-run` |
| T-009 | Poblar `keygo-domain` con las primeras entidades de dominio puras: `Tenant`, `User`, `ClientApp`, `Membership` (sin Spring, sin JPA) | `keygo-domain` | Actualmente es un stub vacío; bloquea el modelo de negocio real |
| T-010 | Poblar `keygo-infra` con puertos de infraestructura transversal: `PasswordHasherPort`, `TokenSignerPort`, `ClockProvider`, `AuditPublisherPort`; `keygo-supabase` se conecta a ellos | `keygo-infra` | Actualmente vacío; `keygo-supabase` sin mediación de puertos genera acoplamiento directo |
| T-011 | Agregar capa `persistence/` en `keygo-supabase` como intermediaria entre `config/` y las features (`user/`, `membership/`) para aislar aún más los detalles de JPA | `keygo-supabase` | Definido en `docs/arch/keygo_server_project_structure.md` sección 5.4 |
| T-012 | Crear `keygo-supabase/support/` para utilidades de persistencia transversales (type converters JPA, entity listeners, auditoría base) | `keygo-supabase` | Evita duplicar código de auditoría en cada entidad |
| T-013 | Implementar tests de integración con Testcontainers PostgreSQL para `keygo-supabase` | `keygo-supabase` | Actualmente sólo hay tests unitarios — la persistencia no se valida con DB real |
| T-014 | Configurar perfiles de entorno separados: `dev`, `test`, `prod`; centralizar configuraciones sensibles en `keygo-run` | `keygo-run` | Actualmente sólo `supabase` y `local`; sin separación clara de entornos |
| T-015 | Agregar comprobación de dependencias con `OWASP Dependency-Check` o similar en el pipeline CI | CI | Detectar CVEs en dependencias antes de merge |
| T-031 | Automatizar verificación de links Markdown rotos en CI usando `markdown-link-check` o `lychee`; configurar como step en `.github/workflows/ci.yml` que falle si hay referencias a archivos que ya no existen | CI / `docs/` | Complemento de T-030 — aplica a todo el repositorio, no solo a `docs/ai/`; previene regresiones de documentación en cualquier PR |
| T-036 | Hacer configurable el TTL de refresh tokens y sesiones vía `application.yml` (actualmente hardcodeado a `Duration.ofDays(30)` en `AuthorizationController`) — agregar `keygo.auth.refresh-token-ttl=30d` y `keygo.auth.session-ttl=30d` | `keygo-run`, `keygo-api` | Permite ajustar el TTL por entorno (dev más corto, prod ajustable) sin recompilar; sigue el patrón de `KeyGoBootstrapProperties` |
| T-037 | Agregar endpoints de gestión de sesiones: `GET /api/v1/tenants/{slug}/users/{userId}/sessions` (listar sesiones activas) y `DELETE /api/v1/tenants/{slug}/users/{userId}/sessions/{sessionId}` (terminar sesión) | `keygo-api`, `keygo-app`, `keygo-supabase` | Permite al administrador y al propio usuario ver y cerrar sesiones activas — funcionalidad estándar en IAM modernos |
| T-039 | Agregar tests de regresión en `BootstrapAdminKeyFilterTest` para los tres nuevos sufijos públicos `/register`, `/verify-email` y `/resend-verification` — verificar que no requieran `X-KEYGO-ADMIN` y que el filtro siga protegiendo el resto de `/api/` | `keygo-run` | La Fase 9 (registro + verificación) agregó tres nuevas propiedades al filtro sin ampliar los tests específicos de cada sufijo |
| T-040 | Hacer configurable el TTL del código de verificación de email vía `application.yml` (actualmente hardcodeado a 30 minutos en `RegisterTenantUserUseCase` y `ResendVerificationEmailUseCase`) — agregar `keygo.registration.verification-code-ttl=30m` | `keygo-run`, `keygo-app` | Permite ajustar el TTL por entorno sin recompilar; sigue el patrón de `KeyGoBootstrapProperties` |
| T-043 | Extender `GetUserInfoUseCase` para filtrar claims según el scope solicitado en el access token: scope `profile` → `given_name`, `family_name`, `picture`, `locale`, `zoneinfo`, `birthdate`, `website`; scope `phone` → `phone_number`. Actualmente retorna todos los campos sin filtrar | `keygo-app`, `keygo-api` | OIDC §5.3 mandates scope-based claim filtering; actualmente todos los campos se retornan siempre |
| T-044 | Crear tabla `membership_attributes` (V14) + `MembershipAttributeEntity` + port + use cases para leer/escribir metadata app-específica del usuario (análogo a Auth0 `app_metadata`) | `keygo-supabase`, `keygo-app`, `keygo-api` | Capa 2 del modelo de perfil: datos específicos por app-usuario, complementa el perfil canónico en `tenant_users` |
| T-045 | Implementar claim mappers por `ClientApp`: el admin configura qué campos de `membership_attributes` incluir como claims custom en `id_token` y `access_token` | `keygo-app`, `keygo-supabase` | Permite a cada app extender el token con atributos propios sin modificar el esquema global |
| T-046 | Agregar scope `profile:write` explícito y validarlo en `PATCH /account/profile` contra los scopes del access token | `keygo-app`, `keygo-api` | Granularidad de permisos; sigue OAuth2 scope-based authorization; actualmente cualquier token válido puede modificar el perfil |
| T-050 | Reemplazar la validación de pertenencia app→tenant en `CreateAppRoleUseCase` (hoy con `findAllByTenantId(...).stream().anyMatch(...)`) por lookup directo app+tenant (`findByIdAndTenantId`) en puertos/adapters | `keygo-app`, `keygo-supabase` | Reduce costo de consulta, evita escaneo en memoria y mejora legibilidad/consistencia de validaciones |
| T-054 | Separar seeds funcionales del schema con estrategia de `reference data` por ambiente (dev/demo) y carga controlada fuera de migraciones estructurales | `keygo-supabase`, `keygo-run` | Evita acoplar datos operativos a Flyway estructural y facilita datasets distintos por entorno |
| T-057 | Formalizar contrato de handoff multi-dominio entre app origen y `keygo-ui` central: definir payload/campos, estrategia de validación o firmado y ejemplos cross-site con `withCredentials`, CORS y cookies `SameSite=None; Secure` | `docs/keygo-ui/`, `docs/api/`, futuro `keygo-ui` | Evita manipulación de parámetros, documenta integraciones por dominio cruzado y estandariza la incorporación de nuevas UIs al login central |
| T-058 | Documentar patrón BFF para login central: ejemplo de canje de `authorization_code` en backend para evitar exposición de tokens en SPA pura y simplificar refresh/logout | `docs/keygo-ui/`, `docs/api/`, futuro BFF de `keygo-ui` | Da una ruta intermedia de endurecimiento para clientes con restricciones de seguridad sin obligar todavía a SSO completo |
| T-063 | Incorporar `traceId`/`requestId` en `ErrorData` y propagarlo desde request a logs y respuestas de error (`GlobalExceptionHandler` + `BootstrapAdminKeyFilter`) | `keygo-api`, `keygo-run` | Mejora trazabilidad entre cliente y logs operativos, reduciendo tiempo de diagnóstico en incidentes |
| T-066 | Agregar `endpointHint`/`actionHint` para errores `CLIENT_TECHNICAL` (ej. `withCredentials`, header faltante, query param requerido) | `keygo-api`, `keygo-run` | Acelera diagnóstico en UI y soporte al sugerir acción concreta para corregir integración técnica |
| T-070 | Implementar `GET /api/v1/tenants/{slug}/stats` — estadísticas específicas del tenant: usuarios (total/activos/pendientes/suspendidos), apps (total), memberships (total/activas), sesiones activas | `keygo-api`, `keygo-app`, `keygo-supabase` | Habilita widgets del dashboard en el rol `ADMIN_TENANT` sin exponer datos globales; complementa `GET /api/v1/platform/stats` de nivel global |
| T-071 | Agregar filtros de fecha `created_after` y `created_before` al endpoint `GET /api/v1/tenants` y al use case `ListTenantsUseCase` / `TenantFilter` | `keygo-api`, `keygo-app`, `keygo-supabase` | Permite al dashboard mostrar tenants creados recientemente (últimos N días) y soporta análisis de crecimiento temporal sin escanear todo el set |
| T-076 | Reemplazar la `recentActivity` aproximada del dashboard (basada en `created_at` de entidades) por una tabla de auditoría formal `audit_events` con `V16__add_audit_events.sql` — campos: `id`, `tenant_id`, `actor_id`, `event_type` (enum), `entity_type`, `entity_id`, `metadata` JSONB, `occurred_at` | `keygo-supabase`, `keygo-app`, `keygo-api` | La implementación actual carga todas las entidades recientes en memoria; la tabla de auditoría permite consultas eficientes, filtrables por tipo y tenant, y es base para auditoría formal (F-034) |
| T-077 | Agregar endpoint de alertas activas dedicado `GET /api/v1/admin/alerts` con paginación (`page`, `size`) y filtros por nivel (`error`, `warning`, `info`) y categoría (`security`, `tenant`, `registration`) — las alertas provienen del `GetPlatformDashboardUseCase.buildAlerts()` y de detecciones proactivas | `keygo-api`, `keygo-app` | Permite al frontend mostrar un feed de alertas sin recargar el dashboard completo; facilita la construcción de un panel de notificaciones operativas persistentes |
| T-084 | Integrar gateway de pago real (MercadoPago / Stripe) que reemplace el endpoint `mock-approve-payment`; adapter configurable por `keygo.billing.payment-provider`; `PaymentGatewayPort` en `keygo-app` | `keygo-app`, `keygo-infra`, `keygo-run` | El mock es solo para dev/test; producción requiere integración real con webhook de confirmación de pago |
| T-085 | Renovación automática de suscripciones vía `@Scheduled` job: detectar suscripciones con `currentPeriodEnd < now()` y `autoRenew=true`, generar nueva factura y actualizar período; campo `nextBillingAt` ya existe en `app_subscriptions` | `keygo-app`, `keygo-run`, `keygo-supabase` | Sin renovación automática, las suscripciones expiran sin aviso; es la base del modelo SaaS recurrente |
| T-086 | Soporte Bearer TENANT_USER en `GET /billing/subscription`: resolver `subscriberId` desde el claim `sub` del JWT en lugar de asumir siempre `SubscriberType.TENANT` | `keygo-api`, `keygo-app` | El controller actual solo soporta TENANT (B2B); B2C requiere identificar al usuario desde el token para consultar su suscripción individual |
| T-092 | Implementar script CI que compare las columnas `NOT NULL` de todas las entidades JPA con las definiciones de las migraciones Flyway correspondientes y falle si detecta desincronía | `keygo-supabase`, CI | Detecta columnas `NOT NULL` en entidades que no existen en el schema sin levantar un contenedor; complementa T-091 (Testcontainers) con una verificación estática más rápida y ejecutable en cualquier runner CI |

---

### Largo plazo

> Capacidades estratégicas del sistema; alto esfuerzo o dependencias externas.
>
> **Prioridad post-lanzamiento:** estandarizar el protocolo hacia redirect `302` y evaluar verdadera sesión compartida entre múltiples UIs como una capacidad diferenciada del hosted login actual.

| ID | Propuesta | Módulo | Justificación |
|---|---|---|---|
| T-017 | Renombrar `keygo-supabase` a `keygo-adapter-persistence-postgres` para neutralizar el nombre respecto al proveedor y reflejar el rol de adapter | Infra | El nombre actual acopla semánticamente a Supabase; facilita soportar otros providers |
| T-018 | Implementar ADRs (Architecture Decision Records) en `docs/adr/` para las decisiones de diseño más relevantes (hexagonal, multi-módulo, Jackson 3, Spring Boot 4) | Docs | Facilita onboarding y justifica decisiones ante nuevos colaboradores |
| T-019 | Evaluar migración a GraalVM Native Image para reducir arranque y footprint en despliegue containerizado | `keygo-run` | Relevante si se despliega en entornos con arranque en frío frecuente |
| T-020 | Implementar observabilidad avanzada: tracing distribuido con OpenTelemetry, métricas en Prometheus, dashboards en Grafana | Infra | Necesario para operación en producción real del SaaS |
| T-021 | Diseñar estrategia de multi-region / alta disponibilidad para el servicio de autenticación | Infra | Crítico para SLAs de producción en IAM |
| T-022 | Implementar caching distribuido (Redis) para tokens, JWKS y sesiones activas | `keygo-infra` | Reducir latencia y carga a DB en validación de tokens |
| T-032 | Evaluar generador de site estático (MkDocs con Material theme / Docusaurus) para consolidar `docs/` + archivos raíz en un portal navegable unificado con búsqueda full-text y versionado | `docs/` | El repositorio ya tiene ~30 archivos Markdown en múltiples carpetas; un site estático facilita onboarding y búsqueda entre categorías |
| T-038 | Implementar lista negra de JTI (JWT ID) de access tokens revocados con TTL en Redis — permite invalidar access tokens antes de su expiración natural sin mantener estado en DB SQL | `keygo-infra`, `keygo-app` | El modelo actual requiere esperar expiración del access token tras revocar un refresh token; con lista negra de JTI + Redis la revocación es inmediata incluso para access tokens ya emitidos |
| T-047 | Implementar SCIM 2.0 endpoint `/api/v1/tenants/{slug}/scim/v2/Users` para aprovisionamiento y sincronización de perfiles desde sistemas HR externos (Workday, BambooHR) | `keygo-api`, `keygo-app` | Estándar de aprovisionamiento de identidades para integraciones enterprise; requiere mapeo `tenant_users` ↔ SCIM User Schema |
| T-048 | Soporte a esquemas de atributos personalizados por tenant — el admin define campos adicionales del perfil (análogo a Keycloak `declarativeUserProfile`); requiere tabla de metadatos de esquema y validación dinámica | `keygo-supabase`, `keygo-app`, `keygo-api` | Permite a cada tenant extender el perfil con campos de negocio propios sin migraciones de DB adicionales |
| T-055 | Implementar bootstrap programático de tenants/apps/roles vía control-plane admin (sin dependencia de seeds SQL para producción) | `keygo-api`, `keygo-app`, `keygo-run` | Habilita inicialización controlada y auditable en despliegues reales, desacoplando provisión de datos de Flyway |
| T-059 | Evolucionar a redirect OAuth2 clásico: el backend entrega `authorization_code` con redirect HTTP `302` hacia `redirect_uri` en vez de retornarlo en JSON | `keygo-api`, `keygo-app`, `keygo-run`, `keygo-ui` | Reduce lógica de orquestación en frontend, mejora interoperabilidad con terceros y acerca la implementación al comportamiento esperado por clientes OAuth estándar |
| T-060 | Evaluar gateway de federación / sesión compartida para `keygo-ui` central: patrón BFF/gateway que administre sesión entre múltiples UIs sin confundirlo con el hosted login actual | `keygo-ui`, `keygo-api`, `keygo-run` | Separa claramente login hospedado de SSO real y prepara una evolución ordenada hacia ecosistemas con varias aplicaciones conectadas |
| T-064 | Estandarizar catálogo i18n de errores por dominio (`auth`, `tenant`, `membership`) combinando `origin` + `clientRequestCause` para resolver `clientMessage` por locale | `keygo-api`, `keygo-app` | Permite mensajes consistentes, accionables y localizables por tipo de error sin exponer detalles técnicos |
| T-072 | Implementar dashboard de sesiones activas en el control-plane: `GET /api/v1/platform/sessions` (total/activas/expiradas), dependiente de T-037 (endpoints de gestión de sesiones) | `keygo-api`, `keygo-app`, `keygo-supabase` | Visibilidad operativa de sesiones activas en toda la plataforma; requiere que T-037 esté implementado primero |
| T-073 | Integrar Micrometer con Prometheus para exportar métricas de plataforma en tiempo real: `keygo_tenants_total`, `keygo_users_total`, `keygo_sessions_active`, `keygo_tokens_issued_total` | `keygo-run`, `keygo-infra` | Complementa `GET /api/v1/platform/stats` (consulta bajo demanda) con métricas push en tiempo real exportables a Grafana/Alertmanager; necesario para observabilidad en producción |
| T-078 | Implementar WebSocket o Server-Sent Events (SSE) en `GET /api/v1/admin/platform/dashboard/stream` para push de métricas cada 30 s — el servidor emite snapshots periódicos del dashboard sin polling del cliente; requiere Spring WebFlux o `SseEmitter` + scheduler | `keygo-api`, `keygo-app`, `keygo-run` | Elimina polling agresivo del frontend y reduce latencia de actualización de KPIs en tiempo real; dependiente de T-074 (caché) para no saturar DB en cada push |
| T-079 | Agregar histograma temporal en el dashboard: `GET /api/v1/admin/platform/dashboard/histogram?days=30` — retorna series de tiempo diarias para registros de usuarios, sesiones iniciadas y logins exitosos/fallidos en los últimos N días, para gráficas de tendencia en el UI | `keygo-api`, `keygo-app`, `keygo-supabase` | Permite visualizar tendencias de crecimiento y patrones de uso; requiere tabla `audit_events` (T-076) para ser preciso, o bien queries nativas de `GROUP BY DATE(created_at)` sobre `tenant_users` y `sessions` como primera aproximación |
| T-087 | Generación de PDF de facturas: `InvoicePdfPort` en `keygo-app` + adapter con iText/JasperReports en `keygo-infra`; PDF almacenado en S3/Supabase Storage; URL guardada en campo `pdf_url` de `invoices` | `keygo-app`, `keygo-infra`, `keygo-supabase` | Mejora experiencia de pago para clientes B2B que requieren factura en PDF para contabilidad; el campo `pdf_url` ya existe en la entidad |
| T-088 | Factura electrónica CFDI México: integración con PAC (Proveedor Autorizado de Certificación); emitir XML CFDI 4.0 post-pago y almacenar UUID fiscal; requiere CSD del emisor y configuración de PAC | `keygo-app`, `keygo-infra`, `keygo-run` | Obligatorio para clientes mexicanos con facturación electrónica; la tabla `invoices` ya tiene `billing_tax_id_snapshot` como campo preparatorio |
| T-089 | Billing multi-currency: tabla `exchange_rates` (`V22__add_exchange_rates.sql`) con tasas diarias; campo `exchange_rate_snapshot` en `invoices`; convertir `base_price` al momento de generar contrato según moneda del suscriptor | `keygo-supabase`, `keygo-app`, `keygo-api` | Soporta clientes internacionales; la tabla `app_plan_versions` ya tiene campo `currency` como punto de partida |
| T-090 | Motor de dunning: tabla `dunning_events`, job que detecta facturas vencidas y en estado `ISSUED` con `due_date < now()`, reintenta cobro en D+1/D+3/D+7 con notificación email por evento; suspende suscripción en D+7 sin respuesta | `keygo-app`, `keygo-run`, `keygo-supabase` | Automatiza recuperación de ingresos sin intervención manual; requiere gateway real (T-084) para ejecutar reintentos de cargo |
| T-093 | Evaluar migración a Liquibase o adopción de jOOQ code generation para mantener el schema en sincronía automática con el código; comparar esfuerzo de migración desde Flyway y curva de aprendizaje del equipo | `keygo-supabase`, `keygo-domain` | Elimina estructuralmente la clase de errores "columna en entidad sin migración"; Liquibase permite diffs automáticos, jOOQ genera entidades tipadas desde el schema; evaluar viabilidad y compatibilidad con Spring Boot 4.x antes de comprometerse |

---

## Propuestas funcionales

> Organizadas según el orden de implementación recomendado en `docs/arch/keygo_server_backlog_v_1.md`.

### Fase 0 — Fundación técnica

| ID | Épica / Historia | Prioridad | Descripción resumida |
|---|---|---|---|
| F-001 | E1-H2: Estándares de calidad | P0 | Lint/format, convenciones de commits, pipeline CI inicial |
| F-002 | E1-H3: Persistencia base | P0 | PostgreSQL + Flyway + Testcontainers operativos end-to-end |
| F-003 | E2-H1: Modelo `Tenant` | P0 | Entidad de dominio, persistencia, unicidad por `slug` |
| F-004 | E2-H2: Resolución de tenant | P0 | Propagar contexto de tenant a la request desde entrada HTTP |
| F-005 | E2-H3: Aislamiento lógico por tenant | P0 | `tenant_id` en entidades relevantes, índices, validaciones |

### Fase 1 — Core IAM usable

| ID | Épica / Historia | Prioridad | Descripción resumida |
|---|---|---|---|
| F-006 | E3-H1: Modelo `ClientApp` | P0 | `client_id`, `client_secret_hash`, tipo PUBLIC/CONFIDENTIAL |
| F-007 | E3-H2: Redirect URIs | P0 | Gestión y validación de URIs permitidas por app |
| F-008 | E4-H1: Modelo `User` | P0 | Email/username, password hash, status, unicidad por tenant |
| F-009 | E5-H1: Modelo `Membership` | P0 | Relación user ↔ app, unicidad, estado |
| F-010 | E6-H1: Endpoint `/oauth2/authorize` | P0 | Validar tenant, client, redirect URI, response type, PKCE |
| F-011 | E6-H2: Hosted Login UI | P0 | Pantalla de login central integrada al flujo OAuth2 |
| F-012 | E6-H3: Autenticación de credenciales | P0 | Validar usuario, password, estado, acceso por membership |
| F-013 | E6-H4: Emisión de authorization code | P0 | Code temporal con expiración, amarrado a PKCE challenge |
| F-014 | E6-H5: Endpoint `/oauth2/token` | P0 | Canje de code, validación PKCE, emisión de tokens |
| F-015 | E8-H1: Signing keys | P0 | Clave activa, historial, soporte a rotación |
| F-016 | E8-H2: Endpoint JWKS | P0 | `/.well-known/jwks.json` por tenant |
| F-017 | E14-H1: Hashing seguro de passwords | P0 | BCrypt/Argon2 desde el primer día |
| F-018 | E14-H2: Hashing seguro de refresh tokens | P0 | Nunca persistir refresh tokens en texto plano |

### Fase 2 — Vendible para terceros

| ID | Épica / Historia | Prioridad | Descripción resumida |
|---|---|---|---|
| F-019 | E3-H3: Grants y scopes por app | P1 | Auth Code+PKCE, Client Credentials, scopes configurables |
| F-020 | E3-H4: Rotación de client secret | P1 | Mostrar secret una sola vez; invalidar anterior |
| F-021 | E4-H2: Alta de usuario desde admin | P1 | Endpoint admin para creación de usuarios |
| F-022 | E4-H3: Desactivar usuario | P1 | Suspensión lógica; impide login |
| F-023 | E4-H4: Reset de contraseña | P1 | Token seguro de reset; endpoint de cambio |
| F-024 | E5-H4: `AppRole` y `MembershipRole` | P1 | Roles locales por app; roles en tokens |
| ~~F-025~~ | ~~E7-H1: `client_credentials` grant~~ | ~~P1~~ | ✅ Completada en Fase 8 — ver historial |
| F-026 | E8-H3: `openid-configuration` | P1 | Metadata OIDC por tenant |
| ~~F-027~~ | ~~E8-H4: Refresh tokens con rotación~~ | ~~P1~~ | ✅ Completada en Fase 7 — ver historial |
| ~~F-028~~ | ~~E9-H1: Endpoint `/userinfo`~~ | ~~P1~~ | ✅ Completada en Fase 7 — ver historial |
| F-029 | E10: Tenant Admin API | P1 | CRUD de apps, usuarios, memberships, roles |
| F-030 | E12-H1/H2/H3: Self-service del usuario | P1 | Forgot password, reset, change password |
| F-031 | E14-H3: Rate limiting | P1 | Proteger login y token endpoint contra fuerza bruta |
| F-032 | E14-H5: Validación estricta de redirect URIs | P1 | Coincidencia exacta; sin wildcards peligrosos |

### Fase 3 — Operación real del SaaS

| ID | Épica / Historia | Prioridad | Descripción resumida |
|---|---|---|---|
| F-033 | E11: Control plane / Platform Admin | P1 | Crear/suspender tenants, auditoría global |
| F-034 | E13-H1: Auditoría de eventos críticos | P0 | Login, token emitido, secret rotado, membership creada |
| F-035 | E13-H2: Logs estructurados | P1 | JSON logs con correlación de request/tenant |
| F-036 | E13-H3: Métricas básicas | P1 | Prometheus/Actuator: logins, tokens, errores |
| F-037 | E12-H4/H5: Ver/cerrar sesiones activas | P2 | Self-service de sesiones para el usuario final |
| F-038 | E13-H4: Alertas operativas | P2 | Umbral de errores de login, tasa de tokens fallidos |
| F-039 | `keygo-ui` — Frontend React unificado + hosted login | P1 | Aplicación React unificada (no tres portales separados) con Vite + React 19 + TypeScript, routing por rol (`ADMIN`, `ADMIN_TENANT`, `USER_TENANT`) y capacidad de operar como login central para otras UIs mediante hosted login. Paquetes compartidos esperados: cliente HTTP, utilidades PKCE/tokens y tipos `BaseResponse`. Manual de implementación en `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`. |
| F-040 | RBAC granular en control-plane admin | P1 | Autorizar endpoints `/api/**` de administración por permisos/acciones (p. ej. `tenant:create`, `user:suspend`) y no solo por rol global `ADMIN`; base para delegación segura por dominio |
| F-041 | SSO multi-app para ecosistema KeyGo | P2 | Diseñar sesión compartida explícita entre múltiples UIs/apps con contrato formal distinto al hosted login actual; evaluar issuer común, sesión federada, logout coordinado y límites entre login central, BFF y aplicaciones consumidoras |

---

## Features fuera del MVP v1

> Capacidades válidas pero explícitamente postergadas para no sobrecomplicar el MVP.

| Feature | Motivo de postergación |
|---|---|
| MFA (TOTP, SMS, WebAuthn) | Aumenta complejidad de UX y backend; agregar en v2 |
| SAML 2.0 | Requerido por enterprise; no necesario para MVP |
| SCIM 2.0 (provisioning) | Integración HR/IdP; postergada a v2-v3 |
| ABAC (Attribute-Based Access Control) | Roles suficientes para MVP; ABAC es extensión |
| Social Login (Google, GitHub, etc.) | OAuth2 externo; útil pero no bloquea MVP |
| WebAuthn / Passkeys | Tecnología emergente; agregar cuando sea estable |
| Multi-region / HA | Necesario en producción real; fuera del alcance del MVP técnico |

---

## Historial de propuestas completadas

> Mover aquí las propuestas implementadas para mantener trazabilidad.

| ID original | Propuesta | Completada | PR / Commit referencia |
|---|---|---|---|
| T-081 | **Tests de controller billing** — `AppBillingPlanControllerTest` (7 tests), `AppBillingContractControllerTest` (5 tests), `AppBillingSubscriptionControllerTest` (3 tests); `CreateAppPlanUseCaseTest` (5 tests: happy path, sin entitlements, duplicado, status correcto, moneda por defecto); patrón Mockito puro sin Spring context | 2026-03-28 | `AppBillingPlanControllerTest.java`; `AppBillingContractControllerTest.java`; `AppBillingSubscriptionControllerTest.java`; `CreateAppPlanUseCaseTest.java` |
| T-080 | **`V21__seed_billing_keygo_plans.sql`** — planes FREE/STARTER/BUSINESS/ENTERPRISE para `keygo-platform` app con precios en MXN (0/299/999/3999 MXN/mes) y 7 entitlements por plan: `MAX_TENANT_USERS`, `MAX_CLIENT_APPS`, `MAX_MONTHLY_TOKENS`, `SOCIAL_LOGIN`, `CUSTOM_DOMAIN`, `SLA_UPTIME_PCT`, `AUDIT_LOG_DAYS`; ENTERPRISE sin límites duros + `DEDICATED_SUPPORT` | 2026-03-28 | `V21__seed_billing_keygo_plans.sql` |
| T-056 | **Hosted login seguro en `keygo-ui`** — implementación de referencia portable en `examples/hosted-login-handoff/` con contrato tipado `HostedLoginParams`, parser/guard runtime, componente reutilizable `HostedLoginBoundary`, ejemplo `createHostedLoginRedirectUrl()` y 7 tests `Vitest`; la guía frontend enlaza el contrato y el boundary mínimos del handoff | 2026-03-26 | `examples/hosted-login-handoff/package.json`; `examples/hosted-login-handoff/src/hostedLoginParams.ts`; `examples/hosted-login-handoff/src/HostedLoginBoundary.tsx`; `examples/hosted-login-handoff/src/exampleUsage.tsx`; `examples/hosted-login-handoff/tests/`; `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md` |
| T-041 | **Fase 9b — V13 + perfil OIDC extendido:** migración `V13__extend_tenant_user_profile.sql` agrega 6 campos OIDC a `tenant_users` (`phone_number`, `locale`, `zoneinfo`, `profile_picture_url`, `birthdate`, `website`); `User` domain extendido con `updateProfile()`; `TenantUserEntity` + `UserPersistenceMapper` actualizados; `UserInfoResult` ahora retorna claims extendidos; `UpdateUserCommand/Request` extendidos | 2026-03-24 | `V13__extend_tenant_user_profile.sql`; `User.java`; `TenantUserEntity.java`; `UserPersistenceMapper.java` |
| T-042 | **Fase 9b — Endpoints self-service de perfil:** `GetUserProfileUseCase`, `UpdateUserProfileUseCase`, `AccountProfileController` (GET+PATCH `/account/profile`); `UserProfileData`, `UpdateUserProfileRequest`; sufijo `accountProfilePathSuffix` en filtro; 7 nuevos tests unitarios | 2026-03-24 | `AccountProfileController.java`; `GetUserProfileUseCase.java`; `UpdateUserProfileUseCase.java`; `UserProfileUseCaseTest.java` |
| T-045 | **Claim `roles` en JWT** — agregar lista de roles del usuario al `access_token` e `id_token`; nuevo método `findRoleCodesByUserAndClientApp()` en `MembershipRepositoryPort`; consulta nativa JOIN en `MembershipJpaRepository`; `TokenClaimsFactoryPort` extendido con parámetro `List<String> roles` en ambas firmas; `StandardTokenClaimsFactory` emite claim solo si la lista es no-nula y no-vacía; `IssueTokensUseCase`, `RotateRefreshTokenUseCase` y `AuthorizationController` actualizados; M2M (`client_credentials`) pasa `null` (sin membresía de usuario) | 2026-03-24 | `MembershipRepositoryPort.java`; `MembershipJpaRepository.java`; `MembershipRepositoryAdapter.java`; `TokenClaimsFactoryPort.java`; `StandardTokenClaimsFactory.java`; `IssueTokensUseCase.java`; `RotateRefreshTokenUseCase.java`; `AuthorizationController.java`; `ApplicationConfig.java` |
| T-046 | **JWT admin en BootstrapAdminKeyFilter** — los endpoints `/api/**` ahora aceptan `Authorization: Bearer <jwt>` con claim `roles` que contenga alguno de `keygo.bootstrap.admin-roles` (default: `["ADMIN"]`) como alternativa a `X-KEYGO-ADMIN`; rutas OAuth2 públicas: `/oauth2/authorize`, `/account/login`, `/oauth2/token`; inyección opcional con `@Autowired(required=false)` de `AccessTokenVerifierPort` + `SigningKeyRepositoryPort`; 8 tests nuevos incluyendo regresión | 2026-03-24 | `BootstrapAdminKeyFilter.java`; `KeyGoBootstrapProperties.java`; `application.yml`; `BootstrapAdminKeyFilterTest.java`; `KeyGoBootstrapPropertiesTest.java` |
| T-052 | **Hardening seguridad admin Bearer-only** — se elimina soporte de `X-KEYGO-ADMIN`; `BootstrapAdminKeyFilter` autentica solo Bearer JWT y publica authorities desde claim `roles`; autorización por endpoint con `@PreAuthorize`; validación tenant en `ADMIN_TENANT` vía `tenantAuthorizationEvaluator` (`tenant_slug` o fallback `iss` vs `tenantSlug` path); OpenAPI migra a `BearerAuth`; emisión de `tenant_slug` en access tokens | 2026-03-25 | `SecurityConfig.java`; `BootstrapAdminKeyFilter.java`; `TenantAuthorizationEvaluator.java`; controllers admin en `keygo-api`; `OpenApiConfig.java`; `IssueTokensUseCase.java`; `RotateRefreshTokenUseCase.java`; `IssueClientCredentialsTokenUseCase.java` |
| T-047 | **SigningKeyInitializer** — `@Profile({"supabase","local"}) ApplicationRunner` que genera un par RSA-2048 y lo persiste via `SigningKeyRepositoryPort` si no existe clave ACTIVE en startup; idempotente en re-inicios. ⚠️ Perfil extendido a `local` el 2026-03-27 tras error `No active signing key found` en H2. | 2026-03-24 | `SigningKeyInitializer.java` (nuevo en `keygo-run/startup/`) |
| T-039 | **Fase 8: `client_credentials` grant (M2M)** — `IssueClientCredentialsTokenUseCase`, rama `grant_type=client_credentials` en `POST /oauth2/token`; solo apps `CONFIDENTIAL`; `sub=clientId`; sin `id_token` ni `refresh_token`; resolución de scopes (intersección o todos si vacío); `CLIENT_CREDENTIALS_TOKEN_ISSUED` `ResponseCode`; `@Bean` en `ApplicationConfig`; 1 request Postman | 2026-03-23 | `IssueClientCredentialsTokenUseCase.java`; `IssueClientCredentialsTokenCommand.java`; `IssueClientCredentialsTokenResult.java`; `AuthorizationController.java` |
| F-025 | **E7-H1: `client_credentials` grant** — M2M sin usuario; access token técnico para apps `CONFIDENTIAL` | 2026-03-23 | `IssueClientCredentialsTokenUseCase`; `AuthorizationController` rama `client_credentials`; Postman request |
| T-027 | **Fase 7: Refresh token (rotación SHA-256), Session, Revocación RFC 7009, UserInfo OIDC §5.3** — `POST /oauth2/token` con `grant_type=refresh_token`, `POST /oauth2/revoke` (público, idempotente), `GET /userinfo` (Bearer token); sesiones persistidas; refresh token hash SHA-256 determinista | 2026-03-22 | `V11__add_refresh_tokens_and_sessions.sql`; `SessionEntity`, `RefreshTokenEntity`; `RotateRefreshTokenUseCase`, `RevokeTokenUseCase`, `GetUserInfoUseCase`, `OpenSessionUseCase`; `RevocationController`, `UserInfoController`; `TokenRequest` multi-grant; 3 nuevas requests Postman |
| F-027 | **E8-H4: Refresh tokens con rotación** — Hash persistido (SHA-256), renovación segura, revocación RFC 7009 | 2026-03-22 | `refresh_tokens` table (V11); `RevokeTokenUseCase`; `RotateRefreshTokenUseCase` |
| F-028 | **E9-H1: Endpoint `/userinfo`** — Claims OIDC del usuario autenticado vía Bearer token | 2026-03-22 | `UserInfoController`; `GetUserInfoUseCase`; `RsaJwtTokenVerifier` |
| — | **Re-auditoría de inconsistencias: corrección de tablas en singular → plural** — `V10__rename_membership_tables_to_plural.sql`: renombra `app_role→app_roles`, `membership→memberships`, `membership_role→membership_roles`; actualiza índices, constraints, entidades JPA (`AppRoleEntity`, `MembershipEntity`), documentación completa (`DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md`, `DATA_DICTIONARY.md`, `INCONSISTENCIAS.datos.md`, `AGENTS.md`) | 2026-03-22 | `V10__rename_membership_tables_to_plural.sql`; `AppRoleEntity.java`; `MembershipEntity.java` |
| T-001 | Corregir bug `BootstrapAdminKeyFilter`: `getRequestURI()` → `getServletPath()` para que el filtro funcione con `context-path` activo; +2 tests de regresión con context-path simulado | 2026-03-21 | `keygo-run/.../filter/BootstrapAdminKeyFilter.java`; `BootstrapAdminKeyFilterTest.java` — 15 tests, 0 fallos |
| T-024 | Implementar `TenantResolutionStrategy` por path variable `/{tenantSlug}/` — los endpoints de Fase 5/6 resuelven tenant desde el path, complementando el header `X-Tenant-Slug` | 2026-03-22 | `TenantContextHolder`; `AuthorizationController`; `AccountController`; `TokenController` — todos los endpoints OAuth2/OIDC ya usan `{slug}` como path variable |
| T-027 | Integrar Swagger / OpenAPI con SpringDoc 3.0.1 (compatible Spring Boot 4.x): `OpenApiConfig` en `keygo-run`, anotaciones `@Tag`/`@Operation`/`@ApiResponses` en los 4 controllers, 3 grupos de API, SecurityScheme `AdminKeyAuth` | 2026-03-21 | `keygo-run/config/OpenApiConfig.java` nuevo; `keygo-api/pom.xml` dependencia springdoc 3.0.1; controllers anotados; Swagger UI en `/keygo-server/swagger-ui/index.html` |
| T-016 | Configurar JaCoCo para cobertura de tests y fallar el build si baja del umbral definido | 2026-03-21 | Plugin en `pom.xml` raíz; umbral 60% instrucciones; `report-aggregate` en `keygo-run`; CI actualizado a `./mvnw verify` |
| T-006 | Configurar GitHub Actions: pipeline CI mínimo con `./mvnw test` y `./mvnw clean package` en cada push/PR | 2026-03-21 | `.github/workflows/ci.yml` creado; Fase 0 cerrada |
| — | Reorganizar paquetes internos por feature (keygo-api, keygo-app, keygo-run, keygo-supabase) | 2026-03-17 | Refactor de estructura interna |
| F-003 | E2-H1: Modelo `Tenant` — entidad de dominio, persistencia, unicidad por `slug` | 2026-03-21 | `keygo-domain/tenant/model/`, `keygo-supabase/tenant/`, migración `V4__add_tenants.sql`, puertos y use cases en `keygo-app/tenant/` |
| F-004 | E2-H2: Resolución de tenant — propagar contexto de tenant a la request desde entrada HTTP | 2026-03-21 | `TenantContextHolder` (keygo-app), `TenantResolutionFilter` por header `X-Tenant-Slug` (keygo-run) |
| F-009 | E5-H1: Modelo `Membership` — relación user ↔ app, unicidad, estado; `AppRole` — roles locales por app | 2026-03-21 | `keygo-domain/membership/model/` (Membership, AppRole, MembershipRole, MembershipStatus, etc.); `keygo-supabase/membership/` (entidades JPA, adapters); migración `V7__add_memberships.sql`; 3 use cases (CreateMembership, RevokeMembership, ListMemberships); 2 controllers con 5 endpoints; 210+ tests totales en proyecto |
| — | **Fase 5: Núcleo OAuth2/OIDC — Authorization Code + PKCE** | 2026-03-22 | Dominio: `AuthorizationCode`, `AuthorizationCodeStatus`, `CodeChallenge`, `ScopeSet`, 4 excepciones; App: 4 puertos, 4 use cases, 4 comandos, 3 results; Supabase: `AuthorizationCodeEntity`, JPA repo, mapper, adapter, migración `V8__add_oauth_authorization_codes.sql`; API: `AuthorizationController` con 3 endpoints (authorize, login, token exchange), 4 DTOs request/response, 5 handlers en `GlobalExceptionHandler`; Infra: `PkceVerifier`; Run: `SystemClockProvider`, 6 `@Bean` nuevos; **270+ tests totales, build SUCCESS, Postman +3 requests en carpeta "🔐 OAuth2 Authorization"** |

---

## Instrucciones de mantenimiento para agentes AI

> Esta sección define cómo los agentes deben actualizar este documento.

### Cuándo actualizar este archivo

| Evento | Acción |
|---|---|
| Se completa una propuesta técnica o funcional | Mover la fila a la tabla **"Historial de propuestas completadas"** con fecha y referencia al commit/PR |
| Se genera una nueva propuesta técnica al concluir una tarea | Agregar fila en la tabla correspondiente de **Propuestas técnicas** (corto/mediano/largo plazo) |
| Se decide descartar o posponer una propuesta | Mover a **"Features fuera del MVP v1"** con justificación; o simplemente eliminar con nota en el commit |
| Se actualiza el horizonte temporal de una propuesta | Mover la fila a la tabla del nuevo horizonte |

### Formato para IDs

- Técnicas: `T-NNN` (correlativo, continuando desde el último)
- Funcionales: `F-NNN` (correlativo, continuando desde el último)

### Regla de escritura

- Las propuestas deben ser **concretas y accionables** (no genéricas).
- Cada propuesta debe indicar el **módulo afectado** y la **justificación**.
- No crear propuestas duplicadas: revisar las tablas antes de agregar.

### Referencia cruzada con AI_CONTEXT.md

Cuando se registre una propuesta en este archivo **que sea recurrente o de alto valor**, añadir también
una entrada breve en `AI_CONTEXT.md → ## Propuestas de mejoras futuras` con la referencia al ID
(p. ej. `ver ROADMAP.md T-010`).

