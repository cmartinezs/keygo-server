# Refactorización de Códigos de Respuesta / Response Codes Refactoring

## Fecha / Date
2026-01-12

## Problema / Problem

Los códigos de respuesta anteriores duplicaban la semántica HTTP y el campo `success/failure` de `BaseResponse`:

Previous response codes duplicated HTTP semantics and the `success/failure` field in `BaseResponse`:

```json
// ❌ Problema: código "SUCCESS" es redundante con el campo "success"
{
  "success": {
    "code": "SUCCESS",
    "message": "..."
  }
}

// ❌ Problema: código "BAD_REQUEST" es redundante con HTTP 400
HTTP 400 Bad Request
{
  "failure": {
    "code": "BAD_REQUEST",
    "message": "..."
  }
}
```

## Solución / Solution

Los códigos de respuesta ahora son **específicos del dominio/endpoint**, no duplican HTTP ni el campo success/failure:

Response codes are now **domain/endpoint-specific**, don't duplicate HTTP or success/failure field:

```json
// ✅ Solución: código específico del endpoint
{
  "success": {
    "code": "SERVICE_INFO_RETRIEVED",
    "message": "Service information retrieved successfully"
  }
}

// ✅ Solución: código de negocio, no HTTP
HTTP 404 Not Found
{
  "failure": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "User with ID 123 not found"
  }
}
```

## Cambios Realizados / Changes Made

### 1. ResponseCode Enum Refactorizado

**Archivo:** `keygo-api/src/main/java/io/cmartinezs/keygo/api/constant/ResponseCode.java`

**Códigos eliminados:**
- ❌ `SUCCESS` → Genérico y redundante
- ❌ `CREATED` → Redundante con HTTP 201
- ❌ `UPDATED` → Redundante con HTTP 200
- ❌ `DELETED` → Redundante con HTTP 200/204
- ❌ `BAD_REQUEST` → Duplica HTTP 400
- ❌ `UNAUTHORIZED` → Duplica HTTP 401
- ❌ `FORBIDDEN` → Duplica HTTP 403
- ❌ `NOT_FOUND` → Duplica HTTP 404
- ❌ `CONFLICT` → Duplica HTTP 409
- ❌ `INTERNAL_ERROR` → Duplica HTTP 500
- ❌ `VALIDATION_ERROR` → Genérico
- ❌ `MISSING_REQUIRED_FIELD` → Ahora `REQUIRED_FIELD_MISSING`
- ❌ `INVALID_FORMAT` → Ahora `INVALID_DATA_FORMAT`
- ❌ `SERVICE_INFO_SUCCESS` → Ahora `SERVICE_INFO_RETRIEVED`

**Códigos nuevos (organizados por categoría):**

**System Operations:**
- ✅ `SERVICE_INFO_RETRIEVED`
- ✅ `RESPONSE_CODES_RETRIEVED`

**Generic Operations:**
- ✅ `OPERATION_COMPLETED`
- ✅ `RESOURCE_CREATED`
- ✅ `RESOURCE_UPDATED`
- ✅ `RESOURCE_DELETED`
- ✅ `RESOURCE_RETRIEVED`

**Validation Errors:**
- ✅ `INVALID_INPUT`
- ✅ `REQUIRED_FIELD_MISSING`
- ✅ `INVALID_DATA_FORMAT`
- ✅ `BUSINESS_RULE_VIOLATION`
- ✅ `DUPLICATE_RESOURCE`

**Resource Errors:**
- ✅ `RESOURCE_NOT_FOUND`
- ✅ `RESOURCE_UNAVAILABLE`

**Authorization Errors:**
- ✅ `INSUFFICIENT_PERMISSIONS`
- ✅ `AUTHENTICATION_REQUIRED`

**System Errors:**
- ✅ `OPERATION_FAILED`
- ✅ `EXTERNAL_SERVICE_ERROR`
- ✅ `DATABASE_ERROR`

### 2. ResponseHelper Simplificado

**Archivo:** `keygo-api/src/main/java/io/cmartinezs/keygo/api/helper/ResponseHelper.java`

**Métodos eliminados:**
- ❌ `success(ResponseCode)` - Confuso, implica que el código es "success"
- ❌ `success(ResponseCode, String)` - Confuso
- ❌ `failure(ResponseCode)` - Confuso, implica que el código es "failure"
- ❌ `failure(ResponseCode, String)` - Confuso

**Métodos nuevos:**
- ✅ `message(ResponseCode)` - Crea MessageResponse con mensaje por defecto
- ✅ `message(ResponseCode, String)` - Crea MessageResponse con mensaje custom
- ✅ `message(String, String)` - Crea MessageResponse con código custom

**Razón:** El helper solo crea `MessageResponse`, no determina si es success/failure. Eso lo determina el campo donde se coloca en `BaseResponse`.

### 3. Controladores Actualizados

**ServiceInfoController:**
```java
// ANTES
.success(ResponseHelper.success(ResponseCode.SERVICE_INFO_SUCCESS))

// DESPUÉS
.success(ResponseHelper.message(ResponseCode.SERVICE_INFO_RETRIEVED))
```

**ResponseCodeController:**
```java
// ANTES
.success(ResponseHelper.success(ResponseCode.SUCCESS))

// DESPUÉS
.success(ResponseHelper.message(ResponseCode.RESPONSE_CODES_RETRIEVED))
```

## Tabla de Migración / Migration Table

| Código Antiguo | Nuevo Código | Uso |
|----------------|--------------|-----|
| `SUCCESS` | `OPERATION_COMPLETED` | Operación genérica |
| `SUCCESS` | `RESOURCE_RETRIEVED` | GET endpoint |
| `CREATED` | `RESOURCE_CREATED` | POST endpoint |
| `UPDATED` | `RESOURCE_UPDATED` | PUT/PATCH endpoint |
| `DELETED` | `RESOURCE_DELETED` | DELETE endpoint |
| `SERVICE_INFO_SUCCESS` | `SERVICE_INFO_RETRIEVED` | /service/info |
| `BAD_REQUEST` | `INVALID_INPUT` | Validación de entrada |
| `UNAUTHORIZED` | `AUTHENTICATION_REQUIRED` | Sin autenticación |
| `FORBIDDEN` | `INSUFFICIENT_PERMISSIONS` | Sin permisos |
| `NOT_FOUND` | `RESOURCE_NOT_FOUND` | Recurso no existe |
| `CONFLICT` | `DUPLICATE_RESOURCE` | Recurso duplicado |
| `INTERNAL_ERROR` | `OPERATION_FAILED` | Error genérico |
| `VALIDATION_ERROR` | `BUSINESS_RULE_VIOLATION` | Validación negocio |
| `MISSING_REQUIRED_FIELD` | `REQUIRED_FIELD_MISSING` | Campo faltante |
| `INVALID_FORMAT` | `INVALID_DATA_FORMAT` | Formato inválido |

## Ejemplos de Uso / Usage Examples

### Respuesta Exitosa Simple
```java
BaseResponse<User> response = BaseResponse.<User>builder()
    .data(user)
    .success(ResponseHelper.message(ResponseCode.RESOURCE_RETRIEVED))
    .build();

return ResponseEntity.ok(response);
```

### Respuesta Exitosa con Mensaje Custom
```java
BaseResponse<Order> response = BaseResponse.<Order>builder()
    .data(order)
    .success(ResponseHelper.message(
        ResponseCode.RESOURCE_CREATED,
        "Order created with ID: " + order.getId()
    ))
    .build();

return ResponseEntity.status(HttpStatus.CREATED).body(response);
```

### Respuesta de Error
```java
BaseResponse<Void> response = BaseResponse.<Void>builder()
    .failure(ResponseHelper.message(
        ResponseCode.RESOURCE_NOT_FOUND,
        "User with email " + email + " not found"
    ))
    .build();

return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
```

### Respuesta de Validación
```java
BaseResponse<Void> response = BaseResponse.<Void>builder()
    .failure(ResponseHelper.message(ResponseCode.INVALID_INPUT))
    .build();

return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
```

## Estructura de JSON Resultante / Resulting JSON Structure

### Éxito con datos
```json
{
  "date": "2026-01-12T01:00:00",
  "success": {
    "code": "SERVICE_INFO_RETRIEVED",
    "message": "Service information retrieved successfully"
  },
  "data": {
    "name": "keygo-server",
    "version": "1.0-SNAPSHOT",
    "title": "KeyGo Server API"
  }
}
```

### Error sin datos
```json
{
  "date": "2026-01-12T01:00:00",
  "failure": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "User with ID 999 not found"
  }
}
```

## Beneficios / Benefits

### 1. ✅ Códigos Específicos del Dominio
Los códigos ahora describen la operación de negocio específica, no el resultado HTTP genérico.

### 2. ✅ Sin Redundancia
- HTTP status code: `404 Not Found`
- Business code: `RESOURCE_NOT_FOUND`
- Message: Detalles específicos del contexto

Cada nivel tiene su propósito sin duplicación.

### 3. ✅ Mejor Trazabilidad
Un cliente puede filtrar logs por código de negocio específico:
- `SERVICE_INFO_RETRIEVED`
- `USER_PROFILE_UPDATED`
- `ORDER_PAYMENT_PROCESSED`

### 4. ✅ Extensibilidad
Fácil agregar códigos específicos por módulo:
```java
// User module
USER_REGISTERED, USER_VERIFIED, USER_PASSWORD_RESET_REQUESTED

// Order module  
ORDER_PLACED, ORDER_PAID, ORDER_SHIPPED, ORDER_DELIVERED

// Payment module
PAYMENT_AUTHORIZED, PAYMENT_CAPTURED, PAYMENT_REFUNDED
```

### 5. ✅ Claridad en el API
El código de negocio es independiente del HTTP status:
- Mismo HTTP 400 puede tener diferentes códigos: `INVALID_INPUT`, `BUSINESS_RULE_VIOLATION`, `DUPLICATE_RESOURCE`
- Mismo código puede tener diferentes HTTP status según contexto

## Testing

### Verificar nuevos códigos
```bash
# Ver catálogo completo
curl http://localhost:8080/api/v1/response-codes | jq

# Ver info del servicio
curl http://localhost:8080/api/v1/service/info | jq
```

### Compilación
```bash
./mvnw clean compile
# BUILD SUCCESS
```

## Documentación / Documentation

- 📚 **Guía completa:** `docs/RESPONSE_CODES_GUIDE.md`
- 📝 **Este documento:** `docs/changes/RESPONSE_CODES_REFACTORING.md`

## Próximos Pasos / Next Steps

1. ✅ Códigos base definidos
2. ⏳ Agregar códigos específicos cuando se implementen módulos:
   - User management codes
   - Authentication codes
   - Business-specific codes
3. ⏳ Exception handling con mapeo automático a códigos
4. ⏳ Internacionalización de mensajes (i18n)

## Breaking Changes

⚠️ **Atención:** Este cambio puede romper clientes que dependan de códigos antiguos.

**Afectados:**
- Frontend/Mobile apps que busquen códigos específicos como "SUCCESS", "BAD_REQUEST"
- Tests que validen códigos específicos
- Documentación de API externa

**Recomendación:** Versionar el API (`/api/v2/`) si hay clientes externos que no se puedan actualizar inmediatamente.

