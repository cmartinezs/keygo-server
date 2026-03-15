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
keygo-domain   → sin dependencias internas ni Spring
keygo-app      → depende de domain; define puertos (interfaces)
keygo-infra    → implementa puertos; depende de app
keygo-api      → llama usecases; devuelve BaseResponse<T>
keygo-supabase → JPA/Flyway; implementaciones de repos Supabase
keygo-run      → cablea todo; tiene application.yml y main
```

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
- `BootstrapAdminKeyFilter` protege `/api/**` con header `X-KEYGO-ADMIN`.
- Con `context-path=/keygo-server`, los URIs tienen prefijo `/keygo-server/` — validar que el filtro aplique correctamente.
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

