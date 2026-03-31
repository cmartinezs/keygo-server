# CLAUDE.md — Instrucciones para Claude Code

> Archivo de comportamiento para **Claude Code** en este repositorio.
> Todo el conocimiento técnico (módulos, patrones, entidades, endpoints, convenciones) vive en los
> documentos canónicos referenciados abajo. Este archivo define únicamente cómo debe comportarse el agente.

**Idioma:** responder siempre en **español** salvo que el usuario pida otro idioma explícitamente.

---

## Documentos canónicos — leer antes de cualquier acción

| Documento | Ruta | Contenido |
|---|---|---|
| Quick-start técnico | `AGENTS.md` | Módulos, comandos, patrones, entidades JPA, endpoints, convenciones de nombrado, tests, seguridad |
| Estado del proyecto | `AI_CONTEXT.md` | Resumen ejecutivo, bugs conocidos, convenciones activas, propuestas de alto valor |
| Lecciones aprendidas | `docs/ai/lecciones.md` | Errores resueltos y buenas prácticas — **leer para no repetir errores** |
| Propuestas | `docs/ai/propuestas.md` | Estado de propuestas T-NNN / F-NNN activas y completadas |
| Inconsistencias | `docs/ai/inconsistencias.md` | Inconsistencias detectadas entre docs y código/DB |
| Roadmap | `ROADMAP.md` | Propuestas técnicas (T-NNN) y funcionales (F-NNN) con horizontes |
| Arquitectura | `ARCHITECTURE.md` | Decisiones de diseño, estructura de módulos, flujo general |
| Historial quick-start | `docs/ai/agents-registro.md` | Registro de cambios a `AGENTS.md` |

Adicionalmente, leer los docs del módulo involucrado (`docs/keygo-api/`, `docs/keygo-run/`, etc.).

---

## Comportamiento obligatorio

### 1 · Planificar → Implementar

Antes de escribir cualquier código:

1. Leer los documentos canónicos relevantes.
2. Presentar un **plan explícito**: módulos afectados, archivos a crear/modificar, flujo de datos, tests.
3. Implementar solo después del plan.

### 2 · Documentación: solo bajo orden explícita

- No generar ni actualizar archivos `.md` automáticamente dentro de un chat.
- Crear o actualizar docs solo cuando el usuario lo ordene de forma explícita.
- Colocar siempre en la ruta que corresponde (`docs/<módulo>/`, raíz, etc.).
- **Excepción:** los docs de base de conocimiento AI (`docs/ai/*.md`, `AGENTS.md`) se actualizan
  siempre al concluir una tarea según la tabla de retroalimentación — sin orden explícita.

#### Diagramas

| Prioridad | Herramienta | Cuándo |
|---|---|---|
| 1 | **Mermaid** | Siempre — soportado en GitHub, GitLab, Notion |
| 2 | **PlantUML** | Si el tipo no es expresable en Mermaid (C4, timing, componentes complejos) |
| 3 | **ASCII art** | Último recurso |

### 3 · Retroalimentación obligatoria al concluir una tarea

Evaluar los siguientes eventos y actualizar el documento correspondiente **antes de cerrar**:

| Evento | Documento | Sección |
|---|---|---|
| Error resuelto / bug / comportamiento inesperado | `docs/ai/lecciones.md` | Agregar entrada |
| Mejor patrón o nueva convención adoptada | `docs/ai/lecciones.md` | Agregar entrada |
| Cambio de versión de dependencia o tecnología | `docs/ai/lecciones.md` | Agregar entrada |
| Inconsistencia detectada entre docs y código/DB | `docs/ai/inconsistencias-<cat>.md` + `docs/ai/inconsistencias.md` | Agregar detalle + actualizar índice |
| Propuesta técnica concreta generada | `ROADMAP.md` + `docs/ai/propuestas.md` | Tabla técnica (ID `T-NNN`) |
| Propuesta funcional nueva | `ROADMAP.md` + `docs/ai/propuestas.md` | Tabla funcional (ID `F-NNN`) |
| Propuesta completada / implementada | `docs/ai/propuestas.md` + `ROADMAP.md` | Marcar ✅ + historial |
| Cambio en módulos, rutas, comandos o patrones del quick-start | `AGENTS.md` + `docs/ai/agents-registro.md` | Sección + entrada en registro |
| Nuevo endpoint REST creado o modificado | `docs/postman/KeyGo-Server.postman_collection.json` | Agregar/actualizar request con `pm.test()` |
| Nuevo endpoint REST creado o modificado | `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md` | Sección §14 — inventario de endpoints |
| Nueva migración Flyway (`V{n}__*.sql`) | `docs/data/DATA_MODEL.md` | Diccionario de nuevas tablas |
| Nueva migración Flyway (`V{n}__*.sql`) | `docs/data/ENTITY_RELATIONSHIPS.md` | Diagramas de contexto afectados |
| Nueva migración Flyway (`V{n}__*.sql`) | `docs/data/MIGRATIONS.md` | Sección "Próximas migraciones" |

**Formato de entrada en `docs/ai/lecciones.md`:**
```markdown
### [YYYY-MM-DD] Título descriptivo
**Contexto:** Tarea o escenario que generó el aprendizaje.
**Problema:** Qué falló o qué patrón mejoró.
**Solución / Buena práctica:** Cómo se resolvió o qué hacer en el futuro.
**Archivos clave:** (opcional) Rutas relevantes.
```

### 4 · Propuestas de mejoras al concluir

Incluir propuestas en tres horizontes. Si son relevantes, registrarlas con ID en `ROADMAP.md` y `docs/ai/propuestas.md`:

| Horizonte | Criterio |
|---|---|
| **Corto plazo** | Relacionado con lo recién implementado; bajo esfuerzo |
| **Mediano plazo** | Evoluciones naturales; esfuerzo moderado |
| **Largo plazo** | Capacidades estratégicas; alto esfuerzo o dependencias externas |

### 5 · Git — nunca ejecutar directamente

Nunca ejecutar comandos `git` (commit, push, merge, rebase…). Listar los comandos sugeridos para que el usuario los ejecute manualmente.

---

## Checklist de reglas críticas

Antes de entregar cualquier implementación, verificar:

- [ ] `keygo-domain` no tiene dependencias Spring ni de otros módulos del proyecto
- [ ] Los endpoints usan `BaseResponse<T>` como envelope y `ResponseCode` del enum en `keygo-api`
- [ ] Todas las URLs incluyen `/keygo-server` como `context-path`
- [ ] Imports Jackson son `tools.jackson.databind.*` — **nunca** `com.fasterxml.jackson.databind.*`
- [ ] Las entidades JPA usan `@Getter @Setter @Builder` — **nunca `@Data`**
- [ ] La autenticación es `Authorization: Bearer <jwt>` — no `X-KEYGO-ADMIN`
- [ ] La próxima migración Flyway es `V19__...` (nunca reutilizar ni editar migraciones existentes)
- [ ] No hay secretos, tokens ni `.env` en el código
- [ ] Se sugieren los comandos de verificación: `./mvnw test` y `./mvnw clean package`
