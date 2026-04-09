# Production Runbook — Cómo Operar KeyGo en Producción

**Propósito:** Guía operacional para desplegar, monitorear, mantener KeyGo en producción.

---

## Pre-Requisitos

### Hardware Mínimo

| Componente | Desarrollo | Producción |
|---|---|---|
| **CPU** | 1 core | 2+ cores |
| **RAM** | 512 MB | 2+ GB |
| **Disco** | 5 GB | 50+ GB (logs + BD) |
| **Latencia BD** | < 100ms | < 50ms (mismo datacenter) |

### Software Requerido

- **Java:** JDK 21+ (LTS)
- **BD:** PostgreSQL 14+
- **Reverse Proxy:** Nginx o Caddy
- **Orchestración:** Docker + Docker Compose O Kubernetes
- **Logging:** Agregador (ELK, Datadog, CloudWatch)
- **Monitoreo:** Prometheus + Grafana O managed service

---

## Deployment

### Opción A: Docker Compose (Simple)

**Usar para:** Hasta 100 usuarios, producción pequeña.

#### 1. Preparar entorno

```bash
# Crear archivo .env
cat > .env << EOF
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/keygo
SPRING_DATASOURCE_USERNAME=keygo
SPRING_DATASOURCE_PASSWORD=<strong-password>

# Application
KEYGO_ISSUER_BASE_URL=https://keygo.example.com
KEYGO_CORS_ALLOWED_ORIGINS=https://app.example.com,https://console.example.com
KEYGO_UI_BASE_URL=https://app.example.com
KEYGO_PLATFORM_REDIRECT_URI=https://console.example.com/callback

# Email
KEYGO_MAIL_FROM=noreply@example.com
SPRING_MAIL_HOST=smtp.sendgrid.net
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=apikey
SPRING_MAIL_PASSWORD=<sendgrid-api-key>

# JVM
JAVA_OPTS=-Xms1g -Xmx2g -XX:+UseG1GC

# Profile
SPRING_PROFILES_ACTIVE=prod
EOF

chmod 600 .env
```

#### 2. Actualizar docker-compose.yml

```yaml
version: '3.8'

services:
  keygo-server:
    image: keygo-server:1.0-SNAPSHOT  # O usa registry
    container_name: keygo-server
    restart: unless-stopped
    
    env_file: .env
    ports:
      - "127.0.0.1:8080:8080"  # Solo localhost, reverse proxy enfrente
    
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 5s
      retries: 3
    
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 3G
        reservations:
          cpus: '1'
          memory: 2G
    
    networks:
      - keygo-network
    
    depends_on:
      postgres:
        condition: service_healthy
    
    logging:
      driver: "json-file"
      options:
        max-size: "100m"
        max-file: "10"

  postgres:
    image: postgres:16-alpine
    container_name: keygo-postgres
    restart: unless-stopped
    
    environment:
      POSTGRES_DB: keygo
      POSTGRES_USER: keygo
      POSTGRES_PASSWORD: ${SPRING_DATASOURCE_PASSWORD}
    
    volumes:
      - postgres-data:/var/lib/postgresql/data
    
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U keygo"]
      interval: 10s
      timeout: 5s
      retries: 5
    
    networks:
      - keygo-network

networks:
  keygo-network:
    driver: bridge

volumes:
  postgres-data:
```

#### 3. Iniciar servicios

```bash
# Build image
docker-compose build

# Start
docker-compose up -d

# Verificar
docker-compose logs -f keygo-server
docker-compose ps
```

---

### Opción B: Kubernetes (Escalable)

**Usar para:** 1000+ usuarios, alta disponibilidad.

#### Manifest: deployment.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: keygo-server
  namespace: production
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  
  selector:
    matchLabels:
      app: keygo-server
  
  template:
    metadata:
      labels:
        app: keygo-server
    
    spec:
      containers:
      - name: keygo-server
        image: registry.example.com/keygo-server:1.0.0
        imagePullPolicy: IfNotPresent
        
        ports:
        - name: http
          containerPort: 8080
        
        envFrom:
        - configMapRef:
            name: keygo-config
        - secretRef:
            name: keygo-secrets
        
        resources:
          limits:
            cpu: "2"
            memory: "3Gi"
          requests:
            cpu: "1"
            memory: "2Gi"
        
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 5
          failureThreshold: 3
        
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 10
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        
        securityContext:
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          runAsNonRoot: true
          runAsUser: 1000
```

#### Manifest: service.yaml

```yaml
apiVersion: v1
kind: Service
metadata:
  name: keygo-server
  namespace: production
spec:
  selector:
    app: keygo-server
  
  type: ClusterIP
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
```

#### Deploy

```bash
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml

# Verificar
kubectl get pods -n production
kubectl logs -f -n production deployment/keygo-server
```

---

## Monitoreo

### Health Checks

```bash
# Liveness (¿está vivo?)
curl http://localhost:8080/actuator/health/liveness
# → 200 OK = OK, 503 = restart

# Readiness (¿está listo para tráfico?)
curl http://localhost:8080/actuator/health/readiness
# → 200 OK = OK, 503 = no enrutar tráfico

# Full info
curl http://localhost:8080/actuator/health
# → Detalla BD, BD migrations, etc.
```

### Métricas (Prometheus)

```bash
# Endpoint
curl http://localhost:8080/actuator/prometheus

# Métricas importantes
http_requests_total{method="POST",path="/api/v1/tenants/*/users"}
http_request_duration_seconds_bucket{le="1.0"}
jvm_memory_used_bytes{area="heap"}
process_cpu_usage
```

### Alertas Recomendadas

| Métrica | Threshold | Acción |
|---|---|---|
| **HTTP 5xx rate** | > 1% | Page oncall |
| **Request latency p99** | > 5s | Investigate |
| **BD connection pool** | > 90% | Investigate |
| **Disk usage** | > 85% | Cleanup logs |
| **JVM heap** | > 80% | Monitor, consider restart |
| **Pod restart count** | > 5 in 1h | Investigate crash loop |

---

## Logging

### Configuración

**applicati on-prod.yml:**
```yaml
logging:
  level:
    root: INFO
    io.cmartinezs.keygo: INFO
    org.springframework: WARN
    org.springframework.security: INFO
  
  file:
    name: /var/log/keygo/app.log
    max-size: 100MB
    max-history: 30
  
  pattern:
    console: "%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n"
```

### Logs a Monitorear

#### Error 5xx

```bash
# Búsqueda
grep "500 OPERATION_FAILED" /var/log/keygo/app.log | head -10

# Análisis
tail -100 /var/log/keygo/app.log | grep -A 5 "Exception"
```

#### Migraciones fallidas

```bash
curl http://localhost:8080/actuator/health | jq '.components.flyway'
# → status: DOWN = migración falló

# Verificar en BD
SELECT version, success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;
```

#### Acceso denegado (403)

```bash
grep "BUSINESS_RULE_VIOLATION" /var/log/keygo/app.log | wc -l

# Detalle por endpoint
grep "403" /var/log/keygo/app.log | awk '{print $NF}' | sort | uniq -c | sort -rn
```

### Agregación (ELK)

**Logstash config:**
```
input {
  file {
    path => "/var/log/keygo/app.log"
    codec => multiline {
      pattern => "^%{TIMESTAMP_ISO8601}"
      negate => true
      what => "previous"
    }
  }
}

filter {
  grok {
    match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} \[%{DATA:thread}\] %{LOGLEVEL:level} %{DATA:logger} - %{GREEDYDATA:message}" }
  }
  
  if [level] == "ERROR" {
    mutate { add_tag => [ "error" ] }
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

## Operaciones Comunes

### 1. Verificar Salud

```bash
#!/bin/bash
# health-check.sh

echo "=== Liveness ===" 
curl -s http://localhost:8080/actuator/health/liveness | jq .

echo "=== Readiness ===" 
curl -s http://localhost:8080/actuator/health/readiness | jq .

echo "=== Database ===" 
curl -s http://localhost:8080/actuator/health | jq '.components.db.status'

echo "=== Migrations ===" 
curl -s http://localhost:8080/actuator/health | jq '.components.flyway.status'
```

### 2. Reinicar Gracefully

```bash
#!/bin/bash
# graceful-restart.sh

# Step 1: Marcar como "not ready" (dejar que requests actuales terminen)
kubectl set env deployment/keygo-server SHUTDOWN_GRACE_PERIOD=60 -n production

# Step 2: Esperar a que se drenen requests
sleep 30

# Step 3: Reiniciar pods
kubectl rollout restart deployment/keygo-server -n production

# Step 4: Esperar a que nuevos pods estén ready
kubectl rollout status deployment/keygo-server -n production
```

### 3. Escalar

```bash
# Aumentar réplicas
kubectl scale deployment keygo-server --replicas=5 -n production

# Verificar
kubectl get deployment keygo-server -n production
```

### 4. Actualizar Versión

```bash
# Step 1: Build y push nueva imagen
docker build -t registry.example.com/keygo-server:1.0.1 .
docker push registry.example.com/keygo-server:1.0.1

# Step 2: Actualizar deployment
kubectl set image deployment/keygo-server \
  keygo-server=registry.example.com/keygo-server:1.0.1 \
  -n production

# Step 3: Esperar rollout
kubectl rollout status deployment/keygo-server -n production
kubectl get pods -n production

# Step 4: Monitorear logs
kubectl logs -f -n production deployment/keygo-server
```

### 5. Rollback

```bash
# Verificar historia
kubectl rollout history deployment/keygo-server -n production

# Rollback a versión anterior
kubectl rollout undo deployment/keygo-server -n production
kubectl rollout status deployment/keygo-server -n production
```

---

## Troubleshooting

### Pod no inicia

```bash
# Ver estado
kubectl describe pod keygo-server-xxx -n production

# Ver logs de startup
kubectl logs keygo-server-xxx -n production

# Causas comunes:
# 1. Imagen no existe: docker pull en registry
# 2. BD no está lista: esperar health check de postgres
# 3. Memory limit bajo: aumentar en deployment
```

### Alto uso de memoria

```bash
# Verificar heap
curl http://localhost:8080/actuator/metrics/jvm.memory.used | jq '.measurements'

# Forzar GC (último recurso)
curl -X POST http://localhost:8080/actuator/shutdown

# Opciones de JVM en variables de entorno:
JAVA_OPTS="-Xms2g -Xmx3g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Latencia Alta

```bash
# Ver request times
curl http://localhost:8080/actuator/prometheus | grep http_request_duration

# Analizar en logs
grep "took.*ms" /var/log/keygo/app.log | awk '{print $NF}' | sort -n | tail -10

# Causas:
# - BD lenta: check query times en EXPLAIN
# - Red congestionada: check network metrics
# - GC pauses: monitor jvm_gc_* metrics
```

### Base de datos corrupta

```bash
# Verificar integridad
docker exec keygo-postgres pg_dump -U keygo keygo | wc -l

# Restaurar desde backup
docker exec keygo-postgres psql -U keygo keygo < backup.sql

# Si Flyway falló:
# 1. Marcar migración como fallida:
UPDATE flyway_schema_history SET success = false WHERE version = 'X';

# 2. Investigar error
docker logs keygo-server | grep "Flyway"

# 3. Arreglar script en code + redeploy
```

---

## Backup & Disaster Recovery

### Backup automático

```bash
#!/bin/bash
# backup.sh

BACKUP_DIR="/backups/keygo"
DATE=$(date +%Y%m%d_%H%M%S)

docker exec keygo-postgres pg_dump -U keygo keygo | \
  gzip > "$BACKUP_DIR/keygo_$DATE.sql.gz"

# Mantener últimos 30 días
find $BACKUP_DIR -name "keygo_*.sql.gz" -mtime +30 -delete
```

**Cron:**
```
0 2 * * * /scripts/backup.sh  # 2am daily
```

### Recovery

```bash
# Restore
gunzip < backup.sql.gz | \
  docker exec -i keygo-postgres psql -U keygo keygo

# Verificar
curl http://localhost:8080/actuator/health
```

---

## Checklist: Pre-Deploy

Antes de deployar a producción:

- [ ] **Tests pasando** (`./mvnw clean verify`)
- [ ] **Imagen built y scaneada** (vulnerabilities, size < 500MB)
- [ ] **Variables de entorno configuradas** (no hardcodeadas)
- [ ] **Secrets rotados** (DB password, JWT signing key, etc.)
- [ ] **Migrations reversibles** (rollback plan)
- [ ] **Health checks configurados**
- [ ] **Logging activado** (level: INFO, not DEBUG)
- [ ] **Monitoreo activado** (alertas configuradas)
- [ ] **Backup policy confirmado** (daily backup running)
- [ ] **Runbook revisado** (equipo conoce el plan)

---

## Referencias

| Aspecto | Ubicación |
|---|---|
| **Actuator** | `docs/development/DEBUG_GUIDE.md` |
| **Error handling** | `docs/design/api/ERROR_CATALOG.md` |
| **Authorization** | `docs/design/AUTHORIZATION_PATTERNS.md` |
| **Config example** | `keygo-run/src/main/resources/application.yml` |
| **Docker setup** | `docker-compose.yml` |
| **Health checks** | `/actuator/health` endpoint |
| **Metrics** | `/actuator/prometheus` endpoint |

---

**Última actualización:** 2026-04-10  
**Estado:** Completado para Sprint 2  
**Próxima revisión:** Cuando se vaya a producción

