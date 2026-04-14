# T-133 — Documentación bilingüe: mantener contenido en en-US y es-CL

**Estado:** ⬜ Registrada
**Horizonte:** largo plazo
**Módulos afectados:** `doc/` (solo documentación, sin impacto en código)

## Requisito

Mantener el contenido de la documentación en dos idiomas: español (es-CL) como idioma principal
actual y English (en-US) como idioma secundario, para permitir colaboración internacional
y alineación con convenciones de la industria.

Implica definir una estrategia de organización (archivos duplicados, carpetas por idioma,
o sistema de traducción) y establecer un flujo de mantenimiento para que ambas versiones
se mantengan sincronizadas ante cambios.

## Decisiones a tomar antes de activar

Antes de planificar los pasos de implementación, resolver:

1. **Estrategia de organización:**
   - Opción A: archivo duplicado por idioma (`data-model.md` / `data-model.es.md`)
   - Opción B: carpeta por idioma (`en/`, `es/`) dentro de cada sección
   - Opción C: sección por idioma dentro de cada archivo (no recomendado — archivos muy grandes)

2. **Idioma canónico:** ¿cuál es la fuente de verdad cuando hay discrepancia? (sugerido: es-CL)

3. **Alcance inicial:** ¿migrar toda la doc de una vez o por secciones priorizadas?
   (sugerido: empezar por `doc/09-ai/` y `doc/02-functional/frontend/`)

4. **Flujo de mantenimiento:** quién traduce / cómo se detecta que una versión quedó desactualizada.

## Pasos de Implementación

> A detallar cuando se active la tarea y se resuelvan las decisiones previas.

## Verificación

> A definir cuando se active la tarea.

- Cada documento bajo `doc/` existe en ambas versiones (en-US y es-CL).
- Las versiones están sincronizadas en contenido (sin secciones presentes en uno y ausentes en otro).
- El flujo de mantenimiento está documentado en `doc/09-ai/agents.md` o `CLAUDE.md`.
