# Análisis Detallado de Deficiencias — Documentación

**Fecha:** 2026-04-09  
**Documento:** Guía de implementación incremental  
**Audiencia:** AI Agent, Equipo de desarrollo

---

## Estructura del Análisis

Para cada deficiencia:
1. **Síntoma:** Qué ve el usuario
2. **Causa raíz:** Por qué existe
3. **Impacto:** A quién le duele y cómo
4. **Solución:** Qué documentar
5. **Pasos concretos:** Cómo ejecutar (para agente)
6. **Dependencias:** Qué leer primero
7. **Esfuerzo estimado:** Horas

---

## Deficiencia 1: Consolidación incompleta de RFCs

### Síntoma
```
Usuario que explora el repo:
 - docs/rfc/restructure-multitenant/ (10 files, 100+ KB)
 - docs/rfc/account-ui-proposal/ (5 files, 40+ KB)  
 - docs/rfc/t108-geoip-sessions/ (4 files, 20 KB)
 - docs/rfc/billing-contractor-refactor/ (8 files, 70 KB)

No sabe cuál es la fuente de verdad:
 ¿ARCHITECTURE.md en raíz?
 ¿API_SURFACE.md?
 ¿Algún RFC?
 ¿Todo a la vez?
```

### Causa Raíz

No existe un **proceso de RFC → canon**:
- RFCs se diseñan en `docs/rfc/`
- Se implementan (o se dejan pendientes)
- No hay checklist de "absorber a fuentes de verdad"
- `ARCHITECTURE.md` raíz resumido pero huérfano del detalle

### Impacto

- **Nuevo dev:** 2-3 horas buscando la versión correcta
- **Agentes AI:** Confusión sobre qué es canon
- **Maintenance:** Inconsistencias entre RFC y código real
- **Ejemplo real:** T-111 (RBAC) tiene RFC de 10 docs pero no está consolidada en `ARCHITECTURE.md`

### Solución

Crear **proceso de cierre de RFC y consolidación**:

```
RFC (diseño)
  ↓ (implementada)
docs/design/XXX.md (absorber decisiones)
  ↓
docs/archive/rfc-XXX-closure-YYYY-MM-DD.md (mover histórico)
  ↓
docs/archive/RFC_CLOSURE_LOG.md (index)
```

**Documentos a crear/actualizar:**

1. **`docs/ai/RFC_CLOSURE_PROCESS.md`** — Checklist + politique
2. **`docs/archive/RFC_CLOSURE_LOG.md`** — Índice de RFCs cerradas
3. **Actualizar `docs/design/ARCHITECTURE.md`** con decisiones de RFCs completadas

### Pasos Concretos para Implementación

**Paso 1: Crear RFC_CLOSURE_PROCESS.md**
```bash
cat > docs/ai/RFC_CLOSURE_PROCESS.md << 'EOF'
# RFC Closure Process

Cuando una RFC está implementada o decidida, aplicar este checklist:

- [ ] RFC técnicamente completa (código merged o decidida como no-hacer)
- [ ] Decisiones clave documentadas en `docs/design/*` (no solo en RFC)
- [ ] API contracts en OpenAPI + Postman (si aplica)
- [ ] Tests agregados (unitarios + integración)
- [ ] Cambios en AGENTS.md, AI_CONTEXT.md si afectan quick-start
- [ ] Lecciones aprendidas en docs/ai/lecciones.md
- [ ] RFC movida a docs/archive/rfc-{name}-closure-YYYY-MM-DD.md
- [ ] Entrada en RFC_CLOSURE_LOG.md con timestamp y canonical doc

## Plantilla de cierre

**RFC:** restructure-multitenant (ejemplo)
**Implementación:** Fases 5-6 completadas 2026-03-26
**Decisiones clave:**
  - Multi-tenancy resolution by path /{slug}/
  - Token roles mapping
  - Tenant auth cascade
**Canonical docs:**
  - docs/design/ARCHITECTURE.md §3 — Decisiones
  - docs/api/AUTH_FLOW.md §2 — Flujo
  - docs/api/BOOTSTRAP_FILTER.md §4 — Rutas públicas
**Closed:** 2026-04-09 by AI Agent
EOF
```

**Paso 2: Analizar cada RFC huérfana**
```bash
# Para cada RFC en docs/rfc/*/README.md:
# 1. Leer estado de implementación
# 2. Identificar qué decisiones merecen docs/design/*
# 3. Crear issues/propuestas para consolidación
```

**Paso 3: Consolidar decisiones en ARCHITECTURE.md**
- Secciones nuevas o referencias a `docs/design/`
- Ejemplo: "Multi-tenancy: ver [`docs/rfc/restructure-multitenant/02-modelo-identidad-multitenancy.md`](../docs/rfc/restructure-multitenant/02-modelo-identidad-multitenancy.md) para detalle; implementada en Fases 5-6"

### Dependencias

- Leer: `docs/rfc/*/README.md` (estado de RFCs)
- Leer: código de implementaciones (git log --grep="RFC\|Phase")

### Esfuerzo Estimado

- RFC_CLOSURE_PROCESS.md: 1 h
- RFC_CLOSURE_LOG.md (esqueleto): 0.5 h
- Consolidar 4 RFCs principales: 8 h (2 h c/u)
- **Total Sprint:** ~10 h

---

## Deficiencia 2: Falta de catálogo consolidado de errores

### Síntoma
```java
// Usuario quiere retornar error personalizado:
// ¿Qué ResponseCode uso?
// ¿Qué fieldErrors debo poner?
// ¿Cómo lo documenta en OpenAPI?

public void createTenant(@Valid CreateTenantRequest req) {
  // Validación de tenant slug único — ¿ErrorCode?
  // TENANT_CREATION_FAILED? DUPLICATE_SLUG?
  // API docs no dicen.
}
```

### Causa Raíz

- `ResponseCode` enum en código sin documentación acompañante
- Cada developer guessa o copia patrón más cercano
- No existe guía de "cuándo retornar X vs Y"
- i18n parcialmente implementada sin ejemplo de integración

### Impacto

- Inconsistencia entre endpoints (error con mismo problema, codes diferentes)
- Documentación de OpenAPI incompleta (falta matriz de errores posibles)
- Frontend debe reverse-engineer los codes
- Cambios en `ResponseCode` rompen sin notificar a consumidores

### Solución

Crear **`docs/design/ERROR_CATALOG.md`** — Catálogo centralizado con:

1. Matriz de `ResponseCode` → caso de uso
2. Reglas de cuándo retornar cada code
3. Estructura de `ErrorData` por contexto
4. Cómo documentar en OpenAPI
5. Cómo usar i18n para `clientMessage`
6. Ejemplos de error responses

### Pasos Concretos

**Paso 1: Extraer lista de ResponseCodes**
```bash
grep -r "enum ResponseCode" keygo-api/src --include="*.java" -A 50 | \
  grep "^\s*[A-Z_]*," | \
  awk '{print $1}' > /tmp/codes.txt

# Resultado esperado: ~45 codes
```

**Paso 2: Crear tabla de codes con contexto**
```bash
cat > docs/design/ERROR_CATALOG.md << 'EOF'
# Catálogo de Códigos de Error

## ResponseCode — Referencia Completa

| Code | Contexto | HTTP | Causa | Solución |
|---|---|---|---|---|
| `INVALID_INPUT` | Siempre | 400 | Body inválido, formato, missing params | Verificar request schema en OpenAPI |
| `INVALID_CREDENTIALS` | Login | 401 | Usuario/password incorrecto | Reintentar o forgot-password |
| `ACCESS_DENIED` | Autorización | 403 | Token válido pero sin permisos | Usar otra cuenta o rol |
| `RESOURCE_NOT_FOUND` | Lookup | 404 | ID no existe | Verificar ID en listar endpoint |
| ... (45 más)

## Estructura de ErrorData

### Siempre presente
```json
{
  "code": "INVALID_INPUT",
  "origin": "CLIENT_REQUEST|INTERNAL",
  "clientMessage": "localized per Accept-Language"
}
```

### Si origin = CLIENT_REQUEST + USER_INPUT
```json
{
  "fieldErrors": [
    {
      "field": "email",
      "error": "invalid_format",
      "attemptedValue": "notanemail"
    }
  ]
}
```

### Si origin = INTERNAL
```json
{
  "internalCause": "database_unavailable",
  "traceId": "abc123"
}
```

## Cómo documentar en OpenAPI

```java
@Operation(summary = "Create tenant")
@ApiResponse(
  responseCode = "400",
  description = "Invalid input",
  content = @Content(
    schema = @Schema(implementation = ApiErrorData.class),
    examples = @ExampleObject(
      name = "invalid_slug_format",
      value = "..."
    )
  )
)
```

## Cuándo usar cada code

### INVALID_INPUT (400)
- Parámetro en path/query inválido
- Body falla validación Bean Validation
- Formato esperado no coincide
- **Nunca para:** lógica de negocio (ej. "tenant ya existe")

### OPERATION_FAILED (500)
- Excepción no mapeada / inesperada
- **Nunca para:** errores de validación o lógica conocida
EOF
```

**Paso 3: Mapear codes actuales a categorías**
```bash
# Leer ResponseCode.java, extraer descripción de cada code
# Agrupar por:
#  - Validación (400)
#  - Autenticación (401)
#  - Autorización (403)
#  - Recurso no encontrado (404)
#  - Conflicto (409)
#  - Negocio (custom)
#  - Internal (500)
```

**Paso 4: Crear ejemplos de uso**
```java
// Ejemplo 1: Validación de dominio
CreateTenantRequest req = ...;
if (tenantRepository.existsBySlug(req.getSlug())) {
  throw new TenantAlreadyExistsException(req.getSlug());
  // Maps to: TENANT_CREATION_FAILED (409)
}

// Ejemplo 2: Validación de entrada
@NotNull @Valid CreateTenantRequest req
// Violation de @NotNull → INVALID_INPUT (400)
// Acceso documentado en OpenAPI
```

### Dependencias

- Leer: `keygo-api/src/**/ResponseCode.java`
- Leer: `keygo-api/src/**/ErrorData.java`
- Leer: GlobalExceptionHandler (mapeo de excepciones)
- Ejecutar: `grep -r "ResponseCode\." keygo-api/src | wc -l` (cuántos codes en uso)

### Esfuerzo Estimado

- Extraer códigos y contexto: 2 h
- Crear matriz + descripciones: 2 h
- Documentar estructura + ejemplos: 2 h
- Validar contra código real: 1 h
- **Total:** 7 h

---

## Deficiencia 3: Guía incompleta de integración frontend

### Síntoma
```
Frontend dev abre docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md
 §14 lista endpoints pero:
  - Algunos desactualizados (ej. `/account/profile` sin ejemplos)
  - No explica OAUTH2 flow end-to-end
  - No documenta cómo manejo de errores por ResponseCode
  - No muestra cómo pasar Accept-Language
  - No explica diferencia TENANT_USER vs ADMIN_TENANT auth
  
Frontend dev abre /v3/api-docs (OpenAPI)
 - Está correcto pero denso
 - No hay ejemplos de flujos completos
 - No hay patrones de error handling
```

### Causa Raíz

- Documento last-updated 2026-04-03 (antes de últimos cambios)
- No existe **proceso de actualización con cada endpoint nuevo**
- Falta de sección de "patterns y ejemplos de código"
- OpenAPI es especificación, no tutorial

### Impacto

- Frontend duplica lógica de error handling
- Descubrimiento de API lento
- Confusion sobre autenticación (qué header, cuándo)
- Onboarding de frontend dev ~3-4 horas

### Solución

Actualizar `docs/keygo-ui/FRONTEND_DEVELOPER_GUIDE.md` con:

1. **Sección de OAuth2 flow end-to-end** — con diagramas y código
2. **Patterns de error handling** — por ResponseCode
3. **Autenticación por contexto** — PLATFORM_USER vs TENANT_USER vs ADMIN_TENANT
4. **Tabla actualizada de endpoints** — con ejemplos de request/response
5. **Variables de entorno** — qué setear en `.env`

### Pasos Concretos

**Paso 1: Crear subsección de OAuth2 flow**
```markdown
## OAuth2 / OIDC Flows

### Authorization Code with PKCE (usuario final)

1. Frontend genera PKCE challenge
   ```js
   const code_challenge = generateCodeChallenge(code_verifier);
   ```

2. Redirige a `/authorize?client_id=...&code_challenge=...&redirect_uri=...`

3. Backend emite authorization_code (redirect a frontend)

4. Frontend canjea con `/token` → get access_token

### Client Credentials (M2M)
... (ejemplo de cómo usar sin usuario)

### Hosted Login
... (cómo iniciar desde app cliente remota)
```

**Paso 2: Crear tabla de endpoints actualizada**
```bash
# Para cada controller en keygo-api:
# GET /api/v1/tenants/{slug}
#   Auth: Bearer ADMIN_TENANT|ADMIN
#   Request: none
#   Response: BaseResponse<TenantData>
#   Errors: TENANT_NOT_FOUND (404), TENANT_ARCHIVED (409)
#   Example:
#     curl -H "Authorization: Bearer $token" \
#          https://api.example.com/keygo-server/api/v1/tenants/acme
```

**Paso 3: Crear sección "Error Handling"**
```markdown
## Error Handling Patterns

### Estructura de error (siempre)
```json
{
  "code": "TENANT_NOT_FOUND",
  "origin": "CLIENT_REQUEST",
  "clientMessage": "El tenant no existe"
}
```

### Por ResponseCode

| Code | HTTP | Qué hacer en frontend |
|---|---|---|
| `INVALID_INPUT` | 400 | Mostrar fieldErrors a usuario, rehighlightear inputs |
| `ACCESS_DENIED` | 403 | Redirigir a login o mostrar "sin permisos" |
| `RESOURCE_NOT_FOUND` | 404 | Ir a listar endpoint, usuario no existe |
| `OPERATION_FAILED` | 500 | Mostrar error genérico, contact support, log traceId |

### Código de ejemplo
```typescript
async function apiCall(method, path, body?) {
  const res = await fetch(`${API_BASE}${path}`, {
    method,
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept-Language': navigator.language,
      'Content-Type': 'application/json',
    },
    credentials: 'include',
    body: body ? JSON.stringify(body) : undefined,
  });

  if (!res.ok) {
    const error = await res.json();
    switch(error.code) {
      case 'INVALID_INPUT':
        // Mostrar fieldErrors
        break;
      case 'ACCESS_DENIED':
        // Redirigir a login
        redirectTo('/login');
        break;
      default:
        // Error genérico
        showNotification(error.clientMessage);
    }
    throw error;
  }

  return res.json();
}
```
```

**Paso 4: Actualizar table de endpoints**
```bash
# Ejecutar linter de OpenAPI:
./docs/scripts/extract-endpoints.sh > /tmp/endpoints.md
# Luego merge a FRONTEND_DEVELOPER_GUIDE.md §14
```

### Dependencias

- Leer: `/v3/api-docs` (actual contract)
- Leer: `docs/api/AUTH_FLOW.md`
- Leer: `docs/design/ERROR_CATALOG.md` (cuando esté)
- Código: todos los controllers

### Esfuerzo Estimado

- Crear OAuth2 flow section: 2 h
- Actualizar table de endpoints: 3 h
- Crear error handling patterns: 2 h
- Crear ejemplos de código (TS/JS): 2 h
- Validar con código actual: 1 h
- **Total:** 10 h

---

## Deficiencia 4: Falta de guía de debugging

### Síntoma
```
Nuevo dev:
 > ./docs/scripts/quick-start.sh
 > curl -H "Authorization: Bearer $token" http://localhost:8080/keygo-server/api/v1/tenants

ERROR: 500 OPERATION_FAILED
Status: Application error

¿Ahora qué?
 - ¿Revisar logs?
 - ¿Setear DEBUG?
 - ¿Base de datos no migrada?
 - ¿Token invalido?
 
No hay guía.
```

### Causa Raíz

- No existe `docs/development/DEBUGGING.md`
- Logs sin estructura clara
- No documentado cómo activar DEBUG en Spring Data
- No existe troubleshooting matrix

### Impacto

- Nuevo dev pierde 2-3 horas por error básico
- Preguntas repetidas en Slack/issues
- Frustración temprana en onboarding

### Solución

Crear **`docs/development/DEBUGGING.md`** con:

1. Cómo leer logs
2. Cómo activar DEBUG por módulo
3. Cómo inspeccionar base de datos
4. Matriz de síntomas → solución
5. Cómo debuggear tests

### Pasos Concretos

**Paso 1: Crear DEBUGGING.md**
```bash
cat > docs/development/DEBUGGING.md << 'EOF'
# Debugging Guide

## Primeros pasos

### 1. Revisar logs

```bash
# Ver logs en tiempo real
./mvnw spring-boot:run -pl keygo-run 2>&1 | grep -E "ERROR|WARN|Exception"

# Ver logs con timestamps
./mvnw spring-boot:run -pl keygo-run | tail -100
```

### 2. Activar DEBUG en Spring Data

```bash
# Crear application-debug.yml en keygo-run/src/main/resources
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        generate_statistics: true
  
logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.data.jpa: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

Luego ejecutar:
```bash
./mvnw spring-boot:run -pl keygo-run -Dspring.profiles.active=debug
```

### 3. Inspeccionar base de datos

```bash
# Conectar a PostgreSQL
./docs/scripts/db/psql.sh  # script que abre psql

# Ver tablas
\dt

# Verificar migraciones aplicadas
SELECT version FROM flyway_schema_history ORDER BY installed_rank DESC;

# Contar filas en tabla
SELECT COUNT(*) FROM tenant_users;
```

## Matriz de síntomas

### Síntoma: 401 UNAUTHORIZED

**Causas posibles:**
1. Token no pasado en Authorization header
2. Token expirado
3. Token inválido (format o signature)

**Diagnóstico:**
```bash
# ¿Header correcto?
curl -H "Authorization: Bearer $token" http://localhost:8080/...

# ¿Token válido?
# Decodificar (sin verificar firma):
echo $token | jq -R 'split(".") | .[1] | @base64d | fromjson'

# ¿Expirado?
# Ver claim 'exp':
echo $token | jq -R 'split(".") | .[1] | @base64d | fromjson' | jq '.exp'
date +%s  # Comparar con now
```

### Síntoma: 403 ACCESS_DENIED

**Causas posibles:**
1. Token válido pero roles insuficientes
2. Tenant mismatch (token slug != request slug)
3. Requerimiento de scope no satisfecho

**Diagnóstico:**
```bash
# ¿Qué roles tiene el token?
echo $token | jq -R 'split(".") | .[1] | @base64d | fromjson | .roles'

# ¿Qué tenant?
echo $token | jq -R 'split(".") | .[1] | @base64d | fromjson | .iss'

# ¿La ruta requiere ADMIN?
grep -r "@PreAuthorize.*ADMIN" keygo-api/src | grep \
  "GET.*tenants"
```

### Síntoma: 500 OPERATION_FAILED (sin detalle)

**Causas probables:**
1. Excepción no mapeada en GlobalExceptionHandler
2. Base de datos no disponible
3. Migración de Flyway fallida

**Diagnóstico:**
```bash
# 1. Revisar logs:
./mvnw spring-boot:run -pl keygo-run 2>&1 | tail -50

# 2. Verificar base de datos:
./docs/scripts/db/psql.sh
SELECT 1;  # ¿Conecta?

# 3. Verificar migraciones:
SELECT version, success FROM flyway_schema_history 
ORDER BY installed_rank DESC LIMIT 5;

# Si hay success=false, la última migración falló
```

## Debugging de Tests

### Ejecutar un test específico con DEBUG

```bash
./mvnw test -pl keygo-api \
  -Dtest=TenantControllerTest#testCreateTenant \
  -Dorg.slf4j.simpleLogger.defaultLogLevel=debug
```

### Debuggear con IDE

1. Abrir IDE (IntelliJ, VSCode)
2. Right-click en test → Run with Debug
3. Setear breakpoints
4. Step through

### Ver cobertura de tests

```bash
./mvnw verify -pl keygo-app
# Reporte en: target/site/jacoco/index.html
```

## Performance Debugging

### Identificar queries lentas

```bash
# Setear en application.yml
spring:
  jpa:
    properties:
      hibernate:
        session:
          events:
            log: true
        format_sql: true
        jdbc:
          fetch_size: 50
          batch_size: 10

# Luego en logs buscar: "SLOWQUERY"
```

### Profile con JFR

```bash
./mvnw spring-boot:run -pl keygo-run \
  -Dcom.sun.management.jmxremote=true \
  -Dcom.sun.management.jmxremote.port=9999 \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false \
  -XX:+UnlockCommercialFeatures \
  -XX:+DebugNonSafepoints \
  -XX:+FlightRecorder \
  -XX:StartFlightRecording=filename=recording.jfr,duration=60s

# Abrir recording.jfr en JDK Mission Control
jmc recording.jfr
```

## Debugging de autenticación

### Valider JWT manualmente

```bash
# 1. Generar token (si tienes credentials)
curl -X POST http://localhost:8080/keygo-server/oauth2/token \
  -d "grant_type=password&username=admin&password=pass" \
  -H "Content-Type: application/x-www-form-urlencoded"

# 2. Decodificar y ver claims
echo $token | jq -R '.'

# 3. Verificar firma (si tienes key pública)
# (implementar script de validación)
```

### Debugging de CORS

```bash
# Setear en application.yml
logging:
  level:
    org.springframework.web.cors: DEBUG

# Luego en logs buscar:
# "CORS request received"
# "CORS denied"
# "CORS request approved"
```

EOF
```

### Dependencias

- Experiencia local (haber debuggeado problemas)
- Leer: logs, `application.yml`, Spring Security docs

### Esfuerzo Estimado

- Base de troubleshooting: 2 h
- Matriz de síntomas: 3 h
- Ejemplos de comandos: 2 h
- Testing debugging: 1.5 h
- Validar con casos reales: 1.5 h
- **Total:** 10 h

---

## Resumen de Esfuerzos Sprint 1

| Deficiencia | Documento | Esfuerzo | Dependencias |
|---|---|---|---|
| 1 | ERROR_CATALOG.md | 7 h | ResponseCode.java, código actual |
| 2 | VALIDATION_STRATEGY.md | 5 h | Tests, código, lecciones |
| 3 | DEBUGGING.md | 10 h | Experiencia local |
| 4 | PATTERNS.md | 4 h | lecciones.md, ARCHITECTURE |
| 5 | ENDPOINT_CATALOG.md | 4 h | OpenAPI, Postman, controllers |
| **Total** | — | **30 h** | — |

**Estimación realista:** 5-7 días de trabajo full-time para una sola persona, o 2 semanas si paralelo.

---

**Próximo paso:** Ejecutar D-01 (ERROR_CATALOG) como sprint de prueba.

