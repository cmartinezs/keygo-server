# T-141 — Endpoint de perfil público de usuario de plataforma para UI

**Estado:** ⬜ Registrada
**Horizonte:** corto plazo
**Módulos afectados:** `keygo-app`, `keygo-api`, `keygo-run`, `postman`, docs

## Requisito

La UI necesita un endpoint que entregue la información pública y segura de un `platform_user`
para poblar superficies como encabezado, perfil básico o contexto de sesión, sin obligar al
frontend a recomponer estos datos desde múltiples respuestas o contratos indirectos.

Ejemplos mínimos de datos esperados:

- correo electrónico
- nombre
- apellido
- URL de avatar

La tarea debe definir un contrato explícito para exponer solo atributos públicos/seguros del
usuario de plataforma, evitando mezclar información sensible, administrativa o interna.

## Decisiones a tomar antes de activar

Antes de planificar la implementación, resolver:

1. Si el endpoint será para el usuario autenticado actual (`me`) o si también debe soportar
   consulta por identificador externo bajo otro contexto.
2. Cuál será el contrato exacto del payload: además de `email`, `firstName`, `lastName` y
   `avatarUrl`, definir si incluye `displayName`, `emailVerified`, `phone`, `locale` u otros
   campos derivados.
3. Qué mecanismo de autenticación/autorización aplicará: sesión del flujo hosted login,
   Bearer token de plataforma, ambos, u otro contexto controlado.
4. Qué política tendrá `avatarUrl` cuando el usuario no tenga imagen: `null`, placeholder
   calculado o URL por defecto provista por backend.
5. Si el contrato se expondrá como un endpoint nuevo de `account/profile`, `account/me` o una
   ruta equivalente más alineada con el resto de la API de plataforma.

## Pasos de Implementación

> A detallar cuando se active la tarea.

## Verificación

> A definir cuando se active la tarea.
