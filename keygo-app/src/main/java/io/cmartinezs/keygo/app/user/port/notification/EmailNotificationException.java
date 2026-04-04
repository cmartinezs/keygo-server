package io.cmartinezs.keygo.app.user.port.notification;

import io.cmartinezs.keygo.app.shared.exception.PortException;

/**
 * Thrown when the {@code EmailNotificationPort} fails to send an email.
 *
 * <p>Lanzada cuando el puerto {@code EmailNotificationPort} falla al enviar un correo.
 * Heredera de {@code PortException} — categorizada como fallo de infraestructura (PORT layer).
 *
 * @author cmartinezs
 */
public class EmailNotificationException extends PortException {
  public EmailNotificationException(String message) {
    super(message);
  }

  public EmailNotificationException(String message, Throwable cause) {
    super(message, cause);
  }
}

