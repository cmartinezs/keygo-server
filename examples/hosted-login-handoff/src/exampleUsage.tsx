import React from 'react';
import {
  buildHostedLoginQuery,
  toAuthorizeQuery,
  type HostedLoginParamsInput,
} from './hostedLoginParams';
import {
  HostedLoginBoundary,
  useHostedLoginParams,
} from './HostedLoginBoundary';

export function createHostedLoginRedirectUrl(
  loginUrl: string,
  params: HostedLoginParamsInput,
): string {
  return `${loginUrl}?${buildHostedLoginQuery(params)}`;
}

function HostedLoginPreview() {
  const params = useHostedLoginParams();
  const authorizeUrl = `/keygo-server/api/v1/tenants/${params.tenantSlug}/oauth2/authorize?${toAuthorizeQuery(params)}`;

  return (
    <section>
      <h1>
        Iniciar sesión en {params.appDisplayName ?? params.clientName ?? params.clientId}
      </h1>
      <p>Tenant efectivo: {params.tenantSlug}</p>
      <p>Client ID efectivo: {params.clientId}</p>
      <code>{authorizeUrl}</code>
    </section>
  );
}

export function HostedLoginPageExample({
  search,
}: Readonly<{
  search: string;
}>) {
  return (
    <HostedLoginBoundary search={search}>
      <HostedLoginPreview />
    </HostedLoginBoundary>
  );
}

export const exampleOriginAppParams: HostedLoginParamsInput = {
  tenantSlug: 'acme-corp',
  clientId: 'acme-storefront',
  redirectUri: 'https://store.acme.com/auth/callback',
  scope: ['openid', 'profile', 'email'],
  state: '9d4fa792-state',
  codeChallenge: 'QmFzZTY0VVJMX1BLQ0VfQ0hBTExFTkdFX0VYQU1QTEVfX18',
  clientName: 'ACME Storefront',
  appDisplayName: 'ACME Store',
  handoffVersion: '1',
};


