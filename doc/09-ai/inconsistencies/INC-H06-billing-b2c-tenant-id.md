# INC-H06 — Flujo B2C billing crea TenantUser sin tenant_id (violación NOT NULL)

**Categoría:** datos
**Criticidad:** 🔴 Crítica
**Estado:** ✅ Resuelta
**Detectada:** 2026-03-30
**Resuelta:** 2026-03-30
**Tarea relacionada:** —

## Descripción

El flujo de registro directo B2C (consumidor sin tenant previo) del modelo billing v1
creaba un `TenantUser` sin asignar `tenant_id`, violando la constraint `NOT NULL` de la
columna. El error ocurría en runtime al intentar persistir el registro.

## Esperado vs. Real

| | Esperado | Real |
|---|---|---|
| Creación de `TenantUser` en flujo B2C | `tenant_id` asignado al tenant implícito o recién creado | `tenant_id = null` → violación NOT NULL en DB |
| Secuencia del flujo | Crear tenant → crear TenantUser con `tenant_id` | TenantUser creado antes de que existiera el tenant asociado |

## Corrección

Flujo B2C corregido para garantizar que el tenant se crea y persiste antes de
crear el `TenantUser`, asignando el `tenant_id` generado. Ajustes en el use case
de registro B2C y en los tests de integración correspondientes.
