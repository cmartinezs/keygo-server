# Hosted Login Handoff Example

Referencia aislada para `T-056`: contrato tipado de hosted login, validación runtime de query params obligatorios y boundary reutilizable antes de llamar `GET /oauth2/authorize`.

## Qué incluye

- `src/hostedLoginParams.ts`
  - `HostedLoginParams`
  - `parseHostedLoginParams()`
  - `assertHostedLoginParams()`
  - `buildHostedLoginQuery()`
  - `toAuthorizeQuery()`
- `src/HostedLoginBoundary.tsx`
  - `HostedLoginBoundary`
  - `useHostedLoginParams()`
- `src/exampleUsage.tsx`
  - ejemplo app origen → login central → authorize
- `tests/`
  - validación de parser/guard y render del boundary

## Contrato de query params obligatorios

- `tenantSlug`
- `client_id`
- `redirect_uri`
- `scope`
- `response_type=code`
- `state`
- `code_challenge`
- `code_challenge_method=S256`

## Uso rápido

```bash
cd examples/hosted-login-handoff
npm install
npm run typecheck
npm test
```

## Ejemplo de handoff

```ts
import { createHostedLoginRedirectUrl, exampleOriginAppParams } from './src/exampleUsage';

const loginUrl = 'https://login.keygo.dev/login';
const redirect = createHostedLoginRedirectUrl(loginUrl, exampleOriginAppParams);
```

El redirect resultante contiene el contexto mínimo que `keygo-ui` debe validar antes de llamar a:

```text
GET /keygo-server/api/v1/tenants/{tenantSlug}/oauth2/authorize
```

## Nota sobre parámetros firmados

Este ejemplo implementa la base **validada** en runtime dentro del frontend. Para una variante **firmada**, la recomendación es generar el handoff desde un backend/BFF de la app origen y versionar el envelope (ver propuesta `T-057`).

