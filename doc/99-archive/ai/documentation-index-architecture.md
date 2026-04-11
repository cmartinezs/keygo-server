---
name: Arquitectura Documental con Índices Navegables
description: Estructura de carpetas temáticas con README sintetizadores para navegación eficiente de IAs
type: project
---

# Arquitectura Documental — Sistema de Índices Navegables

**Fecha:** 2026-04-09  
**Propuesta:** Reorganizar documentación con carpetas temáticas + README como índices  
**Beneficio:** IAs (y humanos) navegan por índices sin leer documentos completos

---

## Problema

Documentos actuales:
- Algunos tienen 20-30 KB de contenido
- IA debe leer TODO para encontrar una sección específica
- Contextuar innecesariamente en detalles no relevantes
- Ineficiente para búsquedas futuras

**Síntoma:** "¿Dónde está la lección sobre patrones de validación?" → Hojear `docs/ai/lecciones.md` (30 KB)

---

## Solución: Estructura de Carpetas Temáticas con README como Índices

### Principio General

```
docs/
├── TEMA/
│   ├── README.md ← ÍNDICE SINTÉTICO (tabla de contenidos + breve descripción)
│   ├── archivo-1.md
│   ├── archivo-2.md
│   └── subcarpeta/
│       ├── README.md ← ÍNDICE de subcarpeta
│       ├── detalle-1.md
│       └── detalle-2.md
```

**README = Tabla de contenidos + metadata mínima:**
- Título
- Objetivo del tema
- Tabla con: `| Sección | Descripción (1 línea) | Link |`
- Cómo navegar
- Referencias cruzadas

**Documentos = Contenido detallado**
- Solo lo relevante para esa sección
- Link "Volver al índice" arriba

---

## Estructura Concreta Propuesta

### 1. **`docs/plans/`** — Planes de mejora / RFCs

```
docs/plans/
├── README.md ← Índice de todos los planes
├── documentacion-2026/
│   ├── README.md ← Índice del plan (resumen + 3 sprints)
│   ├── 01-deficiencias.md (síntoma + causa raíz)
│   ├── 02-sprint-1.md (5 documentos, 30h)
│   ├── 03-sprint-2.md (7 documentos, 21.5h)
│   ├── 04-sprint-3.md (3 documentos, 15h)
│   ├── 05-implementacion/
│   │   ├── README.md ← Índice de pasos concretos
│   │   ├── error-catalog.md (síntoma→solución, pasos)
│   │   ├── debugging.md (síntoma→solución, pasos)
│   │   └── ...
│   └── referencias.md
└── [futuro] otro-plan/
    └── README.md
```

**`docs/plans/README.md`:**
```markdown
# Planes de Mejora y RFCs

## Objetivo
Índice de todos los planes de mejora del proyecto.
Cada plan tiene su propia carpeta con estructura modular.

## Planes Activos

| Plan | Objetivos | Sprints | Documentos | Estado |
|---|---|---|---|---|
| [Documentación 2026](documentacion-2026/) | Onboarding 2h, consolidar RFCs, testing maduro | 3 | 15 | 🟡 En ejecución |
| [Plan X](plan-x/) | ... | ... | ... | 🔲 Pendiente |

## Cómo Navegar

1. Selecciona plan en tabla
2. Abre `README.md` de la carpeta del plan
3. El README muestra tabla de secciones
4. Click en sección → va al documento detallado
```

**`docs/plans/documentacion-2026/README.md`:**
```markdown
# Plan Documentación 2026 — Índice

**Estado:** 🟡 En ejecución | **Sprints:** 3 | **Documentos:** 15 | **Esfuerzo:** ~70h

## Estructura del Plan

| Sección | Descripción | Link |
|---|---|---|
| **Resumen ejecutivo** | 13 deficiencias identificadas; 3 sprints; 15 docs | [`RESUMEN.md`](#) |
| **Sprint 1** | ERROR_CATALOG, DEBUGGING, VALIDATION, PATTERNS, ENDPOINTS | [`01-sprint-1.md`](#) |
| **Sprint 2** | RFC closure, OAuth2, Authorization, Runbook, SCIM, Versioning | [`02-sprint-2.md`](#) |
| **Sprint 3** | Testing, Observability, Frontend update | [`03-sprint-3.md`](#) |
| **Implementación** | Pasos concretos para cada documento | [`implementacion/`](#) |
| **Referencias** | Links a ROADMAP, propuestas, lecciones | [`referencias.md`](#) |

## Deficiencias (Rápida Consulta)

| # | Deficiencia | Impacto | Sprint |
|---|---|---|---|
| 1 | RFCs huérfanas | Alto | 2 |
| 2 | Sin ERROR_CATALOG | Alto | 1 |
| ... | ... | ... | ... |

## Próximos Pasos

1. Leer sección "Resumen ejecutivo"
2. Elegir sprint a ejecutar
3. Ir a carpeta [`implementacion/`](#) para pasos concretos
```

---

### 2. **`docs/ai/`** — Memoria de agentes (lecciones, inconsistencias)

```
docs/ai/
├── README.md ← Índice de memoria AI
├── lecciones/
│   ├── README.md ← Índice de lecciones (por tema, fecha, búsqueda)
│   ├── 2026-04.md (lecciones de abril)
│   ├── 2026-03.md (lecciones de marzo)
│   ├── por-tema/
│   │   ├── validacion.md
│   │   ├── multi-tenancy.md
│   │   ├── jpa-y-orm.md
│   │   └── ...
│   └── por-feature/
│       ├── T-111-rbac.md
│       ├── T-124-billing.md
│       └── ...
├── inconsistencias/
│   ├── README.md ← Índice de inconsistencias
│   ├── datos.md
│   ├── apis.md
│   └── documentacion.md
├── propuestas/
│   ├── README.md ← Estado de propuestas T-NNN / F-NNN
│   ├── 2026-roadmap.md
│   └── por-categoria/
│       ├── infraestructura.md
│       └── features.md
└── agents-registro/
    ├── README.md ← Cambios a AGENTS.md
    └── 2026-04.md
```

**`docs/ai/lecciones/README.md`:**
```markdown
# Lecciones Aprendidas — Índice

**Objetivo:** Encontrar rápidamente lecciones por tema, fecha o feature.

## Búsqueda Rápida

### Por Tema

| Tema | Lecciones | Link |
|---|---|---|
| Validación | 4 entradas | [`por-tema/validacion.md`](#) |
| Multi-tenancy | 8 entradas | [`por-tema/multi-tenancy.md`](#) |
| JPA y ORM | 6 entradas | [`por-tema/jpa-y-orm.md`](#) |
| ... | ... | ... |

### Por Feature (T-NNN / F-NNN)

| Feature | Lecciones | Estado |
|---|---|---|
| T-111 (RBAC) | 5 entradas | ✅ Completada |
| T-124 (Billing) | 3 entradas | ✅ Completada |
| T-128 (Username collision) | 2 entradas | 🔲 Pendiente |

### Por Fecha

| Período | Entradas | Link |
|---|---|---|
| Abril 2026 | 15 | [`2026-04.md`](#) |
| Marzo 2026 | 22 | [`2026-03.md`](#) |

## Formato de Entrada

```markdown
### [YYYY-MM-DD] Título descriptivo

**Síntoma:** Qué salió mal o qué patrón mejoró.
**Causa:** Por qué sucedió.
**Solución:** Cómo se resolvió.
```

Máx 6 líneas por entrada.
```

**`docs/ai/lecciones/por-tema/validacion.md`:**
```markdown
# Lecciones — Validación

[← Volver a índice de lecciones](../README.md)

Lecciones sobre dónde van validaciones, patrones, errores comunes.

## Índice Rápido

| Fecha | Síntoma | Link |
|---|---|---|
| 2026-04-09 | Validación duplicada en dominio + use case | [L-001](#l-001) |
| 2026-04-05 | @Valid en DTO falla sin mensaje humanizado | [L-002](#l-002) |
| ... | ... | ... |

## Lecciones Detalladas

### L-001: [2026-04-09] Validación duplicada en dominio + use case

**Síntoma:** La validación ocurre 2 veces.
**Causa:** No claro dónde va cada validación.
**Solución:** Bean Validation en DTO (HTTP), lógica en dominio (invariantes).

---

### L-002: [2026-04-05] @Valid en DTO falla sin mensaje humanizado

...
```

---

### 3. **`docs/design/`** — Decisiones de arquitectura

```
docs/design/
├── README.md ← Índice de decisiones (tabla de temas + links)
├── core/
│   ├── README.md ← Índice de core architecture
│   ├── ARCHITECTURE.md (actual, solo raíz)
│   ├── DOMAIN_MODEL.md
│   └── MODULES.md
├── patterns/
│   ├── README.md ← Índice de patrones
│   ├── PATTERNS.md (consolidado de lecciones)
│   ├── VALIDATION_STRATEGY.md
│   ├── AUTHORIZATION_PATTERNS.md
│   └── ERROR_HANDLING.md
├── api/
│   ├── README.md ← Índice API
│   ├── ERROR_CATALOG.md
│   ├── ENDPOINT_CATALOG.md
│   └── API_VERSIONING_STRATEGY.md
└── infrastructure/
    ├── README.md ← Índice infra
    ├── PERSISTENCE.md
    ├── JWT_AND_KEYS.md
    └── ...
```

**`docs/design/README.md`:**
```markdown
# Decisiones de Arquitectura — Índice

## Estructura

| Área | Documentos | Objetivo |
|---|---|---|
| [Core](core/) | ARCHITECTURE, DOMAIN_MODEL, MODULES | Pilares del sistema |
| [Patterns](patterns/) | PATTERNS, VALIDATION, AUTHORIZATION, ERROR_HANDLING | Cómo hacemos las cosas |
| [API](api/) | ERROR_CATALOG, ENDPOINTS, VERSIONING | Contratos HTTP |
| [Infrastructure](infrastructure/) | PERSISTENCE, JWT, KEYS | Herramientas + configuración |

## Búsqueda Rápida

**¿Dónde va esta lógica?**
→ Ver [`patterns/PATTERNS.md`](patterns/)

**¿Qué ResponseCode debería usar?**
→ Ver [`api/ERROR_CATALOG.md`](api/)

**¿Cómo valido esto?**
→ Ver [`patterns/VALIDATION_STRATEGY.md`](patterns/)
```

---

### 4. **`docs/development/`** — Guías operativas

```
docs/development/
├── README.md ← Índice (setup, testing, debugging, IDE)
├── ENVIRONMENT_SETUP.md
├── DEBUG_GUIDE.md
├── testing/
│   ├── README.md ← Índice de testing
│   ├── TEST_STRATEGY.md
│   ├── UNIT_TESTING.md
│   ├── INTEGRATION_TESTING.md
│   └── TESTCONTAINERS_GUIDE.md
├── ide/
│   ├── README.md ← Índice de IDEs
│   ├── INTELLIJ.md
│   ├── VSCODE.md
│   └── KEYBINDINGS.md
└── troubleshooting/
    ├── README.md ← Matriz de síntomas → solución
    └── common-issues.md
```

---

## Beneficios

### Para IAs

1. **Lectura eficiente:** "¿Hay lección sobre validación?" → Abre `docs/ai/lecciones/README.md` (1 KB) → tabla → selecciona link
2. **No leer código innecesario:** Solo lee el documento específico, no la carpeta completa
3. **Navegación jerárquica:** Índice general → tema → subtema
4. **Actualización fácil:** Cambios se reflejan en README (tabla de contenidos)

### Para Humanos

1. **Navegación visual:** Estructura clara de carpetas
2. **Búsqueda rápida:** README actúa como tabla de contenidos
3. **Onboarding:** Nuevo dev ve `docs/README.md` + navega por índices
4. **Mantenimiento:** Cambio de contenido = actualizar tabla en README

---

## Proceso de Implementación

### Fase 1: Reorganizar Existente (Sin cambios de contenido)

1. Crear estructura de carpetas
2. Crear README como índices en cada carpeta
3. Mover archivos existentes a carpetas
4. Actualizar links en CLAUDE.md, AGENTS.md, otros documentos

**Esfuerzo:** ~5-8 horas (organizativo, no creativo)

### Fase 2: Crear Nuevos Documentos (Sprint 1-3)

Una vez estructura lista, crear nuevos documentos dentro de carpetas temáticas.

**Esfuerzo:** Ya estimado en plan de documentación (30h + 21.5h + 15h)

---

## Ejemplo Concreto: Navegar para ERROR_CATALOG

**Hoy:**
```
IA: "Necesito entender ResponseCode"
Leer: docs/api/BOOTSTRAP_FILTER.md (5 KB, no es)
Leer: docs/design/API_SURFACE.md (8 KB, no es)
Grep: ResponseCode (encontrar en código)
```

**Después:**
```
IA: "Necesito entender ResponseCode"
Leer: docs/README.md → click en "API"
Leer: docs/api/README.md (tabla de 5 docs)
→ "ERROR_CATALOG.md — Qué ResponseCode usar, ejemplos"
Leer: docs/api/ERROR_CATALOG.md directamente
```

---

## Ejemplo Concreto: Buscar Lección sobre RBAC

**Hoy:**
```
IA: "¿Hay lección sobre RBAC?"
Grep: lecciones.md para RBAC (encontrar 1-2 entradas entre 50)
Leer: docs/ai/lecciones.md completo (30 KB)
```

**Después:**
```
IA: "¿Hay lección sobre RBAC?"
Leer: docs/ai/lecciones/README.md (tabla)
→ "Por Feature: T-111 (RBAC) — 5 entradas"
Leer: docs/ai/lecciones/por-feature/T-111-rbac.md (3 KB)
```

---

## Orden de Ejecución Recomendado

1. **Crear estructura de carpetas** (1 h)
2. **Crear README de índices en cada carpeta** (3 h)
3. **Mover archivos existentes** (2 h)
4. **Actualizar links en docs** (2 h)
5. **Ejecutar Sprint 1 de documentación dentro de nueva estructura** (30 h)

**Total adicional:** ~8 horas de reorganización inicial.

---

## Próximos Pasos

1. **Validar propuesta** con equipo
2. **Crear estructura de carpetas** en rama feature
3. **Crear README como índices**
4. **Ejecutar Sprint 1 dentro de nueva estructura**

---

**Responsable:** AI Agent + Equipo  
**Fecha propuesta:** 2026-04-10 (después de sprint 1)  
**Beneficio:** Reducción 60% en tiempo de navegación de documentación
