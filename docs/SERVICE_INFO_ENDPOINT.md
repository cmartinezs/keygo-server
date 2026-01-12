# Service Info Endpoint - Hexagonal Architecture Implementation

## Descripción / Description

Este documento describe la implementación del endpoint de información pública del servicio siguiendo la arquitectura hexagonal.

This document describes the implementation of the public service information endpoint following hexagonal architecture.

## Arquitectura / Architecture

La implementación sigue el patrón de arquitectura hexagonal (puertos y adaptadores):

```
┌─────────────────────────────────────────────────────────────────┐
│                         keygo-api (API Layer)                    │
│  ┌─────────────────────┐         ┌─────────────────────────┐   │
│  │ ServiceInfoController│ ───────▶│  ServiceInfoResponse    │   │
│  │  (REST Controller)   │         │      (DTO)              │   │
│  └──────────┬───────────┘         └─────────────────────────┘   │
└─────────────┼───────────────────────────────────────────────────┘
              │ uses
              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    keygo-app (Application Layer)                 │
│  ┌─────────────────────┐                                        │
│  │GetServiceInfoUseCase│                                        │
│  │   (Use Case)        │                                        │
│  └──────────┬───────────┘                                        │
│             │ depends on                                         │
│             ▼                                                    │
│  ┌─────────────────────┐                                        │
│  │ServiceInfoProvider  │  ◀── Interface (Port OUT)              │
│  │   (Interface)       │                                        │
│  └─────────────────────┘                                        │
└─────────────────────────────────────────────────────────────────┘
              ▲
              │ implements
              │
┌─────────────┼───────────────────────────────────────────────────┐
│             │              keygo-run (Infrastructure)            │
│  ┌──────────┴──────────────┐        ┌─────────────────────┐    │
│  │ ServiceInfoProperties   │  ◀─────│  application.yml    │    │
│  │ (@ConfigurationProperties)│       │  (Configuration)    │    │
│  │ implements ServiceInfoProvider    └─────────────────────┘    │
│  └─────────────────────────┘                                    │
│                                                                  │
│  ┌─────────────────────┐                                        │
│  │  ApplicationConfig  │  ◀── Spring Configuration              │
│  │  (@Configuration)   │      Bean definitions                  │
│  └─────────────────────┘                                        │
└─────────────────────────────────────────────────────────────────┘
```

## Componentes Implementados / Implemented Components

### 1. **keygo-app** - Application Layer (Domain Logic)

#### `ServiceInfoProvider` (Port OUT - Interface)
- **Ubicación / Location**: `keygo-app/src/main/java/io/cmartinezs/keygo/app/port/out/ServiceInfoProvider.java`
- **Propósito / Purpose**: Define el contrato para obtener información del servicio
- **Métodos / Methods**:
  - `String getTitle()` - Obtiene el título del servicio
  - `String getName()` - Obtiene el nombre del servicio
  - `String getVersion()` - Obtiene la versión del servicio

#### `GetServiceInfoUseCase` (Use Case)
- **Ubicación / Location**: `keygo-app/src/main/java/io/cmartinezs/keygo/app/usecase/GetServiceInfoUseCase.java`
- **Propósito / Purpose**: Caso de uso para obtener información del servicio
- **Dependencias / Dependencies**: `ServiceInfoProvider` (inyectado por constructor)

### 2. **keygo-api** - API Layer (REST Controllers)

#### `ServiceInfoResponse` (DTO)
- **Ubicación / Location**: `keygo-api/src/main/java/io/cmartinezs/keygo/api/dto/ServiceInfoResponse.java`
- **Tipo / Type**: Java Record
- **Campos / Fields**:
  - `String title`
  - `String name`
  - `String version`

#### `ServiceInfoController` (REST Controller)
- **Ubicación / Location**: `keygo-api/src/main/java/io/cmartinezs/keygo/api/controller/ServiceInfoController.java`
- **Mapping**: `@RequestMapping("/api/v1/service")`
- **Endpoint**: `GET /api/v1/service/info`
- **Respuesta / Response**: `ServiceInfoResponse` (JSON)
- **Dependencias / Dependencies**: `GetServiceInfoUseCase`

### 3. **keygo-run** - Infrastructure Layer (Configuration & Implementation)

#### `ServiceInfoProperties` (Adapter - Implementation)
- **Ubicación / Location**: `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/properties/ServiceInfoProperties.java`
- **Anotaciones / Annotations**:
  - `@Component` - Bean de Spring
  - `@ConfigurationProperties(prefix = "key-go-server.info")`
- **Implementa / Implements**: `ServiceInfoProvider`
- **Lee desde / Reads from**: `application.yml`

#### `ApplicationConfig` (Spring Configuration)
- **Ubicación / Location**: `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/ApplicationConfig.java`
- **Anotaciones / Annotations**:
  - `@Configuration`
  - `@ComponentScan(basePackages = "io.cmartinezs.keygo.api")`
- **Beans**:
  - `GetServiceInfoUseCase` - Inyecta `ServiceInfoProvider`

## Configuración / Configuration

### application.yml
```yaml
key-go-server:
  info:
    title: "@project.parent.name@"
    name: "@project.parent.artifactId@"
    version: "@project.parent.version@"
```

**Nota**: Los valores con `@...@` son reemplazados por Maven durante la compilación usando resource filtering.

**Note**: Values with `@...@` are replaced by Maven during compilation using resource filtering.

## Uso / Usage

### Compilar y Ejecutar / Build and Run

```bash
# Compilar el proyecto / Build the project
./mvnw clean package -DskipTests

# Ejecutar la aplicación / Run the application
java -jar keygo-run/target/keygo-run-1.0-SNAPSHOT.jar

# O especificar un puerto diferente / Or specify a different port
java -jar keygo-run/target/keygo-run-1.0-SNAPSHOT.jar --server.port=8081
```

### Probar el Endpoint / Test the Endpoint

```bash
# Usando curl
curl http://localhost:8080/keygo-server/api/v1/service/info

# Respuesta esperada / Expected response:
# {
#   "title": "KeyGo Server",
#   "name": "keygo-server",
#   "version": "1.0-SNAPSHOT"
# }
```

### Con navegador / With browser
```
http://localhost:8080/keygo-server/api/v1/service/info
```

## Ventajas de esta Arquitectura / Architecture Advantages

1. **Inversión de Dependencias / Dependency Inversion**: 
   - El controller solo depende de interfaces, no de implementaciones concretas
   - The controller only depends on interfaces, not concrete implementations

2. **Testabilidad / Testability**: 
   - Fácil de testear con mocks
   - Easy to test with mocks

3. **Flexibilidad / Flexibility**: 
   - Se puede cambiar la fuente de datos sin modificar el controller
   - Data source can be changed without modifying the controller

4. **Separación de Responsabilidades / Separation of Concerns**: 
   - Cada capa tiene una responsabilidad específica
   - Each layer has a specific responsibility

5. **Configuración Externa / External Configuration**: 
   - Usa `@ConfigurationProperties` para leer desde `application.yml`
   - Uses `@ConfigurationProperties` to read from `application.yml`

## Flujo de Datos / Data Flow

```
1. HTTP Request → ServiceInfoController
2. Controller → GetServiceInfoUseCase.execute()
3. UseCase → ServiceInfoProvider (interface)
4. ServiceInfoProperties (implementation) → application.yml values
5. Return values → UseCase → Controller
6. Controller → ServiceInfoResponse (DTO)
7. Spring → JSON serialization
8. HTTP Response (JSON)
```

## Próximos Pasos / Next Steps

- [ ] Agregar tests unitarios / Add unit tests
- [ ] Agregar documentación OpenAPI/Swagger
- [ ] Agregar validación de datos
- [ ] Agregar cache para mejorar performance
- [ ] Agregar métricas con Actuator

## Referencias / References

- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Spring Boot Configuration Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.typesafe-configuration-properties)
- [Maven Resource Filtering](https://maven.apache.org/plugins/maven-resources-plugin/examples/filter.html)

