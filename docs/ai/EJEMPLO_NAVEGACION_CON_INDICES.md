# Ejemplos de Navegación — Sistema de Índices

**Propósito:** Demostrar cómo IAs y humanos navegan la documentación con índices vs sin índices.

---

## Caso 1: Buscar Lección sobre Validación de Dominio

### ❌ HOY (Sin índices)

```
IA: "¿Hay lección sobre validación de dominio?"

1. Abrir: docs/ai/lecciones.md (30 KB)
2. Grep: "validación"
3. Resultado: 3 menciones pero sin tabla de contenidos
4. Leer TODO el archivo para encontrar la sección relevante
5. Encontrada en línea 180 de 320

Tiempo total: 5-7 minutos
Contexto innecesario: 27 KB
```

### ✅ DESPUÉS (Con índices)

```
IA: "¿Hay lección sobre validación de dominio?"

1. Abrir: docs/ai/lecciones/README.md (2 KB)
   ```
   | Tema | Entradas | Link |
   |---|---|---|
   | Validación | 6 | [`por-tema/validacion.md`](#) |
   | Multi-tenancy | 8 | [`por-tema/multi-tenancy.md`](#) |
   | JPA | 5 | [`por-tema/jpa.md`](#) |
   ```

2. Click en "Validación" → [`docs/ai/lecciones/por-tema/validacion.md`]

3. Abrir: docs/ai/lecciones/por-tema/validacion.md (4 KB)
   ```
   | Fecha | Síntoma | Link |
   |---|---|---|
   | 2026-04-09 | Dónde va validación de dominio | [L-001](#l-001) |
   | 2026-04-05 | @Valid en DTO sin mensaje | [L-002](#l-002) |
   ```

4. Click en "Dónde va validación de dominio" → L-001

5. **Leer solo 200 líneas relevantes**

Tiempo total: 1-2 minutos
Contexto innecesario: 0 KB
```

**Ahorro:** 4-5 minutos + 26 KB de lectura innecesaria.

---

## Caso 2: Entender Catálogo de Errores y ResponseCode

### ❌ HOY (Sin índices)

```
IA: "Necesito entender qué ResponseCode usar para un endpoint"

1. Leer: docs/design/API_SURFACE.md (no existe, buscar alternativa)
2. Leer: docs/api/BOOTSTRAP_FILTER.md (no es)
3. Leer: docs/design/ARCHITECTURE.md (menciona códigos pero no detalle)
4. Grep: "ResponseCode" en código
5. Leer: keygo-api/ResponseCode.java (200 líneas sin documentación)
6. Reverse-engineer desde controllers

Tiempo total: 20-30 minutos
Frustración: Alta (información está, pero dispersa)
```

### ✅ DESPUÉS (Con índices)

```
IA: "Necesito entender qué ResponseCode usar"

1. Abrir: docs/design/README.md (1 KB)
   ```
   | Área | Docs | Objetivo |
   |---|---|---|
   | [API](api/) | ERROR_CATALOG, ENDPOINTS, VERSIONING | Contratos HTTP |
   ```

2. Click en "API" → docs/design/api/README.md (2 KB)
   ```
   | Doc | Descripción |
   |---|---|
   | ERROR_CATALOG.md | Qué ResponseCode usar, matriz de códigos, ejemplos OpenAPI |
   | ENDPOINTS.md | Catálogo por dominio, request/response examples |
   | API_VERSIONING.md | Política de breaking changes, deprecation |
   ```

3. Click en "ERROR_CATALOG.md"

4. Abrir: docs/design/api/ERROR_CATALOG.md
   ```
   # Catálogo de Errores
   
   | Code | HTTP | Contexto | Cuándo |
   |---|---|---|---|
   | INVALID_INPUT | 400 | Validación | Params inválidos |
   | INVALID_CREDENTIALS | 401 | Login | User/pass incorrecto |
   | ACCESS_DENIED | 403 | Autorización | Sin permisos |
   ```

5. **Encontrado directamente en 2 minutos**

Tiempo total: 2-3 minutos
Claridad: Alta (documento específico, tabla de referencia rápida)
```

**Ahorro:** 17-27 minutos + frustración eliminada.

---

## Caso 3: Encontrar Plan de Trabajo para Feature X

### ❌ HOY (Sin índices)

```
Equipo: "¿Hay un plan para refactor de documentación?"

1. Buscar en docs/ por "plan" → muchos archivos
2. ¿Es docs/PLAN_DOCUMENTACION_COMPLETA.md? (archivado)
3. ¿Es docs/design/IMPLEMENTATION_PLAN.md? (no es)
4. Grep: "plan" en archivos
5. Finalmente encuentra: docs/PLAN_DOCUMENTACION_2026_REVISION.md

Tiempo: 10-15 minutos
Confianza: Baja ("¿es este el actual?")
```

### ✅ DESPUÉS (Con índices)

```
Equipo: "¿Hay un plan para refactor de documentación?"

1. Abrir: docs/plans/README.md (1 KB)
   ```
   # Planes de Mejora
   
   | Plan | Objetivo | Estado | Sprints |
   |---|---|---|---|
   | [Documentación 2026](documentacion-2026/) | Onboarding 2h, RFCs consolidadas | 🟡 En ejecución | 3 |
   | [Plan X](plan-x/) | ... | 🔲 Pendiente | ... |
   ```

2. Click en "Documentación 2026" → docs/plans/documentacion-2026/README.md

3. Abrir: docs/plans/documentacion-2026/README.md (3 KB)
   ```
   # Plan Documentación 2026
   
   | Sección | Descripción | Link |
   |---|---|---|
   | Resumen | 13 deficiencias, 3 sprints, 15 documentos | [...] |
   | Sprint 1 | ERROR_CATALOG, DEBUGGING, PATTERNS, etc. | [...] |
   | Sprint 2 | RFC closure, OAuth2, Runbook, etc. | [...] |
   | Sprint 3 | Testing, Observability, Frontend | [...] |
   | Implementación | Pasos concretos por documento | [...] |
   ```

4. **Encontrado el documento correcto en 1 minuto**
5. **Sabe exactamente de qué trata**
6. **Puede navegar directamente a la sección que le interesa**

Tiempo: 1-2 minutos
Confianza: Alta (estructura clara, estado visible)
```

**Ahorro:** 9-13 minutos + confianza garantizada.

---

## Caso 4: Buscar Patrón de Autorización con @PreAuthorize

### ❌ HOY (Sin índices)

```
Dev: "¿Cómo se usa @PreAuthorize para validar ADMIN_TENANT?"

1. Buscar en docs/ por "PreAuthorize"
2. ¿docs/api/BOOTSTRAP_FILTER.md? (sólo rutas públicas)
3. ¿docs/design/ARCHITECTURE.md? (menciona pero no detalle)
4. Leer código en controllers...
5. Copiar patrón de otro controller similar

Tiempo: 15-20 minutos
Patrón encontrado: Incorrecto (copied from old code)
```

### ✅ DESPUÉS (Con índices)

```
Dev: "¿Cómo se usa @PreAuthorize para validar ADMIN_TENANT?"

1. Abrir: docs/design/README.md → [Patterns]

2. Click en "Patterns" → docs/design/patterns/README.md (2 KB)
   ```
   | Patrón | Descripción | Link |
   |---|---|---|
   | AUTHORIZATION_PATTERNS | @PreAuthorize, tenant match, RBAC matrix | [...] |
   | VALIDATION_STRATEGY | Bean Validation vs domain logic | [...] |
   | ERROR_HANDLING | Cómo mapear excepciones a ResponseCode | [...] |
   ```

3. Click en "AUTHORIZATION_PATTERNS"

4. Abrir: docs/design/patterns/AUTHORIZATION_PATTERNS.md
   ```
   # Patrones de Autorización
   
   ## @PreAuthorize para ADMIN_TENANT
   
   ```java
   @PreAuthorize("hasRole('ADMIN_TENANT') and #tenantSlug == authentication.principal.claims['tenant_slug']")
   public ResponseEntity<T> updateTenant(@PathVariable String tenantSlug) { ... }
   ```
   
   ### Matriz de RBAC
   | Rol | Endpoint | Validación |
   |---|---|---|
   | ADMIN | /api/v1/admin/* | Solo admin, sin tenant |
   | ADMIN_TENANT | /api/v1/tenants/{slug}/* | Admin del tenant específico |
   | USER | /account/* | Solo datos del usuario |
   ```

5. **Patrón correcto encontrado en 2 minutos**

Tiempo: 2-3 minutos
Patrón encontrado: Correcto (documentado, tested)
```

**Ahorro:** 12-17 minutos + garantía de corrección.

---

## Caso 5: Leer Lecciones Aprendidas de Feature T-111 (RBAC)

### ❌ HOY (Sin índices)

```
IA: "¿Hay lecciones sobre T-111 RBAC?"

1. Abrir: docs/ai/lecciones.md (30 KB)
2. Grep: "T-111\|RBAC"
3. Encontrado en línea 150
4. Leer TODO el archivo para contexto
5. Extraer 3 entradas sobre T-111

Tiempo: 7-10 minutos
Contexto leído: 28 KB de otras lecciones
```

### ✅ DESPUÉS (Con índices)

```
IA: "¿Hay lecciones sobre T-111 RBAC?"

1. Abrir: docs/ai/lecciones/README.md (2 KB)
   ```
   | Búsqueda | Descripción |
   |---|---|
   | Por Feature | T-111 (RBAC) — 5 entradas |
   | Por Tema | Autorización — 8 entradas |
   | Por Fecha | Abril 2026 — 15 entradas |
   ```

2. Click en "Por Feature → T-111"

3. Abrir: docs/ai/lecciones/por-feature/T-111-rbac.md (3 KB)
   ```
   # Lecciones — T-111 RBAC
   
   | Fecha | Síntoma | Link |
   |---|---|---|
   | 2026-04-07 | Soft-delete con índice parcial | [L-001](#) |
   | 2026-04-07 | FK hacia tenant_users, no users | [L-002](#) |
   | 2026-04-07 | Convención: PlatformRole lowercase, TenantRole UPPERCASE | [L-003](#) |
   ```

4. **Todas las lecciones de T-111 juntas en 2 KB**
5. **Leer solo lo relevante**

Tiempo: 2-3 minutos
Contexto leído: 3 KB (only relevant)
```

**Ahorro:** 5-7 minutos + 27 KB de lectura innecesaria.

---

## Resumen: Beneficios por Tipo de Búsqueda

| Búsqueda | Tiempo HOY | Tiempo DESPUÉS | Ahorro | Mejora |
|---|---|---|---|---|
| Lección por tema | 5-7 min | 1-2 min | 4-5 min | 80% |
| Catálogo de codes | 20-30 min | 2-3 min | 17-27 min | 85% |
| Plan específico | 10-15 min | 1-2 min | 9-13 min | 90% |
| Patrón de código | 15-20 min | 2-3 min | 12-17 min | 85% |
| Lección por feature | 7-10 min | 2-3 min | 5-7 min | 70% |
| **Promedio** | **11-16 min** | **2-3 min** | **9-13 min** | **78%** |

---

## Conclusión

Con índices navegables, **la búsqueda de información en documentación es 78% más rápida** y con 0% de contexto innecesario.

**Costo:** 8 horas de reorganización (Fase 0)  
**Beneficio:** Ahorro permanente en cada búsqueda futura + mejor onboarding  

**ROI:** Después de ~30-50 búsquedas, se recuperan las 8 horas.

---

**Próximo paso:** Implementar Fase 0 (crear estructura + README índices) en paralelo con Sprint 1.
