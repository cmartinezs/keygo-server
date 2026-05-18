# 📋 BACK-008: Platform account /access endpoint

> **Status:** DEEPENING
> [← active/README.md](../README.md) | [← planning/README.md](../../README.md)

---

| Campo | Valor |
|---|---|
| ID | 008 |
| Prefijo | BACK |
| Prioridad | P1 / Bloqueante para piloto — frontend recibe 403 |
| Área | Backend (`keygo-server`) |
| Estado | DEEPENING |

Agrega `GET /api/v1/platform/account/access`: dado el `sub` del token de plataforma, retorna todos los tenants donde el usuario tiene membresías y las apps a las que accede en cada uno, con sus roles. Reemplaza la necesidad de usar los endpoints tenant-scoped (`/tenants/{slug}/memberships` y `/tenants/{slug}/apps`) con un token de plataforma.

---

## Scopes

| # | Scope | Spec origen | Depende de | Estado |
|---|-------|-------------|-----------|--------|
| 01 | [GET /platform/account/access](02-deepening/scope-01-platform-account-access.md) | Bug reportado en piloto (403 en tenant endpoints con token de plataforma) | — | PENDING |

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
