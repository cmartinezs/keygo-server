# Plan de Documentación 2026 — Revisión y Mejora

**Fecha:** 2026-04-09  
**Estado:** Activo — implementación incremental  
**Responsable:** AI Agent + Equipo

---

## Resumen Ejecutivo

El plan archivado (2026-04-05) logró consolidar la estructura documental canónica en `docs/README.md` y las fuentes de verdad por categoría. Sin embargo, se identifican **13 deficiencias críticas** en cobertura, completitud y navegabilidad que afectan la onboarding de nuevos desarrolladores y la mantenibilidad del proyecto.

Este documento propone un **roadmap de corto plazo (Sprint 1-3)** para cerrar esos gaps sin duplicar la política en CLAUDE.md.

---

## Deficiencias Identificadas

### 1. **Consolidación incompleta de RFCs en documentos canónicos**

**Impacto:** RFCs diseñadas en `docs/rfc/*/` no migran a fuentes de verdad (`docs/design/*`, `ARCHITECTURE.md`).

| RFC | Estado | Observación |
|---|---|---|
| `restructure-multitenant/*` (10 docs) | 🔲 Huérfana | Contrato OAuth2/multi-dominio/hosted-login sin absorber |
| `account-ui-proposal/*` (5 docs) | 🔲 Parcial | Backend feedback sin integrar a contratos API |
| `t108-geoip-sessions/*` | 🔲 Pendiente | T-108 no implementada aún; RFC huérfana |
| `billing-contractor-refactor/*` (8 docs) | ✅ Parcial | T-124 completada; documentación puede consolidarse |
| `incomplete-sections/` | 🔴 Huérfana | 2 docs sin contexto ni estado |

**Deficiencia:** No existe un **proceso de absorción de RFC → canon** ni checklist.

**Impacto en usuario:** Quién estudia el proyecto encuentra múltiples versiones de verdad en RFC sin saber cuál está activa.

---

### 2. **Falta de catálogo consolidado de errores (ErrorData + ResponseCode)**

**Impacto:** Desarrolladores no saben qué codes retornar en cada endpoint, ni qué textos mostrar.

**Estado actual:**
- `ResponseCode` es un enum en `keygo-api` sin documentación externa
- `ErrorData` estructura en código sin schema humanizado
- `i18n` parcialmente implementada (T-120–T-123 ✅ pero sin doc de uso)
- No existe `docs/design/ERROR_CATALOG.md`

**Deficiencia:** Un desarrollador que agrega endpoint no sabe:
- Qué `ResponseCode` es apropiado para cada caso
- Cómo documentar en OpenAPI el catálogo de errores retornables
- Cuáles campos de `ErrorData` son obligatorios en cada contexto
- Cómo usar el `MessageSource` de i18n

---

### 3. **Guía incompleta de integración frontend (FRONTEND_DEVELOPER_GUIDE.md)**

**Impacto:** Frontend debe hacer reverse-engineering del OpenAPI.

**Deficiencias identificadas:**
- Sección §14 (inventario de endpoints) desactualizada (última revisión 2026-04-03)
- No documenta OAuth2 flow end-to-end con ejemplos de código
- No incluye patrones de error handling por ResponseCode
- No documenta `Accept-Language` + locale resolution
- No explica autenticación de `TENANT_USER` vs `ADMIN_TENANT` en endpoints

---

### 4. **Falta de guía de debugging / troubleshooting**

**Impacto:** Nuevo desarrollador con error desconocido no sabe dónde empezar.

**Inexistente:**
- `docs/development/DEBUGGING.md`
- Matriz de logs esperados por funcionalidad
- Cómo leer logs de migración fallida
- Cómo diagnosticar problemas de multi-tenancy
- Cómo activar DEBUG en Spring Data / JPA queries

---

### 5. **Documentación de patrones y anti-patterns dispersa**

**Impacto:** Nuevo código repite errores preexistentes.

**Lecciones en `docs/ai/lecciones.md`** están allí, pero no hay:
- `docs/design/PATTERNS.md` — resumen de patrones adoptados
- Checklist de "antes de implementar feature X"
- Matriz de dónde va cada tipo de lógica (dominio/use case/adapter)
- Anti-patterns explícitos ("nunca hagas esto porque...")

---

### 6. **Falta de documentación de validación y constraints**

**Impacto:** Validaciones duplicadas o inconsistentes entre dominio, use case y controller.

**Inexistente:**
- `docs/design/VALIDATION_STRATEGY.md`
- Dónde van `@NotNull`, `@Valid`, bean validation vs validación de dominio
- Cómo testear validaciones
- Schema de errores de validación (`fieldErrors`)

---

### 7. **Observabilidad y telemetría sin documentar**

**Impacto:** No hay consenso sobre qué medir, cómo nombrar métricas, logs estructurados.

**Inexistente:**
- Docs de cómo agregar métricas (hay T-020 / T-073 pero sin guía práctica)
- Convención de nombres de logs y métricas
- Qué eventos auditar (T-076 / T-127 pendientes sin contexto)
- Health checks y readiness probes para k8s

---

### 8. **Testing: estrategia parcial, convenciones no consolidadas**

**Existe:** `docs/development/TEST_STRATEGY.md`

**Deficiencias:**
- No documenta **cómo testear use cases** que usan múltiples puertos
- No hay ejemplos de **integration tests con Testcontainers** (T-013, T-025, T-091 pendientes)
- **Matriz de cobertura** (JaCoCo 0.15 en `keygo-supabase` vs 0.60 deseado) sin roadmap
- No existe guía de **cómo mockear vs Testcontainers vs H2 en memoria**

---

### 9. **Ausencia de documentación de operaciones en producción**

**Impacto:** No hay runbook para deployment, rollback, debugging en prod.

**Inexistente:**
- `docs/operations/PRODUCTION_RUNBOOK.md`
- Guía de configuración para diferentes ambientes (local/dev/staging/prod)
- Procedure de rotación de keys (RFC T-028 KMS pending)
- Procedure de migración / upgrade de schema Flyway

---

### 10. **Documentación de seguridad incompleta**

**Existe:** `docs/api/BOOTSTRAP_FILTER.md`

**Deficiencias:**
- No documenta CORS en detalle (T-061 pending)
- No hay guía de **cómo validar autorización** en endpoints custom
- No existe `docs/security/AUTHORIZATION_PATTERNS.md`
- Falta matriz de qué roles pueden hacer qué (T-051 pending)

---

### 11. **Falta de inventario de endpoints organizados por feature**

**Impacto:** Navegar endpoints por `ResponseCode` o module es difícil.

**Estado:**
- OpenAPI `/v3/api-docs` existe
- Postman collection existe
- `FRONTEND_DEVELOPER_GUIDE.md` §14 parcial

**Deficiencia:** No hay **tabla consolidada de endpoints por dominio funcional** (ej. "Account & Settings", "Tenant Management", "Billing", "RBAC").

---

### 12. **Documentación de SCIM/aprovisionamiento y integraciones**

**Impacto:** T-047 (SCIM 2.0) no tiene dirección clara.

**Inexistente:**
- `docs/design/PROVISIONING_STRATEGY.md`
- Cómo extender API para nuevos casos de integración
- Estándar SCIM vs custom endpoints

---

### 13. **Versionado de API no documentado**

**Impacto:** No hay política clara sobre breaking changes, deprecación, versioning.

**Inexistente:**
- `docs/design/API_VERSIONING_STRATEGY.md`
- Cómo versionar endpoints nuevos
- Cómo deprecar sin breaking changes
- Política de backward compatibility

---

## Fase Previa: Arquitectura Documental con Índices (8 h)

**Propuesta:** Reorganizar documentación con carpetas temáticas + README como índices navegables.

**Beneficio:** IAs navegan por índices sin leer documentos completos. Reducción 60% en tiempo de búsqueda.

**Estructura:**
```
docs/
├── plans/            # Planes de mejora + RFCs
├── ai/
│   ├── lecciones/    # Por tema, por feature, por fecha
│   ├── inconsistencias/
│   ├── propuestas/
│   └── agents-registro/
├── design/
│   ├── core/         # ARCHITECTURE.md, DOMAIN_MODEL
│   ├── patterns/     # Patrones, validación, autorización
│   ├── api/          # ERROR_CATALOG, ENDPOINTS, VERSIONING
│   └── infrastructure/
├── development/
│   ├── testing/      # TEST_STRATEGY, TESTCONTAINERS
│   ├── ide/
│   └── troubleshooting/
└── operations/       # Deployment, observability, runbook
```

**Cada carpeta tiene README como índice:**
- Tabla de contenidos
- Búsqueda rápida por tema/feature/fecha
- Links a documentos específicos

**Documentación detallada:** [`docs/ai/ARQUITECTURA_DOCUMENTAL_INDICES.md`](docs/ai/ARQUITECTURA_DOCUMENTAL_INDICES.md)

**Tareas:**
1. Crear estructura de carpetas (1 h)
2. Crear README de índices (3 h)
3. Mover archivos existentes (2 h)
4. Actualizar links en docs (2 h)

---

## Plan de Implementación por Prioridad

### Sprint 0: Arquitectura Documental (Paralelo a Sprint 1)

**Objetivo:** Estructura lista ANTES de crear nuevos documentos.

**Tareas:**
- Crear carpetas temáticas
- Crear README índices en cada carpeta
- Mover docs existentes
- Actualizar referencias cruzadas

**Duración:** 1 semana  
**Bloqueador:** No, puede ejecutarse en paralelo con Sprint 1

---

### Sprint 1 (Corto plazo inmediato: Semana 1-2)

Documentos críticos para onboarding y reducing support burden.

| ID | Doc | Objetivo | Dependencias | Esfuerzo |
|---|---|---|---|---|
| D-01 | `docs/design/ERROR_CATALOG.md` | Catálogo de `ResponseCode` + reglas de uso | Leer `ResponseCode.java` | 2-3 h |
| D-02 | `docs/design/VALIDATION_STRATEGY.md` | Dónde van validaciones, patrones | Lecciones.md + código | 2-3 h |
| D-03 | `docs/development/DEBUGGING.md` | Guía hands-on de debugging | Experiencia local | 3-4 h |
| D-04 | `docs/design/PATTERNS.md` | Patrones + anti-patterns + checklist | Lecciones.md + ARCHITECTURE | 2-3 h |
| D-05 | `docs/api/ENDPOINT_CATALOG.md` | Tabla consolidada de endpoints por dominio | Postman + código | 3-4 h |

**Salida esperada:** Nuevo desarrollador puede onboarding en 2 horas sin preguntar.

---

### Sprint 2 (Corto plazo: Semana 3-4)

Consolidación de RFCs huérfanas y cierre de gaps de arquitectura.

| ID | Doc | Objetivo | Dependencias | Esfuerzo |
|---|---|---|---|---|
| D-06 | `docs/design/OAUTH2_MULTIDOMAIN_CONTRACT.md` | RFC `restructure-multitenant/*` absorbida | RFC 10 docs | 4-5 h |
| D-07 | `docs/design/AUTHORIZATION_PATTERNS.md` | T-051 matriz RBAC + `@PreAuthorize` patterns | BOOTSTRAP_FILTER.md | 3-4 h |
| D-08 | `docs/operations/PRODUCTION_RUNBOOK.md` | Deployment, rollback, incidents | Experiencia ops | 4-5 h |
| D-09 | `docs/design/PROVISIONING_STRATEGY.md` | Arquitectura para SCIM / integraciones (T-047) | RFC pending | 2-3 h |
| D-10 | `docs/design/API_VERSIONING_STRATEGY.md` | Policy de versionado, deprecation | ARCHITECTURE.md | 2 h |

**Salida esperada:** RFCs huérfanas absorbidas, arquitectura documentada para próximas features.

---

### Sprint 3 (Mediano plazo: Semana 5-6)

Documentación de operaciones y testing.

| ID | Doc | Objetivo | Dependencias | Esfuerzo |
|---|---|---|---|---|
| D-11 | `docs/development/TEST_INTEGRATION.md` | Testcontainers, H2, mocking | T-013, T-025, T-091 | 4-5 h |
| D-12 | `docs/operations/OBSERVABILITY.md` | Métricas, logs estructurados, tracing | T-020, T-073 | 4-5 h |
| D-13 | Actualizar `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md` | Refresh §1-14 con ejemplos actualizados | D-05 + código | 3-4 h |

**Salida esperada:** Documentación operativa y testing maduro para lanzamiento.

---

## Proceso de Absorción RFC → Canon

Para evitar futura fragmentación, establecer un checklist:

```markdown
## RFC Closure Checklist

- [ ] RFC completada técnicamente (implementación merged)
- [ ] Decisiones técnicas documentadas en `docs/design/*`
- [ ] Contratos API reflejados en OpenAPI + Postman
- [ ] Ejemplos end-to-end en `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md`
- [ ] Lecciones aprendidas agregadas a `docs/ai/lecciones.md`
- [ ] RFC archivada (mover a `docs/archive/rfc-*` con timestamp)
- [ ] Entry en `docs/archive/RFC_CLOSURE_LOG.md`
```

---

## Referencias Cruzadas y Coherencia

### Documentos que deben actualizarse en coordinación

1. **Nuevo endpoint → actualizar:**
   - OpenAPI + controller
   - Postman collection
   - `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md` §14
   - `docs/design/ERROR_CATALOG.md` (si nuevo ResponseCode)
   - `docs/design/ENDPOINT_CATALOG.md`

2. **Cambio de autorización → actualizar:**
   - `docs/api/BOOTSTRAP_FILTER.md`
   - `docs/design/AUTHORIZATION_PATTERNS.md` (nuevo)
   - Postman (agregar Bearer token)

3. **Nueva migración Flyway → actualizar:**
   - `docs/data/MIGRATIONS.md`
   - `docs/data/DATA_MODEL.md`
   - `docs/data/ENTITY_RELATIONSHIPS.md`

---

## Métricas de Éxito

| Métrica | Target | Cómo medir |
|---|---|---|
| Cobertura de documentos canónicos | 95% | RFCs sin análogos en `docs/design/*` |
| Actualizaciones de FRONTEND_DEVELOPER_GUIDE | Cada 2 semanas | Diff en endpoints §14 |
| Lecciones aprendidas registradas | +1 por tarea | `docs/ai/lecciones.md` |
| Nuevos desarrolladores: onboarding time | < 2 h | Encuesta |
| Links Markdown rotos | 0 | Linter (T-031) |

---

## Tareas Asociadas (Propuestas del Roadmap)

Documentación y tareas técnicas necesarias para cerrar deficiencias:

| Deficiencia | Propuesta Roadmap | Estado |
|---|---|---|
| RFCs huérfanas | *Nueva:* `D-RFC-CONSOLIDATION` | 🔲 Pendiente |
| Error catalog | *Nueva:* `D-ERROR-CATALOG` | 🔲 Pendiente |
| Debugging guide | *Nueva:* `D-DEBUG-GUIDE` | 🔲 Pendiente |
| Validation strategy | *Nueva:* `D-VALIDATION-STRATEGY` | 🔲 Pendiente |
| Verificación links rotos | T-030 | 🔲 Pendiente |
| Patterns consolidation | *Nueva:* `D-PATTERNS-CONSOLIDATION` | 🔲 Pendiente |
| Observability docs | *Relacionada:* T-020, T-073 | 🔲 Pendiente |
| Testing guide | *Relacionada:* T-013, T-025, T-091 | 🔲 Pendiente |

---

## Próximos Pasos

1. **Inmediato:** Crear Sprint 1 tasks en ROADMAP.md (D-01 a D-05)
2. **Leer:** `docs/ai/AGENT_OPERATIONS.md` para política de absorción de RFCs
3. **Validar:** Existencia y actualización de docs listados en Sprint 1 vs código actual
4. **Ejecutar:** Implementar D-01 (ERROR_CATALOG) como prueba de concepto

---

**Autores:** AI Agent  
**Última revisión:** 2026-04-09  
**Estado:** Listo para feedback y ejecución incremental
