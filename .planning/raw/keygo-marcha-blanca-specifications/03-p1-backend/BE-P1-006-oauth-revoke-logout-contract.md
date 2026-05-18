# SPEC Backend P1 — Contrato de revocación OAuth y logout

| Campo | Valor |
|---|---|
| ID | `BE-P1-006` |
| Tipo | Backend specification |
| Prioridad | P1 / Importante |
| Área | Backend |
| Repositorio objetivo | `keygo-server` / OAuth/session modules |
| Módulo sugerido | Logout, token revocation |
| Estado | Propuesta para implementación |

## Problema

Frontend marca revocación como pendiente en comentarios, aunque podría existir controller backend. Para marcha blanca, logout debe cerrar sesión o revocar refresh token de forma consistente.

## Decisión funcional

Definir y exponer contrato real de logout/revoke para frontend. No dejar logout como simple limpieza local si existen refresh tokens persistentes.

## Endpoints recomendados

OAuth estándar:

```http
POST /oauth2/revoke
Content-Type: application/x-www-form-urlencoded

token=<refresh_token>&token_type_hint=refresh_token
```

Logout de sesión KeyGo:

```http
POST /api/v1/auth/logout
Authorization: Bearer <access_token>
```

## Alcance incluido

- Verificar si existe revocation endpoint.
- Documentar endpoint definitivo.
- Revocar refresh token si existe.
- Invalidar sesión si aplica.
- Responder idempotentemente.

## Fuera de alcance

- Single Logout federado avanzado.
- Back-channel logout OIDC.
- Revocación global de todas las sesiones salvo que ya exista.

## Instrucciones para AI Agent

1. Buscar controllers de revoke/logout.
2. Confirmar si refresh tokens se guardan hasheados.
3. Implementar o ajustar revocación.
4. Garantizar idempotencia: revocar un token ya revocado no debe filtrar estado sensible.
5. Agregar tests.
6. Actualizar documentación API.

## Criterios de aceptación

- Existe endpoint real y documentado para logout/revoke.
- Logout invalida refresh token o sesión según modelo actual.
- UI puede llamar endpoint sin depender de mocks.
- La respuesta no expone datos sensibles.

## Pruebas sugeridas

- Refresh token válido revocado: OK.
- Refresh token ya revocado: OK/idempotente.
- Token inválido: respuesta segura.
- Después de revocar, no se puede usar para refresh.

## Definition of Done

- Logout no es solo limpieza cosmética del frontend.
