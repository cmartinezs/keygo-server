## Problema

`web/` sigue siendo una referencia útil pero todavía demasiado delgada frente a `../keygo-docs`. Hoy cubre bien una introducción para integradores/extensores, pero faltan superficies técnicas completas sobre contratos, modelo de datos, versionado, testing, despliegue, operaciones, observabilidad y glosario.

## Decisión de alcance

Sesgo confirmado: **balanceado**. El sitio debe servir tanto a:

1. **Integradores** que consumen KeyGo como IdP / IAM backend.
2. **Extensores** que modifican o amplían `keygo-server`.

La meta no es replicar `keygo-docs`, sino construir una **capa web curada**, navegable y técnica, con páginas cortas y de alta señal.

## Diagnóstico actual

### Cobertura actual en `web/`

- **Integrators:** `auth`, `endpoints`, `tenant-scope`, `errors`
- **Extenders:** `architecture`, `modules`, `contexts`, `patterns`, `security`, `adrs`

### Vacíos principales detectados

- **Integradores**
  - Versionado y compatibilidad de API
  - Discovery / registration con más detalle operativo
  - Contratos de respuesta y convenciones API más completos
  - Modelo de datos conceptual útil para integrar (sesiones, tokens, memberships, apps)
  - Checklist de observabilidad/troubleshooting del cliente integrador

- **Extensores**
  - Database schema / ERD / invariantes
  - Validation strategy y exception mapping
  - Coding standards / naming / boundaries
  - Testing strategy por capa y por contexto
  - Deployment / release / rollback
  - Operations / incident handling / SLAs multi-tenant
  - Monitoring / alerts / dashboards / PromQL examples
  - Glossary técnico del stack

## Arquitectura de información propuesta

### Mantener páginas actuales y ampliarlas

1. **`overview.html`**
   - Agregar mapa de secciones técnicas del sitio
   - Resumen por perfiles: Integrator / Extender

2. **`integrators/endpoints.html`**
   - Agregar convenciones de versionado
   - Agregar notas de compatibilidad / deprecación

3. **`integrators/errors.html`**
   - Expandir taxonomy de errores
   - Agregar tabla de mapping origin/layer/status

4. **`extenders/patterns.html`**
   - Agregar validation strategy
   - Agregar anti-patterns frecuentes

5. **`extenders/security.html`**
   - Expandir rutas públicas/protegidas
   - Agregar matriz de scopes y claims

### Nuevas páginas para Integrators

1. **`integrators/api-conventions.html`**
   - Envelope, paginación, snake_case, headers, correlation IDs
   - Fuente: `api-reference.md`, `contracts/frontend-contracts.md`, `validation-strategy.md`

2. **`integrators/versioning.html`**
   - `/api/v1`, compatibilidad, deprecación, breaking vs non-breaking
   - Fuente: `api-versioning-strategy.md`

3. **`integrators/session-model.html`**
   - platform_session, oauth_session, authorization_code, refresh_token, replay detection
   - Fuente: `oauth2-oidc-contract.md`, `entities.md`, `data-flows.md`

4. **`integrators/observability.html`**
   - Qué mirar desde un cliente: traceId, health, Prometheus, fallas típicas
   - Fuente: `observability.md`, `10-monitoring/*`, `09-operations/*`

### Nuevas páginas para Extenders

1. **`extenders/data-model.html`**
   - ERD curado por dominios
   - tablas críticas y fronteras multi-tenant
   - Fuente: `database-schema.md`, `04-data-model/entities.md`, `relationships.md`

2. **`extenders/validation.html`**
   - HTTP vs Domain vs Use Case validation
   - Exception mapping y ejemplos
   - Fuente: `validation-strategy.md`

3. **`extenders/testing.html`**
   - Pirámide, unit/integration/smoke/security testing
   - Qué probar por bounded context
   - Fuente: `07-testing/README.md` + documentos asociados

4. **`extenders/deployment.html`**
   - ambientes, CI/CD, blue-green, canary, feature flags, rollback
   - Fuente: `08-deployment/README.md` + documentos asociados

5. **`extenders/operations.html`**
   - runbook resumido, incident isolation, support/SLA
   - Fuente: `09-operations/README.md` + documentos asociados

6. **`extenders/monitoring.html`**
   - métricas, alertas, dashboards, ejemplos PromQL por contexto
   - Fuente: `10-monitoring/README.md` + documentos asociados

7. **`extenders/glossary.html`**
   - Jackson 3, JWKS, MDC, ACL, JPA Specifications, Flyway, etc.
   - Fuente: `glossary-technical.md`

## Cambios estructurales requeridos

### Navegación

Actualizar:

- `web/shared/nav.js`
- `web/shared/icons.js`
- `web/shared/i18n.js`

Para soportar nuevas entradas de navegación y orden prev/next.

### Componentes compartidos

Probables mejoras en:

- `web/shared/layout.js`
- `web/shared/keygo.css`
- `web/shared/code-data.js`

Para habilitar:

- tablas técnicas más densas
- callouts operativos
- bloques de checklist
- snippets adicionales
- quizás diagramas Mermaid reutilizables

## Fases propuestas

### Fase 1 — Contratos y modelos base

Objetivo: cerrar vacíos más transversales.

1. `integrators/api-conventions.html`
2. `integrators/versioning.html`
3. `integrators/session-model.html`
4. `extenders/data-model.html`
5. `extenders/validation.html`

### Fase 2 — Operación y calidad

Objetivo: dar profundidad a quienes extienden y operan.

1. `extenders/testing.html`
2. `extenders/deployment.html`
3. `extenders/operations.html`
4. `extenders/monitoring.html`

### Fase 3 — Curaduría y onboarding técnico

Objetivo: facilitar entrada y navegación.

1. `extenders/glossary.html`
2. ampliar `overview.html`
3. ampliar `endpoints.html`, `errors.html`, `patterns.html`, `security.html`
4. ajustar iconografía y homepage para reflejar nuevas secciones

## Criterios para cada página nueva

Cada página debería incluir:

1. **Qué problema resuelve**
2. **Contrato / modelo / flujo principal**
3. **1-2 tablas de referencia**
4. **1 snippet útil o diagrama**
5. **Notas multi-tenant cuando apliquen**
6. **Enlaces internos hacia páginas relacionadas**

## Fuentes prioritarias

### Prioridad alta

- `06-development/oauth2-oidc-contract.md`
- `06-development/authorization-patterns.md`
- `06-development/api-versioning-strategy.md`
- `06-development/database-schema.md`
- `06-development/bootstrap-filter-routes.md`
- `06-development/validation-strategy.md`
- `06-development/observability.md`
- `06-development/glossary-technical.md`
- `04-data-model/entities.md`
- `04-data-model/data-flows.md`

### Prioridad media

- `07-testing/*`
- `08-deployment/*`
- `09-operations/*`
- `10-monitoring/*`

## Riesgos / decisiones a revisar antes de implementar

1. **Volumen de navegación**: si agregamos muchas páginas, quizá convenga agrupar subsecciones o introducir labels/dividers adicionales.
2. **Profundidad por página**: preferir páginas cortas con cross-links, no documentos largos tipo manual.
3. **Orden de implementación**: fase 1 primero; si se hace todo junto, crece demasiado el diff en `i18n.js` y `nav.js`.

## Siguiente implementación recomendada

Comenzar por **Fase 1**, porque entrega el mayor valor técnico inmediato para ambos perfiles y establece las convenciones reutilizables para el resto del sitio.
