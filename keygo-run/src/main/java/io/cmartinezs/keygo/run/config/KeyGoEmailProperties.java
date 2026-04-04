package io.cmartinezs.keygo.run.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración para email en KeyGo.
 *
 * <p>En application.yml:
 *
 * <pre>
 * keygo:
 *   email:
 *     smtp-host: smtp.example.com
 *     smtp-port: 587
 *     template-cache-enabled: true  # ← controla caché Thymeleaf
 * </pre>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "keygo.email")
public class KeyGoEmailProperties {

  /** SMTP host para envío de emails */
  @NotBlank private String smtpHost;

  /** SMTP port para envío de emails (default: 587) */
  private int smtpPort = 587;

  /** Flag para habilitar/deshabilitar caché de templates Thymeleaf */
  private boolean templateCacheEnabled = true;
}

