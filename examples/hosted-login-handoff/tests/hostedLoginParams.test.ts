import {
  assertHostedLoginParams,
  buildHostedLoginQuery,
  parseHostedLoginParams,
  toAuthorizeQuery,
} from '../src/hostedLoginParams';
import { describe, expect, it } from 'vitest';

describe('hostedLoginParams', () => {
  it('parsea y normaliza un handoff válido', () => {
    // Given
    const search =
      '?tenantSlug=acme-corp&client_id=acme-storefront&redirect_uri=https%3A%2F%2Fstore.acme.com%2Fauth%2Fcallback&scope=openid%20profile%20email%20profile&response_type=code&state=12345678-state&code_challenge=QmFzZTY0VVJMX1BLQ0VfQ0hBTExFTkdFX0VYQU1QTEVfX18&code_challenge_method=S256&app_display_name=ACME%20Store';

    // When
    const result = parseHostedLoginParams(search);

    // Then
    expect(result.ok).toBe(true);
    if (!result.ok) {
      throw new Error('Se esperaba un handoff válido.');
    }
    expect(result.value.tenantSlug).toBe('acme-corp');
    expect(result.value.clientId).toBe('acme-storefront');
    expect(result.value.scope).toEqual(['openid', 'profile', 'email']);
    expect(result.value.codeChallengeMethod).toBe('S256');
    expect(result.value.appDisplayName).toBe('ACME Store');
  });

  it('rechaza parámetros obligatorios faltantes', () => {
    // Given
    const search = '?tenantSlug=acme-corp';

    // When
    const result = parseHostedLoginParams(search);

    // Then
    expect(result.ok).toBe(false);
    if (result.ok) {
      throw new Error('Se esperaba resultado inválido.');
    }
    expect(result.errors.some((error) => error.param === 'client_id')).toBe(true);
    expect(result.errors.some((error) => error.param === 'redirect_uri')).toBe(true);
  });

  it('rechaza code_challenge_method distinto de S256', () => {
    // Given
    const search =
      '?tenantSlug=acme-corp&client_id=acme-storefront&redirect_uri=https%3A%2F%2Fstore.acme.com%2Fauth%2Fcallback&scope=openid&response_type=code&state=12345678-state&code_challenge=QmFzZTY0VVJMX1BLQ0VfQ0hBTExFTkdFX0VYQU1QTEVfX18&code_challenge_method=plain';

    // When
    const result = parseHostedLoginParams(search);

    // Then
    expect(result.ok).toBe(false);
    if (result.ok) {
      throw new Error('Se esperaba error de método PKCE.');
    }
    expect(
      result.errors.some(
        (error) => error.code === 'INVALID_CODE_CHALLENGE_METHOD',
      ),
    ).toBe(true);
  });

  it('construye query de handoff y authorize a partir del contrato tipado', () => {
    // Given
    const query = buildHostedLoginQuery({
      tenantSlug: 'acme-corp',
      clientId: 'acme-storefront',
      redirectUri: 'https://store.acme.com/auth/callback',
      scope: ['openid', 'profile', 'email'],
      state: '12345678-state',
      codeChallenge: 'QmFzZTY0VVJMX1BLQ0VfQ0hBTExFTkdFX0VYQU1QTEVfX18',
      appDisplayName: 'ACME Store',
      handoffVersion: '1',
    });

    // When
    const params = assertHostedLoginParams(query);
    const authorizeQuery = toAuthorizeQuery(params);

    // Then
    expect(query).toContain('tenantSlug=acme-corp');
    expect(query).toContain('client_id=acme-storefront');
    expect(authorizeQuery).toContain('client_id=acme-storefront');
    expect(authorizeQuery).not.toContain('tenantSlug=');
    expect(authorizeQuery).not.toContain('app_display_name=');
  });
});


