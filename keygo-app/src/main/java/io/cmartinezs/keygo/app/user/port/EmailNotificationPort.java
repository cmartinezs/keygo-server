package io.cmartinezs.keygo.app.user.port;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Port OUT — contract for sending email notifications.
 *
 * <p>Porto de salida — contrato para el envío de notificaciones por email. Uses a flexible
 * command-based approach with Thymeleaf templates. Utiliza un enfoque flexible basado en comandos
 * con templates Thymeleaf.
 *
 * @author cmartinezs
 * @version 2.0 — Command-based (flexible, scalable)
 */
public interface EmailNotificationPort {

  /**
   * Send an email using a command-based approach. Envía un email usando un enfoque basado en
   * comandos.
   *
   * @param emailType email type identifier (matches template name) tipo de email (debe coincidir
   *     con nombre de template)
   * @param recipientEmail recipient's email address dirección de correo del destinatario
   * @param recipientName recipient's name (for personalization) nombre del destinatario (para
   *     personalización)
   * @param templateVariables variables to render in the template variables a renderizar en el
   *     template
   */
  void sendEmail(
      String emailType,
      String recipientEmail,
      String recipientName,
      Map<String, Object> templateVariables);

  Map<String, String> generateLinks(String emailType);

  // ────────────────────────────────────────────────────────────────────────────
  // Convenience methods (backward compatibility) — Métodos de conveniencia
  // ────────────────────────────────────────────────────────────────────────────

  /**
   * Send a verification code email to a newly registered user. Envía un email con código de
   * verificación a un usuario recién registrado.
   *
   * @param toEmail the recipient's email address
   * @param username the recipient's username (for personalization)
   * @param verificationCode the 6-digit verification code
   */
  default void sendVerificationEmail(String toEmail, String username, String verificationCode) {
    sendEmail(
        "email-verification",
        toEmail,
        username,
        Map.of(
            "userName", username,
            "verificationCode", verificationCode,
            "expiresInMinutes", 30));
  }

  /**
   * Send a verification code email for a billing contract onboarding. Includes the contractId so
   * the recipient can resume the onboarding flow via {@code GET
   * /billing/contracts/{contractId}/resume}.
   *
   * <p>Envía un email con código de verificación para el onboarding de un contrato de billing.
   * Incluye el contractId para que el destinatario pueda retomar el flujo via {@code GET
   * /billing/contracts/{contractId}/resume}.
   *
   * @param toEmail the contractor's email address
   * @param recipientName the contractor's full name (for personalization)
   * @param verificationCode the 6-digit verification code
   * @param contractId the UUID of the contract (needed to resume onboarding)
   */
  default void sendContractVerificationEmail(
      String toEmail, String recipientName, String verificationCode, UUID contractId) {
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

  /**
   * Send a temporary password email to a newly created user. Envía un email con contraseña temporal
   * a un usuario recién creado.
   *
   * @param toEmail the recipient's email address
   * @param username the recipient's username (for personalization)
   * @param rawPassword the plain-text temporary password
   */
  default void sendTemporaryPasswordEmail(String toEmail, String username, String rawPassword) {
    sendEmail(
        "temporary-password",
        toEmail,
        username,
        Map.of(
            "userName", username,
            "temporaryPassword", rawPassword));
  }

  /**
   * Send a password recovery email with a one-time token (self-service forgot-password flow). Envía
   * un email de recuperación de contraseña con token de un solo uso.
   *
   * @param toEmail the recipient's email address
   * @param username the recipient's username (for personalization)
   * @param recoveryToken the 32-char hex recovery token
   * @param tenantSlug the tenant slug (for building the reset URL)
   */
  default void sendPasswordRecoveryEmail(
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

  /**
   * Sends a 6-digit verification code to a user whose login is blocked by {@code
   * status=RESET_PASSWORD}. Envía un código de 6 dígitos a un usuario cuyo login está bloqueado por
   * {@code status=RESET_PASSWORD}.
   *
   * @param toEmail the recipient's email address
   * @param username the recipient's username (for personalization)
   * @param code the 6-digit verification code
   * @param expiresInMinutes TTL in minutes
   */
  default void sendPasswordResetCodeEmail(
      String toEmail, String username, String code, int expiresInMinutes) {
    var links = generateLinks("password-reset-code");
    sendEmail(
        "password-reset-code",
        toEmail,
        username,
        Map.of(
            "userName", username,
            "verificationCode", code,
            "expiresInMinutes", expiresInMinutes,
            "resetPasswordLink", links.getOrDefault("resetPasswordLink", "")));
  }

  /**
   * Send a notification email when a membership has been approved. Envía un email de notificación
   * cuando una membresía ha sido aprobada.
   *
   * @param toEmail the recipient's email address
   * @param username the recipient's username (for personalization)
   * @param appName the name of the application the user now has access to
   */
  default void sendMembershipApprovedEmail(String toEmail, String username, String appName) {
    sendEmail(
        "membership-approved",
        toEmail,
        username,
        Map.of(
            "userName", username,
            "appName", appName));
  }
}
