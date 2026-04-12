# Setup — Configuración del entorno frontend

## Variables de entorno

```bash
# .env.local
VITE_API_BASE_URL=http://localhost:8080/keygo-server/api/v1
VITE_OAUTH_CLIENT_ID=my-web-app
VITE_OAUTH_REDIRECT_URI=http://localhost:3000/auth/callback
VITE_OAUTH_ISSUER=http://localhost:8080/keygo-server
```

> `context-path` del backend: `/keygo-server`. Todos los paths de API incluyen este prefijo.

## Dependencias recomendadas

```bash
npm install axios @tanstack/react-query zustand pkce-gen
```

| Paquete | Propósito |
|---|---|
| `axios` | Cliente HTTP con soporte a interceptores |
| `@tanstack/react-query` | Fetching, caché e invalidación de datos |
| `zustand` | Estado global liviano (auth store) |
| `pkce-gen` | Generación de `code_verifier` y `code_challenge` PKCE |

## Cliente HTTP base

```typescript
// src/services/api.ts
import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { useAuthStore } from '../store/auth';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Adjuntar token en cada request
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().accessToken;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Auto-refresh en 401
apiClient.interceptors.response.use(
  (res) => res,
  async (error: AxiosError) => {
    const req = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    if (error.response?.status === 401 && !req._retry) {
      req._retry = true;
      try {
        await useAuthStore.getState().refreshAccessToken();
        req.headers.Authorization = `Bearer ${useAuthStore.getState().accessToken}`;
        return apiClient(req);
      } catch {
        useAuthStore.getState().logout();
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

## Headers recomendados

```typescript
headers: {
  'X-Trace-ID': crypto.randomUUID(), // correlación con logs del backend
}
```

El backend responde con `X-Trace-ID` en cada response. Usar ese valor al reportar errores.
