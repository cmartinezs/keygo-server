# Workflow de Features, Tareas Tecnicas y RFCs

Define los estados del ciclo de vida de una tarea (`T-NNN` / `F-NNN`) o de una iniciativa que
entra directamente por RFC, desde que se registra hasta que se completa o descarta.

---

## Estados

| Estado | Emoji | Significado |
|---|---|---|
| Registrada | ⬜ | Mínima descripción. Sin análisis ni plan aún. |
| En análisis | 🔍 | Alguien tomó la tarea: leyendo código y documentación para determinar impacto. |
| Planificada | 📋 | Plan completo en el archivo de tarea. Esperando aprobación explícita. |
| En RFC | 📄 | Cambio de alto impacto. Puede nacer desde una tarea o ingresar directo por RFC. Pendiente de aprobacion dentro del flujo general. |
| Aprobada | 🟢 | Aprobación explícita recibida. Lista para implementar. |
| En desarrollo | 🔵 | Implementación en curso. |
| Bloqueada | 🚫 | No puede avanzar por una dependencia o blocker externo. |
| En revisión | 🔄 | Implementación completa. Verificando contra los criterios del plan. |
| Pendiente integración UI | 🧩 | Backend listo, pero la tarea depende de integración o confirmación desde UI. |
| Control de cambio | 🛂 | Se solicita un ajuste sobre una tarea o RFC ya revisado/entregado; se evalúa si reabre el mismo artefacto o deriva uno nuevo. |
| Completada | ✅ | Verificada y cerrada. |
| Archivada | ⬛ | Cancelada, descartada o absorbida por otra tarea. |

---

## Diagrama de transiciones

```mermaid
flowchart TD
    START(( )) --> REG

    REG["⬜ Registrada"]
    ANA["🔍 En análisis"]
    PLAN["📋 Planificada"]
    RFC["📄 En RFC"]
    APR["🟢 Aprobada"]
    DEV["🔵 En desarrollo"]
    BLO["🚫 Bloqueada"]
    REV["🔄 En revisión"]
    UI["🧩 Pendiente integración UI"]
    CC["🛂 Control de cambio"]
    DONE["✅ Completada"]
    ARC["⬛ Archivada"]
    END(( ))

    REG -->|tarea tomada| ANA
    REG -->|descartada| ARC
    START -->|ingreso directo por RFC| RFC

    ANA -->|"cambio acotado\nplan documentado"| PLAN
    ANA -->|"alto impacto\nmulti-módulo / contrato / datos"| RFC

    RFC -->|RFC aprobado| PLAN
    RFC -->|RFC aprobado sin tarea previa| APR
    RFC -->|RFC rechazado| ARC

    PLAN -->|aprobación explícita| APR
    PLAN -->|requiere re-análisis| ANA
    PLAN -->|descartada| ARC

    APR -->|inicio de implementación| DEV
    APR -->|descartada| ARC

    DEV -->|implementación completa| REV
    DEV -->|dependencia no resuelta| BLO
    BLO -->|blocker resuelto| DEV

    REV -->|requiere integración UI| UI
    REV -->|control de cambio sin UI| CC
    REV -->|verificación OK| DONE
    REV -->|requiere ajustes técnicos directos| DEV

    UI -->|integración UI confirmada| DONE
    UI -->|solicitud de ajuste| CC
    CC -->|control aprobado| DEV
    CC -->|control rechazado + nuevo artefacto| DONE

    DONE --> END
    ARC --> END

    style START fill:#555,stroke:none,color:#fff
    style END   fill:#555,stroke:none,color:#fff
    style REG   fill:#e0e0e0,stroke:#999,color:#333
    style ANA   fill:#fff3cd,stroke:#f0ad4e,color:#333
    style PLAN  fill:#cce5ff,stroke:#004085,color:#333
    style RFC   fill:#d1ecf1,stroke:#0c5460,color:#333
    style APR   fill:#d4edda,stroke:#155724,color:#333
    style DEV   fill:#cce5ff,stroke:#0056b3,color:#fff,font-weight:bold
    style BLO   fill:#f8d7da,stroke:#721c24,color:#333
    style REV   fill:#e2d9f3,stroke:#6f42c1,color:#333
    style UI    fill:#ffe5b4,stroke:#b36b00,color:#333
    style CC    fill:#ffd6cc,stroke:#c05621,color:#333
    style DONE  fill:#d4edda,stroke:#155724,color:#333,font-weight:bold
    style ARC   fill:#343a40,stroke:#343a40,color:#fff
```

---

## Regla transversal — carpeta por estado

Cada archivo de tarea (`T-NNN` / `F-NNN`) debe residir en la carpeta que corresponde
a su estado actual dentro de `doc/09-ai/tasks/`. Al cambiar de estado, el archivo se
mueve a la carpeta del nuevo estado **en el mismo commit o conversación** en que se
actualiza el campo `Estado` del archivo.

| Estado | Carpeta |
|---|---|
| ⬜ Registrada | `registered/` |
| 🔍 En análisis | `in-analysis/` |
| 📋 Planificada | `planned/` |
| 📄 En RFC | `in-rfc/` |
| 🟢 Aprobada | `approved/` |
| 🔵 En desarrollo | `in-development/` |
| 🚫 Bloqueada | `blocked/` |
| 🔄 En revisión | `in-review/` |
| 🧩 Pendiente integración UI | `pending-ui/` |
| 🛂 Control de cambio | `change-control/` |
| ✅ Completada | `completed/` |
| ⬛ Archivada | `archived/` |

Reglas complementarias:

- Los links en `tasks/README.md` deben actualizarse al mover el archivo.
- `README.md`, `TEMPLATE.md` y carpetas de estado permanecen en la raíz de `tasks/`.
- Las carpetas vacías no se eliminan; se crean cuando se necesitan.

---

## Criterios de transición

> **Quién activa** — 👤 usuario | 🤖 agente | 👤🤖 cualquiera.
> El prompt pattern es el texto **exacto o equivalente** que debe aparecer en la conversación para que la transición sea válida. Sin ese patrón, el agente no cambia el estado.

---

## Regla transversal — contenido por etapa

Cambiar el estado de una tarea o RFC **no basta por sí solo**. En cada etapa, el agente debe
persistir en el artefacto principal (`T-NNN` o RFC) el contenido generado durante esa fase,
dejando trazabilidad útil para retomar el trabajo después.

### Ubicación del contenido de transiciones

El contenido generado en cada cambio de estado debe quedar **al final de la documentación
inicial del artefacto principal**, en orden cronológico, como historial acumulado de transiciones.

Reglas:

- La documentación inicial del artefacto principal (requisito, análisis base, solución, pasos,
  verificación, etc.) se mantiene al inicio del archivo.
- Cada transición agrega su contenido nuevo **al final**, no reemplaza ni dispersa el
  historial en distintas partes del documento.
- El objetivo es que el artefacto conserve no solo el estado actual, sino también el
  contenido producido en cada cambio de estado.
- Si una sección base necesita actualización, puede ajustarse, pero el contenido propio de
  la transición debe igual quedar registrado al final como trazabilidad.
- Se recomienda usar secciones cronológicas claras, por ejemplo:
  `## Historial de transiciones`, `### 2026-04-13 — 🔍 En análisis`,
  `### 2026-04-13 — 📋 Planificada`, etc.

### Contenido mínimo esperado por etapa

| Etapa | Contenido que debe agregarse o actualizarse en el artefacto principal |
|---|---|
| `🔍 En análisis` | Sección `## Análisis realizado` con hallazgos, impacto técnico, riesgos, drift detectado y decisiones preliminares. |
| `📋 Planificada` | Solución propuesta consolidada, pasos ordenados de implementación y guía de verificación. |
| `📄 En RFC` | Referencia al RFC creado, motivo del RFC, resumen del impacto detectado y, si el RFC es artefacto primario, sus fases y subtareas con estado inicial. |
| `🟢 Aprobada` | Nota breve de aprobación explícita recibida y alcance aprobado si hubo ajustes. |
| `🔵 En desarrollo` | Progreso real de implementación: pasos marcados `APPLIED`, decisiones tomadas y cualquier ajuste relevante al plan. |
| `🚫 Bloqueada` | Descripción concreta del blocker, dependencia o decisión faltante, y condición de desbloqueo. |
| `🔄 En revisión` | Resultado de verificación, pendientes detectados y alcance realmente implementado. |
| `🧩 Pendiente integración UI` | Qué debe integrar la UI, artefactos/notas entregadas al frontend y condición para dar la tarea por cerrada. |
| `🛂 Control de cambio` | Solicitud de ajuste detectada tras la entrega, decisión tomada (aprobar o rechazar), impacto en alcance y referencia a la tarea derivada si corresponde. |
| `✅ Completada` | Cierre de tarea con resultado final, referencias a validación/documentación actualizada y fecha de cierre si aplica. |
| `⬛ Archivada` | Motivo del descarte, absorción o cancelación, con referencia cruzada si fue absorbida por otra tarea/RFC. |

Si el contenido detallado vive mejor en otro artefacto (por ejemplo un RFC), la tarea debe
igual dejar un resumen y el link correspondiente; nunca debe quedar solo el cambio de estado.

---

## Regla transversal — ingreso directo por RFC

No toda iniciativa debe nacer como `T-NNN` o `F-NNN`.

Cuando el primer artefacto correcto es una decision formal, un cambio multi-modulo o una propuesta
de arquitectura, se puede **ingresar directo por RFC** sin crear antes una tarea.

Reglas:

1. El RFC entra al flujo general en `📄 En RFC`.
2. El RFC puede luego:
   - derivar una tarea contenedora o una o varias tareas hijas;
   - seguir como artefacto primario hasta `🟢 Aprobada`, `🔵 En desarrollo`, `🔄 En revisión`,
     `🧩 Pendiente integración UI` y `✅ Completada`;
   - archivarse si se rechaza o descarta.
3. Si no existe tarea previa, la trazabilidad del estado general vive en el propio RFC.
4. Si despues aparecen tareas derivadas, estas deben referenciar al RFC con tipo de relacion
   `derivada de` o `relacionada con RFC`, segun corresponda.

---

## Submáquina interna de RFC

El estado general del RFC **no reemplaza** su control interno. Un RFC puede estar, por ejemplo,
`🔵 En desarrollo` en el flujo general y al mismo tiempo llevar internamente una fase en revisión y
otra ya implementada.

### Regla

Cada RFC debe poder descomponerse en:

- fases;
- subtareas por fase;
- decision por subtarea: `aprobada`, `replanteada` o `descartada`.

### Flujo interno canónico

```mermaid
flowchart TD
    F0["Fases definidas"]
    F1["Subtareas propuestas"]
    F2["Subtareas revisadas\naprobadas / replanteadas / descartadas"]
    F3["Conjunto de subtareas aprobado"]
    F4["Implementacion secuencial\nsubtarea 1..N"]
    F5["Revision integral del RFC"]
    F6["Salida al flujo general\n✅ Completada / 🧩 Pendiente integración UI /\nvolver a 🔵 En desarrollo"]

    F0 --> F1
    F1 --> F2
    F2 -->|hay subtareas replanteadas| F1
    F2 -->|todas las subtareas resueltas| F3
    F3 --> F4
    F4 -->|quedan subtareas aprobadas pendientes| F4
    F4 -->|ultima subtarea implementada| F5
    F5 --> F6
```

### Reglas operativas

1. Cada fase puede tener una o varias subtareas.
2. Ninguna subtarea se implementa mientras el conjunto pendiente de esa fase no haya sido revisado.
3. En la revision de subtareas, cada una debe quedar explicitamente como:
   - `aprobada`;
   - `replanteada`;
   - `descartada`.
4. Las subtareas `replanteadas` deben volver a revision antes de entrar a implementacion.
5. Las subtareas `descartadas` deben quedar trazadas con su motivo; no desaparecen silenciosamente.
6. Una vez que el conjunto completo de subtareas aprobadas queda listo, la implementacion se hace
   **una por una**.
7. Terminada la ultima subtarea, se hace una revision integral del RFC completo antes de decidir la
   salida del flujo general.
8. La revision integral ocurre bajo estado general `🔄 En revisión`.
9. La implementacion secuencial de subtareas (`subtarea 1..N`) ocurre bajo estado general `🔵 En desarrollo`.
10. La revision integral puede terminar en:
    - `✅ Completada`, si el RFC quedo cerrado de punta a punta;
    - `🧩 Pendiente integración UI`, si backend/RFC quedo listo pero falta cierre con UI;
    - `🛂 Control de cambio`, si aparece una solicitud de ajuste posterior a la revision aunque no exista dependencia UI;
    - vuelta a `🔵 En desarrollo`, si la revision integral detecta ajustes adicionales.
11. Si un RFC vuelve a `🔵 En desarrollo`, se debe identificar explicitamente **que fase y que subtarea existente**
    absorben el cambio. El ajuste no debe quedar como trabajo flotante: debe agregarse a una subtarea ya
    existente o crear una nueva subtarea dentro de la fase correspondiente, manteniendo trazabilidad sobre
    lo ya implementado.

### Contenido minimo que debe reflejar un RFC con submáquina interna

El RFC debe mantener, como minimo:

| Sección | Contenido esperado |
|---|---|
| `## Fases` | Lista ordenada de fases del RFC |
| `## Subtareas por fase` | Tabla o listas con cada subtarea y su estado interno |
| `## Decisiones de revisión` | Qué subtareas fueron aprobadas, replanteadas o descartadas y por qué |
| `## Avance de implementación` | Qué subtareas ya se implementaron y cuáles faltan |
| `## Revisión integral` | Resultado final del RFC completo antes de salir a `✅`, `🧩` o `🛂` |
| `## Ajustes posteriores` | Si vuelve a `🔵`, qué fase/subtarea existente absorbe el cambio o qué nueva subtarea se agrega |

---

## Relaciones entre tareas

Cuando una tarea referencie otra (`T-NNN`, `F-NNN`, `RFC-NNN`, `INC-NNN`), no basta con nombrarla:
se debe indicar explícitamente el **tipo de relación**.

### Regla

Cada tarea que tenga dependencias, afinidad funcional o impacto cruzado debe incluir una sección
`## Relaciones` con una tabla como esta:

| Artefacto relacionado | Tipo de relación | Descripción |
|---|---|---|
| `T-NNN` | `bloqueante` | Esta tarea no puede avanzar o cerrarse hasta que la otra se resuelva. |
| `T-MMM` | `habilitadora` | Esta tarea habilita trabajo posterior en la otra. |
| `T-PPP` | `complementaria` | Ambas cubren partes distintas de una misma capacidad y conviene tratarlas coordinadamente. |

### Tipos de relación permitidos

| Tipo | Significado |
|---|---|
| `bloqueante` | La tarea relacionada impide avanzar o cerrar la actual. |
| `habilitadora` | La tarea actual o la relacionada habilita a la otra como prerequisito técnico o funcional. |
| `complementaria` | Ambas tareas se refuerzan, pero ninguna bloquea estrictamente a la otra. |
| `derivada de` | La tarea nace como consecuencia directa de otra tarea, RFC o inconsistencia. |
| `absorbe a` | La tarea actual incorpora el alcance de otra, que luego puede archivarse. |
| `absorbida por` | La tarea actual deja de avanzar por separado y su alcance pasa a otra. |
| `relacionada con UI` | La tarea depende de integración, validación o coordinación con frontend/UI. |
| `relacionada con RFC` | La tarea implementa, detalla o depende de una decisión formalizada en RFC. |
| `relacionada con INC` | La tarea corrige o nace desde una inconsistencia documentada. |

### Criterios

- No usar solo “relacionada con” sin tipificar la relación.
- Si la relación afecta el orden de ejecución, usar `bloqueante` o `habilitadora`, no
  `complementaria`.
- Si una tarea pasa a depender de otra durante la ejecución, actualizar también su archivo y no
  solo el estado.
- Si una tarea queda bloqueada por otra, la relación debe aparecer tanto en `## Relaciones` como
  en la documentación del bloqueo.

---

### ⬜ Registrada → 🔍 En análisis

**Quién activa:** 👤 usuario

**Criterios:**
- El usuario decide tomar la tarea.
- El agente actualiza `**Estado:**` en el archivo de la tarea y en `tasks/README.md`.
- El agente agrega o actualiza la sección `## Análisis realizado` con el contenido producido.

**Prompt pattern:**
```
Analiza T-NNN
```
```
Quiero trabajar en T-NNN
```
```
Toma T-NNN y analízala
```

---

### 🔍 En análisis → 📋 Planificada

**Quién activa:** 🤖 agente (al invocar `/plan`)

**Criterios:**
- Análisis completado: cambio acotado, no requiere RFC.
- El archivo de la tarea tiene: requisito claro, módulos afectados, pasos ordenados y guía de verificación.
- Estado actualizado en el archivo y en `tasks/README.md`.
- El contenido generado durante el análisis queda persistido en la tarea; no se reemplaza solo con el plan.

**Prompt pattern:**
```
/plan T-NNN
```
```
Planifica T-NNN
```

---

### 🔍 En análisis → 📄 En RFC

**Quién activa:** 🤖 agente (al invocar `/plan` cuando detecta alto impacto)

**Criterios:**
- El análisis determina que el cambio afecta múltiples módulos, contratos públicos, modelo de datos o arquitectura.
- El agente crea el RFC en `doc/04-decisions/rfc/` con estado `BORRADOR` y lo referencia en el archivo de la tarea.
- La tarea conserva el análisis y agrega el resumen del RFC generado.
- Si luego se decide que la tarea ya no es el artefacto principal, debe dejar referencia cruzada al RFC y explicitar que el seguimiento principal continua ahi.

**Prompt pattern:** el mismo que para Planificada — el agente decide la ruta según el impacto detectado:
```
/plan T-NNN
```
```
Planifica T-NNN
```

---

### Inicio directo → 📄 En RFC

**Quién activa:** 👤 usuario o 🤖 agente durante planificación de alto impacto

**Criterios:**
- La iniciativa nace directamente como RFC y no aporta valor crear primero una tarea.
- El RFC queda creado como artefacto primario con contexto, impacto, fases y subtareas iniciales.
- Si mas adelante se crean tareas derivadas, estas referencian al RFC y no reemplazan su trazabilidad historica.

**Prompt pattern:**
```
Crea un RFC para [tema]
```
```
Esto debe entrar directo por RFC
```
```
Necesito un RFC, no una tarea
```

---

### 📄 En RFC → 📋 Planificada

**Quién activa:** 👤 usuario (aprueba el RFC) + 🤖 agente (detalla el plan)

**Criterios:**
- Usuario aprueba el RFC explícitamente.
- Existe una tarea asociada o se decide crearla en este punto.
- El agente actualiza el RFC a `APROBADO`, detalla los pasos en el archivo de la tarea y cambia el estado a `📋 Planificada`.
- La tarea agrega el contenido nuevo producido en la etapa RFC/aprobación, no solo la referencia.

**Prompt pattern:**
```
Apruebo RFC-NNN, detalla el plan de T-NNN
```
```
RFC-NNN aprobado, procede con el plan
```

---

### 📄 En RFC → 🟢 Aprobada

**Quién activa:** 👤 usuario

**Criterios:**
- El RFC es el artefacto primario y no se va a convertir primero en una tarea separada.
- El usuario aprueba explícitamente el RFC como paquete de trabajo.
- Las fases y subtareas del RFC quedaron revisadas, con cada subtarea aprobada, replanteada o descartada.
- Solo cuando el conjunto implementable queda aprobado, el RFC puede pasar a `🟢 Aprobada`.

**Prompt pattern:**
```
Apruebo este RFC, implementalo por fases
```
```
RFC aprobado, parte por sus subtareas
```

---

### 📋 Planificada → 🟢 Aprobada

**Quién activa:** 👤 usuario **exclusivamente**

**Criterios:**
- El usuario indica de forma explícita que el plan debe aplicarse.
- Sin este prompt, el agente **no inicia implementación bajo ninguna circunstancia**.
- La tarea deja registrada la aprobación explícita o el ajuste de alcance aprobado si existió.

**Prompt pattern:**
```
Aplica T-NNN
```
```
Implementa T-NNN
```
```
Apruebo T-NNN, procede
```

---

### 🟢 Aprobada → 🔵 En desarrollo

**Quién activa:** 🤖 agente (automático al iniciar implementación)

**Criterios:**
- El agente actualiza el estado al comenzar el primer paso de implementación.
- No requiere prompt adicional — es consecuencia directa de la aprobación.
- El agente comienza a reflejar en la tarea el avance real (`APPLIED`, decisiones, ajustes).

---

### 🔵 En desarrollo → 🚫 Bloqueada

**Quién activa:** 👤🤖 cualquiera

**Criterios:**
- Se detecta una dependencia no resuelta (otra tarea, decisión pendiente, recurso externo).
- El blocker debe quedar documentado en el archivo de la tarea: qué bloquea y por qué.
- Debe agregarse explícitamente la condición para desbloquear la tarea.

**Prompt pattern:**
```
T-NNN está bloqueada por [razón]
```
```
Bloquea T-NNN, depende de T-MMM
```

---

### 🔄 En revisión → 🛂 Control de cambio

**Quién activa:** 👤 usuario

**Criterios:**
- Existe una solicitud de ajuste posterior a la revision integral del artefacto.
- No requiere que exista dependencia UI; puede venir de negocio, arquitectura, backend o revision funcional.
- El agente documenta el cambio pedido, su impacto y si se incorpora al mismo artefacto o si debe derivarse.

**Prompt pattern:**
```
Pasa RFC-NNN a control de cambio
```
```
Abre control de cambio para T-NNN
```
```
Hay ajustes sobre este RFC; llévalo a control de cambio
```

---

### 🚫 Bloqueada → 🔵 En desarrollo

**Quién activa:** 👤 usuario

**Criterios:**
- El blocker fue resuelto.
- El agente retoma la implementación desde el último paso pendiente.
- La tarea debe registrar cómo se resolvió el bloqueo antes de seguir.

**Prompt pattern:**
```
El blocker de T-NNN está resuelto, continúa
```
```
Continúa T-NNN, [razón de desbloqueo]
```

---

### 🔵 En desarrollo → 🔄 En revisión

**Quién activa:** 🤖 agente (automático al completar implementación)

**Criterios:**
- Todos los pasos de implementación marcados como `APPLIED`.
- El código compila y los tests pasan.
- El agente actualiza el estado y notifica al usuario que está listo para verificación.
- La tarea resume el resultado implementado y la verificación realizada.
- Si la implementación reveló trabajo adicional, el agente puede registrar una o más tareas
  derivadas antes o durante esta transición.

---

### 🔄 En revisión → 🧩 Pendiente integración UI

**Quién activa:** 👤🤖 cualquiera

**Criterios:**
- El backend ya quedó implementado y verificado desde su lado.
- Falta que la UI consuma, adapte o confirme la integración para poder cerrar la tarea.
- La tarea documenta explícitamente qué debe hacer UI, qué contrato queda disponible y cuál es la condición de cierre.

**Prompt pattern:**
```
Deja T-NNN pendiente de integración UI
```
```
T-NNN depende de integración UI
```
```
Pasa T-NNN a pendiente UI
```

---

### 🧩 Pendiente integración UI → ✅ Completada

**Quién activa:** 👤 usuario

**Criterios:**
- La integración o confirmación desde UI ya ocurrió.
- La tarea deja trazabilidad de la confirmación recibida o del criterio cumplido.

**Prompt pattern:**
```
UI confirmó T-NNN, ciérrala
```
```
Cierra T-NNN, integración UI completa
```

---

### 🧩 Pendiente integración UI → 🛂 Control de cambio

**Quién activa:** 👤 usuario

**Criterios:**
- La integración UI detectó ajustes necesarios en backend.
- La tarea documenta qué hallazgo de UI obliga a evaluar un ajuste sobre una entrega ya hecha.
- El cambio se registra primero como `🛂 Control de cambio`; desde ahí se decide si reabre la misma tarea o deriva una nueva.

**Prompt pattern:**
```
UI detectó ajustes en T-NNN
```
```
Pasa T-NNN a control de cambio
```
```
Abre control de cambio para T-NNN
```

---

### 🛂 Control de cambio → 🔵 En desarrollo

**Quién activa:** 👤 usuario

**Criterios:**
- El usuario aprueba que el ajuste forme parte de la misma tarea.
- La tarea documenta el alcance adicional aceptado y qué pasos vuelven a estado de trabajo.

**Prompt pattern:**
```
Apruebo el control de cambio de T-NNN
```
```
Reabre T-NNN con el control de cambio aprobado
```

---

### 🛂 Control de cambio → ✅ Completada

**Quién activa:** 👤 usuario

**Criterios:**
- El usuario decide no reabrir la tarea original con el ajuste solicitado.
- Antes del cierre, el agente crea una nueva tarea `T-NNN` derivada que incorpore el cambio rechazado.
- La tarea original documenta la decisión, referencia la nueva tarea y cierra manteniendo intacto su alcance aprobado.

**Prompt pattern:**
```
No apruebo el control de cambio de T-NNN; crea una nueva tarea y cierra esta
```
```
El ajuste de T-NNN va en una tarea nueva; completa la actual
```

---

### 🔄 En revisión → ✅ Completada

**Quién activa:** 👤 usuario

**Criterios:**
- Criterios de verificación del plan cumplidos.
- Documentación actualizada según el tipo de cambio (migrations, OpenAPI, frontend guide, etc.).
- `roadmap.md` actualizado si aplica.
- La tarea incorpora el cierre con el resultado final y deja trazabilidad suficiente para consulta futura.
- Si quedaron extensiones, complementos o deuda técnica fuera del alcance, deben quedar
  registradas como tareas derivadas antes del cierre o dentro del mismo cierre.

**Prompt pattern:**
```
T-NNN verificada, ciérrala
```
```
Cierra T-NNN
```
```
T-NNN completada
```

---

### 🔄 En revisión → 🔵 En desarrollo

**Quién activa:** 👤 usuario

**Criterios:**
- La verificación detectó un fallo o caso no cubierto.
- El usuario debe indicar qué falló para que el agente retome desde el paso correcto.
- La tarea documenta qué falló en revisión antes de volver a desarrollo.

**Prompt pattern:**
```
T-NNN requiere ajustes: [descripción del problema]
```
```
Vuelve a desarrollo en T-NNN, [qué falló]
```

---

### Cualquier estado → ⬛ Archivada

**Quién activa:** 👤 usuario

**Criterios:**
- La tarea fue cancelada, descartada o absorbida por otra.
- El agente documenta la razón en el archivo de la tarea y actualiza el estado.
- El archivo **no se elimina** — mantiene el historial.
- La tarea deja persistido el motivo y la referencia cruzada correspondiente si aplica.

**Prompt pattern:**
```
Archiva T-NNN
```
```
Descarta T-NNN por [razón]
```
```
T-NNN absorbida por T-MMM, archívala
```

---

---

## Ciclo de vida de inconsistencias (INC-NNN)

Define el flujo desde que se detecta una inconsistencia hasta que se resuelve.
Índice completo en [`inconsistencies/README.md`](inconsistencies/README.md).

### Estados de una INC

| Estado | Emoji | Significado |
|---|---|---|
| Pendiente | 🔲 | Detectada, sin corrección en curso |
| En corrección | 🔧 | Tiene tarea T-NNN asociada en desarrollo |
| Resuelta | ✅ | Fix verificado, documentado y movido a Historial |

### Diagrama

```mermaid
flowchart TD
    START(( )) --> DET

    DET["🔍 Detectada\n(usuario o agente)"]
    REG["🔲 Pendiente\nINC-NNN registrada"]
    CRIT{¿🔴 Crítica?}
    BLOK["Resolver antes de\ncerrar la tarea actual"]
    IMPL{¿Requiere\nimplementación?}
    TASK["🔧 En corrección\nT-NNN vinculada"]
    FIX["Fix directo\n(docs / config)"]
    DONE["✅ Resuelta\n+ mover a Historial"]
    END(( ))

    START --> DET
    DET --> REG
    REG --> CRIT
    CRIT -->|Sí| BLOK
    CRIT -->|No| IMPL
    BLOK --> IMPL
    IMPL -->|Sí| TASK
    IMPL -->|No| FIX
    TASK --> DONE
    FIX --> DONE
    DONE --> END

    style START fill:#555,stroke:none,color:#fff
    style END   fill:#555,stroke:none,color:#fff
    style DET   fill:#fff3cd,stroke:#f0ad4e,color:#333
    style REG   fill:#e0e0e0,stroke:#999,color:#333
    style CRIT  fill:#f8d7da,stroke:#721c24,color:#333
    style BLOK  fill:#f8d7da,stroke:#721c24,color:#333
    style IMPL  fill:#d1ecf1,stroke:#0c5460,color:#333
    style TASK  fill:#cce5ff,stroke:#0056b3,color:#333
    style FIX   fill:#cce5ff,stroke:#004085,color:#333
    style DONE  fill:#d4edda,stroke:#155724,color:#333,font-weight:bold
```

---

### Detección y registro

**Quién activa:** 👤 usuario | 🤖 agente

**Criterios:**
- Cualquiera puede detectar una inconsistencia durante análisis, revisión de código o documentación.
- El agente crea el archivo `INC-NNN-<slug>.md` y lo registra en `inconsistencies/README.md`.
- Si es 🔴 Crítica, debe resolverse antes de cerrar la tarea en curso.

**Prompt pattern (usuario):**
```
Registra inconsistencia: [descripción breve]
```
```
Detecté una inconsistencia en [área]: [descripción]
```

**Prompt pattern (agente — al detectarla durante una tarea):**
> El agente crea la INC automáticamente sin esperar prompt explícito.

**Resultado esperado del agente:**
1. Crear `doc/09-ai/inconsistencies/INC-NNN-<slug>.md` usando la plantilla.
2. Agregar fila en la tabla **Abiertas** de `inconsistencies/README.md`.

---

### 🔲 Pendiente → 🔧 En corrección (vía tarea)

**Quién activa:** 👤 usuario

**Criterios:**
- La INC requiere trabajo de código, migración o esfuerzo mayor a una corrección documental.
- El agente crea `T-NNN-<slug>.md`, lo registra en `tasks/README.md` y vincula ambos archivos.

**Prompt pattern:**
```
Crea tarea para INC-NNN
```
```
Registra INC-NNN como tarea
```
```
INC-NNN necesita una tarea, créala
```

**Resultado esperado del agente:**
1. Crear `doc/09-ai/tasks/T-NNN-<slug>.md` con el requisito extraído de la INC.
2. Registrar en `doc/09-ai/tasks/README.md` con estado `⬜ Registrada`.
3. Actualizar `**Estado:**` de la INC a `🔧 En corrección`.
4. Actualizar `**Tarea relacionada:**` en `INC-NNN-<slug>.md` con el link `[T-NNN](../tasks/T-NNN-slug.md)`.

---

### 🔧 En corrección → ✅ Resuelta (al cerrar la tarea vinculada)

**Quién activa:** 🤖 agente (automático al ejecutar cierre de T-NNN)

**Criterios:**
- La tarea `T-NNN` vinculada a la INC transiciona a `✅ Completada`.
- El agente verifica si la tarea tiene `**Inconsistencia relacionada:**` o si la INC tiene la tarea vinculada y actualiza la INC automáticamente.

**Prompt pattern:** el mismo que cierra la tarea:
```
T-NNN verificada, ciérrala
```
```
Cierra T-NNN
```

**Resultado esperado del agente al cerrar T-NNN:**
1. Marcar `**Estado:** ✅ Resuelta` en la INC vinculada.
2. Completar `**Resuelta:** YYYY-MM-DD` en la INC.
3. Mover la fila de **Abiertas** a **Historial** en `inconsistencies/README.md`.

---

### 🔲 Pendiente → ✅ Resuelta (fix directo, sin tarea)

**Quién activa:** 👤 usuario

**Criterios:**
- El fix es solo documental o de configuración; no requiere una tarea formal.
- El agente aplica el fix, actualiza la INC y la mueve a Historial.

**Prompt pattern:**
```
Resuelve INC-NNN
```
```
Aplica fix de INC-NNN
```
```
Marca INC-NNN como resuelta
```

---

## Reglas generales

- El estado vive en el campo `**Estado:**` del archivo de la tarea y en la columna de estado de `tasks/README.md`. Ambos deben mantenerse sincronizados.
- Solo el usuario puede mover una tarea de `📋 Planificada` → `🟢 Aprobada`. El agente no aprueba por cuenta propia.
- Una tarea `🚫 Bloqueada` debe tener documentado el blocker. Sin esa nota, el bloqueo no es válido.
- Las tareas `✅ Completadas` no se eliminan de `tasks/README.md` — se mueven a la sección **Historial** al final del archivo.
- `🧩 Pendiente integración UI` no reemplaza a `🚫 Bloqueada`: se usa cuando el backend ya está listo
  y la dependencia restante es la adopción/confirmación desde frontend, no un bloqueo técnico del backend.
- `🛂 Control de cambio` se usa cuando una tarea ya entregada recibe una solicitud de ajuste posterior; no implica aprobación automática del nuevo alcance.
- Una tarea implementada puede originar **una o más tareas derivadas**. Esto incluye tareas
  de tipo derivada, complementaria, extensión funcional o correctiva cuando queda deuda técnica.
- Si durante `🔵 En desarrollo`, `🔄 En revisión` o `✅ Completada` se detecta trabajo nuevo que no
  corresponde mezclar en la tarea actual, el agente debe crear `T-NNN-<slug>.md`, registrarla en
  `tasks/README.md` y referenciarla explícitamente desde la tarea origen.
- La tarea origen debe dejar ese registro en su historial de transiciones al final del documento,
  indicando el tipo de derivación, la razón y el/los links a las tareas creadas.
- Si un control de cambio se rechaza para la tarea original, la nueva necesidad **debe** registrarse como una tarea derivada antes de cerrar la anterior.
