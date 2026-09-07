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
COPY keygo-supabase/pom.xml keygo-supabase/
COPY keygo-run/pom.xml keygo-run/

# Download dependencies (cached layer) / Descargar dependencias (capa cacheada)
RUN ./mvnw dependency:go-offline -B

# Copy source code / Copiar código fuente
COPY keygo-common/src keygo-common/src
COPY keygo-domain/src keygo-domain/src
COPY keygo-app/src keygo-app/src
COPY keygo-infra/src keygo-infra/src
COPY keygo-api/src keygo-api/src
COPY keygo-supabase/src keygo-supabase/src
COPY keygo-run/src keygo-run/src

# Build application. Repository validation is intentionally a separate gate
# (`make validate`) so Docker layer caching does not become the source of test truth.
RUN ./mvnw clean package -DskipTests -B

# Extract JAR layers / Extraer capas del JAR
WORKDIR /build/keygo-run/target
RUN cp keygo-run-*.jar keygo-run.jar
RUN java -Djarmode=tools -jar keygo-run-*.jar extract --layers --destination extracted

# Stage 2: Runtime / Etapa 2: Ejecución
FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.title="KeyGo Server"
LABEL org.opencontainers.image.description="Enterprise authentication service - open source"
LABEL org.opencontainers.image.version="1.0-SNAPSHOT"
LABEL org.opencontainers.image.authors="Carlos Martínez <https://github.com/cmartinezs>"
LABEL org.opencontainers.image.source="https://github.com/cmartinezs/keygo-server"
LABEL org.opencontainers.image.licenses="AGPL-3.0"

RUN addgroup -S keygo && adduser -S keygo -G keygo
WORKDIR /app

COPY --from=builder --chown=keygo:keygo /build/keygo-run/target/extracted/dependencies/ ./
COPY --from=builder --chown=keygo:keygo /build/keygo-run/target/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=keygo:keygo /build/keygo-run/target/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=keygo:keygo /build/keygo-run/target/extracted/application/ ./
COPY --from=builder --chown=keygo:keygo /build/keygo-run/target/keygo-run.jar ./keygo-run.jar

USER keygo
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/keygo-server/actuator/health || exit 1

# Supabase/PostgreSQL adapter remains the current container persistence profile.
# Shared develop/prod deployments MUST override with the explicit environment profile set.
ENV SPRING_PROFILES_ACTIVE="supabase"
ENV SUPABASE_URL=""
ENV SUPABASE_USER=""
ENV SUPABASE_PASSWORD=""
ENV SUPABASE_DB_SCHEMA="public"

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+OptimizeStringConcat"

# Shell expansion is intentional here so provider/runtime JAVA_OPTS are actually applied.
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Duser.timezone=America/Santiago -Djava.security.egd=file:/dev/./urandom -jar keygo-run.jar"]
