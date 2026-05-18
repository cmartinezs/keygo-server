# SPEC Frontend P0 — Mostrar client secret una sola vez al crear app

| Campo | Valor |
|---|---|
| ID | `FE-P0-001` |
| Tipo | Frontend specification |
| Prioridad | P0 / Bloqueante |
| Área | Frontend |
| Repositorio objetivo | `keygo-web-ui` / React |
| Módulo sugerido | Tenant Console / Apps |
| Estado | Propuesta para implementación |

## Problema

Al crear una app, backend devuelve `ClientAppSecretData` con `client_id` y `client_secret`, pero la UI cierra modal y muestra toast de éxito sin enseñar el secret. El usuario pierde el secret y no puede integrar la app.

## Decisión funcional

Después de crear una app, la UI debe mostrar una pantalla/modal obligatorio de “Secret generado”, permitiendo copiar el secret y confirmando que fue guardado antes de cerrar.

## Alcance incluido

- Interceptar response de creación de app.
- Mostrar `client_id` y `client_secret` en modal/pantalla de disclosure.
- Botón copiar secret.
- Checkbox obligatorio: “Confirmo que guardé el secret”.
- Bloquear cierre accidental del modal hasta confirmación, o al menos advertir.
- Acción final: “Ir al detalle app” o “Cerrar”.

## Fuera de alcance

- Cambiar endpoint backend.
- Volver a mostrar secrets antiguos.
- Implementar rotación de secret si aún no existe UI.

## Mockup funcional

```text
┌───────────────────────────────────────────────┐
│ Aplicación creada                             │
├───────────────────────────────────────────────┤
│ Nombre: Portal Clientes                       │
│ Client ID: kg_app_abc123                      │
│                                               │
│ Secret generado                               │
│ ┌───────────────────────────────────────────┐ │
│ │ kg_secret_xxxxxxxxxxxxxxxxxxxxxxxxx       │ │
│ └───────────────────────────────────────────┘ │
│                                               │
│ ⚠ Este secret no volverá a mostrarse.         │
│                                               │
│ [Copiar secret]                               │
│ [ ] Confirmo que guardé el secret             │
│                                               │
│                         [Ir al detalle app]   │
└───────────────────────────────────────────────┘
```

## Instrucciones para AI Agent

1. Localizar componente/modal de creación de app.
2. Revisar cliente API que consume `POST /api/v1/tenants/{tenantSlug}/apps`.
3. Confirmar shape real de response `ClientAppSecretData`.
4. Evitar descartar response al cerrar modal.
5. Crear componente `ClientSecretDisclosureDialog` o equivalente.
6. Agregar estado local para mantener secret solo en memoria mientras se muestra.
7. Limpiar secret del estado al confirmar cierre.
8. Agregar test de UI si hay framework configurado.

## Reglas UX

- El secret debe poder copiarse con un click.
- Debe existir advertencia explícita: “Este secret no volverá a mostrarse”.
- El botón final debe estar deshabilitado hasta marcar confirmación.
- No mostrar el secret en logs, toast, URL ni almacenamiento persistente.

## Criterios de aceptación

- Al crear app, se muestra modal/pantalla con `client_id` y `client_secret`.
- El usuario puede copiar el secret.
- La UI no permite cerrar flujo final sin reconocer que guardó el secret.
- Al cerrar, el secret se elimina del estado de UI.
- No se persiste secret en localStorage/sessionStorage.

## Pruebas sugeridas

- Test de creación exitosa muestra disclosure.
- Test botón copiar usa Clipboard API o fallback.
- Test botón final deshabilitado hasta checkbox.
- Test no se muestra disclosure si create falla.

## Definition of Done

- Flujo de creación de app ya no pierde secret.
- Tenant admin puede usar secret para integración real.
