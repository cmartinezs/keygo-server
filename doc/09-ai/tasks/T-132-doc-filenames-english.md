# T-132 — Migrar nombres de archivos y carpetas de `doc/` a inglés

**Estado:** ⬜ Registrada
**Horizonte:** largo plazo
**Módulos afectados:** `doc/` (solo documentación, sin impacto en código)

## Requisito

Todos los nombres de archivos y carpetas bajo `doc/` deben estar en inglés (`kebab-case`).
La regla ya está activa para archivos nuevos (ver `CLAUDE.md` → Naming Conventions).
Esta tarea cubre la migración de los nombres existentes que aún están en español.

El **contenido** de los documentos permanece en español (es-CL); solo los nombres de archivo/carpeta cambian.

## Pasos de Implementación

> A detallar cuando se active la tarea.

Tareas previas al inicio:
1. Inventariar todos los archivos y carpetas con nombres en español bajo `doc/`.
2. Definir el mapeo `nombre-actual → nombre-en-inglés` para cada uno.
3. Actualizar referencias internas (links entre documentos, entradas en `README.md` de cada carpeta).
4. Actualizar referencias externas (`CLAUDE.md`, `agents.md`, `ai-context.md`, `agent-operations.md`, etc.).
5. Renombrar en git (`git mv`) para preservar historial.

## Verificación

> A definir cuando se active la tarea.

- No existen archivos/carpetas bajo `doc/` con nombres en español.
- Todos los links internos entre documentos resuelven correctamente.
- `CLAUDE.md` y archivos de agentes referencian las nuevas rutas.
