# Manejo de errores

## Estructura de error

```typescript
interface ErrorData {
  code: string;          // ResponseCode — identificador canónico del error
  message: string;       // Mensaje técnico (para logs)
  clientMessage?: string; // Mensaje localizado para mostrar al usuario
  traceId?: string;      // ID de trazabilidad (correlaciona con logs del backend)
  fieldErrors?: FieldError[]; // Presente en errores de validación (400)
  layer?: string;        // 'DOMAIN' | 'USE_CASE' | 'PORT' (diagnóstico)
}

interface FieldError {
  field: string;
  message: string;
}
```

## Códigos HTTP y su significado

| HTTP | Cuándo ocurre |
|---|---|
| `400` | Input inválido, parámetros faltantes, validación de negocio |
| `401` | Token ausente, expirado o inválido |
| `403` | Sin permisos para el recurso (rol, tenant scope) |
| `404` | Recurso no encontrado |
| `409` | Conflicto de estado (ej. usuario ya activo, email duplicado) |
| `422` | La entidad no pudo procesarse por reglas de dominio |
| `500` | Error inesperado del servidor |

## Utility: parseError

```typescript
// src/services/errorHandler.ts
import axios from 'axios';

export class ApiError extends Error {
  constructor(
    public readonly code: string,
    message: string,
    public readonly status: number,
    public readonly traceId?: string,
    public readonly fieldErrors?: FieldError[]
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export function parseError(error: unknown): ApiError {
  if (axios.isAxiosError(error)) {
    const res = error.response;
    const err = res?.data?.error;
    if (err) {
      return new ApiError(err.code, err.message, res!.status, err.traceId, err.fieldErrors);
    }
    return new ApiError('NETWORK_ERROR', res?.statusText ?? 'Network error', res?.status ?? 0);
  }
  return new ApiError('UNKNOWN_ERROR', String(error), 0);
}
```

## Códigos de respuesta frecuentes

| `code` | HTTP | Contexto |
|---|---|---|
| `USER_NOT_FOUND` | 404 | Usuario no existe en el tenant |
| `TENANT_NOT_FOUND` | 404 | Slug de tenant inválido |
| `DUPLICATE_EMAIL` | 409 | Email ya registrado |
| `MEMBERSHIP_NOT_FOUND` | 404 | Membership no existe |
| `MEMBERSHIP_ALREADY_ACTIVE` | 409 | Intento de aprobar una membresía ya activa |
| `MEMBERSHIP_PENDING` | 403 | Login bloqueado por membresía en estado PENDING |
| `INVALID_CREDENTIALS` | 401 | Credenciales incorrectas |
| `AUTHENTICATION_REQUIRED` | 401 | Sesión/token ausente, inválido o expirada |
| `USER_PASSWORD_RESET_REQUIRED` | 403 | Usuario debe cambiar contraseña antes de continuar |
| `PLATFORM_USER_SUSPENDED` | 403 | Usuario de plataforma suspendido |
| `PLATFORM_USER_EMAIL_FOUND` | 200 | Hosted login de plataforma: el email ya existe y la UI debe enviar al login |
| `PLATFORM_USER_EMAIL_NOT_FOUND` | 404 | Hosted login de plataforma: el email no existe y la UI puede continuar onboarding |
| `RESET_PASSWORD_REQUIRED` | 403 | Reset de contraseña requerido |
| `OPERATION_FAILED` | 500 | Error genérico del servidor |
| `INVALID_INPUT` | 400 | Parámetro de request inválido o faltante |

> Lista completa en el código: `keygo-api/.../response/ResponseCode.java`

## Errores de validación (400 con fieldErrors)

```json
{
  "success": false,
  "error": {
    "code": "INVALID_INPUT",
    "message": "Validation failed",
    "fieldErrors": [
      { "field": "email", "message": "must be a valid email address" },
      { "field": "username", "message": "must not be blank" }
    ]
  }
}
```

```typescript
function handleValidationError(error: ApiError, setErrors: (e: Record<string, string>) => void) {
  const map: Record<string, string> = {};
  error.fieldErrors?.forEach(fe => { map[fe.field] = fe.message; });
  setErrors(map);
}
```

## Internacionalización de mensajes

El backend resuelve `clientMessage` según el header `Accept-Language`. Enviar siempre:

```typescript
apiClient.defaults.headers.common['Accept-Language'] = navigator.language;
```

Si `clientMessage` está presente, mostrarlo directamente al usuario. Si no, usar el mapeo local como fallback:

```typescript
const fallbackMessages: Record<string, string> = {
  DUPLICATE_EMAIL: 'Este correo ya está registrado.',
  USER_NOT_FOUND: 'Usuario no encontrado.',
  INVALID_CREDENTIALS: 'Credenciales incorrectas.',
  // ...
};

export function getDisplayMessage(error: ApiError): string {
  return error.clientMessage ?? fallbackMessages[error.code] ?? 'Ha ocurrido un error inesperado.';
}
```

## Trazabilidad

Siempre incluir `traceId` al reportar errores:

```typescript
catch (err) {
  const apiError = parseError(err);
  Sentry.captureException(apiError, { tags: { traceId: apiError.traceId } });
  console.error(`[${apiError.traceId}] ${apiError.code}: ${apiError.message}`);
}
```

## Troubleshooting

### 401 Unauthorized
1. Verificar que el token existe en `sessionStorage`.
2. Decodificar el JWT y revisar `exp`: `new Date(payload.exp * 1000)`.
3. Si expiró, el cliente HTTP debería haberlo renovado automáticamente (ver `01-setup.md`).
4. Si el refresh también falla, redirigir a login.

### 403 Forbidden
1. Decodificar el access token y revisar `roles`.
2. El endpoint puede requerir un rol específico (`ADMIN`, `ADMIN_TENANT`).
3. Si `code = MEMBERSHIP_PENDING`: la membresía del usuario aún no fue aprobada.
4. Si `code = USER_PASSWORD_RESET_REQUIRED`: redirigir al flujo de cambio de contraseña.

### CORS
1. Verificar `VITE_API_BASE_URL` apunta al origen correcto.
2. El backend controla orígenes permitidos via `KEYGO_CORS_ALLOWED_ORIGINS_0`. Notificar al equipo de backend para whitelistear el dominio.

### 500 inesperado
1. Capturar y loggear el `traceId` del response.
2. Compartirlo con el equipo de backend para correlacionar con los logs del servidor.
