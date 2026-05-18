# 🔍 DEEPENING: Scope 01 — Extracción de SendVerificationEmailUseCase

> **Status:** PENDING
> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)

**Spec origen:** Detección durante fix del resend (2026-05-17) | **Prioridad:** P2 / Deuda técnica

---

## Objective

Extraer el ensamblado y envío del email de verificación en un único componente reutilizable. Ambos flujos —registro inicial y reenvío on-demand— deben delegar en él, eliminando la duplicación del `Map` de parámetros y garantizando que cualquier cambio futuro al contrato del template (agregar, renombrar o eliminar un parámetro) se aplique en un solo lugar.

---

## Análisis del estado actual

### Código duplicado

| Punto de duplicación | Archivo | Parámetros enviados |
|---|---|---|
| Envío post-registro | `RegisterTenantUserUseCase.java:158` | `userUsername`, `userFirstName`, `userLastName`, `verificationCode`, `registration_id`, `client_id`, `expiresInMinutes` |
| Envío en reenvío | `ResendVerificationEmailUseCase.java:103` | Mismos 7 parámetros (corregido en fix 2026-05-17) |

### Raíz del problema

El `Map.of(...)` no tiene un contrato explícito. Cada use case lo construye de forma independiente. El bug corregido en el fix (`registration_id` y `client_id` ausentes en el reenvío) es evidencia directa de que la duplicación es frágil: un cambio en un sitio no se propaga al otro.

---

## Opciones de diseño

| Opción | Descripción | Pros | Contras |
|---|---|---|---|
| **A — Nuevo use case inyectable** | `SendVerificationEmailUseCase` en `keygo-app`; recibe `User`, `String verificationCode`, `String clientId` y envía | Consistente con el modelo hexagonal; testeable de forma aislada; explícita como dependencia | Nueva clase; cableado en `ApplicationConfig` |
| **B — Helper estático interno** | Método `package-private` o `static` en una clase utilitaria dentro de `keygo-app` | Mínimo overhead | No inyectable, no mockeable directamente; no alineado con el modelo de arquitectura del proyecto |
| **C — Default method en EmailNotificationPort** | Método `sendVerificationEmail(User, code, clientId)` en el puerto | El puerto sabe "cómo armar" el email | Viola el SRP del puerto: mezcla el "qué enviar" con el "cómo armar parámetros" |

**Decisión a tomar:** Opción A (nuevo use case) es la más alineada con la arquitectura hexagonal vigente y con el patrón establecido por `CreatePlatformUserUseCase` + `AssignPlatformRoleUseCase`. Confirmar antes de implementar.

---

## Tasks

| # | Tarea | Workflow | Estado | Output |
|---|-------|----------|--------|--------|
| 1 | Decisión de diseño: confirmar opción A (nuevo use case) u otra | — | PENDING | Decisión registrada en este scope |
| 2 | Crear `SendVerificationEmailUseCase` con firma `execute(User, String verificationCode, String clientId)` | GENERATE-DOCUMENT | PENDING | `SendVerificationEmailUseCase.java` |
| 3 | Refactorizar `RegisterTenantUserUseCase`: inyectar y delegar en el nuevo use case | GENERATE-DOCUMENT | PENDING | `RegisterTenantUserUseCase.java` actualizado |
| 4 | Refactorizar `ResendVerificationEmailUseCase`: inyectar y delegar en el nuevo use case | GENERATE-DOCUMENT | PENDING | `ResendVerificationEmailUseCase.java` actualizado |
| 5 | Actualizar `ApplicationConfig`: cablear `SendVerificationEmailUseCase` e inyectarlo en los dos use cases | GENERATE-DOCUMENT | PENDING | `ApplicationConfig.java` actualizado |
| 6 | Tests de `SendVerificationEmailUseCase`: cubrir parámetros del email, null safety de firstName/lastName | GENERATE-DOCUMENT | PENDING | `SendVerificationEmailUseCaseTest.java` |
| 7 | Adaptar `RegisterTenantUserUseCaseTest` y `ResendVerificationEmailUseCaseTest`: mockear el nuevo use case en lugar del port directo | GENERATE-DOCUMENT | PENDING | Tests actualizados |
| 8 | Actualizar `TRACEABILITY.md` | UPDATE-TRACEABILITY | PENDING | `TRACEABILITY.md` |

---

## Done Criteria

- [ ] Un único lugar ensambla los parámetros del email de verificación.
- [ ] `RegisterTenantUserUseCase` delega el envío sin construir el `Map` directamente.
- [ ] `ResendVerificationEmailUseCase` delega el envío sin construir el `Map` directamente.
- [ ] Agregar o renombrar un parámetro del template requiere cambiar solo `SendVerificationEmailUseCase`.
- [ ] Tests del nuevo use case cubren el contrato completo del `Map` (todos los 7 parámetros esperados por el template).
- [ ] Tests de los use cases que delegan mockean el nuevo componente (no el port directamente).
- [ ] `TRACEABILITY.md` actualizado.

---

## Decisiones de diseño

| Decisión | Alternativas consideradas | Elección | Razón |
|---|---|---|---|
| Componente de extracción | Use case inyectable (A) vs helper estático (B) vs default method en port (C) | Por definir — ver tabla de opciones | — |
| Firma del método | `execute(User, String code, String clientId)` vs `execute(SendVerificationEmailCommand)` | Por definir | Un `Command` es más extensible si el contrato crece |

---

## Inconsistencies Found

| # | Descripción | Archivos involucrados | Estado | Resolución |
|---|-------------|----------------------|--------|-----------|
| — | *Ninguna* | — | — | — |

---

## Residuals

| # | Descripción | Diferido a | Estado |
|---|-------------|-----------|--------|
| — | *Ninguno* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← README.md](../README.md)
