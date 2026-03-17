# CLAUDE.md — Reglas para agentes

> Este archivo es para agentes que soportan reglas a nivel de repo (Claude, Copilot agent mode, etc.).
> Si estás usando **GitHub Copilot Chat**, la fuente principal de instrucciones es `.github/copilot-instructions.md`.

## Identidad del proyecto

- Repo: **KeyGo Server** — servicio de identidad/accesos (IAM) open source.
- Stack: Java 21 + Spring Boot, monorepo Maven multi-módulo.
- Arquitectura: Hexagonal / Ports & Adapters.
- Módulo ejecutable: `keygo-run`.

## Flujo de trabajo obligatorio

> Aplica a **toda** acción que implique generar o modificar código, configuración o estructura.

### Planificar → Implementar

Antes de escribir cualquier código el agente **debe**:

1. Consultar los documentos de referencia obligatorios (ver abajo).
2. Presentar un **plan explícito**: módulos afectados, archivos a crear/modificar, flujo de datos y tests a agregar.
3. Solo después de ese plan proceder a implementar.

### Documentación: solo bajo orden explícita

- **Dentro de un mismo contexto de chat, NO generar documentación automáticamente.**
- Crear o actualizar archivos `.md` solo cuando el usuario lo ordene de forma explícita.
- Toda documentación va en la ruta que le corresponde (`docs/<módulo>/`, raíz, etc.) — nunca en una ruta arbitraria.

### Documentos de referencia obligatorios

Antes de cualquier acción consultar:

| Documento | Ruta | Para qué sirve |
|---|---|---|
| Contexto general AI | `AI_CONTEXT.md` | Estado del proyecto, bugs, convenciones, lecciones aprendidas |
| Arquitectura | `ARCHITECTURE.md` | Decisiones de diseño y estructura de módulos |
| Guía de agentes | `AGENTS.md` | Quick-start: módulos, comandos, patrones y flujos concretos |
| Instrucciones Copilot | `.github/copilot-instructions.md` | Lineamientos para Copilot Chat / agent mode |
| Este archivo | `CLAUDE.md` | Reglas de oro del agente |

Adicionalmente, leer los docs específicos del módulo involucrado (`docs/keygo-api/`, `docs/keygo-run/`, etc.).

### Aprendizaje continuo y retroalimentación obligatoria

Al concluir **cualquier tarea**, el agente debe evaluar si ocurrió alguno de estos eventos y actualizar el documento correspondiente **antes de cerrar la tarea**:

| Evento | Documento | Sección |
|---|---|---|
| Error resuelto (compilación, test, comportamiento inesperado) | `AI_CONTEXT.md` | `## Lecciones aprendidas` |
| Mejor patrón de implementación encontrado | `AI_CONTEXT.md` | `## Lecciones aprendidas` |
| Cambio de versión de dependencia o tecnología | `AI_CONTEXT.md` | `## Lecciones aprendidas` |
| Nueva convención acordada | `AI_CONTEXT.md` | `## Lecciones aprendidas` |
| Propuesta recurrente o de alto valor | `AI_CONTEXT.md` | `## Propuestas de mejoras futuras` |
| Cambio en módulos, rutas, comandos o patrones del quick-start | `AGENTS.md` | Sección correspondiente |

> ⚠️ Esta actualización **no está sujeta** a la regla "solo bajo orden explícita". Los documentos
> de base de conocimiento AI (`AI_CONTEXT.md`, `AGENTS.md`) son parte del ciclo de trabajo del agente.

**Formato de entrada en `## Lecciones aprendidas`:**
```markdown
### [YYYY-MM-DD] Título descriptivo
**Contexto:** Tarea o escenario que generó el aprendizaje.
**Problema:** Qué falló o qué patrón mejoró.
**Solución / Buena práctica:** Cómo se resolvió o qué hacer en el futuro.
**Archivos clave:** (opcional) Rutas relevantes.
```

### Git — prohibición de ejecución directa

- El agente **nunca debe ejecutar comandos `git`** directamente (commit, push, merge, rebase, etc.).
- Listar los comandos sugeridos para que el usuario los ejecute manualmente.

### Propuesta de mejoras futuras

Al concluir cualquier tarea, el agente **debe** incluir propuestas organizadas en tres horizontes:

| Horizonte | Criterio orientativo |
|---|---|
| **Corto plazo** | Relacionado con lo recién implementado; bajo esfuerzo |
| **Mediano plazo** | Evoluciones naturales; esfuerzo moderado |
| **Largo plazo** | Capacidades estratégicas; alto esfuerzo o dependencias externas |

- Las propuestas deben ser **concretas y accionables**, no genéricas.
- Si son recurrentes o relevantes para el proyecto, registrarlas en `AI_CONTEXT.md` bajo `## Propuestas de mejoras futuras`.

## Reglas de oro

1. **No inventes** estructura del repo: apóyate en los módulos existentes (`keygo-api`, `keygo-app`, etc.).
2. Mantén `keygo-domain` **libre de dependencias Spring** y de otros módulos del proyecto.
3. Cualquier endpoint REST debe:
   - Estar en `keygo-api`.
   - Usar `BaseResponse<T>` como envelope.
   - Emitir `ResponseCode` apropiado.
4. No asumas paths sin `/keygo-server` — hay `context-path` activo.
5. **Nunca** agregues secretos, tokens ni `.env` a Git.
6. Antes de dar por finalizado un cambio, sugiere siempre:
   ```bash
   ./mvnw test
   ./mvnw clean package
   ```

## Cómo trabajar al implementar una feature

1. **Diseño mínimo primero:** consulta los documentos de referencia, luego describe clases, módulos afectados y flujo antes de generar código.
2. **Cambios pequeños:** genera un commit lógico por vez.
3. **Tests:** agrega tests unitarios (JUnit 5 + Mockito/AssertJ).
4. **Docs:** actualiza `README.md` o `ARCHITECTURE.md` **solo si el usuario lo solicita explícitamente**.

## Módulos y sus roles

| Módulo | Rol | Estado |
|---|---|---|
| `keygo-domain` | Dominio puro. Sin Spring. | 🚧 Stub vacío |
| `keygo-app` | Usecases + puertos (interfaces OUT). | ✅ Activo |
| `keygo-infra` | Implementaciones de puertos. | 🚧 Stub vacío |
| `keygo-api` | REST controllers + DTOs + error handlers. | ✅ Activo |
| `keygo-supabase` | JPA/Flyway + entidades + repos de Supabase. | ✅ Activo |
| `keygo-run` | Main + wiring + `application.yml`. | ✅ Activo |
| `keygo-bom` | Gestión de versiones de dependencias. | ✅ Activo |
| `keygo-common` | Utilidades compartidas. | 🚧 Stub vacío |

## Conocimiento específico útil

- Supabase/DB se habilita con perfil `supabase` en `SPRING_PROFILES_ACTIVE`.
- Scripts de DB local en `keygo-supabase/scripts/`.
- `KEYGO_ADMIN_KEY` protege `/api/**` vía header `X-KEYGO-ADMIN` — default `changeMe` solo para dev.
- El filtro `BootstrapAdminKeyFilter` puede tener problemas de matching con `context-path`: siempre validar.

## Ejemplo de prompt interno recomendado

```
Implementa la feature X siguiendo la arquitectura hexagonal del repo.
Asegúrate de que compile y tenga tests unitarios.
Si tocas endpoints, documenta considerando context-path=/keygo-server.
No introduzcas secretos ni dependencias innecesarias.
Al finalizar, indica los comandos exactos para verificar (build + tests).
```

