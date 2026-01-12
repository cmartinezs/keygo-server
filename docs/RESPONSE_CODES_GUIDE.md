# Guía de Códigos de Respuesta / Response Codes Guide

## Filosofía / Philosophy

Los códigos de respuesta en KeyGo Server son **códigos de negocio/endpoint específicos**, no duplican la semántica HTTP:

Response codes in KeyGo Server are **business/endpoint-specific codes**, they don't duplicate HTTP semantics:

### ❌ Mal / Wrong
```json
{
  "date": "2026-01-12T00:59:00",
  "success": {
    "code": "SUCCESS",           // ❌ Redundante con el campo "success"
    "message": "Operation successful"
  },
  "data": { ... }
}
```

```json
{
  "date": "2026-01-12T00:59:00",
  "failure": {
    "code": "BAD_REQUEST",       // ❌ Redundante con HTTP 400 Bad Request
    "message": "Invalid data"
  }
}
```

### ✅ Bien / Correct
```json
{
  "date": "2026-01-12T00:59:00",
  "success": {
    "code": "SERVICE_INFO_RETRIEVED",  // ✅ Específico del endpoint
    "message": "Service information retrieved successfully"
  },
  "data": {
    "name": "keygo-server",
    "version": "1.0-SNAPSHOT"
  }
}
```

```json
{
  "date": "2026-01-12T00:59:00",
  "failure": {
    "code": "RESOURCE_NOT_FOUND",      // ✅ Código de negocio
    "message": "User with ID 123 not found"
  }
}
```

## Estructura de Respuesta / Response Structure

### BaseResponse
```java
public class BaseResponse<T> {
  private LocalDateTime date;        // Timestamp de la respuesta
  private MessageResponse success;   // Mensaje de éxito (si aplica)
  private MessageResponse failure;   // Mensaje de error (si aplica)
  private T data;                    // Datos de respuesta (si aplica)
  private MessageResponse debug;     // Debug info (solo dev)
  private String throwable;          // Stack trace (solo dev)
}
```

### MessageResponse
```java
public class MessageResponse {
  private String code;               // Código de negocio
  private String message;            // Mensaje descriptivo
}
```

## Uso de ResponseHelper

### Crear mensaje de respuesta simple
```java
// Con mensaje por defecto del enum
MessageResponse msg = ResponseHelper.message(ResponseCode.RESOURCE_CREATED);

// Con mensaje personalizado
MessageResponse msg = ResponseHelper.message(
    ResponseCode.RESOURCE_CREATED, 
    "Usuario creado con ID: 123"
);

// Con código custom (raro, preferir usar enum)
MessageResponse msg = ResponseHelper.message("CUSTOM_CODE", "Custom message");
```

### Ejemplo de respuesta exitosa
```java
@GetMapping("/{id}")
public ResponseEntity<BaseResponse<User>> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    
    BaseResponse<User> response = BaseResponse.<User>builder()
        .data(user)
        .success(ResponseHelper.message(ResponseCode.RESOURCE_RETRIEVED))
        .build();
    
    return ResponseEntity.ok(response);
}
```

### Ejemplo de respuesta con error
```java
@PostMapping
public ResponseEntity<BaseResponse<User>> createUser(@RequestBody UserDto dto) {
    if (userService.existsByEmail(dto.getEmail())) {
        BaseResponse<User> response = BaseResponse.<User>builder()
            .failure(ResponseHelper.message(
                ResponseCode.DUPLICATE_RESOURCE,
                "User with email " + dto.getEmail() + " already exists"
            ))
            .build();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    User user = userService.create(dto);
    
    BaseResponse<User> response = BaseResponse.<User>builder()
        .data(user)
        .success(ResponseHelper.message(ResponseCode.RESOURCE_CREATED))
        .build();
    
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

## Catálogo de Códigos / Code Catalog

### Operaciones de Sistema / System Operations
| Código | Uso | HTTP Status Típico |
|--------|-----|-------------------|
| `SERVICE_INFO_RETRIEVED` | Info del servicio obtenida | 200 OK |
| `RESPONSE_CODES_RETRIEVED` | Catálogo de códigos obtenido | 200 OK |

### Operaciones Genéricas / Generic Operations
| Código | Uso | HTTP Status Típico |
|--------|-----|-------------------|
| `OPERATION_COMPLETED` | Operación genérica exitosa | 200 OK |
| `RESOURCE_CREATED` | Recurso creado | 201 Created |
| `RESOURCE_UPDATED` | Recurso actualizado | 200 OK |
| `RESOURCE_DELETED` | Recurso eliminado | 200 OK o 204 No Content |
| `RESOURCE_RETRIEVED` | Recurso obtenido | 200 OK |

### Errores de Validación / Validation Errors
| Código | Uso | HTTP Status Típico |
|--------|-----|-------------------|
| `INVALID_INPUT` | Datos de entrada inválidos | 400 Bad Request |
| `REQUIRED_FIELD_MISSING` | Campo requerido falta | 400 Bad Request |
| `INVALID_DATA_FORMAT` | Formato de dato inválido | 400 Bad Request |
| `BUSINESS_RULE_VIOLATION` | Violación de regla de negocio | 400 Bad Request |
| `DUPLICATE_RESOURCE` | Recurso duplicado | 409 Conflict |

### Errores de Recursos / Resource Errors
| Código | Uso | HTTP Status Típico |
|--------|-----|-------------------|
| `RESOURCE_NOT_FOUND` | Recurso no encontrado | 404 Not Found |
| `RESOURCE_UNAVAILABLE` | Recurso no disponible temporalmente | 503 Service Unavailable |

### Errores de Autorización / Authorization Errors
| Código | Uso | HTTP Status Típico |
|--------|-----|-------------------|
| `INSUFFICIENT_PERMISSIONS` | Permisos insuficientes | 403 Forbidden |
| `AUTHENTICATION_REQUIRED` | Se requiere autenticación | 401 Unauthorized |

### Errores de Sistema / System Errors
| Código | Uso | HTTP Status Típico |
|--------|-----|-------------------|
| `OPERATION_FAILED` | Operación falló | 500 Internal Server Error |
| `EXTERNAL_SERVICE_ERROR` | Error en servicio externo | 502 Bad Gateway o 503 |
| `DATABASE_ERROR` | Error de base de datos | 500 Internal Server Error |

## Agregar Nuevos Códigos / Adding New Codes

### Paso 1: Agregar al enum ResponseCode
```java
@Getter
@RequiredArgsConstructor
public enum ResponseCode {
  // ... existing codes ...
  
  // User Operations
  USER_CREATED("USER_CREATED", "User created successfully"),
  USER_PROFILE_UPDATED("USER_PROFILE_UPDATED", "User profile updated successfully"),
  USER_PASSWORD_CHANGED("USER_PASSWORD_CHANGED", "Password changed successfully"),
  USER_EMAIL_ALREADY_EXISTS("USER_EMAIL_ALREADY_EXISTS", "Email address is already registered"),
  USER_INVALID_CREDENTIALS("USER_INVALID_CREDENTIALS", "Invalid username or password"),
  
  private final String code;
  private final String message;
}
```

### Paso 2: Usar en el controlador
```java
@PostMapping("/register")
public ResponseEntity<BaseResponse<UserResponse>> register(@RequestBody RegisterDto dto) {
    BaseResponse<UserResponse> response = BaseResponse.<UserResponse>builder()
        .data(userResponse)
        .success(ResponseHelper.message(ResponseCode.USER_CREATED))
        .build();
    
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

@PostMapping("/login")
public ResponseEntity<BaseResponse<LoginResponse>> login(@RequestBody LoginDto dto) {
    // Si falla la autenticación
    BaseResponse<LoginResponse> response = BaseResponse.<LoginResponse>builder()
        .failure(ResponseHelper.message(ResponseCode.USER_INVALID_CREDENTIALS))
        .build();
    
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
}
```

## Buenas Prácticas / Best Practices

### ✅ DO
1. **Usar códigos específicos del dominio/endpoint**
   ```java
   ResponseCode.USER_PROFILE_UPDATED
   ResponseCode.ORDER_PLACED
   ResponseCode.PAYMENT_PROCESSED
   ```

2. **Agrupar códigos por módulo/feature**
   ```java
   // User module
   USER_CREATED, USER_UPDATED, USER_DELETED
   
   // Order module
   ORDER_PLACED, ORDER_CONFIRMED, ORDER_SHIPPED
   ```

3. **Nombres descriptivos en pasado participio (para success)**
   ```java
   RESOURCE_CREATED (not CREATE_RESOURCE)
   USER_AUTHENTICATED (not AUTHENTICATE_USER)
   ```

4. **Mensajes personalizados cuando sea útil**
   ```java
   ResponseHelper.message(
       ResponseCode.RESOURCE_NOT_FOUND, 
       "Product with SKU " + sku + " not found in inventory"
   )
   ```

### ❌ DON'T
1. **No duplicar semántica HTTP**
   ```java
   ❌ ResponseCode.OK
   ❌ ResponseCode.BAD_REQUEST
   ❌ ResponseCode.INTERNAL_SERVER_ERROR
   ```

2. **No usar nombres genéricos como SUCCESS**
   ```java
   ❌ ResponseCode.SUCCESS
   ✅ ResponseCode.OPERATION_COMPLETED
   ✅ ResponseCode.USER_CREATED
   ```

3. **No mezclar idiomas**
   ```java
   ❌ ResponseCode.USUARIO_CREADO
   ✅ ResponseCode.USER_CREATED (inglés en código)
   ```

4. **No hardcodear strings en controllers**
   ```java
   ❌ ResponseHelper.message("USER_NOT_FOUND", "User not found")
   ✅ ResponseHelper.message(ResponseCode.RESOURCE_NOT_FOUND)
   ```

## Endpoint de Catálogo / Catalog Endpoint

Para ver todos los códigos disponibles:
```bash
GET /api/v1/response-codes
```

Respuesta:
```json
{
  "date": "2026-01-12T00:59:00",
  "success": {
    "code": "RESPONSE_CODES_RETRIEVED",
    "message": "Response codes catalog retrieved successfully"
  },
  "data": {
    "successCodes": [
      {
        "code": "SERVICE_INFO_RETRIEVED",
        "message": "Service information retrieved successfully",
        "type": "SUCCESS"
      },
      // ...
    ],
    "failureCodes": [
      {
        "code": "INVALID_INPUT",
        "message": "Invalid input data provided",
        "type": "FAILURE"
      },
      // ...
    ]
  }
}
```

## Migración de Códigos Antiguos / Migration from Old Codes

| Código Antiguo | Nuevo Código | Razón |
|----------------|--------------|-------|
| `SUCCESS` | `OPERATION_COMPLETED` o específico | Más descriptivo |
| `CREATED` | `RESOURCE_CREATED` | Claridad |
| `BAD_REQUEST` | `INVALID_INPUT` | No duplicar HTTP |
| `UNAUTHORIZED` | `AUTHENTICATION_REQUIRED` | Más claro |
| `FORBIDDEN` | `INSUFFICIENT_PERMISSIONS` | Más descriptivo |
| `NOT_FOUND` | `RESOURCE_NOT_FOUND` | Claridad |
| `SERVICE_INFO_SUCCESS` | `SERVICE_INFO_RETRIEVED` | Consistencia |

---

**Nota:** Este diseño separa claramente:
- **HTTP Status Code**: Estado del transporte/protocolo (200, 400, 500)
- **Business Code**: Código específico de la operación de negocio
- **Success/Failure field**: Indica si la operación fue exitosa o no

