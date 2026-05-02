package io.cmartinezs.keygo.infra.config;

import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propiedades de configuración para la UI en KeyGo.
 *
 * <p>En application.yml:
 *
 * <pre>
 * keygo:
 *   ui:
 *     base-url: "http://localhost:5173"
 *     paths:
 *       reset-password:
 *         route: "/reset-password"
 *         query-params:
 *           - request-id
 * </pre>
 *
 * <p>Permite construir URLs directas a la UI para links en emails, notificaciones, etc.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "keygo.ui")
public class KeyGoUiProperties {

  /** URL base de la UI (ej: http://localhost:5173, https://app.keygo.io) */
  @NotBlank private String baseUrl = "http://localhost:5173";

  /** Mapa de rutas disponibles en la UI con sus parámetros */
  private Map<String, UiPath> paths = new HashMap<>();

  /**
   * Representa una ruta en la UI con parámetros de query opcionales.
   *
   * <p>Ejemplo:
   *
   * <pre>
   * reset-password:
   *   route: "/reset-password"
   *   query-params:
   *     - request-id
   *     - token
   * </pre>
   */
  @Getter
  @Setter
  public static class UiPath {

    /** Ruta en la UI (ej: /reset-password) */
    private String route;

    /** Parámetros de query esperados (ej: [request-id, token]) */
    private List<String> queryParams = List.of();
  }
}


