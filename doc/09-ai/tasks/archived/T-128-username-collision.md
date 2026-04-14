# T-128 — Colisión de username generado en contratos

**Estado:** ⬛ Archivada — absorbida por [T-150](../planned/T-150-platform-user-username-removal.md)
**Horizonte:** corto plazo
**Módulos afectados:** `keygo-app`, `keygo-domain`

## Requisito

`AppContract.generateUsername()` no verifica unicidad en DB. Si dos contractors generan
el mismo username (ej: "Carlos Martínez" y "Christian Martínez" → `cmartinez`),
`VerifyContractEmailUseCase` falla con UNIQUE constraint.

**Solución:** verificar unicidad con `PlatformUserRepositoryPort.existsByUsername()` y
agregar sufijo numérico incremental. Afecta también el preview del username en emails
pre-registro.

## Pasos de Implementación

| # | Acción | Estado |
|---|---|---|
| 1 | Agregar `existsByUsername(Username)` en `PlatformUserRepositoryPort` | PENDING |
| 2 | Implementar lógica de sufijo incremental en `AppContract.generateUsername()` o use case | PENDING |
| 3 | Actualizar emails pre-registro para reflejar username final resuelto | PENDING |
| 4 | Agregar test unitario de colisión | PENDING |

## Verificación

- Dos contractors con nombre similar generan usernames distintos.
- El email pre-registro muestra el username que efectivamente se asignará.
- No hay fallo con UNIQUE constraint en `VerifyContractEmailUseCase`.
