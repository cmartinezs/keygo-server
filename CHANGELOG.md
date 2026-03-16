# Changelog

**English:** All notable changes to this project will be documented in this file.

**Español:** Todos los cambios notables de este proyecto serán documentados en este archivo.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/).

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto sigue [Semantic Versioning](https://semver.org/lang/es/).

## [Unreleased]

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
- **EN:** Lombok version aligned with Spring Boot parent (no hardcoded version in annotation processor paths)
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

