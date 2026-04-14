# T-136 — Redefinir contrato de mensajes backend para UX/UI

**Estado:** ⬜ Registrada
**Horizonte:** corto plazo
**Módulos afectados:** `keygo-api`, `keygo-run`, docs, Postman

## Requisito

Redefinir la estructura de mensajes que entrega el backend, especialmente en respuestas de
error, para que la UI pueda determinar con claridad:

1. **Qué mensaje mostrar** al usuario final.
2. **Cuándo mostrarlo** (toast, inline, banner, modal, estado vacío, bloqueo de flujo).
3. **Cómo mostrarlo** (severidad, persistencia, acción sugerida, contexto de campo o pantalla).

La tarea debe cubrir no solo errores, sino también mensajes de éxito, advertencia e información
cuando formen parte del contrato funcional con frontend.

El objetivo es que el backend entregue señales explícitas y consistentes para UX, evitando que
la UI tenga que inferir semántica desde textos libres, códigos HTTP o `ResponseCode` ambiguos.

## Relación con tareas existentes

- `T-063` agrega trazabilidad (`traceId`/`requestId`) en `ErrorData`.
- `T-064` aborda catálogo i18n de errores por dominio.
- `T-066` propone `endpointHint`/`actionHint` para errores técnicos.

Esta tarea debe consolidar esas líneas en un **contrato de mensajería orientado a UI/UX**.

## Decisiones a tomar antes de activar

Antes de planificar la implementación, resolver:

1. Si el mensaje visible para usuario vendrá directamente desde backend o si backend entregará
   claves/códigos semánticos para que UI resuelva el texto final vía i18n propio.
2. Cuál será la estructura canónica del payload de mensaje (`code`, `severity`, `displayMode`,
   `field`, `actionHint`, `retryable`, etc.).
3. Qué mensajes son parte del contrato estable y cuáles deben seguir siendo internos/técnicos.
4. Cómo separar errores de validación de campo, errores de negocio, errores recuperables y
   errores técnicos no recuperables.
5. Si `BaseResponse<T>` debe ampliarse también para respuestas exitosas con mensajes UX.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
