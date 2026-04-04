package io.cmartinezs.keygo.app.user.port.notification;

import io.cmartinezs.keygo.app.shared.exception.PortException;

/**
 * Clase base para excepciones del puerto {@code EmailNotificationPort}.
 *
 * <p>Lanzada cuando el puerto {@code EmailNotificationPort} falla al enviar un correo.
 * Heredera de {@code PortException} — categorizada como fallo de infraestructura (PORT layer).
 *
 * <p>Sub-excepciones específicas (jerarquía):
 * - {@link io.cmartinezs.keygo.app.user.port.notification.exception.EmailTemplateException}
 *   → Errores de renderización de templates Thymeleaf
 * - {@link io.cmartinezs.keygo.app.user.port.notification.exception.EmailSmtpException}
 *   → Errores de envío SMTP / infraestructura de email
 * - {@link io.cmartinezs.keygo.app.user.port.notification.exception.EmailValidationException}
 *   → Errores de validación de dirección de email
 *
 * <p>Patrón de uso:
 * <ul>
 *   <li>Catch base {@code EmailNotificationException} para tratamiento genérico de cualquier error de email</li>
 *   <li>Catch específico para cada sub-excepción si se necesita manejo diferenciado</li>
 * </ul>
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

