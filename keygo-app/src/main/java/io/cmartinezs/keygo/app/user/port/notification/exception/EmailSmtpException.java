package io.cmartinezs.keygo.app.user.port.notification.exception;

import io.cmartinezs.keygo.app.user.port.notification.EmailNotificationException;

/**
 * Excepción lanzada cuando falla el envío de email vía SMTP.
 *
 * <p>Causas comunes:
 * - Servidor SMTP no disponible
 * - Credenciales SMTP inválidas
 * - Puertos bloqueados
 * - Errores en la construcción del MimeMessage
 * - Fallos de red
 * - Límites de rate-limiting alcanzados
 *
 * <p>Constructor:
 * - {@code (String reason)}: descripción del error SMTP
 * - {@code (String reason, Throwable cause)}: con causa raíz (típicamente MessagingException)
 *
 * <p>Heredera de {@link EmailNotificationException} → {@link io.cmartinezs.keygo.app.shared.exception.PortException}
 *
 * @author cmartinezs
 */
public class EmailSmtpException extends EmailNotificationException {

  public EmailSmtpException(String reason) {
    super("SMTP email delivery failed: %s".formatted(reason));
  }

  public EmailSmtpException(String reason, Throwable cause) {
    super("SMTP email delivery failed: %s".formatted(reason), cause);
  }
}


