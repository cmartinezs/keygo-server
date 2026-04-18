# T-160 — Revisión y optimización de arranque de Spring Boot

**Estado:** ⬜ Registrada  
**Prioridad:** 🟡 Media  
**Esfuerzo estimado:** 🕐 S–M  
**Área:** Rendimiento / Configuración

---

## Problema / Requisito

El arranque de la aplicación es perceptiblemente lento en entorno de desarrollo. Esto impacta
el ciclo de feedback al iterar (test → run → verify) y sugiere configuración subóptima o beans
innecesarios inicializados de forma eager.

---

## Alcance de la revisión

### 1. Medir línea base

Antes de optimizar, obtener tiempos reales con `--debug` y el actuator:

```bash
./mvnw spring-boot:run -pl keygo-run \
  -Dspring-boot.run.jvmArguments="-Dspring.main.log-startup-info=true"
```

Revisar el log `Started KeygoServerApplication in X.XXX seconds`.

### 2. Lazy initialization

Spring Boot 2.2+ soporta inicialización lazy de beans:

```yaml
spring:
  main:
    lazy-initialization: true
```

Habilitar en perfil `local`/`h2` — evaluar si introduce errores en tiempo de primer request
(beans con dependencias circulares o inicialización con efecto secundario).

### 3. Auditoría de beans iniciados en arranque

Usar el endpoint actuator para identificar beans costosos:

```
GET /keygo-server/actuator/beans
```

Identificar candidatos a lazy o a eliminación:
- Beans de infraestructura no usados en perfil `local`
- Auto-configuraciones de Spring que no aplican (excluir con `@SpringBootApplication(exclude = [...]`)
- Inicialización eager de `SecureRandom` u otros recursos criptográficos

### 4. Flyway en arranque

Flyway ejecuta migraciones en cada arranque. En `local`/`h2` esto incluye hasta V25 migraciones
con seeds pesados (V16, V17, V21). Opciones:

- Verificar si `spring.flyway.enabled=false` en perfil `h2` acelera el arranque para tests
  que no necesitan la DB migrada
- Separar seeds de schema (ya registrado en T-054) para reducir trabajo de Flyway en `local`

### 5. Datasource y connection pool

Revisar si el pool HikariCP está configurado con `minimumIdle` alto o `connectionTimeout`
largo que bloquea el arranque:

```yaml
spring:
  datasource:
    hikari:
      minimum-idle: 1          # reducir en local
      connection-timeout: 3000 # reducir en local (default 30 000 ms)
      initialization-fail-timeout: 1
```

### 6. JVM flags de desarrollo

Agregar flags de JVM para acelerar arranque en modo desarrollo:

```
-XX:TieredStopAtLevel=1       # deshabilita JIT de nivel 4 (C2) — arranque más rápido, menor peak throughput
-Xverify:none                 # omitir bytecode verification (solo dev)
-XX:+UseParallelGC            # GC más simple para procesos de vida corta
```

Documentar que estos flags **no deben usarse en producción**.

### 7. Spring Boot DevTools

Verificar que `spring-boot-devtools` está presente en `keygo-run` con scope `optional`/`runtime`
para habilitar hot-reload sin afectar el artefacto final.

### 8. Anotaciones `@ConditionalOn*`

Revisar beans que se inicializan en todos los perfiles pero solo son necesarios en `supabase`
(ej. adaptadores Supabase, configuración de email SMTP). Envolver con
`@ConditionalOnProperty` o `@Profile`.

---

## Pasos de implementación

| # | Paso | Módulo | Estado |
|---|------|--------|--------|
| 1 | Medir tiempo de arranque actual con `log-startup-info=true` y anotar línea base | keygo-run | PENDING |
| 2 | Habilitar `lazy-initialization: true` en perfil `local`/`h2` y medir delta | keygo-run | PENDING |
| 3 | Auditar beans via actuator — identificar candidatos costosos | keygo-run | PENDING |
| 4 | Revisar exclusiones de auto-configuración innecesarias | keygo-run | PENDING |
| 5 | Ajustar HikariCP `minimum-idle` y `connection-timeout` en perfil `local` | keygo-run | PENDING |
| 6 | Documentar JVM flags de desarrollo en `doc/07-operations/environment-setup.md` | doc | PENDING |
| 7 | Verificar/agregar `spring-boot-devtools` en `keygo-run` | keygo-run | PENDING |
| 8 | Envolver beans exclusivos de `supabase` con `@ConditionalOnProperty` o `@Profile` | keygo-run / keygo-supabase | PENDING |
| 9 | Medir tiempo final y documentar mejora obtenida | doc | PENDING |

---

## Relaciones

| Artefacto | Tipo | Descripción |
|-----------|------|-------------|
| T-054 | complementaria | Separar seeds de schema — reduciría trabajo de Flyway en arranque local |
| T-149 | complementaria | Imagen Docker nativa GraalVM — arranque < 500 ms como objetivo de producción |
| T-152 | complementaria | Perfiles `develop`/`test`/`prod` — prerequisito para aplicar flags solo en dev |

---

## Guía de verificación

```bash
# Línea base
time ./mvnw spring-boot:run -pl keygo-run -Dspring-boot.run.profiles=local

# Con lazy init habilitado
time ./mvnw spring-boot:run -pl keygo-run \
  -Dspring-boot.run.profiles=local \
  -Dspring-boot.run.jvmArguments="-Dspring.main.lazy-initialization=true"

# Beans cargados
curl http://localhost:8080/keygo-server/actuator/beans | jq '.contexts | .[] | .beans | keys | length'
```

---

## Historial de transiciones

- 2026-04-15 → ⬜ Registrada (arranque lento identificado en desarrollo)
