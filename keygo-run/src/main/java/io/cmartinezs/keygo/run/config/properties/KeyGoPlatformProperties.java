package io.cmartinezs.keygo.run.config.properties;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración para la plataforma KeyGo.
 *
 * <p>Incluye la allowlist de redirect URIs permitidas para el flujo PKCE
 * y el nombre de la aplicación de plataforma.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "keygo.platform")
public class KeyGoPlatformProperties {

  /** URIs de redirección permitidas para el flujo PKCE de plataforma. */
  private List<String> allowedRedirectUris = List.of("http://localhost:5173/callback");

  /** Nombre visible de la aplicación de plataforma. */
  private String applicationName = "KeyGo Platform";
}
