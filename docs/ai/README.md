# Memoria de Agentes — Índice

**Propósito:** Registro centralizado de lecciones aprendidas, inconsistencias, propuestas y cambios al quick-start.

Base de conocimiento acumulado del proyecto para IAs y desarrolladores.

---

## 🎯 Búsqueda Rápida

| Necesidad | Documento | Tipo |
|---|---|---|
| Política operativa compartida de agentes | [`AGENT_OPERATIONS.md`](AGENT_OPERATIONS.md) | Canon |
| Buscar lección por tema o feature | [`lecciones/README.md`](lecciones/README.md) | Índice |
| Ver lecciones de abril 2026 | [`lecciones/2026-04.md`](lecciones/2026-04.md) | Histórico |
| Lección sobre validación | [`lecciones/por-tema/validacion.md`](lecciones/por-tema/validacion.md) | Por tema |
| Lección sobre T-111 RBAC | [`lecciones/por-feature/T-111-rbac.md`](lecciones/por-feature/T-111-rbac.md) | Por feature |
| Inconsistencias doc vs código | [`inconsistencies/README.md`](inconsistencies/README.md) | Índice |
| Estado de propuestas T-NNN / F-NNN | [`propuestas/README.md`](propuestas/README.md) | Índice |
| Cambios a AGENTS.md | [`agents-registro/README.md`](agents-registro/README.md) | Índice |

---

## 📚 Estructura de Carpetas

### Lecciones Aprendidas
- **Índice:** [`lecciones/README.md`](lecciones/README.md) — búsqueda por tema, feature, período
- **Por Tema:** [`lecciones/por-tema/`](lecciones/por-tema/) — validación, multi-tenancy, JPA, etc.
- **Por Feature:** [`lecciones/por-feature/`](lecciones/por-feature/) — T-111, T-124, T-128, etc.
- **Por Período:** [`lecciones/2026-04.md`](lecciones/2026-04.md), [`lecciones/2026-03.md`](lecciones/2026-03.md), etc.

### Inconsistencias Detectadas
- **Índice:** [`inconsistencies/README.md`](inconsistencies/README.md)
- **Datos:** [`inconsistencies/datos.md`](inconsistencies/datos.md)
- **APIs:** [`inconsistencies/apis.md`](inconsistencies/apis.md)
- **Documentación:** [`inconsistencies/documentacion.md`](inconsistencies/documentacion.md)

### Propuestas Técnicas y Funcionales
- **Índice/Matriz:** [`propuestas/README.md`](propuestas/README.md) — T-NNN / F-NNN + estado
- **Roadmap 2026:** [`propuestas/roadmap-2026.md`](propuestas/roadmap-2026.md)

### Registro de Cambios
- **Índice:** [`agents-registro/README.md`](agents-registro/README.md) — cambios a AGENTS.md
- **2026-04:** [`agents-registro/2026-04.md`](agents-registro/2026-04.md)
- **Histórico:** [`agents-registro-historico.md`](agents-registro-historico.md)

---

## 🔗 Documentos Canónicos

| Documento | Rol |
|---|---|
| [`AGENT_OPERATIONS.md`](AGENT_OPERATIONS.md) | Política operativa compartida de agentes (NO cambiar) |
| [`../../AGENTS.md`](../../AGENTS.md) | Quick-start técnico resumido (raíz) |
| [`../../AI_CONTEXT.md`](../../AI_CONTEXT.md) | Snapshot operativo del proyecto (raíz) |
| [`../../CLAUDE.md`](../../CLAUDE.md) | Instrucciones de comportamiento Claude Code (raíz) |

---

## 📋 Contenido Existente (Será Reorganizado)

### Lecciones (Será dividido en carpetas)
- `lecciones.md` (30 KB) → Dividir en:
  - `lecciones/por-tema/validacion.md`
  - `lecciones/por-tema/multi-tenancy.md`
  - `lecciones/por-feature/T-111-rbac.md`
  - `lecciones/2026-04.md`

### Inconsistencias (Será dividido en carpetas)
- `inconsistencias.md` → `inconsistencies/README.md`
- `inconsistencias-datos.md` → `inconsistencies/datos.md`
- `inconsistencias-seguridad.md` (contenido) → `inconsistencies/apis.md`

### Propuestas (Será dividido en carpetas)
- `propuestas.md` → `propuestas/README.md` + `propuestas/roadmap-2026.md`

### Registro (Será organizado por período)
- `agents-registro.md` → `agents-registro/README.md` + `agents-registro/2026-04.md`
- `agents-registro-historico.md` → `agents-registro-historico.md`

---

## 🔄 Cómo Usar

### Si eres IA

1. Abre [`lecciones/README.md`](lecciones/README.md) (tabla de búsqueda)
2. Busca por tema, feature o período
3. Click en link → Documento específico
4. Lee lección relevante → Aplica patrón
5. Al completar tarea, agrega lección nueva a `lecciones/`

### Si eres Desarrollador

1. Antes de implementar → Consulta [`lecciones/README.md`](lecciones/README.md)
2. Durante desarrollo → Usa patrones documentados
3. Después de terminar → Agrega lección a `lecciones/`
4. Si encuentras inconsistencia → Repórta en `inconsistencies/`

### Si eres Tech Lead

1. Audit propuestas → [`propuestas/README.md`](propuestas/README.md)
2. Deuda técnica → [`inconsistencies/README.md`](inconsistencies/README.md)
3. Cambios en convenciones → [`agents-registro/README.md`](agents-registro/README.md)

---

## ⚠️ Reglas de Uso

1. **Canónica para memoria AI:** Única fuente de verdad para lecciones y decisiones aprendidas
2. **No para documentación pública:** Esta carpeta es solo para agentes y tech team
3. **Política compartida en AGENT_OPERATIONS.md:** NO modificar sin consenso del equipo
4. **Reorganización en curso:** Migración de archivos .md monolíticos a carpetas temáticas

---

## 📊 Matriz de Contenido Actual vs Futuro

| Actual | Futuro | Estado |
|---|---|---|
| `lecciones.md` (30 KB) | `lecciones/` (carpeta) | 🔄 En migración |
| `inconsistencias.md` + 2 más | `inconsistencies/` (carpeta) | 🔄 En migración |
| `propuestas.md` | `propuestas/` (carpeta) | 🔄 En migración |
| `agents-registro.md` | `agents-registro/` (carpeta) | 🔄 En migración |

---

## 📚 Referencias Cruzadas

- **Planes de mejora:** [`../plans/README.md`](../plans/README.md)
- **Decisiones arquitectónicas:** [`../design/README.md`](../design/README.md)
- **Desarrollo:** [`../development/README.md`](../development/README.md)
- **Propuestas oficial:** [`../../ROADMAP.md`](../../ROADMAP.md)

---

**Última actualización:** 2026-04-09  
**Estado:** Reorganización en curso (Fase 0 del plan de documentación)  
**Responsable:** AI Agent
