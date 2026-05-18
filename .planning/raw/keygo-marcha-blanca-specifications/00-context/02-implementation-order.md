# Orden de implementación recomendado

| Campo | Valor |
|---|---|
| Tipo | Delivery plan |
| Área | Frontend y Backend |
| Prioridad | Obligatoria para ejecución ordenada |

## Sprint corto P0

Ejecutar en este orden:

1. `BE-P0-001` — Unificar nomenclatura de roles administrativos.
2. `FE-P0-003` — Alinear route guards y permisos de UI a la nomenclatura definitiva.
3. `BE-P0-004` — Validar contrato backend para OAuth config de apps.
4. `FE-P0-002` — Exponer redirect URIs y scopes en creación/edición de app.
5. `FE-P0-001` — Mostrar `client_secret` una sola vez al crear app.
6. `BE-P0-002` — Corregir DTO de memberships con roles legibles y `created_at` real.
7. `FE-P0-004` — Consumir memberships con roles legibles.
8. `FE-P0-005` — Agregar UI básica para roles por app.
9. `BE-P0-003` — Confirmar claims mínimos de token.
10. Ejecutar `QA-001`.

## Sprint P1

Ejecutar según necesidad del piloto:

1. `BE-P1-001` y `FE-P1-001` — Contrato suspend/activate.
2. `BE-P1-002` y `FE-P1-002` — Sesiones admin por usuario o retiro temporal de UI.
3. `BE-P1-003` y `FE-P1-004` — Búsqueda remota/autocomplete.
4. `BE-P1-004` y `FE-P1-005` — Bandeja de pendientes si se mantiene `PENDING`.
5. `BE-P1-005` y `FE-P1-006` — Política de acceso/registro por app.
6. `BE-P1-006` y `FE-P1-007` — Logout/revocación OAuth.
7. `FE-P1-008` — Usuario detalle con apps asignadas.
8. `FE-P1-009` — Pantalla sin acceso a app.
9. Ejecutar `QA-002`.

## Criterio de priorización

Todo cambio que evite pérdida de secretos, errores 403 por roles inconsistentes, memberships ilegibles o tokens sin claims funcionales debe ir antes de mejoras de experiencia secundaria.
