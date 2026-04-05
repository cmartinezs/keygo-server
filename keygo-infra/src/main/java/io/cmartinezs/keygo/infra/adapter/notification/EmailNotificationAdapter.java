package io.cmartinezs.keygo.infra.adapter.notification;

import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.notification.EmailNotificationException;
import io.cmartinezs.keygo.app.user.port.notification.exception.EmailSmtpException;
import io.cmartinezs.keygo.app.user.port.notification.exception.EmailTemplateException;
import io.cmartinezs.keygo.app.user.port.notification.exception.EmailValidationException;
import io.cmartinezs.keygo.infra.adapter.notification.strategy.ContractVerificationStrategy;
import io.cmartinezs.keygo.infra.adapter.notification.strategy.EmailValidationStrategy;
import io.cmartinezs.keygo.infra.adapter.notification.strategy.PasswordRecoveryStrategy;
import io.cmartinezs.keygo.infra.adapter.notification.strategy.PasswordResetCodeStrategy;
import io.cmartinezs.keygo.infra.adapter.notification.strategy.TemporaryPasswordStrategy;
import io.cmartinezs.keygo.infra.mail.EmailStrategy;
import io.cmartinezs.keygo.infra.mail.SendEmailCommand;
import jakarta.mail.MessagingException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Adaptador para envío de emails usando Thymeleaf + JavaMail.
 *
 * <p>Flujo: 1. Recibe SendEmailCommand 2. Resuelve Strategy basado en emailType 3. Renderiza
 * template Thymeleaf con variables 4. Crea MimeMessage y lo envía por SMTP
 *
 * <p>Responsabilidades: - Resolver strategy correcta - Renderizar templates - Enviar emails vía
 * SMTP - Logging y error handling
 */
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationAdapter implements EmailNotificationPort {

  private final TemplateEngine emailTemplateEngine;
  private final JavaMailSender mailSender;

  @Override
  public void sendEmail(
      String emailType,
      String recipientEmail,
      String recipientName,
      Map<String, Object> templateVariables) {
    try {
      final var cmd =
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

  /**
   * Método interno que envía el email usando SendEmailCommand.
   *
   * @param cmd Comando con todos los detalles del email
   * @throws EmailNotificationException o sub-excepciones si falla el envío
   */
  private void sendEmailInternal(SendEmailCommand cmd) throws EmailNotificationException {
    try {
      log.debug(
          "Processing email send request: type={}, to={}",
          cmd.getEmailType(),
          cmd.getRecipientEmail());

      // 1. Resolver estrategia basada en tipo de email
      final var strategy = resolveStrategy(cmd);

      // 2. Renderizar template con variables
      final var htmlContent = renderTemplate(strategy);

      // 3. Crear y enviar MimeMessage
      sendMimeMessage(strategy, htmlContent);

      log.info(
          "Email sent successfully: type={}, to={}", cmd.getEmailType(), cmd.getRecipientEmail());
    } catch (EmailNotificationException e) {
      // Re-throw específicas para que se propaguen con su tipo exacto
      log.error(
          "Email notification failed: type={}, to={}, cause={}",
          cmd.getEmailType(),
          cmd.getRecipientEmail(),
          e.getClass().getSimpleName(),
          e);
      throw e;
    } catch (Exception e) {
      // Catch-all para excepciones inesperadas
      log.error(
          "Unexpected error while sending email: type={}, to={}",
          cmd.getEmailType(),
          cmd.getRecipientEmail(),
          e);
      throw new EmailNotificationException("Failed to send email: " + e.getMessage(), e);
    }
  }

  /**
   * Resuelve la estrategia correcta basada en emailType.
   *
   * @param cmd comando con emailType
   * @return EmailStrategy correspondiente
   * @throws EmailNotificationException si emailType no es soportado
   */
  private EmailStrategy resolveStrategy(SendEmailCommand cmd) throws EmailNotificationException {
    return switch (cmd.getEmailType()) {
      case "email-validation" -> new EmailValidationStrategy(cmd);
      case "password-recovery" -> new PasswordRecoveryStrategy(cmd);
      case "contract-verification" -> new ContractVerificationStrategy(cmd);
      case "temporary-password" -> new TemporaryPasswordStrategy(cmd);
      case "password-reset-code" -> new PasswordResetCodeStrategy(cmd);
      default -> throw new EmailNotificationException("Unknown email type: " + cmd.getEmailType());
    };
  }

  /**
   * Renderiza el template Thymeleaf usando las variables de la estrategia.
   *
   * @param strategy estrategia que define template y variables
   * @return HTML renderizado
   * @throws EmailTemplateException si falla la renderización
   */
  private String renderTemplate(EmailStrategy strategy) throws EmailTemplateException {
    try {
      final var context = new Context();

      // Agregar todas las variables de la estrategia
      context.setVariables(strategy.getTemplateVariables());

      // Renderizar template
      final var html = emailTemplateEngine.process(strategy.getTemplateName(), context);

      log.debug("Template rendered successfully: {}", strategy.getTemplateName());

      return html;
    } catch (EmailTemplateException e) {
      throw e;
    } catch (Exception e) {
      throw new EmailTemplateException(strategy.getTemplateName(), e);
    }
  }

  /**
   * Crea y envía MimeMessage con HTML renderizado.
   *
   * @param strategy estrategia (contiene From, Subject)
   * @param htmlContent HTML renderizado
   * @throws EmailSmtpException si falla el envío SMTP
   * @throws EmailValidationException si el email del destinatario es inválido
   */
  private void sendMimeMessage(EmailStrategy strategy, String htmlContent)
      throws EmailSmtpException, EmailValidationException {
    try {
      // Validar email del destinatario antes de intentar enviar
      final var recipientEmail = strategy.getCommand().getRecipientEmail();
      if (recipientEmail == null || recipientEmail.isBlank()) {
        throw new EmailValidationException("null or blank");
      }

      final var mimeMessage = mailSender.createMimeMessage();
      final var helper = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart

      // Configurar header
      helper.setFrom(strategy.getFromAddress(), strategy.getFromName());
      helper.setTo(recipientEmail);
      helper.setSubject(strategy.getSubject());

      // Configurar content (HTML)
      helper.setText(htmlContent, true); // true = HTML mode

      // Enviar
      mailSender.send(mimeMessage);
    } catch (EmailValidationException e) {
      throw e;
    } catch (MessagingException | java.io.UnsupportedEncodingException e) {
      throw new EmailSmtpException("Failed to send MIME message: " + e.getMessage(), e);
    } catch (Exception e) {
      throw new EmailSmtpException("Unexpected error during SMTP delivery: " + e.getMessage(), e);
    }
  }

  // ── Métodos de conveniencia (implementación de EmailNotificationPort) ──────

  @Override
  public void sendVerificationEmail(String toEmail, String username, String verificationCode) {
    sendEmail(
        "email-validation",
        toEmail,
        username,
        Map.of(
            "userName", username,
            "verificationCode", verificationCode,
            "expiresInMinutes", 30));
  }

  @Override
  public void sendContractVerificationEmail(
      String toEmail, String recipientName, String verificationCode, java.util.UUID contractId) {
    sendEmail(
        "contract-verification",
        toEmail,
        recipientName,
        Map.of(
            "userName",
            recipientName,
            "verificationCode",
            verificationCode,
            "contractId",
            contractId.toString(),
            "expiresInMinutes",
            30));
  }

  @Override
  public void sendTemporaryPasswordEmail(String toEmail, String username, String rawPassword) {
    sendEmail(
        "temporary-password",
        toEmail,
        username,
        Map.of(
            "userName", username,
            "temporaryPassword", rawPassword));
  }

  @Override
  public void sendPasswordRecoveryEmail(
      String toEmail, String username, String recoveryToken, String tenantSlug) {
    sendEmail(
        "password-recovery",
        toEmail,
        username,
        Map.of(
            "userName", username,
            "recoveryToken", recoveryToken,
            "tenantSlug", tenantSlug));
  }
}
