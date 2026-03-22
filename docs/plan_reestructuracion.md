
# Plan: Reestructuración completa de `docs/` — KeyGo Server

El repositorio acumula **más de 55 archivos Markdown** distribuidos en 7 carpetas, con redundancias severas. La propuesta consolida la documentación en **5 categorías temáticas**, elimina archivos históricos ya absorbidos por `CHANGELOG.md`/`AGENTS.registro.md` y crea los gaps faltantes.

---

## 1. Problemas detectados

### 🔴 Redundancias críticas

| Problema | Archivos involucrados |
|---|---|
| **3 docs de arquitectura** con solapamiento del 70%+ | `ARCHITECTURE.md` (raíz), `docs/keygo-server/ARCHITECTURE.md`, `docs/arch/keygo_server_architecture.md` |
| **5 docs sobre el mismo bug Lombok/IntelliJ** | `docs/keygo-server/INTELLIJ_BUILD_FIX.md`, `docs/keygo-server/LOMBOK_INTEGRATION.md`, `docs/keygo-supabase/INTELLIJ_FIX.md`, `docs/keygo-supabase/QUICK_FIX.md`, `docs/keygo-supabase/SOLUTION_SUMMARY.md` |
| **2 guías de integración Supabase** con el mismo contenido | `docs/keygo-supabase/INTEGRATION.md`, `docs/keygo-supabase/SUPABASE_INTEGRATION.md` |
| **2 docs de gestión de entornos** solapados | `docs/keygo-supabase/ENV_QUICK_REFERENCE.md`, `docs/keygo-supabase/ENVIRONMENT_STRATEGY.md` |
| **`DATA_DICTIONARY.md` sin contenido propio** | Solo 290 líneas que indexan `DATA_MODEL.md` y `ENTITY_RELATIONSHIPS.md` |
| **`docs/keygo-server/changes/`** — 11 archivos históricos | Duplican lo que ya está en `CHANGELOG.md` + `AGENTS.registro.md` |

### 🟠 Información desactualizada

| Documento | Problema |
|---|---|
| `docs/keygo-supabase/MIGRATIONS.md` | Solo documenta V1–V3; existen V1–V10 |
| `docs/keygo-supabase/SUMMARY.md` + `INTEGRATION.md` + `SUPABASE_INTEGRATION.md` | Estructura con solo V1–V3; desactualizados |
| `docs/keygo-run/BOOTSTRAP_SECURITY_FILTER.md` | Describe bug T-001 como **"Known Issue" abierto**, pero ya fue corregido el 2026-03-21 |
| `docs/keygo-api/RESPONSE_ENTITY_REFACTORING.md` | Documento de cambio histórico sin valor como referencia actual |
| `docs/keygo-server/ARCHITECTURE.md` | Dice que `keygo-domain` e `keygo-infra` son stubs vacíos; ambos están activos |
| `docs/keygo-supabase/RUNNER_CONFIGURATION.md` | Referencia `KeyGoRunner` como clase principal; fue renombrada a `KeygoApplication` |
| `docs/arch/keygo_server_api_surface.md` | Diseño original del API; no refleja los 17 endpoints actuales |
| `CHANGELOG.md` | Fases 1–6 completadas no están registradas |

### 🟡 Gaps de documentación

| Gap | Impacto |
|---|---|
| No existe `docs/keygo-infra/` | `keygo-infra` tiene JWT signer (Nimbus), JWKS builder, PkceVerifier — sin doc |
| No hay guía de API REST actualizada | `docs/arch/keygo_server_api_surface.md` desactualizado con los 17 endpoints reales |
| No hay doc de OpenAPI/Swagger | Swagger UI funcional sin documentación de cómo usarla |
| `docs/arch/` usa snake_case vs UPPER_CASE del resto | Inconsistencia de naming que confunde la navegación |

---

## 2. Estructura propuesta

```
keygo-server/
│
├── [raíz — sin cambios en archivos de agentes/proceso]
│   ├── AI_CONTEXT.md, AI_CONTEXT.lecciones.md, AI_CONTEXT.propuestas.md
│   ├── AGENTS.md, AGENTS.registro.md
│   ├── INCONSISTENCIAS.md, INCONSISTENCIAS.datos.md
│   ├── ARCHITECTURE.md  ← resumen + enlace a docs/design/
│   ├── ROADMAP.md, CHANGELOG.md, CLAUDE.md
│   └── README.md, SECURITY.md, CONTRIBUTING.md, CODE_OF_CONDUCT.md
│
└── docs/
    ├── README.md  ← ACTUALIZAR: nuevo índice con 5 categorías
    │
    ├── design/                    ← RENOMBRAR desde docs/arch/
    │   ├── ARCHITECTURE.md        ← FUSIONAR los 3 docs de arquitectura
    │   ├── DOMAIN_MODEL.md        ← MOVER keygo_server_domain_model.md
    │   ├── IMPLEMENTATION_PLAN.md ← MOVER keygo_server_implementation_plan.md
    │   ├── API_SURFACE.md         ← MOVER + ACTUALIZAR con 17 endpoints reales
    │   ├── BACKLOG.md             ← MOVER keygo_server_backlog_v_1.md
    │   └── PROJECT_STRUCTURE.md   ← MOVER keygo_server_project_structure.md
    │
    ├── api/                       ← CONSOLIDAR docs/keygo-api/ + docs/keygo-run/ + auth
    │   ├── RESPONSE_CODES.md      ← RENOMBRAR de RESPONSE_CODES_GUIDE.md
    │   ├── AUTH_FLOW.md           ← MOVER de docs/keygo-server/AUTH_FLOW.md
    │   ├── BOOTSTRAP_FILTER.md    ← FUSIONAR BOOTSTRAP_SECURITY_FILTER.md + BOOTSTRAP_PROPERTIES.md + corregir T-001
    │   └── OPENAPI.md             ← CREAR: Swagger UI + springdoc config
    │
    ├── data/                      ← CONSOLIDAR docs/keygo-server/DATA_*.md
    │   ├── DATA_MODEL.md          ← MOVER de docs/keygo-server/DATA_MODEL.md
    │   ├── ENTITY_RELATIONSHIPS.md← MOVER de docs/keygo-server/ENTITY_RELATIONSHIPS.md
    │   └── MIGRATIONS.md          ← REESCRIBIR: V1–V10 completo (reemplaza el desactualizado)
    │
    ├── development/               ← NUEVO: herramientas de desarrollo
    │   ├── CODE_STYLE.md          ← MOVER de docs/keygo-server/CODE_STYLE.md
    │   ├── INTELLIJ_SETUP.md      ← FUSIONAR 7 docs de IntelliJ/Lombok en uno
    │   ├── TEST_STRATEGY.md       ← FUSIONAR TESTING_GUIDE.md + TEST_DEPENDENCIES_STRATEGY.md
    │   └── ENVIRONMENT_SETUP.md   ← FUSIONAR ENV_QUICK_REFERENCE.md + ENVIRONMENT_STRATEGY.md
    │
    └── operations/                ← NUEVO: despliegue y operación
        ├── DOCKER.md              ← MOVER de docs/keygo-server/DOCKER.md
        └── SIGNING_AND_JWKS.md   ← CREAR: JWT signer, JWKS builder, PkceVerifier en keygo-infra
```

---

## 3. Plan de migración

### Eliminar (ruido sin valor actual)

| Archivo a eliminar | Justificación |
|---|---|
| `docs/keygo-server/changes/` (11 archivos) | Historial absorbido en `CHANGELOG.md` + `AGENTS.registro.md` |
| `docs/keygo-supabase/INTEGRATION.md` | Contenido "ya implementado"; absorbido en `AGENTS.md` |
| `docs/keygo-supabase/SUPABASE_INTEGRATION.md` | Duplicado de INTEGRATION.md; ambos desactualizados |
| `docs/keygo-supabase/SUMMARY.md` | Estructura antigua V1–V3 |
| `docs/keygo-server/ARCHITECTURE.md` | Duplicado más desactualizado que el de raíz |
| `docs/keygo-server/DATA_DICTIONARY.md` | Solo índice de 290 líneas sin contenido propio |
| `docs/keygo-api/RESPONSE_ENTITY_REFACTORING.md` | Historial sin valor como referencia |
| `docs/keygo-api/SERVICE_INFO_ENDPOINT.md` | Endpoint ya estable; patrón cubierto en `AGENTS.md` |

### Fusionar (varios → uno)

| Fuentes | Destino | Notas |
|---|---|---|
| `ARCHITECTURE.md` (raíz) + `docs/keygo-server/ARCHITECTURE.md` + `docs/arch/keygo_server_architecture.md` | `docs/design/ARCHITECTURE.md` | Raíz mantiene resumen + enlace |
| 7 docs de IntelliJ/Lombok | `docs/development/INTELLIJ_SETUP.md` | `INTELLIJ_BUILD_FIX.md`, `LOMBOK_INTEGRATION.md`, `INTELLIJ_FIX.md`, `QUICK_FIX.md`, `SOLUTION_SUMMARY.md`, `RUNNER_CONFIGURATION.md`, `INTELLIJ_SETUP.md` |
| `BOOTSTRAP_SECURITY_FILTER.md` + `BOOTSTRAP_PROPERTIES.md` | `docs/api/BOOTSTRAP_FILTER.md` | Actualizar: T-001 ya corregido |
| `TESTING_GUIDE.md` + `TEST_DEPENDENCIES_STRATEGY.md` | `docs/development/TEST_STRATEGY.md` | — |
| `ENV_QUICK_REFERENCE.md` + `ENVIRONMENT_STRATEGY.md` | `docs/development/ENVIRONMENT_SETUP.md` | — |

### Mover y renombrar (sin cambio de contenido)

| Origen | Destino |
|---|---|
| `docs/arch/keygo_server_domain_model.md` | `docs/design/DOMAIN_MODEL.md` |
| `docs/arch/keygo_server_implementation_plan.md` | `docs/design/IMPLEMENTATION_PLAN.md` |
| `docs/arch/keygo_server_api_surface.md` | `docs/design/API_SURFACE.md` (+ nota de estado) |
| `docs/arch/keygo_server_backlog_v_1.md` | `docs/design/BACKLOG.md` |
| `docs/arch/keygo_server_project_structure.md` | `docs/design/PROJECT_STRUCTURE.md` |
| `docs/keygo-api/RESPONSE_CODES_GUIDE.md` | `docs/api/RESPONSE_CODES.md` |
| `docs/keygo-server/AUTH_FLOW.md` | `docs/api/AUTH_FLOW.md` |
| `docs/keygo-server/DATA_MODEL.md` | `docs/data/DATA_MODEL.md` |
| `docs/keygo-server/ENTITY_RELATIONSHIPS.md` | `docs/data/ENTITY_RELATIONSHIPS.md` |
| `docs/keygo-server/CODE_STYLE.md` | `docs/development/CODE_STYLE.md` |
| `docs/keygo-server/DOCKER.md` | `docs/operations/DOCKER.md` |

### Crear nuevos (gaps actuales)

| Archivo nuevo | Contenido |
|---|---|
| `docs/api/OPENAPI.md` | Cómo usar Swagger UI, springdoc config, SecurityScheme, ejemplos de uso |
| `docs/data/MIGRATIONS.md` | Guía completa de migraciones V1–V10 + convenciones para futuras |
| `docs/operations/SIGNING_AND_JWKS.md` | JWT signer (Nimbus RSA), JWKS builder, PkceVerifier — módulo `keygo-infra` |
| `docs/README.md` | Actualizar índice con la nueva estructura de 5 categorías |

---

## 4. Prioridad de ejecución

| # | Acción | Urgencia | Por qué |
|---|---|---|---|
| **P0** ✅ | Eliminar `docs/keygo-server/changes/` (12 archivos) | 🔴 Alta | Ruido puro; ninguna referencia activa apunta a estos docs — **aplicado 2026-03-22** |
| **P0** ✅ | Eliminar `INTEGRATION.md`, `SUPABASE_INTEGRATION.md`, `SUMMARY.md` de keygo-supabase | 🔴 Alta | 3 docs desactualizados del mismo tema — **aplicado 2026-03-22** |
| **P0** ✅ | Eliminar `docs/keygo-server/ARCHITECTURE.md` | 🔴 Alta | Duplicado desactualizado; dice que keygo-infra es stub vacío — **aplicado 2026-03-22** |
| **P0** ✅ | Actualizar `BOOTSTRAP_SECURITY_FILTER.md` → corregir T-001 como **cerrado** | 🔴 Alta | Un agente creerá que el bug está abierto — **aplicado 2026-03-22** |
| **P1** ✅ | Fusionar 7 docs de IntelliJ/Lombok → `docs/development/INTELLIJ_SETUP.md` | 🟠 Media | El problema más repetido; 7 archivos para 1 solo tema — **aplicado 2026-03-22** |
| **P1** ✅ | Mover + renombrar `docs/arch/*.md` → `docs/design/` | 🟠 Media | Normaliza naming convention; sin pérdida de contenido — **aplicado 2026-03-22** |
| **P1** ✅ | Fusionar 3 docs de arquitectura → `docs/design/ARCHITECTURE.md` | 🟠 Media | Elimina confusión sobre cuál es el canónico — **aplicado 2026-03-22** |
| **P1** ✅ | Fusionar `BOOTSTRAP_SECURITY_FILTER.md` + `BOOTSTRAP_PROPERTIES.md` → `docs/api/BOOTSTRAP_FILTER.md` | 🟠 Media | Dos docs del mismo componente — **aplicado 2026-03-22** |
| **P2** ✅ | Crear `docs/operations/SIGNING_AND_JWKS.md` | 🟡 Normal | `keygo-infra` activo sin documentación; gap real — **aplicado 2026-03-22** |
| **P2** ✅ | Crear `docs/data/MIGRATIONS.md` actualizado (V1–V10) | 🟡 Normal | MIGRATIONS.md anterior solo cubría V1–V3 — **aplicado 2026-03-22** |
| **P2** ✅ | Crear `docs/api/OPENAPI.md` | 🟡 Normal | Swagger UI funcional sin documentación — **aplicado 2026-03-22** |
| **P3** ✅ | Actualizar `CHANGELOG.md` con Fases 1–6 | 🟢 Baja | `AGENTS.registro.md` ya cubre el historial — **aplicado 2026-03-22** |
| **P3** ✅ | Actualizar `docs/README.md` con nuevo índice | 🟢 Baja | Útil pero no bloquea nada — **aplicado 2026-03-22** |

---

## 5. Resultado esperado

| Métrica | Antes | Después |
|---|---|---|
| Total archivos .md en docs/ | ~55 | ~20 |
| Directorios temáticos | 4 confusos (arch, keygo-api, keygo-run, keygo-server, keygo-supabase) | 5 claros (design, api, data, development, operations) |
| Docs de IntelliJ/Lombok | 7 archivos | 1 archivo |
| Docs de arquitectura | 3 solapados | 1 canónico |
| Docs de Supabase desactualizados | 4 | 0 |
| Gaps documentados | 0 | 3 nuevos (OPENAPI, MIGRATIONS, SIGNING_AND_JWKS) |
