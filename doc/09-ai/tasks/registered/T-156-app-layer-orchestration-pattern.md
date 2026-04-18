# T-156 — Patrón Orchestration Use Case en keygo-app

**Estado:** ⬜ Registrada
**Módulos afectados:** `keygo-app`, docs

---

## Problema / Requisito

T-154 introduce el primer caso donde un flujo de negocio necesita combinar más de un use case
en secuencia: verificar email **y** crear Membership según política. La solución acordada es
un use case orquestador específico del flujo.

Este patrón se repetirá en T-155 (aceptar invitación = crear user ACTIVE + crear Membership)
y en cualquier flujo futuro que combine operaciones de distintos bounded contexts dentro de
la capa `keygo-app`.

El objetivo de esta tarea es **documentar el patrón formalmente** para que todos los flujos
lo apliquen de forma coherente sin tener que redescubrirlo cada vez.

## Relaciones

| Artefacto relacionado | Tipo de relación | Descripción |
|---|---|---|
| T-154 | derivada de | T-154 introduce el primer orquestador; esta tarea documenta el patrón |
| T-155 | habilitadora | T-155 debe seguir este patrón al implementar `AcceptInvitationUseCase` |

---

## Requisito

Crear documentación de referencia en `doc/` que cubra:

1. **Distinción de roles:**
   - `XxxUseCase` — operación atómica, un solo bounded context, reutilizable como
     building block.
   - `XxxOrchestrator` — coordina múltiples use cases, puede cruzar bounded contexts,
     no debe usarse como building block de otro orquestador.

2. **Cuándo introducir un orquestador** vs extender un use case existente.

3. **Convención de nombre:** sufijo `Orchestrator`, prefijo = contexto del flujo de negocio
   (ej. `SelfRegistrationOrchestrator`, `AcceptInvitationOrchestrator`).

4. **Regla de ubicación:** el orquestador vive bajo
   `keygo-app/<bounded-context-dominante>/orchestrator/`.

5. **Estructura canónica:** qué inyecta, qué retorna, cómo propaga excepciones de los
   use cases subordinados.

6. **Ejemplo de referencia** con el código de `SelfRegistrationOrchestrator`
   (introducido en T-154).

---

## Pasos de implementación

| # | Acción | Archivo | Estado |
|---|---|---|---|
| 1 | Crear documento de patrón en `doc/03-architecture/` | `doc/03-architecture/patterns/orchestration-use-case.md` | PENDING |
| 2 | Registrar entrada en `doc/03-architecture/README.md` | — | PENDING |

---

## Historial de transiciones

- 2026-04-15 → ⬜ Registrada
