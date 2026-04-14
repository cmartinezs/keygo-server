# T-151 — Estrategia de despliegue y CI/CD (develop / test / prod)

**Estado:** ⬜ Registrada
**Módulos afectados:** `keygo-run`, `keygo-supabase`, docs, infraestructura, CI/CD

---

## Problema / Requisito

No existe una definición formal de cómo se despliega KeyGo Server en ambientes distintos al
desarrollo local. Se deben resolver las siguientes preguntas antes de poder operar en producción:

- ¿Qué ambientes existen y qué garantías ofrece cada uno?
- ¿Se usa infraestructura propia, un cloud provider (AWS / GCP / Azure) o un PaaS
  (Railway, Render, Fly.io, Supabase hosted, etc.)?
- ¿Cómo se construye y publica la imagen Docker?
- ¿Cómo se gestiona la configuración sensible (secrets) por ambiente?
- ¿Cómo se ejecutan las migraciones Flyway en cada ambiente de forma segura?
- ¿Qué pipeline de CI/CD implementa build, test, análisis de calidad y despliegue?

---

## Relaciones

| Artefacto relacionado | Tipo de relación | Descripción |
|---|---|---|
| T-152 | bloqueante | Los perfiles `develop`/`test`/`prod` y la externalización de config unsafe deben existir antes de definir qué inyectar en cada ambiente |
| T-023 | bloqueante | Checkstyle debe fallar el build en CI para que el quality gate sea real; actualmente solo emite warnings |
| T-091 | bloqueante | El pipeline debe validar migraciones Flyway con DB real (Testcontainers) antes de cada despliegue |
| T-149 | habilitadora | T-149 produce la imagen Docker nativa optimizada; T-151 puede funcionar con imagen JVM mientras tanto |
| T-020 | complementaria | Observabilidad (OpenTelemetry/Prometheus) idealmente disponible en test y prod, pero no bloquea el pipeline base |
| T-036 | complementaria | TTLs de tokens configurables por ambiente; conveniente pero no bloquea el despliegue inicial |

---

## Solución propuesta

<!-- A completar cuando la tarea transite a 📋 Planificada -->

Puntos a definir y documentar:

### 1. Ambientes objetivo

| Ambiente | Propósito | Rama fuente |
|---|---|---|
| `local` | Desarrollo individual, H2 in-memory | cualquier rama |
| `develop` | Integración continua, base de datos compartida efímera | `develop` |
| `test` (staging) | Validación funcional previa a prod, réplica de datos anonimizados | `release/*` o `main` |
| `prod` | Producción real | `main` |

### 2. Decisión de plataforma

Evaluar y decidir entre:

- **PaaS ligero** (Railway, Render, Fly.io): menor operación, costo bajo en early stage, buena DX
- **Cloud managed** (AWS ECS/Fargate + RDS, GCP Cloud Run + Cloud SQL): más control, mayor madurez
- **Supabase hosted** para base de datos (ya usado en local con perfil `supabase`)
- **Contenedor propio** en VPS (DigitalOcean / Hetzner): costo mínimo, mayor operación

Criterios de decisión: costo, complejidad operativa, capacidad de escalar, integración con Supabase.

### 3. Pipeline CI/CD

Etapas mínimas a implementar (GitHub Actions o equivalente):

```
push → [build] → [test] → [quality-gate] → [build-image] → [push-registry] → [deploy]
```

| Etapa | Herramienta | Detalle |
|---|---|---|
| Build | Maven Wrapper | `./mvnw clean package -DskipTests` |
| Test | JUnit + Testcontainers | `./mvnw verify` con Flyway validate |
| Quality gate | Checkstyle + JaCoCo | Falla si cobertura < umbral o violaciones Checkstyle |
| Build image | Docker multi-stage | Imagen JVM o nativa (T-149) |
| Push registry | GHCR / Docker Hub / ECR | Tag por rama y SHA corto |
| Deploy | Plataforma elegida | Rolling deploy con health check |

### 4. Gestión de secrets

- Variables de entorno inyectadas por la plataforma (no en repositorio)
- `application.yml` con `${ENV_VAR:default}` para cada secret
- Flyway credentials separadas del runtime si la plataforma lo permite

### 5. Migraciones en despliegue

- Flyway se ejecuta automáticamente al arrancar (`spring.flyway.enabled=true`)
- En prod: evaluar modo `validate` en runtime y ejecutar migraciones en job previo aislado

---

## Pasos de implementación

| # | Acción | Archivo | Estado |
|---|---|---|---|
| 1 | Definir y documentar los ambientes con sus garantías y configuraciones | `doc/07-operations/environments.md` | PENDING |
| 2 | Evaluar y decidir plataforma de despliegue (PaaS vs cloud); registrar en ADR | `doc/04-decisions/adr/` | PENDING |
| 3 | Crear `Dockerfile` multi-stage para imagen JVM | `Dockerfile` | PENDING |
| 4 | Crear workflow CI base en GitHub Actions (build + test + quality gate) | `.github/workflows/ci.yml` | PENDING |
| 5 | Crear workflow CD por ambiente (develop, test, prod) | `.github/workflows/cd-develop.yml`, `cd-prod.yml` | PENDING |
| 6 | Definir estrategia de secrets por ambiente y documentarla | `doc/07-operations/secrets-management.md` | PENDING |
| 7 | Validar ejecución de Flyway en CI con Testcontainers (T-091) | — | PENDING |
| 8 | Documentar runbook de despliegue manual de emergencia | `doc/07-operations/runbook-deploy.md` | PENDING |
| 9 | Actualizar `doc/07-operations/environment-setup.md` con referencias a los nuevos docs | `doc/07-operations/environment-setup.md` | PENDING |

---

## Guía de verificación

```bash
# Pipeline CI ejecuta sin errores
# (verificar en GitHub Actions / equivalente)

# Build local de imagen Docker
docker build -t keygo-server:local .
docker run --rm -e SPRING_PROFILES_ACTIVE=local keygo-server:local

# Health check tras arranque
curl http://localhost:8080/keygo-server/actuator/health
```

Criterios de aceptación:
- Cada push a `develop` dispara CI completo (build + tests + quality gate)
- Cada merge a `main` dispara despliegue automático a prod con zero-downtime
- Los secrets nunca aparecen en el repositorio ni en los logs
- Flyway se ejecuta correctamente en el arrange del contenedor en cada ambiente

---

## Historial de transiciones

- 2026-04-14 — Creada en estado ⬜ Registrada.
