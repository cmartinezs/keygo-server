# Plan: Estrategia de trazabilidad, telemetría y logging

Implementar trazabilidad end-to-end usando MDC de SLF4J para propagar un `traceId` UUID desde la entrada de cada petición a través de **todas las capas** (filtros → controller → use case → adapter → respuesta). Agregar `traceId` a respuestas de error (T-063), cabecera `X-Trace-ID` en todas las respuestas, y configurar logging estructurado JSON para producción.

El flujo objetivo es:
```
[IN]  RequestTracingFilter → traceId MDC ─────────────────────────────────────────► [OUT]
        BootstrapAdminKeyFilter  → MDC + userId                          header X-Trace-ID
          TenantResolutionFilter → MDC + tenantSlug                      ErrorData.traceId
            Controller (log entrada/salida)
              UseCase    (log inicio/fin)
                Adapter  (log query/result)
```

---

## Pasos

1. **Crear `RequestTracingFilter.java` en `keygo-run/filter/`** — genera `traceId` (UUID o reusa `X-Request-ID` del cliente), lo pone en MDC con claves `traceId`, `method`, `path`; mide duración; escribe `[REQ_IN]` al entrar y `[REQ_OUT method=GET path=/api/... status=200 durationMs=45]` al salir; agrega cabecera `X-Trace-ID` a la respuesta; limpia MDC en `finally`.

2. **Enriquecer MDC en filtros existentes** — `BootstrapAdminKeyFilter` agrega `MDC.put("userId", claims.get("sub"))` tras validar el JWT; `TenantResolutionFilter` agrega `MDC.put("tenantSlug", ...)` tras resolver el tenant. Ambos limpian sus propias claves en `finally` sin borrar `traceId`.

3. **Añadir `traceId` a `ErrorData.java`** — nuevo campo `String traceId` con `@JsonInclude(NON_NULL)`; `ApiErrorDataFactory` lo lee de `MDC.get("traceId")` en el método privado `fromDetail(...)` (resuelve T-063).

4. **Crear `logback-spring.xml` en `keygo-run/src/main/resources/`** — perfil `local`: patrón colorido en consola con `[%X{traceId}] [%X{tenantSlug}] [%X{userId}]`; perfil `default`/`prod`: appender JSON estructurado usando `logstash-logback-encoder` con campos extra `traceId`, `tenantSlug`, `userId`, `method`, `path`; nivel raíz `INFO`, `io.cmartinezs.keygo` en `DEBUG` (configurable por `logging.level`).

5. **Registrar el filtro en `ApplicationConfig.java`** — `@Bean FilterRegistrationBean<RequestTracingFilter>` con orden `Ordered.HIGHEST_PRECEDENCE` (antes de `BootstrapAdminKeyFilter`) y URL pattern `/*`.

6. **Añadir dependencia `logstash-logback-encoder` al `pom.xml` de `keygo-run`** para el appender JSON de producción (compatible con Logback/SLF4J, sin conflicto con Jackson 3).

7. **Tests unitarios `RequestTracingFilterTest`** — verificar que `traceId` se genera, se pone en MDC, aparece en header `X-Trace-ID`, y el MDC queda limpio tras la petición; usar `MockHttpServletRequest/Response`.

---

## Consideraciones adicionales

1. **¿`traceId` solo en `ErrorData` o también en `BaseResponse<T>` de éxito?** Opción A (recomendada): solo en `ErrorData` + header `X-Trace-ID` en todas las respuestas. Opción B: campo `traceId` en el envelope `BaseResponse<T>` siempre visible. La Opción A es menos invasiva y no rompe contratos de respuesta existentes.

2. **¿AOP para log automático en use cases?** Opción A: `@Aspect` en `keygo-run` con `@Around("execution(* io.cmartinezs.keygo.app..*UseCase.execute(..))")` — elegante, cero cambio en use cases. Opción B: logging manual en cada use case — más explícito y testeable. Recomiendo implementar primero la Opción A como mejora de bajo esfuerzo y decidir si ampliar.

3. **¿Métricas HTTP de Micrometer en este scope?** `spring-boot-actuator` ya está incluido; basta con agregar `management.metrics.web.server.request.autotime.enabled=true` en `application.yml` para obtener histogramas HTTP (`http.server.requests`) sin código adicional. Las métricas de negocio (tokens, logins, etc.) corresponden a T-073 y quedan fuera de este alcance.

---

## Módulos afectados

| Módulo | Acción |
|---|---|
| `keygo-run` | Nuevo `RequestTracingFilter`; modificar `BootstrapAdminKeyFilter` y `TenantResolutionFilter`; nuevo `logback-spring.xml`; wiring en `ApplicationConfig`; dependencia `logstash-logback-encoder` en `pom.xml` |
| `keygo-api` | Modificar `ErrorData` (campo `traceId`); modificar `ApiErrorDataFactory` (leer MDC) |

## Archivos a crear

| Archivo | Módulo |
|---|---|
| `keygo-run/.../filter/RequestTracingFilter.java` | `keygo-run` |
| `keygo-run/src/main/resources/logback-spring.xml` | `keygo-run` |

## Archivos a modificar

| Archivo | Cambio |
|---|---|
| `keygo-run/.../filter/BootstrapAdminKeyFilter.java` | MDC enrich con `userId` tras validar JWT |
| `keygo-run/.../filter/TenantResolutionFilter.java` | MDC enrich con `tenantSlug` tras resolver tenant |
| `keygo-api/.../error/ErrorData.java` | Nuevo campo `traceId` |
| `keygo-api/.../error/ApiErrorDataFactory.java` | Leer `MDC.get("traceId")` al construir `ErrorData` |
| `keygo-run/.../config/ApplicationConfig.java` | Registrar `RequestTracingFilter` con `HIGHEST_PRECEDENCE` |
| `keygo-run/pom.xml` | Agregar `logstash-logback-encoder` |
| `keygo-run/src/main/resources/application.yml` | Activar autotime de Micrometer |

## Tests a crear

| Archivo | Descripción |
|---|---|
| `keygo-run/.../filter/RequestTracingFilterTest.java` | MDC set/clear, header `X-Trace-ID`, log entrada/salida |

