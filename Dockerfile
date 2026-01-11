# Stage 1: Build / Etapa 1: Construcción
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory / Establecer directorio de trabajo
WORKDIR /build

# Copy Maven wrapper and pom files / Copiar Maven wrapper y archivos pom
COPY .mvn/ .mvn/
COPY mvnw .
COPY pom.xml .
COPY keygo-bom/pom.xml keygo-bom/
COPY keygo-common/pom.xml keygo-common/
COPY keygo-domain/pom.xml keygo-domain/
COPY keygo-app/pom.xml keygo-app/
COPY keygo-infra/pom.xml keygo-infra/
COPY keygo-api/pom.xml keygo-api/
COPY keygo-run/pom.xml keygo-run/

# Download dependencies (cached layer) / Descargar dependencias (capa cacheada)
RUN ./mvnw dependency:go-offline -B

# Copy source code / Copiar código fuente
COPY keygo-common/src keygo-common/src
COPY keygo-domain/src keygo-domain/src
COPY keygo-app/src keygo-app/src
COPY keygo-infra/src keygo-infra/src
COPY keygo-api/src keygo-api/src
COPY keygo-run/src keygo-run/src

# Build application / Construir aplicación
RUN ./mvnw clean package -DskipTests -B

# Extract JAR layers / Extraer capas del JAR
WORKDIR /build/keygo-run/target
RUN cp keygo-run-*.jar keygo-run.jar
RUN java -Djarmode=tools -jar keygo-run-*.jar extract --layers --destination extracted

# Stage 2: Runtime / Etapa 2: Ejecución
FROM eclipse-temurin:21-jre-alpine

# Add metadata / Agregar metadata
LABEL org.opencontainers.image.title="KeyGo Server"
LABEL org.opencontainers.image.description="Enterprise authentication service - open source"
LABEL org.opencontainers.image.version="1.0-SNAPSHOT"
LABEL org.opencontainers.image.authors="Carlos Martínez <https://github.com/cmartinezs>"
LABEL org.opencontainers.image.source="https://github.com/cmartinezs/keygo-server"
LABEL org.opencontainers.image.licenses="AGPL-3.0"

# Create non-root user / Crear usuario sin privilegios root
RUN addgroup -S keygo && adduser -S keygo -G keygo

# Set working directory / Establecer directorio de trabajo
WORKDIR /app

# Copy JAR layers from builder / Copiar capas del JAR desde builder
COPY --from=builder --chown=keygo:keygo /build/keygo-run/target/extracted/dependencies/ ./
COPY --from=builder --chown=keygo:keygo /build/keygo-run/target/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=keygo:keygo /build/keygo-run/target/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=keygo:keygo /build/keygo-run/target/extracted/application/ ./
COPY --from=builder --chown=keygo:keygo /build/keygo-run/target/keygo-run.jar ./keygo-run.jar

# Switch to non-root user / Cambiar a usuario sin privilegios
USER keygo

# Expose port / Exponer puerto
EXPOSE 8080

# Health check / Verificación de salud
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/keygo-server/actuator/health || exit 1

# Set JVM options / Configurar opciones de JVM
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+OptimizeStringConcat"

# Run application / Ejecutar aplicación
ENTRYPOINT ["java", "-Duser.timezone=\"America/Santiago\"","-Djava.security.egd=file:/dev/./urandom","-jar","keygo-run.jar"]
