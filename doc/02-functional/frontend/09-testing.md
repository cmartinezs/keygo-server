# Testing — Patrones de integración frontend

## Estrategia

| Nivel | Herramienta | Qué cubre |
|---|---|---|
| Unitario | Vitest | Servicios, stores, utilidades puras |
| Componente | React Testing Library | Flujos de UI con API mockeada |
| E2E | Playwright (recomendado) | Flujos completos con backend real |

## Mock del cliente HTTP (MSW)

Usar [Mock Service Worker](https://mswjs.io/) para interceptar requests en tests de componentes:

```typescript
// src/mocks/handlers.ts
import { http, HttpResponse } from 'msw';

export const handlers = [
  http.get('/api/v1/tenants/:slug/users', () => {
    return HttpResponse.json({
      success: true,
      data: {
        content: [{ userId: 'uuid-1', email: 'test@example.com', status: 'ACTIVE' }],
        page: 0, size: 20, totalElements: 1, totalPages: 1,
      },
    });
  }),

  http.post('/api/v1/tenants/:slug/users', () => {
    return HttpResponse.json({ success: true, data: { userId: 'uuid-new' } }, { status: 201 });
  }),
];

// src/mocks/server.ts
import { setupServer } from 'msw/node';
import { handlers } from './handlers';
export const server = setupServer(...handlers);
```

```typescript
// src/setupTests.ts
import { server } from './mocks/server';
beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());
```

## Test de OAuthService

```typescript
// src/services/oauth.spec.ts
import { describe, it, expect, vi } from 'vitest';
import { OAuthService } from './oauth';

const config = {
  clientId: 'test-client',
  redirectUri: 'http://localhost:3000/callback',
  issuer: 'http://localhost:8080/keygo-server',
};

describe('OAuthService', () => {
  it('genera code_challenge y redirige al authorize', async () => {
    const service = new OAuthService(config);
    Object.defineProperty(window, 'location', { value: { href: '' }, writable: true });

    await service.startLogin();

    expect(window.location.href).toContain('client_id=test-client');
    expect(window.location.href).toContain('code_challenge=');
    expect(window.location.href).toContain('code_challenge_method=S256');
  });

  it('canjea el código por tokens', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        access_token: 'at-123',
        refresh_token: 'rt-456',
        expires_in: 3600,
        token_type: 'Bearer',
      }),
    });

    sessionStorage.setItem('pkce_verifier', 'verifier-abc');
    const service = new OAuthService(config);
    const tokens = await service.handleCallback('code-xyz');

    expect(tokens.access_token).toBe('at-123');
    expect(sessionStorage.getItem('pkce_verifier')).toBeNull();
  });
});
```

## Test de componente con API mockeada

```typescript
// src/components/UserList.spec.tsx
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { UserList } from './UserList';

function wrapper({ children }: { children: React.ReactNode }) {
  return (
    <QueryClientProvider client={new QueryClient()}>
      {children}
    </QueryClientProvider>
  );
}

describe('UserList', () => {
  it('muestra usuarios del tenant', async () => {
    render(<UserList tenantSlug="acme" />, { wrapper });

    await waitFor(() => {
      expect(screen.getByText('test@example.com')).toBeInTheDocument();
    });
  });

  it('muestra error cuando la API falla', async () => {
    server.use(
      http.get('/api/v1/tenants/acme/users', () =>
        HttpResponse.json({ success: false, error: { code: 'TENANT_NOT_FOUND' } }, { status: 404 })
      )
    );

    render(<UserList tenantSlug="acme" />, { wrapper });

    await waitFor(() => {
      expect(screen.getByText(/tenant not found/i)).toBeInTheDocument();
    });
  });
});
```

## Test del parseError utility

```typescript
// src/services/errorHandler.spec.ts
import { parseError } from './errorHandler';
import axios from 'axios';

describe('parseError', () => {
  it('extrae code, status y traceId de un error de API', () => {
    const axiosError = {
      isAxiosError: true,
      response: {
        status: 404,
        data: { error: { code: 'USER_NOT_FOUND', message: 'Not found', traceId: 'trace-abc' } },
      },
    };
    vi.spyOn(axios, 'isAxiosError').mockReturnValue(true);
    Object.assign(axiosError, { config: {} });

    const err = parseError(axiosError);
    expect(err.code).toBe('USER_NOT_FOUND');
    expect(err.status).toBe(404);
    expect(err.traceId).toBe('trace-abc');
  });
});
```

## Configuración Vitest recomendada

```typescript
// vite.config.ts
import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/setupTests.ts'],
    globals: true,
  },
});
```
