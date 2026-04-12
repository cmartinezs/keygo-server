# Convenciones de la API

## Envelope de respuesta

Todos los endpoints (excepto los RFC/OIDC nativos) responden con `BaseResponse<T>`:

```typescript
interface BaseResponse<T> {
  success: boolean;
  data: T | null;
  error: ErrorData | null;
  timestamp: string; // ISO-8601
}
```

Ejemplo exitoso:
```json
{
  "success": true,
  "data": { "userId": "abc-123", "email": "user@example.com" },
  "error": null,
  "timestamp": "2026-04-12T10:00:00"
}
```

Ejemplo con error:
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "User not found",
    "traceId": "550e8400-e29b-41d4-a716-446655440000"
  },
  "timestamp": "2026-04-12T10:00:00"
}
```

> Endpoints que **no** usan este envelope: `POST /oauth2/token`, `POST /oauth2/revoke`, `GET /oauth2/userinfo`, `GET /.well-known/jwks.json`, `GET /.well-known/openid-configuration`.

## Paginación

La API usa paginación offset (Spring Data `Page`):

```
GET /api/v1/tenants/{slug}/users?page=0&size=20&sort=createdAt&order=DESC
```

| Parámetro | Tipo | Default | Descripción |
|---|---|---|---|
| `page` | `int` | `0` | Número de página (base 0) |
| `size` | `int` | `20` | Elementos por página |
| `sort` | `string` | varía por endpoint | Campo de ordenamiento |
| `order` | `ASC\|DESC` | `DESC` | Dirección |

Respuesta paginada (`PagedData<T>`):

```typescript
interface PagedData<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
```

Hook React Query con paginación:
```typescript
export function useUsers(slug: string, page = 0, size = 20) {
  return useQuery(
    ['users', slug, page, size],
    () => apiClient
      .get(`/tenants/${slug}/users`, { params: { page, size } })
      .then(r => r.data.data as PagedData<User>),
    { keepPreviousData: true }
  );
}
```

## Filtros disponibles (por recurso)

| Recurso | Filtros |
|---|---|
| Tenants | `status`, `name_like` |
| Users | `status`, `email_like` |
| Apps (ClientApp) | `status`, `name_like` |
| Memberships | `status` |
| Roles | `name_like` |

## Headers

### Request

| Header | Cuándo usarlo |
|---|---|
| `Authorization: Bearer <token>` | Todos los endpoints protegidos |
| `X-Trace-ID: <uuid>` | Opcional; el backend lo genera si no se envía |
| `Accept-Language: es, en;q=0.9` | Para recibir `clientMessage` en el idioma correcto |

### Response

| Header | Contenido |
|---|---|
| `X-Trace-ID` | UUID de trazabilidad; usar en reportes de error |

## Utility: extraer datos de la respuesta

```typescript
// src/services/api.ts
export function extractData<T>(response: AxiosResponse<BaseResponse<T>>): T {
  if (!response.data.success || response.data.data === null) {
    throw new Error(response.data.error?.code ?? 'UNKNOWN_ERROR');
  }
  return response.data.data;
}

// Uso:
const user = extractData(await apiClient.get(`/tenants/${slug}/users/${id}`));
```
