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
   - `CLAUDE.md`
   - `.github/copilot-instructions.md`
   - Documentos específicos del módulo involucrado (`docs/keygo-api/`, `docs/keygo-run/`, etc.)
2. **Presentar un plan explícito** (módulos, archivos, flujo, tests) antes de escribir código.
3. **Implementar** solo después de tener el plan.

### Documentación: solo bajo orden explícita

- En un mismo contexto de chat, **NO** generar ni actualizar archivos `.md` de forma automática.
- Solo crear/actualizar documentación cuando el usuario lo ordene de forma explícita.
- Toda documentación debe colocarse en **la ruta que le corresponde** (`docs/<módulo>/`, raíz, etc.).

### Aprendizaje continuo

- Si una acción produce un resultado no satisfactorio (error de compilación, test fallido, comportamiento inesperado): **registrar el aprendizaje** en la sección `## Lecciones aprendidas` de este archivo **antes** de reintentar.
- Buenas prácticas nuevas, actualizaciones de versiones o cambios tecnológicos detectados deben registrarse también en `## Lecciones aprendidas`.

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

## Propuestas de mejoras futuras

> Acumulador de propuestas concretas generadas al concluir tareas.
> Organizadas por horizonte temporal. Actualizar conforme se agreguen o descarten.

### Corto plazo

<!-- Mejoras de bajo esfuerzo relacionadas con funcionalidades recientes -->

### Mediano plazo

<!-- Evoluciones naturales del sistema; esfuerzo moderado -->

### Largo plazo

<!-- Capacidades estratégicas; alto esfuerzo o dependencias externas -->


