# T-108 — Plan de Implementación

## Reglas críticas antes de implementar

- [ ] `keygo-app` no tiene dependencias Spring — `GeoIpPort` y `GeoIpLocation` son Java puro
- [ ] Jackson 3: imports `tools.jackson.databind.*` (nunca `com.fasterxml.*`)
- [ ] Sin migración Flyway — solo cambios en código
- [ ] Compilar siempre con `-am`: `./mvnw -pl keygo-infra -am compile -q`

---

## Fase A — Capa App (keygo-app)

### A1 — Crear `GeoIpLocation.java`

**Ruta:** `keygo-app/src/main/java/io/cmartinezs/keygo/app/user/model/GeoIpLocation.java`

```java
package io.cmartinezs.keygo.app.user.model;

public record GeoIpLocation(String city, String region, String country, String countryCode) {

    public String format() {
        if (city != null && !city.isBlank()) {
            return city + ", " + country;
        }
        return country != null ? country : "";
    }
}
```

### A2 — Crear `GeoIpPort.java`

**Ruta:** `keygo-app/src/main/java/io/cmartinezs/keygo/app/user/port/GeoIpPort.java`

```java
package io.cmartinezs.keygo.app.user.port;

import io.cmartinezs.keygo.app.user.model.GeoIpLocation;
import java.util.Optional;

public interface GeoIpPort {
    Optional<GeoIpLocation> resolve(String ipAddress);
}
```

### A3 — Actualizar `SessionInfoResult.java`

Agregar `String location` como último campo del record:
```java
public record SessionInfoResult(
    UUID sessionId, String status, String userAgent, String ipAddress,
    Instant createdAt, Instant lastAccessedAt, Instant expiresAt,
    boolean isCurrent,
    String location) {}   // ← nuevo
```

### A4 — Actualizar `ListUserSessionsUseCase.java`

1. Agregar `GeoIpPort geoIpPort` al constructor
2. En `toResult()`, agregar resolución:
   ```java
   String location = geoIpPort.resolve(session.getIpAddress())
       .map(GeoIpLocation::format).orElse(null);
   ```
3. Incluir `location` en el constructor de `SessionInfoResult`

---

## Fase B — Capa Infra (keygo-infra)

### B1 — Crear `NoOpGeoIpAdapter.java`

**Ruta:** `keygo-infra/src/main/java/io/cmartinezs/keygo/infra/geoip/NoOpGeoIpAdapter.java`

```java
public class NoOpGeoIpAdapter implements GeoIpPort {
    @Override
    public Optional<GeoIpLocation> resolve(String ipAddress) {
        return Optional.empty();
    }
}
```

### B2 — Crear `IpApiGeoIpAdapter.java`

**Ruta:** `keygo-infra/src/main/java/io/cmartinezs/keygo/infra/geoip/IpApiGeoIpAdapter.java`

Lógica:
1. Si `ipAddress` es null o vacío → `Optional.empty()`
2. Detectar IPs privadas → `Optional.empty()` (sin HTTP call)
3. Consultar caché `ConcurrentHashMap<String, Optional<GeoIpLocation>>`
4. Si no está en caché y `cache.size() < cacheMaxSize` → `computeIfAbsent`
5. HTTP GET → parse JSON → return `Optional.of(GeoIpLocation)` o `Optional.empty()`

**Detección de IPs privadas:**
```java
private static boolean isPrivateIp(String ip) {
    if (ip == null || ip.isBlank() || ip.equals("::1") || ip.startsWith("127.")) return true;
    if (ip.startsWith("10.")) return true;
    if (ip.startsWith("192.168.")) return true;
    if (ip.startsWith("0.0.0.0")) return true;
    // 172.16.0.0 – 172.31.255.255
    if (ip.startsWith("172.")) {
        String[] parts = ip.split("\\.");
        if (parts.length >= 2) {
            int second = Integer.parseInt(parts[1]);
            return second >= 16 && second <= 31;
        }
    }
    return false;
}
```

**HTTP call:**
```java
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create(apiUrl + "/" + ip + "?fields=city,regionName,country,countryCode"))
    .timeout(Duration.ofSeconds(timeoutSeconds))
    .GET().build();

HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
```

**Parse con Jackson 3:**
```java
// tools.jackson.databind.JsonNode
JsonNode node = mapper.readTree(response.body());
if (!"success".equals(node.path("status").asText())) return Optional.empty();
return Optional.of(new GeoIpLocation(
    node.path("city").asText(null),
    node.path("regionName").asText(null),
    node.path("country").asText(null),
    node.path("countryCode").asText(null)));
```

### B3 — Crear `KeyGoGeoIpProperties.java`

**Ruta:** `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/properties/KeyGoGeoIpProperties.java`

```java
@ConfigurationProperties("keygo.session.geoip")
@Validated
public class KeyGoGeoIpProperties {
    private boolean enabled = false;
    private int timeoutSeconds = 3;
    private String apiUrl = "http://ip-api.com/json";
    private int cacheMaxSize = 500;
    // getters/setters
}
```

---

## Fase C — Capa API (keygo-api)

### C1 — Actualizar `AccountSessionData.java`

Agregar `String location` al record:
```java
public record AccountSessionData(
    UUID sessionId, String status,
    String browser, String os, String deviceType,
    String ipAddress,
    Instant createdAt, Instant lastAccessedAt, Instant expiresAt,
    boolean isCurrent,
    String location) {        // ← nuevo

  public static AccountSessionData from(SessionInfoResult result) {
    return new AccountSessionData(
        result.sessionId(), result.status(),
        UserAgentParser.extractBrowser(result.userAgent()),
        UserAgentParser.extractOs(result.userAgent()),
        UserAgentParser.extractDeviceType(result.userAgent()),
        result.ipAddress(),
        result.createdAt(), result.lastAccessedAt(), result.expiresAt(),
        result.isCurrent(),
        result.location());   // ← nuevo
  }
}
```

---

## Fase D — Spring Wiring (keygo-run)

### D1 — Actualizar `ApplicationConfig.java`

1. Import `KeyGoGeoIpProperties`, `GeoIpPort`, `IpApiGeoIpAdapter`, `NoOpGeoIpAdapter`
2. Agregar bean:
```java
@Bean
public GeoIpPort geoIpPort(KeyGoGeoIpProperties geoIpProperties) {
    if (geoIpProperties.isEnabled()) {
        return new IpApiGeoIpAdapter(
            geoIpProperties.getApiUrl(),
            geoIpProperties.getTimeoutSeconds(),
            geoIpProperties.getCacheMaxSize());
    }
    return new NoOpGeoIpAdapter();
}
```
3. Actualizar `listUserSessionsUseCase()` para inyectar `GeoIpPort geoIpPort`

### D2 — Actualizar `application.yml`

```yaml
keygo:
  session:
    geoip:
      enabled: false
      timeout-seconds: 3
      api-url: "http://ip-api.com/json"
      cache-max-size: 500
```

---

## Fase E — Tests

### E1 — `GeoIpLocationTest` (keygo-app)

- `format_withCityAndCountry_returnsCityCommaCountry()`
- `format_withNullCity_returnsCountryOnly()`
- `format_withBlankCity_returnsCountryOnly()`
- `format_withNullCountry_returnsEmpty()`

### E2 — `IpApiGeoIpAdapterTest` (keygo-infra)

Usar Mockito para `HttpClient` o sobreescribir `sendRequest()` como método protegido testeable.
Alternativa más simple: extraer `doHttpCall(String ip)` como método paquete-visible y usarlo con subclase en tests.

Casos:
- IP privada `192.168.1.1` → `Optional.empty()` (sin HTTP)
- IP loopback `127.0.0.1` → `Optional.empty()` (sin HTTP)
- Respuesta `status: success` → `Optional.of(GeoIpLocation)`
- Respuesta `status: fail` → `Optional.empty()`
- Excepción HTTP → `Optional.empty()` (catch → log warn)

### E3 — `ListUserSessionsUseCaseTest` actualización

Agregar `@Mock GeoIpPort geoIpPort` al test existente.  
Actualizar `setUp()` para pasar `geoIpPort` al constructor.  
Agregar:
- Test: `execute_shouldIncludeLocationFromGeoIp()`
- Test: `execute_withPrivateIp_shouldHaveNullLocation()`

### E4 — Suite completa

```bash
./mvnw -pl keygo-app,keygo-infra,keygo-api test -q
```

---

## Fase F — Documentación AI

Actualizar al finalizar (sin orden explícita):
- `AGENTS.md` — listar `GeoIpPort`, `IpApiGeoIpAdapter`, `KeyGoGeoIpProperties`; indicar que `/account/sessions` retorna `location`
- `docs/ai/lecciones.md` — patrón null-object + feature flag para adapters opcionales
- `docs/ai/propuestas.md` + `ROADMAP.md` — marcar T-108 ✅ completada

---

## Verificación final

```bash
# Compile full stack
./mvnw -pl keygo-run -am compile -q

# Tests relevantes
./mvnw -pl keygo-app,keygo-infra,keygo-api test -q

# Smoke test local (con keygo.session.geoip.enabled=true)
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/keygo-server/api/v1/tenants/keygo/account/sessions
```
