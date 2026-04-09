# API Versioning Strategy — Cómo Evolucionar APIs sin Romper Clientes

**Propósito:** Documentar cómo manejar cambios en API, deprecation, y migración entre versiones.

---

## Decisión: URI Path Versioning

**Estrategia:** Version en la URL (`/api/v1`, `/api/v2`)

```
/api/v1/tenants/{slug}     ← stable, backward-compatible
/api/v2/tenants/{slug}     ← new, breaking changes allowed
```

**Ventajas:**
- Explícito: cliente elige versión en URL
- Cacheable: CDN/proxies pueden cachear por versión
- Evita header negotiation
- OpenAPI clara (docs por version)

**Desventajas:**
- Duplicación de endpoints
- Debe mantener v1 + v2 en paralelo

**Alternativas rechazadas:**
- ❌ Content-Type versioning (`application/vnd.keygo.v2+json`) — demasiado complejo
- ❌ Header versioning (`X-API-Version: 2`) — no cacheble, menos explícito
- ❌ Implicit versioning (estoy en v1 siempre) — impossible evolucionar

---

## Semantic Versioning de API

```
/api/v{MAJOR}.{MINOR}
      └────────────────── breaking changes only
```

**v1.0 → v1.1:** Cambios backward-compatible (nuevo endpoint, nuevo campo opcional)
**v1.0 → v2.0:** Breaking changes (remover endpoint, cambiar estructura)

**Dentro de v1:** Solo agregar fields, endpoints, never quitar.

---

## Tipos de Cambios

### Backward-Compatible (No requiere v2)

Agregar en **v1**, opcional en cliente:

✅ **Nuevo endpoint**
```
POST /api/v1/tenants/{slug}/notifications/subscribe  ← v1.1
```

✅ **Nuevo field opcional en response**
```json
{
  "id": "...",
  "name": "...",
  "createdAt": "2026-04-10T...",  ← v1.1, nuevo campo
  "lastActivityAt": "2026-04-10T..."  ← v1.1, nuevo campo
}
```

✅ **Nuevo query parameter**
```
GET /api/v1/tenants?include=stats  ← v1.1, opcional
```

✅ **Nuevo HTTP header response**
```
X-RateLimit-Remaining: 99  ← v1.1, nuevo header
```

### Breaking Changes (Requiere v2)

❌ **Remover endpoint**
- v1: POST /api/v1/tenants/{slug}/foo
- v2: POST /api/v2/tenants — sin /foo

❌ **Cambiar nombre de field**
- v1: `firstName`
- v2: `givenName`

❌ **Cambiar tipo de field**
- v1: `createdAt` (timestamp string)
- v2: `createdAt` (unix epoch integer)

❌ **Remover campo requerido**
- v1: `email` (required)
- v2: `email` (removed)

❌ **Cambiar estructura de error**
- v1: `{ "error": { "code": "...", "message": "..." } }`
- v2: `{ "code": "...", "detail": "..." }`

---

## Lifecycle: De v1 a v2

### Fase 1: Desarrollo (Mes 1-2)

- Diseñar v2 en documentación (RFC)
- Implementar v2 endpoints en paralelo con v1
- Tests para ambas versiones
- v1 sigue siendo default

### Fase 2: Anuncio (Mes 3)

```
Email a clientes:
"v2 disponible en /api/v2. Plan de deprecación de v1:
- Ahora: v2 en beta
- Mes 6: v1 deprecated (legacy support)
- Mes 9: v1 removida"
```

**Docs actualizado:**
```markdown
## v2.0 — Current

Recomendado para nuevas integraciones.

[API Reference]

---

## v1.0 — Legacy (deprecated, sunset 2026-10-10)

Soportado hasta 2026-10-10. Migrar a v2.
[Migration guide →]
```

### Fase 3: Soporte Dual (Mes 3-6)

- v1 + v2 ambos soportados
- v1 documentado como "legacy"
- Clientes migran a su ritmo

### Fase 4: Deprecation (Mes 6-9)

```
GET /api/v1/...

Response headers:
Deprecation: true
Sunset: Sun, 10 Oct 2026 00:00:00 GMT
Link: </api/v2/...>; rel="successor-version"
```

### Fase 5: Sunset (Mes 9)

- v1 removida
- v2 es la única versión
- Clientes que no migraron obtienen 410 Gone

```
GET /api/v1/...
↓
410 Gone
{
  "code": "API_VERSION_SUNSET",
  "message": "API v1 is no longer supported. Migrate to v2.",
  "successor": "https://docs.keygo.local/api/v2"
}
```

---

## Implementación: Estructura de Carpetas

```
keygo-api/src/main/java/io/cmartinezs/keygo/api/
├── v1/
│   ├── controller/
│   │   ├── TenantController.java
│   │   ├── UserController.java
│   │   └── ...
│   ├── request/
│   │   ├── CreateTenantRequest.java
│   │   └── ...
│   └── response/
│       ├── TenantData.java
│       └── ...
│
├── v2/
│   ├── controller/
│   │   ├── TenantController.java    ← Nuevo diseño
│   │   └── ...
│   ├── request/
│   │   ├── CreateTenantRequest.java  ← Estructura diferente
│   │   └── ...
│   └── response/
│       ├── TenantData.java           ← Nuevos campos
│       └── ...
│
└── shared/
    ├── ResponseCode.java             ← Compartido entre versiones
    ├── ErrorData.java                ← Compartido
    └── BaseResponse.java             ← Compartido
```

### Registro de Paths

```java
@Configuration
public class ApiVersionConfig {
  
  // v1 — Legacy
  @Bean
  public WebMvcRegistrations v1Registration() {
    return new WebMvcRegistrations() {
      @Override
      public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
        mapping.setOrder(1);  // v1 tiene prioridad menor
        return mapping;
      }
    };
  }
  
  // v2 — Current
  @Bean
  public WebMvcRegistrations v2Registration() {
    return new WebMvcRegistrations() {
      @Override
      public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
        mapping.setOrder(0);  // v2 tiene prioridad mayor
        return mapping;
      }
    };
  }
}
```

### Controllers

```java
// v1 — Legacy
@RestController
@RequestMapping("/api/v1/tenants")
@Tag(name = "Tenants (v1 Legacy)")
public class TenantControllerV1 { ... }

// v2 — Current
@RestController
@RequestMapping("/api/v2/tenants")
@Tag(name = "Tenants (v2 Current)")
public class TenantControllerV2 { ... }
```

---

## Migration Guide: v1 → v2

**Cambios clave en v2:**

| Aspecto | v1 | v2 |
|---|---|---|
| **Tenant ID** | slug (string) | id (UUID) |
| **Response wrap** | `{ data, success }` | `{ data, meta }` |
| **Error structure** | `{ error: { code, message } }` | `{ error: { code, detail, traceId } }` |
| **Pagination** | `page, size, totalElements` | `offset, limit, total` (cursor-friendly) |

**Ejemplo: GET /tenants**

**v1:**
```bash
GET /api/v1/tenants?page=0&size=20

Response:
{
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 100
  },
  "success": { "code": "TENANT_LIST_RETRIEVED" }
}
```

**v2:**
```bash
GET /api/v2/tenants?offset=0&limit=20

Response:
{
  "data": {
    "items": [...],
    "pagination": {
      "offset": 0,
      "limit": 20,
      "total": 100
    }
  },
  "meta": {
    "version": "2.0",
    "timestamp": "2026-04-10T10:00:00Z"
  }
}
```

---

## Deprecation Headers

Agregar a todas las respuestas v1:

```
Deprecation: true
Sunset: Sun, 10 Oct 2026 00:00:00 GMT
Link: </api/v2/tenants>; rel="successor-version"
```

**Cliente ve:**
```
Warning: 299 - "Deprecation: API version v1 sunset on 2026-10-10"
```

---

## Estrategia: Feature Flags (Alternativa)

Si no quieres duplicar endpoints, usar feature flags:

```java
@GetMapping("/api/v1/tenants/{slug}")
public ResponseEntity<?> getTenant(
    @PathVariable String slug,
    @RequestParam(defaultValue = "false") boolean useV2Format) {
  
  Tenant tenant = tenantService.getBySlug(slug);
  
  if (useV2Format || client.isPinned(2)) {
    return ResponseEntity.ok(tenant.toV2Response());
  } else {
    return ResponseEntity.ok(tenant.toV1Response());
  }
}
```

**Ventaja:** No duplicar código
**Desventaja:** Lógica condicional, difícil de mantener a largo plazo

**Recomendación:** Solo para cambios menores (v1.1 → v1.2). Para cambios mayores (v1 → v2), usar URI path versioning.

---

## Checklist: Cambio de API

¿Quieres cambiar un endpoint?

- [ ] **¿Breaking change?**
  - Sí → Implementar en v2, agregar deprecation headers en v1, planificar sunset
  - No → Agregar en v1.x

- [ ] **¿Qué clientes afecta?** (buscar en logs)
  - Pocos → rápido sunset
  - Muchos → soporte dual más largo

- [ ] **¿Documentación actualizada?** (ENDPOINT_CATALOG.md, OpenAPI, migration guide)

- [ ] **¿Tests para ambas versiones?** (v1 + v2)

- [ ] **¿Backward-compatible fallback?** (si es posible)

---

## Anti-Patterns: Evitar

### ❌ Cambiar v1 sin warning

```java
// MAL: Cambio breaking sin aviso
GET /api/v1/tenants/{slug}
// Response v1: { data: { id, name } }
// Ahora: { id, name }  ← Clientes rompen
```

### ✅ Deprecated header + migration guide

```java
// BIEN
if (client.isLegacy()) {
  response.addHeader("Deprecation", "true");
  response.addHeader("Sunset", "Sun, 10 Oct 2026 00:00:00 GMT");
}
return TenantResponse.of(tenant);
```

---

### ❌ Mantener versiones antiguas indefinidamente

```
// MAL: v1, v2, v3, v4 todas soportadas
→ Pesadilla de mantenimiento
```

### ✅ Sunset policy clara

```
v1: Sunset en octubre 2026
v2: Sunset en octubre 2027
v3: Current, sin sunset aún
```

---

## Referencias

| Aspecto | Recurso |
|---|---|
| **RFC 7231** | HTTP Semantics (Deprecation header) |
| **REST API Versioning** | https://restfulapi.net/versioning/ |
| **Current Endpoints** | `docs/design/api/ENDPOINT_CATALOG.md` |
| **Migration Path** | (crear cuando haya v2) |
| **OpenAPI Docs** | `/v3/api-docs` (ambas versiones) |

---

**Última actualización:** 2026-04-10  
**Estado:** Completado para Sprint 2  
**Próxima versión:** v2.0 (roadmap futuro)

