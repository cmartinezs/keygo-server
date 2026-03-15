# Arquitectura de KeyGo Server

## Visión General

KeyGo Server está construido siguiendo los principios de **Arquitectura Hexagonal** (también conocida como Ports & Adapters), lo que permite mantener la lógica de negocio independiente de frameworks y tecnologías específicas.

## Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                        keygo-run                            │
│              (Configuración y Arranque)                     │
└─────────────────────────┬───────────────────────────────────┘
                          │
          ┌───────────────┴───────────────┐
          │                               │
┌─────────▼──────────┐          ┌─────────▼─────────┐
│     keygo-api      │          │    keygo-infra    │
│  (REST Controllers)│          │ (Implementaciones)│
│                    │          │  - Persistencia   │
│  - Endpoints       │          │  - APIs externas  │
│  - DTOs            │          │  - Seguridad      │
└─────────┬──────────┘          └─────────┬─────────┘
          │                               │
          └───────────────┬───────────────┘
                          │
                 ┌────────▼─────────┐
                 │    keygo-app     │
                 │  (Casos de Uso)  │
                 │                  │
                 │  - Servicios     │
                 │  - Puertos       │
                 └────────┬─────────┘
                          │
                 ┌────────▼─────────┐
                 │   keygo-domain   │
                 │  (Core Business) │
                 │                  │
                 │  - Entidades     │
                 │  - Value Objects │
                 │  - Reglas        │
                 └────────┬─────────┘
                          │
                 ┌────────▼─────────┐
                 │   keygo-common   │
                 │   (Utilidades)   │
                 └──────────────────┘

                 ┌──────────────────┐
                 │    keygo-bom     │
                 │  (Dependencies)  │
                 └──────────────────┘
```

## Módulos

### keygo-domain (Núcleo)
**Propósito**: Contiene la lógica de negocio pura sin dependencias externas.

**Contenido:**
- **Entidades**: Objetos de negocio con identidad (User, Application, Service, etc.)
- **Value Objects**: Objetos inmutables (Email, Password, Token, etc.)
- **Reglas de Negocio**: Validaciones y lógica del dominio
- **Eventos de Dominio**: Eventos que ocurren en el negocio

**Principios:**
- ❌ Sin dependencias de frameworks
- ❌ Sin anotaciones de infraestructura
- ✅ Java puro
- ✅ Independiente de tecnología

### keygo-app (Aplicación)
**Propósito**: Orquesta los casos de uso del sistema.

**Contenido:**
- **Casos de Uso**: Servicios de aplicación que implementan funcionalidades
- **Puertos (Interfaces)**: Contratos para entrada y salida
  - Puertos de entrada (Use Cases)
  - Puertos de salida (Repositories, Services)
- **DTOs internos**: Objetos de transferencia entre capas

**Dependencias:**
- ✅ keygo-domain

### keygo-infra (Infraestructura)
**Propósito**: Implementaciones técnicas y adaptadores.

**Contenido:**
- **Adaptadores de Persistencia**: Implementaciones de repositorios
  - JPA/Hibernate
  - Base de datos
- **Adaptadores de Seguridad**: JWT, OAuth2, etc.
- **Adaptadores de APIs Externas**: Clientes HTTP, SMTP, etc.
- **Configuración**: Beans de Spring, configuraciones

**Dependencias:**
- ✅ keygo-app (implementa los puertos)
- ✅ keygo-domain

### keygo-api (API REST)
**Propósito**: Exponer la funcionalidad via REST API.

**Contenido:**
- **Controllers**: Endpoints REST
- **DTOs de API**: Request/Response objects
- **Mappers**: Conversión entre DTOs de API y objetos de dominio
- **Validaciones**: Bean Validation
- **Exception Handlers**: Manejo de errores HTTP

**Dependencias:**
- ✅ keygo-app (invoca casos de uso)

### keygo-common (Común)
**Propósito**: Utilidades compartidas entre módulos.

**Contenido:**
- **Excepciones base**: Jerarquía de excepciones
- **Utilidades**: Helpers, constantes
- **Anotaciones**: Anotaciones personalizadas
- **Interfaces genéricas**: Contratos comunes

**Dependencias:**
- ❌ Sin dependencias de otros módulos

### keygo-bom (Bill of Materials)
**Propósito**: Gestión centralizada de versiones de dependencias.

**Contenido:**
- **Dependencias versionadas**: Spring Boot, Libraries, etc.
- **Plugin management**: Versiones de plugins Maven

### keygo-run (Ejecución)
**Propósito**: Punto de entrada y configuración de la aplicación.

**Contenido:**
- **Main class**: Clase principal de Spring Boot
- **Configuración de beans**: Wiring de dependencias
- **Application properties**: Configuraciones del entorno
- **Perfiles**: Dev, Test, Prod

**Dependencias:**
- ✅ keygo-api
- ✅ keygo-infra

## Flujo de una Request

```
1. HTTP Request
   ↓
2. keygo-api (Controller)
   - Valida entrada
   - Convierte DTO → Domain
   ↓
3. keygo-app (Use Case)
   - Ejecuta lógica de aplicación
   - Usa puertos
   ↓
4. keygo-domain (Business Logic)
   - Aplica reglas de negocio
   - Valida invariantes
   ↓
5. keygo-infra (Repository)
   - Persiste datos
   - Llama APIs externas
   ↓
6. Response hacia arriba
```

## Principios de Diseño

### 1. Dependency Rule
Las dependencias apuntan hacia adentro (hacia el dominio).
```
infra → app → domain
api → app → domain
```

### 2. Separation of Concerns
Cada módulo tiene una responsabilidad clara y única.

### 3. Testability
- Domain: 100% testeable con tests unitarios puros
- App: Testeable con mocks de puertos
- Infra/API: Tests de integración

### 4. Independence
- El dominio es independiente de frameworks
- La aplicación es independiente de tecnología
- Los adaptadores son intercambiables

## Tecnologías (Futuras)

- **Framework**: Spring Boot 3.x
- **Persistencia**: JPA/Hibernate + PostgreSQL
- **Seguridad**: Spring Security + JWT
- **API**: REST + OpenAPI/Swagger
- **Tests**: JUnit 5 + Mockito + Testcontainers
- **Build**: Maven

## Referencias

- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)

