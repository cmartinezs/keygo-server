# Changelog

**English:** All notable changes to this project will be documented in this file.

**Español:** Todos los cambios notables de este proyecto serán documentados en este archivo.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto sigue [Semantic Versioning](https://semver.org/lang/es/).

## [Unreleased]

### Added / Añadido — Platform Account Profile Endpoints (2026-04-14)
- **EN:** `keygo-app`: `GetPlatformUserProfileCommand`, `UpdatePlatformUserProfileCommand`; use cases `GetPlatformUserProfileUseCase`, `UpdatePlatformUserProfileUseCase` for self-service platform user profile management
- **ES:** `keygo-app`: `GetPlatformUserProfileCommand`, `UpdatePlatformUserProfileCommand`; casos de uso `GetPlatformUserProfileUseCase`, `UpdatePlatformUserProfileUseCase` para gestión self-service de perfil de usuarios de plataforma
- **EN:** `keygo-api`: endpoints `GET /api/v1/platform/account/profile`, `PATCH /api/v1/platform/account/profile` in `PlatformAccountController` — reuses existing `UserProfileData` and `UserProfileResult`
- **ES:** `keygo-api`: endpoints `GET /api/v1/platform/account/profile`, `PATCH /api/v1/platform/account/profile` en `PlatformAccountController` — reutiliza `UserProfileData` y `UserProfileResult`
- **EN:** `keygo-run`: 2 new `@Bean` factories in `ApplicationConfig` for dependency injection (T-153 resolution)
- **ES:** `keygo-run`: 2 nuevos factories `@Bean` en `ApplicationConfig` para inyección de dependencias (resolución de T-153)
- **EN:** Documentation: Task registered in workflow as completed (T-153); feedback entry BE-007 for UI integration
- **ES:** Documentación: Tarea registrada en workflow como completada (T-153); entrada de feedback BE-007 para integración UI
- **EN:** Reuses existing response codes: `USER_PROFILE_RETRIEVED`, `USER_PROFILE_UPDATED`; platform users return `tenantId`, `birthdate`, `website` as `null`
- **ES:** Reutiliza códigos de respuesta existentes: `USER_PROFILE_RETRIEVED`, `USER_PROFILE_UPDATED`; usuarios de plataforma retornan `tenantId`, `birthdate`, `website` como `null`

### Added / Añadido — Fase 6: Firma de tokens y metadata OIDC (2026-03-22)
- **EN:** `keygo-domain`: `SigningKey`, `SigningKeyId`, `SigningKeyStatus`, `SigningKeyAlgorithm`, `NoActiveSigningKeyException`
- **ES:** `keygo-domain`: `SigningKey`, `SigningKeyId`, `SigningKeyStatus`, `SigningKeyAlgorithm`, `NoActiveSigningKeyException`
- **EN:** `keygo-app`: ports `SigningKeyRepositoryPort`, `TokenSignerPort`, `TokenClaimsFactoryPort`, `JwksBuilderPort`; use cases `IssueTokensUseCase`, `GetJwksUseCase`, `GetOidcConfigurationUseCase`
- **ES:** `keygo-app`: puertos `SigningKeyRepositoryPort`, `TokenSignerPort`, `TokenClaimsFactoryPort`, `JwksBuilderPort`; casos de uso `IssueTokensUseCase`, `GetJwksUseCase`, `GetOidcConfigurationUseCase`
- **EN:** `keygo-infra`: `RsaJwtTokenSigner` (Nimbus JOSE+JWT, RS256/RS384/RS512), `StandardTokenClaimsFactory` (RFC 9068 + OIDC `at_hash`), `JwkSetBuilder` (RFC 7517)
- **ES:** `keygo-infra`: `RsaJwtTokenSigner` (Nimbus JOSE+JWT, RS256/RS384/RS512), `StandardTokenClaimsFactory` (RFC 9068 + OIDC `at_hash`), `JwkSetBuilder` (RFC 7517)
- **EN:** `keygo-supabase`: `SigningKeyEntity`, `SigningKeyJpaRepository`, mapper, adapter, migration `V9__add_signing_keys.sql`
- **ES:** `keygo-supabase`: `SigningKeyEntity`, `SigningKeyJpaRepository`, mapper, adapter, migración `V9__add_signing_keys.sql`
- **EN:** `keygo-api`: `JwksController` (`GET /.well-known/jwks.json`), `OidcMetadataController` (`GET /.well-known/openid-configuration`)
- **ES:** `keygo-api`: `JwksController` (`GET /.well-known/jwks.json`), `OidcMetadataController` (`GET /.well-known/openid-configuration`)
- **EN:** `keygo-run`: `SigningKeyBootstrapService` (auto-generates RSA 2048 keypair on startup when no ACTIVE key, profile `supabase` only)
- **ES:** `keygo-run`: `SigningKeyBootstrapService` (auto-genera par RSA 2048 al arrancar sin clave ACTIVE, solo perfil `supabase`)
- **EN:** 29 new unit tests — total: **299 tests**; Postman: folder `🔑 OIDC & JWKS` with 2 requests (**25 total**)
- **ES:** 29 nuevos tests unitarios — total: **299 tests**; Postman: carpeta `🔑 OIDC & JWKS` con 2 requests (**25 en total**)

### Added / Añadido — Fase 5: Núcleo OAuth2/OIDC Authorization Code + PKCE (2026-03-22)
- **EN:** `keygo-domain`: `AuthorizationCode`, `AuthorizationCodeId`, `AuthorizationCodeStatus`, `CodeChallenge`, `ScopeSet`; exceptions `InvalidAuthorizationCodeException`, `AuthorizationCodeExpiredException`, `InvalidPkceVerificationException`, `ScopeNotGrantedException`
- **ES:** `keygo-domain`: `AuthorizationCode`, `AuthorizationCodeId`, `AuthorizationCodeStatus`, `CodeChallenge`, `ScopeSet`; excepciones correspondientes
- **EN:** `keygo-app`: ports `AuthorizationCodeRepositoryPort`, `ClockPort`; 4 commands, 3 results, 4 use cases (`InitiateAuthorizationUseCase`, `AuthenticateUserForAuthorizationUseCase`, `IssueAuthorizationCodeUseCase`, `ExchangeAuthorizationCodeUseCase`)
- **ES:** `keygo-app`: puertos `AuthorizationCodeRepositoryPort`, `ClockPort`; 4 comandos, 3 results, 4 casos de uso del flujo OAuth2
- **EN:** `keygo-infra`: `PkceVerifier` (S256 and plain methods, RFC 7636)
- **ES:** `keygo-infra`: `PkceVerifier` (métodos S256 y plain, RFC 7636)
- **EN:** `keygo-supabase`: `AuthorizationCodeEntity`, repository, mapper, adapter, migration `V8__add_oauth_authorization_codes.sql`
- **ES:** `keygo-supabase`: `AuthorizationCodeEntity`, repositorio, mapper, adapter, migración `V8__add_oauth_authorization_codes.sql`
- **EN:** `keygo-api`: `AuthorizationController` — 3 endpoints: `GET /oauth2/authorize`, `POST /account/login`, `POST /oauth2/token`; 4 new `ResponseCode`
- **ES:** `keygo-api`: `AuthorizationController` — 3 endpoints: `GET /oauth2/authorize`, `POST /account/login`, `POST /oauth2/token`; 4 nuevos `ResponseCode`
- **EN:** `keygo-run`: `SystemClockProvider`; 6 new `@Bean` factories
- **ES:** `keygo-run`: `SystemClockProvider`; 6 nuevos factories `@Bean`
- **EN:** ~60 new unit tests — total: **270+ tests**; Postman: folder `🔐 OAuth2 Authorization` with 3 requests (**23 total**)
- **ES:** ~60 nuevos tests — total: **270+ tests**; Postman: carpeta `🔐 OAuth2 Authorization` con 3 requests (**23 en total**)

### Added / Añadido — Fase 4: Memberships y roles por app (2026-03-21)
- **EN:** `keygo-domain`: `Membership`, `AppRole`, `MembershipRole`, `RoleCode`; membership exceptions
- **ES:** `keygo-domain`: `Membership`, `AppRole`, `MembershipRole`, `RoleCode`; excepciones de membership
- **EN:** `keygo-app`: ports `MembershipRepositoryPort`, `AppRoleRepositoryPort`; 4 use cases
- **ES:** `keygo-app`: puertos `MembershipRepositoryPort`, `AppRoleRepositoryPort`; 4 casos de uso
- **EN:** `keygo-supabase`: `AppRoleEntity`, `MembershipEntity`, repositories, adapters, migration `V7__add_memberships.sql` (+ `V10__rename_membership_tables_to_plural.sql`)
- **ES:** `keygo-supabase`: `AppRoleEntity`, `MembershipEntity`, repositorios, adapters, migraciones V7 y V10
- **EN:** `keygo-api`: `TenantMembershipController` (3 endpoints), `TenantAppRoleController` (2 endpoints); 6 new `ResponseCode`
- **ES:** `keygo-api`: `TenantMembershipController` (3 endpoints), `TenantAppRoleController` (2 endpoints); 6 nuevos `ResponseCode`
- **EN:** ~45 new unit tests — total: **210+ tests**; Postman: folders `📋 Memberships` + `👥 Roles` with 5 requests
- **ES:** ~45 nuevos tests — total: **210+ tests**; Postman: carpetas `📋 Memberships` + `👥 Roles` con 5 requests

### Added / Añadido — Fase 3: User identity per tenant (2026-03-21)
- **EN:** `keygo-domain`: `TenantUser`, `EmailAddress`, `PasswordHash`, `Username`; exceptions `UserNotFoundException`, `UserAlreadyExistsException`
- **ES:** `keygo-domain`: `TenantUser`, `EmailAddress`, `PasswordHash`, `Username`; excepciones de usuario
- **EN:** `keygo-app`: ports `TenantUserRepositoryPort`, `PasswordHasherPort`; 6 use cases (create, list, get, update, validate credentials, reset password)
- **ES:** `keygo-app`: puertos `TenantUserRepositoryPort`, `PasswordHasherPort`; 6 casos de uso de usuario
- **EN:** `keygo-supabase`: `TenantUserEntity`, repository, mapper, adapter, migration `V6__add_tenant_users.sql`
- **ES:** `keygo-supabase`: `TenantUserEntity`, repositorio, mapper, adapter, migración `V6__add_tenant_users.sql`
- **EN:** `keygo-api`: `TenantUserController` — 6 endpoints; 3 new `ResponseCode`; `BCryptPasswordHasher`
- **ES:** `keygo-api`: `TenantUserController` — 6 endpoints; 3 nuevos `ResponseCode`; `BCryptPasswordHasher`
- **EN:** ~50 new unit tests — total: **165+ tests**; Postman: folder `👤 Users` with 6 requests (**17 total**)
- **ES:** ~50 nuevos tests — total: **165+ tests**; Postman: carpeta `👤 Users` con 6 requests (**17 en total**)

### Added / Añadido — Fase 2: Client app model (2026-03-21)
- **EN:** `keygo-domain`: `ClientApp`, `ClientId`, `ClientType`, `ClientSecret`; client app exceptions
- **ES:** `keygo-domain`: `ClientApp`, `ClientId`, `ClientType`, `ClientSecret`; excepciones de client app
- **EN:** `keygo-app`: port `ClientAppRepositoryPort`; 5 use cases (create, get, list, update, rotate secret)
- **ES:** `keygo-app`: puerto `ClientAppRepositoryPort`; 5 casos de uso de client app
- **EN:** `keygo-supabase`: `ClientAppEntity` + `ClientRedirectUriEntity`, `ClientAllowedGrantEntity`, `ClientAllowedScopeEntity`; migration `V5__add_client_apps.sql`
- **ES:** `keygo-supabase`: `ClientAppEntity` y entidades hijo (redirect URIs, grants, scopes); migración `V5__add_client_apps.sql`
- **EN:** `keygo-api`: `TenantClientAppController` — 5 endpoints + `rotate-secret`; 5 new `ResponseCode`
- **ES:** `keygo-api`: `TenantClientAppController` — 5 endpoints + `rotate-secret`; 5 nuevos `ResponseCode`
- **EN:** Postman: folder `🔐 Client Apps` with 6 requests (**11 total**)
- **ES:** Postman: carpeta `🔐 Client Apps` con 6 requests (**11 en total**)

### Added / Añadido — Fase 1: Multitenancy (2026-03-21)
- **EN:** `keygo-domain`: `Tenant`, `TenantId`, `TenantSlug`, `TenantStatus`, `TenantNotFoundException`
- **ES:** `keygo-domain`: `Tenant`, `TenantId`, `TenantSlug`, `TenantStatus`, `TenantNotFoundException`
- **EN:** `keygo-app`: port `TenantRepositoryPort`; use cases `CreateTenantUseCase`, `GetTenantBySlugUseCase`, `SuspendTenantUseCase`; `TenantContextHolder` (ThreadLocal, no Spring)
- **ES:** `keygo-app`: puerto `TenantRepositoryPort`; casos de uso Create/Get/Suspend; `TenantContextHolder`
- **EN:** `keygo-supabase`: `TenantEntity`, `TenantJpaRepository`, migration `V4__add_tenants.sql`
- **ES:** `keygo-supabase`: `TenantEntity`, `TenantJpaRepository`, migración `V4__add_tenants.sql`
- **EN:** `keygo-api`: `TenantController` — 3 endpoints (`POST /tenants`, `GET /tenants/{slug}`, `PUT /tenants/{slug}/suspend`)
- **ES:** `keygo-api`: `TenantController` — 3 endpoints de gestión de tenants
- **EN:** `keygo-run`: `TenantResolutionFilter` (header `X-Tenant-Slug`)
- **ES:** `keygo-run`: `TenantResolutionFilter` (header `X-Tenant-Slug`)
- **EN:** 39 new unit tests; Postman: folder `🏢 Tenants` with 3 requests
- **ES:** 39 nuevos tests unitarios; Postman: carpeta `🏢 Tenants` con 3 requests

### Added / Añadido — Fase 0: Hardening estructural (2026-03-21)
- **EN:** Maven Enforcer plugin (versión convergence + dependency management rules)
- **ES:** Plugin Maven Enforcer (convergencia de versiones + reglas de dependencias)
- **EN:** JaCoCo coverage check (60% instruction threshold) with `report-aggregate` in `keygo-run`
- **ES:** JaCoCo con umbral 60% instrucciones y `report-aggregate` en `keygo-run`
- **EN:** SpringDoc OpenAPI 3.0.1 (Swagger UI at `/keygo-server/swagger-ui/index.html`); 4 API groups; `AdminKeyAuth` security scheme
- **ES:** SpringDoc OpenAPI 3.0.1 (Swagger UI en `/keygo-server/swagger-ui/index.html`); 4 grupos de API; esquema de seguridad `AdminKeyAuth`
- **EN:** `keygo-infra` module activated (was stub); Nimbus JOSE+JWT dependency added
- **ES:** Módulo `keygo-infra` activado (era stub); dependencia Nimbus JOSE+JWT agregada
- **EN:** Sub-documents: `AI_CONTEXT.lecciones.md`, `AI_CONTEXT.propuestas.md`, `AGENTS.registro.md`, `INCONSISTENCIAS.md`, `INCONSISTENCIAS.datos.md`
- **ES:** Sub-documentos: `AI_CONTEXT.lecciones.md`, `AI_CONTEXT.propuestas.md`, `AGENTS.registro.md`, `INCONSISTENCIAS.md`, `INCONSISTENCIAS.datos.md`

### Fixed / Corregido — 2026-03-21
- **EN:** `BootstrapAdminKeyFilter` T-001: replaced `request.getRequestURI()` with `request.getServletPath()` — filter now works correctly with `context-path=/keygo-server`
- **ES:** `BootstrapAdminKeyFilter` T-001: reemplazado `request.getRequestURI()` por `request.getServletPath()` — el filtro funciona correctamente con `context-path=/keygo-server`
- **EN:** Membership table names changed from singular to plural vía `V10__rename_membership_tables_to_plural.sql` (`app_role`→`app_roles`, `membership`→`memberships`, `membership_role`→`membership_roles`)
- **ES:** Nombres de tablas de membership corregidos de singular a plural vía migración V10

### Changed / Cambiado — 2026-03-22 docs restructuring
- **EN:** `docs/` restructured from 7 legacy folders (~55 files) to 5 thematic categories (~20 files): `design/`, `api/`, `data/`, `development/`, `operations/`
- **ES:** `docs/` reestructurado de 7 carpetas legacy (~55 archivos) a 5 categorías temáticas (~20 archivos): `design/`, `api/`, `data/`, `development/`, `operations/`
- **EN:** `ARCHITECTURE.md` (root) converted to quick-reference summary; full doc moved to `docs/design/ARCHITECTURE.md`
- **ES:** `ARCHITECTURE.md` (raíz) convertido a resumen de referencia rápida; doc completo en `docs/design/ARCHITECTURE.md`

### Added / Añadido
- **EN:** `AI_CONTEXT.md` — compact context file for Copilot/Claude agents
- **ES:** `AI_CONTEXT.md` — archivo de contexto compacto para agentes Copilot/Claude
- **EN:** `.github/copilot-instructions.md` and `.github/prompts/` for agent guidance
- **ES:** `.github/copilot-instructions.md` y `.github/prompts/` para orientación de agentes
- **EN:** `ARCHITECTURE.md` (root) — operational architecture with Mermaid diagrams, flows and CI/CD proposal
- **ES:** `ARCHITECTURE.md` (raíz) — arquitectura operacional con diagramas Mermaid, flujos y propuesta CI/CD
- **EN:** `CLAUDE.md` — rules for AI coding agents
- **ES:** `CLAUDE.md` — reglas para agentes de codificación AI
- **EN:** Unit tests: 79 total (keygo-api: 33, keygo-app: 3, keygo-run: 43)
- **ES:** Tests unitarios: 79 en total (keygo-api: 33, keygo-app: 3, keygo-run: 43)
- **EN:** Bilingual documentation (English/Spanish) across all main docs
- **ES:** Documentación bilingüe (Inglés/Español) en todos los docs principales

### Changed / Cambiado
- **EN:** `ServiceInfoController` refactored to return `ResponseEntity<BaseResponse<ServiceInfoData>>`
- **ES:** `ServiceInfoController` refactorizado para retornar `ResponseEntity<BaseResponse<ServiceInfoData>>`
- **EN:** `ResponseCode` enum with business-specific codes (replaces generic `SUCCESS`/`CREATED`)
- **ES:** Enum `ResponseCode` con códigos específicos de negocio (reemplaza genéricos `SUCCESS`/`CREATED`)
- **EN:** `BootstrapAdminKeyFilter` improved; known issue with `getRequestURI()` vs `getServletPath()` documented
- **ES:** `BootstrapAdminKeyFilter` mejorado; bug conocido con `getRequestURI()` vs `getServletPath()` documentado
- **EN:** `application.yml` properties unified under `keygo.*` prefix with Maven resource filtering
- **ES:** Propiedades de `application.yml` unificadas bajo el prefijo `keygo.*` con filtrado de recursos Maven
- **EN:** Lombok versión aligned with Spring Boot parent (no hardcoded versión in annotation processor paths)
- **ES:** Versión de Lombok alineada con Spring Boot parent (sin versión hardcodeada en annotation processor paths)

### Fixed / Corregido
- **EN:** Flyway compatibility with PostgreSQL 17 (added `flyway-database-postgresql` dependency)
- **ES:** Compatibilidad de Flyway con PostgreSQL 17 (agregada dependencia `flyway-database-postgresql`)
- **EN:** Shell scripts portability (`#!/usr/bin/env bash`, removed bash-specific syntax)
- **ES:** Portabilidad de scripts de shell (`#!/usr/bin/env bash`, eliminada sintaxis específica de bash)

## [1.0-SNAPSHOT] - 2026-01-11

### Added / Añadido
- **EN:** KeyGo Server project initialization
- **ES:** Inicialización del proyecto KeyGo Server
- **EN:** Modules: common, domain, app, infra, api, run, bom
- **ES:** Módulos: common, domain, app, infra, api, run, bom
- **EN:** Base configuration for Java 21
- **ES:** Configuración base para Java 21

---

[Unreleased]: https://github.com/cmartinezs/keygo-server/compare/v1.0-SNAPSHOT...HEAD
[1.0-SNAPSHOT]: https://github.com/cmartinezs/keygo-server/releases/tag/v1.0-SNAPSHOT

