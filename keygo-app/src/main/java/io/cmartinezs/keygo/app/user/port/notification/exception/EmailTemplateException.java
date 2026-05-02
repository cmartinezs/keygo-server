package io.cmartinezs.keygo.app.user.port.notification.exception;

import io.cmartinezs.keygo.app.user.port.notification.EmailNotificationException;

/**
 * Excepción lanzada cuando falla la renderización de un template de email Thymeleaf.
 *
 * <p>Causas comunes:
 * - Template no encontrado
 * - Variables faltantes
 * - Error en expresiones Thymeleaf
 * - Configuración incorrecta del motor de templates
 *
 * <p>Constructor:
 * - {@code (String templateName)}: cuando solo se conoce el nombre del template
 * - {@code (String templateName, Throwable cause)}: con causa raíz de Thymeleaf
 *
 * <p>Heredera de {@link EmailNotificationException} → {@link io.cmartinezs.keygo.app.shared.exception.PortException}
 *
 * @author cmartinezs
 */
public class EmailTemplateException extends EmailNotificationException {

  public EmailTemplateException(String templateName) {
    super("Failed to render email template: %s".formatted(templateName));
  }

  public EmailTemplateException(String templateName, Throwable cause) {
    super("Failed to render email template: %s".formatted(templateName), cause);
  }
}

