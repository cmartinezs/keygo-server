# 📋 BACK-006: Fix contrato resend-verification

> **Status:** DEEPENING
> [← active/README.md](../README.md) | [← planning/README.md](../../README.md)

---

| Campo | Valor |
|---|---|
| ID | 006 |
| Prefijo | BACK |
| Prioridad | P1 / Bug de contrato bloqueante para el flujo de self-registration |
| Área | Backend (`keygo-server`) |
| Estado | DEEPENING |

Ajusta el contrato de `POST /api/v1/tenants/{tenantSlug}/apps/{clientId}/resend-verification` para aceptar `registration_id` como identificador principal, eliminando la dependencia del campo `email` que el frontend no tiene disponible en el paso de verificación.

---

## Scopes

| # | Scope | Spec origen | Depende de | Estado |
|---|-------|-------------|-----------|--------|
| 01 | [Contrato resend-verification: registration_id como identificador](02-deepening/scope-01-resend-verification-contract.md) | Bug reportado en piloto | — | PENDING |

---

## Archivos

| Archivo | Descripción |
|---------|-------------|
| [00-initial.md](00-initial.md) | Intent y contexto |
| [01-expansion.md](01-expansion.md) | Scopes y dependencias |
| [02-deepening/](02-deepening/) | Detalle de tareas por scope |
| [TRACEABILITY.md](TRACEABILITY.md) | Matriz de trazabilidad |

---

> [← active/README.md](../README.md) | [← planning/README.md](../../README.md)
