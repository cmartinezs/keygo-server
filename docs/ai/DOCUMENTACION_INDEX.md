# Índice de Documentación del Proyecto — Plaques de Revisión

**Última actualización:** 2026-04-09  
**Estado:** Activo — implementación incremental

---

## 🆕 Arquitectura Documental con Índices Navegables

**Propuesta:** Reorganizar documentación con carpetas temáticas + README como índices.

**Beneficio:** Búsqueda de información **78% más rápida**. IAs navegan por índices sin leer documentos completos.

### Documentos relacionados

1. **[`docs/ai/ARQUITECTURA_DOCUMENTAL_INDICES.md`](ARQUITECTURA_DOCUMENTAL_INDICES.md)** — Propuesta completa
2. **[`docs/ai/EJEMPLO_NAVEGACION_CON_INDICES.md`](EJEMPLO_NAVEGACION_CON_INDICES.md)** — Casos de uso (antes vs después)

### Estructura Propuesta

```
docs/
├── plans/                 # Planes + RFCs
├── design/
│   ├── core/             # ARCHITECTURE.md, DOMAIN_MODEL
│   ├── patterns/         # PATTERNS.md, VALIDATION, AUTHORIZATION
│   ├── api/              # ERROR_CATALOG, ENDPOINTS, VERSIONING
│   └── infrastructure/
├── ai/
│   ├── lecciones/        # Por tema, feature, fecha
│   ├── inconsistencies/
│   ├── propuestas/
│   └── agents-registro/
├── development/
│   ├── testing/
│   ├── ide/
│   └── troubleshooting/
└── operations/
```

**Cada carpeta tiene README como índice:**
- Tabla de contenidos
- Búsqueda por tema/feature/fecha
- Links a documentos específicos

### Fase 0: Implementación (8 h)

| Tarea | Esfuerzo | Dependencias |
|---|---|---|
| Crear estructura de carpetas | 1 h | Ninguna |
| Crear README de índices | 3 h | Estructuras |
| Mover archivos existentes | 2 h | README |
| Actualizar links en docs | 2 h | Movimientos |
| **Total** | **8 h** | — |

**Ejecutar:** En paralelo con Sprint 1 (no bloqueador)

---

## Planes de Documentación Vigentes

### 📋 Plan Principal

**[`docs/PLAN_DOCUMENTACION_2026_REVISION.md`](../PLAN_DOCUMENTACION_2026_REVISION.md)**

Plan ejecutivo con 3 sprints de implementación. **Leer esto primero.**

- 13 deficiencias críticas identificadas
- 3 sprints con tareas específicas y esfuerzo estimado
- Proceso de absorción RFC → canon
- Métricas de éxito

### 🔍 Análisis Detallado

**[`docs/DEFICIENCIAS_DOCUMENTACION_DETALLE.md`](../DEFICIENCIAS_DOCUMENTACION_DETALLE.md)**

Análisis profundo de cada deficiencia con:
- Síntoma
- Causa raíz
- Impacto
- Solución propuesta
- **Pasos concretos de implementación** (para agente)
- Dependencias
- Esfuerzo estimado

Usar para entender QUÉ y CÓMO implementar cada documento.

### 📊 Resumen Rápido

**[`docs/RESUMEN_PLAN_DOCUMENTACION.md`](../RESUMEN_PLAN_DOCUMENTACION.md)**

TL;DR en 2 páginas: tabla de deficiencias, impacto, solución. Para lectura rápida o referencias cruzadas.

---

## Documentación a Crear (Priorizada)

### Sprint 1 (Semana 1-2) — Crítica para Onboarding

| # | Documento | Ubicación | Esfuerzo | Estado |
|---|---|---|---|---|
| D-01 | **ERROR_CATALOG.md** | `docs/design/` | 7 h | 🔲 Pendiente |
| D-02 | **DEBUGGING.md** | `docs/development/` | 10 h | 🔲 Pendiente |
| D-03 | **VALIDATION_STRATEGY.md** | `docs/design/` | 5 h | 🔲 Pendiente |
| D-04 | **PATTERNS.md** | `docs/design/` | 4 h | 🔲 Pendiente |
| D-05 | **ENDPOINT_CATALOG.md** | `docs/design/` | 4 h | 🔲 Pendiente |

**Beneficio:** Nuevo dev onboards en 2h sin preguntas.

---

### Sprint 2 (Semana 3-4) — Consolidación RFCs

| # | Documento | Ubicación | Esfuerzo | Estado |
|---|---|---|---|---|
| D-06 | **RFC_CLOSURE_PROCESS.md** | `docs/ai/` | 1.5 h | 🔲 Pendiente |
| D-07 | **RFC_CLOSURE_LOG.md** | `docs/archive/` | 2 h | 🔲 Pendiente |
| D-08 | **OAUTH2_MULTIDOMAIN_CONTRACT.md** | `docs/design/` | 4 h | 🔲 Pendiente |
| D-09 | **AUTHORIZATION_PATTERNS.md** | `docs/design/` | 4 h | 🔲 Pendiente |
| D-10 | **PRODUCTION_RUNBOOK.md** | `docs/operations/` | 5 h | 🔲 Pendiente |
| D-11 | **PROVISIONING_STRATEGY.md** | `docs/design/` | 3 h | 🔲 Pendiente |
| D-12 | **API_VERSIONING_STRATEGY.md** | `docs/design/` | 2 h | 🔲 Pendiente |

**Beneficio:** RFCs absorbidas, claridad arquitectónica para próximas features.

---

### Sprint 3 (Semana 5-6) — Testing y Operaciones

| # | Documento | Ubicación | Esfuerzo | Estado |
|---|---|---|---|---|
| D-13 | **TEST_INTEGRATION.md** | `docs/development/` | 5 h | 🔲 Pendiente |
| D-14 | **OBSERVABILITY.md** | `docs/operations/` | 5 h | 🔲 Pendiente |
| D-15 | **FRONTEND_DEVELOPER_GUIDE update** | `docs/keygo-ui/` | 5 h | 🔲 Pendiente |

**Beneficio:** Documentación operativa y testing maduro para lanzamiento.

---

## Vinculación a Propuestas de Roadmap

Cada documento está vinculado a propuestas existentes o propone nuevas:

| Documento | Propuesta Roadmap | Relación |
|---|---|---|
| ERROR_CATALOG.md | *Nueva: D-ERROR-CAT* | Soporta T-120–T-123 (i18n) |
| DEBUGGING.md | *Nueva: D-DEBUG* | Soporte a todas las features |
| VALIDATION_STRATEGY.md | *Nueva: D-VAL-STRATEGY* | Reduce bugs en validación |
| PATTERNS.md | *Consolidar de lecciones.md* | Documentar patrones existentes |
| ENDPOINT_CATALOG.md | T-049 (Postman) | Completar inventario |
| RFC_CLOSURE_PROCESS.md | *Nueva: D-RFC-PROCESS* | Prevenir fragmentación futura |
| OAUTH2_MULTIDOMAIN_CONTRACT.md | RFC restructure-multitenant (10 docs) | Absorber decisiones de RFC |
| AUTHORIZATION_PATTERNS.md | T-051 | Matriz RBAC + @PreAuthorize |
| PRODUCTION_RUNBOOK.md | *Nueva: D-PROD-OPS* | Soporte a deployment |
| PROVISIONING_STRATEGY.md | T-047 | SCIM 2.0 architecture |
| API_VERSIONING_STRATEGY.md | *Nueva: D-API-VERSION* | Prevenir breaking changes |
| TEST_INTEGRATION.md | T-013, T-025, T-091 | Testcontainers + H2 |
| OBSERVABILITY.md | T-020, T-073 | Métricas, logs, tracing |
| FRONTEND_DEVELOPER_GUIDE update | Mantener § activa | Refresh endpoints + ejemplos |

---

## Proceso Recomendado

### Para agente (IA)

1. **Leer** `docs/PLAN_DOCUMENTACION_2026_REVISION.md` — Entender objetivo y sprints
2. **Leer** `docs/DEFICIENCIAS_DOCUMENTACION_DETALLE.md` — Entender síntoma → solución
3. **Seleccionar** documento de Sprint 1 (ej. D-01 ERROR_CATALOG)
4. **Seguir** pasos concretos en sección "Pasos Concretos" del análisis detallado
5. **Validar** contra código actual antes de finalizar

### Para humano (Product owner / Team lead)

1. **Leer** `docs/RESUMEN_PLAN_DOCUMENTACION.md` — 2 páginas
2. **Decidir** prioridad de sprints (¿empezar con D-01? ¿D-02?)
3. **Asignar** docs a agentes o humanos
4. **Medir** con las métricas de éxito listadas en plan principal

---

## Referencias Cruzadas

Este índice está vinculado a:

- **CLAUDE.md** — Política de comportamiento de agentes
- **docs/ai/AGENT_OPERATIONS.md** — Operación compartida de agentes
- **ROADMAP.md** — Propuestas técnicas y funcionales
- **docs/ai/lecciones.md** — Lecciones aprendidas (fuente de PATTERNS.md)
- **docs/ai/inconsistencias.md** — Inconsistencias detectadas

---

## Estado General

**Documentación existente (fuentes de verdad):** ✅ Bien estructurada  
**RFCs huérfanas:** 🔴 Crítica  
**Onboarding de nuevo dev:** 🟠 4-5 horas (debería ser 2)  
**Clarity de patrones:** 🟡 Dispersa en lecciones.md  
**Frontend integration:** 🟠 Sin ejemplos de flujos  
**Operations:** 🔴 Crítica (sin runbook)  

**Objetivo:** 🟡 → ✅ en 3 sprints (6-7 semanas)

---

**Próximo paso:** Ejecutar Sprint 1, comenzando con D-01 (ERROR_CATALOG.md).

**Responsable:** AI Agent + Equipo  
**Última revisión:** 2026-04-09
