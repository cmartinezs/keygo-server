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

  /**
   * Send a temporary password to a provisioned user (e.g. created via billing contract).
   * The user must reset the password on first login.
   * <p>Envía una contraseña temporal a un usuario aprovisionado (ej. creado vía contrato de billing).
   * El usuario debe resetear la contraseña en el primer inicio de sesión.
   *
   * @param toEmail       the recipient's email address
   * @param username      the recipient's username (for personalization)
   * @param rawPassword   the temporary raw password (plain text — sent once, never stored)
   */
  void sendTemporaryPasswordEmail(String toEmail, String username, String rawPassword);
}

