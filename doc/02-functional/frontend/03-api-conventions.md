# Convenciones de la API

## Naming JSON

- Los atributos de **request body** y **response body** deben escribirse en `snake_case`.
- No introducir claves JSON en `camelCase` en contratos nuevos o al modificar contratos existentes.
- Si existe drift entre contratos documentados/implementados y esta regla, debe corregirse y
  dejarse trazabilidad en una tarea técnica.

## Envelope de respuesta

Todos los endpoints (excepto los RFC/OIDC nativos) responden con `BaseResponse<T>`:

```typescript
interface BaseResponse<T> {
  success: {
    code: string;
    message: string;
  } | null;
  data: T | null;
  failure: {
    code: string;
    message: string;
  } | null;
  date: string; // ISO-8601
}
```

Ejemplo exitoso:
```json
{
  "success": {
    "code": "USER_RETRIEVED",
    "message": "User retrieved successfully"
  },
  "data": { "user_id": "abc-123", "email": "user@example.com" },
  "failure": null,
  "date": "2026-04-12T10:00:00"
}
```

Ejemplo con error:
```json
{
  "success": null,
  "data": null,
  "failure": {
    "code": "USER_NOT_FOUND",
    "message": "User not found"
  },
  "date": "2026-04-12T10:00:00"
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
  total_elements: number;
  total_pages: number;
  last: boolean;
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
