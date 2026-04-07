package io.cmartinezs.keygo.infra.adapter.notification;

import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.notification.EmailNotificationException;
import io.cmartinezs.keygo.app.user.port.notification.exception.EmailSmtpException;
import io.cmartinezs.keygo.app.user.port.notification.exception.EmailTemplateException;
import io.cmartinezs.keygo.app.user.port.notification.exception.EmailValidationException;
import io.cmartinezs.keygo.infra.config.KeyGoEmailProperties;
import io.cmartinezs.keygo.infra.config.KeyGoEmailProperties.EmailTypeConfig;
import io.cmartinezs.keygo.infra.config.KeyGoUiProperties;
import io.cmartinezs.keygo.infra.mail.ConfigurableEmailStrategy;
import io.cmartinezs.keygo.infra.mail.EmailStrategy;
import io.cmartinezs.keygo.infra.mail.SendEmailCommand;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Adaptador para envío de emails usando Thymeleaf + JavaMail.
 *
 * <p>Los tipos de email se resuelven desde la configuración YAML ({@code keygo.email.types}) — no
 * se necesitan clases Strategy individuales. Para agregar un nuevo tipo basta con agregar la entrada
 * en YAML y el template HTML correspondiente.
 *
 * <p>Flujo:
 *
 * <ol>
 *   <li>Recibe tipo + variables vía {@link #sendEmail}
 *   <li>Resuelve configuración del tipo desde {@link KeyGoEmailProperties}
 *   <li>Crea {@link ConfigurableEmailStrategy} con la configuración
 *   <li>Genera links si el tipo los define (usando {@link KeyGoUiProperties})
 *   <li>Renderiza template Thymeleaf con variables + defaults + links
 *   <li>Envía MimeMessage vía SMTP
 * </ol>
 */
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationAdapter implements EmailNotificationPort {

  private final TemplateEngine emailTemplateEngine;
  private final JavaMailSender mailSender;
  private final KeyGoUiProperties uiProperties;
  private final KeyGoEmailProperties emailProperties;

  private static final String TEMPLATE_PREFIX = "templates/email/";
  private static final String TEMPLATE_SUFFIX = ".html";

  /**
   * Validación fail-fast al arranque: verifica que cada tipo de email configurado en YAML tenga
   * subject, template y que el archivo .html exista en el classpath. Evita descubrir templates
   * faltantes en runtime (cuando un usuario espera recibir un email).
   */
  @PostConstruct
  void validateTemplatesExist() {
    var types = emailProperties.getTypes();
    if (types == null || types.isEmpty()) {
      log.warn(
          "No email types configured in keygo.email.types — email sending will fail at runtime");
      return;
    }

    for (var entry : types.entrySet()) {
      var emailType = entry.getKey();
      var config = entry.getValue();

      if (config.getTemplate() == null || config.getTemplate().isBlank()) {
        throw new IllegalStateException(
            "Email type '" + emailType + "' has no template configured in keygo.email.types");
      }
      if (config.getSubject() == null || config.getSubject().isBlank()) {
        throw new IllegalStateException(
            "Email type '" + emailType + "' has no subject configured in keygo.email.types");
      }

      // Validar que el archivo .html del template base existe en classpath
      assertTemplateFileExists(emailType, config.getTemplate());

      // Validar templates i18n si están configurados
      for (var i18nEntry : config.getTemplatesI18n().entrySet()) {
        assertTemplateFileExists(
            emailType + " [i18n:" + i18nEntry.getKey() + "]", i18nEntry.getValue());
      }

      log.info("Email type registered: {} → template={}", emailType, config.getTemplate());
    }
  }

  private void assertTemplateFileExists(String emailType, String templateName) {
    var resourcePath = TEMPLATE_PREFIX + templateName + TEMPLATE_SUFFIX;
    var resource = getClass().getClassLoader().getResource(resourcePath);
    if (resource == null) {
      throw new IllegalStateException(
          "Email template file not found for type '"
              + emailType
              + "': expected classpath resource '"
              + resourcePath
              + "'");
    }
  }

  @Override
  public void sendEmail(
      String emailType,
      String recipientEmail,
      String recipientName,
      Map<String, Object> templateVariables) {
    try {
      var cmd =
          SendEmailCommand.builder()
              .emailType(emailType)
              .recipientEmail(recipientEmail)
              .recipientName(recipientName)
              .variables(templateVariables)
              .build();

      sendEmailInternal(cmd);
    } catch (EmailNotificationException e) {
      throw e;
    } catch (Exception e) {
      throw new EmailNotificationException(
          "Failed to send email to " + recipientEmail + ": " + e.getMessage());
    }
  }

  private Map<String, String> generateLinks(String emailType) {
    var typeConfig = emailProperties.getType(emailType);
    if (typeConfig == null || typeConfig.getLinks().isEmpty()) {
      return Map.of();
    }

    return typeConfig.getLinks().entrySet().stream()
        .filter(e -> uiProperties.getPaths().containsKey(e.getValue()))
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                e -> generateLink(uiProperties.getPaths().get(e.getValue()))));
  }

  private void sendEmailInternal(SendEmailCommand cmd) throws EmailNotificationException {
    try {
      log.debug(
          "Processing email send request: type={}, to={}",
          cmd.getEmailType(),
          cmd.getRecipientEmail());

      var strategy = resolveStrategy(cmd);

      // Inyectar links generados como variables de template
      var links = generateLinks(cmd.getEmailType());
      if (!links.isEmpty()) {
        var enrichedVars = new HashMap<>(cmd.getVariables());
        links.forEach(enrichedVars::putIfAbsent);
        cmd = cmd.toBuilder().variables(enrichedVars).build();
        strategy = resolveStrategy(cmd);
      }

      var htmlContent = renderTemplate(strategy);
      sendMimeMessage(strategy, htmlContent);

      log.info(
          "Email sent successfully: type={}, to={}", cmd.getEmailType(), cmd.getRecipientEmail());
    } catch (EmailNotificationException e) {
      log.error(
          "Email notification failed: type={}, to={}, cause={}",
          cmd.getEmailType(),
          cmd.getRecipientEmail(),
          e.getClass().getSimpleName(),
          e);
      throw e;
    } catch (Exception e) {
      log.error(
          "Unexpected error while sending email: type={}, to={}",
          cmd.getEmailType(),
          cmd.getRecipientEmail(),
          e);
      throw new EmailNotificationException("Failed to send email: " + e.getMessage(), e);
    }
  }

  /**
   * Resuelve la estrategia leyendo la configuración YAML del tipo de email.
   *
   * @param cmd comando con emailType
   * @return EmailStrategy configurada desde YAML
   * @throws EmailNotificationException si el tipo no está registrado en YAML
   */
  private EmailStrategy resolveStrategy(SendEmailCommand cmd) throws EmailNotificationException {
    // Lookup por key en el mapa YAML; fallback a emailType con normalización (e.g., "email-validation" → "email-verification")
    var typeConfig = emailProperties.getType(cmd.getEmailType());

    // Backward compat: "email-validation" era el nombre legacy
    if (typeConfig == null && "email-validation".equals(cmd.getEmailType())) {
      typeConfig = emailProperties.getType("email-verification");
    }

    if (typeConfig == null) {
      throw new EmailNotificationException(
          "Unknown email type: '"
              + cmd.getEmailType()
              + "'. Register it in keygo.email.types in application.yml");
    }

    return new ConfigurableEmailStrategy(
        cmd, typeConfig, emailProperties, LocaleContextHolder.getLocale());
  }

  private String renderTemplate(EmailStrategy strategy) throws EmailTemplateException {
    try {
      var context = new Context(LocaleContextHolder.getLocale());
      context.setVariables(strategy.getTemplateVariables());

      var html = emailTemplateEngine.process(strategy.getTemplateName(), context);

      log.debug("Template rendered successfully: {}", strategy.getTemplateName());
      return html;
    } catch (EmailTemplateException e) {
      throw e;
    } catch (Exception e) {
      throw new EmailTemplateException(strategy.getTemplateName(), e);
    }
  }

  private void sendMimeMessage(EmailStrategy strategy, String htmlContent)
      throws EmailSmtpException, EmailValidationException {
    try {
      var recipientEmail = strategy.getCommand().getRecipientEmail();
      if (recipientEmail == null || recipientEmail.isBlank()) {
        throw new EmailValidationException("null or blank");
      }

      var mimeMessage = mailSender.createMimeMessage();
      var helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

      helper.setFrom(strategy.getFromAddress(), strategy.getFromName());
      helper.setTo(recipientEmail);
      helper.setSubject(strategy.getSubject());
      helper.setText(htmlContent, true);

      mailSender.send(mimeMessage);
    } catch (EmailValidationException e) {
      throw e;
    } catch (MessagingException | UnsupportedEncodingException e) {
      throw new EmailSmtpException("Failed to send MIME message: " + e.getMessage(), e);
    } catch (Exception e) {
      throw new EmailSmtpException("Unexpected error during SMTP delivery: " + e.getMessage(), e);
    }
  }

  private String generateLink(KeyGoUiProperties.UiPath uiPath) {
    var uriBuilder =
        UriComponentsBuilder.fromUri(URI.create(uiProperties.getBaseUrl())).path(uiPath.getRoute());
    var queryParams = uiPath.getQueryParams();
    if (queryParams != null && !queryParams.isEmpty()) {
      queryParams.forEach(uriBuilder::queryParam);
    }
    return uriBuilder.build().toUriString();
  }
}
