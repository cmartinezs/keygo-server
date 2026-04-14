---
id: T-149
slug: docker-native-image
title: Imagen Docker optimizada con compilación nativa (GraalVM)
status: registered
created: 2026-04-14
---

# T-149 — Imagen Docker optimizada con compilación nativa (GraalVM)

**Estado:** ⬜ REGISTRADA  
**Módulos afectados:** `keygo-run`, raíz del proyecto (Dockerfile, `.dockerignore`, CI)

---

## Requisito

Crear una imagen Docker de producción donde el artefacto está compilado de forma nativa con
GraalVM (Spring Boot 3.x/4.x Native). El objetivo es arranque en milisegundos, huella de
memoria mínima y una imagen final sin JDK.

---

## Propuesta de solución

### Estrategia de build

Usar **multi-stage Dockerfile**:

1. **Stage `builder`** — imagen `ghcr.io/graalvm/native-image` (o `eclipse-temurin` + GraalVM CE):
   compila el proyecto con `./mvnw -Pnative native:compile -pl keygo-run`.
2. **Stage `runtime`** — imagen base `gcr.io/distroless/base-debian12` (sin JDK, sin shell):
   copia solo el binario nativo resultante.

### Perfil Maven

Añadir perfil `native` en el POM raíz (o en `keygo-run/pom.xml`) con el plugin
`org.graalvm.buildtools:native-maven-plugin` configurado para el módulo `keygo-run`.

### Configuraciones de hint necesarias

Spring Boot Native requiere hints de reflexión/proxy para:
- Entidades JPA (`@Entity`) — cubierto por `spring-boot-starter-data-jpa` AOT processor.
- Enums con `@JsonValue` / `@EnumValue`.
- Beans de `keygo-infra` que usen proxies dinámicos.
- `@ConfigurationProperties` — cubierto por AOT.

Se deben registrar en `src/main/resources/META-INF/native-image/` o vía `@RegisterReflectionForBinding`.

### Tamaño estimado de imagen final

| Layer | Tamaño aprox. |
|---|---|
| distroless base | ~20 MB |
| binario nativo keygo-server | ~80–130 MB |
| **Total** | **~100–150 MB** |

vs. imagen JVM actual (~350–500 MB con JDK).

---

## Pasos de implementación

| # | Acción | Archivo | Estado |
|---|---|---|---|
| 1 | Añadir perfil `native` con `native-maven-plugin` en `keygo-run/pom.xml` | `keygo-run/pom.xml` | 🔲 PENDING |
| 2 | Crear `Dockerfile` multi-stage (builder GraalVM + runtime distroless) | `Dockerfile` | 🔲 PENDING |
| 3 | Crear `.dockerignore` optimizado | `.dockerignore` | 🔲 PENDING |
| 4 | Verificar hints de reflexión: entidades, enums, proxies | `META-INF/native-image/` | 🔲 PENDING |
| 5 | Ajustar `application.yml` para perfil `native`/`prod` (sin H2, sin devtools) | `keygo-run/src/main/resources/` | 🔲 PENDING |
| 6 | Validar arranque y health check del contenedor nativo | local / CI | 🔲 PENDING |
| 7 | Documentar comando de build y run en `doc/07-operations/` | `doc/07-operations/docker-native.md` | 🔲 PENDING |

---

## Verificación

```bash
# Build nativo local (requiere GraalVM instalado o Docker con builder)
./mvnw -Pnative native:compile -pl keygo-run -DskipTests

# Build imagen Docker
docker build -t keygo-server:native .

# Smoke test
docker run --rm -p 8080:8080 keygo-server:native
curl http://localhost:8080/keygo-server/actuator/health
```

Criterios de aceptación:
- Imagen construye sin errores en CI.
- Arranque en < 500 ms medido en logs de Spring.
- `/actuator/health` responde `UP`.
- Tamaño de imagen < 200 MB.
