# T-108 — Diseño Técnico

## Proveedor: ip-api.com

### Llamada HTTP

```
GET http://ip-api.com/json/{ip}?fields=city,regionName,country,countryCode
```

### Respuesta exitosa
```json
{
  "status": "success",
  "city": "Mexico City",
  "regionName": "Mexico City",
  "country": "Mexico",
  "countryCode": "MX",
  "query": "187.243.1.1"
}
```

### Respuesta IP privada
```json
{ "status": "fail", "message": "private range", "query": "192.168.1.1" }
```

### Límites
- Gratuito: ≤ 45 req/min
- Sin API key requerida (uso no comercial)
- HTTPS disponible en plan de pago

---

## Arquitectura hexagonal

```
keygo-app
  └── app/user/model/GeoIpLocation.java      ← value object (record)
  └── app/user/port/GeoIpPort.java           ← port OUT

keygo-infra
  └── infra/geoip/NoOpGeoIpAdapter.java     ← null-object (feature flag OFF)
  └── infra/geoip/IpApiGeoIpAdapter.java    ← implementación HTTP real

keygo-run
  └── config/properties/KeyGoGeoIpProperties.java  ← @ConfigurationProperties
  └── config/ApplicationConfig.java                ← @Bean GeoIpPort condicional
  └── resources/application.yml                    ← keygo.session.geoip.*
```

---

## GeoIpLocation (keygo-app)

```java
public record GeoIpLocation(String city, String region, String country, String countryCode) {

    /** "Mexico City, Mexico" o "Mexico" si city es null */
    public String format() {
        if (city != null && !city.isBlank()) {
            return city + ", " + country;
        }
        return country;
    }
}
```

---

## GeoIpPort (keygo-app)

```java
public interface GeoIpPort {
    Optional<GeoIpLocation> resolve(String ipAddress);
}
```

---

## IpApiGeoIpAdapter (keygo-infra)

### Detección de IPs privadas (sin llamada HTTP)

| Rango | Descripción |
|---|---|
| `127.0.0.0/8` | Loopback IPv4 |
| `::1` | Loopback IPv6 |
| `10.0.0.0/8` | Red privada clase A |
| `172.16.0.0/12` | Red privada clase B |
| `192.168.0.0/16` | Red privada clase C |
| `0.0.0.0` | Sin IP |

Para IPs privadas → retorna `Optional.empty()` sin llamada HTTP.

### Cache

```java
ConcurrentHashMap<String, Optional<GeoIpLocation>> cache = new ConcurrentHashMap<>();
// computeIfAbsent → resolve solo una vez por IP única
```

La caché vive durante el ciclo de vida de la JVM.  
No tiene eviction activa (las IPs únicas por deployment son acotadas).  
`cacheMaxSize` se usa para parar de cachear si se excede (prevenir memory leak bajo ataque).

### HTTP Client

```java
// Java 11+ built-in — sin nuevas dependencias
HttpClient client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(timeout))
    .build();
```

### Parse de respuesta

Jackson 3 (`tools.jackson.databind.ObjectMapper`) ya disponible en `keygo-infra` vía BOM.

```java
// Verificar status == "success" antes de mapear campos
if (!"success".equals(node.get("status").asText())) return Optional.empty();
```

---

## NoOpGeoIpAdapter (keygo-infra)

```java
public class NoOpGeoIpAdapter implements GeoIpPort {
    @Override
    public Optional<GeoIpLocation> resolve(String ipAddress) {
        return Optional.empty();
    }
}
```

---

## KeyGoGeoIpProperties (keygo-run)

```java
@ConfigurationProperties("keygo.session.geoip")
public class KeyGoGeoIpProperties {
    boolean enabled = false;
    int timeoutSeconds = 3;
    String apiUrl = "http://ip-api.com/json";
    int cacheMaxSize = 500;
}
```

---

## application.yml

```yaml
keygo:
  session:
    geoip:
      enabled: false                        # Habilitar en prod con proveedor configurado
      timeout-seconds: 3
      api-url: "http://ip-api.com/json"
      cache-max-size: 500
```

---

## ApplicationConfig wiring

```java
@Bean
public GeoIpPort geoIpPort(KeyGoGeoIpProperties props) {
    if (props.isEnabled()) {
        return new IpApiGeoIpAdapter(props.getApiUrl(), props.getTimeoutSeconds(), props.getCacheMaxSize());
    }
    return new NoOpGeoIpAdapter();
}

@Bean
public ListUserSessionsUseCase listUserSessionsUseCase(
    SigningKeyRepositoryPort signingKeyRepositoryPort,
    AccessTokenVerifierPort accessTokenVerifierPort,
    TenantRepositoryPort tenantRepositoryPort,
    SessionRepositoryPort sessionRepositoryPort,
    GeoIpPort geoIpPort) {       // ← nuevo parámetro
  return new ListUserSessionsUseCase(
      signingKeyRepositoryPort, accessTokenVerifierPort,
      tenantRepositoryPort, sessionRepositoryPort,
      geoIpPort);
}
```

---

## Flujo actualizado en ListUserSessionsUseCase

```java
private SessionInfoResult toResult(Session session, String reqUserAgent, String reqIpAddress) {
    boolean isCurrent = Objects.equals(session.getUserAgent(), reqUserAgent)
        && Objects.equals(session.getIpAddress(), reqIpAddress);

    String location = geoIpPort.resolve(session.getIpAddress())
        .map(GeoIpLocation::format)
        .orElse(null);

    return new SessionInfoResult(
        session.getId().value(),
        session.getStatus().getValue(),
        session.getUserAgent(),
        session.getIpAddress(),
        session.getCreatedAt(),
        session.getLastAccessedAt(),
        session.getExpiresAt(),
        isCurrent,
        location);   // ← nuevo
}
```
