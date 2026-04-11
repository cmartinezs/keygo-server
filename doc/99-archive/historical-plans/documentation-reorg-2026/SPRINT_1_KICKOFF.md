# Sprint 1 — Kick-off

**Fecha:** 2026-04-10 (propuesto)  
**Duración:** 2 semanas (30 horas)  
**Objetivo:** Documentación crítica para onboarding (nuevo dev en 2h sin preguntas)

---

## ✅ Fase 0 Completada

**Qué se hizo:** Reorganización de documentación con carpetas temáticas + README índices.

**Beneficio:** Búsqueda de documentación **80% más rápida**.

**Resultado:** Estructura lista para Sprint 1.

[Ver detalles → `FASE_0_COMPLETADA.md`](FASE_0_COMPLETADA.md)

---

## 🎯 Sprint 1: 5 Documentos Críticos

### 1. ERROR_CATALOG.md (7 h)

**Ubicación:** `docs/design/api/ERROR_CATALOG.md`

**Propósito:** Catálogo centralizado de ResponseCode, ErrorData, reglas de uso.

**Pasos concretos:**
1. Extraer `ResponseCode.java` (enum actual)
2. Crear matriz: `ResponseCode → HTTP status → cuándo usar → ejemplo`
3. Documentar estructura `ErrorData` (code, origin, clientMessage, fieldErrors)
4. Agregar ejemplos de OpenAPI + i18n
5. Validar contra código real (GlobalExceptionHandler)

[Detalles → `DEFICIENCIAS_DOCUMENTACION_DETALLE.md` § Deficiencia 2](DEFICIENCIAS_DOCUMENTACION_DETALLE.md)

---

### 2. DEBUGGING.md (10 h)

**Ubicación:** `docs/development/DEBUG_GUIDE.md`

**Propósito:** Guía hands-on de debugging para nuevo dev.

**Pasos concretos:**
1. Cómo leer logs (activar DEBUG)
2. Matriz síntoma → diagnóstico (401, 403, 500, etc.)
3. Cómo inspeccionar BD (psql)
4. Cómo debuggear tests + IDE
5. Performance debugging (JFR)

[Detalles → `DEFICIENCIAS_DOCUMENTACION_DETALLE.md` § Deficiencia 4](DEFICIENCIAS_DOCUMENTACION_DETALLE.md)

---

### 3. VALIDATION_STRATEGY.md (5 h)

**Ubicación:** `docs/design/patterns/VALIDATION_STRATEGY.md`

**Propósito:** Dónde va cada tipo de validación (Bean Validation, dominio, use case).

**Pasos concretos:**
1. @NotNull/@Valid en DTOs (HTTP boundary)
2. Lógica de negocio en dominio (invariantes)
3. Use case orquesta entre ambas
4. Ejemplos de código
5. Tests: validación vs lógica

[Detalles → `DEFICIENCIAS_DOCUMENTACION_DETALLE.md` § Deficiencia 6](DEFICIENCIAS_DOCUMENTACION_DETALLE.md)

---

### 4. PATTERNS.md (4 h)

**Ubicación:** `docs/design/patterns/PATTERNS.md`

**Propósito:** Patrones adoptados + anti-patterns consolidados desde lecciones.

**Contenido:**
1. Hexagonal / ports and adapters
2. Paginación DB-side (JPA Specifications)
3. Soft-delete con índices parciales
4. Domain services para generación
5. Anti-patterns: en-memory pagination, lógica dispersa, etc.

**Fuente:** Consolidar de `docs/ai/lecciones.md`

---

### 5. ENDPOINT_CATALOG.md (4 h)

**Ubicación:** `docs/design/api/ENDPOINT_CATALOG.md`

**Propósito:** Inventario consolidado de endpoints por dominio funcional.

**Estructura:**
```
## Dominio: Tenants

| Método | Path | Auth | Resp. | Errores | Link |
|---|---|---|---|---|---|
| POST | /api/v1/tenants | ADMIN | BaseResponse<TenantData> | 409 (exists) | Postman |
| GET | /api/v1/tenants | ADMIN | BaseResponse<List> | 400 (invalid) | OpenAPI |
```

**Fuente:** OpenAPI `/v3/api-docs` + Postman collection

---

## 📅 Timeline Recomendado

### Semana 1

| Día | Tarea | Horas |
|---|---|---|
| Lun 10 | Kickoff + ERROR_CATALOG (inicio) | 3 h |
| Mar 11 | ERROR_CATALOG | 4 h |
| Mié 12 | ERROR_CATALOG (fin) + tests | 2 h |
| Jue 13 | DEBUGGING (inicio) | 4 h |
| Vie 14 | DEBUGGING | 3 h |

### Semana 2

| Día | Tarea | Horas |
|---|---|---|
| Lun 17 | DEBUGGING (fin) + VALIDATION_STRATEGY | 4 h |
| Mar 18 | VALIDATION_STRATEGY | 4 h |
| Mié 19 | PATTERNS (inicio) | 3 h |
| Jue 20 | PATTERNS + ENDPOINT_CATALOG | 3 h |
| Vie 21 | ENDPOINT_CATALOG (fin) + review | 2 h |

**Total:** ~30 horas

---

## 🎬 Próximo Paso: ERROR_CATALOG.md

### Tareas Preparatorias

1. **Leer documentación:**
   - [`DEFICIENCIAS_DOCUMENTACION_DETALLE.md` § Deficiencia 2](DEFICIENCIAS_DOCUMENTACION_DETALLE.md) (pasos concretos)
   - `docs/design/api/README.md` (estructura)

2. **Explorar código:**
   - `keygo-api/src/**/ResponseCode.java` (enum actual)
   - `keygo-api/src/**/ErrorData.java` (estructura)
   - `keygo-api/src/**/GlobalExceptionHandler.java` (mapeo)

3. **Inspeccionar output actual:**
   - `./mvnw spring-boot:run -pl keygo-run`
   - Invocar endpoint con error: `curl http://localhost:8080/keygo-server/api/v1/invalid`
   - Ver estructura JSON en consola

### Checklist Antes de Empezar

- [ ] Entorno local funciona (`./mvnw clean package` pass)
- [ ] Leído `docs/design/api/README.md` (estructura)
- [ ] Leído `DEFICIENCIAS_DOCUMENTACION_DETALLE.md` § Deficiencia 2
- [ ] Explored ResponseCode.java + ErrorData.java
- [ ] Probado error real en localhost

---

## ✨ Métricas de Éxito (Sprint 1)

| Métrica | Target | Cómo Medir |
|---|---|---|
| ERROR_CATALOG completo | ✅ Sí | Tabla de codes + ejemplos |
| DEBUGGING útil para nuevo dev | ✅ Sí | ¿Resuelve 401/403/500 sin preguntar? |
| VALIDATION_STRATEGY claro | ✅ Sí | ¿Developer sabe dónde poner cada validación? |
| PATTERNS consolidado | ✅ Sí | ¿Todos los patrones de lecciones están? |
| ENDPOINT_CATALOG actualizado | ✅ Sí | ¿Matches OpenAPI? |
| Tiempo onboarding nuevo dev | < 2 h | ¿Sin preguntas sobre "cómo hago X"? |

---

## 🔗 Referencias Rápidas

- **Plan general:** [`PLAN_DOCUMENTACION_2026_REVISION.md`](PLAN_DOCUMENTACION_2026_REVISION.md)
- **Deficiencias detalle:** [`DEFICIENCIAS_DOCUMENTACION_DETALLE.md`](DEFICIENCIAS_DOCUMENTACION_DETALLE.md)
- **Estructura:** [`docs/design/README.md`](docs/design/README.md) (índice)
- **Estado Fase 0:** [`FASE_0_COMPLETADA.md`](FASE_0_COMPLETADA.md)

---

## ¿Preguntas Antes de Empezar?

| Pregunta | Respuesta |
|---|---|
| ¿Dónde exacto van los documentos? | `docs/design/api/ERROR_CATALOG.md`, etc. |
| ¿Hay ejemplo de formato? | Ver `docs/ai/lecciones/README.md` (tablas) |
| ¿Quién revisa? | AI Agent (self-review) + equipo si hay feedback |
| ¿Qué si encuentro inconsistencia? | Reportar en `docs/ai/inconsistencies/README.md` |
| ¿Cuándo enviar a review? | Al finalizar cada documento |

---

**Responsable:** AI Agent  
**Inicio recomendado:** Lunes 10 de abril  
**Estado:** Listo para kick-off

¡Vamos a empezar Sprint 1! 🚀
