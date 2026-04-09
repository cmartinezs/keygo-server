# Lecciones Aprendidas — Índice

**Propósito:** Encuentra lecciones por tema, feature o período sin leer todo el archivo.

Errores resueltos, buenas prácticas y patrones adoptados en el proyecto.

---

## 🔍 Búsqueda por Tema

| Tema | Entradas | Link |
|---|---|---|
| **Validación** | 6 | [`por-tema/validacion.md`](por-tema/validacion.md) |
| **Multi-tenancy** | 8 | [`por-tema/multi-tenancy.md`](por-tema/multi-tenancy.md) |
| **JPA y ORM** | 5 | [`por-tema/jpa.md`](por-tema/jpa.md) |
| **Seguridad** | 4 | [`por-tema/seguridad.md`](por-tema/seguridad.md) |
| **Billing** | 3 | [`por-tema/billing.md`](por-tema/billing.md) |
| **Testing** | 3 | [`por-tema/testing.md`](por-tema/testing.md) |
| **Performance** | 2 | [`por-tema/performance.md`](por-tema/performance.md) |

---

## 🏷️ Búsqueda por Feature (T-NNN / F-NNN)

| Feature | Estado | Lecciones | Link |
|---|---|---|---|
| T-111 (RBAC) | ✅ Completada | 5 | [`por-feature/T-111-rbac.md`](por-feature/T-111-rbac.md) |
| T-124 (Billing) | ✅ Completada | 3 | [`por-feature/T-124-billing.md`](por-feature/T-124-billing.md) |
| T-125 (Membership.PENDING) | ✅ Completada | 2 | [`por-feature/T-125-membership.md`](por-feature/T-125-membership.md) |
| T-128 (Username collision) | 🔲 Pendiente | 1 | [`por-feature/T-128-username.md`](por-feature/T-128-username.md) |
| F-043 (Reset password) | ✅ Completada | 2 | [`por-feature/F-043-password-recovery.md`](por-feature/F-043-password-recovery.md) |

---

## 📅 Búsqueda por Período

| Período | Entradas | Link |
|---|---|---|
| **Abril 2026** | 15 | [`2026-04.md`](2026-04.md) |
| **Marzo 2026** | 22 | [`2026-03.md`](2026-03.md) |
| **Histórico** | +50 | Archivado |

---

## 📝 Formato de Entrada

Cada lección sigue el patrón:

```markdown
### [YYYY-MM-DD] Título Descriptivo

**Síntoma:** Qué salió mal o qué patrón mejoró (1-2 líneas).
**Causa:** Por qué sucedió (1-2 líneas).
**Solución:** Cómo se resolvió o qué hacer en el futuro (2-3 líneas).
```

**Máximo 6 líneas por entrada.** Ver ejemplos en `2026-04.md`.

---

## 🚀 Cómo Usar

### Si eres IA

1. Abre este README
2. Busca en tabla: tema, feature o período
3. Click en link → documento específico
4. Lee lección relevante
5. Aplica patrón en tu código
6. **Al terminar tarea:** Agrega lección nueva

### Si eres Developer

1. Antes de implementar → Consulta tabla
2. ¿Hay lección relevante? → Aprende de errores
3. Durante código → Sigue patrones documentados
4. Después de terminar → **Agrega lección nueva**

### Si eres Tech Lead

1. Auditar aprendizajes → Revisar `2026-04.md` (último período)
2. Buscar patrón específico → Usar tabla de temas
3. Evaluar feature completa → Ver carpeta `por-feature/`

---

## 📁 Estructura de Archivos

```
lecciones/
├── README.md (este índice)
├── 2026-04.md (entradas de abril)
├── 2026-03.md (entradas de marzo)
├── por-tema/
│   ├── validacion.md (6 entradas)
│   ├── multi-tenancy.md (8 entradas)
│   ├── jpa.md (5 entradas)
│   ├── seguridad.md (4 entradas)
│   ├── billing.md (3 entradas)
│   ├── testing.md (3 entradas)
│   └── performance.md (2 entradas)
└── por-feature/
    ├── T-111-rbac.md (5 entradas)
    ├── T-124-billing.md (3 entradas)
    ├── T-125-membership.md (2 entradas)
    ├── T-128-username.md (1 entrada)
    └── F-043-password-recovery.md (2 entradas)
```

---

## 📊 Matriz: Qué Lección Leer

| Necesidad | Ir a |
|---|---|
| "¿Cómo valido en dominio?" | `por-tema/validacion.md` |
| "¿Cuál es el error que cometimos en T-111?" | `por-feature/T-111-rbac.md` |
| "¿Qué aprendimos en abril?" | `2026-04.md` |
| "¿Hay algo sobre JPA que me ayude?" | `por-tema/jpa.md` |
| "¿Qué pasó con billing?" | `por-feature/T-124-billing.md` |

---

## ✍️ Agregar Nueva Lección

Después de completar una tarea:

1. Identifica: ¿Fue error, patrón o decisión técnica?
2. Categoriza: ¿Por tema o por feature?
3. Agrega entrada a:
   - **Por período:** `2026-04.md` (siempre)
   - **Por tema:** `por-tema/xxx.md` (si aplica)
   - **Por feature:** `por-feature/T-NNN.md` (si está en ROADMAP)

4. Formato: síntoma (1-2 líneas) + causa (1-2 líneas) + solución (2-3 líneas) = máx 6 líneas

---

## 🔗 Referencias Cruzadas

- **Planes de documentación:** [`../../plans/README.md`](../../plans/README.md)
- **Decisiones técnicas:** [`../../design/README.md`](../../design/README.md)
- **Propuestas:** [`../propuestas/README.md`](../propuestas/README.md)
- **Inconsistencias:** [`../inconsistencies/README.md`](../inconsistencies/README.md)

---

**Última actualización:** 2026-04-09  
**Total entradas:** 30+  
**Responsable:** Equipo (actualizar al terminar cada tarea)
