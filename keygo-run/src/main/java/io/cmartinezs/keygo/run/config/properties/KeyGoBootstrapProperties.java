package io.cmartinezs.keygo.run.config.properties;

import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for KeyGo bootstrap settings.
 * Propiedades de configuración para ajustes de arranque de KeyGo.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Component
@ConfigurationProperties(prefix = "keygo.bootstrap")
@Validated
@Getter
@Setter
public class KeyGoBootstrapProperties {

  /**
   * Whether bootstrap is enabled. Default is true.
   * Si el arranque está habilitado. Por defecto es true.
   */
  private boolean enabled = true;

  /**
   * Admin key for bootstrap operations.
   * Clave de administrador para operaciones de arranque.
   */
  private String adminKey;

  /**
   * API path prefix that requires authentication.
   * Prefijo de ruta de API que requiere autenticación.
   * Must be configured in application.yml (e.g., "/api/")
   * Debe ser configurado en application.yml (ej., "/api/")
   */
  private String apiPathPrefix;

  /**
   * Actuator path prefix that is public.
   * Prefijo de ruta de actuator que es público.
   * Must be configured in application.yml (e.g., "/actuator/")
   * Debe ser configurado en application.yml (ej., "/actuator/")
   */
  private String actuatorPathPrefix;

  /**
   * Service info path prefix that is public.
   * Prefijo de ruta de información de servicio que es público.
   * Must be configured in application.yml (e.g., "/service/info")
   * Debe ser configurado en application.yml (ej., "/service/info")
   */
  private String serviceInfoPathPrefix;

  /**
   * Validates that adminKey is not blank when enabled is true.
   * Valida que adminKey no esté vacía cuando enabled es true.
   *
   * @return true if validation passes
   */
  @AssertTrue(message = "adminKey must not be blank when bootstrap is enabled")
  private boolean isAdminKeyValid() {
    if (!enabled) {
      return true;
    }
    return adminKey != null && !adminKey.isBlank();
  }
}


