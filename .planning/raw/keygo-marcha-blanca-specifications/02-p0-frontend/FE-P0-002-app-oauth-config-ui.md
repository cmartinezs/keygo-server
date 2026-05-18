# SPEC Frontend P0 — Configurar Redirect URIs y Scopes en UI de apps

| Campo | Valor |
|---|---|
| ID | `FE-P0-002` |
| Tipo | Frontend specification |
| Prioridad | P0 / Bloqueante |
| Área | Frontend |
| Repositorio objetivo | `keygo-web-ui` / React |
| Módulo sugerido | Tenant Console / Apps / OAuth |
| Estado | Propuesta para implementación |

## Problema

La UI contempla schema de `redirect_uris` y `scopes`, pero el modal visible gestiona principalmente nombre, descripción, tipo y grants. Para `AUTHORIZATION_CODE`, una app sin redirect URI o scopes claros no queda integrable.

## Decisión funcional

La UI debe exponer configuración OAuth mínima al crear y editar apps:

- grants,
- redirect URIs,
- scopes base,
- validaciones condicionales.

## Alcance incluido

- Agregar inputs para redirect URIs.
- Agregar selección de scopes base: `openid`, `profile`, `email`.
- Validar que `AUTHORIZATION_CODE` requiere al menos una redirect URI.
- Mostrar mensajes de error por campo.
- Mantener compatibilidad con create/update app.

## Fuera de alcance

- Consent screen avanzado.
- Scope management global.
- Dynamic client registration.

## Diseño funcional sugerido

```text
OAuth

Grants
[x] Authorization Code + PKCE
[ ] Client Credentials
[x] Refresh Token

Redirect URIs
- https://cliente.cl/callback
[+ Agregar URI]

Scopes
[x] openid  [x] profile  [x] email
[+ Agregar scope custom]
```

## Instrucciones para AI Agent

1. Localizar form de creación/edición de app.
2. Revisar types actuales de app create/update.
3. Verificar si los campos usan snake_case o camelCase en frontend.
4. Agregar field array para redirect URIs.
5. Agregar selector de scopes.
6. Agregar validación:
   - si grants incluye `AUTHORIZATION_CODE`, redirect URIs no puede estar vacío;
   - cada redirect URI debe ser URL válida;
   - scopes base sugeridos por defecto.
7. Actualizar cliente API si actualmente no envía esos campos.
8. Manejar errores backend `400` por campo si existen.

## Criterios de aceptación

- Crear app Authorization Code exige redirect URI.
- Crear app muestra scopes base seleccionables.
- Editar app permite modificar redirect URIs y scopes.
- El payload enviado al backend incluye grants, redirect URIs y scopes.
- Los errores de backend se muestran de forma entendible.

## Pruebas sugeridas

- Render form con scopes base.
- Validación falla si Auth Code está activo sin redirect URI.
- Payload contiene `redirectUris`/`redirect_uris` según contrato real.
- Update app conserva configuración existente.

## Definition of Done

- App OAuth queda integrable desde UI.
- No se requiere editar datos manualmente en backend para configurar una app básica.
