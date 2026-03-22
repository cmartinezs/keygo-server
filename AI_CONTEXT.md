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
- **Swagger UI:** `GET /keygo-server/swagger-ui/index.html` (público)
- **OpenAPI spec:** `GET /keygo-server/v3/api-docs` (público)

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
   - `ROADMAP.md` — para entender qué está planificado y evitar duplicar trabajo
   - Documentos específicos del módulo involucrado (`docs/keygo-api/`, `docs/keygo-run/`, etc.)
2. **Presentar un plan explícito** (módulos, archivos, flujo, tests) antes de escribir código.
3. **Implementar** solo después de tener el plan.

### Documentación: solo bajo orden explícita

- En un mismo contexto de chat, **NO** generar ni actualizar archivos `.md` de forma automática.
- Solo crear/actualizar documentación cuando el usuario lo ordene de forma explícita.
- Toda documentación debe colocarse en **la ruta que le corresponde** (`docs/<módulo>/`, raíz, etc.).

#### Diagramas en documentación

Cuando sea necesario incluir un diagrama, usar el siguiente orden de preferencia:

| Prioridad | Herramienta | Cuándo usarla |
|---|---|---|
| 1 | **Mermaid** | Primera opción siempre — soportado nativamente en GitHub, GitLab, Notion y la mayoría de editores Markdown |
| 2 | **PlantUML** | Si el tipo de diagrama no es expresable con Mermaid (p. ej. diagramas de componentes complejos, C4, timing) |
| 3 | **ASCII art** | Último recurso — solo si ni Mermaid ni PlantUML son viables en el contexto |

### Aprendizaje continuo y retroalimentación obligatoria

Al concluir **cualquier tarea** (feature, corrección, refactor, configuración, etc.), el agente **debe** evaluar si ocurrió alguno de los eventos listados a continuación y, si es así, actualizar el documento correspondiente **antes de cerrar la tarea**:

 Evento  Documento a actualizar  Sección destino 
---------
 Error de compilación encontrado y resuelto  `AI_CONTEXT.md`  `## Lecciones aprendidas` 
 Test fallido detectado y corregido  `AI_CONTEXT.md`  `## Lecciones aprendidas` 
 Comportamiento inesperado descubierto (bug, quirk del framework)  `AI_CONTEXT.md`  `## Lecciones aprendidas` 
 Mejor forma de implementar un patrón ya existente  `AI_CONTEXT.md`  `## Lecciones aprendidas` 
 Cambio de versión de dependencia o tecnología relevante  `AI_CONTEXT.md`  `## Lecciones aprendidas` 
 Nueva convención establecida o patrón acordado  `AI_CONTEXT.md`  `## Lecciones aprendidas` 
 Propuesta recurrente o de alto valor para el proyecto  `AI_CONTEXT.md` + `ROADMAP.md`  `## Propuestas de mejoras futuras` + tabla técnica o funcional correspondiente 
 Propuesta técnica concreta generada al concluir tarea  `ROADMAP.md`  Tabla **Propuestas técnicas** (horizonte correspondiente) 
 Propuesta funcional nueva o aclaración de épica existente  `ROADMAP.md`  Tabla **Propuestas funcionales** 
| Propuesta completada / implementada | `ROADMAP.md` | Tabla **Historial de propuestas completadas** |
| Cambio en módulos, rutas, comandos o URLs del quick-start | `AGENTS.md` | Sección correspondiente |
| Nuevo endpoint REST creado o modificado | `postman/KeyGo-Server.postman_collection.json` | Agregar o actualizar request con método, URL, headers, body y `pm.test()` |

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

### [2026-03-21] SpringDoc 3.0.1 con Spring Boot 4.x — integración y anotaciones de seguridad
**Contexto:** Integración de Swagger / OpenAPI al proyecto usando `springdoc-openapi-starter-webmvc-ui`.
**Problema:** Dos puntos críticos encontrados: (1) `@SecurityRequirementsOptional` **no existe** en `swagger-annotations-jakarta` — el compilador falla con "cannot find symbol". (2) La versión correcta para Spring Boot 4.x es **SpringDoc 3.x** (`springdoc-openapi-starter-webmvc-ui:3.0.1`); SpringDoc 2.x usa Spring Boot 3.x parent y puede no ser compatible con Spring 7/Jackson 3.
**Solución / Buena práctica:**
- Usar `springdoc-openapi-starter-webmvc-ui:3.0.1` (parent `spring-boot-starter-parent:4.0.1` confirmado en el POM).
- Para marcar endpoints como **públicos** (sin autenticación en la UI): **no** usar ninguna anotación de seguridad en esos controllers/métodos. Si hay un `SecurityRequirement` global en `OpenAPI`, quitarlo y poner `@SecurityRequirement(name="...")` solo en los controllers que realmente lo necesitan.
- La anotación correcta para controladores protegidos es `@SecurityRequirement(name = "AdminKeyAuth")` a nivel de clase o método.
- La anotación `@SecurityRequirements({})` con array vacío también funciona para sobrescribir el global en un endpoint específico, pero es más limpio no tener global.
- Agregar los prefijos `/swagger-ui` y `/v3/api-docs` en `KeyGoBootstrapProperties` + `BootstrapAdminKeyFilter.isPublicPath()` para preparar el futuro fix del bug T-001.
**Archivos clave:** `keygo-run/config/OpenApiConfig.java`, `keygo-api/pom.xml`, `pom.xml` (raíz — propiedad `springdoc.version`), `BootstrapAdminKeyFilter.java`, `KeyGoBootstrapProperties.java`, `application.yml`

### [2026-03-21] Configuración de JaCoCo en monorepo Maven multi-módulo con Spring Boot 4**Contexto:** Implementación de la propuesta T-016 — configurar JaCoCo para cobertura de tests con umbral mínimo y reporte consolidado.
**Problema:** Tres puntos de atención en monorepos multi-módulo con JaCoCo: (1) los módulos sin código (`keygo-infra`, `keygo-common`) fallan silenciosamente el check si no se excluyen; (2) `keygo-bom` (empaquetado `pom`) también ejecuta JaCoCo aunque no tenga código — JaCoCo lo maneja con "Skipping due to missing execution data file", sin bloquear el build; (3) el goal `report-aggregate` requiere que el módulo ejecutor tenga como dependencias (directas o transitivas) todos los módulos que se quieren reportar — `keygo-run` es el candidato natural porque ya depende de todos.
**Solución / Buena práctica:**
- Configurar `prepare-agent` + `report` + `check` en `pluginManagement` del POM raíz y referenciarlos en `<build><plugins>` para que hereden todos los módulos.
- Marcar módulos stub vacíos con `<jacoco.skip>true</jacoco.skip>` en sus `<properties>` para desactivar el agente, reporte y check.
- Agregar el goal `report-aggregate` **solo** en `keygo-run` (módulo que depende de todos los demás), en una ejecución separada con su propio `id`.
- Usar `<jacoco.minimum.coverage>` como propiedad parametrizable para poder sobrescribir en CI (`-Djacoco.minimum.coverage=0`) sin cambiar el POM.
- En CI (`ci.yml`): cambiar `./mvnw test` → `./mvnw verify` para que las fases `jacoco-report` y `jacoco-check` se ejecuten. El step de `package` puede seguir usando `-DskipTests`.
- Versión recomendada: `0.8.12` (última estable con soporte pleno de Java 21 bytecode).
**Archivos clave:** `pom.xml` (raíz), `keygo-run/pom.xml`, `keygo-infra/pom.xml`, `keygo-common/pom.xml`, `.github/workflows/ci.yml`

### [2026-03-21] Convenciones de coding Java adoptadas para el codebase
**Contexto:** Revisión y corrección masiva de estilo de código en todos los módulos activos.
**Problema:** Se detectaron cuatro patrones de código que generan alertas de IDE (IntelliJ) y/o problemas de performance: (1) líneas en blanco dentro de bloques JavaDoc; (2) comentarios `/** */` en atributos/campos en lugar de `/* */`; (3) entidades JPA con `@Data` de Lombok (genera `equals`/`hashCode`/`toString` sobre todas las claves incluyendo colecciones lazy); (4) literales de string duplicados en tests de la misma clase; (5) `@Override toString()` sin `@NotNull` en IntelliJ (warning de nullability).
**Solución / Buena práctica:**
- **JavaDoc sin líneas en blanco:** usar `<p>` para separar párrafos dentro de la descripción; eliminar la línea en blanco antes de tags (`@param`, `@return`, `@throws`, `@author`).
- **Comentarios en campos:** usar `/* */` (comentario simple multilinea), **no** `/** */`. Aplica a: campos de clase, componentes de records, constantes de enums, campos de `@ConfigurationProperties`.
- **Entidades JPA:** reemplazar `@Data` por `@Getter @Setter`. Mantener `@Builder @NoArgsConstructor @AllArgsConstructor` por separado. Esto evita `equals`/`hashCode`/`toString` sobre colecciones lazy que pueden causar `LazyInitializationException` y degradación de performance.
- **Literales duplicados en tests:** extraer a constantes `private static final` en la clase de test.
- **`toString()` override:** anotar con `@SuppressWarnings("NullableProblems")` para suprimir el warning de IntelliJ sin agregar dependencias externas.
- **Lombok en domain:** agregar Lombok como dependencia `provided` a `keygo-domain` y usar `@Builder` en el constructor privado de la clase (en vez de builder manual) + `@Getter` a nivel de clase.
**Archivos clave:** `keygo-domain/pom.xml`, `Tenant.java`, `TenantId.java`, `TenantSlug.java`, `TenantStatus.java`, `TenantEntity.java`, `UserEntity.java`, `RoleEntity.java`, `PermissionEntity.java`, `CreateTenantUseCaseTest.java`, `TenantTest.java`

### [2026-03-21] jakarta.validation-api no es transitivo en keygo-api
**Contexto:** Implementación de la Fase 1 (multitenancy) — creación de `CreateTenantRequest` record con anotaciones `@NotBlank`, `@Size`, `@Email`, `@Pattern`, y uso de `@Valid` en el controller.
**Problema:** El build falló con "package jakarta.validation.constraints does not exist" porque `keygo-api` solo tiene `spring-boot-starter-web` y `spring-boot-starter-actuator`, ninguno de los cuales expone `jakarta.validation-api` como dependencia transitiva directa en Spring Boot 4.
**Solución / Buena práctica:** Agregar `jakarta.validation-api` explícitamente en `keygo-api/pom.xml` (sin scope especial). Esto da acceso a las anotaciones de constraints sin activar el motor de validación; el motor (Hibernate Validator) ya está activo en `keygo-run` mediante `spring-boot-starter-validation`. Este patrón aplica a cualquier módulo que declare DTOs con anotaciones de validación: la API de anotaciones y el runtime del motor pueden estar en módulos distintos.
**Archivos clave:** `keygo-api/pom.xml`, `keygo-api/tenant/request/CreateTenantRequest.java`

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

### [2026-03-21] Fase 0 cerrada: qué faltaba vs. qué se asumía como completo
**Contexto:** Verificación del estado real de la Fase 0 del plan de implementación (`docs/arch/keygo_server_implementation_plan.md`). El documento `AGENTS.md` la marcaba como `✅ Done` tras la reorganización de paquetes (2026-03-17), pero el punto 0.4 (base de calidad) no estaba completo.
**Problema:** La reorganización de paquetes (0.2) se completó y se marcó la fase como hecha, pero faltaban: (a) pipeline CI — no había ningún archivo en `.github/workflows/`; (b) enforcement automático de format/lint; (c) las convenciones de código no estaban documentadas formalmente.
**Solución / Buena práctica:** Al marcar una fase como completa, verificar **cada sub-punto** de la lista, no solo el trabajo más visible. Para el CI: crear `.github/workflows/ci.yml`. Para calidad de código: usar Maven Enforcer Plugin (fácil de pasar) para reglas de proyecto, y documentar el estilo en `docs/keygo-server/CODE_STYLE.md`. El Checkstyle/Spotless se deja como T-023 en el ROADMAP para no bloquear el cierre de la fase. Esta separación —"convención documentada" vs. "enforcement automático"— es pragmática y accionable.
**Archivos clave:** `.github/workflows/ci.yml`, `pom.xml` (raíz — Maven Enforcer Plugin), `docs/keygo-server/CODE_STYLE.md`, `ROADMAP.md` (T-023, T-006 completada)

### [2026-03-17] Script de verificación de actividad del agente AI (extendido a AGENTS.md)
**Contexto:** Creación y extensión de `scripts/check-ai-docs.sh` para verificar actividad reciente en los documentos de base de conocimiento AI.
**Problema:** Inicialmente el script solo verificaba `AI_CONTEXT.md → ## Lecciones aprendidas`. `AGENTS.md` podía quedar desactualizado sin detectarse. Además, la lógica de escaneo estaba duplicada para cada archivo.
**Solución / Buena práctica:** Se refactorizó con una función reutilizable `check_section(FILE, SECTION_LABEL)` que usa arrays globales `_check_found` y `_check_recent` para evitar namerefs (requieren bash 4.3+). Una función `report_result(FILE, LABEL, SECTION)` orquesta la llamada y el reporte por documento. Se añadió `## Registro de cambios` a `AGENTS.md` como sección objetivo. El exit code final es el peor de los dos documentos (`worst = max(exit_ai, exit_agents)`). Los bloques `<!-- -->` se ignoran para evitar falsos positivos con templates de ejemplo. Compatible con GNU date (Linux) y BSD date (macOS).
**Archivos clave:** `scripts/check-ai-docs.sh`, `AI_CONTEXT.md`, `AGENTS.md`

### [2026-03-21] Generación de colecciones Postman para pruebas funcionales manuales
**Contexto:** El proyecto no contaba con ninguna colección Postman ni entorno importable, dificultando las pruebas manuales de los endpoints existentes.
**Problema:** Sin colecciones estándar, cada prueba manual requería configurar headers, URLs y bodies manualmente en Postman, con riesgo de error y sin scripts de validación automática de respuestas.
**Solución / Buena práctica:** Crear archivos bajo `postman/` siguiendo el schema Postman Collection v2.1.0. Puntos clave: (1) autenticación `apikey` a nivel de colección con `X-KEYGO-ADMIN` → heredada automáticamente por todos los requests protegidos; (2) pre-request script global que compone `{{fullBaseUrl}}` desde variables de entorno; (3) pre-request en `POST Create Tenant` que genera slug único con timestamp para evitar conflictos entre ejecuciones; (4) script post-request guarda `tenantSlug` en el entorno para reutilizarlo en requests subsiguientes; (5) los endpoints públicos (Service Info, Actuator) overridean auth a `noauth`. Incluir siempre respuestas de ejemplo en cada request facilita la comprensión sin ejecutar.
**Archivos clave:** `postman/KeyGo-Server.postman_collection.json`, `postman/KeyGo-Server-Local.postman_environment.json`

### [2026-03-21] Fase 2 — ClientApp: import faltante de enum en use case
**Contexto:** Implementación de la Fase 2 (modelo de aplicaciones cliente) — creación de `CreateClientAppUseCase` que referencia `ClientType.CONFIDENTIAL`.
**Problema:** El compilador reportó "package ClientType does not exist" porque `ClientType` no estaba importado explícitamente, aunque otros tipos del mismo paquete (`ClientApp`, `ClientAppId`, etc.) sí lo estaban. Ocurre cuando se agrega un tipo de dominio en un if-check dentro del mismo método sin recordar el import.
**Solución / Buena práctica:** Al agregar comparaciones de enum (`ClientType.CONFIDENTIAL.equals(...)`) en un use case, siempre verificar que el enum esté en la lista de imports. El compilador de javac no infiere imports implícitos. La detección fue rápida al ejecutar `./mvnw test --also-make -pl keygo-app` en lugar del build completo.
**Archivos clave:** `keygo-app/.../clientapp/usecase/CreateClientAppUseCase.java`

### [2026-03-21] Fase 2 — Patrón de orphanRemoval en colecciones JPA de entidades hijo
**Contexto:** Diseño de `ClientAppEntity` con colecciones `@OneToMany(cascade=ALL, orphanRemoval=true)` para `ClientRedirectUriEntity`, `ClientAllowedGrantEntity`, `ClientAllowedScopeEntity`.
**Problema:** Si al actualizar la entidad padre se reasigna directamente la referencia de la lista (`entity.setRedirectUris(newList)`), `orphanRemoval` no elimina los hijos anteriores porque Hibernate pierde el tracking de la colección original. Esto genera registros huérfanos en DB.
**Solución / Buena práctica:** Para que `orphanRemoval` funcione correctamente al actualizar colecciones: usar `entity.getRedirectUris().clear()` seguido de `entity.getRedirectUris().addAll(nuevaLista)`. Nunca reasignar la referencia de la colección. Este patrón está implementado en `ClientAppPersistenceMapper.toEntity()`. Aplicar también en futuros adapters con relaciones `@OneToMany` + `orphanRemoval`.
**Archivos clave:** `keygo-supabase/.../clientapp/mapper/ClientAppPersistenceMapper.java`, `keygo-supabase/.../clientapp/entity/ClientAppEntity.java`

### [2026-03-21] Guía de pruebas Postman para perfil Testing — convención de documentación
**Contexto:** Creación del documento `docs/keygo-server/TESTING_GUIDE.md` dirigido a un perfil de QA/Testing que usa Postman o herramienta compatible para probar los endpoints REST de KeyGo Server.
**Problema:** Sin un documento de referencia, el perfil de Testing debía inferir el orden de ejecución, las dependencias entre endpoints y las configuraciones necesarias leyendo el código o la colección Postman directamente — con alto riesgo de errores (ej: listar antes de crear, usar un tenant suspendido para crear apps).
**Solución / Buena práctica:** Documentar tres dimensiones: (1) **configuración** — qué datos necesita con/sin acceso al código (variables de entorno, URL, admin key); (2) **dependencias** — qué recursos deben existir antes de ejecutar cada endpoint; (3) **orden recomendado** — fases de ejecución de menor a mayor dependencia (Smoke → Plataforma → Tenants → Client Apps → Errores). Incluir diagrama Mermaid de dependencias entre endpoints y tabla de solución de problemas frecuentes.
**Archivos clave:** `docs/keygo-server/TESTING_GUIDE.md`, `postman/KeyGo-Server.postman_collection.json`, `postman/KeyGo-Server-Local.postman_environment.json`

### [2026-03-21] Generación automática de slug a partir del nombre del tenant
**Contexto:** El campo `slug` del tenant era requerido en el request body (`CreateTenantRequest`). Se migró a generación automática en el servidor a partir del campo `name`.
**Problema:** Exponer el slug como campo editable en el API obliga al cliente a construir slugs válidos, introduciendo validaciones redundantes en la capa de presentación y riesgo de slugs inconsistentes con el nombre real del tenant.
**Solución / Buena práctica:** Centralizar la generación de slug en el dominio: (1) crear `SlugUtils.toSlug(String)` en `keygo-domain/shared/util/` (Java puro, sin Spring); (2) agregar factory `TenantSlug.fromName(String name)` que delega en `SlugUtils` y luego valida el resultado con el compact constructor del record; (3) quitar `slug` de `CreateTenantCommand` y `CreateTenantRequest`; (4) `CreateTenantUseCase.execute()` llama `TenantSlug.fromName(command.name())`. Para evitar colisiones en Postman, el pre-request script ahora agrega un timestamp al nombre en lugar de al slug.
**Archivos clave:** `keygo-domain/.../shared/util/SlugUtils.java`, `keygo-domain/.../tenant/model/TenantSlug.java`, `keygo-app/.../tenant/command/CreateTenantCommand.java`, `keygo-app/.../tenant/usecase/CreateTenantUseCase.java`, `keygo-api/.../tenant/request/CreateTenantRequest.java`

### [2026-03-21] Bug T-001 — BootstrapAdminKeyFilter: getRequestURI() vs. getServletPath() con context-path
**Contexto:** Corrección del bug conocido en `BootstrapAdminKeyFilter` donde el filtro de seguridad nunca bloqueaba las rutas de API, dejando todos los endpoints públicos aunque `keygo.bootstrap.enabled=true`.
**Problema:** El filtro usaba `request.getRequestURI()` para comparar con los prefijos configurados en `application.yml` (`/api/`, `/actuator/`, etc.). Sin embargo, con `server.servlet.context-path=/keygo-server` activo, `getRequestURI()` retorna `/keygo-server/api/v1/...` (incluye el context-path), mientras que los prefijos configurados no lo incluyen. Por tanto, `requestPath.startsWith("/api/")` nunca era `true` y el filtro pasaba todas las rutas a `filterChain.doFilter()` sin autenticar.
**Solución / Buena práctica:** Usar `request.getServletPath()` en lugar de `request.getRequestURI()`. `getServletPath()` retorna la ruta relativa al context-path (ej: `/api/v1/tenants`), que sí coincide con los prefijos de `application.yml`. La misma corrección aplica en `validateAdminKey()` para el log de advertencia. En tests: usar `request.setServletPath()` (no `setRequestURI()`) para que el mock refleje el mismo comportamiento. Se añadieron dos tests de regresión con `setContextPath("/keygo-server")` + `setRequestURI(...)` + `setServletPath(...)` para documentar el escenario exacto del bug.
**Archivos clave:** `keygo-run/src/main/java/.../filter/BootstrapAdminKeyFilter.java`, `keygo-run/src/test/java/.../filter/BootstrapAdminKeyFilterTest.java`

### [2026-03-21] Fase 2 — TenantEntity como referencia no-managed en ClientAppRepositoryAdapter
**Contexto:** Al guardar un `ClientApp`, el adapter necesita asociar el `TenantEntity` correcto en la FK `tenant_id` de `ClientAppEntity` sin cargar el tenant desde la DB.
**Problema:** JPA requiere una referencia a una entidad para la asociación `@ManyToOne`. No es posible pasar un UUID directamente como FK en la entidad. Pero cargar el `TenantEntity` completo solo para asociarlo sería ineficiente.
**Solución / Buena práctica:** Crear una instancia de `TenantEntity` con solo el `id` seteado (`new TenantEntity(); tenantRef.setId(id)`) y asignarla a `entity.setTenant(tenantRef)`. JPA/Hibernate trata esto como una referencia a un proxy de la entidad existente, resolviendo la FK sin hacer una query adicional. Este patrón es seguro cuando la FK ya existe en DB. Alternativa más idiomática en JPA: usar `entityManager.getReference(TenantEntity.class, id)`.
**Archivos clave:** `keygo-supabase/.../clientapp/adapter/ClientAppRepositoryAdapter.java`

## Propuestas de mejoras futuras

> El acumulador principal de propuestas es **[`ROADMAP.md`](ROADMAP.md)** en la raíz del repositorio.
> Esta sección resume las más relevantes con referencia al ID en ROADMAP.md.
> Al registrar aquí una propuesta, agregarla también en la tabla correspondiente de ROADMAP.md.

### Corto plazo

- ~~**T-001** — Corregir bug `BootstrapAdminKeyFilter` (`getRequestURI()` → `getServletPath()`): todas las rutas son actualmente públicas.~~ ✅ **Completada 2026-03-21**
- **T-002** — Agregar mapper en `keygo-api/platform/` para descargar al controller del mapeo `ServiceInfoProvider → ServiceInfoData`. Ver `ROADMAP.md T-002`.
- **T-023** — Configurar plugin de lint/formato automático (Checkstyle con Google Java Style o Spotless). Convención ya documentada en `docs/keygo-server/CODE_STYLE.md`. Ver `ROADMAP.md T-023`.
- **T-024** — Implementar `TenantResolutionStrategy` por path variable `/{tenantSlug}/` como alternativa al header `X-Tenant-Slug`, necesaria para los endpoints OAuth2 de la Fase 5. Ver `ROADMAP.md T-024`.
- **T-026** — Mantener colecciones Postman actualizadas al agregar cada nuevo endpoint; crear environment `KeyGo-Server-Docker`. Ver `ROADMAP.md T-026`.

### Mediano plazo

- **T-009** — Poblar `keygo-domain` con las primeras entidades de dominio puras: `Tenant`, `User`, `ClientApp`, `Membership`. Ver `ROADMAP.md T-009`. (**Tenant ya implementado en Fase 1**).
- **T-010** — Poblar `keygo-infra` con puertos de infraestructura transversal: `PasswordHasherPort`, `TokenSignerPort`, `ClockProvider`, `AuditPublisherPort`. Ver `ROADMAP.md T-010`.
- **T-013** — Implementar tests de integración con Testcontainers para `keygo-supabase` (incluyendo `TenantRepositoryAdapter`). Ver `ROADMAP.md T-013`.
- **T-025** — Agregar tests de integración con Testcontainers para el flujo completo de Tenant (crear → consultar → suspender). Ver `ROADMAP.md T-025`.

### Largo plazo

- **T-017** — Renombrar `keygo-supabase` → `keygo-adapter-persistence-postgres` para neutralizar acoplamiento al proveedor. Ver `ROADMAP.md T-017`.
- **T-020** — Observabilidad avanzada con OpenTelemetry + Prometheus + Grafana. Ver `ROADMAP.md T-020`.
- **F-010 a F-016** — Core OAuth2/OIDC: authorize, token, JWKS, Auth Code + PKCE. Ver `ROADMAP.md` Fase 1.


