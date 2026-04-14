# T-137 — Modelar restricción de reutilización de N contraseñas anteriores

**Estado:** ⬜ Registrada
**Horizonte:** corto plazo
**Módulos afectados:** `keygo-app`, `keygo-domain`, `keygo-supabase`, `keygo-api`, docs

## Requisito

Diseñar y luego permitir implementar una solución para restringir la reutilización de las
últimas **N** contraseñas usadas por un usuario cuando ocurra cualquiera de estos flujos:

1. cambio de contraseña autenticado
2. reseteo de contraseña
3. recuperación / “forgot password”

La solución debe definir cómo persistir el historial de contraseñas, cómo validar la nueva
contraseña contra ese historial y cómo parametrizar el valor de **N** sin romper los flujos
actuales de credenciales.

El objetivo es cubrir una política de seguridad reutilizable y consistente en todos los
entry points donde hoy se establece una nueva contraseña.

## Relaciones

| Artefacto relacionado | Tipo de relación | Descripción |
|---|---|---|
| `T-105` | `complementaria` | La expiración de contraseñas temporales y la no reutilización forman parte de la misma política de lifecycle de credenciales. |

## Decisiones a tomar antes de activar

Antes de planificar la implementación, resolver:

1. Si el historial se almacenará como hashes dedicados en tabla separada o dentro del agregado
   de usuario con otra estrategia.
2. Si la política aplica igual para `PlatformUser` y `TenantUser`, o si requiere variantes.
3. Cuántas contraseñas anteriores deben bloquearse y cómo se configurará ese valor.
4. Si la validación debe considerar solo contraseñas “efectivas” previas o también
   contraseñas temporales/provisionales.
5. Qué respuesta contractual debe recibir la UI cuando la nueva contraseña coincide con una
   de las últimas N usadas.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
