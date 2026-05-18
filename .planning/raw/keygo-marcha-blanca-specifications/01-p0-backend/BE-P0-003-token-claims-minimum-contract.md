# SPEC Backend P0 — Claims mínimos funcionales en tokens

| Campo | Valor |
|---|---|
| ID | `BE-P0-003` |
| Tipo | Backend specification |
| Prioridad | P0 / Bloqueante |
| Área | Backend |
| Repositorio objetivo | `keygo-server` / auth modules |
| Módulo sugerido | Token service, OAuth2/OIDC, JWT |
| Estado | Propuesta para implementación |

## Problema

Para que una app externa pueda validar acceso por membership sin consultar KeyGo en cada request, el access token debe incluir claims funcionales mínimos. El diagnóstico recomienda confirmar `sub`, `tid`, `cid`, `roles`, `scp/scopes`.

## Decisión funcional

El `access_token` emitido para una app cliente debe incluir claims suficientes para identificar usuario, tenant, app origen, roles de membership y scopes concedidos.

## Claims mínimos requeridos

| Claim | Descripción | Fuente |
|---|---|---|
| `iss` | Issuer por tenant o issuer de KeyGo con tenant resoluble | Configuración OIDC |
| `sub` | ID del usuario autenticado | User |
| `tid` | ID o slug del tenant | Tenant |
| `cid` | Client ID de la app que originó el login | ClientApp |
| `roles` | Roles del usuario en esa app | MembershipRole |
| `scp` o `scopes` | Scopes concedidos | OAuth grant |

## Ejemplo esperado

```json
{
  "iss": "https://auth.keygo.cl/t/acme",
  "sub": "user_123",
  "tid": "tenant_123",
  "cid": "kg_app_abc123",
  "roles": ["USER", "VIEWER"],
  "scp": "openid profile email",
  "exp": 1790000000,
  "iat": 1789996400
}
```

## Alcance incluido

- Revisar generación de access token.
- Confirmar que roles provienen de membership activa para la app actual.
- Confirmar que no se mezclan roles administrativos KeyGo con roles de app en el claim `roles`, salvo decisión documentada.
- Agregar test para usuario con membership y roles.
- Agregar test para usuario sin membership activa.

## Fuera de alcance

- Rediseñar OIDC completo.
- Implementar UserInfo si no existe.
- Cambiar algoritmo de firma.

## Instrucciones para AI Agent

1. Localizar servicio de emisión de tokens.
2. Identificar cómo se resuelve tenant y client app durante `/token`.
3. Verificar que la membership activa sea consultada para `sub + cid + tid`.
4. Poblar `roles` desde roles de app asociados a la membership.
5. Poblar `scp` o `scopes` desde scopes concedidos.
6. Agregar o actualizar tests.
7. Documentar si se usa `scp` string estilo OAuth o `scopes` array.

## Criterios de aceptación

- El access token contiene `sub`, `tid`, `cid`, `roles`, `scp` o `scopes`.
- Un usuario sin membership activa no recibe token válido para esa app en modo closed/invite-only.
- Los roles del token corresponden a la app actual, no a otra app del mismo tenant.
- Los tokens siguen validando con JWKS.

## Pruebas sugeridas

- Test de token para usuario con membership `ACTIVE`.
- Test de token para membership `SUSPENDED` o ausente.
- Test de aislamiento: roles de app A no aparecen en token de app B.

## Definition of Done

- Token funcional validable por app externa.
- Claims mínimos documentados.
- Tests de regresión disponibles.
