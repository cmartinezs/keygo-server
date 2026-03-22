# Documentación — KeyGo Server

> **Última actualización:** 2026-03-22 — Reestructuración completa: 7 carpetas legacy → 5 categorías temáticas + categoría `ai/` para base de conocimiento del agente.

---

## Estructura

```
docs/
├── ai/          Base de conocimiento para agentes AI (lecciones, propuestas, inconsistencias, registro)
├── design/      Arquitectura, modelo de dominio, diseño de API y backlog
├── api/         Guías de uso de la API REST: auth, bootstrap, OpenAPI, códigos de respuesta
├── data/        Modelo de datos, relaciones entre entidades y migraciones Flyway
├── development/ Herramientas de desarrollo: IntelliJ, entornos, tests, estilo de código
├── operations/  Despliegue, Docker, firma de tokens y JWKS
└── research/    Investigaciones y reportes técnicos de referencia
```

---

## 🤖 ai/ — Base de conocimiento AI

> Documentación mantenida por el agente de forma continua. No requiere orden explícita del usuario.

| Documento | Descripción | Audiencia |
|---|---|---|
| [lecciones.md](ai/lecciones.md) | Errores resueltos, buenas prácticas y convenciones adoptadas durante el trabajo del agente | AI Agents |
| [propuestas.md](ai/propuestas.md) | Propuestas técnicas (T-NNN) y funcionales (F-NNN) — resumen de estado rápido | AI Agents |
| [inconsistencias.md](ai/inconsistencias.md) | Centralizador de inconsistencias detectadas entre docs y código/DB | AI Agents |
| [inconsistencias-datos.md](ai/inconsistencias-datos.md) | Detalle de inconsistencias en el modelo de datos / schema DB | AI Agents, DBA |
| [agents-registro.md](ai/agents-registro.md) | Historial detallado de cambios en módulos, comandos, patrones y URLs del quick-start | AI Agents |

> Ver también `AI_CONTEXT.md` y `AGENTS.md` en la raíz del repo para los resúmenes de referencia rápida con enlaces a esta carpeta.

---

## 📐 design/ — Arquitectura y diseño

| Documento | Descripción | Audiencia |
|---|---|---|
| [ARCHITECTURE.md](design/ARCHITECTURE.md) | **Doc canónico de arquitectura** — objetivos, módulos, flujos OAuth2/OIDC, multi-tenancy, planos del sistema | Arquitectos, Devs Senior |
| [DOMAIN_MODEL.md](design/DOMAIN_MODEL.md) | Modelo de dominio: entidades, value objects, invariantes, estados | Arquitectos, Devs |
| [IMPLEMENTATION_PLAN.md](design/IMPLEMENTATION_PLAN.md) | Plan de implementación Fases 0–11, estado de avance | Arquitectos, Devs |
| [API_SURFACE.md](design/API_SURFACE.md) | Superficie de API del MVP: planos, endpoints, payloads, reglas de seguridad | Arquitectos, Devs |
| [BACKLOG.md](design/BACKLOG.md) | Backlog técnico v1: épicas, historias, criterios de aceptación | Producto, Devs |
| [PROJECT_STRUCTURE.md](design/PROJECT_STRUCTURE.md) | Estructura de módulos Maven y convenciones de paquetes | Devs |

> Ver también `ARCHITECTURE.md` en la raíz del repo para un resumen rápido con enlace a este directorio.

---

## 🌐 api/ — API REST

| Documento | Descripción | Audiencia |
|---|---|---|
| [AUTH_FLOW.md](api/AUTH_FLOW.md) | Flujo completo OAuth 2.0 Authorization Code + PKCE — guía para clientes (SPA, Mobile, Backend) | Devs Frontend/Mobile |
| [OPENAPI.md](api/OPENAPI.md) | Swagger UI, grupos de API, autenticación en la UI, anotaciones en controllers, springdoc config | Devs, QA |
| [BOOTSTRAP_FILTER.md](api/BOOTSTRAP_FILTER.md) | Filtro `X-KEYGO-ADMIN`: configuración, rutas protegidas/públicas, testing | Devs, DevOps |
| [RESPONSE_CODES.md](api/RESPONSE_CODES.md) | Catálogo de `ResponseCode`, uso de `BaseResponse<T>`, manejo de errores | Devs, QA |

---

## 🗄️ data/ — Modelo de datos

| Documento | Descripción | Audiencia |
|---|---|---|
| [DATA_MODEL.md](data/DATA_MODEL.md) | Diccionario completo de tablas: campos, tipos, constraints, reglas de negocio | Devs, QA, DBA |
| [ENTITY_RELATIONSHIPS.md](data/ENTITY_RELATIONSHIPS.md) | Diagramas E/R, flujos de datos, state machines, índices SQL | Devs, Arquitectos |
| [MIGRATIONS.md](data/MIGRATIONS.md) | Migraciones Flyway V1–V10 + convenciones para futuras migraciones | Devs, DBA |

---

## 🛠️ development/ — Herramientas de desarrollo

| Documento | Descripción | Audiencia |
|---|---|---|
| [INTELLIJ_SETUP.md](development/INTELLIJ_SETUP.md) | IntelliJ IDEA: Lombok, annotation processing, runner, EnvFile plugin | Devs |
| [ENVIRONMENT_SETUP.md](development/ENVIRONMENT_SETUP.md) | Variables de entorno, archivos `.env`, cambio de ambientes, CI/CD | Devs, DevOps |
| [TEST_STRATEGY.md](development/TEST_STRATEGY.md) | Estrategia de testing: JUnit 5, Mockito, Testcontainers, Postman, dependencias Maven | Devs, QA |
| [CODE_STYLE.md](development/CODE_STYLE.md) | Convenciones de código, nombres, imports, patrones | Devs |

---

## ⚙️ operations/ — Operaciones y despliegue

| Documento | Descripción | Audiencia |
|---|---|---|
| [DOCKER.md](operations/DOCKER.md) | Build Docker, Compose, registro de imágenes, deployment | DevOps, Devs |
| [SIGNING_AND_JWKS.md](operations/SIGNING_AND_JWKS.md) | JWT signer RSA (Nimbus), JWKS builder, PkceVerifier, ciclo de vida de signing keys | Devs, DevOps |

---

## 🔍 Navegación rápida por perfil

### Soy nuevo en el proyecto
1. [`ARCHITECTURE.md`](design/ARCHITECTURE.md) — estructura general
2. [`IMPLEMENTATION_PLAN.md`](design/IMPLEMENTATION_PLAN.md) — qué está implementado
3. [`INTELLIJ_SETUP.md`](development/INTELLIJ_SETUP.md) — configurar el IDE
4. [`ENVIRONMENT_SETUP.md`](development/ENVIRONMENT_SETUP.md) — configurar entornos

### Quiero entender el flujo OAuth2/OIDC
1. [`AUTH_FLOW.md`](api/AUTH_FLOW.md) — flujo completo desde el cliente
2. [`SIGNING_AND_JWKS.md`](operations/SIGNING_AND_JWKS.md) — firma de tokens y JWKS
3. [`OPENAPI.md`](api/OPENAPI.md) — probar los endpoints en Swagger UI

### Quiero trabajar con la base de datos
1. [`MIGRATIONS.md`](data/MIGRATIONS.md) — migraciones V1–V10 y cómo crear V11+
2. [`DATA_MODEL.md`](data/DATA_MODEL.md) — diccionario de tablas
3. [`ENTITY_RELATIONSHIPS.md`](data/ENTITY_RELATIONSHIPS.md) — relaciones E/R

### Soy un agente AI
- Ver [`AGENTS.md`](../AGENTS.md) — quick-start con módulos, patrones y flujos
- Ver [`AI_CONTEXT.md`](../AI_CONTEXT.md) — estado del proyecto, convenciones
- Ver [`docs/ai/`](ai/) — lecciones, propuestas, inconsistencias y registro de cambios
