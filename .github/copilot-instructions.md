# Copilot Instructions — KeyGo Server

Responde en **español (es-MX/es)** por defecto, salvo que el usuario pida otro idioma.

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
  - Actualización de docs si cambian APIs o configuración.

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

