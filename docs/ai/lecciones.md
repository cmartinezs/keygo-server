# AI Context — Lecciones Aprendidas

> Sub-documento de [`AI_CONTEXT.md`](../../AI_CONTEXT.md).
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
| 2026-03-26 | [El agente nunca debe ejecutar comandos `git` directamente — usar listas de comandos sugeridos](#2026-03-26-el-agente-nunca-debe-ejecutar-comandos-git-directamente) | Agente / Reglas de trabajo |
| 2026-03-26 | [Verificar estado de compilación pre-existente con `./mvnw clean` antes de atribuir errores a cambios propios](#2026-03-26-verificar-estado-de-compilación-pre-existente-con-mvnw-clean) | Build / Testing |
| 2026-03-26 | [Documentar Swagger: 5 controllers de auth/OIDC sin anotaciones `@Tag`/`@Operation` y grupos desactualizados](#2026-03-26-documentar-swagger-controllers-sin-anotaciones-y-grupos-desactualizados) | API / Swagger / Docs |
| 2026-03-26 | [Contraseñas de seed SQL deben documentarse siempre junto al hash BCrypt](#2026-03-26-contraseñas-de-seed-sql-deben-documentarse-junto-al-hash-bcrypt) | DB / Seed / Convenciones |
| 2026-03-26 | [ADR-001: documentar decisiones de error handling como ADR (`docs/keygo-ui/ADR-001-error-handling-oauth2.md`)](#2026-03-26-adr-001-documentar-decisiones-de-error-handling-como-adr) | API / Error Handling / Docs |
| 2026-03-26 | [Subclasificar `CLIENT_REQUEST` en `CLIENT_TECHNICAL` vs `USER_INPUT` mejora triage de UI y soporte](#2026-03-26-subclasificar-client_request-en-client_technical-vs-user_input-mejora-triage-de-ui-y-soporte) | API / Error Handling |
| 2026-03-26 | [En Spring Framework 7, `HttpMessageNotReadableException` en tests requiere `HttpInputMessage`](#2026-03-26-en-spring-framework-7-httpmessagenotreadableexception-en-tests-requiere-httpinputmessage) | Testing / Spring |
| 2026-03-26 | [Clasificar errores por origen (`CLIENT_REQUEST`, `BUSINESS_RULE`, `SERVER_PROCESSING`) mejora diagnóstico frontend y soporte](#2026-03-26-clasificar-errores-por-origen-client_request-business_rule-server_processing-mejora-diagnóstico-frontend-y-soporte) | API / Error Handling |
| 2026-03-26 | [Errores API con `BaseResponse.data`: detalle tecnico en `local/dev`, mensaje amigable en otros perfiles](#2026-03-26-errores-api-con-baseresponsedata-detalle-tecnico-en-localdev-mensaje-amigable-en-otros-perfiles) | API / Error Handling / Testing |
| 2026-03-26 | [CORS en Spring Boot 4: `CorsConfigurationSource` + `http.cors(...)` en `SecurityFilterChain`](#2026-03-26-cors-en-spring-boot-4-configurar-corsconfigurationsource--httpcors-en-securityfilterchain) | Security / CORS |
| 2026-03-26 | [ClientApp CONFIDENTIAL en tests de adapter/mapper debe incluir `hashedSecret`](#2026-03-26-clientapp-confidential-en-tests-de-adaptermapper-debe-incluir-hashedsecret) | Testing / Dominio |
| 2026-03-26 | [Missing query param en MockMvc standalone: sin handler específico cae en 500 (`OPERATION_FAILED`)](#2026-03-26-missing-query-param-en-mockmvc-standalone-sin-handler-específico-cae-en-500-operation_failed) | Testing / Error Handling |
| 2026-03-26 | [OAuth2 authorize: para query params en snake_case usar `@RequestParam(name = ...)` en lugar de depender de `@ModelAttribute`](#2026-03-26-oauth2-authorize-para-query-params-en-snake_case-usar-requestparamname---en-lugar-de-depender-de-modelattribute) | API / Spring MVC / OAuth2 |
| 2026-03-26 | [Vitest en ejemplos aislados: importar `describe/it/expect` explícitamente evita depender de globals](#2026-03-26-vitest-en-ejemplos-aislados-importar-describeitexpect-explícitamente-evita-depender-de-globals) | Frontend / Testing |
| 2026-03-26 | [Hosted login compartido: la UI central no debe apropiarse del contexto OAuth2 del tenant origen](#2026-03-26-hosted-login-compartido-la-ui-central-no-debe-apropiarse-del-contexto-oauth2-del-tenant-origen) | OAuth2 / Frontend / Arquitectura |
| 2026-03-25 | [Mermaid en Markdown: evitar signos de interrogación invertidos en nodos validados por parser](#2026-03-25-mermaid-en-markdown-evitar-signos-de-interrogación-invertidos-en-nodos-validados-por-parser) | Documentación / Tooling |
| 2026-03-25 | [Tests Maven por módulo en monorepo: usar `-am` para resolver dependencias de clases](#2026-03-25-tests-maven-por-módulo-en-monorepo-usar--am-para-resolver-dependencias-de-clases) | Build / Testing |
| 2026-03-25 | [Bearer-only admin auth: `@PreAuthorize` + tenant match token/path](#2026-03-25-bearer-only-admin-auth-preauthorize--tenant-match-tokenpath) | Security / Authorization |
| 2026-03-25 | [Claims map puede ser inmutable en tests: copiar antes de agregar `tenant_slug`](#2026-03-25-claims-map-puede-ser-inmutable-en-tests-copiar-antes-de-agregar-tenant_slug) | OAuth2 / Testing |
| 2026-03-24 | [Endpoint `POST /roles`: evitar respuestas "exitosas" sin persistencia real](#2026-03-24-endpoint-post-roles-evitar-respuestas-exitosas-sin-persistencia-real) | API / Hexagonal / Persistencia |
| | 2026-03-24 | [Claim `roles` en JWT para que el frontend lea roles directamente desde el JWT](#2026-03-24-claim-roles-en-jwt-para-que-el-frontend-keygo-ui-lea-los-roles-directamente-desde-el-jwt-sin-llamadas-adicionales-a-la-api) | OAuth2 / JWT / Arquitectura |
| | 2026-03-24 | [JWT admin en filtro: rutas OAuth2 públicas + Bearer con rol admin](#2026-03-24-jwt-admin-en-filtro-rutas-oauth2-públicas--bearer-con-rol-admin) | Security / Filter |
| 2026-03-24 | [SigningKeyInitializer: auto-generar clave RSA en startup con @Profile](#2026-03-24-signingkeyinitializer-auto-generar-clave-rsa-en-startup) | Spring / Startup |
| 2026-03-24 | [replace_string_in_file duplica clase si el string a reemplazar es solo la cabecera](#2026-03-24-replace_string_in_file-puede-duplicar-clase-si-el-texto-a-reemplazar-es-solo-el-importpaquete) | Tooling |
| 2026-03-23 | [keygo-ui — app unificada con roles en JWT (no tres portales separados)](#2026-03-23-keygo-ui--arquitectura-de-app-unificada-con-roles-en-jwt) | Arquitectura / Frontend |
| 2026-03-23 | [Manual frontend: flujo OAuth2 retorna code en JSON, no HTTP 302](#2026-03-23-manual-frontend-flujo-oauth2-retorna-code-en-json-no-http-302) | OAuth2 / Frontend |
| 2026-03-23 | [Nuevas variables de entorno deben documentarse en .env y ENVIRONMENT_SETUP.md](#2026-03-23-nuevas-variables-de-entorno-deben-documentarse-en-env-y-environment_setupmd) | Convenciones / Entorno |
| 2026-03-23 | [Registro con verificación email — ClientApp requiere campos obligatorios en tests](#2026-03-23-registro-con-verificación-email--clientapp-requiere-campos-obligatorios-en-tests) | Tests / Dominio |
| | 2026-03-23 | [Fase 8: client_credentials — sub=clientId, sin refresh_token ni id_token](#2026-03-23-fase-8-client_credentials--sub-clientid-sin-refresh_token-ni-id_token) | OAuth2 / M2M |
| 2026-03-22 | [Flyway: CREATE TABLE IF NOT EXISTS oculta errores de esquema incompleto](#2026-03-22-flyway-create-table-if-not-exists-oculta-errores-de-esquema-incompleto-de-ejecuciones-parciales) | Flyway / DB |
| 2026-03-22 | [Fase 7: SHA-256 como hash determinista para refresh tokens](#2026-03-22-fase-7-sha-256-como-hash-determinista-para-refresh-tokens) | Security / OAuth2 |
| 2026-03-22 | [Fase 7: Mockito UnnecessaryStubbing en tests de use cases complejos](#2026-03-22-fase-7-mockito-unnecessarystubbing-en-tests-de-use-cases-complejos) | Testing |
| 2026-03-22 | [Fase 7: BootstrapAdminKeyFilter — rutas userinfo y revoke como públicas](#2026-03-22-fase-7-bootstrapadminkeyfilter--rutas-userinfo-y-revoke-como-públicas) | Security / Filter |
| 2026-03-22 | [Fase 6: jacoco.skip en módulos que maduran de stub a activo](#2026-03-22-fase-6-eliminar-jacocoskip-cuando-un-módulo-stub-se-activa) | Maven / CI |
| 2026-03-22 | [Fase 6: tests de controllers con MockMvc standalone sin Spring context](#2026-03-22-fase-6-tests-de-controllers-oidcjwks-con-mockmvc-standalone) | Testing |
| 2026-03-22 | [Reorganización de docs AI a docs/ai/](#2026-03-22-reorganización-de-documentos-ai-a-docsai) | Proceso / Documentación |
| 2026-03-22 | [Conversión de diagramas ASCII a Mermaid — criterio de selección](#2026-03-22-conversión-de-diagramas-ascii-a-mermaid--criterio-de-selección) | Documentación / Diagramas |
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

### [2026-03-26] Contraseñas de seed SQL deben documentarse junto al hash BCrypt
**Contexto:** Al revisar los usuarios de seed de V2 y V14 para pruebas del flujo OAuth2, se detectó que el hash BCrypt `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy` no coincide con ninguna contraseña común y no estaba documentado en ningún lugar del repositorio.
**Problema:** Un agente AI generó ese hash sin registrar el texto plano correspondiente, dejando los usuarios de seed inutilizables para pruebas hasta que se ejecute una migración correctiva.
**Solución / Buena práctica:** Siempre que se inserte un `password_hash` en una migración Flyway de seed, documentar la contraseña en texto plano de forma explícita:
1. Como comentario en el propio archivo SQL.
2. En la tabla de credenciales de `AGENTS.md` (sección "Seed credentials").
3. En `docs/data/MIGRATIONS.md` en la sección de esa migración.
Se creó `V15__reset_seed_user_passwords.sql` para corregir el hash con contraseñas conocidas y verificadas mediante `BCryptPasswordEncoder`.
**Archivos clave:** `keygo-supabase/src/main/resources/db/migration/V15__reset_seed_user_passwords.sql`, `docs/data/MIGRATIONS.md`, `AGENTS.md`

### [2026-03-26] ADR-001: documentar decisiones de error handling como ADR
**Contexto:** Se completó la clasificación de errores con `ErrorData` (`origin`, `clientRequestCause`, `clientMessage`) y se actualizaron los docs de flujo OAuth2 y la guía de frontend.
**Problema:** El estándar de manejo de errores quedaba documentado de forma dispersa entre `AUTH_FLOW.md`, `FRONTEND_DEVELOPER_GUIDE.md` y código Java, sin un documento canónico de decisión arquitectónica para onboarding de nuevos integradores o cambio de decisión futura.
**Solución / Buena práctica:** Crear `docs/keygo-ui/ADR-001-error-handling-oauth2.md` como ADR de una página que consolide: contexto, decisión, alternativas descartadas, consecuencias, mapa de errores por etapa OAuth2, árbol de decisión (Mermaid), contrato JSON + tipos TypeScript, helper de referencia y propuestas de evolución (T-064/T-065/T-066).
**Archivos clave:** `docs/keygo-ui/ADR-001-error-handling-oauth2.md`, `docs/api/AUTH_FLOW.md`, `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`

### [2026-03-26] Subclasificar `CLIENT_REQUEST` en `CLIENT_TECHNICAL` vs `USER_INPUT` mejora triage de UI y soporte
**Contexto:** Se necesitaba distinguir, dentro de errores de cliente, si la falla proviene de implementación UI/protocolo (cookies, payload, headers, sesión) o de datos capturados por el usuario.
**Problema:** Con solo `origin=CLIENT_REQUEST`, frontend y soporte seguían sin poder decidir rápidamente quién debía corregir: equipo UI o experiencia/formulario del usuario.
**Solución / Buena práctica:** Agregar `clientRequestCause` opcional en `ErrorData` con valores `CLIENT_TECHNICAL` y `USER_INPUT`, calculado en `ApiErrorDataFactory` usando `ResponseCode` y tipo de excepción (`InvalidCredentialsException`, `MethodArgumentNotValidException`, `MissingServletRequestParameterException`, `HttpMessageNotReadableException`).
**Archivos clave:** `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/ErrorData.java`, `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/ApiErrorDataFactory.java`, `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/GlobalExceptionHandler.java`

### [2026-03-26] En Spring Framework 7, `HttpMessageNotReadableException` en tests requiere `HttpInputMessage`
**Contexto:** Se agregó cobertura para payload JSON malformado en `GlobalExceptionHandlerTest`.
**Problema:** El test falló a compilación al usar `new HttpMessageNotReadableException("...")` porque en Spring Framework 7 no existe constructor de un solo parámetro.
**Solución / Buena práctica:** Construir la excepción con `MockHttpInputMessage`, por ejemplo `new HttpMessageNotReadableException("Malformed JSON", new MockHttpInputMessage(new byte[0]))`.
**Archivos clave:** `keygo-api/src/test/java/io/cmartinezs/keygo/api/error/GlobalExceptionHandlerTest.java`

### [2026-03-26] Clasificar errores por origen (`CLIENT_REQUEST`, `BUSINESS_RULE`, `SERVER_PROCESSING`) mejora diagnóstico frontend y soporte
**Contexto:** Se mejoró el contrato de errores para distinguir más claramente si la falla viene del request del cliente, de una regla de negocio o del procesamiento del servidor.
**Problema:** Con solo `code` y `clientMessage` era difícil para frontend y soporte decidir rápidamente si debían corregir payload, ajustar flujo funcional o escalar una incidencia backend.
**Solución / Buena práctica:** Agregar `origin` en `ErrorData` y calcularlo de forma centralizada en `ApiErrorDataFactory` a partir de `ResponseCode` para mantener consistencia entre `GlobalExceptionHandler` y `BootstrapAdminKeyFilter`.
**Archivos clave:** `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/ErrorData.java`, `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/ApiErrorDataFactory.java`, `keygo-run/src/main/java/io/cmartinezs/keygo/run/filter/BootstrapAdminKeyFilter.java`

### [2026-03-26] Errores API con `BaseResponse.data`: detalle tecnico en `local/dev`, mensaje amigable en otros perfiles
**Contexto:** Se ajusto el manejo global de errores y el `BootstrapAdminKeyFilter` para que siempre respondan `BaseResponse` con `failure` + `data`.
**Problema:** Los errores devolvian solo `failure` (sin `data`), lo cual complicaba diagnostico en desarrollo y dejaba respuestas poco orientadas al cliente en produccion.
**Solucion / Buena practica:** Centralizar la construccion de `data` de error con una factory (`ApiErrorDataFactory`) y conmutar por perfil (`local/dev`): en dev incluir `detail` y `exception`; en otros perfiles exponer solo `code` + `clientMessage` amigable. En tests con Mockito strict, usar `lenient()` para el stubbing por defecto que se sobreescribe en casos puntuales.
**Archivos clave:** `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/ApiErrorDataFactory.java`, `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/GlobalExceptionHandler.java`, `keygo-run/src/main/java/io/cmartinezs/keygo/run/filter/BootstrapAdminKeyFilter.java`, `keygo-api/src/test/java/io/cmartinezs/keygo/api/error/GlobalExceptionHandlerTest.java`

### [2026-03-26] ClientApp CONFIDENTIAL en tests de adapter/mapper debe incluir `hashedSecret`
**Contexto:** Se agregó `ClientAppRepositoryAdapterTest` para validar el fix de lazy loading en búsqueda de client apps usadas por `/oauth2/authorize`.
**Problema:** El test falló al mapear `ClientAppEntity` a dominio con `IllegalArgumentException: CONFIDENTIAL client apps must have a hashed secret` porque las entidades de prueba `CONFIDENTIAL` no incluían `hashedSecret`.
**Solución / Buena práctica:** En tests de adapters/mappers que construyen `ClientAppEntity` de tipo `CONFIDENTIAL`, setear siempre `hashedSecret` para respetar invariantes del dominio al ejecutar `ClientApp.builder().build()`.
**Archivos clave:** `keygo-supabase/src/test/java/io/cmartinezs/keygo/supabase/clientapp/adapter/ClientAppRepositoryAdapterTest.java`, `keygo-domain/src/main/java/io/cmartinezs/keygo/domain/clientapp/model/ClientApp.java`

### [2026-03-26] Missing query param en MockMvc standalone: sin handler específico cae en 500 (`OPERATION_FAILED`)
**Contexto:** Tests de `AuthorizationController` al migrar `/oauth2/authorize` a `@RequestParam` explícitos.
**Problema:** El caso de `response_type` faltante lanzó `MissingServletRequestParameterException`; en el `GlobalExceptionHandler` actual no hay handler dedicado para esa excepción, por lo que entra al catch-all y responde `500 OPERATION_FAILED`.
**Solución / Buena práctica:** En tests de controller standalone, al validar parámetros faltantes, alinear expectativas con el manejo real de excepciones configurado. Si se desea `400 INVALID_INPUT`, agregar un handler explícito para `MissingServletRequestParameterException`.
**Archivos clave:** `keygo-api/src/test/java/io/cmartinezs/keygo/api/auth/controller/AuthorizationControllerTest.java`, `keygo-api/src/main/java/io/cmartinezs/keygo/api/error/GlobalExceptionHandler.java`

### [2026-03-26] OAuth2 authorize: para query params en snake_case usar `@RequestParam(name = ...)` en lugar de depender de `@ModelAttribute`
**Contexto:** Corrección del endpoint `GET /api/v1/tenants/{tenantSlug}/oauth2/authorize` tras recibir error de validación con `response_type=code`.
**Problema:** Con `@Valid @ModelAttribute AuthorizationRequest`, el parámetro RFC `response_type` no se enlazaba automáticamente al campo Java `responseType`; el backend terminaba evaluando `null` y lanzaba `response_type must be 'code' for this implementation`.
**Solución / Buena práctica:** Para query params OAuth2/OIDC con naming estándar en snake_case, declarar `@RequestParam(name = "...")` explícitos en el controller y mapearlos al request interno. Configuración de Jackson no aplica en este binding porque no interviene en `@ModelAttribute` de query string.
**Archivos clave:** `keygo-api/src/main/java/io/cmartinezs/keygo/api/auth/controller/AuthorizationController.java`, `keygo-api/src/test/java/io/cmartinezs/keygo/api/auth/controller/AuthorizationControllerTest.java`

### [2026-03-26] Vitest en ejemplos aislados: importar `describe/it/expect` explícitamente evita depender de globals
**Contexto:** Validación del ejemplo `examples/hosted-login-handoff/` agregado para implementar `T-056`.
**Problema:** Al ejecutar `vitest run`, ambos archivos de test fallaron con `ReferenceError: describe is not defined` porque el paquete de ejemplo no tenía habilitado `globals: true` y los tests asumían esa configuración implícita.
**Solución / Buena práctica:** En ejemplos aislados o paquetes pequeños, importar `describe`, `it` y `expect` directamente desde `vitest` hace los tests más portables y evita depender de configuración global adicional.
**Archivos clave:** `examples/hosted-login-handoff/tests/hostedLoginParams.test.ts`, `examples/hosted-login-handoff/tests/HostedLoginBoundary.test.tsx`

### [2026-03-26] Hosted login compartido: la UI central no debe apropiarse del contexto OAuth2 del tenant origen
**Contexto:** Actualización del flujo de autenticación y de la guía frontend para documentar cómo una app de otro tenant puede reutilizar el login visual de `keygo-ui`.
**Problema:** Es fácil confundir "usar la misma pantalla de login" con "autenticar siempre contra el tenant `keygo`" o con hacer que la UI central canjee y almacene tokens de una app ajena.
**Solución / Buena práctica:** En el patrón recomendado de hosted login, la UI central solo presta la experiencia visual y ejecuta `/oauth2/authorize` + `/account/login` con el contexto recibido. El `tenantSlug`, `client_id`, `redirect_uri`, `state`, `code_verifier` y el canje final en `/oauth2/token` deben seguir perteneciendo a la app origen.
**Archivos clave:** `docs/api/AUTH_FLOW.md`, `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`

### [2026-03-25] Mermaid en Markdown: evitar signos de interrogación invertidos en nodos validados por parser
**Contexto:** Validación de documentos AI tras registrar una inconsistencia documental de seguridad.
**Problema:** El validador reportó error en un bloque Mermaid de `docs/ai/inconsistencias.md` cuando un nodo usaba texto con signo de interrogación invertido (`¿Corregida?`).
**Solución / Buena práctica:** Cuando un Markdown sea validado por parser estricto, preferir labels Mermaid simples y ASCII-safe en los nodos (por ejemplo `Corregida?`) para evitar falsos errores de sintaxis.
**Archivos clave:** `docs/ai/inconsistencias.md`, `docs/ai/lecciones.md`

### [2026-03-25] Tests Maven por módulo en monorepo: usar `-am` para resolver dependencias de clases
**Contexto:** Validación de cambios de migración en `keygo-supabase` ejecutando tests del módulo de forma aislada.
**Problema:** `./mvnw -pl keygo-supabase test` falló con `NoClassDefFoundError` de clases en `keygo-app` y `keygo-domain` durante tests del adapter, porque no se habían construido dependencias reactor necesarias en esa ejecución.
**Solución / Buena práctica:** En monorepo Maven multi-módulo, para correr tests de un módulo que depende de artefactos locales, usar `-am` (`also-make`): `./mvnw -pl keygo-supabase -am test`. Esto compila módulos requeridos y evita errores de classpath.
**Archivos clave:** `keygo-supabase/pom.xml`, `docs/ai/lecciones.md`

### [2026-03-25] Bearer-only admin auth: `@PreAuthorize` + tenant match token/path
**Contexto:** Migración de seguridad para endpoints admin: remover `X-KEYGO-ADMIN` y usar solo `Authorization: Bearer`.
**Problema:** Validar JWT en el filtro no es suficiente para autorización fina; sin control por endpoint ni tenant se puede habilitar acceso cruzado entre tenants para usuarios con rol administrativo.
**Solución / Buena práctica:**
1. Mantener autenticación central en filtro (`BootstrapAdminKeyFilter`) para validar firma/expiración y publicar authorities desde claim `roles`.
2. Aplicar `@PreAuthorize` en cada controller admin para declarar explícitamente el rol requerido.
3. Usar evaluador SpEL (`tenantAuthorizationEvaluator`) para comparar `tenantSlug` del path con claim `tenant_slug` (fallback a `iss`) en tokens de `ADMIN_TENANT`.
4. Reservar bypass global por rol solo para `ADMIN` plataforma.
**Archivos clave:** `keygo-run/src/main/java/io/cmartinezs/keygo/run/filter/BootstrapAdminKeyFilter.java`, `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/security/SecurityConfig.java`, `keygo-api/src/main/java/io/cmartinezs/keygo/api/security/TenantAuthorizationEvaluator.java`

### [2026-03-25] Claims map puede ser inmutable en tests: copiar antes de agregar `tenant_slug`
**Contexto:** Al agregar claim `tenant_slug` en emisión de access tokens (`IssueTokensUseCase` y `RotateRefreshTokenUseCase`), algunos tests empezaron a fallar con `UnsupportedOperationException`.
**Problema:** En tests se mockeaba `TokenClaimsFactoryPort` con `Map.of(...)` (mapas inmutables). Al hacer `claims.put("tenant_slug", ...)` sobre ese mapa, la ejecución falla.
**Solución / Buena práctica:** Siempre copiar el resultado del factory a un mapa mutable (`new LinkedHashMap<>(...)`) antes de enriquecer claims adicionales en el caso de uso.
**Archivos clave:** `keygo-app/src/main/java/io/cmartinezs/keygo/app/auth/usecase/IssueTokensUseCase.java`, `keygo-app/src/main/java/io/cmartinezs/keygo/app/auth/usecase/RotateRefreshTokenUseCase.java`, `keygo-app/src/main/java/io/cmartinezs/keygo/app/auth/usecase/IssueClientCredentialsTokenUseCase.java`

### [2026-03-24] Endpoint `POST /roles`: evitar respuestas "exitosas" sin persistencia real
**Contexto:** Al preparar bootstrap de tenants/apps/usuarios para `keygo-ui`, se detectó que `POST /api/v1/tenants/{tenantSlug}/apps/{clientAppId}/roles` devolvía `201 ROLE_CREATED`, pero el rol no se guardaba en DB.
**Problema:** El controller construía el objeto de dominio en memoria y respondía éxito sin pasar por un use case ni por `AppRoleRepositoryPort.save()`. Esto rompe el contrato funcional y provoca fallos encadenados en memberships/scripts.
**Solución / Buena práctica:** Mover la creación a `CreateAppRoleUseCase` en `keygo-app`, usando command + validaciones (tenant activo, app perteneciente al tenant, código no duplicado) y persistencia vía puerto de salida. El controller debe orquestar request/response y nunca simular persistencia.
**Archivos clave:** `keygo-app/src/main/java/io/cmartinezs/keygo/app/membership/usecase/CreateAppRoleUseCase.java`, `keygo-api/src/main/java/io/cmartinezs/keygo/api/membership/controller/TenantAppRoleController.java`, `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/ApplicationConfig.java`

### [2026-03-24] Claim `roles` en JWT para que el frontend `keygo-ui` lea los roles directamente desde el JWT sin llamadas adicionales a la API.
**Problema:** El claim `roles` debe propagarse por toda la cadena de emisión de tokens: `MembershipRepositoryPort` → use case → `TokenClaimsFactoryPort` → `StandardTokenClaimsFactory`. Al agregar un parámetro a la interfaz, **todos** los callers deben actualizarse: `IssueTokensUseCase`, `RotateRefreshTokenUseCase`, `AuthorizationController` y también `IssueClientCredentialsTokenUseCase` (que pasa `null` porque M2M no tiene usuario ni membresía).
**Solución / Buena práctica:**
1. Agregar `findRoleCodesByUserAndClientApp(UUID userId, UUID clientAppId): List<String>` al port `MembershipRepositoryPort`.
2. Usar `@Query(nativeQuery = true)` en `MembershipJpaRepository` para el JOIN de 3 tablas (`app_roles` ← `membership_roles` ← `memberships`) filtrando por `status = 'ACTIVE'` — evita múltiples roundtrips.
3. Propagar `List<String> roles` como parámetro explícito en **ambas** firmas de `TokenClaimsFactoryPort`.
4. Para M2M (`client_credentials`), pasar `null` — la factory omite el claim si la lista es null o vacía.
5. Al agregar `MembershipRepositoryPort` como dependencia de `RotateRefreshTokenUseCase`, actualizar el bean en `ApplicationConfig` con el nuevo parámetro en el orden correcto del constructor.
6. Los tests de `IssueTokensUseCaseTest`, `RotateRefreshTokenUseCaseTest` y `StandardTokenClaimsFactoryTest` deben actualizarse para reflejar las nuevas firmas.
**Archivos clave:** `keygo-app/src/main/java/.../app/auth/port/TokenClaimsFactoryPort.java`, `keygo-app/src/main/java/.../app/membership/port/MembershipRepositoryPort.java`, `keygo-infra/src/main/java/.../infra/auth/jwt/StandardTokenClaimsFactory.java`, `keygo-supabase/src/main/java/.../supabase/membership/repository/MembershipJpaRepository.java`, `keygo-run/src/main/java/.../run/config/ApplicationConfig.java`

### [2026-03-24] JWT admin en filtro: rutas OAuth2 públicas + Bearer con rol admin
**Contexto:** Implementar el ítem "Los endpoints admin aún no validan JWT" para habilitar el frontend `keygo-ui` a llamar endpoints protegidos con su Bearer token (obtenido tras login OAuth2).
**Problema:** Dos issues combinados: (1) las rutas OAuth2 del flujo de autorización (`/oauth2/authorize`, `/account/login`, `/oauth2/token`) estaban protegidas por `X-KEYGO-ADMIN`, creando un círculo vicioso — el frontend no puede obtener un JWT sin autenticarse primero. (2) el filtro solo aceptaba `X-KEYGO-ADMIN` pero no Bearer JWT.
**Solución / Buena práctica:**
1. Agregar sufijos públicos para las rutas OAuth2: `authorizePathSuffix`, `loginPathSuffix`, `tokenPathSuffix` en `KeyGoBootstrapProperties` + `application.yml`.
2. Agregar dos campos opcionales al filtro con `@Autowired(required = false)` (package-private para permitir inyección en tests del mismo paquete): `AccessTokenVerifierPort` y `SigningKeyRepositoryPort`.
3. El nuevo método `validateBearerAdminToken()` usa `signingKeyRepository.findPublishableKeys()` + `accessTokenVerifier.verify()` y luego comprueba el claim `roles` contra `bootstrapProperties.getAdminRoles()`.
4. El método `validateAuthentication()` prueba primero `X-KEYGO-ADMIN` (si está presente), luego Bearer JWT. Si ninguno está disponible, retorna false.
5. Cuando se usa `replace_string_in_file` sobre archivos largos con coincidencia de texto corta, el tool puede DUPLICAR el archivo. La solución segura es usar `create_file` para archivos nuevos o `head -N > tmp && mv tmp original` para truncar duplicaciones.
**Archivos clave:** `keygo-run/.../filter/BootstrapAdminKeyFilter.java`, `keygo-run/.../properties/KeyGoBootstrapProperties.java`, `keygo-run/src/main/resources/application.yml`

### [2026-03-24] SigningKeyInitializer: auto-generar clave RSA en startup
**Contexto:** El frontend necesita un signing key RSA activo para que el flujo OAuth2 emita JWTs. Sin una clave activa, el endpoint de token falla con `NoActiveSigningKeyException`.
**Problema:** No había mecanismo automático para generar la primera clave RSA al iniciar el servidor en un entorno nuevo (DB vacía). Los desarrolladores debían insertar la clave manualmente o via script SQL.
**Solución / Buena práctica:** Crear `SigningKeyInitializer implements ApplicationRunner` en `keygo-run`, anotado con `@Profile("supabase")` para que solo se active cuando hay DB. En `run()`: si no hay clave ACTIVE, genera un par RSA-2048 usando `KeyPairGenerator`, codifica a PEM con `Base64.getMimeEncoder(64, ...)` y persiste via `SigningKeyRepositoryPort.save()`. Esto es idempotente: en re-inicios con clave existente, el método es un no-op.
**Archivos clave:** `keygo-run/src/main/java/.../run/startup/SigningKeyInitializer.java`

### [2026-03-24] replace_string_in_file puede duplicar clase si el texto a reemplazar es solo el import/paquete
**Contexto:** Al actualizar `UserPersistenceMapper.java` para agregar campos OIDC extendidos, se usó `replace_string_in_file` con solo la sección de imports como `oldString`, reemplazándola por el archivo completo nuevo.
**Problema:** El tool concatenó el nuevo contenido al principio del string coincidente pero dejó el resto del contenido original intacto. El resultado fue que el archivo tenía dos declaraciones `public class UserPersistenceMapper` — error `duplicate class` en compilación.
**Solución / Buena práctica:** Cuando se quiere reescribir un archivo Java completamente, usar el comando shell `cat > file << 'EOF' ... EOF` en lugar de `replace_string_in_file`. Alternativamente, si se usa `replace_string_in_file` para reemplazar por contenido completo, el `oldString` debe incluir la clase completa (package + imports + class body) para que el reemplazo sea total.
**Archivos clave:** `keygo-supabase/src/main/java/io/cmartinezs/keygo/supabase/user/mapper/UserPersistenceMapper.java`

### [2026-03-24] Al extender un record Java (command/request/result), actualizar todos los sitios de construcción del record
**Contexto:** Se extendió `UpdateUserCommand` de 4 a 10 parámetros para soportar campos OIDC extendidos del perfil de usuario.
**Problema:** Los tests existentes que usaban `new UpdateUserCommand(slug, id, "Jane", "Smith")` fallaron al compilar porque el constructor del record cambió.
**Solución / Buena práctica:** Tras cambiar la firma de un record, buscar con grep todos los `new UpdateUserCommand(` / `new UpdateUserRequest(` en el código de producción y tests, y actualizar los constructores con los nuevos parámetros (pasar `null` para opcionales). Hacer esto antes de compilar evita el ciclo error-corrección.
**Archivos clave:** `keygo-app/src/test/java/.../UpdateResetValidateUseCaseTest.java`, `keygo-api/src/test/java/.../TenantUserControllerTest.java`

### [2026-03-24] Diseño de perfil de usuario en IAM: perfil canónico en tenant_users, metadata app en membership_attributes
**Contexto:** Decisión de diseño sobre si el perfil de usuario debía vivir en `tenant_users` (nivel tenant) o en `memberships` (nivel app).
**Problema:** La pregunta era válida ya que cada app podría querer campos diferentes del usuario.
**Solución / Buena práctica:** Adoptar el modelo en dos capas que siguen Auth0, Keycloak y OIDC §5.3:
  - **Capa 1 (V13):** perfil canónico OIDC en `tenant_users` — los 6 claims estándar (`phone_number`, `locale`, `zoneinfo`, `profile_picture_url`, `birthdate`, `website`) viven aquí porque son del usuario, no de la app.
  - **Capa 2 (pendiente V14):** metadata app-específica en `membership_attributes` — pares clave-valor para datos que varían por app.
  La regla de oro: si el dato respondería a "¿quién eres?" → perfil canónico. Si responde a "¿qué eres en esta app?" → membership_attributes.
**Archivos clave:** `docs/data/MIGRATIONS.md`, `V13__extend_tenant_user_profile.sql`, `keygo-domain/src/main/java/.../user/model/User.java`



### [2026-03-23] Manual frontend: flujo OAuth2 retorna code en JSON, no HTTP 302

**Contexto:** Al crear el manual de desarrollador frontend (`docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`), se revisó el comportamiento actual del endpoint `POST /account/login`.

**Problema:** El estándar OAuth2 (RFC 6749) define que el authorization code se entrega al cliente mediante un redirect HTTP 302 hacia `redirect_uri?code=...&state=...`. El backend de KeyGo actualmente devuelve el código directamente en el JSON de la respuesta (`data.code`) sin hacer el redirect. Esto es una desviación del estándar que debe ser transparente para el desarrollador frontend.

**Solución / Buena práctica:** El frontend debe:
1. Leer el `code` del JSON de la respuesta del `POST /account/login`.
2. Construir manualmente la URL del callback y navegar a ella (`window.location.href`).
3. Diseñar el `CallbackPage` para que pueda procesar el código tanto desde query params (cuando el backend implemente el redirect 302 real) como desde el estado de navegación (para compatibilidad futura).

El manual documenta ambos comportamientos con notas explícitas. Ver `AGENTS.md` → tabla de fases (`POST /account/login` sección "Fase 7 planificada").

**Archivos clave:** `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`, `docs/api/AUTH_FLOW.md` sección "Paso 2".

---

### [2026-03-23] keygo-ui — arquitectura de app unificada con roles en JWT

**Contexto:** Se revisó y corrigió el diseño del manual de frontend. La primera versión planteaba tres portales separados (tres apps React), lo cual es un antipatrón para este tipo de sistema IAM.

**Problema:** Diseñar portales separados implica múltiples apps registradas en el sistema, múltiples flujos de autenticación diferenciados y duplicación de código. Además, los admins siguen siendo usuarios del sistema y deben autenticarse igual que cualquier otro usuario.

**Solución / Buena práctica:**
- **Una sola app React** (`keygo-ui`) registrada como `ClientApp` en el tenant `keygo` (tenant raíz).
- **Un solo flujo OAuth2/PKCE** para todos los usuarios, sin importar su rol.
- Los roles (`ADMIN`, `ADMIN_TENANT`, `USER_TENANT`) se determinan por los claims del JWT.
- El routing y las vistas se adaptan al rol con `<RoleGuard>` y `useHasRole()`.
- El `ADMIN_TENANT` necesita el claim `managed_tenant` en el JWT para saber qué tenant gestiona — este claim es **pendiente de implementación en backend**.
- Mientras el backend no emita roles en el JWT, simularlos con `VITE_MOCK_ROLE` y MSW en desarrollo.

**Archivos clave:** `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`, `src/auth/roleGuard.tsx`, `src/auth/jwksVerify.ts`.

---

### [2026-03-23] Nuevas variables de entorno deben documentarse en .env y ENVIRONMENT_SETUP.md

**Contexto:** Al agregar `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `KEYGO_MAIL_FROM` y `KEYGO_MAIL_APP_NAME` para el flujo de registro con verificación de email, se detectó que solo se habían declarado en `application.yml` pero no en los archivos `.env` ni en la documentación de entornos.

**Problema:** Las variables de entorno nuevas declaradas en `application.yml` con sintaxis `${VAR:default}` no se propagan automáticamente a los archivos `.env*` ni a la documentación. El desarrollador que active un nuevo ambiente no sabrá qué variables configurar.

**Solución / Buena práctica:** Ante **cualquier** variable de entorno nueva en `application.yml`, actualizar obligatoriamente:
1. `keygo-supabase/.env.example` — agregar la variable con valor de ejemplo y comentario explicativo
2. `keygo-supabase/.env-local` — valor apropiado para desarrollo local (ej: MailHog sin autenticación)
3. `keygo-supabase/.env-desa` — valor para sandbox/staging (ej: Mailtrap)
4. `keygo-supabase/.env-prod` — valor placeholder con comentario de dónde obtenerlo
5. `keygo-supabase/.env` — mismo valor que `.env-desa` (es el ambiente activo)
6. `docs/development/ENVIRONMENT_SETUP.md` — agregar a la tabla completa y, si aplica, al bloque `application.yml`
7. `scripts/quick-start.sh` — si la variable es usada por el flujo de desarrollo

**Regla de oro:** Si aparece en `application.yml` como `${VARIABLE:default}`, debe aparecer en todos los `.env*`.

**Archivos clave:** `keygo-supabase/.env.example`, `docs/development/ENVIRONMENT_SETUP.md`, `scripts/quick-start.sh`

---

### [2026-03-23] Registro con verificación email — ClientApp requiere campos obligatorios en tests

**Contexto:** Implementación del flujo de registro de usuarios con verificación por email (Fase 9). Tests unitarios para `RegisterTenantUserUseCase`, `VerifyEmailUseCase` y `ResendVerificationEmailUseCase`.

**Problema:** Al construir un objeto `ClientApp` en los tests usando el builder sin todos los campos requeridos (`id`, `type`, `status`, `accessPolicy`), se obtenía `IllegalArgumentException: ClientApp id cannot be null` antes de que los tests pudieran ejecutarse. El dominio es estricto en validaciones en el constructor.

**Solución / Buena práctica:** Siempre construir objetos de dominio con todos sus campos requeridos en los tests, aunque el objeto solo se use como valor de retorno de un mock. Para `ClientApp`: se requieren `id (ClientAppId.generate())`, `type (ClientType.PUBLIC)`, `status (ClientAppStatus.ACTIVE)`, y `accessPolicy (new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))`.

**Archivos clave:** `keygo-domain/src/main/java/.../domain/clientapp/model/ClientApp.java` (líneas 44-52 — validaciones del constructor)

### [2026-03-23] Fase 8: client_credentials — sub=clientId, sin refresh_token ni id_token
**Contexto:** Implementación del grant `client_credentials` (Fase 8 — M2M) en `IssueClientCredentialsTokenUseCase` y rama correspondiente en `AuthorizationController`.
**Problema:** El grant `client_credentials` no representa a un usuario final, lo que implica diferencias importantes respecto a `authorization_code`:
- El `sub` del access token debe ser el `client_id` (string), **no** un UUID de usuario.
- **No se emite** `id_token` (OIDC es para usuarios; M2M no tiene identidad de usuario).
- **No se emite** `refresh_token` (las apps M2M pueden solicitar un token nuevo directamente).
- Solo apps de tipo `CONFIDENTIAL` pueden usar este grant (las PUBLIC no tienen secret).
- El campo `client_secret` es obligatorio en el request.
**Solución / Buena práctica:**
- En `TokenData`, los campos `idToken` y `refreshToken` se excluyen automáticamente por la config `NON_NULL` de Jackson — no hace falta serialización especial.
- La resolución de scopes M2M es diferente: si no se solicitan scopes específicos, se retornan **todos los permitidos**; si se solicitan, se filtra la intersección con los permitidos.
- El `aud` del token M2M se establece igual al `sub` (`clientId`), diferente al flujo user donde `aud = clientId` pero `sub = userId`.
**Archivos clave:** `IssueClientCredentialsTokenUseCase.java`, `AuthorizationController.java` (método `handleClientCredentialsGrant`), `IssueClientCredentialsTokenCommand.java`, `IssueClientCredentialsTokenResult.java`

---

### [2026-03-22] Fase 6: eliminar jacoco.skip cuando un módulo stub se activa
**Contexto:** `keygo-infra` tenía `<jacoco.skip>true</jacoco.skip>` porque era un módulo "stub vacío". Al implementar la Fase 6 se llenó con código de producción (`RsaJwtTokenSigner`, `JwkSetBuilder`, `StandardTokenClaimsFactory`) y sus tests unitarios.
**Problema:** Si `jacoco.skip=true` queda activo tras activar un módulo, la cobertura de ese código nunca se mide ni reporta, generando un punto ciego en el quality gate.
**Solución / Buena práctica:** Al escribir el primer código productivo en un módulo anteriormente stub, remover `<jacoco.skip>true</jacoco.skip>` en el mismo commit. Un módulo con tests reales no debería tener skip de cobertura.
**Archivos clave:** `keygo-infra/pom.xml`

---

### [2026-03-22] Fase 6: tests de controllers OIDC/JWKS con MockMvc standalone
**Contexto:** `JwksController` y `OidcMetadataController` retornan `Map<String, Object>` (JSON nativo RFC 7517 / OIDC Discovery 1.0) en lugar del envelope `BaseResponse<T>`.
**Problema:** Los controllers no podían testearse con el patrón habitual de `BaseResponse`. Además, `@WebMvcTest` cargaría el contexto Spring completo con beans que no están en `keygo-api`.
**Solución / Buena práctica:** Usar `MockMvcBuilders.standaloneSetup(controller).build()` con `@ExtendWith(MockitoExtension.class)` (sin Spring). Verificar JSON nativo directamente con `jsonPath("$.keys")`, `jsonPath("$.issuer")`, etc. — no buscar envelope. El `setUp()` se llama al inicio de cada test en lugar de `@BeforeEach` para evitar posibles interferencias entre tests.
**Archivos clave:** `keygo-api/src/test/java/.../auth/controller/JwksControllerTest.java`, `OidcMetadataControllerTest.java`

---

### [2026-03-22] Reorganización de documentos AI a docs/ai/
**Contexto:** Limpieza de la raíz del repositorio — los sub-documentos AI estaban directamente en la raíz, mezclados con documentos de producto.
**Problema:** Los archivos `AI_CONTEXT.lecciones.md`, `AI_CONTEXT.propuestas.md`, `AGENTS.registro.md`, `INCONSISTENCIAS.md` e `INCONSISTENCIAS.datos.md` en la raíz dificultaban la navegación. La raíz debería tener solo la info general y los enlaces al detalle.
**Solución / Buena práctica:** Crear `docs/ai/` como categoría específica para documentación de agentes. Los documentos raíz AI (`AI_CONTEXT.md`, `AGENTS.md`) son resúmenes con info general y enlaces a `docs/ai/`. Los sub-documentos detallados viven en `docs/ai/`. El script `check-ai-docs.sh` actualizado apunta a los nuevos paths. Las tablas de referencia en `CLAUDE.md` y `.github/copilot-instructions.md` también actualizadas.
**Archivos clave:** `docs/ai/`, `AI_CONTEXT.md`, `AGENTS.md`, `CLAUDE.md`, `.github/copilot-instructions.md`, `scripts/check-ai-docs.sh`

---

### [2026-03-22] Corrección de inconsistencias docs vs DB — criterio de decisión
**Contexto:** Re-auditoría de las 12 inconsistencias detectadas en `inconsistencias-datos.md` y marcadas como "resueltas". El usuario solicitó revisar si las correcciones fueron suficientes o si la DB debía ajustarse también.
**Problema:** La corrección anterior actualizó los documentos para reflejar lo que había en la DB (tablas en singular: `app_role`, `membership`, `membership_role`). Pero la convención estándar PostgreSQL exige nombres en plural, y la documentación original sí los tenía en plural. Al corregir solo los docs, se perpetuó una inconsistencia real en el schema.
**Solución / Buena práctica:**
Al revisar una inconsistencia entre docs y código/DB, aplicar este criterio:
1. **La documentación manda en convenciones de nomenclatura** (singular/plural, casing, patrones de nombres). Si la doc dice plural, la DB debe ser plural → crear migración.
2. **La implementación manda cuando hay razón técnica clara** (normalización, columnas redundantes, estándares RFC, seguridad). Si la implementación omitió `tenant_id` en `membership` porque sería redundante, es la implementación la correcta.
3. **Ambos pueden tener razón parcial** → aplicar el criterio de menor impacto y mayor consistencia con el sistema.
4. **Corregir la documentación** para reflejar el criterio aplicado (no simplemente para aceptar la implementación si esta está mal).
5. **Nunca marcar como "corregido" una inconsistencia donde solo se ajustó la documentación para aceptar algo que viola convenciones**. Agregar una nota de "pendiente de migración" en ese caso.

**Archivos clave:** `docs/ai/inconsistencias-datos.md`, `V10__rename_membership_tables_to_plural.sql`, `AppRoleEntity.java`, `MembershipEntity.java`

---

### [2026-03-22] Documentación de datos desincronizada con migraciones Flyway reales
**Contexto:** Actualización explícita de `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md`, `DATA_DICTIONARY.md` y `AUTH_FLOW.md`. Se leyeron las migraciones SQL reales (V1–V9) y se compararon con los documentos existentes.
**Problema:** Múltiples discrepancias críticas. Ver detalle completo en [`inconsistencias-datos.md`](inconsistencias-datos.md).
**Solución / Buena práctica:** Al generar documentación de datos, **siempre leer las migraciones SQL reales** antes de escribir el diccionario. No asumir columnas ni tipos — verificar cada campo en `V{n}__*.sql`. **Regla obligatoria:** al crear cualquier migración Flyway nueva, actualizar `DATA_MODEL.md`, `ENTITY_RELATIONSHIPS.md` y `DATA_DICTIONARY.md` antes de cerrar la tarea.
**Archivos clave:** `docs/data/DATA_MODEL.md`, `docs/ai/inconsistencias-datos.md`, `keygo-supabase/src/main/resources/db/migration/V1–V9`

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
**Archivos clave:** `.github/workflows/ci.yml`, `pom.xml` (Maven Enforcer), `docs/development/CODE_STYLE.md`

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
**Solución / Buena práctica:** Función reutilizable `check_section(FILE, SECTION_LABEL)` con arrays globales. Compatibilidad GNU/BSD date. Exit code = peor de los dos docs. Tras reorganización a `docs/ai/`, el script verifica `docs/ai/lecciones.md` y `docs/ai/agents-registro.md`.
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
**Solución / Buena práctica:** Crear 3 documentos complementarios en `docs/data/`: `DATA_MODEL.md` (diccionario), `ENTITY_RELATIONSHIPS.md` (flujos), `MIGRATIONS.md` (índice). Usar Mermaid para todos los diagramas. Referencias cruzadas entre los 3.
**Archivos clave:** `docs/data/DATA_MODEL.md`, `docs/data/ENTITY_RELATIONSHIPS.md`, `docs/data/MIGRATIONS.md`

---

### [2026-03-22] Conversión de diagramas ASCII a Mermaid — criterio de selección
**Contexto:** Auditoría de todos los archivos `.md` del repositorio para identificar diagramas hechos con caracteres de dibujo de caja (box-drawing Unicode: `┌ ┐ └ ┘ │ ─ ▼ ▲`) y convertirlos a Mermaid.
**Problema:** Tres archivos tenían diagramas en ASCII: arquitectura de módulos, componentes hexagonales y flujo del filtro de seguridad con jerarquía de excepciones. Además, múltiples archivos tenían árboles de directorios con `├──` `└──` que *parecían* diagramas ASCII pero no lo son.
**Solución / Buena práctica:**
- Usar `grep -rP '[\x{2500}-\x{257F}]'` para encontrar archivos con box-drawing Unicode.
- **Convertir** diagramas visuales de cajas/flechas → Mermaid (`flowchart TD`, `classDiagram`, `flowchart LR`).
- **No convertir** árboles de directorios (`├──`, `└──`): Mermaid no tiene tipo nativo de árbol de directorios y la versión `flowchart TD` es más verbosa y menos legible.
- Al convertir, actualizar el estado de los módulos en el diagrama para reflejar el estado actual del proyecto (no copiar datos desactualizados del ASCII).
**Archivos clave:**
- `docs/design/ARCHITECTURE.md` (3 diagramas: módulos, flujo request, regla de dependencia)
- `docs/api/BOOTSTRAP_FILTER.md` (2 diagramas: flowchart de autenticación + classDiagram de excepciones)

---

### [2026-03-22] Fase 7: SHA-256 como hash determinista para refresh tokens
**Contexto:** Implementación de refresh tokens (Fase 7). El refresh token plano se genera con `SecureRandom` y no se persiste; solo se guarda su hash para búsqueda posterior.
**Problema:** BCrypt genera un salt distinto por cada invocación, haciendo imposible la búsqueda directa en DB por hash. Para encontrar un refresh token a partir del valor plano recibido del cliente, se necesita un hash determinista.
**Solución / Buena práctica:**
- Usar **SHA-256 (hex, 64 chars)** como `token_hash` en DB — determinista, permite búsqueda directa con `WHERE token_hash = ?`.
- El campo está indexado (`idx_refresh_tokens_hash`) para búsquedas eficientes.
- El token plano tiene 256 bits de entropía (`SecureRandom.nextBytes(32)` + Base64URL), lo que hace inviable la fuerza bruta sobre el hash SHA-256.
- BCrypt no se usa para refresh tokens (solo para contraseñas de usuario).
- Exponer el método `sha256Hex()` como `static` package-private en `RotateRefreshTokenUseCase` para que los tests puedan calcular el hash esperado sin duplicar la lógica.
**Archivos clave:**
- `keygo-app/src/main/java/io/cmartinezs/keygo/app/auth/usecase/RotateRefreshTokenUseCase.java`
- `keygo-app/src/main/java/io/cmartinezs/keygo/app/auth/usecase/RevokeTokenUseCase.java`
- `keygo-supabase/src/main/resources/db/migration/V11__add_refresh_tokens_and_sessions.sql`

### [2026-03-22] Fase 7: Mockito UnnecessaryStubbing en tests de use cases complejos
**Contexto:** Test `RotateRefreshTokenUseCaseTest` configuraba stub para `clientApp.getClientId()` en el método `setupCommonMocks`.
**Problema:** Mockito strict stubbing lanzó `UnnecessaryStubbingException` porque `RotateRefreshTokenUseCase` compara el `clientAppId` (UUID) pero nunca llama a `getClientId()` — ese método es para el `ClientId` (string), no para el `ClientAppId` (UUID).
**Solución / Buena práctica:**
- Revisar qué métodos del mock usa realmente el use case antes de agregar stubs en helpers de tests.
- Si un mock se usa con muchos métodos en múltiples tests, preferir `@MockitoSettings(strictness = Strictness.LENIENT)` o usar `lenient().when(...)` solo para los stubs opcionales.
- Eliminar `when(clientApp.getClientId())` ya que el use case solo usa `clientApp.getId()`.
**Archivos clave:**
- `keygo-app/src/test/java/io/cmartinezs/keygo/app/auth/usecase/RotateRefreshTokenUseCaseTest.java`

### [2026-03-22] Fase 7: BootstrapAdminKeyFilter — rutas userinfo y revoke como públicas
**Contexto:** El filtro `BootstrapAdminKeyFilter` protege todas las rutas bajo `/api/` con `X-KEYGO-ADMIN`. Los nuevos endpoints `/userinfo` y `/oauth2/revoke` son endpoints de usuario (Bearer token) y deben ser accesibles sin clave admin.
**Problema:** Al agregar `/api/v1/tenants/{slug}/userinfo` y `/api/v1/tenants/{slug}/oauth2/revoke` bajo `/api/`, el filtro los bloqueaba con 401 si no se enviaba `X-KEYGO-ADMIN`.
**Solución / Buena práctica:**
- Agregar propiedades `userinfo-path-suffix: /userinfo` y `revocation-path-suffix: /oauth2/revoke` a `KeyGoBootstrapProperties`.
- En `isPublicPath()` del filtro, usar `path.endsWith()` para estos sufijos.
- Patrón: cada tipo de ruta pública tiene su propia propiedad de configuración, lo que permite ajustes sin recompilar.
**Archivos clave:**
- `keygo-run/src/main/java/io/cmartinezs/keygo/run/filter/BootstrapAdminKeyFilter.java`
- `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/properties/KeyGoBootstrapProperties.java`
- `keygo-run/src/main/resources/application.yml`

---

### [2026-03-22] Flyway: CREATE TABLE IF NOT EXISTS oculta errores de esquema incompleto de ejecuciones parciales

**Contexto:** La migración V11 fallaba con `ERROR: column "tenant_id" does not exist (SQLState 42703)` al intentar aplicarse.

**Problema:** La tabla `sessions` ya existía en la base de datos desde una ejecución anterior de V11 que falló a mitad (Flyway no transaccional o interrupción). La sentencia `CREATE TABLE IF NOT EXISTS sessions (...)` la saltó silenciosamente porque la tabla existía, pero esa tabla estaba incompleta (no tenía la columna `tenant_id`). Cuando Flyway llegó a `CREATE INDEX IF NOT EXISTS idx_sessions_user_tenant ON sessions(user_id, tenant_id)`, PostgreSQL lanzó el error 42703 porque `tenant_id` no existía en la tabla residual.

**Solución / Buena práctica:**
- Para migraciones que crean tablas **nuevas** (no parte de un esquema ya en producción), agregar `DROP TABLE IF EXISTS <tabla> CASCADE;` al inicio del archivo, **antes** de los `CREATE TABLE IF NOT EXISTS`.
- Esto garantiza idempotencia real: si la tabla existe con un esquema incompleto o anterior, se elimina y se recrea correctamente.
- Después de modificar un archivo de migración que Flyway ya registró (aunque sea como fallido), ejecutar `flyway:repair` para recalcular el checksum:
  ```bash
  ./mvnw -pl keygo-supabase flyway:repair \
    -Dflyway.url=$SUPABASE_URL \
    -Dflyway.user=$SUPABASE_USER \
    -Dflyway.password=$SUPABASE_PASSWORD
  ```
- **Regla:** `CREATE TABLE IF NOT EXISTS` solo es seguro cuando el esquema de la tabla no va a cambiar. En migraciones nuevas (desarrollo), preferir `DROP TABLE IF EXISTS ... CASCADE` + `CREATE TABLE`.

**Archivos clave:**
- `keygo-supabase/src/main/resources/db/migration/V11__add_refresh_tokens_and_sessions.sql`

---

### [2026-03-26] CORS en Spring Boot 4: configurar `CorsConfigurationSource` + `http.cors(...)` en `SecurityFilterChain`
**Contexto:** El frontend SPA (`http://localhost:5173`) bloqueaba todas las llamadas a `/api/v1/tenants/{slug}/oauth2/authorize` por error de CORS: `No 'Access-Control-Allow-Origin' header is present on the requested resource`.
**Problema:** `SecurityConfig` no tenía ninguna configuración CORS. Spring Security bloqueaba los preflight `OPTIONS` antes de que llegaran al controller. Sin `http.cors(...)`, Spring Security 6 ignora completamente las políticas CORS del servidor.
**Solución / Buena práctica:**
1. Crear `@ConfigurationProperties("keygo.cors")` (`KeyGoCorsProperties`) con `allowedOrigins`, `allowedMethods`, `allowedHeaders`, `allowCredentials` y `maxAge`.
2. Registrar un `@Bean CorsConfigurationSource` en `SecurityConfig` que aplique la config a `/**`.
3. Habilitar con `http.cors(cors -> cors.configurationSource(corsConfigurationSource))` en el `SecurityFilterChain` — este es el único punto donde Spring Security aplica CORS antes de evaluar el filtro de autenticación.
4. `allowCredentials: true` es indispensable cuando el frontend mantiene `JSESSIONID` entre `GET /oauth2/authorize` y `POST /account/login` (cookie cross-origin).
5. `allowedHeaders: ["*"]` es válido con Spring — refleja los headers del preflight. No aplica la restricción del estándar para `allowCredentials` porque Spring hace el echo correcto.
6. Los tests unitarios de la config pueden hacerse sin Spring context: instanciar `SecurityConfig` directamente + `KeyGoCorsProperties` + `CorsConfigurationSource.getCorsConfiguration(request)`.
**Archivos clave:** `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/security/SecurityConfig.java`, `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/properties/KeyGoCorsProperties.java`, `keygo-run/src/main/resources/application.yml`

---

### [2026-03-26] El agente nunca debe ejecutar comandos `git` directamente

**Contexto:** Al verificar si errores de compilación en `keygo-api` eran pre-existentes o introducidos por cambios propios, se ejecutó `git stash` / `git stash pop` para comparar el estado antes y después.
**Problema:** Las instrucciones del proyecto (`.github/copilot-instructions.md`, `CLAUDE.md`, `AGENTS.md`) prohíben explícitamente que el agente ejecute cualquier comando `git` directamente — no porque cause daño necesariamente, sino porque las operaciones de control de versiones pertenecen al flujo del desarrollador humano, no al agente. Ejecutar `git stash` puede interferir con el estado de trabajo del usuario.
**Solución / Buena práctica:** Para verificar si un error es pre-existente, utilizar únicamente `./mvnw clean test` o revisar el log de compilación anterior. Si se necesita comparar estados del repo, **listar los comandos sugeridos** para que el usuario los ejecute manualmente. Nunca invocar `git stash`, `git commit`, `git push`, `git merge`, `git rebase` ni ningún otro subcomando de git desde el agente.
**Archivos clave:** `.github/copilot-instructions.md` §5, `CLAUDE.md` §Git, `AGENTS.md` §Git

---

### [2026-03-26] Verificar estado de compilación pre-existente con `./mvnw clean` antes de atribuir errores a cambios propios

**Contexto:** Al ejecutar `./mvnw -pl keygo-api test` tras agregar anotaciones Swagger, aparecieron errores de compilación en `GlobalExceptionHandler` (clases no encontradas: `ScopeNotGrantedException`, `NoActiveSigningKeyException`, etc.). Se intentó erróneamente verificar con `git stash`.
**Problema:** Maven puede usar clases compiladas previamente en `target/`. Si el proyecto tiene errores pre-existentes en archivos no tocados por el agente, `mvnw test` (sin `clean`) puede fallar por artefactos desactualizados. Atribuir esos errores a los cambios del agente lleva a buscar la causa en el lugar equivocado.
**Solución / Buena práctica:** Antes de asumir que mis cambios introdujeron un error de compilación, ejecutar `./mvnw clean test -pl <módulo>` para asegurar compilación desde cero. Si el error persiste en `clean`, revisar si el archivo afectado fue modificado en esta sesión. Si no lo fue, el error es pre-existente y no debe bloquearse en él.

---

### [2026-03-26] Documentar Swagger: controllers sin anotaciones `@Tag`/`@Operation` y grupos desactualizados

**Contexto:** Al revisar la coherencia entre `docs/api/OPENAPI.md` y el código de los controllers, se detectó que 5 controllers de auth/OIDC (implementados en fases 5-9) nunca recibieron anotaciones Swagger, y `docs/api/OPENAPI.md` quedó congelado en el estado de la fase 4 (2026-03-22).
**Problema:**
- `AuthorizationController`, `JwksController`, `OidcMetadataController`, `RevocationController`, `UserInfoController` — sin `@Tag`, sin `@Operation`, sin `@ApiResponse`.
- `TenantMembershipController` y `TenantAppRoleController` con tags numéricos (`"5-memberships"`, `"6-roles"`) inconsistentes con el patrón del resto.
- `OpenApiConfig.java` tenía 4 grupos; el grupo `2-tenants` capturaba sin filtrar auth, OIDC, memberships y account/profile.
- `OPENAPI.md` documentaba el esquema de seguridad obsoleto `AdminKeyAuth`/`X-KEYGO-ADMIN` (cambiado a `BearerAuth`/Bearer JWT en fase 5), 21 endpoints en lugar de 33, y swagger-ui.path incorrecto.
**Solución / Buena práctica:**
- Al implementar un controller nuevo, agregar siempre `@Tag` (con nombre descriptivo sin prefijo numérico), `@Operation` y `@ApiResponse` en cada método en la misma sesión. No diferirlo.
- Al agregar grupos en `OpenApiConfig`, usar `pathsToExclude` explícito en el grupo `2-tenants` para que no absorba los nuevos paths por defecto.
- Los controllers de OIDC/JWKS que retornan `Map<String, Object>` (no `BaseResponse`) son correctos por diseño — documentarlo con `@Content(mediaType = "application/json")` sin `@Schema`.
- Los tags de membership/roles se nombraron `"5-memberships"` y `"6-roles"` probablemente para ordenación en Swagger UI; la solución correcta es usar `tags-sorter: alpha` en springdoc y tags descriptivos puros.
**Archivos clave:** `keygo-api/src/main/java/io/cmartinezs/keygo/api/auth/controller/`, `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/OpenApiConfig.java`, `docs/api/OPENAPI.md`
---

