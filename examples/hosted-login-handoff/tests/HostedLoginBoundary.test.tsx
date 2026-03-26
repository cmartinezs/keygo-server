import React from 'react';
import { renderToStaticMarkup } from 'react-dom/server';
import { describe, expect, it } from 'vitest';
import {
  HostedLoginBoundary,
  useHostedLoginParams,
} from '../src/HostedLoginBoundary';

function HostedLoginConsumer() {
  const params = useHostedLoginParams();
  return (
    <div>
      {params.tenantSlug}:{params.clientId}:{params.scope.join(',')}
    </div>
  );
}

describe('HostedLoginBoundary', () => {
  it('inyecta contexto cuando el handoff es válido', () => {
    // Given
    const search =
      '?tenantSlug=acme-corp&client_id=acme-storefront&redirect_uri=https%3A%2F%2Fstore.acme.com%2Fauth%2Fcallback&scope=openid%20profile&response_type=code&state=12345678-state&code_challenge=QmFzZTY0VVJMX1BLQ0VfQ0hBTExFTkdFX0VYQU1QTEVfX18&code_challenge_method=S256';

    // When
    const html = renderToStaticMarkup(
      <HostedLoginBoundary search={search}>
        <HostedLoginConsumer />
      </HostedLoginBoundary>,
    );

    // Then
    expect(html).toContain('acme-corp:acme-storefront:openid,profile');
  });

  it('renderiza fallback cuando el handoff es inválido', () => {
    // Given
    const search = '?tenantSlug=acme-corp';

    // When
    const html = renderToStaticMarkup(
      <HostedLoginBoundary
        search={search}
        invalidFallback={<p>handoff inválido</p>}
      >
        <HostedLoginConsumer />
      </HostedLoginBoundary>,
    );

    // Then
    expect(html).toContain('handoff inválido');
    expect(html).not.toContain('acme-corp:');
  });

  it('expone errores detallados vía renderInvalid', () => {
    // Given
    const search = '?tenantSlug=ACME';

    // When
    const html = renderToStaticMarkup(
      <HostedLoginBoundary
        search={search}
        renderInvalid={(errors) => (
          <p>{errors.map((error) => error.code).join(',')}</p>
        )}
      >
        <HostedLoginConsumer />
      </HostedLoginBoundary>,
    );

    // Then
    expect(html).toContain('MISSING_REQUIRED_PARAM');
  });
});


