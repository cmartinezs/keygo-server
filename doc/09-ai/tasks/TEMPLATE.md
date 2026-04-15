# Plantilla de Tareas — T-NNN / F-NNN

## Reglas de nombrado e identificación

### Identificador correlativo

| Prefijo | Uso |
|---|---|
| `T-NNN` | Tarea técnica o de producto (bug, feature, refactor, endpoint, etc.) |
| `F-NNN` | Propuesta futura de largo plazo, sin fecha de implementación definida |

- `NNN` es un entero secuencial de **al menos 3 dígitos** con cero a la izquierda (001, 012, 143…).
- El número es **único y permanente**: nunca se reutiliza aunque la tarea sea archivada.
- Para determinar el próximo correlativo: leer el mayor `T-NNN` o `F-NNN` registrado en
  `tasks/README.md` (incluir Historial) y sumar 1.

### Nombre de archivo

```
T-NNN-<slug>.md
F-NNN-<slug>.md
```

- `<slug>`: kebab-case, en inglés, 2–6 palabras que describen el objeto de la tarea.
- Ejemplos válidos: `T-143-platform-user-roles-read-endpoint.md`,
  `T-146-platform-roles-catalog-endpoint.md`, `F-040-rbac-granular.md`.
- No usar verbos de acción en el slug (preferir sustantivos y calificadores).

### Ubicación

Todos los archivos de tarea viven en `doc/09-ai/tasks/`.

---

## Plantilla de archivo

> Copiar desde aquí. Reemplazar cada `{{ ... }}` con el valor correspondiente.
> Eliminar las instrucciones entre `<!-- ... -->` al usar la plantilla.

---

```markdown
# {{ T-NNN }} — {{ Título conciso de la tarea }}

**Estado:** ⬜ Registrada
**Módulos afectados:** <!-- lista: keygo-domain, keygo-app, keygo-api, keygo-supabase, keygo-run, postman, docs, etc. -->

---

## Problema / Requisito

<!-- Una o dos secciones:
     - Qué necesita el usuario / la UI / el sistema.
     - Cuál es el gap o el drift detectado que da origen a esta tarea.
     Mantenerlo conciso; el detalle técnico va en Análisis realizado (cuando se transite). -->

## Relaciones

<!-- Omitir si no hay relaciones conocidas al momento de registrar.
     Agregar cuando aparezcan durante el ciclo de vida.
     Usar los tipos definidos en workflow.md#relaciones-entre-tareas. -->

| Artefacto relacionado | Tipo de relación | Descripción |
|---|---|---|
| <!-- T-NNN / F-NNN / RFC slug / INC-NNN --> | <!-- bloqueante · habilitadora · complementaria · derivada de · absorbe a · absorbida por · relacionada con UI · relacionada con RFC · relacionada con INC --> | <!-- breve descripción --> |

## Solución propuesta

<!-- Solo presente cuando la tarea pasa a 📋 Planificada.
     Incluir: diseño técnico, contrato HTTP si aplica, componentes a crear/modificar. -->

### Contrato esperado

<!-- Si el cambio produce o modifica un endpoint, documentarlo aquí con HTTP method, path,
     request/response shape y códigos de respuesta esperados.
     Omitir esta sub-sección si no aplica. -->

## Pasos de implementación

<!-- Tabla con estado PENDING / APPLIED por fila.
     Usar rutas de archivo desde la raíz del proyecto cuando sea posible. -->

| # | Acción | Archivo | Estado |
|---|---|---|---|
| 1 | <!-- descripción --> | <!-- ruta --> | PENDING |

<!-- Regla: si el endpoint es consumible por UI, el último paso SIEMPRE debe ser:
     "Crear feedback out BE-NNN en doc/02-functional/frontend/feedback/ y actualizar feedback/README.md" -->

## Guía de verificación

<!-- Comandos de test, curl o criterios funcionales para validar que la tarea está completa.
     Incluir al menos un comando de test ejecutable. -->

```bash
# Ejemplo
./mvnw -pl keygo-run -am test -Dtest=SomeControllerTest
```

---

<!-- Todo lo que sigue se agrega durante el ciclo de vida. No completar al registrar la tarea. -->

## Historial de transiciones

<!-- Sección acumulativa. Cada transición agrega una entrada al final, nunca reemplaza. -->
```

---

## Registro en el índice

Al crear un archivo de tarea, agregar **inmediatamente** una fila en `tasks/README.md`:

```markdown
| [T-NNN-<slug>.md](T-NNN-<slug>.md) | {{ Resumen de una línea }} | ⬜ Registrada |
```

- Colocar en la sección de horizonte que corresponda: **Corto plazo**, **Mediano plazo** o
  **Largo plazo**.
- Las tareas completadas se mueven a la sección **Historial — Completadas** con su fecha de cierre.
