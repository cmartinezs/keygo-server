# AGENTS — Registro de Cambios

> Sub-documento del quick-start de agentes consolidado en [`agents.md`](agents.md).
>
> Historial cronológico de actualizaciones al quick-start: módulos, comandos, patrones y URLs.
> Entradas anteriores a 2026-03-25 en [`../99-archive/ai/agents-change-log-historical.md`](../99-archive/ai/agents-change-log-historical.md).
>
> **Regla:** Agregar entrada cada vez que cambie estructura de módulos, comandos, patrones o URLs.

---

## Formato de entrada

```markdown
### [YYYY-MM-DD] Descripción del cambio
- Bullet con detalle
```

---

## Registro de cambios

### [2026-04-13] Regla de estilo: respuestas sin verborrea innecesaria

- **`agents.md` y `agent-operations.md`:** reforzada la instrucción de que las respuestas del agente deben ser solo lo justo, concisas, precisas y centradas en lo importante.
- **Aclaración explícita:** la regla aplica a la conversación del asistente y no restringe el nivel de detalle necesario en código, tests, contratos o documentación técnica.

### [2026-04-13] Workflow RFC: ingreso directo y submáquina interna

- **`workflow.md`:** ampliado para permitir ingreso directo por RFC sin tarea previa y para modelar el RFC como artefacto que sigue el flujo general existente.
- **Submaquina interna de RFC:** agregada definicion formal de fases y subtareas, con decisiones por subtarea (`aprobada`, `replanteada`, `descartada`) e implementacion secuencial solo despues de aprobar el conjunto implementable.
- **Semantica refinada:** `Implementacion secuencial subtarea 1..N` ocurre en estado general `🔵 En desarrollo`; la verificacion del RFC completo ocurre en `🔄 En revisión`; `🛂 Control de cambio` puede dispararse desde revision aun sin dependencia UI.
- **Regla de reingreso a desarrollo:** si un RFC vuelve a `🔵 En desarrollo`, el ajuste debe anexarse a una fase/subtarea existente o crear una nueva subtarea en la fase correspondiente, manteniendo continuidad sobre lo ya implementado.
- **`agents.md`, `agent-operations.md` y `tasks/README.md`:** alineados para explicar que un RFC puede ser el artefacto primario y que su seguimiento interno no reemplaza el estado general.

### [2026-04-13] Regla de contratos API: JSON en `snake_case`

- **`03-api-conventions.md`:** explicitada la regla de que request bodies y response bodies deben usar claves `snake_case`, corrigiendo ejemplos base del envelope y paginación.
- **`frontend/README.md`:** agregado el naming JSON como convención de integración que UI debe considerar.
- **`agents.md` y `agent-operations.md`:** incorporada la regla operativa para evitar nuevos contratos `camelCase` en body de request/response.

### [2026-04-13] Workflow de tareas: nuevo estado `🛂 Control de cambio`

- **`workflow.md`:** se incorporó el estado `🛂 Control de cambio` para solicitudes de ajuste detectadas después de `🧩 Pendiente integración UI`.
- **Transiciones nuevas:** `🧩 Pendiente integración UI → 🛂 Control de cambio → 🔵 En desarrollo` si el usuario aprueba reabrir la misma tarea; o `🛂 Control de cambio → ✅ Completada` si se rechaza y el nuevo alcance se registra como tarea derivada.
- **`agents.md`, `agent-operations.md` y `tasks/README.md`:** alineados para exigir trazabilidad del control de cambio y la creación de una nueva tarea cuando el ajuste no se incorpora al alcance original.

### [2026-04-13] Regla: feedback resuelto debe apuntar al artefacto que lo cerró

- **`doc/02-functional/frontend/feedback/README.md`:** nueva regla para que todo feedback `🟢 Resuelto` deje explícita la tarea, RFC o artefacto que materializó la resolución.
- **`doc/02-functional/frontend/feedback/README.md`:** plantilla y pasos de mantenimiento actualizados para exigir la referencia al cerrar una entrada.
- **`agents.md` y `agent-operations.md`:** incorporada la regla operativa para mantener trazabilidad entre feedback UI↔Backend y la tarea técnica que lo resolvió.

### [2026-04-13] Regla de codificación: evitar `Object` y `Object[]` en firmas

- **`agents.md` §Reglas críticas:** nueva regla para evitar `Object`, `Object[]` y genéricos parametrizados con `Object` en firmas públicas o internas relevantes.
- **Criterio:** preferir contratos tipados explícitos (`record`, DTO, value object, proyección o interfaz dedicada) para parámetros, retornos y estructuras genéricas.
- **Motivación:** reducir ambigüedad, casts inseguros y fallas tardías en compilación/tests/runtime.

### [2026-04-12] Regla: documentación obligatoria al usar `/plan`

- **`CLAUDE.md` §2 (Solution plan):** ampliado con instrucción explícita: al invocar `/plan`, antes de cerrar el modo plan y **sin esperar instrucción del usuario**, se debe (1) agregar entrada `T-NNN` en `doc/09-ai/proposals.md`, (2) crear `doc/09-ai/tasks/T-NNN-<slug>.md` con requisito, diseño, pasos ordenados y guía de verificación, (3) registrar la entrada en `doc/09-ai/tasks/README.md`.
- **`agents.md` §Mantenimiento documental obligatorio:** agregado ítem "Uso de `/plan`" con los mismos 3 pasos.
- **Motivación:** los planes se pueden implementar en una conversación futura; sin persistencia inmediata se pierde el contexto entre sesiones.

### [2026-04-12] Frontend — documentación expandida a 9 documentos temáticos

- Monolito `frontend-developer-guide.md` archivado en `99-archive/frontend/`.
- Creados 9 documentos en `doc/02-functional/frontend/`: setup, authentication, api-conventions, error-handling, endpoints-account, endpoints-tenant, endpoints-billing, endpoints-admin, testing.
- `README.md` del frontend actualizado como índice operativo con reglas de mantenimiento por tipo de cambio.

### [2026-04-12] Política operativa de agentes — nuevas reglas en `agent-operations.md` y `CLAUDE.md`

- **Respuestas:** exactas, concisas y precisas; sin verborrea, sin sacrificar claridad.
- **Flujo obligatorio pre-implementación:** análisis previo (reusar/refactorizar/nuevo) → plan documentado (`PENDIENTE`/`APLICADO`) → RFC si afecta múltiples módulos/contratos/modelo (`BORRADOR`/`APROBADO`/`APLICADO`) → esperar aprobación explícita → registrar ideas futuras detectadas en `proposals.md`.
- **Navegación por índices:** leer `README.md` de carpeta antes de abrir archivos; solicitar documento de partida; si no hay uno, partir de `doc/README.md`; mantener índices al agregar o crear documentos.
- **Cierre de tarea:** preguntar qué debe recordarse → comprimir y persistir solo lo no derivable del código/git → actualizar `README.md` de la carpeta si se crea/modifica algún documento.
- **`doc/09-ai/README.md`:** convertido de lista plana a tabla con descripción breve por documento.

### [2026-04-09] Refactoring documental canónico multi-agente

- **Entrypoints raíz:** `README.md`, `AI_CONTEXT.md`, `ARCHITECTURE.md` y `AGENTS.md` reescritos como documentos resumidos, actuales y sin redundancia masiva.
- **Nueva fuente de verdad compartida para agentes:** `docs/ai/AGENT_OPERATIONS.md`; `CLAUDE.md` y `.github/copilot-instructions.md` quedan como wrappers livianos.
- **Índice documental:** `docs/README.md` consolidado como politica de ubicacion y tabla de fuentes de verdad.
- **Quick-start del agente:** se eliminan de `AGENTS.md` el inventario exhaustivo de endpoints, el ledger completo de migraciones y la narrativa historica extensa; ese detalle queda referenciado a `docs/api/`, `docs/data/` y `docs/ai/agents-registro.md`.
- **Drift corregido:** seguridad Bearer-only, scripts reales en `docs/scripts/`, rutas `docs/data/*`, `docs/operations/DOCKER.md`, y numeracion de migraciones actualizada a `V1-V33`.
- **Compatibilidad documental:** `docs/PLAN_DOCUMENTACION_COMPLETA.md` queda como stub y su referencia historica se archiva en `docs/archive/plans/`.

### [2026-04-07] T-125 — Membership.PENDING como estado inicial + flujo de aprobación

- **Dominio (`keygo-domain`):** Nuevo método `Membership.approve()` (valida PENDING→ACTIVE, lanza `MembershipAlreadyActiveException` si activa, `MembershipAlreadySuspendedException` si suspendida). Nueva excepción `MembershipAlreadyActiveException`.
- **Use case (`keygo-app`):** `CreateMembershipUseCase` ahora crea membresías con `MembershipStatus.PENDING` (antes `ACTIVE`). Nuevo `ApproveMembershipUseCase` (busca por ID+tenant, llama `approve()`, persiste).
- **Controller (`keygo-api`):** Nuevo endpoint `PUT /api/v1/tenants/{slug}/memberships/{membershipId}/approve` en `TenantMembershipController`. Constructor actualizado con `ApproveMembershipUseCase`.
- **ResponseCode:** Nuevo `MEMBERSHIP_APPROVED`.
- **GlobalExceptionHandler:** Nuevo handler `MembershipAlreadyActiveException` → 409 Conflict.
- **ApplicationConfig (`keygo-run`):** Nuevo bean `approveMembershipUseCase`.
- **Tests:** `ApproveMembershipUseCaseTest` (5 tests), `CreateMembershipUseCaseTest` (5 tests — nuevo). Total: 950 tests, 0 fallos.

### [2026-04-07] Refactorización: tabla unificada verification_codes + cascada multi-capa en login

- **Migración V31 (`keygo-supabase`):** Tabla unificada `verification_codes` con discriminador `purpose` (EMAIL_VERIFICATION, PASSWORD_RESET, PASSWORD_RECOVERY). Migra datos de 3 tablas antiguas y las elimina.
- **Dominio (`keygo-domain`):** `VerificationCode`, `VerificationPurpose` (enum), 3 excepciones unificadas: `VerificationCodeExpiredException`, `VerificationCodeInvalidException`, `VerificationCodeAlreadyUsedException` — todas parametrizadas por `VerificationPurpose`.
- **Eliminados dominio:** `EmailVerification`, `PasswordResetCode`, `PasswordRecoveryToken` + 11 excepciones específicas.
- **Puerto unificado (`keygo-app`):** `VerificationCodeRepositoryPort` reemplaza `EmailVerificationRepositoryPort`, `PasswordResetCodeRepositoryPort`, `PasswordRecoveryTokenRepositoryPort`.
- **Entidad/Repo/Adapter (`keygo-supabase`):** `VerificationCodeEntity`, `VerificationCodeJpaRepository`, `VerificationCodeRepositoryAdapter`. Eliminados 9 archivos antiguos.
- **Nuevas excepciones dominio:** `PlatformUserSuspendedException`, `MembershipPendingException`.
- **Nuevo método:** `Membership.isPending()` en dominio.
- **Use cases actualizados:** 7 use cases migrados a `VerificationCodeRepositoryPort`; `AuthenticatePlatformUserUseCase` ahora valida `RESET_PASSWORD`; `ValidateUserCredentialsUseCase` ahora valida `PlatformUser.SUSPENDED` (cascade); `IssueAuthorizationCodeUseCase` ahora valida `Membership.PENDING`.
- **`ValidateUserCredentialsUseCase`:** nuevo parámetro constructor `PlatformUserRepositoryPort`.
- **`GlobalExceptionHandler`:** 2 nuevos handlers (`PlatformUserSuspendedException` → 403, `MembershipPendingException` → 403); 7 handlers de excepciones eliminadas → 3 unificados.
- **ApplicationConfig:** bean `validateUserCredentialsUseCase` actualizado con 4 parámetros.
- **Tests nuevos:** `ValidateUserCredentialsUseCaseTest` (10 tests), `IssueAuthorizationCodeUseCaseTest` (4 tests), test RESET_PASSWORD en `AuthenticatePlatformUserUseCaseTest`.
- **Tests actualizados:** 8 archivos de test + `UpdateResetValidateUseCaseTest` (7 instancias del constructor corregidas).
- **Próxima migración:** `V32__...`

### [2026-04-07] T-111 — Modelo RBAC multi-ámbito (plataforma + tenant)

- **Dominio (`keygo-domain`):** 8 nuevos tipos en `membership/model/`: `PlatformRoleId`, `PlatformRole`, `PlatformUserRoleId`, `PlatformUserRole`, `TenantRoleId`, `TenantRole`, `TenantUserRoleId`, `TenantUserRole`.
- **TenantRole:** código UPPERCASE `^[A-Z][A-Z0-9_]*$`; métodos `deactivate()`, `reactivate()`, `updateMetadata()`. Diferente de `AppRole` que usa lowercase.
- **TenantUserRole:** soft-delete vía `removedAt`; método `revoke()` con validación idempotente.
- **Entidades JPA (`keygo-supabase/membership/entity`):** `PlatformRoleEntity`, `PlatformUserRoleEntity`, `TenantRoleEntity`, `TenantUserRoleEntity`.
- **Repositorios JPA:** `PlatformRoleJpaRepository`, `PlatformUserRoleJpaRepository`, `TenantRoleJpaRepository`, `TenantUserRoleJpaRepository`.
- **Mapper:** `MembershipPersistenceMapper` — 4 nuevos `toDomain()` overloads.
- **Puertos OUT (`keygo-app/membership/port`):** `PlatformRoleRepositoryPort`, `PlatformUserRoleRepositoryPort`, `TenantRoleRepositoryPort`, `TenantUserRoleRepositoryPort`.
- **Adaptadores:** `PlatformRoleRepositoryAdapter`, `PlatformUserRoleRepositoryAdapter`, `TenantRoleRepositoryAdapter`, `TenantUserRoleRepositoryAdapter`.
- **Use cases:** `AssignPlatformRoleUseCase`, `RevokePlatformRoleUseCase`, `CreateTenantRoleUseCase`, `AssignTenantRoleUseCase`, `RevokeTenantRoleUseCase`.
- **Excepciones:** `PlatformRoleNotFoundException`, `DuplicateTenantRoleException`, `TenantRoleNotFoundException`.
- **Migraciones Flyway:** `V24__platform_roles_and_user_roles.sql`, `V25__tenant_roles_and_user_roles.sql`, `V26__seed_platform_and_tenant_roles.sql`. `V1__drop_all.sql` actualizado.
- **Wiring (`keygo-run/ApplicationConfig`):** 5 nuevos `@Bean` para los use cases T-111.
- **Decisión arquitectural:** `platform_user_roles.tenant_user_id → tenant_users.id` (administradores de plataforma = TenantUsers en el tenant `keygo`). No existe tabla global `users`. Documentado en `docs/plans/documentacion-2026/implementacion/T-111/MODEL.md`.
- **Próxima migración:** `V27__...`



- **`UserNotificationPreferencesEntity`:** campos `UUID userId/tenantId` → `@ManyToOne(LAZY) TenantUserEntity user` + `@ManyToOne(LAZY) TenantEntity tenant`. Repository: `findByUserIdAndTenantId` → `findByUser_IdAndTenant_Id`. Adapter: usa `getReferenceById()` para proxy sin SELECT.
- **`SigningKeyEntity`:** nueva FK `@ManyToOne(LAZY) TenantEntity tenant` (nullable — null = clave global de plataforma).
- **`SessionEntity` + `RefreshTokenEntity`:** nueva FK `@ManyToOne(LAZY) SigningKeyEntity signingKey` (nullable — permite auditar qué clave firmó cada token).
- **Migración:** `V22__signing_key_tenant_scope_and_audit_refs.sql` — agrega columnas `tenant_id` en `signing_keys`, `signing_key_id` en `sessions` y `refresh_tokens`.
- **Dominio:** `SigningKey` + `tenantId` (nullable); `Session.open()` + `Session.reconstitute()` + `RefreshToken.issue()` + `RefreshToken.reconstitute()` reciben `String signingKeyId` como último param (nullable).
- **Puertos:** `SigningKeyRepositoryPort` agrega `findActiveKeyForTenant(TenantId)` y `findPublishableKeysForTenant(TenantId)`.
- **Use cases:** `IssueTokensUseCase.execute()` recibe `TenantId` como primer parámetro; `GetJwksUseCase.execute(tenantSlug)` es ahora tenant-aware con fallback global.
- **Repository JPA:** `SigningKeyJpaRepository.findFirstByStatus()` → `findFirstByTenantIsNullAndStatus()` y `findFirstByTenant_IdAndStatus()`.
- **API:** `JwksController` pasa `tenantSlug` al use case.
- **Próxima migración:** `V23__...`
- **Tests actualizados:** `RefreshTokenTest`, `SessionTest`, `IssueTokensUseCaseTest`, `GetJwksUseCaseTest`, `RevokeTokenUseCaseTest`, `RotateRefreshTokenUseCaseTest`, `ListUserSessionsUseCaseTest`, `JwksControllerTest`, `SigningKeyRepositoryAdapterTest`, `SigningKeyPersistenceMapperTest`, `CreateAppContractUseCaseTest`.
- **Fix colateral:** `PlatformDashboardAdapter.findActiveSigningKey()` actualizado a `findFirstByTenantIsNullAndStatus`.



- **Nueva clase:** `KeyGoUiProperties` (`keygo-run/config/`) con anotación `@ConfigurationProperties(prefix = "keygo.ui")`
  - Propiedades: `baseUrl` (variable de entorno `KEYGO_UI_BASE_URL`), `paths` (mapa de rutas disponibles)
  - Inner class `UiPath` con método `buildUrl(baseUrl, params)` para construir URLs completas con query params
- **Configuración YAML:** sección `keygo.ui` en `application.yml` con:
  ```yaml
  keygo:
    ui:
      base-url: "${KEYGO_UI_BASE_URL:http://localhost:5173}"
      paths:
        reset-password:
          route: "/reset-password"
          query-params: [request-id]
  ```
- **Variables de entorno:** `KEYGO_UI_BASE_URL` añadida a todos los templates (`.env-local`, `.env-desa`, `.env-prod`, `.env.example`)
- **Tests:** `KeyGoUiPropertiesTest` con 11 test cases unitarios validando defaults, URL building con/sin parámetros, y configuración
- **Documentación:** nuevo archivo `docs/design/UI_CONFIGURATION.md` con guía de uso, ejemplos en email templates, y patrón para agregar nuevas rutas
- **Actualización AGENTS.md:** tabla Environment variables + enlace a `UI_CONFIGURATION.md`
- **Propósito:** centralizar la configuración de rutas UI para usar en email adapters (Thymeleaf) sin hardcoding de URLs, facilitando mantenimiento y reutilización

### [2026-04-04] KeyGoTracingAspect — trazabilidad AOP input/output por método

- **Nuevo aspect:** `KeyGoTracingAspect` (`keygo-run/aop/`) con `@Around` que intercepta todos los métodos en `io.cmartinezs.keygo.*` excepto getters/setters y los marcados con `@NoLog`.
- **Formato de log (nivel DEBUG):**
  - `[TRACE_IN]  ClassName.method(param=value, secret=[REDACTED])`
  - `[TRACE_OUT] ClassName.method(param=value) → result [42ms]`
  - `[TRACE_ERR] ClassName.method(param=value) ⚠ ExceptionType: msg [5ms]`
- **Nueva anotación:** `@NoLog` para excluir métodos o clases completas del aspect.
- **Propiedad de control:** `keygo.tracing.method-logging-enabled: true` en `application.yml` — desactivar con `false` en entornos que no lo requieran.
- **Enmascaramiento sensible:** parámetros cuyo nombre contiene `password`, `secret`, `token`, `credential`, `apikey`, `privatekey`, `hash`, `pin` → se muestran como `[REDACTED]`.
- **Fast-path:** si `logger.isDebugEnabled()` es false, el aspect llama `pjp.proceed()` directamente sin ninguna reflexión adicional.
- **Dependencia:** `spring-boot-starter-aspectj` (en Spring Boot 4.x, `spring-boot-starter-aop` fue renombrado y ya no está en el BOM).
- **Tests:** 6 tests unitarios en `KeyGoTracingAspectTest` usando `@SpringJUnitConfig` + `@EnableAspectJAutoProxy` + `ListAppender<ILoggingEvent>` de Logback.

### [2026-04-03] Corrección `logback-spring.xml` — fix `%clr`, appenders por perfil, caracteres literales

- **`logback-spring.xml`:** tres bugs de Logback corregidos en la misma sesión:
  1. `%clr` no registrado → agregado `<include resource="org/springframework/boot/logging/logback/defaults.xml"/>` al inicio del archivo.
  2. Appender `CONSOLE` definido globalmente pero referenciado solo en `<springProfile>` → movida la definición dentro del bloque de perfil que lo usa.
  3. `\[` / `\]` en el patrón causaba `Illegal char '['` → eliminadas barras de escape (los corchetes son literales en Logback).
- **`GlobalExceptionHandlerTest.java`:** `HttpStatus.UNPROCESSABLE_ENTITY` → `HttpStatus.UNPROCESSABLE_CONTENT` (renombrado en Spring Boot 4 / RFC 9110).
- **`logback-spring.xml` (mejoras de diseño):** condición cambiada de `!local` a `!(desa | prod)` para que el perfil `default` también use la consola colorida; `<springProfile name="desa | prod">` agrega archivo JSON rotativo diario (vía `LogstashEncoder`) + consola mínima WARN+. `management.metrics.web.server.request.autotime` eliminado (deprecado en Spring Boot 4, habilitado por defecto vía `WebMvcObservationAutoConfiguration`).
- **Header unificado:** `X-Request-ID` (entrada) y `X-Trace-ID` (salida) unificados en un solo header simétrico `X-Trace-ID` en ambas direcciones — `RequestTracingFilter` lo reutiliza si viene en el request o genera un UUID nuevo.

### [2026-04-02] Trazabilidad end-to-end: RequestTracingFilter + MDC enriquecido + logback-spring.xml

- **Nuevo filtro:** `RequestTracingFilter` (`keygo-run/filter/`) — genera `traceId` UUID (o reutiliza header `X-Trace-ID` del cliente), pone en MDC `traceId`/`method`/`path`, agrega `X-Trace-ID` al response (simétrico), limpia MDC en `finally`.
- **`BootstrapAdminKeyFilter`:** agrega `MDC.put("userId", sub)` tras validar JWT, limpia en `finally`. Nuevo método privado `enrichMdcWithUserId()`.
- **`TenantResolutionFilter`:** agrega `MDC.put("tenantSlug", ...)` tras resolver tenant, limpia en `finally`.
- **`ErrorData.java`:** nuevo campo `traceId` (posición 0 del builder, `@JsonInclude(NON_NULL)` heredado).
- **`ApiErrorDataFactory.java`:** lee `MDC.get("traceId")` en `fromDetail()` y `fromValidationErrors()`. Import `org.slf4j.MDC` agregado.
- **`logback-spring.xml`:** creado en `keygo-run/src/main/resources/`. Perfil `local` → consola colorida con `[%X{traceId}] [%X{tenantSlug}] [%X{userId}]`. Perfil `!local` → `LogstashEncoder` JSON con todos los campos MDC y `customFields.app`.
- **`ApplicationConfig.java`:** `FilterRegistrationBean<RequestTracingFilter>` con `Ordered.HIGHEST_PRECEDENCE` y `/*`. Imports `FilterRegistrationBean`, `Ordered` agregados.
- **`keygo-run/pom.xml`:** `net.logstash.logback:logstash-logback-encoder:8.0` agregado.
- **`application.yml`:** `management.metrics.web.server.request.autotime.enabled=true` activado.
- **Tests:** `RequestTracingFilterTest` (9 casos): generación de UUID, reutilización de `X-Request-ID`, limpieza MDC en éxito y excepción, llamada a filterChain, propagación MDC/method/path.
- **Docs:** plan movido de `plan-tracingTelemetryLogging.prompt.md` → `docs/design/TRACING_TELEMETRY.md`.

### [2026-03-31] Reorganización de /docs — compactación y limpieza

- `docs/ai/lecciones.md`: 1.745 → 514 líneas. Nuevo formato compacto: **Síntoma / Causa / Solución**.
- `docs/ai/agents-registro.md`: 781 → 129 líneas. Entradas pre-2026-03-25 archivadas en `agents-registro-historico.md`.
- `docs/plan_reestructuracion.md`: eliminado (ya ejecutado).
- `docs/research/` → `docs/archive/research/` (5 archivos de investigación históricos).
- `AI_CONTEXT.md`: tabla de retroalimentación duplicada eliminada — apunta a `CLAUDE.md`.
- `ARCHITECTURE.md` (raíz): sección Comandos eliminada; seguridad corregida (Bearer, no X-KEYGO-ADMIN); migraciones actualizadas a V1–V18.
- `docs/README.md`: estructura actualizada con `archive/` y correcciones de versiones.

---

### [2026-03-31] Endpoints billing onboarding: resume + resend-verification

- **Dominio:** `AppContract` — `isVerificationCodeExpired()`, `renewVerificationCode()`
- **App:** `ResumeContractOnboardingUseCase`, `ResendContractVerificationUseCase`
- **API:** `AppContractResumeData` (DTO), 2 nuevos métodos en `AppBillingContractController`, 2 nuevos `ResponseCode`
- **Run:** 2 nuevos `@Bean` en `ApplicationConfig`
- **URLs nuevas:**
  - `GET /keygo-server/api/v1/billing/contracts/{contractId}/resume` — Público
  - `POST /keygo-server/api/v1/billing/contracts/{contractId}/resend-verification` — Público
- **Nota:** Rutas cubiertas por `hasSegment("/billing/contracts")` en filtro. Sin cambios en filtro ni `application.yml`.

---

### [2026-03-30] Reestructuración Flyway — Modelo v2 Contractors integrado desde V1

- Backup de V1–V17 en `backup_20260330/`
- Nuevas migraciones: V11 (contractors), V12 (billing_contracts v2), V13 (billing_subscriptions), V14 (billing_invoices_and_usage), V15 (billing_support_tables), V16 (seed_foundation), V17 (seed_billing_plans), V18 (seed_contractors)
- Cambio de modelo: `subscriber_tenant_id`/`subscriber_tenant_user_id` → `contractor_id` en contratos, suscripciones y usage
- **Próxima migración:** `V19__...`
- Credenciales seed: `contractor@keygo.local` / `Admin1234!`

---

### [2026-03-29] Escalera de planes de billing v2 (USD)

- `V17__seed_keygo_billing_plans_v2.sql`: versiones v2.0 en USD (FREE/BUSINESS/ENTERPRISE) + v1.0 nuevos (PERSONAL/TEAM/FLEX)
- Escalera: FREE $0 | PERSONAL $5 | TEAM $49 | BUSINESS $149 | FLEX pay-per-use | ENTERPRISE custom/año
- ENTERPRISE: `billing_period = YEARLY`, `base_price = 0`

---

### [2026-03-29] Reestructuración Flyway V1–V26 → V1–V17

- Eliminadas V11–V26 (parches y seeds fragmentados). Una migración por dominio.
- V11: billing_contracts, V12: billing_subscriptions, V13: invoices+usage, V14: billing_support_tables
- V15: seed foundation, V16: seed billing_platform_app, V16/V17: seed billing_plans
- `docs/data/MIGRATIONS.md` actualizado. **Próxima (en ese momento):** `V18__...`

---

### [2026-03-28] Endpoint `GET /api/v1/admin/platform/dashboard` + refactorización GROUP BY

- **App:** `PlatformDashboardPort` (9 métodos `Map<K,Long>`), `GetPlatformDashboardUseCase`, `PlatformDashboardResult`
- **Supabase:** `PlatformDashboardAdapter` con helpers `toCountMap()`/`toStringCountMap()`
- **API:** `PlatformDashboardController` (`@PreAuthorize("hasRole('ADMIN')")`), `PlatformDashboardData` (12 sub-DTOs)
- **Reducción:** ~25 queries independientes → ~9 queries GROUP BY por petición
- **URL:** `GET /keygo-server/api/v1/admin/platform/dashboard` — requiere Bearer ADMIN

---

### [2026-03-28] Corrección de documentación: endpoint `GET /api/v1/tenants`

- `ROADMAP.md`: estado actualizado (endpoints 26→27, tests 338+→527+, Postman 40→42)
- `FRONTEND_DEVELOPER_GUIDE.md` §8.2: marcado ✅, `nameLike`→`name_like` corregido
- `AGENTS.md`: endpoint `GET /api/v1/tenants` agregado a lista de URLs

---

### [2026-03-27] Endpoint `GET /api/v1/tenants` — listado paginado

- **App:** `TenantFilter`, `PagedResult<T>`, `ListTenantsUseCase`, nuevo método en `TenantRepositoryPort`
- **Supabase:** `TenantJpaRepository` con `JpaSpecificationExecutor`, `findAll()` con Specification ILIKE
- **API:** `PagedData<T>`, nuevo `@GetMapping listTenants()`, `ResponseCode.TENANT_LIST_RETRIEVED`
- **URL:** `GET /keygo-server/api/v1/tenants?status=ACTIVE&name_like=...&page=0&size=20` — Bearer ADMIN

---

### [2026-03-27] Reorganización: `scripts/` → `docs/scripts/`, `postman/` → `docs/postman/`

- `scripts/` renombrado a `docs/scripts/`; `postman/` renombrado a `docs/postman/`
- `PROJECT_ROOT` corregido en `keygo.sh`, `switch-env.sh`, `check-ai-docs.sh`, `quick-start.sh`
- `data-local.sql`: `ON CONFLICT DO NOTHING` → `INSERT ... WHERE NOT EXISTS` (H2 compatible)
- `AGENTS.md`, `CLAUDE.md`, `AI_CONTEXT.md`, `copilot-instructions.md`: todas las referencias actualizadas

---

### [2026-03-26] Refinación: `envs/` a raíz del proyecto, `.env` activo en raíz

- `scripts/envs/` → `envs/` (raíz del proyecto)
- `.env` activo: se copia a `$PROJECT_ROOT/.env` (antes en `keygo-supabase/.env`)
- `_load-env.sh`: apunta a `$PROJECT_ROOT/.env`
- `keygo-supabase/scripts/`: vaciada

---

### [2026-03-26] Script `keygo.sh` — menú principal + centralización `scripts/db/`

- Nuevo `docs/scripts/keygo.sh`: 20 opciones en 5 categorías (Ambiente / BD / App / Tests / Setup)
- Stubs de compatibilidad en `keygo-supabase/scripts/` delegan con `exec` a `docs/scripts/db/`
- `AGENTS.md` sección "Essential commands" actualizada con rutas centralizadas

---

### [2026-03-26] CORS habilitado en SecurityFilterChain

- Nuevo: `KeyGoCorsProperties` (`keygo.cors.*`), `CorsConfigurationSource` en `SecurityConfig`
- `application.yml`: `keygo.cors.allowed-origins: http://localhost:5173`
- Spring Security aplica CORS antes del `BootstrapAdminKeyFilter`; preflight OPTIONS resuelto

---

### [2026-03-25] Seguridad admin Bearer-only + RBAC por endpoint

- `BootstrapAdminKeyFilter`: autenticación solo por `Authorization: Bearer` (eliminado `X-KEYGO-ADMIN`)
- `@PreAuthorize` en todos los controllers admin; `TenantAuthorizationEvaluator` para aislamiento por tenant
- `OpenApiConfig`: esquema migrado a `BearerAuth`
- Claim `tenant_slug` emitido en access tokens (auth code, refresh, client_credentials)

---

### [2026-04-02] RFC Account & Settings — Phase 6 documentation

- Added 7 new endpoints to "REST API — Account Settings" section:
  - `POST /account/change-password`, `GET /account/sessions`, `DELETE /account/sessions/{sessionId}`,
    `GET /account/notification-preferences`, `PATCH /account/notification-preferences`, `GET /account/access`
- Added V19 (billing seeds), V20 (role hierarchy), V21 (user_notification_preferences) migration descriptions
- Updated "Próxima migración" from V19 → V22
- Added 4 bootstrap property rows: `accountChangePasswordPathSuffix`, `accountSessionsPathSuffix`, `accountNotificationPreferencesPathSuffix`, `accountAccessPathSuffix`

---

### [2026-04-08] RFC billing-contractor-refactor — Fases F/G/H (Platform Billing)

- **Fase F (Seguridad):** `BootstrapAdminKeyFilter` — cambio `hasSuffix` → `hasSegment` para `billingCatalogPathSuffix` (cubre sub-paths `/catalog/{planCode}` tanto en app como plataforma); test convertido a `@ParameterizedTest` con 4 paths
- **Fase G (Tests):** 24 tests nuevos:
  - 5 use case tests: `GetPlatformPlanCatalogUseCaseTest` (2), `GetPlatformPlanUseCaseTest` (2), `GetPlatformSubscriptionUseCaseTest` (3), `CancelPlatformSubscriptionUseCaseTest` (4), `ListPlatformInvoicesUseCaseTest` (4)
  - 1 controller test: `PlatformBillingControllerTest` (9 tests en 5 `@Nested` groups)
- **Fase H (Documentación):** `AGENTS.md` endpoints de plataforma billing, `FRONTEND_DEVELOPER_GUIDE.md` §14.3.1 con tabla de 5 endpoints nuevos, `lecciones.md` con 2 entradas (hasSuffix bug, AppSubscription builder), `propuestas.md` actualizado, Postman collection con 5 requests
- **Endpoints nuevos documentados:**
  - `GET /api/v1/platform/billing/catalog` (público)
  - `GET /api/v1/platform/billing/catalog/{planCode}` (público)
  - `GET /api/v1/platform/billing/subscription` (Bearer KEYGO_ADMIN/KEYGO_TENANT_ADMIN)
  - `POST /api/v1/platform/billing/subscription/cancel` (Bearer KEYGO_ADMIN/KEYGO_TENANT_ADMIN)
  - `GET /api/v1/platform/billing/invoices` (Bearer KEYGO_ADMIN/KEYGO_TENANT_ADMIN)
- Suite completa: 636 tests, 0 fallos

### [2026-04-09] T-125 — Membership.PENDING + flujo aprobación + email notificación
- `Membership.approve()` domain method: PENDING→ACTIVE, lanza `MembershipAlreadyActiveException` (ACTIVE) o `MembershipAlreadySuspendedException` (SUSPENDED)
- `CreateMembershipUseCase` ahora crea membresías en estado `PENDING` (antes `ACTIVE`)
- Nuevo `ApproveMembershipUseCase`: valida tenant scope, aprueba, envía email notificación
- Nuevo endpoint `PUT /api/v1/tenants/{slug}/apps/{clientId}/memberships/{membershipId}/approve`
- Nuevo `ResponseCode.MEMBERSHIP_APPROVED`
- Email notificación: `MembershipApprovedStrategy` + template `membership-approved.html` + convenience method `sendMembershipApprovedEmail()`
- `GlobalExceptionHandler`: handler `MembershipAlreadyActiveException` → 409 Conflict
- 7 tests `ApproveMembershipUseCaseTest` (incl. email sent, email failure no bloquea approve)
- 5 tests `CreateMembershipUseCaseTest`
- Suite completa: 952 tests, 0 fallos

### [2026-04-07] Identidad de plataforma — endpoints de cuenta, bugs críticos, validación

**Nuevos endpoints (`PlatformAccountController`):**
- `POST /api/v1/platform/account/forgot-password` — solicitar recovery token (anti-enumeración: siempre 200)
- `POST /api/v1/platform/account/recover-password` — restablecer contraseña con recovery token
- `POST /api/v1/platform/account/reset-password` — reset con contraseña temporal + verification code + request_id
- `POST /api/v1/platform/oauth2/revoke` — revocar token RFC 7009 (reusa `RevokeTokenUseCase`)

**Nuevos use cases de plataforma:**
- `ForgotPlatformPasswordUseCase` — busca en `PlatformUserRepositoryPort`, envía email, anti-enumeración
- `RecoverPlatformPasswordUseCase` — valida token, resetea contraseña, activa si PENDING
- `ResetPlatformPasswordUseCase` — valida requestId + verification code + temporary password

**Bugs críticos corregidos:**
- Hibernate UUID persistence: `id=null` para `persist()` vs `merge()` → `ObjectOptimisticLockingFailureException`
- JSONB mapping: `@JdbcTypeCode(SqlTypes.JSON)` requerido además de `@Column(columnDefinition = "jsonb")` en Hibernate 6+
- `verification_codes` FK constraint: V32 agrega `platform_user_id` nullable FK + CHECK dual-user
- `DataAccessException` SQL leak: nuevo handler dedicado en `GlobalExceptionHandler` que nunca expone SQL
- SB4 `UserDetailsServiceAutoConfiguration`: paquete movido a `org.springframework.boot.security.autoconfigure`

**Convenciones nuevas:**
- `@Valid` obligatorio en todos los `@RequestBody`
- Anotaciones de validación (`@NotBlank`, `@NotNull`, `@Email`) en todos los Request DTOs
- `@NotBlank` para strings (más restrictivo que `@NotNull` o `@NotEmpty`)

**Migraciones V27–V32:**
- V27: Tabla `platform_users`
- V28: Refactor `sessions` para platform users
- V29: Seed platform_users + rename role
- V30: Billing contractor → PlatformUser
- V31: Tabla unificada `verification_codes`
- V32: `platform_user_id` en `verification_codes`

**Bootstrap filter:** 8 nuevos path prefixes públicos para plataforma en `BootstrapAdminKeyFilter`
**Tests:** 14 tests nuevos (3 test files: ForgotPlatformPasswordUseCaseTest, RecoverPlatformPasswordUseCaseTest, ResetPlatformPasswordUseCaseTest)
**Próxima migración:** `V33__...`
