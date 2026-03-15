# Docker Setup for KeyGo Server / Configuración Docker para KeyGo Server

[English](#english) | [Español](#español)

---

## English

### Prerequisites

- Docker 20.10+
- Docker Compose 2.0+ (optional)
- At least 1GB of available RAM

### Quick Start

#### Option 1: Docker

Build the image:
```bash
docker build -t keygo-server:1.0-SNAPSHOT .
```

Run the container:
```bash
docker run -d \
  --name keygo-server \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  keygo-server:1.0-SNAPSHOT
```

#### Option 2: Docker Compose (Recommended)

Start the services:
```bash
docker-compose up -d
```

Stop the services:
```bash
docker-compose down
```

View logs:
```bash
docker-compose logs -f keygo-server
```

### Dockerfile Architecture

#### Multi-Stage Build

**Stage 1: Builder**
- Base: `eclipse-temurin:21-jdk-alpine`
- Purpose: Compile the application
- Features:
  - Maven dependency caching
  - Multi-module project support
  - JAR layer extraction for optimization

**Stage 2: Runtime**
- Base: `eclipse-temurin:21-jre-alpine`
- Purpose: Run the application
- Features:
  - Minimal JRE (smaller image)
  - Non-root user for security
  - Health check configured
  - Optimized JVM settings

#### Image Layers

The final image uses Spring Boot's layered JAR approach:
1. **dependencies** - External libraries (rarely change)
2. **spring-boot-loader** - Spring Boot loader
3. **snapshot-dependencies** - Snapshot dependencies
4. **application** - Your application code (changes frequently)

This layering maximizes Docker cache efficiency.

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PORT` | `8080` | Application port |
| `SPRING_PROFILES_ACTIVE` | - | Active Spring profile |
| `JAVA_OPTS` | See Dockerfile | JVM options |

### Health Check

The container includes a health check that verifies the application is running:
- **Endpoint:** `/keygo-server-1.0-SNAPSHOT/actuator/health`
- **Interval:** 30 seconds
- **Timeout:** 3 seconds
- **Retries:** 3

### Resource Limits

Default recommendations (configure in docker-compose.yml):
- **Memory:** 512MB minimum, 768MB maximum
- **CPU:** 0.5 cores minimum, 1.0 core maximum

### Security Features

✅ Non-root user (`keygo:keygo`)
✅ Minimal base image (Alpine)
✅ No unnecessary packages
✅ Health check enabled
✅ Resource limits configured

### Image Size

- **Builder stage:** ~400-500MB (not included in final image)
- **Final runtime image:** ~250-300MB
- **Application layers:** ~50-100MB

### Troubleshooting

**Container won't start:**
```bash
docker logs keygo-server
```

**Check health status:**
```bash
docker inspect --format='{{.State.Health.Status}}' keygo-server
```

**Access container shell:**
```bash
docker exec -it keygo-server sh
```

### Building for Different Architectures

```bash
# For ARM64 (Apple Silicon, ARM servers)
docker buildx build --platform linux/arm64 -t keygo-server:1.0-SNAPSHOT .

# For multi-platform
docker buildx build --platform linux/amd64,linux/arm64 -t keygo-server:1.0-SNAPSHOT .
```

---

## Español

### Requisitos Previos

- Docker 20.10+
- Docker Compose 2.0+ (opcional)
- Al menos 1GB de RAM disponible

### Inicio Rápido

#### Opción 1: Docker

Construir la imagen:
```bash
docker build -t keygo-server:1.0-SNAPSHOT .
```

Ejecutar el contenedor:
```bash
docker run -d \
  --name keygo-server \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  keygo-server:1.0-SNAPSHOT
```

#### Opción 2: Docker Compose (Recomendado)

Iniciar los servicios:
```bash
docker-compose up -d
```

Detener los servicios:
```bash
docker-compose down
```

Ver logs:
```bash
docker-compose logs -f keygo-server
```

### Arquitectura del Dockerfile

#### Build Multi-Etapa

**Etapa 1: Builder**
- Base: `eclipse-temurin:21-jdk-alpine`
- Propósito: Compilar la aplicación
- Características:
  - Caché de dependencias Maven
  - Soporte para proyecto multi-módulo
  - Extracción de capas del JAR para optimización

**Etapa 2: Runtime**
- Base: `eclipse-temurin:21-jre-alpine`
- Propósito: Ejecutar la aplicación
- Características:
  - JRE mínimo (imagen más pequeña)
  - Usuario sin privilegios root para seguridad
  - Health check configurado
  - Configuración JVM optimizada

#### Capas de la Imagen

La imagen final usa el enfoque de JAR por capas de Spring Boot:
1. **dependencies** - Librerías externas (raramente cambian)
2. **spring-boot-loader** - Cargador de Spring Boot
3. **snapshot-dependencies** - Dependencias snapshot
4. **application** - Código de tu aplicación (cambia frecuentemente)

Esta separación en capas maximiza la eficiencia del caché de Docker.

### Variables de Entorno

| Variable | Por Defecto | Descripción |
|----------|-------------|-------------|
| `PORT` | `8080` | Puerto de la aplicación |
| `SPRING_PROFILES_ACTIVE` | - | Perfil activo de Spring |
| `JAVA_OPTS` | Ver Dockerfile | Opciones de JVM |

### Health Check

El contenedor incluye un health check que verifica que la aplicación esté funcionando:
- **Endpoint:** `/keygo-server-1.0-SNAPSHOT/actuator/health`
- **Intervalo:** 30 segundos
- **Timeout:** 3 segundos
- **Reintentos:** 3

### Límites de Recursos

Recomendaciones por defecto (configurar en docker-compose.yml):
- **Memoria:** 512MB mínimo, 768MB máximo
- **CPU:** 0.5 cores mínimo, 1.0 core máximo

### Características de Seguridad

✅ Usuario sin privilegios root (`keygo:keygo`)
✅ Imagen base mínima (Alpine)
✅ Sin paquetes innecesarios
✅ Health check habilitado
✅ Límites de recursos configurados

### Tamaño de la Imagen

- **Etapa builder:** ~400-500MB (no incluido en imagen final)
- **Imagen runtime final:** ~250-300MB
- **Capas de aplicación:** ~50-100MB

### Solución de Problemas

**El contenedor no inicia:**
```bash
docker logs keygo-server
```

**Verificar estado de salud:**
```bash
docker inspect --format='{{.State.Health.Status}}' keygo-server
```

**Acceder a shell del contenedor:**
```bash
docker exec -it keygo-server sh
```

### Construir para Diferentes Arquitecturas

```bash
# Para ARM64 (Apple Silicon, servidores ARM)
docker buildx build --platform linux/arm64 -t keygo-server:1.0-SNAPSHOT .

# Para multi-plataforma
docker buildx build --platform linux/amd64,linux/arm64 -t keygo-server:1.0-SNAPSHOT .
```

---

## Best Practices / Mejores Prácticas

### English:
1. **Use specific tags** - Avoid `latest` tag in production
2. **Set resource limits** - Prevent container from consuming all resources
3. **Monitor logs** - Use `docker-compose logs` to track application behavior
4. **Regular updates** - Keep base images updated for security patches
5. **Volume mounts** - Use volumes for persistent data (databases, logs)

### Español:
1. **Usa tags específicos** - Evita el tag `latest` en producción
2. **Establece límites de recursos** - Previene que el contenedor consuma todos los recursos
3. **Monitorea logs** - Usa `docker-compose logs` para seguir el comportamiento de la aplicación
4. **Actualizaciones regulares** - Mantén las imágenes base actualizadas para parches de seguridad
5. **Montajes de volúmenes** - Usa volúmenes para datos persistentes (bases de datos, logs)

---

**Created by / Creado por:** KeyGo Server Team
**Last updated / Última actualización:** January 2026

