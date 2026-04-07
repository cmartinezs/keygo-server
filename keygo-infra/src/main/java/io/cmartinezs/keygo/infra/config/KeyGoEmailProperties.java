package io.cmartinezs.keygo.infra.config;

import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propiedades de configuración para email en KeyGo.
 *
 * <p>Cada tipo de email se define declarativamente en YAML — no se necesita código Java nuevo para
 * agregar un tipo de email. Basta con agregar la entrada en {@code keygo.email.types.<nombre>} y el
 * template HTML correspondiente.
 *
 * <p>Ejemplo en application.yml:
 *
 * <pre>
 * keygo:
 *   email:
 *     smtp-host: smtp.example.com
 *     smtp-port: 587
 *     template-cache-enabled: true
 *     default-from-address: "noreply@keygo.local"
 *     default-from-name: "KeyGo"
 *     types:
 *       email-verification:
 *         subject: "Verifica tu email en KeyGo"
 *         from-name: "KeyGo - Verificación"
 *         template: "html/email-validation"
 *         defaults:
 *           expiresInMinutes: 30
 *         subjects-i18n:
 *           en: "Verify your email on KeyGo"
 * </pre>
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "keygo.email")
public class KeyGoEmailProperties {

  /** SMTP host para envío de emails */
  @NotBlank private String smtpHost;

  /** SMTP port para envío de emails (default: 587) */
  private int smtpPort = 587;

  /** Flag para habilitar/deshabilitar caché de templates Thymeleaf */
  private boolean templateCacheEnabled = true;

  /** Dirección de remitente por defecto (heredada si el tipo no define fromAddress) */
  private String defaultFromAddress = "noreply@keygo.local";

  /** Nombre de remitente por defecto (heredado si el tipo no define fromName) */
  private String defaultFromName = "KeyGo";

  /** Registro declarativo de tipos de email. Key = emailType identifier. */
  private Map<String, EmailTypeConfig> types = new LinkedHashMap<>();

  /**
   * Obtiene la configuración de un tipo de email.
   *
   * @param emailType identificador del tipo (e.g., "email-verification")
   * @return configuración del tipo, o {@code null} si no existe
   */
  public EmailTypeConfig getType(String emailType) {
    return types.get(emailType);
  }

  /**
   * Configuración declarativa de un tipo de email.
   *
   * <p>Define subject, remitente, template, defaults de variables y links a generar. Todos los
   * campos excepto {@code subject} y {@code template} son opcionales y heredan del nivel global.
   */
  @Getter
  @Setter
  public static class EmailTypeConfig {

    /** Asunto del email (idioma por defecto — español) */
    private String subject;

    /** Nombre del remitente (hereda de {@code defaultFromName} si es null) */
    private String fromName;

    /** Dirección del remitente (hereda de {@code defaultFromAddress} si es null) */
    private String fromAddress;

    /** Nombre del template Thymeleaf (e.g., "html/email-validation") */
    private String template;

    /** Valores por defecto para variables del template (el caller puede sobreescribirlos) */
    private Map<String, Object> defaults = new HashMap<>();

    /**
     * Mapa de links a generar para este tipo de email. Key = nombre de variable en el template,
     * Value = key en {@code keygo.ui.paths}.
     *
     * <p>Ejemplo: {@code resetPasswordLink: "reset-password"} → genera URL usando la ruta
     * configurada en {@code keygo.ui.paths.reset-password}.
     */
    private Map<String, String> links = new HashMap<>();

    /**
     * Subject por locale para i18n. Key = código de idioma (e.g., "en", "pt"), Value = subject
     * localizado.
     *
     * <p>Si el locale del contexto tiene entrada aquí, se usa; si no, se usa {@code subject}.
     */
    private Map<String, String> subjectsI18n = new HashMap<>();

    /**
     * Sufijo de template por locale para i18n. Key = código de idioma, Value = sufijo a agregar al
     * nombre del template.
     *
     * <p>Ejemplo: si template="html/email-validation" y templatesI18n contiene "en" →
     * "html/email-validation_en". Si no hay entrada para el locale, se usa el template base.
     */
    private Map<String, String> templatesI18n = new HashMap<>();
  }
}
