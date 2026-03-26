package io.cmartinezs.keygo.run.config.properties;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for CORS settings.
 * <p>Propiedades de configuración para ajustes de CORS.
 *
 * <p>Configured via {@code keygo.cors.*} in {@code application.yml}.
 * Override per environment via env var (e.g., set YAML list directly
 * or use {@code KEYGO_CORS_ALLOWED_ORIGINS_0=https://app.example.com}).
 *
 * @author cmartinezs
 * @version 1.0
 */
@Component
@ConfigurationProperties(prefix = "keygo.cors")
@Getter
@Setter
public class KeyGoCorsProperties {

  /**
   * Allowed origins for CORS. Defaults to the local Vite dev server.
   * In production, set to the actual frontend origin(s).
   */
  private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:5173"));

  /**
   * Allowed HTTP methods. Includes OPTIONS for preflight.
   */
  private List<String> allowedMethods = new ArrayList<>(
      List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

  /**
   * Allowed request headers. "*" tells Spring to echo back whatever the preflight requests.
   */
  private List<String> allowedHeaders = new ArrayList<>(List.of("*"));

  /**
   * Whether to allow credentials (cookies, Authorization headers).
   * Required to maintain JSESSIONID across /oauth2/authorize → /account/login.
   */
  private boolean allowCredentials = true;

  /**
   * How long (in seconds) the browser may cache preflight results.
   */
  private long maxAge = 3600L;
}

