# Observability Strategy — Logs, Métricas, Tracing

**Propósito:** Documentar cómo observar, monitorear y troubleshoot KeyGo en producción a través de logs, métricas y distributed tracing.

---

## Tres Pilares: Logs, Métricas, Traces

```
Logs          Métricas         Traces
│             │                │
├─ ¿Qué pasó? ├─ ¿Cuánto? ¿Qué? ├─ ¿Por qué?
├─ Eventos    ├─ Series tiempo │  ├─ Request path
├─ Error msgs │  ├─ Latencia   │  ├─ Dependencies
└─ Context    └─ Errors/sec    └─ Timing breakdown
```

---

## Logging Strategy

### Niveles & Uso

```
TRACE  → DEBUG verbose (nunca en prod)
DEBUG  → Development details (dev/staging)
INFO   → Important events (login, deploy, config)
WARN   → Recoverable issues (retry, fallback)
ERROR  → Failures, exceptions (alert)
FATAL  → System down (emergency)
```

### Configuration: application-prod.yml

```yaml
logging:
  level:
    root: INFO
    io.cmartinezs.keygo: INFO
    io.cmartinezs.keygo.domain: DEBUG  # ← Domain logic
    org.springframework: WARN
    org.springframework.security: INFO
    org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor: WARN
  
  file:
    name: /var/log/keygo/app.log
    max-size: 100MB
    max-history: 30  # Keep 30 days
  
  pattern:
    console: "%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{ISO8601} [%thread] %-5level %logger{36} - %X{traceId} - %msg%n"  # Correlation ID
```

### Structured Logging with MDC

**MDC = Mapped Diagnostic Context** — Inyecta valores en todos los logs del request.

```java
// Filter que agrega correlation ID a cada request
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
  
  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    
    String traceId = request.getHeader("X-Trace-ID");
    if (traceId == null) {
      traceId = UUID.randomUUID().toString();
    }
    
    MDC.put("traceId", traceId);
    response.addHeader("X-Trace-ID", traceId);
    
    try {
      chain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }
}
```

**Ahora todos los logs incluyen traceId:**
```
2026-04-10 10:30:45 [http-nio-8080-exec-1] INFO  CreateUserUseCase - 550e8400 - User created: john@example.com
2026-04-10 10:30:46 [http-nio-8080-exec-1] INFO  EmailService - 550e8400 - Sending welcome email to john@example.com
2026-04-10 10:30:47 [http-nio-8080-exec-1] INFO  AuditLog - 550e8400 - AUDIT: user.created by admin-user
```

**Buscar todas operaciones de un request:**
```bash
grep "550e8400" /var/log/keygo/app.log
```

### Log Events (Domain)

```java
// En domain service
@Component
public class CreateUserUseCase {
  
  private static final Logger log = LoggerFactory.getLogger(CreateUserUseCase.class);
  
  public User execute(CreateUserRequest request, TenantContext context) {
    log.info("Creating user: email={}", request.getEmail());
    
    User user = User.builder()
        .email(request.getEmail())
        .username(request.getUsername())
        .tenantId(context.getTenantId())
        .build();
    
    User saved = userRepository.save(user);
    
    log.info("User created successfully: userId={}, email={}", 
        saved.getId(), saved.getEmail());
    
    return saved;
  }
}
```

### Error Logging

```java
// Controller
@PostMapping("/users")
public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
  try {
    User user = createUserUseCase.execute(request, tenantContext);
    return ResponseEntity.status(201).body(CreateUserResponse.of(user));
  } catch (DomainException e) {
    log.warn("Business rule violation: code={}, message={}", 
        e.getCode(), e.getMessage());
    return ErrorResponse.of(e);
  } catch (Exception e) {
    log.error("Unexpected error creating user", e);  // ← Stack trace
    return ErrorResponse.internalError("USER_CREATION_FAILED");
  }
}
```

### Log Aggregation (ELK Stack)

**Filebeat** →  **Logstash** → **Elasticsearch** → **Kibana**

```yaml
# filebeat.yml
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /var/log/keygo/app.log
  
  processors:
    - add_kubernetes_metadata:
        in_cluster: true

output.logstash:
  hosts: ["logstash:5000"]
```

**Logstash pipeline:**
```
filter {
  grok {
    match => { "message" => 
      "%{TIMESTAMP_ISO8601:timestamp} \[%{DATA:thread}\] %{LOGLEVEL:level} %{DATA:logger} - %{DATA:traceId} - %{GREEDYDATA:msg}" 
    }
  }
  
  if [level] == "ERROR" {
    mutate { add_tag => [ "alert" ] }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "keygo-%{+YYYY.MM.dd}"
  }
}
```

---

## Metrics Strategy

### Prometheus Exposition

```
GET /actuator/prometheus
```

```
# HELP http_requests_total Total HTTP requests
# TYPE http_requests_total counter
http_requests_total{method="POST",path="/api/v1/tenants/*/users",status="201"} 1023
http_requests_total{method="POST",path="/api/v1/tenants/*/users",status="400"} 15
http_requests_total{method="POST",path="/api/v1/tenants/*/users",status="403"} 5

# HELP http_request_duration_seconds Request latency
# TYPE http_request_duration_seconds histogram
http_request_duration_seconds_bucket{path="/api/v1/tenants/*/users",le="0.5"} 950
http_request_duration_seconds_bucket{path="/api/v1/tenants/*/users",le="1.0"} 1018
http_request_duration_seconds_bucket{path="/api/v1/tenants/*/users",le="5.0"} 1023

# HELP jvm_memory_used_bytes JVM memory used
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap"} 1536000000
jvm_memory_used_bytes{area="nonheap"} 51200000
```

### Custom Metrics

```java
@Component
@Aspect
public class UseCaseMetrics {
  
  private final MeterRegistry meterRegistry;
  
  public UseCaseMetrics(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }
  
  @Around("@annotation(io.cmartinezs.keygo.api.common.Metrics)")
  public Object measureUseCase(ProceedingJoinPoint joinPoint) throws Throwable {
    String useCaseName = joinPoint.getSignature().getName();
    long startTime = System.nanoTime();
    
    try {
      Object result = joinPoint.proceed();
      meterRegistry.counter("usecase.success", "name", useCaseName).increment();
      return result;
    } catch (Exception e) {
      meterRegistry.counter("usecase.failure", "name", useCaseName).increment();
      throw e;
    } finally {
      long duration = System.nanoTime() - startTime;
      meterRegistry.timer("usecase.duration", "name", useCaseName)
          .record(duration, TimeUnit.NANOSECONDS);
    }
  }
}
```

Usage:
```java
@Component
public class CreateUserUseCase {
  
  @Metrics
  public User execute(CreateUserRequest request, TenantContext context) {
    // ... implementation
  }
}
```

**Prometheus output:**
```
usecase_success_total{name="CreateUserUseCase"} 1023
usecase_failure_total{name="CreateUserUseCase"} 5
usecase_duration_seconds_bucket{name="CreateUserUseCase",le="1.0"} 1018
```

### Key Metrics to Track

| Métrica | Tipo | Umbral | Acción |
|---|---|---|---|
| `http_requests_total{status="5xx"}` | Counter | > 1/sec | Page oncall |
| `http_request_duration_seconds{quantile="0.99"}` | Histogram | > 5s | Investigate slow query |
| `jvm_memory_used_bytes{area="heap"}` | Gauge | > 80% | Monitor GC, may OOM |
| `db_connection_pool_size_active` | Gauge | > 90% | Investigate slow queries |
| `authentication_failures_total` | Counter | Spike | Check for brute force |
| `api_rate_limit_exceeded_total` | Counter | Spike | Review tenant quotas |

---

## Distributed Tracing

### Instrumentation: Micrometer + Spring Cloud Sleuth

```yaml
# application-prod.yml
management:
  tracing:
    sampling:
      probability: 0.1  # Sample 10% to reduce overhead
```

**Automated:** Spring auto-instruments HTTP calls, DB queries.

```java
// Explicit span for complex operation
@Component
public class UserProvisioningService {
  
  private final Tracer tracer;
  
  public void provisionUser(User user, Tenant tenant) {
    Span span = tracer.nextSpan().name("provision_user").start();
    
    try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
      span.event("fetch_directory_info");
      DirectoryUser dirUser = directoryClient.fetch(user.getExternalId());
      
      span.event("assign_roles");
      assignRoles(user, dirUser.getGroups());
      
      span.event("send_notification");
      notificationService.sendWelcomeEmail(user);
      
      span.tag("user.id", user.getId().toString());
      span.tag("directory", "okta");
    } finally {
      span.finish();
    }
  }
}
```

### Export to Jaeger/Zipkin

```yaml
# application-prod.yml
management:
  otlp:
    tracing:
      endpoint: http://jaeger-collector:4317
```

**Trace visualization:**
```
User Request (total: 250ms)
├─ Authentication (15ms)
├─ Database Query (80ms)
│  └─ Flyway Migration Check (5ms)
├─ Domain Logic (50ms)
├─ Email Service (100ms)
└─ Audit Log (5ms)
```

---

## Health Checks

### Liveness & Readiness

```java
@Component
public class CustomHealthIndicator extends AbstractHealthIndicator {
  
  private final UserRepository userRepository;
  
  @Override
  protected void doHealthCheck(Health.Builder builder) {
    try {
      // Quick DB ping
      long count = userRepository.count();
      builder.up()
          .withDetail("user_count", count);
    } catch (Exception e) {
      builder.down()
          .withDetail("error", e.getMessage());
    }
  }
}
```

**Endpoints:**
```bash
# Is app alive?
curl http://localhost:8080/actuator/health/liveness
# → 200 OK (or 503 Service Unavailable)

# Is app ready to serve traffic?
curl http://localhost:8080/actuator/health/readiness
# → 200 OK (or 503)

# Detailed info
curl http://localhost:8080/actuator/health
```

Response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": { "database": "PostgreSQL" }
    },
    "flyway": {
      "status": "UP",
      "details": { "flywayVersion": "9.22.3" }
    },
    "customHealth": {
      "status": "UP",
      "details": { "user_count": 1523 }
    }
  }
}
```

---

## Dashboards: Grafana

### Important Panels

#### 1. Request Rate & Errors
```
panel_type: graph
metrics:
  - rate(http_requests_total[5m])  # Requests/sec
  - rate(http_requests_total{status="5xx"}[5m])  # Errors/sec
alerts:
  - error_rate > 0.01  # More than 1% errors
```

#### 2. Latency Percentiles
```
metrics:
  - histogram_quantile(0.50, http_request_duration_seconds)  # p50
  - histogram_quantile(0.95, http_request_duration_seconds)  # p95
  - histogram_quantile(0.99, http_request_duration_seconds)  # p99
```

#### 3. Database Performance
```
metrics:
  - db_connection_pool_size_active  # Active connections
  - rate(sql_execution_seconds_count[5m])  # Queries/sec
  - histogram_quantile(0.95, sql_execution_seconds)  # p95 latency
```

#### 4. JVM Health
```
metrics:
  - jvm_memory_used_bytes / jvm_memory_max_bytes  # Heap %
  - rate(jvm_gc_seconds_count[5m])  # GC frequency
  - jvm_gc_seconds{quantile="0.95"}  # p95 pause time
```

---

## Alerts & Escalation

### Prometheus Rules

```yaml
# prometheus-rules.yml
groups:
  - name: keygo
    rules:
      - alert: HighErrorRate
        expr: rate(http_requests_total{status="5xx"}[5m]) > 0.01
        for: 1m
        annotations:
          summary: "High error rate ({{ $value | humanizePercentage }})"
          runbook: "https://internal.keygo.io/runbooks/high-error-rate"
      
      - alert: HighLatency
        expr: histogram_quantile(0.99, http_request_duration_seconds) > 5
        for: 5m
        annotations:
          summary: "p99 latency > 5s"
      
      - alert: DiskSpaceLow
        expr: node_filesystem_avail_bytes{mountpoint="/"} / node_filesystem_size_bytes < 0.15
        for: 10m
        annotations:
          summary: "Only {{ humanize $value }}% disk available"
```

### PagerDuty Integration

```yaml
# alertmanager.yml
alerting:
  alertmanagers:
    - static_configs:
        - targets: ['alertmanager:9093']

global:
  resolve_timeout: 5m

route:
  receiver: 'pagerduty'
  group_by: ['alertname', 'cluster']
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 12h

receivers:
  - name: 'pagerduty'
    pagerduty_configs:
      - service_key: 'INTEGRATION_KEY'
        description: '{{ .GroupLabels.alertname }}'
```

---

## Troubleshooting Playbook

### High Memory Usage

1. **Check GC activity:**
   ```bash
   curl http://localhost:8080/actuator/metrics/jvm.gc.pause | jq '.'
   ```

2. **Identify memory leak:**
   - Heap dump: `jmap -dump:live,format=b,file=dump.hprof <PID>`
   - Analyze: Use Eclipse MAT or jhat

3. **Restart if necessary:**
   ```bash
   kubectl rollout restart deployment/keygo-server -n production
   ```

### High Latency

1. **Check database query times:**
   ```sql
   -- Slowest queries
   SELECT query, calls, mean_exec_time
   FROM pg_stat_statements
   ORDER BY mean_exec_time DESC
   LIMIT 10;
   ```

2. **Check connection pool:**
   ```bash
   curl http://localhost:8080/actuator/prometheus | grep db_connection
   ```

3. **Check GC pauses:**
   ```bash
   curl http://localhost:8080/actuator/metrics/jvm.gc.pause | jq '.measurements[] | select(.statistic=="MAX")'
   ```

### Error Rate Spike

1. **Check recent changes:**
   ```bash
   git log --oneline -20
   ```

2. **Review logs for error pattern:**
   ```bash
   grep "ERROR" /var/log/keygo/app.log | tail -100
   ```

3. **Check authentication failures:**
   ```bash
   grep "INVALID_CREDENTIALS\|PERMISSION_DENIED" /var/log/keygo/app.log | wc -l
   ```

---

## Anti-Patterns: Evitar

### ❌ Logging Sensitive Data

```java
// MAL
log.info("User password: {}", user.getPassword());
log.info("JWT token: {}", jwtToken);
log.info("API key: {}", apiKey);
```

### ✅ Log Only Safe Data

```java
// BIEN
log.info("User authenticated: userId={}", user.getId());
log.info("JWT token issued for userId={}", extractSubject(jwtToken));
```

---

### ❌ Sampling Only Errors

```
// MAL: Lose good traces
sample_errors_only: true
```

### ✅ Balanced Sampling

```yaml
# BIEN: Sample 10% including success and errors
sampling:
  probability: 0.1
```

---

### ❌ Metrics with High Cardinality

```java
// MAL: Too many unique values
meterRegistry.counter("http_request", 
    "user_id", user.getId(),  // ← 1M+ values
    "method", method).increment();
```

### ✅ Use Labels Wisely

```java
// BIEN: Limited cardinality
meterRegistry.counter("http_request", 
    "method", method,
    "path", normalizePath(path),  // ← 50 values max
    "status", status).increment();
```

---

## Checklist: Observability

- [ ] **Logging:** Structured logs con correlation IDs
- [ ] **Metrics:** Prometheus endpoint expone custom metrics
- [ ] **Tracing:** Distributed traces exportados a Jaeger
- [ ] **Health:** Liveness y readiness checks configurados
- [ ] **Alerts:** Reglas de alerta configuradas (error rate, latency, disk)
- [ ] **Dashboards:** Grafana panels para key metrics
- [ ] **No PII:** Logs no contienen passwords, tokens, emails
- [ ] **Sampling:** Tracing sampling balanceado (10-50%)
- [ ] **Retention:** Logs rotados, métricas por 15+ días

---

## Referencias

| Aspecto | Ubicación |
|---|---|
| **Spring Boot Actuator** | `/actuator` endpoint |
| **Prometheus Metrics** | `/actuator/prometheus` |
| **Health Checks** | `/actuator/health` |
| **MDC Logging** | `docs/design/TEST_INTEGRATION.md` |
| **Production Runbook** | `docs/operations/PRODUCTION_RUNBOOK.md` |
| **Architecture** | `docs/ARCHITECTURE.md` |

---

**Última actualización:** 2026-04-10  
**Estado:** Completado para Sprint 3  
**Próxima:** FRONTEND_DEVELOPER_GUIDE
