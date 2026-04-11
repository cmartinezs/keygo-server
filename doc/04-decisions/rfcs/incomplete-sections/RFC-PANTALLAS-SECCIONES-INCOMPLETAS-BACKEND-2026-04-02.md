# RFC: Pantallas y secciones incompletas por información y/o contrato backend

- Fecha: 2026-04-02
- Estado: Propuesto
- Autor: Copilot (GPT-5.3-Codex)
- Alcance: SPA keygo-web-ui (rutas públicas y autenticadas)

## 1. Objetivo

Consolidar en un solo RFC todas las pantallas y secciones que hoy están incompletas por al menos una de estas causas:

1. Falta de endpoint backend o contrato oficial.
2. Endpoint existente pero sin wiring funcional en UI.
3. Sección con contenido o acción aún no definida (gap de información funcional).

## 2. Fuentes de verdad usadas

Fuentes principales (prioridad alta):

1. docs/FRONTEND_DEVELOPER_GUIDE.md (inventario disponible vs pendiente, flujos por rol)
2. docs/api-docs.json (contrato OpenAPI técnico)
3. src/App.tsx (rutas reales expuestas)
4. src/layouts/AdminLayout.tsx (navegación real por rol)
5. src/pages/** y src/api/** (estado implementado real)
6. docs/FUNCTIONAL_GUIDE.md y docs/BACKLOG.md (contexto funcional y deuda documentada)

## 3. Criterio de clasificación

- Tipo A: Incompleta por backend pendiente.
- Tipo B: Incompleta por contenido/definición funcional pendiente.
- Tipo C: Incompleta por wiring UI pendiente (backend existe, UI no completa).

## 4. Inventario consolidado de gaps vigentes

## 4.1 Público (sin login)

| Pantalla/Sección | Ruta | Tipo | Gap detectado | Endpoint/Contrato relacionado | Evidencia principal |
|---|---|---|---|---|---|
| Recuperación de cuenta (Olvidé mi contraseña / Reset) | Sin ruta activa en App.tsx | A + C | No existe pantalla operativa ni ruta para el flujo. En OpenAPI no aparecen endpoints self-service de cuenta para forgot/reset; solo existe reset administrativo por userId | Self-service esperado: POST /api/v1/tenants/keygo/account/forgot-password, POST /api/v1/tenants/keygo/account/reset-password. Existente pero distinto: POST /api/v1/tenants/{tenantSlug}/users/{userId}/reset-password | FRONTEND_DEVELOPER_GUIDE 10.4 + docs/api-docs.json + src/App.tsx + src/api/users.ts |
| Pago de suscripción en producción | /subscribe (paso de pago) | A | En producción no hay pasarela integrada, solo mensaje informativo; en dev se usa mock approve | Endpoint de PSP real pendiente (referencia backlog: /billing/contracts/{id}/pay por definir) | src/pages/register/steps/PaymentStep.tsx + docs/FUNCTIONAL_GUIDE.md + docs/BACKLOG.md |
| Landing > Developers > SDKs e integraciones | / (sección Developers) | B | CTA marcado "Próximamente" y badge "En desarrollo" sin entrega funcional | N/A (definición de producto/documentación) | src/pages/landing/DevelopersSection.tsx |

## 4.2 Dashboard transversal y por rol

| Pantalla/Sección | Ruta | Tipo | Gap detectado | Endpoint/Contrato relacionado | Evidencia principal |
|---|---|---|---|---|---|
| Módulos placeholder por navegación | /dashboard/feature/:featureId | C (y en algunos casos A/B) | Vista placeholder genérica para módulos aún no conectados | Depende del featureId (apps, users, access, audit, signing-keys, sessions, tokens, api) | src/pages/dashboard/FeaturePlaceholderPage.tsx + src/layouts/AdminLayout.tsx |
| Home dashboard rol ADMIN_TENANT | /dashboard | C + B | Tarjetas muestran "--" y mensaje de métricas pendientes de integrar | Falta definir/consumir endpoints de métricas tenant scoped | src/pages/dashboard/DashboardHomePage.tsx |
| Home dashboard rol USER_TENANT | /dashboard | C + B | Tarjetas informativas sin datos reales (sesiones, último acceso, alertas) | Requiere fuentes backend para actividad/sesiones/alertas de usuario | src/pages/dashboard/DashboardHomePage.tsx |
| Header dashboard admin > selector de rango | /dashboard | B (posible A) | Selector Hoy/7/30 días declarado como visual, sin impacto en query | Parametrización backend para rango (si aplica) | docs/FUNCTIONAL_GUIDE.md (2.1 Encabezado) + src/pages/admin/DashboardPage.tsx |
| Header dashboard admin > Acciones rápidas | /dashboard | B + C | Botón visible pero sin contenido/flujo definido | N/A o endpoint según acción final | docs/FUNCTIONAL_GUIDE.md (2.1 Encabezado) + src/pages/admin/DashboardPage.tsx |

## 4.3 Cuenta de usuario (autenticado)

| Pantalla/Sección | Ruta | Tipo | Gap detectado | Endpoint/Contrato relacionado | Evidencia principal |
|---|---|---|---|---|---|
| Mi cuenta > pestaña Actividad (interna) | /dashboard/account (tab activity) | C + B | Sigue como panel de texto/placeholder, sin timeline real conectado | Falta definir fuente oficial de actividad personal | src/pages/dashboard/user/UserProfilePage.tsx |
| Configuración > Conexiones | /dashboard/account/settings?tab=connections | A (temporal cubierto con mock) | Implementado con MSW temporal, pendiente contrato backend oficial F-042 | GET/POST/DELETE /account/connections* pendiente backend oficial | src/api/account.ts + src/mocks/handlers.ts + docs/FUNCTIONAL_GUIDE.md |

## 4.4 Areas tenant/admin con deuda funcional asociada a backend

| Pantalla/Sección | Ruta | Tipo | Gap detectado | Endpoint/Contrato relacionado | Evidencia principal |
|---|---|---|---|---|---|
| Gestión de usuarios tenant > suspender/activar usuario | /dashboard/tenant/users | A + C | Operación no expuesta en UI actual. Tampoco se encuentra path equivalente en OpenAPI actual para usuarios (solo existe suspend/activate de tenant) | Esperado por negocio: PUT /api/v1/tenants/{tenantSlug}/users/{userId}/suspend y /activate. Existente distinto: PUT /api/v1/tenants/{slug}/suspend y /activate | FRONTEND_DEVELOPER_GUIDE 14.2.4 + docs/api-docs.json + src/pages/dashboard/tenant/TenantUsersPage.tsx |
| Gestión de usuarios tenant > ver sesiones por usuario | /dashboard/tenant/users | A + C | Sin acción de sesiones en UI y sin path equivalente en OpenAPI actual para sesiones por userId administradas por tenant-admin | Esperado por negocio: GET /api/v1/tenants/{tenantSlug}/users/{userId}/sessions | FRONTEND_DEVELOPER_GUIDE 14.2.4 + docs/api-docs.json + src/pages/dashboard/tenant/TenantUsersPage.tsx |

## 4.5 Verificación meticulosa de endpoints homonimos (scope correcto)

La validación se hizo contra docs/api-docs.json para evitar falsos pendientes por nombres parecidos.

| Caso | Endpoint esperado por pantalla | Estado en OpenAPI | Endpoint parecido que si existe | Conclusión |
|---|---|---|---|---|
| Recuperación de cuenta pública | POST /api/v1/tenants/keygo/account/forgot-password | No encontrado | POST /api/v1/tenants/{tenantSlug}/users/{userId}/reset-password | No equivalen. El existente es reset administrativo (tenant-admin), no forgot/reset self-service de usuario final |
| Reset por código de recuperación | POST /api/v1/tenants/keygo/account/reset-password | No encontrado | POST /api/v1/tenants/{tenantSlug}/users/{userId}/reset-password | No equivalen por flujo, auth y actor |
| Suspender usuario del tenant | PUT /api/v1/tenants/{tenantSlug}/users/{userId}/suspend | No encontrado | PUT /api/v1/tenants/{slug}/suspend | No equivalen. El existente suspende tenant completo |
| Activar usuario del tenant | PUT /api/v1/tenants/{tenantSlug}/users/{userId}/activate | No encontrado | PUT /api/v1/tenants/{slug}/activate | No equivalen. El existente activa tenant completo |
| Sesiones de usuario administradas por tenant-admin | GET /api/v1/tenants/{tenantSlug}/users/{userId}/sessions | No encontrado | GET /api/v1/tenants/{tenantSlug}/account/sessions | No equivalen. El existente es solo sesiones propias del usuario autenticado |
| Sesiones propias (cuenta) | GET /api/v1/tenants/{tenantSlug}/account/sessions | Encontrado | N/A | Disponible y ya consumido en UI |
| Cierre de sesión propia remota | DELETE /api/v1/tenants/{tenantSlug}/account/sessions/{sessionId} | Encontrado | N/A | Disponible y ya consumido en UI |

Nota: este RFC no marca como pendiente un endpoint solo por ausencia en frontend. Primero valida si existe en OpenAPI y después clasifica el gap como backend o wiring UI.

## 5. Observaciones de consistencia documental

Se detectaron discrepancias entre documentos históricos y el estado actual del código/backend:

1. docs/BACKLOG.md aún lista como pendientes algunos endpoints que en FRONTEND_DEVELOPER_GUIDE y api-docs.json ya aparecen disponibles (ej. change-password, account/sessions, notification-preferences en cuenta propia).
2. docs/FUNCTIONAL_GUIDE.md sección 5 mezcla items ya implementados con items realmente pendientes.
3. docs/account-ui-proposal contiene fases historicas y gaps que hoy ya no aplican totalmente.

Impacto:

- Riesgo de priorizar trabajo equivocado.
- Riesgo de abrir tareas de "pendiente backend" para endpoints ya productivos.

## 6. Priorización recomendada

### P0 (bloqueante de experiencia o contrato)

1. Definir con backend el contrato final de conexiones de cuenta (F-042) para reemplazar MSW.
2. Cerrar definición e implementación de flujo forgot/reset password (endpoint + UI pública).
3. Definir e integrar pasarela PSP real para /subscribe en producción.

### P1 (completitud de navegación por rol)

1. Reemplazar placeholders de /dashboard/feature/:featureId por módulos funcionales priorizados por negocio.
2. Completar dashboard home para ADMIN_TENANT y USER_TENANT con métricas reales.
3. Resolver actividad real en tab "Actividad" de Mi cuenta.

### P2 (calidad de producto/documentación)

1. Definir contenido y acción para "Acciones rapidas" del dashboard admin.
2. Publicar deliverable real para "SDKs e integraciones" en landing developers.
3. Sincronizar BACKLOG y FUNCTIONAL_GUIDE con estado real de backend/UI.

## 7. Criterio de cierre por item

Un ítem se considera cerrado cuando cumple simultáneamente:

1. Contrato backend confirmado en docs/api-docs.json.
2. Estado actualizado en docs/FRONTEND_DEVELOPER_GUIDE.md.
3. Wiring completo en UI (sin placeholder ni mock temporal).
4. Cobertura mínima de pruebas (API + pantalla afectada).
5. Actualización de docs/FUNCTIONAL_GUIDE.md y docs/TECHNICAL_GUIDE.md.

## 8. Anexo: mapa rápido de placeholders navegables actuales

Rutas que hoy pasan por placeholder genérico:

- /dashboard/feature/apps
- /dashboard/feature/users
- /dashboard/feature/access
- /dashboard/feature/audit
- /dashboard/feature/signing-keys
- /dashboard/feature/sessions
- /dashboard/feature/tokens
- /dashboard/feature/api

Nota: estas rutas están enlazadas desde el sidebar por rol en src/layouts/AdminLayout.tsx y renderizan src/pages/dashboard/FeaturePlaceholderPage.tsx.

## 9. Referencia operativa

Para seguimiento endpoint por endpoint (UI esperada vs OpenAPI vs API front vs consumo en pantalla), revisar:

- docs/ENDPOINT_COVERAGE_MATRIX_UI_OPENAPI_2026-04-02.md
