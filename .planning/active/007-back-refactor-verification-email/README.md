# 📋 BACK-007: Refactor envío de email de verificación

> **Status:** DEEPENING
> [← active/README.md](../README.md) | [← planning/README.md](../../README.md)

---

| Campo | Valor |
|---|---|
| ID | 007 |
| Prefijo | BACK |
| Prioridad | P2 / Deuda técnica — sin urgencia de negocio |
| Área | Backend (`keygo-server`) |
| Estado | DEEPENING |

Extraer la lógica de ensamblado y envío del email de verificación en un componente compartido, eliminando la duplicación entre `RegisterTenantUserUseCase` y `ResendVerificationEmailUseCase`.

---

## Scopes

| # | Scope | Spec origen | Depende de | Estado |
|---|-------|-------------|-----------|--------|
| 01 | [Extracción de SendVerificationEmailUseCase](02-deepening/scope-01-shared-email-sender.md) | Detección durante fix del resend (2026-05-17) | — | PENDING |

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
