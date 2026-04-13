# T-129 — Consolidar lógica de generación en Domain Services

**Estado:** 🔲 PENDING
**Horizonte:** corto plazo
**Módulos afectados:** `keygo-domain`, `keygo-app`

## Requisito

La lógica de generación de valores está dispersa en ~15 use cases. Debe vivir en
`keygo-domain` como domain services puros. Cinco factories a crear:

| Factory | Responsabilidad |
|---|---|
| `CredentialFactory` | Passwords, secrets, hashing |
| `VerificationCodeFactory` | Códigos de 6 dígitos |
| `TokenIdentifierFactory` | JTI, refresh tokens opacos |
| `InvoiceNumberGenerator` | Números de factura |
| `UsernameFactory` | Generación + resolución de colisiones (ver T-128) |

**Principio:** use cases orquestan, el dominio genera.

## Pasos de Implementación

| # | Acción | Estado |
|---|---|---|
| 1 | Crear `CredentialFactory` en `keygo-domain` | PENDING |
| 2 | Crear `VerificationCodeFactory` en `keygo-domain` | PENDING |
| 3 | Crear `TokenIdentifierFactory` en `keygo-domain` | PENDING |
| 4 | Crear `InvoiceNumberGenerator` en `keygo-domain` | PENDING |
| 5 | Crear `UsernameFactory` con resolución de colisiones (depende de T-128) | PENDING |
| 6 | Refactorizar use cases para delegar generación a los factories | PENDING |
| 7 | Actualizar tests afectados | PENDING |

## Verificación

- Ningún use case genera valores directamente; delega a factories del dominio.
- `keygo-domain` no tiene dependencias de Spring.
- Tests unitarios de cada factory con casos de borde.
