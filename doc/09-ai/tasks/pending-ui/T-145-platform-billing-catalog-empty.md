# T-145 — Catálogo de billing de plataforma retorna `data: []`

**Estado:** 🧩 Pendiente integración UI  
**Módulos afectados:** `keygo-supabase`, docs  
**Tipo de registro:** retroactivo

---

## Problema / Requisito

La UI reportó que `GET /api/v1/platform/billing/catalog` respondía `200 OK` con
`PLATFORM_PLAN_CATALOG_RETRIEVED`, pero con `data: []`.

El problema no estaba en el controller ni en el use case inmediato, sino en una deriva entre la
capa Java y el baseline Flyway activo:

- el código de platform billing esperaba planes con `client_app_id = NULL`
- el baseline vigente no permitía eso en `app_plans`
- tampoco existían seeds para el catálogo público de plataforma

## Relaciones

- **derivada de:** `doc/02-functional/frontend/feedback/UI-003-platform-billing-catalog-empty.md`

## Solución aplicada

Se corrigió el baseline para soportar explícitamente el catálogo de plataforma:

- nueva migración `V20__platform_plan_catalog.sql`
- `ALTER TABLE app_plans ALTER COLUMN client_app_id DROP NOT NULL`
- índice único filtrado para códigos de planes de plataforma
- seed de planes `FREE`, `PERSONAL`, `TEAM`, `BUSINESS`, `FLEX`, `ENTERPRISE`
- seed de `app_plan_versions`, `app_plan_billing_options` y `app_plan_entitlements`
- documentación alineada (`migrations.md`, `database-schema.md`, `data-model.md`, `entity-relationships.md`)

### Contrato impactado

No cambió la firma HTTP del endpoint:

- `GET /api/v1/platform/billing/catalog`
- `200 OK`
- `BaseResponse<List<AppPlanData>>`
- `success.code = PLATFORM_PLAN_CATALOG_RETRIEVED`

El cambio es de **disponibilidad y consistencia de datos**: el endpoint deja de responder catálogo
vacío cuando el baseline se aplica correctamente.

## Pasos aplicados

| # | Acción | Archivo | Estado |
|---|---|---|---|
| 1 | Confirmar causa raíz entre endpoint, use case y persistencia | `keygo-api/.../PlatformBillingController.java`, `keygo-app/.../GetPlatformPlanCatalogUseCase.java`, `keygo-supabase/.../AppPlanRepositoryAdapter.java` | APPLIED |
| 2 | Verificar deriva con baseline Flyway y seeds | `V13__billing_catalog.sql`, `V17__seed_billing.sql`, `doc/08-reference/data/migrations.md` | APPLIED |
| 3 | Crear migración para habilitar planes de plataforma | `keygo-supabase/.../V20__platform_plan_catalog.sql` | APPLIED |
| 4 | Sembrar catálogo público de plataforma con versiones, billing options y entitlements | `keygo-supabase/.../V20__platform_plan_catalog.sql` | APPLIED |
| 5 | Alinear documentación del baseline y modelo de datos | `doc/08-reference/data/...`, `doc/03-architecture/database-schema.md` | APPLIED |
| 6 | Validar cobertura focalizada del catálogo/controlador de plataforma | `GetPlatformPlanCatalogUseCaseTest`, `GetPlatformPlanUseCaseTest`, `PlatformBillingControllerTest` | APPLIED |

---

## Verificación

```bash
./mvnw --% -pl keygo-app,keygo-api -am test -Dtest=GetPlatformPlanCatalogUseCaseTest,GetPlatformPlanUseCaseTest,PlatformBillingControllerTest -Dsurefire.failIfNoSpecifiedTests=false
```

## Nota de trazabilidad

Este documento se registra **retroactivamente** porque primero se aplicó la corrección técnica y
después se observó que faltaba seguir el flujo de trazabilidad desde feedback UI → tarea técnica.
La referencia canónica del requerimiento de UI queda preservada en `UI-003`.

## Historial de transiciones

### 2026-04-13 — 🔵 En desarrollo

- Se identificó que el endpoint estaba sano a nivel HTTP, pero sin datos por deriva entre código y
  baseline Flyway.
- Se definió como corrección mínima viable crear una nueva migración de catálogo de plataforma en
  vez de alterar semánticamente el controller.

### 2026-04-13 — 🔄 En revisión

- Se agregó `V20__platform_plan_catalog.sql` y se alineó la documentación del baseline.
- La validación focalizada del catálogo/controlador pasó correctamente.

### 2026-04-13 — 🧩 Pendiente integración UI

- El backend ya quedó preparado para que `GET /platform/billing/catalog` devuelva planes de
  plataforma.
- Falta reaplicar migraciones/reiniciar el backend local y confirmar consumo desde UI.
