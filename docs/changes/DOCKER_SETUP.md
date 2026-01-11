# Docker Configuration for KeyGo Server / Configuración Docker para KeyGo Server

[English](#english) | [Español](#español)

---

## English

### Summary

Docker support has been added to KeyGo Server with a multi-stage build configuration optimized for production use.

### Files Created

1. **Dockerfile** - Multi-stage build configuration
2. **.dockerignore** - Optimize build context
3. **docker-compose.yml** - Orchestration configuration
4. **docs/DOCKER.md** - Complete Docker documentation

### Dockerfile Features

#### Stage 1: Builder
- **Base Image:** `eclipse-temurin:21-jdk-alpine`
- **Purpose:** Build the application
- **Optimization:**
  - Maven dependency caching (separate layer)
  - Multi-module project support
  - JAR layer extraction

#### Stage 2: Runtime
- **Base Image:** `eclipse-temurin:21-jre-alpine` (smaller)
- **Features:**
  - Non-root user for security
  - Health check configured
  - Optimized JVM settings
  - Layered JAR approach

### Key Benefits

✅ **Multi-stage build** - Smaller final image (~250-300MB)
✅ **Layer caching** - Faster rebuilds (dependencies cached)
✅ **Security** - Non-root user, minimal base image
✅ **Health checks** - Built-in monitoring
✅ **Production-ready** - Resource limits, restart policies
✅ **Easy deployment** - Docker Compose included

### Image Layers

The final image uses Spring Boot's layered approach:
1. Dependencies (external libraries - rarely change)
2. Spring Boot Loader
3. Snapshot dependencies
4. Application code (changes frequently)

This maximizes Docker cache efficiency.

### Quick Start Commands

Build:
```bash
docker build -t keygo-server:1.0-SNAPSHOT .
```

Run:
```bash
docker-compose up -d
```

Logs:
```bash
docker-compose logs -f keygo-server
```

Stop:
```bash
docker-compose down
```

### Configuration

**Environment Variables:**
- `PORT` - Application port (default: 8080)
- `SPRING_PROFILES_ACTIVE` - Active Spring profile
- `JAVA_OPTS` - JVM options

**Resource Limits (docker-compose.yml):**
- Memory: 512MB minimum, 768MB maximum
- CPU: 0.5 cores minimum, 1.0 core maximum

### Security Features

1. **Non-root user:** Application runs as `keygo:keygo`
2. **Minimal base:** Alpine Linux (small attack surface)
3. **Health checks:** Automatic restart on failure
4. **Resource limits:** Prevent resource exhaustion

### Documentation

Complete Docker documentation available at: `docs/DOCKER.md`

Includes:
- Detailed architecture explanation
- Troubleshooting guide
- Best practices
- Multi-architecture builds
- Environment variables reference

---

## Español

### Resumen

Se ha agregado soporte Docker a KeyGo Server con una configuración de build multi-etapa optimizada para uso en producción.

### Archivos Creados

1. **Dockerfile** - Configuración de build multi-etapa
2. **.dockerignore** - Optimizar contexto de build
3. **docker-compose.yml** - Configuración de orquestación
4. **docs/DOCKER.md** - Documentación completa de Docker

### Características del Dockerfile

#### Etapa 1: Builder
- **Imagen Base:** `eclipse-temurin:21-jdk-alpine`
- **Propósito:** Construir la aplicación
- **Optimización:**
  - Caché de dependencias Maven (capa separada)
  - Soporte para proyecto multi-módulo
  - Extracción de capas del JAR

#### Etapa 2: Runtime
- **Imagen Base:** `eclipse-temurin:21-jre-alpine` (más pequeña)
- **Características:**
  - Usuario sin privilegios root para seguridad
  - Health check configurado
  - Configuración JVM optimizada
  - Enfoque de JAR por capas

### Beneficios Principales

✅ **Build multi-etapa** - Imagen final más pequeña (~250-300MB)
✅ **Caché de capas** - Rebuilds más rápidos (dependencias en caché)
✅ **Seguridad** - Usuario sin root, imagen base mínima
✅ **Health checks** - Monitoreo integrado
✅ **Listo para producción** - Límites de recursos, políticas de reinicio
✅ **Despliegue fácil** - Docker Compose incluido

### Capas de la Imagen

La imagen final usa el enfoque por capas de Spring Boot:
1. Dependencias (librerías externas - raramente cambian)
2. Spring Boot Loader
3. Dependencias snapshot
4. Código de aplicación (cambia frecuentemente)

Esto maximiza la eficiencia del caché de Docker.

### Comandos de Inicio Rápido

Construir:
```bash
docker build -t keygo-server:1.0-SNAPSHOT .
```

Ejecutar:
```bash
docker-compose up -d
```

Logs:
```bash
docker-compose logs -f keygo-server
```

Detener:
```bash
docker-compose down
```

### Configuración

**Variables de Entorno:**
- `PORT` - Puerto de la aplicación (por defecto: 8080)
- `SPRING_PROFILES_ACTIVE` - Perfil activo de Spring
- `JAVA_OPTS` - Opciones de JVM

**Límites de Recursos (docker-compose.yml):**
- Memoria: 512MB mínimo, 768MB máximo
- CPU: 0.5 cores mínimo, 1.0 core máximo

### Características de Seguridad

1. **Usuario sin root:** La aplicación se ejecuta como `keygo:keygo`
2. **Base mínima:** Alpine Linux (superficie de ataque pequeña)
3. **Health checks:** Reinicio automático en caso de fallo
4. **Límites de recursos:** Prevenir agotamiento de recursos

### Documentación

Documentación completa de Docker disponible en: `docs/DOCKER.md`

Incluye:
- Explicación detallada de la arquitectura
- Guía de solución de problemas
- Mejores prácticas
- Builds multi-arquitectura
- Referencia de variables de entorno

---

## Technical Details / Detalles Técnicos

### Why Multi-Stage? / ¿Por qué Multi-Etapa?

**English:**
- Separates build tools from runtime
- Reduces final image size by ~60%
- Improves security (no build tools in production)
- Faster deployments (smaller images)

**Español:**
- Separa herramientas de build del runtime
- Reduce tamaño de imagen final en ~60%
- Mejora seguridad (sin herramientas de build en producción)
- Despliegues más rápidos (imágenes más pequeñas)

### Layer Caching Strategy / Estrategia de Caché de Capas

**Order of operations (least to most frequent changes):**
**Orden de operaciones (de menos a más cambios frecuentes):**

1. Base image / Imagen base
2. Maven wrapper / Wrapper de Maven
3. POM files / Archivos POM
4. Dependencies download / Descarga de dependencias
5. Source code / Código fuente
6. Build / Compilación

This order maximizes cache hits during development.
Este orden maximiza aciertos de caché durante desarrollo.

---

**Date Created / Fecha de Creación:** January 11, 2026
**Version / Versión:** 1.0

