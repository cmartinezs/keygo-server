package io.cmartinezs.keygo.app.user.port;

import java.util.UUID;

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
   * Send a verification code email for a billing contract onboarding.
   * Includes the contractId so the recipient can resume the onboarding flow via
   * {@code GET /billing/contracts/{contractId}/resume}.
   * <p>Envía un email con código de verificación para el onboarding de un contrato de billing.
   * Incluye el contractId para que el destinatario pueda retomar el flujo via
   * {@code GET /billing/contracts/{contractId}/resume}.
   *
   * @param toEmail          the contractor's email address
   * @param recipientName    the contractor's full name (for personalization)
   * @param verificationCode the 6-digit verification code
   * @param contractId       the UUID of the contract (needed to resume onboarding)
   */
  void sendContractVerificationEmail(String toEmail, String recipientName, String verificationCode, UUID contractId);

  /**
   * Send a temporary password email to a newly created user.
   * <p>Envía un email con contraseña temporal a un usuario recién creado.
   * @param toEmail      the recipient's email address
   * @param username     the recipient's username (for personalization)
   * @param rawPassword  the plain-text temporary password
   */
  void sendTemporaryPasswordEmail(String toEmail, String username, String rawPassword);

  /**
   * Send a password recovery email with a one-time token (self-service forgot-password flow).
   * <p>Envía un email de recuperación de contraseña con token de un solo uso.
   * @param toEmail      the recipient's email address
   * @param username     the recipient's username (for personalization)
   * @param recoveryToken the 32-char hex recovery token
   * @param tenantSlug   the tenant slug (for building the reset URL)
   */
  void sendPasswordRecoveryEmail(String toEmail, String username, String recoveryToken, String tenantSlug);
}

