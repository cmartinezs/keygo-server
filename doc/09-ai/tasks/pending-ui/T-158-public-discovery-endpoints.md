# T-158 — Public discovery endpoints para tenant/app selection en registro

**Estado:** 🧩 Pendiente integración UI  
**Prioridad:** 🔴 Alta  
**Bloqueante para:** T-154 (completar UX de registro)  
**Esfuerzo estimado:** 🕐 S (2-3 endpoints públicos)

---

## Problema / Requisito

En T-154 se expone flujo de self-registro con 3 endpoints públicos (POST /register, POST /verify-email, POST /resend-verification). Sin embargo, **antes de que el usuario pueda acceder a estos endpoints, necesita saber**:

1. ¿Qué tenants existen? (nombre, slug)
2. ¿Qué apps tiene cada tenant? (de cuáles puede registrarse)

**Situación actual:** No existen endpoints públicos para descubrir esta información. Los endpoints existentes están protegidos con autenticación JWT.

**Consecuencia:** La UI no puede armar la UX de "seleccionar empresa" antes del formulario de registro. El usuario no sabe a qué aplicación se va a registrar.

---


## Especificación

### ⚠️ Restricción crítica (aplica a ambos endpoints)

**Para que un tenant O app aparezca en los endpoints públicos de descubrimiento, DEBE cumplir:**
1. `status = ACTIVE`
2. `registration_policy != INVITE_ONLY` ← **Aparecerá en listados públicos solo si permite registro**

Esto significa:
- Un tenant con `registration_policy = INVITE_ONLY` → **no aparece en `/tenants/public`**
- Una app con `registration_policy = INVITE_ONLY` → **no aparece en `/tenants/{slug}/apps/public`**

Rationale: El endpoint "public" es para descubrimiento de registro abierto. Los INVITE_ONLY se descubren por invitación directa, no por listado.

---

### Endpoint 1: Listar tenants públicos

```
GET /api/v1/tenants/public
Authorization: ninguna (público)
```

**Response 200:**
```json
{
  "code": "TENANT_LIST_RETRIEVED",
  "data": {
    "content": [
      {
        "id": "uuid1",
        "name": "Acme Corp",
        "slug": "acme-corp",
        "display_name": "Acme Corporation",
        "description": "Software vendor"
      },
      {
        "id": "uuid2",
        "name": "TechStart",
        "slug": "techstart",
        "display_name": null,
        "description": null
      }
    ],
    "page": 0,
    "size": 50,
    "total_elements": 2,
    "total_pages": 1,
    "is_last": true
  }
}
```

**Notas:**
- Paginación opcional: `?page=0&size=50`
- Buscar por nombre: `?name_like=acme`
- Ordenar: `?sort=name&order=ASC`
- Consultar sección "Restricción crítica" arriba para filtros de status y registration_policy

---

### Endpoint 2: Listar apps públicas de un tenant

```
GET /api/v1/tenants/{tenantSlug}/apps/public
Authorization: ninguna (público)
```

**Response 200:**
```json
{
  "code": "CLIENT_APP_LIST_RETRIEVED",
  "data": {
    "content": [
      {
        "id": "uuid-app1",
        "client_id": "app_prod_001",
        "name": "Acme Production App",
        "description": "Main customer-facing app",
        "type": "PUBLIC",
        "registration_policy": "OPEN_AUTO_ACTIVE",
        "is_active": true
      },
      {
        "id": "uuid-app2",
        "client_id": "app_staging_001",
        "name": "Acme Staging",
        "description": "Development and testing",
        "type": "PUBLIC",
        "registration_policy": "OPEN_AUTO_PENDING",
        "is_active": true
      }
    ],
    "page": 0,
    "size": 50,
    "total_elements": 2,
    "total_pages": 1,
    "is_last": true
  }
}
```

**Filtros implícitos:**
- Solo apps con `status = ACTIVE`
- Solo apps con `type = PUBLIC` (excluir CONFIDENTIAL — no son para self-registration)
- **Solo apps con `registration_policy != INVITE_ONLY`** (excluir si no permiten registro abierto)
- Consultar sección "Restricción crítica" arriba para más detalles

**Error 404:**
```json
{
  "code": "RESOURCE_NOT_FOUND",
  "data": {
    "message": "Tenant not found or is not active"
  }
}
```

---

## Análisis técnico

### Código existente reutilizable

| Componente | Ubicación | Reutilizable |
|---|---|---|
| `ListTenantsUseCase` | `keygo-app/tenant/usecase/` | ✅ Reutilizar con filtros forzados desde el controller |
| `ListClientAppsUseCase` | `keygo-app/clientapp/usecase/` | ✅ Reutilizar con filtros forzados desde el controller |
| `TenantFilter` | `keygo-app/tenant/filter/` | ✅ Sin cambios; pasar `status=ACTIVE` como filtro forzado |
| `ClientAppFilter` | `keygo-app/clientapp/filter/` | ⚠️ Falta soporte para filtrar por `type` y `registrationPolicy` |
| `TenantRepositoryPort.findAll()` | `keygo-app/tenant/port/` | ✅ Ya soporta `TenantFilter` con paginación |
| `ClientAppRepositoryPort.findAllPaged()` | `keygo-app/clientapp/port/` | ✅ Ya soporta `ClientAppFilter` con paginación |
| `RegistrationPolicy` enum | `keygo-domain/clientapp/model/` | ✅ Ya existe con los 4 valores necesarios |
| `PagedData<T>` + `BaseResponse<T>` | `keygo-api/shared/` | ✅ Sin cambios |
| `ResponseCode.TENANT_LIST_RETRIEVED` | `keygo-api/shared/` | ✅ Ya existe |
| `ResponseCode.CLIENT_APP_LIST_RETRIEVED` | `keygo-api/shared/` | ✅ Ya existe |

### Gaps que requieren cambios

1. **`ClientAppFilter`** no tiene campos `type` (PUBLIC/CONFIDENTIAL) ni `excludeRegistrationPolicy`. Hay que extenderlo para soportar el filtro implícito `type=PUBLIC` y `registrationPolicy != INVITE_ONLY`.

2. **`ClientAppRepositoryPort.findAllPaged()`** y su implementación en `keygo-supabase` deben aplicar los nuevos criterios del filter.

3. **DTOs reducidos**: `TenantData` y `ClientAppData` actuales exponen campos internos no apropiados para endpoints públicos (`status`, `redirectUris`, `grants`, `scopes`, `createdAt`, `updatedAt`). Se crean `TenantPublicData` y `ClientAppPublicData` con solo los campos del contrato público.

4. **Nuevo controlador `PublicDiscoveryController`**: Los controllers existentes están protegidos con `@PreAuthorize`. El nuevo no lleva ninguna anotación de seguridad — la `SecurityConfig` ya tiene `anyRequest().permitAll()`, por lo que no hay trabajo extra en la capa de security.

### Decisión: ¿nuevos use cases o reutilizar los existentes?

Los use cases `ListTenantsUseCase` y `ListClientAppsUseCase` son genéricos y sin lógica de acceso. **Se reutilizan directamente** — los filtros forzados (`status=ACTIVE`, `type=PUBLIC`, `registrationPolicy != INVITE_ONLY`) se aplican en el controller al construir el `Filter`. Esto evita duplicar lógica y mantiene los use cases cohesivos.

### Decisión: ¿nuevo controller o extender `RegistrationController`?

Se crea **`PublicDiscoveryController`** separado bajo `/api/v1/tenants`. Los endpoints de discovery son semánticamente distintos del flujo de registro; agruparlos en `RegistrationController` rompería la cohesión. El path `/tenants/public` no colisiona con `/{slug}` en `PlatformTenantController` porque Spring resuelve literales antes que path variables.

---

## Implementación

| # | Paso | Módulo | Estado |
|---|------|--------|--------|
| 1 | Agregar campos `type` y `excludeRegistrationPolicy` a `ClientAppFilter` | keygo-app | APPLIED |
| 2 | `ClientAppRepositoryPort` — sin cambio de firma; los nuevos campos del filter son consumidos por el adapter | keygo-app | APPLIED |
| 3 | Actualizar `ClientAppRepositoryAdapter` (supabase) para aplicar `type` y `excludeRegistrationPolicy` en la query JPA | keygo-supabase | APPLIED |
| 4 | Crear `TenantPublicData` (`id`, `name`, `slug`) en `keygo-api/discovery/response/` | keygo-api | APPLIED |
| 5 | Crear `ClientAppPublicData` (`id`, `client_id`, `name`, `description`, `type`, `registration_policy`, `is_active`) en `keygo-api/discovery/response/` | keygo-api | APPLIED |
| 6 | Crear `PublicDiscoveryController` en `keygo-api/discovery/controller/` — sin `@PreAuthorize`, filtros forzados en el método; verifica tenant activo antes de listar apps | keygo-api | APPLIED |
| 7 | Registrar beans en `ApplicationConfig` — no requiere cambios: los tres use cases ya están registrados y el controller es `@RestController` (auto-managed) | keygo-run | APPLIED |
| 8 | Actualizar Postman con 2 nuevos endpoints | postman/ | APPLIED |
| 9 | Tests unitarios de controller — 11 tests (filtros forzados, mapping, 404 en tenant suspendido/inexistente) | keygo-api | APPLIED |

---

## Impacto en T-154

**Cambios en feedback BE-008:**
- Agregar Pantalla 0 de descubrimiento (tenant/app selection)
- Actualizar flujo completo para UI para incluir estos endpoints

**Cambios en T-154 mismo:**
- Ninguno (T-154 ya está completado; T-158 es complementario)

---

## Consideraciones de seguridad

- ✅ Endpoints públicos — no exponen información sensible (solo tenant/app names, slugs, status)
- ✅ Filtros implícitos — solo ACTIVE tenants/apps con registration permitido
- ✅ Sin exposición de credenciales, secretos o datos de usuarios
- ⚠️ Potencial: rate limit en estos endpoints si escala

---

## Relaciones

| Artefacto | Tipo | Descripción |
|-----------|------|-------------|
| T-154 | habilitadora | T-158 completa la UX de T-154 |
| BE-008 | derivada de | Feedback que requerirá actualización |

---

## Guía de verificación

```bash
# Después de implementar
./mvnw clean package -DskipTests -pl keygo-api

# Tests
./mvnw test -pl keygo-api -Dtest="*PublicDiscovery*"

# Manual: sin Bearer token debe funcionar
curl -X GET http://localhost:8080/keygo-server/api/v1/tenants/public
curl -X GET http://localhost:8080/keygo-server/api/v1/tenants/my-tenant/apps/public
```

---

## Notas

- Esta tarea es **complementaria** a T-154, no bloqueante. T-154 se puede usar con URLs compartidas/directas
  (mailto links, invitations, etc.)
- Sin embargo, para UX completa de self-registro, T-158 es necesario antes de lanzar a UI

---

## Historial de transiciones

- 2026-04-15 → 🔲 Registrada (derivada de revisión T-154 + feedback BE-008)
- 2026-04-15 → 🟢 Aprobada (análisis técnico completado: gaps identificados, decisiones de diseño documentadas)
- 2026-04-15 → 🔵 En desarrollo (pasos 1–7 aplicados: ClientAppFilter extendido, adapter actualizado, DTOs y PublicDiscoveryController creados; pendiente Postman + tests)
- 2026-04-15 → 🧩 Pendiente integración UI (pasos 8–9 completados: folder "Public Discovery" en Postman, 11 tests unitarios — todos pasan)
