package io.cmartinezs.keygo.app.user.port.notification.exception;

import io.cmartinezs.keygo.app.user.port.notification.EmailNotificationException;

/**
 * Excepción lanzada cuando la dirección de email del destinatario es inválida.
 *
 * <p>Causas comunes:
 * - Formato de email incorrecto
 * - Email nulo o vacío
 * - Caracteres no permitidos en el email
 * - Longitud excesiva
 *
 * <p>Constructor:
 * - {@code (String email)}: el email inválido
 * - {@code (String email, String reason)}: con razón específica de validación
 *
 * <p>Heredera de {@link EmailNotificationException} → {@link io.cmartinezs.keygo.app.shared.exception.PortException}
 *
 * @author cmartinezs
 */
public class EmailValidationException extends EmailNotificationException {

  public EmailValidationException(String email) {
    super("Invalid recipient email address: %s".formatted(email));
  }

  public EmailValidationException(String email, String reason) {
    super("Invalid recipient email address '%s': %s".formatted(email, reason));
  }
}

