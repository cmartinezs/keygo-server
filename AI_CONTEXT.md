# AI Context — KeyGo Server

> Este archivo existe para que **Copilot/Claude/agentes** entiendan rápido el repo sin leer todo el código.
>
> 📖 **Sub-documentos de este archivo (detalle en `docs/ai/`):**
> - [`docs/ai/lecciones.md`](docs/ai/lecciones.md) — Lecciones aprendidas y buenas prácticas
> - [`docs/ai/propuestas.md`](docs/ai/propuestas.md) — Propuestas de mejoras futuras
> - [`docs/ai/inconsistencias.md`](docs/ai/inconsistencias.md) — Inconsistencias detectadas (centralizador)

---

## TL;DR

- Proyecto: Java 21 + Spring Boot 4.x (monorepo Maven multi-módulo).
- Módulo ejecutable: `keygo-run`.
- API REST: `keygo-api`.
- Lógica de negocio: `keygo-app` (usecases) + `keygo-domain`.
- Persistencia: `keygo-supabase` (Spring Data JPA + Flyway + PostgreSQL, perfil `supabase`).
- Arquitectura: Hexagonal / Ports & Adapters.

---

## Comandos esenciales

```bash
./mvnw clean package                   # Build completo
./mvnw test                            # Tests (sin cobertura)
./mvnw verify                          # Tests + JaCoCo coverage check
./mvnw spring-boot:run -pl keygo-run   # Correr app localmente
./mvnw -pl keygo-api test              # Tests de un módulo específico
```

---

## URLs base (local)

> El servicio usa `context-path=/keygo-server`. Todos los endpoints lo incluyen.

| URL | Descripción |
|---|---|
| `http://localhost:8080/keygo-server/api/v1/service/info` | Info del servicio |
| `http://localhost:8080/keygo-server/actuator/health` | Health check |
| `http://localhost:8080/keygo-server/swagger-ui/index.html` | Swagger UI (público) |
| `http://localhost:8080/keygo-server/v3/api-docs` | OpenAPI spec (público) |

Ver lista completa de endpoints en [`AGENTS.md`](AGENTS.md) § "context-path is always active".

---

## DB local (perfil supabase)

```bash
cd keygo-supabase && ./scripts/dev-start.sh   # Levanta PostgreSQL 15 + PgAdmin

export SPRING_PROFILES_ACTIVE="supabase,local"
export SUPABASE_URL="jdbc:postgresql://localhost:5432/keygo"
export SUPABASE_USER="postgres"
export SUPABASE_PASSWORD="postgres"
```

---

## Variables de entorno relevantes

| Variable | Descripción | Default |
|---|---|---|
| `PORT` | Puerto del servidor | `8080` |
| `SPRING_PROFILES_ACTIVE` | Perfiles activos | `default` |
| `KEYGO_ADMIN_KEY` | Bootstrap admin key | `changeMe` ⚠️ |
| `SUPABASE_URL` | JDBC URL de PostgreSQL | — |
| `SUPABASE_USER` | Usuario de DB | — |
| `SUPABASE_PASSWORD` | Contraseña de DB | — |

---

## Convenciones del proyecto

### Regla de dependencias (hexagonal)

```
keygo-domain   → sin dependencias internas ni Spring
keygo-app      → depende de domain; define puertos (interfaces)
keygo-infra    → implementa puertos; depende de app
keygo-api      → llama usecases; devuelve BaseResponse<T>
keygo-supabase → JPA/Flyway; implementaciones de repos; depende de infra
keygo-run      → cablea todo; tiene application.yml y main
keygo-common   → utilidades compartidas  [🚧 stub vacío]
```

### Respuestas API

- **Siempre** usar `BaseResponse<T>` como envelope (excepción: endpoints OIDC/JWKS — JSON nativo RFC 7517).
- Usar `ResponseCode` para códigos de negocio (enum en `keygo-api`).
- Endpoints versionados bajo `/api/v1/...`.

### Jackson 3 (Spring Boot 4.x) — namespace cambiado

```java
// ✅ Correcto (Jackson 3)
import tools.jackson.databind.json.JsonMapper;
// ❌ Incorrecto (Jackson 2 — no compila)
import com.fasterxml.jackson.databind.ObjectMapper;
// ✅ Anotaciones siguen igual
import com.fasterxml.jackson.annotation.JsonInclude;
```

---

## Seguridad

- `KEYGO_ADMIN_KEY` default `changeMe` — **no válido en producción**.
- `BootstrapAdminKeyFilter` protege `/api/**` con header `X-KEYGO-ADMIN`.
- Usa `request.getServletPath()` (no `getRequestURI()`) para comparar prefijos. Ver lección [Bug T-001](AI_CONTEXT.lecciones.md#2026-03-21-bug-t-001--bootstrapadminkeyfilter-getrequesturi-vs-getservletpath-con-context-path).
- Actuator expuesto completo — **restringir en prod**.

---

## Comportamiento obligatorio del agente

### Flujo: Planificar → Implementar

1. **Leer** antes de cualquier acción:
   - Este archivo (`AI_CONTEXT.md`) + sub-documentos en [`docs/ai/`](docs/ai/)
   - [`ARCHITECTURE.md`](ARCHITECTURE.md) — decisiones de diseño
   - [`AGENTS.md`](AGENTS.md) — quick-start, módulos, patrones
   - [`CLAUDE.md`](CLAUDE.md) / [`.github/copilot-instructions.md`](.github/copilot-instructions.md)
   - [`ROADMAP.md`](ROADMAP.md) — propuestas activas y completadas
   - [`docs/ai/inconsistencias.md`](docs/ai/inconsistencias.md) — inconsistencias conocidas
   - Docs del módulo involucrado (`docs/keygo-api/`, `docs/keygo-run/`, etc.)
2. **Presentar plan explícito** (módulos, archivos, flujo, tests) antes de escribir código.
3. **Implementar** solo después de tener el plan aprobado.

### Documentación: solo bajo orden explícita

- En un mismo contexto de chat, **NO** generar ni actualizar archivos `.md` automáticamente.
- Excepción: documentos de base de conocimiento AI en `docs/ai/` (`lecciones.md`, `propuestas.md`, `agents-registro.md`, `inconsistencias.md`) y `AGENTS.md`.

#### Diagramas: orden de preferencia

| Prioridad | Herramienta | Cuándo |
|---|---|---|
| 1 | **Mermaid** | Siempre — soportado en GitHub, GitLab, Notion |
| 2 | **PlantUML** | Si el tipo no es expresable en Mermaid |
| 3 | **ASCII art** | Último recurso |

### Retroalimentación obligatoria al concluir tarea

Al terminar **cualquier tarea**, evaluar los eventos de la siguiente tabla y actualizar los docs correspondientes **antes de cerrar**:

| Evento | Documento | Sección |
|---|---|---|
| Error de compilación / test fallido / bug resuelto | [`docs/ai/lecciones.md`](docs/ai/lecciones.md) | Agregar entrada |
| Mejor patrón o convención nueva | [`docs/ai/lecciones.md`](docs/ai/lecciones.md) | Agregar entrada |
| Inconsistencia detectada entre docs y código/DB | [`docs/ai/inconsistencias-<cat>.md`](docs/ai/inconsistencias.md) | Agregar entrada + registrar en `docs/ai/inconsistencias.md` |
| Propuesta técnica o funcional nueva | [`docs/ai/propuestas.md`](docs/ai/propuestas.md) + [`ROADMAP.md`](ROADMAP.md) | Agregar con ID T-NNN o F-NNN |
| Propuesta completada | [`docs/ai/propuestas.md`](docs/ai/propuestas.md) + [`ROADMAP.md`](ROADMAP.md) | Marcar ✅ + mover a historial |
| Cambio en módulos, rutas o patrones quick-start | [`AGENTS.md`](AGENTS.md) + [`docs/ai/agents-registro.md`](docs/ai/agents-registro.md) | Actualizar sección + entrada registro |
| Nuevo endpoint REST | `postman/KeyGo-Server.postman_collection.json` | Agregar request con `pm.test()` |
| Nueva migración Flyway (`V{n}__*.sql`) | `docs/data/DATA_MODEL.md` | Diccionario de nuevas tablas |
| Nueva migración Flyway (`V{n}__*.sql`) | `docs/data/ENTITY_RELATIONSHIPS.md` | Diagramas de contexto afectados |
| Nueva migración Flyway (`V{n}__*.sql`) | `docs/data/MIGRATIONS.md` | Sección "Próximas migraciones" |

> ⚠️ Esta retroalimentación **no está sujeta** a la regla "solo bajo orden explícita".

**Formato de entrada en lecciones:**
```markdown
### [YYYY-MM-DD] Título descriptivo
**Contexto:** Tarea que generó el aprendizaje.
**Problema:** Qué falló o qué mejoró.
**Solución / Buena práctica:** Cómo se resolvió.
**Archivos clave:** (opcional)
```

### Git — prohibición de ejecución directa

- **Nunca** ejecutar comandos `git` directamente (commit, push, merge, rebase…).
- Listar los comandos sugeridos para ejecución manual.

### Propuestas de mejoras futuras

Al concluir, incluir propuestas en tres horizontes:

| Horizonte | Criterio | Registrar en |
|---|---|---|
| **Corto plazo** | Relacionado con lo recién implementado; bajo esfuerzo | [`AI_CONTEXT.propuestas.md`](AI_CONTEXT.propuestas.md) |
| **Mediano plazo** | Evoluciones naturales; esfuerzo moderado | [`AI_CONTEXT.propuestas.md`](AI_CONTEXT.propuestas.md) |
| **Largo plazo** | Capacidades estratégicas; alto esfuerzo | [`ROADMAP.md`](ROADMAP.md) |

---

## Referencias rápidas

| Necesito... | Ir a... |
|---|---|
| Lecciones aprendidas / no repetir errores | [`docs/ai/lecciones.md`](docs/ai/lecciones.md) |
| Propuestas activas y su estado | [`docs/ai/propuestas.md`](docs/ai/propuestas.md) |
| Inconsistencias conocidas | [`docs/ai/inconsistencias.md`](docs/ai/inconsistencias.md) |
| Quick-start: módulos, comandos, endpoints | [`AGENTS.md`](AGENTS.md) |
| Historial de cambios al quick-start | [`docs/ai/agents-registro.md`](docs/ai/agents-registro.md) |
| Roadmap completo con IDs T-NNN / F-NNN | [`ROADMAP.md`](ROADMAP.md) |
| Modelo de datos / diccionario DB | `docs/data/DATA_MODEL.md` |
| Flujo OAuth2 / autenticación | `docs/api/AUTH_FLOW.md` |

---

**Última actualización:** 2026-03-23 | **Responsable:** AI Agent
