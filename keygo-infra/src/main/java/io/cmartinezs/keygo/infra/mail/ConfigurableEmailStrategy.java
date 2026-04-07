package io.cmartinezs.keygo.infra.mail;

import io.cmartinezs.keygo.infra.config.KeyGoEmailProperties;
import io.cmartinezs.keygo.infra.config.KeyGoEmailProperties.EmailTypeConfig;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Estrategia de email configurable por YAML.
 *
 * <p>Lee subject, from, template y defaults desde {@link EmailTypeConfig} en lugar de tener valores
 * hardcodeados en código. Esto elimina la necesidad de crear una clase Strategy por cada tipo de
 * email.
 *
 * <p>Para agregar un nuevo tipo de email basta con:
 *
 * <ol>
 *   <li>Agregar entrada en {@code keygo.email.types.<nombre>} en application.yml
 *   <li>Crear el template HTML en {@code templates/email/}
 * </ol>
 *
 * <p>Soporta i18n: si {@code subjectsI18n} contiene el locale activo, usa ese subject; si no,
 * fallback al subject por defecto. Mismo patrón con {@code templatesI18n} para templates
 * localizados.
 */
@Slf4j
public class ConfigurableEmailStrategy extends EmailStrategy {

  private final EmailTypeConfig typeConfig;
  private final KeyGoEmailProperties emailProperties;
  private final Locale locale;

  public ConfigurableEmailStrategy(
      SendEmailCommand cmd,
      EmailTypeConfig typeConfig,
      KeyGoEmailProperties emailProperties,
      Locale locale) {
    super(cmd);
    this.typeConfig = typeConfig;
    this.emailProperties = emailProperties;
    this.locale = locale;
  }

  @Override
  public String getTemplateName() {
    if (locale != null && !typeConfig.getTemplatesI18n().isEmpty()) {
      var langKey = locale.getLanguage();
      var localizedTemplate = typeConfig.getTemplatesI18n().get(langKey);
      if (localizedTemplate != null) {
        return localizedTemplate;
      }
    }
    return typeConfig.getTemplate();
  }

  @Override
  public String getSubject() {
    if (locale != null && !typeConfig.getSubjectsI18n().isEmpty()) {
      var langKey = locale.getLanguage();
      var localizedSubject = typeConfig.getSubjectsI18n().get(langKey);
      if (localizedSubject != null) {
        return localizedSubject;
      }
    }
    return typeConfig.getSubject();
  }

  @Override
  public String getFromAddress() {
    if (typeConfig.getFromAddress() != null && !typeConfig.getFromAddress().isBlank()) {
      return typeConfig.getFromAddress();
    }
    return emailProperties.getDefaultFromAddress();
  }

  @Override
  public String getFromName() {
    if (typeConfig.getFromName() != null && !typeConfig.getFromName().isBlank()) {
      return typeConfig.getFromName();
    }
    return emailProperties.getDefaultFromName();
  }

  @Override
  public Map<String, Object> getTemplateVariables() {
    var variables = new HashMap<String, Object>();

    // 1. Cargar defaults del tipo (prioridad baja)
    variables.putAll(typeConfig.getDefaults());

    // 2. Sobreescribir con variables del comando (prioridad alta)
    variables.putAll(cmd.getVariables());

    // 3. Asegurar variables estándar
    variables.putIfAbsent(
        "userName", cmd.getRecipientName() != null ? cmd.getRecipientName() : "Usuario");
    variables.putIfAbsent("recipientEmail", cmd.getRecipientEmail());

    log.debug(
        "ConfigurableEmailStrategy [{}] rendered with variables: {}",
        cmd.getEmailType(),
        variables.keySet());

    return variables;
  }
}
