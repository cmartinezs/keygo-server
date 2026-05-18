# KeyGo — Specifications para marcha blanca controlada

Este paquete transforma el diagnóstico previo de marcha blanca de KeyGo en documentos individuales tipo **specification**, separados por responsabilidad **Frontend** y **Backend**. Cada documento está preparado para ser usado como entrada de trabajo por un AI agent de implementación.

## Objetivo del paquete

Preparar KeyGo para una **marcha blanca interna o piloto guiado**, con alcance limitado, evitando habilitar autoservicio público o flujos incompletos antes de cerrar las brechas P0/P1.

## Criterio general de marcha blanca

KeyGo puede entrar a marcha blanca solo cuando se cumpla como mínimo:

```text
GO si:
- crear app muestra secret
- app permite redirect URIs/scopes
- tenant admin puede entrar sin 403 por rol
- membership muestra app + roles legibles + fecha válida
- usuario puede login y recibir token con roles correctos
- app externa puede validar acceso por membership

NO-GO si:
- el secret se pierde
- tenant admin no pasa autorización backend
- memberships muestran UUIDs/fechas inválidas
- hay mocks activos en flujos centrales
- no puedes configurar roles de app desde UI
```

## Estructura

```text
keygo-marcha-blanca-specifications/
├── 00-context/
│   ├── 00-agent-implementation-guidelines.md
│   ├── 01-marcha-blanca-scope.md
│   └── 02-implementation-order.md
├── 01-p0-backend/
│   ├── BE-P0-001-unify-admin-role-naming.md
│   ├── BE-P0-002-membership-dto-readable-roles-created-at.md
│   ├── BE-P0-003-token-claims-minimum-contract.md
│   └── BE-P0-004-app-oauth-config-contract-validation.md
├── 02-p0-frontend/
│   ├── FE-P0-001-client-secret-one-time-disclosure.md
│   ├── FE-P0-002-app-oauth-config-ui.md
│   ├── FE-P0-003-route-guards-role-naming-alignment.md
│   ├── FE-P0-004-membership-readable-roles-ui.md
│   └── FE-P0-005-app-detail-roles-ui.md
├── 03-p1-backend/
│   ├── BE-P1-001-user-status-action-contract.md
│   ├── BE-P1-002-admin-user-sessions-endpoint.md
│   ├── BE-P1-003-users-and-apps-search-support.md
│   ├── BE-P1-004-membership-approval-policy.md
│   ├── BE-P1-005-app-registration-access-policy.md
│   └── BE-P1-006-oauth-revoke-logout-contract.md
├── 04-p1-frontend/
│   ├── FE-P1-001-user-status-action-integration.md
│   ├── FE-P1-002-admin-user-sessions-ui-or-disable.md
│   ├── FE-P1-003-disable-msw-and-real-api-matrix.md
│   ├── FE-P1-004-remote-autocomplete-users-apps.md
│   ├── FE-P1-005-membership-pending-requests-ui.md
│   ├── FE-P1-006-app-registration-access-policy-ui.md
│   ├── FE-P1-007-oauth-revoke-logout-integration.md
│   ├── FE-P1-008-user-detail-assigned-apps.md
│   └── FE-P1-009-no-membership-screen.md
└── 05-quality-gates/
    ├── QA-001-marcha-blanca-go-no-go-checklist.md
    └── QA-002-pilot-test-scenarios.md
```

## Orden recomendado de ejecución

1. Ejecutar primero todas las specifications P0 Backend y P0 Frontend.
2. Validar el checklist de `05-quality-gates/QA-001-marcha-blanca-go-no-go-checklist.md`.
3. Ejecutar P1 según necesidad del piloto.
4. Correr escenarios de `05-quality-gates/QA-002-pilot-test-scenarios.md`.

## Convención de IDs

- `BE`: Backend.
- `FE`: Frontend.
- `QA`: Validación, pruebas o control de salida.
- `P0`: Bloqueante para marcha blanca real.
- `P1`: Importante para operar sin fricción.
