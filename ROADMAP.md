# KeyGo Server — Roadmap de Mejoras

> **Documento vivo.** Los agentes AI deben actualizar este archivo cuando generen nuevas propuestas
> concretas al concluir tareas. Ver instrucciones de mantenimiento al final.

## Estado actual del producto (2026-03-21)

| Dimensión | Estado |
|---|---|
| Arquitectura | Hexagonal definida, módulos activos: `keygo-app`, `keygo-api`, `keygo-run`, `keygo-supabase` |
| Autenticación | Sin implementar — sólo filtro Bootstrap de clave admin |
| Persistencia | Entidades y repositorios JPA base (User, Role, Permission, **Tenant**), Tenant conectado a puerto de negocio |
| API pública | 4 endpoints: `GET /api/v1/service/info`, `GET /api/v1/response-codes`, `POST /api/v1/tenants`, `GET /api/v1/tenants/{slug}`, `PUT /api/v1/tenants/{slug}/suspend` |
| CI/CD | ✅ Pipeline activo en `.github/workflows/ci.yml` (test + package en push/PR a main/develop) |
| Stubs vacíos | `keygo-infra`, `keygo-common` (`keygo-domain` ya tiene modelo Tenant) |
| Tests | 128+ tests unitarios — sin integración ni e2e |
| Fase actual | **Fase 1 🔄 en progreso** — Núcleo de multitenancy (modelo + persistencia + API + resolver) |

---

## Propuestas técnicas

### Corto plazo

> Relacionadas con la base de código actual; bajo esfuerzo; bloquean calidad inmediata.

| ID | Propuesta | Módulo | Justificación |
|---|---|---|---|
| T-001 | Corregir bug `BootstrapAdminKeyFilter`: reemplazar `getRequestURI()` por `getServletPath()` para que el filtro funcione correctamente con `context-path` activo | `keygo-run` | Bug conocido documentado: todas las rutas son actualmente públicas |
| T-002 | Agregar mapper dedicado en `keygo-api/platform/` para transformar `ServiceInfoProvider` → `ServiceInfoData` y descargar al controller de la lógica de mapeo | `keygo-api` | Principio de responsabilidad única en controllers |
| T-003 | Agregar `request/` bajo `keygo-api/platform/` para DTOs de entrada cuando aparezcan endpoints con body o query params | `keygo-api` | Anticipa crecimiento ordenado |
| T-004 | Crear sub-paquetes `command/`, `query/` y `result/` en `keygo-app/platform/` al implementar el primer use case con entrada/salida propia | `keygo-app` | Patrón CQRS mínimo para separar intención |
| T-005 | Restringir `management.endpoints.web.exposure.include` a `health,info` en el perfil `prod` | `keygo-run` | Actuator actualmente expone todos los endpoints (`"*"`) — riesgo de seguridad |
| T-007 | Renombrar config de IntelliJ `.run/KeyGo Runner.run.xml` a `.run/KeygoApplication.run.xml` para reflejar el nombre actual de la clase principal | Infra dev | Consistencia tras renombrado de `KeyGoRunner` → `KeygoApplication` |
| T-023 | Configurar plugin de lint/formato automático (Checkstyle con Google Java Style o Spotless) en el `pom.xml` raíz; integrar como paso en CI | `pom.xml` raíz / CI | La convención de 2 espacios ya está documentada en `docs/keygo-server/CODE_STYLE.md`; falta enforcement automático |
| T-024 | Implementar `TenantResolutionStrategy` por path variable `/{tenantSlug}/` como complemento al header `X-Tenant-Slug` del `TenantResolutionFilter` actual | `keygo-run` / `keygo-api` | La Fase 5 requiere endpoints `/{tenantSlug}/oauth2/authorize`; el filtro actual solo resuelve por header |
| T-025 | Agregar tests de integración con Testcontainers para el ciclo completo de Tenant: crear → consultar → suspender vía `TenantRepositoryAdapter` | `keygo-supabase` | El adaptador solo tiene tests unitarios con Mockito; la persistencia real no se valida aún |

---

### Mediano plazo

> Evoluciones naturales de la arquitectura actual; esfuerzo moderado; habilitan features reales.

| ID | Propuesta | Módulo | Justificación |
|---|---|---|---|
| T-008 | Definir interfaz `BootstrapFilterProperties` en `keygo-api` que `KeyGoBootstrapProperties` implemente; mover `BootstrapAdminKeyFilter` a `keygo-api/security/filter/` | `keygo-api` / `keygo-run` | Elimina dependencia circular que hoy fuerza el filtro a vivir en `keygo-run` |
| T-009 | Poblar `keygo-domain` con las primeras entidades de dominio puras: `Tenant`, `User`, `ClientApp`, `Membership` (sin Spring, sin JPA) | `keygo-domain` | Actualmente es un stub vacío; bloquea el modelo de negocio real |
| T-010 | Poblar `keygo-infra` con puertos de infraestructura transversal: `PasswordHasherPort`, `TokenSignerPort`, `ClockProvider`, `AuditPublisherPort`; `keygo-supabase` se conecta a ellos | `keygo-infra` | Actualmente vacío; `keygo-supabase` sin mediación de puertos genera acoplamiento directo |
| T-011 | Agregar capa `persistence/` en `keygo-supabase` como intermediaria entre `config/` y las features (`user/`, `membership/`) para aislar aún más los detalles de JPA | `keygo-supabase` | Definido en `docs/arch/keygo_server_project_structure.md` sección 5.4 |
| T-012 | Crear `keygo-supabase/support/` para utilidades de persistencia transversales (type converters JPA, entity listeners, auditoría base) | `keygo-supabase` | Evita duplicar código de auditoría en cada entidad |
| T-013 | Implementar tests de integración con Testcontainers PostgreSQL para `keygo-supabase` | `keygo-supabase` | Actualmente sólo hay tests unitarios — la persistencia no se valida con DB real |
| T-014 | Configurar perfiles de entorno separados: `dev`, `test`, `prod`; centralizar configuraciones sensibles en `keygo-run` | `keygo-run` | Actualmente sólo `supabase` y `local`; sin separación clara de entornos |
| T-015 | Agregar comprobación de dependencias con `OWASP Dependency-Check` o similar en el pipeline CI | CI | Detectar CVEs en dependencias antes de merge |
| T-016 | Configurar Jacoco para cobertura de tests y fallar el build si baja del umbral definido | CI / Build | Sin métrica de cobertura actualmente |

---

### Largo plazo

> Capacidades estratégicas del sistema; alto esfuerzo o dependencias externas.

| ID | Propuesta | Módulo | Justificación |
|---|---|---|---|
| T-017 | Renombrar `keygo-supabase` a `keygo-adapter-persistence-postgres` para neutralizar el nombre respecto al proveedor y reflejar el rol de adapter | Infra | El nombre actual acopla semánticamente a Supabase; facilita soportar otros providers |
| T-018 | Implementar ADRs (Architecture Decision Records) en `docs/adr/` para las decisiones de diseño más relevantes (hexagonal, multi-módulo, Jackson 3, Spring Boot 4) | Docs | Facilita onboarding y justifica decisiones ante nuevos colaboradores |
| T-019 | Evaluar migración a GraalVM Native Image para reducir arranque y footprint en despliegue containerizado | `keygo-run` | Relevante si se despliega en entornos con arranque en frío frecuente |
| T-020 | Implementar observabilidad avanzada: tracing distribuido con OpenTelemetry, métricas en Prometheus, dashboards en Grafana | Infra | Necesario para operación en producción real del SaaS |
| T-021 | Diseñar estrategia de multi-region / alta disponibilidad para el servicio de autenticación | Infra | Crítico para SLAs de producción en IAM |
| T-022 | Implementar caching distribuido (Redis) para tokens, JWKS y sesiones activas | `keygo-infra` | Reducir latencia y carga a DB en validación de tokens |

---

## Propuestas funcionales

> Organizadas según el orden de implementación recomendado en `docs/arch/keygo_server_backlog_v_1.md`.

### Fase 0 — Fundación técnica

| ID | Épica / Historia | Prioridad | Descripción resumida |
|---|---|---|---|
| F-001 | E1-H2: Estándares de calidad | P0 | Lint/format, convenciones de commits, pipeline CI inicial |
| F-002 | E1-H3: Persistencia base | P0 | PostgreSQL + Flyway + Testcontainers operativos end-to-end |
| F-003 | E2-H1: Modelo `Tenant` | P0 | Entidad de dominio, persistencia, unicidad por `slug` |
| F-004 | E2-H2: Resolución de tenant | P0 | Propagar contexto de tenant a la request desde entrada HTTP |
| F-005 | E2-H3: Aislamiento lógico por tenant | P0 | `tenant_id` en entidades relevantes, índices, validaciones |

### Fase 1 — Core IAM usable

| ID | Épica / Historia | Prioridad | Descripción resumida |
|---|---|---|---|
| F-006 | E3-H1: Modelo `ClientApp` | P0 | `client_id`, `client_secret_hash`, tipo PUBLIC/CONFIDENTIAL |
| F-007 | E3-H2: Redirect URIs | P0 | Gestión y validación de URIs permitidas por app |
| F-008 | E4-H1: Modelo `User` | P0 | Email/username, password hash, status, unicidad por tenant |
| F-009 | E5-H1: Modelo `Membership` | P0 | Relación user ↔ app, unicidad, estado |
| F-010 | E6-H1: Endpoint `/oauth2/authorize` | P0 | Validar tenant, client, redirect URI, response type, PKCE |
| F-011 | E6-H2: Hosted Login UI | P0 | Pantalla de login central integrada al flujo OAuth2 |
| F-012 | E6-H3: Autenticación de credenciales | P0 | Validar usuario, password, estado, acceso por membership |
| F-013 | E6-H4: Emisión de authorization code | P0 | Code temporal con expiración, amarrado a PKCE challenge |
| F-014 | E6-H5: Endpoint `/oauth2/token` | P0 | Canje de code, validación PKCE, emisión de tokens |
| F-015 | E8-H1: Signing keys | P0 | Clave activa, historial, soporte a rotación |
| F-016 | E8-H2: Endpoint JWKS | P0 | `/.well-known/jwks.json` por tenant |
| F-017 | E14-H1: Hashing seguro de passwords | P0 | BCrypt/Argon2 desde el primer día |
| F-018 | E14-H2: Hashing seguro de refresh tokens | P0 | Nunca persistir refresh tokens en texto plano |

### Fase 2 — Vendible para terceros

| ID | Épica / Historia | Prioridad | Descripción resumida |
|---|---|---|---|
| F-019 | E3-H3: Grants y scopes por app | P1 | Auth Code+PKCE, Client Credentials, scopes configurables |
| F-020 | E3-H4: Rotación de client secret | P1 | Mostrar secret una sola vez; invalidar anterior |
| F-021 | E4-H2: Alta de usuario desde admin | P1 | Endpoint admin para creación de usuarios |
| F-022 | E4-H3: Desactivar usuario | P1 | Suspensión lógica; impide login |
| F-023 | E4-H4: Reset de contraseña | P1 | Token seguro de reset; endpoint de cambio |
| F-024 | E5-H4: `AppRole` y `MembershipRole` | P1 | Roles locales por app; roles en tokens |
| F-025 | E7-H1: `client_credentials` grant | P1 | M2M sin usuario; access token técnico |
| F-026 | E8-H3: `openid-configuration` | P1 | Metadata OIDC por tenant |
| F-027 | E8-H4: Refresh tokens con rotación | P1 | Hash persistido, renovación segura, revocación |
| F-028 | E9-H1: Endpoint `/userinfo` | P1 | Devolver claims básicas del usuario autenticado |
| F-029 | E10: Tenant Admin API | P1 | CRUD de apps, usuarios, memberships, roles |
| F-030 | E12-H1/H2/H3: Self-service del usuario | P1 | Forgot password, reset, change password |
| F-031 | E14-H3: Rate limiting | P1 | Proteger login y token endpoint contra fuerza bruta |
| F-032 | E14-H5: Validación estricta de redirect URIs | P1 | Coincidencia exacta; sin wildcards peligrosos |

### Fase 3 — Operación real del SaaS

| ID | Épica / Historia | Prioridad | Descripción resumida |
|---|---|---|---|
| F-033 | E11: Control plane / Platform Admin | P1 | Crear/suspender tenants, auditoría global |
| F-034 | E13-H1: Auditoría de eventos críticos | P0 | Login, token emitido, secret rotado, membership creada |
| F-035 | E13-H2: Logs estructurados | P1 | JSON logs con correlación de request/tenant |
| F-036 | E13-H3: Métricas básicas | P1 | Prometheus/Actuator: logins, tokens, errores |
| F-037 | E12-H4/H5: Ver/cerrar sesiones activas | P2 | Self-service de sesiones para el usuario final |
| F-038 | E13-H4: Alertas operativas | P2 | Umbral de errores de login, tasa de tokens fallidos |

---

## Features fuera del MVP v1

> Capacidades válidas pero explícitamente postergadas para no sobrecomplicar el MVP.

| Feature | Motivo de postergación |
|---|---|
| MFA (TOTP, SMS, WebAuthn) | Aumenta complejidad de UX y backend; agregar en v2 |
| SAML 2.0 | Requerido por enterprise; no necesario para MVP |
| SCIM 2.0 (provisioning) | Integración HR/IdP; postergada a v2-v3 |
| ABAC (Attribute-Based Access Control) | Roles suficientes para MVP; ABAC es extensión |
| Social Login (Google, GitHub, etc.) | OAuth2 externo; útil pero no bloquea MVP |
| WebAuthn / Passkeys | Tecnología emergente; agregar cuando sea estable |
| Multi-region / HA | Necesario en producción real; fuera del alcance del MVP técnico |

---

## Historial de propuestas completadas

> Mover aquí las propuestas implementadas para mantener trazabilidad.

| ID original | Propuesta | Completada | PR / Commit referencia |
|---|---|---|---|
| T-006 | Configurar GitHub Actions: pipeline CI mínimo con `./mvnw test` y `./mvnw clean package` en cada push/PR | 2026-03-21 | `.github/workflows/ci.yml` creado; Fase 0 cerrada |
| — | Reorganizar paquetes internos por feature (keygo-api, keygo-app, keygo-run, keygo-supabase) | 2026-03-17 | Refactor de estructura interna |
| F-003 | E2-H1: Modelo `Tenant` — entidad de dominio, persistencia, unicidad por `slug` | 2026-03-21 | `keygo-domain/tenant/model/`, `keygo-supabase/tenant/`, migración `V4__add_tenants.sql`, puertos y use cases en `keygo-app/tenant/` |
| F-004 | E2-H2: Resolución de tenant — propagar contexto de tenant a la request desde entrada HTTP | 2026-03-21 | `TenantContextHolder` (keygo-app), `TenantResolutionFilter` por header `X-Tenant-Slug` (keygo-run) |

---

## Instrucciones de mantenimiento para agentes AI

> Esta sección define cómo los agentes deben actualizar este documento.

### Cuándo actualizar este archivo

| Evento | Acción |
|---|---|
| Se completa una propuesta técnica o funcional | Mover la fila a la tabla **"Historial de propuestas completadas"** con fecha y referencia al commit/PR |
| Se genera una nueva propuesta técnica al concluir una tarea | Agregar fila en la tabla correspondiente de **Propuestas técnicas** (corto/mediano/largo plazo) |
| Se decide descartar o posponer una propuesta | Mover a **"Features fuera del MVP v1"** con justificación; o simplemente eliminar con nota en el commit |
| Se actualiza el horizonte temporal de una propuesta | Mover la fila a la tabla del nuevo horizonte |

### Formato para IDs

- Técnicas: `T-NNN` (correlativo, continuando desde el último)
- Funcionales: `F-NNN` (correlativo, continuando desde el último)

### Regla de escritura

- Las propuestas deben ser **concretas y accionables** (no genéricas).
- Cada propuesta debe indicar el **módulo afectado** y la **justificación**.
- No crear propuestas duplicadas: revisar las tablas antes de agregar.

### Referencia cruzada con AI_CONTEXT.md

Cuando se registre una propuesta en este archivo **que sea recurrente o de alto valor**, añadir también
una entrada breve en `AI_CONTEXT.md → ## Propuestas de mejoras futuras` con la referencia al ID
(p. ej. `ver ROADMAP.md T-010`).

