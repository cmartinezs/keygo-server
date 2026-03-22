# AI Context — Lecciones Aprendidas

> Sub-documento de [`AI_CONTEXT.md`](AI_CONTEXT.md).
>
> Registra **errores encontrados, buenas prácticas descubiertas y convenciones adoptadas** durante
> el trabajo del agente en el repositorio. Consultar antes de implementar para no repetir errores pasados.
>
> **⚠️ Regla de actualización:** Al concluir cualquier tarea donde ocurra un error, bug, comportamiento
> inesperado o mejor patrón, agregar una entrada aquí **antes de cerrar la tarea**.

---

## Formato de entrada

```markdown
### [YYYY-MM-DD] Título descriptivo de la lección
**Contexto:** Breve descripción de la tarea o escenario que generó el aprendizaje.
**Problema:** Qué falló, qué comportamiento inesperado se detectó o qué patrón mejoró.
**Solución / Buena práctica:** Cómo se resolvió o qué debe hacerse en el futuro.
**Archivos clave:** (opcional) Rutas relevantes para contextualizar la solución.
```

---

## Índice de lecciones

| Fecha | Tema | Categoría |
|---|---|---|
| 2026-03-22 | [Corrección de inconsistencias: docs vs DB — criterio de decisión](#2026-03-22-corrección-de-inconsistencias-docs-vs-db--criterio-de-decisión) | Convenciones / DB |
| 2026-03-22 | [Docs de datos desincronizados con migraciones Flyway](#2026-03-22-documentación-de-datos-desincronizada-con-migraciones-flyway-reales) | Documentación |
| 2026-03-22 | [Value objects: acceso con records](#2026-03-22-value-objects-acceso-a-value-diferente-según-record-vs-clase-regular) | Java / Domain |
| 2026-03-22 | [Nimbus JOSE+JWT vía dependencia transitiva](#2026-03-22-nimbus-josejwt-en-spring-boot-4--dependencia-transitiva-vía-spring-security-oauth2-jose) | Maven / Deps |
| 2026-03-22 | [OIDC/JWKS retornar JSON nativo](#2026-03-22-endpoints-oidcjwks-deben-retornar-json-nativo-no-baseresponse) | API / OIDC |
| 2026-03-22 | [JwksBuilderPort fuera de keygo-api](#2026-03-22-jwksbuilderport--arquitectura-hexagonal-para-nimbus-fuera-de-keygo-api) | Arquitectura |
| 2026-03-22 | [OAuth2: estado entre /authorize y /login](#2026-03-22-oauth2-authorization-code-pasar-estado-entre-get-authorize-y-post-login-vía-http-session) | OAuth2 |
| 2026-03-21 | [JaCoCo en monorepo multi-módulo](#2026-03-21-configuración-de-jacoco-en-monorepo-maven-multi-módulo-con-spring-boot-4) | Maven / CI |
| 2026-03-21 | [Convenciones de coding Java](#2026-03-21-convenciones-de-coding-java-adoptadas-para-el-codebase) | Java / Style |
| 2026-03-21 | [jakarta.validation-api no transitivo](#2026-03-21-jakartavalidation-api-no-es-transitivo-en-keygo-api) | Maven / Deps |
| 2026-03-21 | [SpringDoc 3.0.1 con Spring Boot 4.x](#2026-03-21-springdoc-301-con-spring-boot-4x--integración-y-anotaciones-de-seguridad) | Spring / OpenAPI |
| 2026-03-21 | [Retroalimentación obligatoria de docs AI](#2026-03-17-retroalimentación-obligatoria-de-documentos-ai-tras-cada-tarea) | Proceso |
| 2026-03-21 | [Fase 0 cerrada: verificar sub-puntos](#2026-03-21-fase-0-cerrada-qué-faltaba-vs-qué-se-asumía-como-completo) | Proceso |
| 2026-03-21 | [Generación de colecciones Postman](#2026-03-21-generación-de-colecciones-postman-para-pruebas-funcionales-manuales) | Testing |
| 2026-03-21 | [Slug generado automáticamente](#2026-03-21-generación-automática-de-slug-a-partir-del-nombre-del-tenant) | Domain / API |
| 2026-03-21 | [Bug T-001 BootstrapAdminKeyFilter](#2026-03-21-bug-t-001--bootstrapadminkeyfilter-getrequesturi-vs-getservletpath-con-context-path) | Bug / Security |
| 2026-03-21 | [Reorganización de paquetes por feature](#2026-03-17-reorganización-de-paquetes-internos-por-feature-en-monorepo-multi-módulo) | Maven / Build |
| 2026-03-21 | [SupabaseJpaConfig basePackages](#2026-03-17-supabasejpaconfig-requiere-basepackages-ampliado-al-reorganizar-entidades-por-feature) | Spring / JPA |
| 2026-03-21 | [Script check-ai-docs.sh](#2026-03-17-script-de-verificación-de-actividad-del-agente-ai-extendido-a-agentsmd) | Tooling |
| 2026-03-21 | [Fase 2: import enum en use case](#2026-03-21-fase-2--clientapp-import-faltante-de-enum-en-use-case) | Java / Compiler |
| 2026-03-21 | [orphanRemoval en colecciones JPA](#2026-03-21-fase-2--patrón-de-orphanremoval-en-colecciones-jpa-de-entidades-hijo) | JPA / Hibernate |
| 2026-03-21 | [TenantEntity referencia no-managed](#2026-03-21-fase-2--tenantentity-como-referencia-no-managed-en-clientapprepositoryAdapter) | JPA |
| 2026-03-21 | [Mockito UnnecessaryStubbing con tryFindByEmail](#2026-03-21-fase-3--mockito-unnecessarystubbing-con-tryfindbyemail-que-captura-iae) | Testing |
| 2026-03-21 | [PasswordHash.toString() redactado](#2026-03-21-fase-3--passwordhashtostring-nunca-expone-el-hash-seguridad) | Security |
| 2026-03-21 | [Import correcto de BaseResponse](#2026-03-21-fase-4--import-correcto-de-baseresponse-en-controllers-rest) | Java / API |

---

## Lecciones

### [2026-03-22] Corrección de inconsistencias docs vs DB — criterio de decisión
**Contexto:** Re-auditoría de las 12 inconsistencias detectadas en `INCONSISTENCIAS.datos.md` y marcadas como "resueltas". El usuario solicitó revisar si las correcciones fueron suficientes o si la DB debía ajustarse también.
**Problema:** La corrección anterior actualizó los documentos para reflejar lo que había en la DB (tablas en singular: `app_role`, `membership`, `membership_role`). Pero la convención estándar PostgreSQL exige nombres en plural, y la documentación original sí los tenía en plural. Al corregir solo los docs, se perpetuó una inconsistencia real en el schema.
**Solución / Buena práctica:**  
Al revisar una inconsistencia entre docs y código/DB, aplicar este criterio:
1. **La documentación manda en convenciones de nomenclatura** (singular/plural, casing, patrones de nombres). Si la doc dice plural, la DB debe ser plural → crear migración.
2. **La implementación manda cuando hay razón técnica clara** (normalización, columnas redundantes, estándares RFC, seguridad). Si la implementación omitió `tenant_id` en `membership` porque sería redundante, es la implementación la correcta.
3. **Ambos pueden tener razón parcial** → aplicar el criterio de menor impacto y mayor consistencia con el sistema.
4. **Corregir la documentación** para reflejar el criterio aplicado (no simplemente para aceptar la implementación si esta está mal).
5. **Nunca marcar como "corregido" una inconsistencia donde solo se ajustó la documentación para aceptar algo que viola convenciones**. Agregar una nota de "pendiente de migración" en ese caso.

**Archivos clave:** `INCONSISTENCIAS.datos.md`, `V10__rename_membership_tables_to_plural.sql`, `AppRoleEntity.java`, `MembershipEntity.java`

---

### [2026-03-22] Documentación de datos desincronizada con migraciones Flyway reales
**Contexto:** Actualización explícita de `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md`, `DATA_DICTIONARY.md` y `AUTH_FLOW.md`. Se leyeron las migraciones SQL reales (V1–V9) y se compararon con los documentos existentes.
**Problema:** Múltiples discrepancias críticas. Ver detalle completo en [`INCONSISTENCIAS.datos.md`](INCONSISTENCIAS.datos.md).
**Solución / Buena práctica:** Al generar documentación de datos, **siempre leer las migraciones SQL reales** antes de escribir el diccionario. No asumir columnas ni tipos — verificar cada campo en `V{n}__*.sql`. **Regla obligatoria:** al crear cualquier migración Flyway nueva, actualizar `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md` y `DATA_DICTIONARY.md` antes de cerrar la tarea.
**Archivos clave:** `docs/keygo-server/DATA_MODEL.md`, `INCONSISTENCIAS.datos.md`, `keygo-supabase/src/main/resources/db/migration/V1–V9`

---

### [2026-03-22] Value objects: acceso a `.value()` diferente según record vs clase regular
**Contexto:** Fase 5 — modelos de dominio para OAuth2 (`AuthorizationCode`, `CodeChallenge`, `ScopeSet`).
**Problema:** Records Java exponen cada parámetro de constructor como método público automáticamente:
- `record AuthorizationCodeId(UUID id)` → acceso `.id()` (no `.getValue()`)
- `record ClientId(String value)` → acceso `.value()`
- `record UserId(UUID value)` → acceso `.value()`
**Solución / Buena práctica:** Todos los value objects del proyecto son records. Nombrar el parámetro de forma descriptiva (`id`, `value`, `email`…). No usar `.getValue()` — usar el nombre exacto del parámetro.
**Archivos clave:** `keygo-domain/.../model/*Id.java`, `AuthorizationCodeId.java`

---

### [2026-03-22] Nimbus JOSE+JWT en Spring Boot 4 — dependencia transitiva vía spring-security-oauth2-jose
**Contexto:** Fase 6 — implementación de firma JWT con RSA en `keygo-infra`.
**Problema:** `com.nimbusds:nimbus-jose-jwt` directo en `keygo-infra/pom.xml` sin versión → Maven falla: "version is missing". Spring Boot 4.x no expone Nimbus en su BOM directamente.
**Solución / Buena práctica:** Usar `org.springframework.security:spring-security-oauth2-jose` (sin versión) como dependencia. Nimbus llega transitivamente con versión compatible garantizada.
**Archivos clave:** `keygo-infra/pom.xml`

---

### [2026-03-22] Endpoints OIDC/JWKS deben retornar JSON nativo, no BaseResponse
**Contexto:** Fase 6 — `JwksController` y `OidcMetadataController`.
**Problema:** Las librerías OAuth2 (Spring Security Resource Server, Auth0 SDK, etc.) consumen `/.well-known/jwks.json` y esperan JSON nativo RFC 7517 — sin envelope `BaseResponse`.
**Solución / Buena práctica:** Controllers OIDC/JWKS retornan `ResponseEntity<Map<String, Object>>`. El resto sigue con `BaseResponse<T>`. Paths `/.well-known` configurados como públicos en `BootstrapAdminKeyFilter`.
**Archivos clave:** `keygo-api/.../JwksController.java`, `keygo-api/.../OidcMetadataController.java`

---

### [2026-03-22] JwksBuilderPort — arquitectura hexagonal para Nimbus fuera de keygo-api
**Contexto:** Fase 6 — `JwkSetBuilder` (Nimbus) no puede ir en `keygo-api` porque `keygo-api` no depende de `keygo-infra`.
**Problema:** Importar implementación Nimbus desde `keygo-api` viola hexagonal — el controller no debe conocer implementaciones concretas.
**Solución / Buena práctica:** Definir `JwksBuilderPort` en `keygo-app` (interface). `JwkSetBuilder` en `keygo-infra` implementa el puerto. El wiring en `ApplicationConfig` (`keygo-run`).
**Archivos clave:** `keygo-app/.../JwksBuilderPort.java`, `keygo-infra/.../JwkSetBuilder.java`

---

### [2026-03-22] OAuth2 Authorization Code: pasar estado entre GET /authorize y POST /login vía HTTP Session
**Contexto:** Fase 5 — flujo OAuth2, corrección de `/account/login` para emitir el authorization code.
**Problema:** El estado del `GET /authorize` (clientId, redirectUri, scope, codeChallenge) debe estar disponible cuando llega el `POST /login`. Sin HTTP Session, cada request llega sin contexto.
**Solución / Buena práctica:**
1. `GET /authorize` guarda `AuthorizationSessionState` en `HttpSession` (`JSESSIONID` cookie).
2. `POST /login` (misma sesión) recupera el estado, autentica usuario y emite authorization code.
3. El cliente **debe** enviar cookies entre los dos pasos (`credentials: 'include'` en fetch / `CookieJar` en OkHttp).
**Archivos clave:** `AuthorizationController.java`, `AuthorizationSessionState.java`, `LoginData.java`

---

### [2026-03-21] SpringDoc 3.0.1 con Spring Boot 4.x — integración y anotaciones de seguridad
**Contexto:** Integración de Swagger/OpenAPI.
**Problema:** (1) `@SecurityRequirementsOptional` no existe en `swagger-annotations-jakarta`. (2) SpringDoc 2.x es para Spring Boot 3.x; se necesita 3.x para Spring Boot 4.x.
**Solución / Buena práctica:** Usar `springdoc-openapi-starter-webmvc-ui:3.0.1`. Para endpoints públicos, no usar anotación de seguridad (no SecurityRequirement global). Para protegidos: `@SecurityRequirement(name = "AdminKeyAuth")` a nivel de clase/método.
**Archivos clave:** `keygo-run/config/OpenApiConfig.java`, `keygo-api/pom.xml`, `application.yml`

---

### [2026-03-21] Configuración de JaCoCo en monorepo Maven multi-módulo con Spring Boot 4
**Contexto:** Propuesta T-016 — JaCoCo con umbral mínimo y reporte consolidado.
**Problema:** (1) Módulos sin código fallan el check si no se excluyen. (2) `keygo-bom` (packaging `pom`) ejecuta JaCoCo sin código. (3) `report-aggregate` requiere que el módulo ejecutor tenga todos los módulos como dependencias.
**Solución / Buena práctica:**
- `prepare-agent` + `report` + `check` en `pluginManagement` del POM raíz.
- Módulos stub vacíos: `<jacoco.skip>true</jacoco.skip>`.
- `report-aggregate` solo en `keygo-run`.
- `<jacoco.minimum.coverage>` como propiedad parametrizable.
- En CI: `./mvnw verify` (no `test`) para ejecutar fases de JaCoCo.
- Versión recomendada: `0.8.12`.
**Archivos clave:** `pom.xml` (raíz), `keygo-run/pom.xml`, `.github/workflows/ci.yml`

---

### [2026-03-21] Convenciones de coding Java adoptadas para el codebase
**Contexto:** Revisión masiva de estilo en todos los módulos activos.
**Problema:** (1) Líneas en blanco en JavaDoc. (2) `/** */` en campos en vez de `/* */`. (3) `@Data` en entidades JPA. (4) Literales string duplicados en tests. (5) `@Override toString()` sin `@NotNull`.
**Solución / Buena práctica:**
- JavaDoc: usar `<p>` para separar párrafos; no líneas en blanco antes de `@param`/`@return`.
- Campos: `/* */`, nunca `/** */`.
- Entidades JPA: `@Getter @Setter` (no `@Data`). Evitar `equals`/`hashCode` sobre colecciones lazy.
- Tests: constantes `private static final` para literales repetidos.
- `toString()` override: `@SuppressWarnings("NullableProblems")`.
- Domain: Lombok `provided`.
**Archivos clave:** `keygo-domain/pom.xml`, `Tenant.java`, `TenantEntity.java`, `UserEntity.java`

---

### [2026-03-21] jakarta.validation-api no es transitivo en keygo-api
**Contexto:** Fase 1 — DTOs con `@NotBlank`, `@Size`, `@Valid` en `keygo-api`.
**Problema:** Build falla con "package jakarta.validation.constraints does not exist". `spring-boot-starter-web` no expone `jakarta.validation-api` transitivamente en Spring Boot 4.
**Solución / Buena práctica:** Agregar `jakarta.validation-api` explícitamente en `keygo-api/pom.xml`. Aplica a cualquier módulo que declare DTOs con anotaciones de validación.
**Archivos clave:** `keygo-api/pom.xml`, `CreateTenantRequest.java`

---

### [2026-03-21] Fase 0 cerrada: qué faltaba vs. qué se asumía como completo
**Contexto:** Verificación del estado real de Fase 0 del plan de implementación.
**Problema:** La reorganización de paquetes (0.2) se marcó como Fase 0 completa, pero faltaban: (a) pipeline CI; (b) enforcement lint; (c) convenciones de código documentadas.
**Solución / Buena práctica:** Al marcar una fase como completa, verificar **cada sub-punto** de la lista. No marcar ✅ solo por el trabajo más visible.
**Archivos clave:** `.github/workflows/ci.yml`, `pom.xml` (Maven Enforcer), `docs/keygo-server/CODE_STYLE.md`

---

### [2026-03-21] Generación de colecciones Postman para pruebas funcionales manuales
**Contexto:** El proyecto carecía de colecciones Postman importables.
**Problema:** Sin colecciones estándar, cada prueba requería configuración manual.
**Solución / Buena práctica:** Crear archivos bajo `postman/` (schema v2.1.0). Puntos clave: (1) auth `apikey` a nivel de colección heredada; (2) `{{fullBaseUrl}}` compuesto por pre-request script global; (3) slug único con timestamp en pre-request de Create Tenant; (4) guardar `tenantSlug` en entorno post-request; (5) endpoints públicos overridean auth a `noauth`.
**Archivos clave:** `postman/KeyGo-Server.postman_collection.json`, `postman/KeyGo-Server-Local.postman_environment.json`

---

### [2026-03-21] Generación automática de slug a partir del nombre del tenant
**Contexto:** `slug` era requerido en request body — se migró a generación automática.
**Problema:** Slug editable en API → validaciones redundantes + riesgo de slugs inconsistentes.
**Solución / Buena práctica:** `SlugUtils.toSlug(String)` en `keygo-domain/shared/util/` (Java puro). `TenantSlug.fromName(String)` genera y valida. Postman: timestamp en `name`, no en `slug`.
**Archivos clave:** `SlugUtils.java`, `TenantSlug.java`, `CreateTenantCommand.java`, `CreateTenantUseCase.java`

---

### [2026-03-21] Bug T-001 — BootstrapAdminKeyFilter: getRequestURI() vs. getServletPath() con context-path
**Contexto:** Corrección del bug donde el filtro de seguridad nunca bloqueaba rutas de API.
**Problema:** `getRequestURI()` incluye el context-path (`/keygo-server/api/...`) → nunca coincide con prefijos configurados (`/api/`).
**Solución / Buena práctica:** Usar `request.getServletPath()` — retorna ruta relativa al context-path (`/api/v1/...`). En tests: usar `setServletPath()` (no `setRequestURI()`). Agregar 2 tests de regresión con `setContextPath` + `setServletPath`.
**Archivos clave:** `BootstrapAdminKeyFilter.java`, `BootstrapAdminKeyFilterTest.java`

---

### [2026-03-17] Retroalimentación obligatoria de documentos AI tras cada tarea
**Contexto:** Revisión de documentos de guía para agentes.
**Problema:** `AGENTS.md` no estaba en la lista de lectura obligatoria. No había instrucciones explícitas sobre cuándo actualizar estos docs.
**Solución / Buena práctica:** Los docs AI (`AI_CONTEXT.md`, `AGENTS.md`) son base de conocimiento — actualizar siempre al concluir tarea donde haya error resuelto, mejor patrón, cambio tecnológico o nueva convención.
**Archivos clave:** `AI_CONTEXT.md`, `AGENTS.md`, `CLAUDE.md`, `.github/copilot-instructions.md`

---

### [2026-03-17] Reorganización de paquetes internos por feature en monorepo multi-módulo
**Contexto:** Reorganización de paquetes de organización técnica a organización por feature.
**Problema:** `./mvnw -pl keygo-run test` usaba JARs viejos → errores "cannot find symbol".
**Solución / Buena práctica:** Al reorganizar paquetes en módulos dependidos, ejecutar primero `./mvnw -pl <modulos> install -DskipTests`. Solo el build completo (`./mvnw clean package`) garantiza orden correcto.
**Archivos clave:** `keygo-app/platform/port`, `keygo-api/shared`, `keygo-supabase/config/SupabaseJpaConfig.java`

---

### [2026-03-17] SupabaseJpaConfig requiere basePackages ampliado al reorganizar entidades por feature
**Contexto:** Reorganización de entidades JPA a sub-paquetes por feature.
**Problema:** `@EntityScan` y `@EnableJpaRepositories` con rutas exactas fallan al reorganizar.
**Solución / Buena práctica:** Usar el paquete raíz: `"io.cmartinezs.keygo.supabase"`. Spring Data escanea recursivamente. Cambiar en el mismo commit que la reorganización.
**Archivos clave:** `SupabaseJpaConfig.java`

---

### [2026-03-17] Script de verificación de actividad del agente AI (extendido a AGENTS.md)
**Contexto:** Extensión de `scripts/check-ai-docs.sh`.
**Problema:** Solo verificaba `AI_CONTEXT.md`. `AGENTS.md` podía quedar desactualizado sin detectarse.
**Solución / Buena práctica:** Función reutilizable `check_section(FILE, SECTION_LABEL)` con arrays globales. Compatibilidad GNU/BSD date. Exit code = peor de los dos docs.
**Archivos clave:** `scripts/check-ai-docs.sh`

---

### [2026-03-21] Fase 2 — ClientApp: import faltante de enum en use case
**Contexto:** `CreateClientAppUseCase` referencia `ClientType.CONFIDENTIAL`.
**Problema:** Compilador: "package ClientType does not exist" — enum no importado.
**Solución / Buena práctica:** Al agregar comparaciones de enum en un use case, verificar siempre que el enum esté en imports. `./mvnw test --also-make -pl keygo-app` detecta rápido.
**Archivos clave:** `CreateClientAppUseCase.java`

---

### [2026-03-21] Fase 2 — Patrón de orphanRemoval en colecciones JPA de entidades hijo
**Contexto:** `ClientAppEntity` con `@OneToMany(cascade=ALL, orphanRemoval=true)`.
**Problema:** Reasignar referencia de lista (`entity.setRedirectUris(newList)`) → `orphanRemoval` no elimina hijos — Hibernate pierde tracking de colección original.
**Solución / Buena práctica:** Usar `entity.getRedirectUris().clear()` + `addAll(nuevaLista)`. Nunca reasignar la referencia de la colección.
**Archivos clave:** `ClientAppPersistenceMapper.java`, `ClientAppEntity.java`

---

### [2026-03-21] Fase 2 — TenantEntity como referencia no-managed en ClientAppRepositoryAdapter
**Contexto:** Guardar `ClientApp` con FK `tenant_id` sin cargar el tenant de DB.
**Problema:** JPA requiere referencia de entidad para `@ManyToOne`; cargar el tenant completo sería ineficiente.
**Solución / Buena práctica:** Crear instancia de `TenantEntity` con solo `id` seteado — Hibernate lo trata como proxy. Alternativa idiomática: `entityManager.getReference(TenantEntity.class, id)`.
**Archivos clave:** `ClientAppRepositoryAdapter.java`

---

### [2026-03-21] Fase 3 — Mockito UnnecessaryStubbing con tryFindByEmail que captura IAE
**Contexto:** Tests de `ValidateUserCredentialsUseCase`.
**Problema:** Stubear `findByTenantIdAndEmail` para credencial tipo username ("johndoe") → `UnnecessaryStubbing` porque `tryFindByEmail()` captura `IllegalArgumentException` antes de llamar al mock.
**Solución / Buena práctica:** Al testear username: no stubear el mock de email. Al testear "user not found": usar email válido (`"nobody@acme.com"`) para que el mock SÍ se invoque.
**Archivos clave:** `ValidateUserCredentialsUseCase.java`

---

### [2026-03-21] Fase 3 — PasswordHash.toString() nunca expone el hash (seguridad)
**Contexto:** Value object `PasswordHash` wrappea hash BCrypt.
**Problema:** `toString()` que retorna el hash expondría credenciales en logs.
**Solución / Buena práctica:** `PasswordHash.toString()` siempre retorna `"PasswordHash[REDACTED]"`. El hash real solo con `.value()` en los lugares necesarios.
**Archivos clave:** `PasswordHash.java`

---

### [2026-03-21] Fase 4 — Import correcto de BaseResponse en controllers REST
**Contexto:** `TenantMembershipController` y `TenantAppRoleController`.
**Problema:** Error "cannot find symbol: class BaseResponse" — se intentó importar de `shared.BaseResponse` (incorrecto).
**Solución / Buena práctica:** Siempre importar de `io.cmartinezs.keygo.api.shared.response.BaseResponse` (subpaquete `.response`). Los IDEs pueden auto-completar incorrectamente.
**Archivos clave:** `keygo-api/shared/response/BaseResponse.java`

---

### [2026-03-22] Documentación de modelo de datos y diccionario — estructura, flujos y relaciones
**Contexto:** Generación de documentación completa del modelo de datos bajo orden explícita.
**Problema:** Sin doc centralizada, los devs debían navegar migraciones Flyway + entidades JPA + archivos de dominio por separado.
**Solución / Buena práctica:** Crear 3 documentos complementarios en `docs/keygo-server/`: `DATA_MODEL.md` (diccionario), `ENTITY_RELATIONSHIPS.md` (flujos), `DATA_DICTIONARY.md` (índice). Usar Mermaid para todos los diagramas. Referencias cruzadas entre los 3.
**Archivos clave:** `docs/keygo-server/DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md`, `DATA_DICTIONARY.md`

---

**Última actualización:** 2026-03-22 | **Responsable:** AI Agent

