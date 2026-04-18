# Frontend — Documentación de integración con KeyGo API

Punto de entrada para equipos de UI/frontend que consumen la API de KeyGo Server.

## Índice

| Documento | Descripción |
|---|---|
| [01-setup.md](01-setup.md) | Variables de entorno, dependencias, cliente HTTP base y headers |
| [02-authentication.md](02-authentication.md) | Flujo OAuth2/PKCE, OAuthService, auth store, almacenamiento de tokens, claims del JWT |
| [03-api-conventions.md](03-api-conventions.md) | Envelope `BaseResponse<T>`, paginación, filtros, headers de request/response |
| [04-error-handling.md](04-error-handling.md) | Estructura de error, códigos frecuentes, i18n de mensajes, trazabilidad, troubleshooting |
| [05-endpoints-account.md](05-endpoints-account.md) | Self-service: perfil, contraseña, sesiones, preferencias de notificación |
| [06-endpoints-tenant.md](06-endpoints-tenant.md) | Recursos de tenant: usuarios, apps, memberships (aprobación), roles |
| [07-endpoints-billing.md](07-endpoints-billing.md) | Catálogo de planes, contratos, suscripciones, facturas (tenant y platform) |
| [08-endpoints-admin.md](08-endpoints-admin.md) | Admin de plataforma: tenants, dashboard, stats, platform users, roles requeridos |
| [09-testing.md](09-testing.md) | Patrones de test con MSW, Vitest y React Testing Library |
| [10-endpoints-registration.md](10-endpoints-registration.md) | Discovery público (tenants/apps) y flujo de auto-registro: /register, /verify-email, /resend-verification |
| [feedback/README.md](feedback/README.md) | Registro de comunicación activa: gaps reportados por UI y cambios de contrato notificados por backend |

## Qué cubre esta sección

- Contratos HTTP consumibles por el frontend (request/response, headers, códigos de error).
- Flujos de autenticación y sesión desde la perspectiva del cliente.
- Convenciones de paginación, filtrado y ordenamiento.
- Convención de naming JSON: request/response bodies en `snake_case`.
- Ejemplos por dominio: account, tenant, billing, admin.
- Patrones de testing e integración con mocks.

## Reglas de mantenimiento

Actualizar ante cualquier cambio que impacte al cliente UI:

| Cambio en el backend | Qué actualizar |
|---|---|
| Nuevo endpoint consumible por UI | Documento del dominio correspondiente (`05` al `08`) + este `README.md` si aplica |
| Cambio de contrato (request/response/headers) | Sección del endpoint en el documento del dominio |
| Cambio de convención de naming JSON (`snake_case` / `camelCase`) | `03-api-conventions.md` |
| Nuevo `ResponseCode` o cambio de código de error | `04-error-handling.md` — tabla de códigos frecuentes |
| Cambio de flujo OAuth2/OIDC | `02-authentication.md` + `../authentication-flow.md` |
| Nueva convención de paginación o filtros | `03-api-conventions.md` |
| Nuevo documento en esta carpeta | Agregar fila a la tabla de índice de este `README.md` |
| Backend modifica contrato, flujo o estructura de response | `feedback/02-backend-to-ui.md` — nueva entrada `BE-NNN` |
| UI detecta endpoint faltante, campo ausente o tipo incorrecto | `feedback/01-ui-to-backend.md` — nueva entrada `UI-NNN` |
