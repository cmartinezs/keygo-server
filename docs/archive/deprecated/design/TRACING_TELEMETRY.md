# Tracing & Telemetry (Consolidated)

⚠️ **This documentation has been consolidated into Observability strategy.**

**See:** [`OBSERVABILITY.md`](OBSERVABILITY.md) for complete logging, metrics, and tracing implementation:
- MDC + Correlation IDs section
- Structured logging with ELK Stack
- Distributed tracing with Micrometer + Jaeger
- Custom metrics
- Health checks
- Alerts and dashboards

This file is maintained for backward compatibility only. All updates are made to the canonical `OBSERVABILITY.md`.

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

