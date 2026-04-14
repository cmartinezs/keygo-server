# T-138 — Endurecer tests de Controller con guards `isNotNull()` sobre `BaseResponse`

**Estado:** ⬜ Registrada
**Horizonte:** corto plazo
**Módulos afectados:** `keygo-api`, docs

## Requisito

Revisar y ajustar los tests de controllers para que, antes de leer cualquier dato desde
`response.getBody()` o desde alguno de sus objetos anidados, exista un `assertThat(...).isNotNull()`
explícito sobre el nodo que luego será dereferenciado.

La convención a aplicar es:

1. antes de usar `response.getBody()`, validar `assertThat(response.getBody()).isNotNull()`
2. antes de leer campos desde `getFailure()`, `getSuccess()`, `getDebug()`, `getThrowable()`,
   `getDate()` o `getData()`, validar `assertThat(...).isNotNull()` sobre ese objeto
3. aplicar esta regla sobre todo cuando después se validará contenido interno del objeto
   (`code`, `message`, `detail`, etc.)

El objetivo es que los tests fallen con una causa clara cuando el contrato `BaseResponse`
cambie o una rama del controller deje de poblar alguno de esos nodos.

## Decisiones a tomar antes de activar

Antes de planificar la implementación, resolver:

1. Si la tarea será solo correctiva sobre tests existentes o si además creará helpers reutilizables
   para reducir repetición.
2. Si la convención se aplicará únicamente en `keygo-api` o también en suites futuras de integración
   que validen `BaseResponse`.
3. Si se agregará una regla automática/revisión estática o si quedará como convención manual de PR.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
