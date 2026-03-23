package io.cmartinezs.keygo.app.user.port;

/**
 * Port OUT — contract for sending email notifications.
 * <p>Puerto de salida — contrato para el envío de notificaciones por email.
 * @author cmartinezs
 * @version 1.0
 */
public interface EmailNotificationPort {

  /**
   * Send a verification code email to a newly registered user.
   * <p>Envía un email con código de verificación a un usuario recién registrado.
   * @param toEmail          the recipient's email address
   * @param username         the recipient's username (for personalization)
   * @param verificationCode the 6-digit verification code
   */
  void sendVerificationEmail(String toEmail, String username, String verificationCode);
}

