# Shared Agent Operations

Política común para agentes AI que trabajan en este repositorio.

## Orden de lectura

1. [agents.md](agents.md)
2. [ai-context.md](ai-context.md)
3. [architecture.md](../03-architecture/architecture.md)
4. [roadmap.md](../05-delivery/roadmap.md)
5. [doc/README.md](../README.md)

## Navegación por índices

La documentación está estructurada de forma jerárquica: cada carpeta tiene un `README.md` con resumen del contenido y un índice con descripción breve de cada documento.

### Reglas de búsqueda

- **Navegar por índices, no por archivos.** Antes de abrir un documento, leer el `README.md` de la carpeta correspondiente para determinar si ese documento es el correcto.
- **Solicitar siempre un documento de partida.** Si el usuario no indica por dónde empezar, preguntar. Si la tarea no tiene un punto de entrada claro, partir desde `doc/README.md` y navegar en cascada por los índices hasta llegar a la fuente correcta.
- **No explorar archivos a ciegas.** Si el índice no resuelve la búsqueda, ir al nivel superior o a la sección más afín antes de abrir archivos individuales.

### Regla de mantenimiento de índices

Toda documentación nueva o ampliada debe mantener el patrón:

- Si se agrega un archivo a una carpeta existente → actualizar el `README.md` de esa carpeta con el nombre del archivo y una descripción breve.
- Si se crea una carpeta nueva → crear su `README.md` con resumen de propósito e índice inicial.
- El índice maestro `doc/README.md` debe actualizarse si se agrega una sección de primer nivel.

## Reglas de trabajo

- Las respuestas deben ser exactas, concisas y precisas; evitar verborrea sin sacrificar claridad.
- Leer documentos canónicos antes de tocar código o contratos.
- Toda documentación nueva debe vivir en `doc/`.
- No usar `99-archive/` como fuente primaria.
- Si una decisión ya es efectiva, dejar ADR o actualizar el existente.
- Si hay drift documental, registrarlo en `inconsistencies.md`.
- Todo diagrama debe escribirse en **Mermaid** cuando sea técnicamente viable.
- Si Mermaid no alcanza para el caso, usar **PlantUML**.
- Diagramas ASCII solo se aceptan como último recurso y deben evitarse en documentación canónica.

## Flujo de trabajo obligatorio antes de implementar

### 1. Análisis previo

Antes de cualquier cambio, analizar la documentación existente y el código para determinar:

- Si ya existe algo equivalente probado → **reutilizarlo**.
- Si existe pero puede mejorar → **refactorizar aplicando el patrón de diseño adecuado**.
- Si es algo nuevo → continuar con el plan.

### 2. Plan de solución

Siempre crear un plan documentado con:

- Problema o requerimiento.
- Solución propuesta (componentes, módulos, patrones involucrados).
- Pasos de implementación ordenados.
- Estado: `PENDIENTE` | `APLICADO`.

El plan debe guardarse en `doc/09-ai/proposals.md` o en el documento de tarea correspondiente.

### 3. RFC para cambios grandes

Si el cambio afecta múltiples módulos, contratos públicos, modelo de datos o arquitectura, se debe crear un RFC en `doc/04-decisions/rfc/` con:

- Contexto y motivación.
- Propuesta detallada (qué, cómo, dónde).
- Impacto en módulos, migraciones y documentación.
- Criterios de aceptación.
- Estado: `BORRADOR` | `APROBADO` | `APLICADO`.

### 4. Esperar aprobación explícita

**No iniciar ninguna implementación** hasta que el usuario indique de forma explícita que el plan y/o RFC debe aplicarse. Proponer, documentar y esperar.

### 5. Registrar ideas futuras detectadas

A medida que el software crece, una feature implementada puede habilitar naturalmente otras futuras. Si se detecta una de estas oportunidades, **registrarla brevemente** en `doc/09-ai/proposals.md` con:

- ID correlativo (`T-NNN` o `F-NNN`).
- Descripción corta (1–2 líneas): qué habilita y por qué tiene sentido.
- Estado: `🔲 Pendiente`.

No desarrollar el análisis completo en ese momento. Queda pendiente para cuando se retome: se hará el análisis, el plan y/o RFC según corresponda.

## Cierre de tarea y compresión de contexto

Al finalizar cualquier tarea, ejecutar el siguiente protocolo antes de dar la conversación por cerrada:

### 1. Preguntar qué debe recordarse

Preguntar al usuario si hay algo de la tarea que deba quedar registrado de forma más extensa (decisiones, aprendizajes, patrones aplicados, problemas encontrados, cambios de diseño).

### 2. Comprimir y persistir

Con base en la respuesta, guardar únicamente lo que no sea derivable del código o del historial git:

| Qué recordar | Dónde guardar |
|---|---|
| Aprendizaje o patrón reutilizable | `lessons-learned.md` |
| Inconsistencia doc-código detectada | `inconsistencies.md` |
| Propuesta futura detectada | `proposals.md` |
| Cambio de reglas de agentes | `agents.md` + `agents-change-log.md` |
| Decisión arquitectónica efectiva | ADR en `04-decisions/adr/` |

### 3. Mantener el principio de índices

Si se crea o modifica un documento durante el cierre, actualizar el `README.md` de su carpeta. El contenido guardado debe ser comprimido: suficiente para orientar una búsqueda futura, no un reporte exhaustivo.

## Regla de mantenimiento

| Cambio | Actualizar |
|---|---|
| Nuevo endpoint o contrato HTTP | OpenAPI, Postman y guía frontend |
| Nueva migración Flyway | `08-reference/data/migrations.md`, `data-model.md`, `entity-relationships.md` |
| Cambio de quick-start o reglas de agentes | `agents.md` y `agents-change-log.md` |
| Nuevo aprendizaje | `lessons-learned.md` |
| Nueva propuesta | `proposals.md` y, si aplica, `05-delivery/roadmap.md` |

## Checklist rápido

- `keygo-domain` sin Spring
- Nullable del dominio expuesto como `Optional<T>`
- Entidades JPA sin `@Data`
- JSONB con `@JdbcTypeCode(SqlTypes.JSON)`
- Seguridad admin con Bearer JWT
- Documentación viva bajo `doc/`
