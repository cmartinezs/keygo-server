# Service Info Endpoint - Refactorización con ResponseEntity y BaseResponse

## 🔄 Cambios Realizados

Se ha refactorizado el controller `ServiceInfoController` para cumplir con los siguientes requisitos:

1. ✅ **Un solo endpoint** - Eliminado endpoint duplicado
2. ✅ **BaseResponse** - Todos los endpoints retornan `BaseResponse<T>`
3. ✅ **ResponseEntity** - Envuelto en `ResponseEntity` para manejar códigos HTTP

## 📋 Antes vs Después

### ❌ Antes (Múltiples endpoints, sin estándar)

```java
@GetMapping("/info")
public ServiceInfoResponse getServiceInfo() {
    // Retorna Record simple
    return new ServiceInfoResponse(title, name, version);
}

@GetMapping("/info-detailed")  
public BaseResponse<ServiceInfoData> getServiceInfoDetailed() {
    // Retorna BaseResponse sin ResponseEntity
    return BaseResponse.builder()...build();
}
```

**Problemas:**
- Dos endpoints para lo mismo
- Respuestas inconsistentes
- No se puede controlar código HTTP
- Un endpoint usa Record, otro BaseResponse

### ✅ Después (Un solo endpoint, estandarizado)

```java
@GetMapping("/info")
public ResponseEntity<BaseResponse<ServiceInfoData>> getServiceInfo() {
    ServiceInfoProvider info = getServiceInfoUseCase.execute();

    ServiceInfoData data = ServiceInfoData.builder()
        .title(info.getTitle())
        .name(info.getName())
        .version(info.getVersion())
        .build();

    BaseResponse<ServiceInfoData> response = BaseResponse.<ServiceInfoData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.SERVICE_INFO_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
}
```

**Ventajas:**
- Un solo endpoint
- Respuesta estandarizada con `BaseResponse`
- Control total del código HTTP con `ResponseEntity`
- Código de negocio específico del endpoint vía `ResponseCode`
- Consistente con el resto de la API

## 📦 Estructura de la Respuesta

```json
{
  "date": "2026-01-11T21:15:17.983462425",
  "success": {
    "code": "SERVICE_INFO_RETRIEVED",
    "message": "Service information retrieved successfully"
  },
  "data": {
    "title": "KeyGo Server",
    "name": "keygo-server",
    "version": "1.0-SNAPSHOT"
  },
  "failure": null,
  "debug": null,
  "throwable": null
}
```

### Campos de BaseResponse

| Campo | Tipo | Descripción | Cuándo se usa |
|-------|------|-------------|---------------|
| `date` | `LocalDateTime` | Timestamp de la respuesta | Siempre (por defecto) |
| `success` | `MessageResponse` | Mensaje de éxito | Cuando operación exitosa |
| `failure` | `MessageResponse` | Mensaje de error | Cuando hay error |
| `data` | `T` (genérico) | Datos de respuesta | Cuando hay datos |
| `debug` | `MessageResponse` | Info de depuración | Solo en modo debug |
| `throwable` | `String` | Stack trace | Solo en errores |

## 🎯 Beneficios de ResponseEntity

### 1. Control de Código HTTP

```java
// OK (200)
return ResponseEntity.status(HttpStatus.OK).body(response);

// Created (201)
return ResponseEntity.status(HttpStatus.CREATED).body(response);

// No Content (204)
return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

// Bad Request (400)
return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

// Not Found (404)
return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

// Internal Server Error (500)
return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
```

### 2. Control de Headers

```java
return ResponseEntity
    .status(HttpStatus.OK)
    .header("X-Custom-Header", "value")
    .body(response);
```

### 3. Manejo de Errores Estandarizado

```java
@ExceptionHandler(SomeException.class)
public ResponseEntity<BaseResponse<Void>> handleException(SomeException ex) {
    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.OPERATION_FAILED, ex.getMessage()))
        .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
}
```

## 📝 Archivos Modificados

### 1. ServiceInfoController.java ✅
- Eliminado endpoint `/info-detailed`
- Modificado `/info` para retornar `ResponseEntity<BaseResponse<ServiceInfoData>>`
- Agregado import de `ResponseEntity` y `HttpStatus`

### 2. ServiceInfoResponse.java ❌ (Eliminado)
- Ya no se necesita, usamos `BaseResponse` con `ServiceInfoData`

### 3. ServiceInfoData.java ✅ (Ya existía)
- DTO con Lombok para los datos específicos del servicio

## 🧪 Pruebas

### Test con curl

```bash
# Request (requiere header bajo /api/)
curl -i http://localhost:8080/keygo-server/api/v1/service/info \
  -H "X-KEYGO-ADMIN: $KEYGO_ADMIN_KEY"

# Response Headers
HTTP/1.1 200 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Mon, 12 Jan 2026 00:22:50 GMT

# Response Body
{
  "date": "2026-01-11T21:15:17.983462425",
  "success": {
    "code": "SERVICE_INFO_RETRIEVED",
    "message": "Service information retrieved successfully"
  },
  "data": {
    "title": "KeyGo Server",
    "name": "keygo-server",
    "version": "1.0-SNAPSHOT"
  }
}
```

### Test Script

```bash
# Ejecutar script de prueba
./test-service-info.sh
```

## 🏗️ Patrón para Futuros Endpoints

### Template para Operaciones Exitosas

```java
@GetMapping("/example")
public ResponseEntity<BaseResponse<YourDataType>> exampleEndpoint() {
    // 1. Ejecutar caso de uso
    YourDataType data = yourUseCase.execute();

    // 2. Construir respuesta con ResponseCode específico del endpoint
    BaseResponse<YourDataType> response = BaseResponse.<YourDataType>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.RESOURCE_RETRIEVED))
        .build();

    // 3. Retornar con código HTTP
    return ResponseEntity.status(HttpStatus.OK).body(response);
}
```

### Template para Operaciones con Error

```java
@PostMapping("/example")
public ResponseEntity<BaseResponse<YourDataType>> exampleWithValidation(@RequestBody Request req) {
    // Validar
    if (!isValid(req)) {
        BaseResponse<YourDataType> response = BaseResponse.<YourDataType>builder()
            .failure(ResponseHelper.message(ResponseCode.INVALID_INPUT,
                     "Invalid request: campo X requerido"))
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Procesar
    YourDataType data = yourUseCase.execute(req);

    BaseResponse<YourDataType> response = BaseResponse.<YourDataType>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.RESOURCE_CREATED))
        .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

> 💡 Siempre usa códigos del enum `ResponseCode`. Si el código aún no existe, agrégalo antes de usarlo.
> Consulta la guía completa en [`RESPONSE_CODES_GUIDE.md`](RESPONSE_CODES_GUIDE.md).

## ✅ Checklist para Nuevos Endpoints

- [ ] Retorna `ResponseEntity<BaseResponse<T>>`
- [ ] Usa código HTTP apropiado (`HttpStatus`)
- [ ] Incluye `MessageResponse` en `success` o `failure`
- [ ] El tipo genérico `T` es un DTO claro (no primitivos)
- [ ] Maneja errores con códigos HTTP correctos
- [ ] Documentado con JavaDoc
- [ ] Tests unitarios incluidos

## 🎓 Buenas Prácticas

### ✅ DO (Hacer)

```java
// Usar ResponseEntity con BaseResponse
return ResponseEntity.ok(baseResponse);

// Códigos HTTP semánticos
return ResponseEntity.status(HttpStatus.CREATED).body(response);

// Usar ResponseHelper con ResponseCode del enum
ResponseHelper.message(ResponseCode.RESOURCE_CREATED)
ResponseHelper.message(ResponseCode.USER_CREATED, "User johndoe created")
```

### ❌ DON'T (No hacer)

```java
// No retornar DTOs directamente
return new SomeDTO(); // ❌

// No usar siempre 200 OK para errores
return ResponseEntity.ok(errorResponse); // ❌

// No hardcodear strings como código
ResponseHelper.message("CUSTOM_CODE", "msg") // ❌ si ya existe en el enum

// No mensajes genéricos sin ResponseCode
MessageResponse.builder().code("SUCCESS").build() // ❌ usar ResponseCode
```

## 📊 Resumen

| Aspecto | Antes | Después |
|---------|-------|---------|
| **Endpoints** | 2 endpoints | 1 endpoint ✅ |
| **Tipo retorno** | Record/BaseResponse | ResponseEntity<BaseResponse<T>> ✅ |
| **Control HTTP** | No | Sí ✅ |
| **Estandarización** | Inconsistente | Consistente ✅ |
| **Manejo errores** | Limitado | Completo ✅ |

## 🚀 Próximos Pasos

1. ✅ Aplicar este patrón a todos los futuros endpoints
2. ✅ Crear `@ExceptionHandler` global para manejo centralizado de errores
3. ✅ Agregar validación de datos con `@Valid` y `ConstraintViolation`
4. ✅ Implementar logging de requests/responses
5. ✅ Agregar documentación OpenAPI/Swagger

## 🎉 Conclusión

El endpoint de información del servicio ahora sigue el estándar:
- ✅ Un solo endpoint claro
- ✅ `ResponseEntity<BaseResponse<T>>` siempre
- ✅ Control total del código HTTP
- ✅ Respuestas consistentes y predecibles
- ✅ Listo para manejar errores adecuadamente

¡Perfecto para arquitectura empresarial! 🏗️

