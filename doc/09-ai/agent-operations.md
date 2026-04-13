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

- Las respuestas del asistente deben ser exactas, concisas y precisas; evitar siempre la verborrea innecesaria y limitarse a lo importante.
- Esta regla aplica a la respuesta conversacional del agente, no al código: el código, tests, contratos y documentación técnica deben desarrollarse con el nivel de detalle que realmente requiera la solución.
- Leer documentos canónicos antes de tocar código o contratos.
- Toda documentación nueva debe vivir en `doc/`.
- No usar `99-archive/` como fuente primaria.
- Si una decisión ya es efectiva, dejar ADR o actualizar el existente.
- Si hay drift documental, registrarlo en `inconsistencies/README.md`.
- Los atributos JSON de request body y response body deben escribirse en `snake_case`, no `camelCase`.
- Todo diagrama debe escribirse en **Mermaid** cuando sea técnicamente viable.
- Si Mermaid no alcanza para el caso, usar **PlantUML**.
- Diagramas ASCII solo se aceptan como último recurso y deben evitarse en documentación canónica.

## Ciclo de vida de una tarea

El ciclo completo de estados está definido en [workflow.md](workflow.md).
Resumen de la transición crítica que el agente debe respetar:

```
⬜ Registrada → 🔍 En análisis → 📋 Planificada → 🟢 Aprobada → 🔵 En desarrollo → 🔄 En revisión → ✅ Completada
                                           └─ si queda a la espera de UI: 🧩 Pendiente integración UI → 🛂 Control de cambio → 🔵 En desarrollo / ✅ Completada
```

- El agente puede mover una tarea hasta `📋 Planificada`.
- Solo el usuario puede aprobarla (`🟢 Aprobada`). Sin esa transición, no se implementa.
- Si el cambio es de alto impacto: `🔍 En análisis → 📄 En RFC → 📋 Planificada`.
- Una iniciativa tambien puede **entrar directo por RFC** sin tarea previa; en ese caso el RFC usa el mismo flujo general y actua como artefacto primario.
- Todo RFC mantiene ademas una submaquina interna de fases y subtareas: las subtareas se revisan, se aprueban/replantean/descartan, y solo cuando el conjunto implementable queda aprobado se ejecutan una a una dentro de `🔵 En desarrollo`.
- La verificacion del RFC completo corresponde al estado general `🔄 En revisión`.
- Un RFC o tarea en `🔄 En revisión` puede pasar a `🛂 Control de cambio` aun cuando no exista dependencia UI.
- Si un RFC vuelve de `🛂` o de `🔄` a `🔵`, primero se debe identificar que fase/subtarea existente absorbe el ajuste, o crear una nueva subtarea dentro de la fase correspondiente.
- Si una tarea en `🧩 Pendiente integración UI` recibe un ajuste posterior, debe pasar antes por `🛂 Control de cambio`; solo si el usuario lo aprueba vuelve a `🔵 En desarrollo`.
- Si el control de cambio es rechazado para la tarea original, esta puede cerrarse, pero el nuevo alcance debe quedar como tarea derivada explícita.

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

El plan debe guardarse en `doc/09-ai/tasks/T-NNN-<slug>.md`, registrando la entrada en `doc/09-ai/tasks/README.md`.

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

A medida que el software crece, una feature implementada puede habilitar naturalmente otras futuras. Si se detecta una de estas oportunidades, **registrarla brevemente** creando `doc/09-ai/tasks/T-NNN-<slug>.md` con:

- ID correlativo (`T-NNN` o `F-NNN`).
- Descripción corta (1–2 líneas) como requisito.
- Estado: `🔲 PENDING`.

Agregar la entrada en `doc/09-ai/tasks/README.md`. No desarrollar el análisis completo en ese momento — queda pendiente hasta que se retome explícitamente.

## Cierre de tarea y compresión de contexto

Al finalizar cualquier tarea, ejecutar el siguiente protocolo antes de dar la conversación por cerrada:

### 1. Preguntar qué debe recordarse

Preguntar al usuario si hay algo de la tarea que deba quedar registrado de forma más extensa (decisiones, aprendizajes, patrones aplicados, problemas encontrados, cambios de diseño).

### 2. Comprimir y persistir

Con base en la respuesta, guardar únicamente lo que no sea derivable del código o del historial git:

| Qué recordar | Dónde guardar |
|---|---|
| Aprendizaje o patrón reutilizable | `lessons-learned/<categoría>.md` |
| Inconsistencia doc-código detectada | `inconsistencies/README.md` |
| Propuesta futura detectada | `tasks/T-NNN-<slug>.md` + entrada en `tasks/README.md` |
| Cambio de reglas de agentes | `agents.md` + `agents-change-log.md` |
| Decisión arquitectónica efectiva | ADR en `04-decisions/adr/` |

### 3. Mantener el principio de índices

Si se crea o modifica un documento durante el cierre, actualizar el `README.md` de su carpeta. El contenido guardado debe ser comprimido: suficiente para orientar una búsqueda futura, no un reporte exhaustivo.

## Regla de mantenimiento

| Cambio | Actualizar |
|---|---|
| Nuevo endpoint o contrato HTTP consumible por UI | OpenAPI, Postman y [`02-functional/frontend/frontend-developer-guide.md`](../02-functional/frontend/frontend-developer-guide.md) |
| Cambio de contrato, `ResponseCode` o flujo OAuth consumible por UI | [`02-functional/frontend/frontend-developer-guide.md`](../02-functional/frontend/frontend-developer-guide.md) |
| Nueva migración Flyway | `08-reference/data/migrations.md`, `data-model.md`, `entity-relationships.md` |
| Cambio de quick-start o reglas de agentes | `agents.md` y `agents-change-log.md` |
| Feedback UI↔Backend resuelto | Dejar en el archivo la tarea, RFC o artefacto que materializó la resolución |
| Nuevo aprendizaje | `lessons-learned/<categoría>.md` |
| Nueva propuesta | `tasks/T-NNN-<slug>.md` + `tasks/README.md` y, si aplica, `05-delivery/roadmap.md` |

## Checklist rápido

- `keygo-domain` sin Spring
- Nullable del dominio expuesto como `Optional<T>`
- Entidades JPA sin `@Data`
- JSONB con `@JdbcTypeCode(SqlTypes.JSON)`
- Seguridad admin con Bearer JWT
- Documentación viva bajo `doc/`
