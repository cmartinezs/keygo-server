# AI Context — KeyGo Server

> Este archivo existe para que **Copilot/Claude/agentes** entiendan rápido el repo sin leer todo el código.

## TL;DR

- Proyecto: Java 21 + Spring Boot (monorepo Maven multi-módulo).
- Módulo ejecutable: `keygo-run`.
- API REST: `keygo-api`.
- Lógica de negocio: `keygo-app` (usecases) + `keygo-domain`.
- Persistencia (en progreso): `keygo-supabase` (Spring Data JPA + Flyway + PostgreSQL).
- Arquitectura: Hexagonal / Ports & Adapters.

## Comandos esenciales

```bash
# Build completo
./mvnw clean package

# Tests
./mvnw test

# Correr app (Maven)
./mvnw spring-boot:run -pl keygo-run

# Correr jar
java -jar keygo-run/target/keygo-run-1.0-SNAPSHOT.jar
```

## URLs base (local)

> El servicio usa `context-path=/keygo-server` por defecto.

- Base: `http://localhost:8080/keygo-server`
- Service info: `GET /keygo-server/api/v1/service/info`
- Response codes: `GET /keygo-server/api/v1/response-codes`
- Health: `GET /keygo-server/actuator/health`

## DB local (perfil supabase)

```bash
# Levantar PostgreSQL 15 + PgAdmin
cd keygo-supabase
./scripts/dev-start.sh
```

Variables de entorno mínimas:

```bash
export SPRING_PROFILES_ACTIVE="supabase,local"
export SUPABASE_URL="jdbc:postgresql://localhost:5432/keygo"
export SUPABASE_USER="postgres"
export SUPABASE_PASSWORD="postgres"
```

## Variables de entorno relevantes

| Variable | Descripción | Default |
|---|---|---|
| `PORT` | Puerto del servidor | `8080` |
| `SPRING_PROFILES_ACTIVE` | Perfiles activos | `default` |
| `KEYGO_ADMIN_KEY` | Bootstrap admin key | `changeMe` ⚠️ |
| `SUPABASE_URL` | JDBC URL de PostgreSQL | — |
| `SUPABASE_USER` | Usuario de DB | — |
| `SUPABASE_PASSWORD` | Contraseña de DB | — |

## Convenciones del proyecto

### Regla de dependencias (hexagonal)

```
keygo-domain   → sin dependencias internas ni Spring  [🚧 vacío actualmente]
keygo-app      → depende de domain; define puertos (interfaces)
keygo-infra    → implementa puertos; depende de app   [🚧 vacío actualmente]
keygo-api      → llama usecases; devuelve BaseResponse<T>
keygo-supabase → JPA/Flyway; implementaciones de repos Supabase; depende de infra
keygo-run      → cablea todo; tiene application.yml y main
keygo-common   → utilidades compartidas               [🚧 vacío actualmente]
```

> Los módulos `keygo-domain`, `keygo-infra` y `keygo-common` son **stubs vacíos** que reservan
> la estructura hexagonal. Al implementar nueva funcionalidad, respetar dónde debe ir cada pieza.

### Respuestas API

- **Siempre** usar `BaseResponse<T>` como envelope de respuesta.
- Usar `ResponseCode` para códigos de negocio (no mezclar con HTTP status).
- Endpoints versionados bajo `/api/v1/...`.

### Configuración

- `application.yml` en `keygo-run` usa filtering con `@project.*@` (Maven).
- `context-path` = `/${keygo.info.name}` → típicamente `/keygo-server`.
- Configuración de Supabase en `keygo-supabase/src/main/resources/application-supabase.yml`.

## Seguridad — puntos importantes

- `KEYGO_ADMIN_KEY` default `changeMe` **no es válido en producción**.
- `BootstrapAdminKeyFilter` pretende proteger `/api/**` con header `X-KEYGO-ADMIN`.
- **Bug conocido:** el filtro usa `request.getRequestURI()` (incluye el context-path `/keygo-server/`) pero los prefijos configurados son `/api/`, `/actuator/`, `/service/info` (sin el prefijo). Con `context-path` activo **ningún path coincide** → el filtro no aplica y todas las rutas son efectivamente públicas.
  - Fix correcto: usar `request.getServletPath()` en lugar de `getRequestURI()`.
- Actuator expuesto completo en config actual (`include: "*"`) — **restringir en prod**.

## Prompts sugeridos para Copilot/Claude

### Agregar endpoint REST (hexagonal)

> Agrega un endpoint `GET /api/v1/<recurso>/...` en `keygo-api` que devuelva `BaseResponse<T>`.
> Crea el usecase en `keygo-app` y define un puerto OUT si hace falta.
> Mantén `keygo-domain` libre de Spring.
> Incluye tests unitarios (JUnit 5 + Mockito/AssertJ).
> El base path real incluye `/keygo-server` por `context-path`.

### Agregar entidad JPA + repo en keygo-supabase

> Crea una entidad JPA en `keygo-supabase` (UUID como PK, timestamps).
> Agrega repository interface Spring Data con métodos mínimos.
> Si requiere migración, propone estrategia Flyway (sin hardcodear credenciales).

### Endurecer configuración para producción

> Propón cambios de configuración por perfiles para limitar Actuator en prod y endurecer seguridad.
> No cambies código directamente; entrega un plan y diffs sugeridos.

## "System message" sugerida para agentes externos

```
Eres un agente de ingeniería trabajando en un monorepo Maven multi-módulo (Java 21, Spring Boot).
Debes seguir arquitectura hexagonal (domain/app/infra/api/run).
Devuelve cambios mínimos y consistentes.
No introduzcas secretos en el código ni en los commits.
Siempre incluye pasos de verificación (build/tests).
Documenta endpoints considerando que el context-path es /keygo-server.
```

## Comportamiento esperado del agente

> Estas reglas aplican a cualquier agente (Copilot, Claude, etc.) que opere en este repositorio.

### Flujo obligatorio: Planificar → Implementar

1. **Leer** los documentos de referencia obligatorios antes de cualquier acción:
   - `AI_CONTEXT.md` (este archivo)
   - `ARCHITECTURE.md`
   - `AGENTS.md`
   - `CLAUDE.md`
   - `.github/copilot-instructions.md`
   - Documentos específicos del módulo involucrado (`docs/keygo-api/`, `docs/keygo-run/`, etc.)
2. **Presentar un plan explícito** (módulos, archivos, flujo, tests) antes de escribir código.
3. **Implementar** solo después de tener el plan.

### Documentación: solo bajo orden explícita

- En un mismo contexto de chat, **NO** generar ni actualizar archivos `.md` de forma automática.
- Solo crear/actualizar documentación cuando el usuario lo ordene de forma explícita.
- Toda documentación debe colocarse en **la ruta que le corresponde** (`docs/<módulo>/`, raíz, etc.).

### Aprendizaje continuo y retroalimentación obligatoria

Al concluir **cualquier tarea** (feature, corrección, refactor, configuración, etc.), el agente **debe** evaluar si ocurrió alguno de los eventos listados a continuación y, si es así, actualizar el documento correspondiente **antes de cerrar la tarea**:

| Evento | Documento a actualizar | Sección destino |
|---|---|---|
| Error de compilación encontrado y resuelto | `AI_CONTEXT.md` | `## Lecciones aprendidas` |
| Test fallido detectado y corregido | `AI_CONTEXT.md` | `## Lecciones aprendidas` |
| Comportamiento inesperado descubierto (bug, quirk del framework) | `AI_CONTEXT.md` | `## Lecciones aprendidas` |
| Mejor forma de implementar un patrón ya existente | `AI_CONTEXT.md` | `## Lecciones aprendidas` |
| Cambio de versión de dependencia o tecnología relevante | `AI_CONTEXT.md` | `## Lecciones aprendidas` |
| Nueva convención establecida o patrón acordado | `AI_CONTEXT.md` | `## Lecciones aprendidas` |
| Propuesta recurrente o de alto valor para el proyecto | `AI_CONTEXT.md` | `## Propuestas de mejoras futuras` |
| Cambio en módulos, rutas, comandos o URLs del quick-start | `AGENTS.md` | Sección correspondiente |

> ⚠️ Esta actualización **no está sujeta** a la regla "solo bajo orden explícita", ya que los documentos
> de base de conocimiento AI (`AI_CONTEXT.md`, `AGENTS.md`) son parte del ciclo de trabajo del agente,
> no documentación de producto.

**Formato obligatorio para entradas en `## Lecciones aprendidas`:**

```markdown
### [YYYY-MM-DD] Título descriptivo de la lección
**Contexto:** Breve descripción de la tarea o escenario que generó el aprendizaje.
**Problema:** Qué falló, qué comportamiento inesperado se detectó o qué patrón mejoró.
**Solución / Buena práctica:** Cómo se resolvió o qué debe hacerse en el futuro.
**Archivos clave:** (opcional) Rutas relevantes para contextualizar la solución.
```

### Git — prohibición de ejecución directa

- El agente **nunca debe ejecutar comandos `git`** directamente (commit, push, merge, rebase, etc.).
- Si el flujo requiere operaciones git, listar los comandos sugeridos para ejecución manual por el usuario.

### Propuesta de mejoras futuras

Al concluir cualquier tarea (feature, corrección, refactor, configuración, etc.), el agente **debe** incluir propuestas en tres horizontes:

| Horizonte | Criterio orientativo | Ejemplos |
|---|---|---|
| **Corto plazo** | Relacionado directamente con lo recién implementado; bajo esfuerzo | Validaciones, tests adicionales, TODOs |
| **Mediano plazo** | Evoluciones naturales de la funcionalidad; esfuerzo moderado | Endpoints relacionados, caché, paginación |
| **Largo plazo** | Capacidades estratégicas; alto esfuerzo o dependencias externas | OAuth2, multi-tenancy, observabilidad avanzada |

- Las propuestas deben ser **concretas y accionables**.
- Si son recurrentes o relevantes para el proyecto, registrarlas en `## Propuestas de mejoras futuras` (sección de este archivo).

## Lecciones aprendidas

> Sección de aprendizaje continuo. Registrar aquí cualquier falla, corrección, buena práctica nueva
> o actualización de tecnología detectada durante las tareas del agente.

<!-- Ejemplo de entrada:
### [YYYY-MM-DD] Título de la lección
**Contexto:** Breve descripción de la tarea que generó el aprendizaje.
**Problema:** Qué falló o qué se detectó.
**Solución / Buena práctica:** Cómo se resolvió o qué debe hacerse en el futuro.
-->

### [2026-03-17] Retroalimentación obligatoria de documentos AI tras cada tarea
**Contexto:** Revisión y consolidación de los documentos de guía para agentes (`AI_CONTEXT.md`, `CLAUDE.md`, `AGENTS.md`, `.github/copilot-instructions.md`).
**Problema:** Los documentos de referencia para agentes no incluían `AGENTS.md` en la lista de lectura obligatoria. Tampoco había instrucciones explícitas sobre cuándo y cómo actualizar estos mismos documentos al finalizar una tarea (retroalimentación).
**Solución / Buena práctica:** Se agregó `AGENTS.md` como documento obligatorio en los cuatro archivos de guía. Se estableció que los documentos AI (`AI_CONTEXT.md`, `AGENTS.md`) son "base de conocimiento del agente" y **deben** actualizarse al concluir cualquier tarea donde ocurra: error resuelto, mejor patrón encontrado, cambio tecnológico, nueva convención o propuesta relevante. Esta regla es **independiente** de la regla "documentación solo bajo orden explícita" (que aplica únicamente a docs de producto: README, ARCHITECTURE, docs/).
**Archivos clave:** `AI_CONTEXT.md`, `AGENTS.md`, `CLAUDE.md`, `.github/copilot-instructions.md`

### [2026-03-17] Reorganización de paquetes internos por feature en monorepo multi-módulo
**Contexto:** Reorganización completa de los paquetes internos de `keygo-api`, `keygo-app`, `keygo-run` y `keygo-supabase` de organización técnica genérica (constant, helper, dto, controller, exception, entity, repository) a organización por feature (shared, platform, error, user, membership).
**Problema:** Al ejecutar `./mvnw -pl keygo-run test` después de actualizar solo keygo-api y keygo-app, Maven usaba los JARs viejos del repositorio local, causando errores de compilación ("cannot find symbol", "package does not exist"). El orden correcto es: primero `install` los módulos dependidos, luego `test` el módulo consumidor.
**Solución / Buena práctica:** Cuando se reorganizan paquetes en módulos de los que dependen otros, ejecutar `./mvnw -pl <modulos-actualizados> install -DskipTests` antes de compilar/probar los módulos que los consumen. Solo el build completo (`./mvnw clean package`) garantiza el orden correcto de forma automática.
**Archivos clave:** `keygo-app/platform/port`, `keygo-app/platform/usecase`, `keygo-api/shared`, `keygo-api/platform`, `keygo-api/error`, `keygo-supabase/user`, `keygo-supabase/membership`, `keygo-supabase/config/SupabaseJpaConfig.java`

### [2026-03-17] SupabaseJpaConfig requiere basePackages ampliado al reorganizar entidades por feature
**Contexto:** Reorganización de entidades JPA y repositories de paquetes planos (`supabase.entity`, `supabase.repository`) a sub-paquetes por feature (`supabase.user.entity`, `supabase.membership.entity`, etc.).
**Problema:** Las anotaciones `@EntityScan(basePackages = "io.cmartinezs.keygo.supabase.entity")` y `@EnableJpaRepositories(basePackages = "io.cmartinezs.keygo.supabase.repository")` apuntan a rutas exactas que ya no existen tras el refactor, causando que Spring no encuentre entidades ni repositorios al arrancar con perfil `supabase`.
**Solución / Buena práctica:** Usar el paquete raíz del módulo como basePackage: `"io.cmartinezs.keygo.supabase"`. Spring Data escaneará recursivamente todos los sub-paquetes, independientemente de cuántos features se agreguen en el futuro. Este cambio es obligatorio y debe hacerse en el mismo commit que la reorganización de paquetes.
**Archivos clave:** `keygo-supabase/src/main/java/io/cmartinezs/keygo/supabase/config/SupabaseJpaConfig.java`

### [2026-03-17] Script de verificación de actividad del agente AI (extendido a AGENTS.md)
**Contexto:** Creación y extensión de `scripts/check-ai-docs.sh` para verificar actividad reciente en los documentos de base de conocimiento AI.
**Problema:** Inicialmente el script solo verificaba `AI_CONTEXT.md → ## Lecciones aprendidas`. `AGENTS.md` podía quedar desactualizado sin detectarse. Además, la lógica de escaneo estaba duplicada para cada archivo.
**Solución / Buena práctica:** Se refactorizó con una función reutilizable `check_section(FILE, SECTION_LABEL)` que usa arrays globales `_check_found` y `_check_recent` para evitar namerefs (requieren bash 4.3+). Una función `report_result(FILE, LABEL, SECTION)` orquesta la llamada y el reporte por documento. Se añadió `## Registro de cambios` a `AGENTS.md` como sección objetivo. El exit code final es el peor de los dos documentos (`worst = max(exit_ai, exit_agents)`). Los bloques `<!-- -->` se ignoran para evitar falsos positivos con templates de ejemplo. Compatible con GNU date (Linux) y BSD date (macOS).
**Archivos clave:** `scripts/check-ai-docs.sh`, `AI_CONTEXT.md`, `AGENTS.md`

## Propuestas de mejoras futuras

> Acumulador de propuestas concretas generadas al concluir tareas.
> Organizadas por horizonte temporal. Actualizar conforme se agreguen o descarten.

### Corto plazo

- Agregar `request/` bajo `keygo-api/platform/` cuando se necesiten DTOs de entrada en los endpoints existentes.
- Crear sub-paquetes `command/`, `query/` y `result/` en `keygo-app/platform/` al implementar el primer caso de uso que reciba y retorne DTOs propios.
- Renombrar la configuración de IntelliJ `.run/KeyGo Runner.run.xml` para reflejar el nuevo nombre `KeygoApplication` (ya actualizada la referencia interna).
- Agregar mapper `platform/` en `keygo-api` para mapear entre `ServiceInfoProvider` y `ServiceInfoData` sin que el controller haga la transformación directamente.

### Mediano plazo

- Introducir el paquete `keygo-api/security/filter/` y mover `BootstrapAdminKeyFilter` desde `keygo-run` una vez que se defina una interfaz de propiedades en `keygo-api` que `KeyGoBootstrapProperties` implemente (elimina la dependencia circular actual).
- Implementar features reales por subdominio (`user/`, `auth/`, `tenant/`) siguiendo el patrón `keygo-app/platform/` ya establecido.
- Agregar sub-paquete `persistence/` en `keygo-supabase` como capa intermedia entre `config/` y las features, según la estructura objetivo en `docs/arch/keygo_server_project_structure.md`.
- Crear `keygo-supabase/support/` para utilidades de persistencia transversales (converters, listeners, etc.).

### Largo plazo

- Evaluar renombrar `keygo-supabase` a `keygo-adapter-persistence-postgres` cuando el stack sea estable, para neutralizar el nombre respecto al proveedor.
- Implementar puertos de salida para infraestructura transversal en `keygo-infra`: `PasswordHasherPort`, `TokenSignerPort`, `ClockProvider`, `AuditPublisherPort`.
- Implementar ADRs (Architecture Decision Records) sugeridos en `docs/arch/keygo_server_project_structure.md` secciones 11 y 12.


