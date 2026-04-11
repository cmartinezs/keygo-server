# Debugging — Guía Práctica

**Propósito:** Cómo investigar y resolver errores sin perder tiempo.

---

## Activar DEBUG

### Opción 1: Profile `debug`

```bash
# En keygo-run/src/main/resources/application-debug.yml:
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        generate_statistics: true
  
logging:
  level:
    root: INFO
    io.cmartinezs.keygo: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
    org.springframework.data.jpa: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

```bash
./mvnw spring-boot:run -pl keygo-run -Dspring.profiles.active=debug
```

### Opción 2: IDE Debugger

IntelliJ: Right-click test/main → Debug  
VS Code: Run & Debug (F5)

---

## Error 401 UNAUTHORIZED

**Causas posibles:**
1. Token no pasado en header
2. Token expirado
3. Token inválido (signature/format)

**Diagnóstico:**

```bash
# ¿Header correcto?
curl -H "Authorization: Bearer $token" http://localhost:8080/keygo-server/api/v1/tenants

# ¿Token válido? (decode sin verificar firma)
echo $token | jq -R 'split(".") | .[1] | @base64d | fromjson'

# ¿Expirado?
echo $token | jq -R 'split(".") | .[1] | @base64d | fromjson | .exp'
date +%s  # Comparar: exp > now?
```

**Solución:**
- Usar token válido: `./docs/scripts/test-service-info.sh` (genera token de test)
- Refresh token si expirado

---

## Error 403 ACCESS_DENIED

**Causas posibles:**
1. Token válido, roles insuficientes
2. Tenant mismatch (token slug ≠ request slug)
3. Scope incorrecto

**Diagnóstico:**

```bash
# ¿Qué roles?
echo $token | jq -R 'split(".") | .[1] | @base64d | fromjson | .roles'

# ¿Qué tenant?
echo $token | jq -R 'split(".") | .[1] | @base64d | fromjson | .iss'

# ¿Endpoint requiere ADMIN?
grep -r "@PreAuthorize.*ADMIN" keygo-api/src | grep "GET.*tenants"
```

**Solución:**
- Verificar roles en token vs endpoint
- Validar tenant_slug en URL vs token claim
- Ver `docs/design/AUTHORIZATION_PATTERNS.md`

---

## Error 500 OPERATION_FAILED

**Causas probables:**
1. Excepción no mapeada
2. BD no disponible
3. Migración Flyway fallida

**Diagnóstico:**

```bash
# 1. Revisar logs (últimas 50 líneas)
tail -50 $PROJECT/target/spring.log | grep -E "ERROR|Exception|Stacktrace"

# 2. ¿Base de datos?
./docs/scripts/db/psql.sh
SELECT 1;

# 3. ¿Migraciones ok?
SELECT version, success FROM flyway_schema_history 
ORDER BY installed_rank DESC LIMIT 5;
# Si success=false → última migración falló

# 4. Run con DEBUG
./mvnw spring-boot:run -pl keygo-run -Dspring.profiles.active=debug 2>&1 | grep -A 10 "Exception"
```

**Solución:**
- Leer stacktrace completo en logs
- Verificar BD está up
- Si migración falló: `./docs/scripts/db/migrate.sh`

---

## Error 400 INVALID_INPUT

**Causas:**
1. Body JSON malformado
2. Validación Bean Validation falla
3. Campo tipo incorrecto

**Diagnóstico:**

```bash
# Ver respuesta completa con fieldErrors
curl -X POST http://localhost:8080/keygo-server/api/v1/tenants \
  -H "Content-Type: application/json" \
  -d '{"slug": ""}' \
  -H "Authorization: Bearer $token" | jq .

# Resultado incluirá:
# {
#   "code": "INVALID_INPUT",
#   "fieldErrors": [
#     {"field": "slug", "message": "must not be blank"}
#   ]
# }
```

**Solución:**
- Validar schema vs request
- Ver `docs/design/patterns/VALIDATION_STRATEGY.md`

---

## Error 409 DUPLICATE_RESOURCE

**Causa:** Violación de constraint unique.

**Diagnóstico:**

```bash
# ¿Recurso ya existe?
./docs/scripts/db/psql.sh
SELECT * FROM tenants WHERE slug = 'my-slug';
SELECT * FROM tenant_users WHERE email = 'test@example.com';
```

**Solución:**
- Usar slug/email único
- O permitir reuso (idempotent)

---

## Error 404 RESOURCE_NOT_FOUND

**Diagnóstico:**

```bash
# ¿ID existe?
./docs/scripts/db/psql.sh
SELECT * FROM tenants WHERE id = 'abc-123';

# Si no existe → error esperado
```

---

## Inspeccionar Base de Datos

```bash
# Conectar
./docs/scripts/db/psql.sh

# Comandos útiles
\dt                    # Ver tablas
\d tabla_name          # Ver estructura
SELECT COUNT(*) FROM tabla;
SELECT * FROM tabla LIMIT 5;

# Ver índices
SELECT * FROM pg_indexes WHERE tablename = 'tabla';

# Ver constraints
SELECT * FROM information_schema.key_column_usage 
WHERE table_name = 'tabla';
```

---

## Debug de Tests

```bash
# Run test específico con DEBUG
./mvnw test -pl keygo-api \
  -Dtest=TenantControllerTest#testCreateTenant \
  -Dorg.slf4j.simpleLogger.defaultLogLevel=debug

# IDE: Right-click test → Debug

# Ver cobertura
./mvnw verify -pl keygo-app
# Report: target/site/jacoco/index.html
```

---

## Queries Lentas

```yaml
# application-debug.yml
spring:
  jpa:
    properties:
      hibernate:
        generate_statistics: true
        session:
          events:
            log: true

logging:
  level:
    org.hibernate.stat.Statistics: DEBUG
```

Buscar en logs: "N+1", "SLOWQUERY"

---

## Performance: JFR (Java Flight Recorder)

```bash
./mvnw spring-boot:run -pl keygo-run \
  -XX:+UnlockDiagnosticVMOptions \
  -XX:+DebugNonSafepoints \
  -XX:StartFlightRecording=filename=recording.jfr,duration=60s

# Abrir en JDK Mission Control
jmc recording.jfr
```

---

## Debugging de JWT

```bash
# 1. ¿Token generado correctamente?
POST http://localhost:8080/keygo-server/oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&username=user@example.com&password=pass

# 2. Decodificar y ver claims
echo $token | jq -R 'split(".") | .[0] | @base64d | fromjson'  # Header
echo $token | jq -R 'split(".") | .[1] | @base64d | fromjson'  # Payload
echo $token | jq -R 'split(".") | .[2]'                        # Signature (base64)

# 3. Verificar JWKS
curl http://localhost:8080/keygo-server/.well-known/jwks.json | jq .
```

---

## Debugging de CORS

```yaml
logging:
  level:
    org.springframework.web.cors: DEBUG
```

Buscar en logs: "CORS request", "CORS denied", "CORS approved"

---

## Checklist de Debugging

1. ✅ ¿Logs? → `tail -50 target/spring.log | grep ERROR`
2. ✅ ¿BD? → `./docs/scripts/db/psql.sh` → `SELECT 1;`
3. ✅ ¿Migraciones? → `SELECT version FROM flyway_schema_history;`
4. ✅ ¿Token? → Decodificar y verificar roles/claims
5. ✅ ¿@PreAuthorize? → Grep endpoint, verificar roles
6. ✅ ¿Validación? → Ver fieldErrors en respuesta
7. ✅ ✅ ¿Profile debug? → `./mvnw spring-boot:run -Dspring.profiles.active=debug`

---

## Scripts Útiles

```bash
./docs/scripts/quick-start.sh          # Setup completo
./docs/scripts/db/start.sh             # PostgreSQL up
./docs/scripts/db/migrate.sh           # Flyway
./docs/scripts/db/psql.sh              # Connect
./docs/scripts/test-service-info.sh    # GET /service-info (genera token)
./docs/scripts/test-response-codes.sh  # GET /response-codes
```

---

**Última actualización:** 2026-04-10
