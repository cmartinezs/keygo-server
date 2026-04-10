# Design & Architecture Documentation

Core architectural and design decisions for KeyGo.

---

## Búsqueda Rápida

| Pregunta | Documento |
|---|---|
| ¿Cómo funciona la arquitectura general? | [`core/ARCHITECTURE.md`](core/ARCHITECTURE.md) |
| ¿Cuáles son los patrones adoptados? | [`patterns/PATTERNS.md`](patterns/PATTERNS.md) |
| ¿Qué ResponseCode debería usar? | [`api/ERROR_CATALOG.md`](api/ERROR_CATALOG.md) |
| ¿Dónde va cada tipo de validación? | [`patterns/VALIDATION_STRATEGY.md`](patterns/VALIDATION_STRATEGY.md) |
| ¿Cómo uso @PreAuthorize? | [`patterns/AUTHORIZATION_PATTERNS.md`](patterns/AUTHORIZATION_PATTERNS.md) |
| ¿Cómo mapeo excepciones a errores? | [`patterns/ERROR_HANDLING.md`](patterns/ERROR_HANDLING.md) |
| ¿Qué endpoints existen por dominio? | [`api/ENDPOINT_CATALOG.md`](api/ENDPOINT_CATALOG.md) |
| ¿Cómo versiono la API? | [`api/API_VERSIONING_STRATEGY.md`](api/API_VERSIONING_STRATEGY.md) |

---

## Estructura por Categoría

### 🏛️ Core — Fundamentos

| Documento | Descripción |
|---|---|
| [`ARCHITECTURE.md`](core/ARCHITECTURE.md) | Pilares del sistema: módulos, dependencias, flujos |
| [`DOMAIN_MODEL.md`](core/DOMAIN_MODEL.md) | Entidades de dominio, invariantes, agregados |
| [`MODULES.md`](core/MODULES.md) | Responsabilidad de cada módulo Java |

**Cuándo leer:** Nuevo dev, refactorización mayor, decisiones arquit...

### 🎨 Patterns — Cómo Hacemos las Cosas

| Documento | Descripción |
|---|---|
| [`PATTERNS.md`](patterns/PATTERNS.md) | Patrones + anti-patterns consolidados |
| [`VALIDATION_STRATEGY.md`](patterns/VALIDATION_STRATEGY.md) | Bean Validation vs dominio vs use case |
| [`AUTHORIZATION_PATTERNS.md`](patterns/AUTHORIZATION_PATTERNS.md) | @PreAuthorize, tenant match, RBAC matrix |
| [`ERROR_HANDLING.md`](patterns/ERROR_HANDLING.md) | Mapeo de excepciones a ResponseCode |

**Cuándo leer:** Antes de implementar feature, al hacer code review

### 🔌 API — Contratos HTTP

| Documento | Descripción |
|---|---|
| [`ERROR_CATALOG.md`](api/ERROR_CATALOG.md) | Catálogo de ResponseCode, ErrorData, ejemplos |
| [`ENDPOINT_CATALOG.md`](api/ENDPOINT_CATALOG.md) | Inventario de endpoints por dominio |
| [`API_VERSIONING_STRATEGY.md`](api/API_VERSIONING_STRATEGY.md) | Política de breaking changes, deprecation |

**Cuándo leer:** Diseñar endpoint, documentación de frontend, cambios de API

### ⚙️ Infrastructure — Herramientas

| Documento | Descripción |
|---|---|
| [`PERSISTENCE.md`](infrastructure/PERSISTENCE.md) | JPA, Flyway, schema design |
| [`JWT_AND_KEYS.md`](infrastructure/JWT_AND_KEYS.md) | JWT signing, JWKS, key rotation |
| [`CACHING_STRATEGY.md`](infrastructure/CACHING_STRATEGY.md) | Redis, Caffeine, invalidation |

**Cuándo leer:** Trabajar con persistencia, JWT, performance

---

## Por Estado

### ✅ Documentos Existentes

- `core/ARCHITECTURE.md` (actual, será reorganizado)
- `DOMAIN_MODEL.md`
- `API_SURFACE.md`
- `EXCEPTION_HIERARCHY.md`
- `I18N_STRATEGY.md`
- `UI_CONFIGURATION.md`
- `PROJECT_STRUCTURE.md`
- `TRACING_TELEMETRY.md`
- `BACKLOG.md`

### 🔲 Documentos Nuevos (Sprint 1)

- `patterns/PATTERNS.md` (consolidado)
- `patterns/VALIDATION_STRATEGY.md`
- `patterns/AUTHORIZATION_PATTERNS.md`
- `api/ERROR_CATALOG.md`
- `api/ENDPOINT_CATALOG.md`

### 📋 Pendientes (Sprint 2-3)

- `api/API_VERSIONING_STRATEGY.md`
- `infrastructure/CACHING_STRATEGY.md`
- `infrastructure/PROVISIONING_STRATEGY.md`

---

## Navegación Jerárquica

```
design/
├── README.md (este índice)
├── core/
│   ├── README.md (índice de core)
│   ├── ARCHITECTURE.md
│   └── DOMAIN_MODEL.md
├── patterns/
│   ├── README.md (índice de patrones)
│   ├── PATTERNS.md
│   └── VALIDATION_STRATEGY.md
├── api/
│   ├── README.md (índice de API)
│   ├── ERROR_CATALOG.md
│   └── ENDPOINT_CATALOG.md
└── infrastructure/
    ├── README.md (índice de infra)
    └── PERSISTENCE.md
```

**Workflow:**
1. Abre este README (está aquí)
2. Busca en tabla "Búsqueda Rápida" o ve a subcarpeta
3. Abre README de subcarpeta para más detalles
4. Click en documento específico

---

## Decisiones Clave Documentadas

### Multi-Tenancy
- Resolución por `/{tenantSlug}/` en path
- Tenant validation en todos los endpoints de lectura/escritura
- Ver: `core/ARCHITECTURE.md` + `docs/api/AUTH_FLOW.md`

### RBAC
- Jerarquía: PlatformRole → TenantRole → AppRole
- Validación cascada: Platform → Tenant → Membership
- Ver: `patterns/AUTHORIZATION_PATTERNS.md` + `ROADMAP.md` (T-111)

### Validación
- @NotNull/@Valid en DTOs (HTTP boundary)
- Lógica de negocio en dominio
- Mensajes de error localizados vía i18n
- Ver: `patterns/VALIDATION_STRATEGY.md`

### Errores
- ResponseCode enum en `keygo-api`
- ErrorData con origin, clientMessage, fieldErrors
- Mapeo automático de excepciones en GlobalExceptionHandler
- Ver: `api/ERROR_CATALOG.md`

---

## Referencias Cruzadas

- **Propuestas técnicas:** [`../../ROADMAP.md`](../../ROADMAP.md)
- **Lecciones aprendidas:** [`../ai/lecciones/README.md`](../ai/lecciones/README.md)
- **Inconsistencias detectadas:** [`../ai/inconsistencies/README.md`](../ai/inconsistencies/README.md)
- **Flujos OAuth/OIDC:** [`../api/AUTH_FLOW.md`](../api/AUTH_FLOW.md)
- **Rutas públicas/privadas:** [`../api/BOOTSTRAP_FILTER.md`](../api/BOOTSTRAP_FILTER.md)

---

**Última actualización:** 2026-04-09  
**Responsable:** AI Agent

Próximo paso: Leer el documento específico que necesitas → Subcarpetas tienen sus propios README.
