package io.cmartinezs.keygo.app.user.exception;

import io.cmartinezs.keygo.app.shared.exception.PortException;

/**
 * Thrown when the {@code EmailNotificationPort} fails to deliver an email.
 */
public class EmailNotificationException extends PortException {

  public EmailNotificationException(String recipient, String reason) {
    super("Failed to send email to %s: %s".formatted(recipient, reason));
  }

  public EmailNotificationException(String recipient, String reason, Throwable cause) {
    super("Failed to send email to %s: %s".formatted(recipient, reason), cause);
  }
}
