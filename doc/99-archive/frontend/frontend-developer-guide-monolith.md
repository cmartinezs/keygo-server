# Frontend Developer Guide — Integración con KeyGo API

**Propósito:** Guía para desarrolladores frontend que integran con KeyGo API, cubriendo autenticación, endpoints comunes, manejo de errores y testing.

---

## Inicio Rápido

### Environment Setup

```bash
# .env.local
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_OAUTH_CLIENT_ID=my-web-app
VITE_OAUTH_REDIRECT_URI=http://localhost:3000/auth/callback
VITE_OAUTH_ISSUER=http://localhost:8080/api/v1/platform
```

### Install Dependencies

```bash
npm install axios react-query zustand
# axios: HTTP client
# react-query: Data fetching & caching
# zustand: Simple state management
```

---

## Authentication

### 1. OAuth2 Authorization Code Flow (PKCE)

**Flow Diagram:**
```
1. User clicks "Sign In"
2. Frontend redirects to /oauth/authorize?client_id=...&code_challenge=...
3. User logs in at KeyGo
4. KeyGo redirects back to callback with code
5. Frontend exchanges code for token (with code_verifier)
6. Frontend stores token, authenticated
```

### 2. Implementation: OAuth Service

```typescript
// src/services/oauth.ts

import { generateCodeChallenge, generateCodeVerifier } from 'pkce-gen';

export class OAuthService {
  private clientId: string;
  private redirectUri: string;
  private issuer: string;
  
  constructor(config: OAuthConfig) {
    this.clientId = config.clientId;
    this.redirectUri = config.redirectUri;
    this.issuer = config.issuer;
  }
  
  /**
   * Initiate OAuth flow
   */
  async startLogin(): Promise<void> {
    const codeVerifier = generateCodeVerifier();
    const codeChallenge = await generateCodeChallenge(codeVerifier);
    
    // Store in sessionStorage (not localStorage — more secure)
    sessionStorage.setItem('oauth_code_verifier', codeVerifier);
    
    const authorizeUrl = new URL(`${this.issuer}/oauth/authorize`);
    authorizeUrl.searchParams.set('client_id', this.clientId);
    authorizeUrl.searchParams.set('redirect_uri', this.redirectUri);
    authorizeUrl.searchParams.set('response_type', 'code');
    authorizeUrl.searchParams.set('code_challenge', codeChallenge);
    authorizeUrl.searchParams.set('code_challenge_method', 'S256');
    authorizeUrl.searchParams.set('scope', 'openid profile email');
    
    window.location.href = authorizeUrl.toString();
  }
  
  /**
   * Handle callback from KeyGo
   */
  async handleCallback(code: string): Promise<TokenResponse> {
    const codeVerifier = sessionStorage.getItem('oauth_code_verifier');
    if (!codeVerifier) {
      throw new Error('Code verifier not found');
    }
    
    const response = await fetch(`${this.issuer}/oauth/token`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        grant_type: 'authorization_code',
        code,
        client_id: this.clientId,
        redirect_uri: this.redirectUri,
        code_verifier: codeVerifier,
      }),
    });
    
    if (!response.ok) {
      throw new Error(`Token exchange failed: ${response.statusText}`);
    }
    
    const tokens = await response.json();
    sessionStorage.removeItem('oauth_code_verifier');
    
    return tokens;
  }
  
  /**
   * Refresh access token using refresh token
   */
  async refreshToken(refreshToken: string): Promise<TokenResponse> {
    const response = await fetch(`${this.issuer}/oauth/token`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        grant_type: 'refresh_token',
        refresh_token: refreshToken,
        client_id: this.clientId,
      }),
    });
    
    if (!response.ok) {
      throw new Error('Token refresh failed');
    }
    
    return await response.json();
  }
  
  /**
   * Logout: revoke tokens
   */
  async logout(accessToken: string): Promise<void> {
    await fetch(`${this.issuer}/oauth/revoke`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        token: accessToken,
        client_id: this.clientId,
      }),
    });
  }
}

interface TokenResponse {
  access_token: string;
  refresh_token: string;
  expires_in: number;
  token_type: 'Bearer';
}

interface OAuthConfig {
  clientId: string;
  redirectUri: string;
  issuer: string;
}
```

### 3. Auth Store (Zustand)

```typescript
// src/store/auth.ts

import { create } from 'zustand';
import { OAuthService } from '../services/oauth';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: User | null;
  isLoading: boolean;
  error: string | null;
  
  // Actions
  initializeAuth: () => Promise<void>;
  handleCallback: (code: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshAccessToken: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set, get) => {
  const oauthService = new OAuthService({
    clientId: import.meta.env.VITE_OAUTH_CLIENT_ID,
    redirectUri: import.meta.env.VITE_OAUTH_REDIRECT_URI,
    issuer: import.meta.env.VITE_OAUTH_ISSUER,
  });
  
  return {
    accessToken: localStorage.getItem('access_token'),
    refreshToken: localStorage.getItem('refresh_token'),
    user: null,
    isLoading: false,
    error: null,
    
    initializeAuth: async () => {
      const token = localStorage.getItem('access_token');
      if (!token) return;
      
      // TODO: Decode JWT and set user
      const user = decodeJWT(token);
      set({ user, accessToken: token });
    },
    
    handleCallback: async (code: string) => {
      set({ isLoading: true });
      try {
        const { access_token, refresh_token, expires_in } = 
            await oauthService.handleCallback(code);
        
        localStorage.setItem('access_token', access_token);
        localStorage.setItem('refresh_token', refresh_token);
        localStorage.setItem('token_expires_at', 
            (Date.now() + expires_in * 1000).toString());
        
        const user = decodeJWT(access_token);
        set({ 
          accessToken: access_token,
          refreshToken: refresh_token,
          user,
          error: null 
        });
      } catch (err) {
        set({ error: String(err) });
        throw err;
      } finally {
        set({ isLoading: false });
      }
    },
    
    logout: async () => {
      const { accessToken } = get();
      if (accessToken) {
        try {
          await oauthService.logout(accessToken);
        } catch (err) {
          console.error('Logout failed:', err);
        }
      }
      
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      localStorage.removeItem('token_expires_at');
      set({ accessToken: null, refreshToken: null, user: null });
    },
    
    refreshAccessToken: async () => {
      const { refreshToken } = get();
      if (!refreshToken) throw new Error('No refresh token');
      
      const { access_token, expires_in } = 
          await oauthService.refreshToken(refreshToken);
      
      localStorage.setItem('access_token', access_token);
      localStorage.setItem('token_expires_at', 
          (Date.now() + expires_in * 1000).toString());
      
      set({ accessToken: access_token });
    },
  };
});
```

### 4. HTTP Client with Auto-Refresh

```typescript
// src/services/api.ts

import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '../store/auth';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to every request
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Auto-refresh on 401
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { retry?: boolean };
    
    if (error.response?.status === 401 && !originalRequest.retry) {
      originalRequest.retry = true;
      
      try {
        await useAuthStore.getState().refreshAccessToken();
        const token = useAuthStore.getState().accessToken;
        originalRequest.headers.Authorization = `Bearer ${token}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        // Refresh failed, logout
        await useAuthStore.getState().logout();
        window.location.href = '/login';
        throw refreshError;
      }
    }
    
    return Promise.reject(error);
  }
);

export default apiClient;
```

---

## Common Endpoints

### Users

```typescript
// Create user
POST /api/v1/tenants/{tenantSlug}/users
Content-Type: application/json
Authorization: Bearer {accessToken}

{
  "email": "john@example.com",
  "username": "john_doe"
}

Response: 201 Created
{
  "data": {
    "userId": "550e8400-...",
    "email": "john@example.com",
    "username": "john_doe",
    "createdAt": "2026-04-10T10:00:00Z"
  }
}
```

```typescript
// Get user
GET /api/v1/tenants/{tenantSlug}/users/{userId}
Authorization: Bearer {accessToken}

Response: 200 OK
{
  "data": {
    "userId": "...",
    "email": "...",
    ...
  }
}
```

```typescript
// List users (with pagination)
GET /api/v1/tenants/{tenantSlug}/users?page=0&size=20
Authorization: Bearer {accessToken}

Response: 200 OK
{
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

### React Query Hook Example

```typescript
// src/hooks/useUsers.ts

import { useQuery, useMutation, useQueryClient } from 'react-query';
import apiClient from '../services/api';

export function useUsers(tenantSlug: string, page: number = 0, size: number = 20) {
  return useQuery(
    ['users', tenantSlug, page, size],
    () => apiClient.get(`/tenants/${tenantSlug}/users`, {
      params: { page, size }
    }).then(r => r.data.data),
    {
      staleTime: 5 * 60 * 1000,  // Cache 5 minutes
      cacheTime: 10 * 60 * 1000,
    }
  );
}

export function useCreateUser(tenantSlug: string) {
  const queryClient = useQueryClient();
  
  return useMutation(
    (userData: CreateUserRequest) =>
      apiClient.post(`/tenants/${tenantSlug}/users`, userData)
        .then(r => r.data.data),
    {
      onSuccess: () => {
        // Invalidate users list cache
        queryClient.invalidateQueries(['users', tenantSlug]);
      },
    }
  );
}
```

Usage in Component:
```typescript
function UserList({ tenantSlug }: Props) {
  const { data: users, isLoading, error } = useUsers(tenantSlug);
  const createUser = useCreateUser(tenantSlug);
  
  if (isLoading) return <div>Loading...</div>;
  if (error) return <div>Error: {error.message}</div>;
  
  return (
    <ul>
      {users?.content.map(user => (
        <li key={user.userId}>{user.email}</li>
      ))}
    </ul>
  );
}
```

---

## Error Handling

### API Error Structure

```json
{
  "error": {
    "code": "DUPLICATE_USER",
    "message": "User with this email already exists",
    "detail": "email=john@example.com, tenantId=550e8400-...",
    "traceId": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

### Error Handler Utility

```typescript
// src/services/errorHandler.ts

export class ApiError extends Error {
  constructor(
    public code: string,
    public message: string,
    public status: number,
    public traceId?: string
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export function parseError(error: unknown): ApiError {
  if (axios.isAxiosError(error)) {
    const { response } = error;
    
    if (response?.data?.error) {
      const { code, message, detail, traceId } = response.data.error;
      return new ApiError(code, message, response.status, traceId);
    }
    
    return new ApiError(
      'UNKNOWN_ERROR',
      response?.statusText || 'Unknown error',
      response?.status || 500
    );
  }
  
  return new ApiError('UNKNOWN_ERROR', String(error), 0);
}

// User-friendly error messages
export function getUserFriendlyMessage(code: string): string {
  const messages: Record<string, string> = {
    'DUPLICATE_USER': 'A user with this email already exists',
    'USER_NOT_FOUND': 'User not found',
    'PERMISSION_DENIED': 'You do not have permission to perform this action',
    'INVALID_REQUEST': 'The request is invalid',
    'INTERNAL_ERROR': 'An unexpected error occurred. Please try again.',
    'TENANT_NOT_FOUND': 'Tenant not found',
  };
  
  return messages[code] || messages['INTERNAL_ERROR'];
}
```

Usage in Component:
```typescript
function CreateUserForm() {
  const createUser = useCreateUser(tenantSlug);
  const [errorMessage, setErrorMessage] = useState('');
  
  const handleSubmit = async (formData: CreateUserRequest) => {
    try {
      await createUser.mutateAsync(formData);
      // Success
    } catch (err) {
      const apiError = parseError(err);
      setErrorMessage(getUserFriendlyMessage(apiError.code));
      console.error(`[${apiError.traceId}]`, apiError.message);
    }
  };
  
  return (
    <form onSubmit={handleSubmit}>
      {/* ... form fields ... */}
      {errorMessage && <div className="error">{errorMessage}</div>}
    </form>
  );
}
```

---

## Pagination

### Cursor-based (Recommended for Large Datasets)

```typescript
// Next API uses cursor pagination
GET /api/v1/tenants/{tenantSlug}/users?cursor=abc123&limit=20

Response:
{
  "data": {
    "items": [...],
    "pagination": {
      "cursor": "abc123",
      "nextCursor": "def456",
      "hasMore": true,
      "limit": 20
    }
  }
}
```

Infinite Query Example:
```typescript
export function useUsersPaginated(tenantSlug: string) {
  return useInfiniteQuery(
    ['users', tenantSlug],
    ({ pageParam = null }) =>
      apiClient.get(`/tenants/${tenantSlug}/users`, {
        params: { cursor: pageParam, limit: 20 }
      }).then(r => r.data.data),
    {
      getNextPageParam: (lastPage) => 
        lastPage.pagination.hasMore ? lastPage.pagination.nextCursor : null,
    }
  );
}
```

### Offset-based (Simple)

```typescript
GET /api/v1/tenants/{tenantSlug}/users?offset=0&limit=20

Response:
{
  "data": {
    "items": [...],
    "pagination": {
      "offset": 0,
      "limit": 20,
      "total": 150
    }
  }
}
```

---

## Rate Limiting

### Headers

```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1681234567
```

### Handling 429

```typescript
// src/services/rateLimitHandler.ts

export function handleRateLimit(remainingRequests: number, resetTime: number) {
  if (remainingRequests === 0) {
    const waitSeconds = Math.ceil((resetTime * 1000 - Date.now()) / 1000);
    console.warn(`Rate limited. Wait ${waitSeconds} seconds.`);
    
    // Exponential backoff
    return Math.min(waitSeconds * 1000, 60000);  // Max 60s
  }
  return 0;
}

// In interceptor
apiClient.interceptors.response.use(
  (response) => {
    const remaining = response.headers['x-ratelimit-remaining'];
    const reset = response.headers['x-ratelimit-reset'];
    
    if (remaining && Number(remaining) < 100) {
      console.warn(`Low rate limit: ${remaining} remaining`);
    }
    
    return response;
  },
  async (error) => {
    if (error.response?.status === 429) {
      const waitTime = handleRateLimit(
        0,
        parseInt(error.response.headers['x-ratelimit-reset'])
      );
      
      // Exponential backoff
      await new Promise(resolve => setTimeout(resolve, waitTime));
      return apiClient(error.config);
    }
    
    return Promise.reject(error);
  }
);
```

---

## Testing

### Unit Tests (Vitest)

```typescript
// src/services/oauth.spec.ts

import { describe, it, expect, vi } from 'vitest';
import { OAuthService } from './oauth';

describe('OAuthService', () => {
  const config = {
    clientId: 'test-client',
    redirectUri: 'http://localhost:3000/callback',
    issuer: 'http://localhost:8080/api/v1/platform',
  };
  
  it('should start login flow', async () => {
    const service = new OAuthService(config);
    
    // Mock window.location.href
    delete (window as any).location;
    window.location = { href: '' } as any;
    
    await service.startLogin();
    
    expect(window.location.href).toContain('client_id=test-client');
    expect(window.location.href).toContain('code_challenge=');
  });
  
  it('should exchange code for token', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: () => ({
        access_token: 'token123',
        refresh_token: 'refresh123',
        expires_in: 3600,
      }),
    });
    
    sessionStorage.setItem('oauth_code_verifier', 'verifier123');
    const service = new OAuthService(config);
    const tokens = await service.handleCallback('code123');
    
    expect(tokens.access_token).toBe('token123');
  });
});
```

### Integration Tests (React Testing Library)

```typescript
// src/components/LoginButton.spec.tsx

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClientProvider } from 'react-query';
import { LoginButton } from './LoginButton';

describe('LoginButton', () => {
  it('should redirect to OAuth flow on click', async () => {
    const mockLocation = { href: '' };
    Object.defineProperty(window, 'location', { value: mockLocation });
    
    render(<LoginButton />, { wrapper: QueryClientProvider });
    
    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));
    
    await waitFor(() => {
      expect(mockLocation.href).toContain('oauth/authorize');
    });
  });
});
```

---

## Best Practices

### 1. Token Storage

```typescript
// ✅ GOOD: Use sessionStorage (cleared on tab close)
sessionStorage.setItem('access_token', token);

// ✅ GOOD: Use memory store (cleared on refresh)
let accessToken = null;

// ❌ BAD: localStorage persists (vulnerable if compromised)
// localStorage.setItem('access_token', token);
```

### 2. Request Headers

```typescript
// ✅ GOOD: Include correlation ID for debugging
headers: {
  'X-Trace-ID': crypto.randomUUID(),
  'X-Requested-By': 'myapp-web',
}

// ✅ GOOD: Accept version header
headers: {
  'Accept': 'application/json; version=1',
}
```

### 3. Error Reporting

```typescript
// ✅ GOOD: Report with trace ID
Sentry.captureException(error, {
  tags: { traceId: apiError.traceId },
});

// ✅ GOOD: Log with context
console.error('Failed to create user', {
  email: formData.email,
  code: apiError.code,
  traceId: apiError.traceId,
});
```

### 4. Data Validation

```typescript
// ✅ GOOD: Validate before sending
if (!email.includes('@')) {
  setError('Invalid email format');
  return;
}

const response = await createUser({ email });

// ✅ GOOD: Validate response
if (!response.data?.userId) {
  throw new Error('Invalid response from server');
}
```

---

## Troubleshooting

### 401 Unauthorized

**Cause:** Access token expired or invalid

**Solution:**
1. Check token in localStorage/sessionStorage
2. Token should start with `eyJ` (JWT header)
3. Decode and check expiry: `new Date(exp * 1000)`
4. Re-login if expired

### 403 Forbidden

**Cause:** User lacks permission for endpoint

**Solution:**
1. Check user's roles in token: `jwt_decode(token).roles`
2. Verify endpoint requires expected role
3. Contact tenant admin if role should be granted

### CORS Errors

**Cause:** Frontend domain not allowed by backend

**Solution:**
1. Check browser console for `Access-Control-Allow-Origin` header
2. Verify `VITE_API_BASE_URL` matches backend CORS config
3. Contact backend team to whitelist domain

---

## References

| Aspect | Link |
|---|---|
| **API Endpoints** | `docs/design/api/ENDPOINT_CATALOG.md` |
| **Authorization** | `docs/design/AUTHORIZATION_PATTERNS.md` |
| **OAuth2 Flows** | `docs/design/OAUTH2_MULTIDOMAIN_CONTRACT.md` |
| **Error Codes** | `docs/design/api/ERROR_CATALOG.md` |
| **API Versioning** | `docs/design/API_VERSIONING_STRATEGY.md` |

---

**Última actualización:** 2026-04-10  
**Estado:** Completado para Sprint 3  
**Próxima revisión:** Cuando se libere API v2
