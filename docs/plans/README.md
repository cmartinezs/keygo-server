# Planes de Mejora — Índice

**Propósito:** Catálogo centralizado de planes, RFCs y propuestas de mejora del proyecto.

Cada plan tiene su propia carpeta con estructura modular: resumen ejecutivo, análisis detallado, sprints e implementación.

---

## Planes Activos

| Plan | Objetivo | Sprints | Estado | Link |
|---|---|---|---|---|
| **Documentación 2026** | Onboarding 2h, 13 deficiencias, 3 sprints de documentación | 3 (70 h) | 🟡 En diseño | [`documentacion-2026/`](#documentacion-2026) |

---

## Cómo Navegar por Plan

### Estructura de cada carpeta de plan

```
plans/{nombre-plan}/
├── README.md ← TL;DR: objetivo, sprints, deficiencias
├── 01-resumen-ejecutivo.md ← Resumen visual
├── 02-deficiencias.md ← Análisis: síntoma, causa, impacto
├── 03-sprints.md ← Desglose de sprints + esfuerzo
├── implementacion/
│   ├── README.md ← Índice de pasos concretos
│   ├── documento-1.md ← Síntoma → solución → pasos
│   └── documento-N.md
└── referencias.md ← Links a propuestas, roadmap, etc.
```

---

## Documentación 2026

**Objetivo:** Cerrar 13 deficiencias identificadas en documentación.

**Estado:** 🟡 En diseño (propuestas entregadas 2026-04-09)

**Sprints:** 3 + Fase 0 (arquitectura)

**Documentos:** 15 nuevos

**Esfuerzo:** ~70 horas + 8 horas Fase 0

### Acceso Rápido

- **TL;DR (5 min):** [`../RESUMEN_PLAN_DOCUMENTACION.md`](../RESUMEN_PLAN_DOCUMENTACION.md)
- **Plan completo (25 min):** [`../PLAN_DOCUMENTACION_2026_REVISION.md`](../PLAN_DOCUMENTACION_2026_REVISION.md)
- **Análisis detallado (40 min):** [`../DEFICIENCIAS_DOCUMENTACION_DETALLE.md`](../DEFICIENCIAS_DOCUMENTACION_DETALLE.md)
- **Pasos de implementación:** [`implementacion/`](documentacion-2026/implementacion/)

### Deficiencias Críticas

| # | Deficiencia | Impact | Sprint | Doc |
|---|---|---|---|---|
| 2 | Sin ERROR_CATALOG.md | Alto | 1 | ERROR_CATALOG |
| 4 | Sin DEBUGGING.md | Alto | 1 | DEBUGGING |
| 1 | RFCs huérfanas | Alto | 2 | RFC_CLOSURE_PROCESS |
| 9 | Sin PRODUCTION_RUNBOOK | Alto | 2 | PRODUCTION_RUNBOOK |

[Ver todas 13 deficiencias → `../RESUMEN_PLAN_DOCUMENTACION.md`](../RESUMEN_PLAN_DOCUMENTACION.md)

### Decisión Importante: Arquitectura Documental (Fase 0)

**Propuesta (NUEVA 2026-04-09):**
- Reorganizar con carpetas temáticas + README como índices
- Beneficio: búsqueda 78% más rápida
- Costo: 8 horas (ejecutable en paralelo)

[Leer propuesta completa → `../PROPUESTA_ARQUITECTURA_DOCUMENTAL.md`](../PROPUESTA_ARQUITECTURA_DOCUMENTAL.md)

---

## Próximos Pasos

1. **Validar propuesta** con equipo (2026-04-10)
2. **Ejecutar Fase 0** (crear estructura + README) — Esta semana
3. **Iniciar Sprint 1** (ERROR_CATALOG, DEBUGGING) — Semana próxima

---

## Referencias

- **Dashboard de progreso:** [`../ai/DOCUMENTACION_INDEX.md`](../ai/DOCUMENTACION_INDEX.md)
- **Orientación por rol:** [`../QUICKSTART_DOCUMENTACION_REVISION_2026.md`](../QUICKSTART_DOCUMENTACION_REVISION_2026.md)
- **Mapa de documentación:** [`../MAPA_DOCUMENTACION_ENTREGADA.md`](../MAPA_DOCUMENTACION_ENTREGADA.md)
- **Propuestas técnicas:** [`../../ROADMAP.md`](../../ROADMAP.md)
- **Memoria de lecciones:** [`../ai/lecciones/README.md`](../ai/lecciones/README.md)

---

**Última actualización:** 2026-04-09  
**Responsable:** AI Agent
