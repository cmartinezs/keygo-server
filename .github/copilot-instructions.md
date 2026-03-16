# Copilot Instructions — KeyGo Server

Responde en **español (es-MX/es)** por defecto, salvo que el usuario pida otro idioma.

## Flujo de trabajo obligatorio del agente

> Aplica a **toda** acción que implique generar o modificar código, configuración o estructura.

### 1 · Planificar primero, implementar después

Antes de escribir cualquier línea de código o hacer cualquier cambio, el agente **debe**:

1. Leer y considerar los documentos de referencia obligatorios (ver sección siguiente).
2. Presentar un **plan explícito** que incluya:
   - Módulos afectados y justificación arquitectónica.
   - Clases/archivos a crear o modificar.
   - Flujo de datos / secuencia de llamadas.
   - Tests a agregar.
3. Esperar confirmación implícita (continuar el chat) o explícita antes de implementar.

### 2 · Documentación: solo cuando se ordene explícitamente

- **Dentro de un mismo contexto de chat, NO generar documentación automáticamente.**
- Generar o actualizar archivos `.md` únicamente cuando el usuario lo indique con una orden explícita (p. ej. "documenta esto", "actualiza el README").
- Cuando se genere documentación, colocarla **siempre en la ruta que corresponde** (ver tabla de ubicaciones en `ARCHITECTURE.md` o `docs/`).

### 3 · Documentos de referencia obligatorios

Antes de cualquier acción, el agente debe consultar:

| Documento | Ruta | Para qué sirve |
|---|---|---|
| Contexto general AI | `AI_CONTEXT.md` | Estado del proyecto, bugs conocidos, convenciones |
| Arquitectura | `ARCHITECTURE.md` | Decisiones de diseño y estructura de módulos |
| Reglas de agentes | `CLAUDE.md` | Reglas de oro y flujo de trabajo |
| Instrucciones Copilot | `.github/copilot-instructions.md` | Este mismo archivo |

Adicionalmente, consultar los documentos específicos de los módulos involucrados en la tarea (p. ej. `docs/keygo-api/`, `docs/keygo-run/`).

### 4 · Aprendizaje continuo

- Si una acción produce un resultado **no satisfactorio** (error de compilación, test fallido, comportamiento inesperado), documentar el aprendizaje en `AI_CONTEXT.md` bajo la sección `## Lecciones aprendidas` antes de reintentar.
- Las **buenas prácticas nuevas**, actualizaciones de versiones de dependencias o cambios tecnológicos detectados durante una tarea deben registrarse también en `AI_CONTEXT.md` para que estén disponibles en futuras tareas.

### 5 · Git — prohibición de ejecución directa

- El agente **nunca debe ejecutar comandos `git`** (commit, push, merge, rebase, etc.) directamente.
- Si un flujo requiere operaciones de git, listar los comandos sugeridos para que el usuario los ejecute manualmente.

## Contexto del repositorio

- Monorepo Maven multi-módulo (Java 21, Spring Boot).
- Módulo ejecutable: `keygo-run`.
- Arquitectura: **Hexagonal / Ports & Adapters**.
- Base path en runtime: `context-path=/keygo-server` (todos los endpoints lo incluyen).
- DB opcional: `keygo-supabase` con Spring Data JPA + Flyway + PostgreSQL (perfil `supabase`).

## Reglas de implementación

- **NO** pongas dependencias de Spring en `keygo-domain`.
- Los endpoints REST van **solo** en `keygo-api` y devuelven `BaseResponse<T>`.
- Sigue el versionado `/api/v1/...` para endpoints nuevos.
- La lógica de negocio va en usecases dentro de `keygo-app`.
- Implementaciones concretas (repos, clients externos) van en `keygo-infra` o `keygo-supabase`.
- Si necesitas DB:
  - Perfil `supabase` debe estar activo (`SPRING_PROFILES_ACTIVE`).
  - Variables requeridas: `SUPABASE_URL`, `SUPABASE_USER`, `SUPABASE_PASSWORD`.
- Seguridad:
  - **Nunca** incluyas secretos, tokens ni passwords en el código o commits.
  - El filtro `BootstrapAdminKeyFilter` protege `/api/**` con header `X-KEYGO-ADMIN`.
  - Validar siempre el comportamiento con `context-path` activo antes de asumir que funciona.

## Convenciones de calidad

- Cambios pequeños y coherentes por commit.
- Siempre incluir en las respuestas:
  - Tests unitarios (JUnit 5 + AssertJ + Mockito).
  - Comandos de verificación (`./mvnw test`, `./mvnw clean package`).
- **No** actualizar documentación de forma automática; esperar orden explícita del usuario.
- Si falla un intento de implementación, registrar el aprendizaje en `AI_CONTEXT.md` (sección `## Lecciones aprendidas`) antes de continuar.

## Comandos de referencia

```bash
# Build
./mvnw clean package

# Tests
./mvnw test

# Correr app
./mvnw spring-boot:run -pl keygo-run

# Correr módulo específico de tests
./mvnw -pl keygo-api test
./mvnw -pl keygo-supabase test
```

## Alcance de estas instrucciones

Aplican a **Copilot Chat y agent mode**. No afectan las sugerencias inline mientras se escribe código.

