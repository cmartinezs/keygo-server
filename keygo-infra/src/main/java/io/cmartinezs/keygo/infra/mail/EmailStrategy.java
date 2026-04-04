package io.cmartinezs.keygo.infra.mail;

import java.util.Map;

/**
 * Clase base abstracta para estrategias de email.
 *
 * <p>Cada tipo de email (email-validation, password-recovery, etc.) tiene su propia estrategia
 * que:
 * - Define el template a usar
 * - Define el subject
 * - Define las variables que necesita el template
 * - Extrae información del comando y la transforma para el template
 *
 * <p>Ejemplo:
 *
 * <pre>
 * public class EmailValidationStrategy extends EmailStrategy {
 *   public EmailValidationStrategy(SendEmailCommand cmd) {
 *     super(cmd);
 *   }
 *
 *   public String getTemplateName() { return "email-validation"; }
 *   public String getSubject() { return "Verifica tu email"; }
 *   // ...
 * }
 * </pre>
 */
public abstract class EmailStrategy {

  protected final SendEmailCommand cmd;

  protected EmailStrategy(SendEmailCommand cmd) {
    this.cmd = cmd;
  }

  /**
   * Nombre del template Thymeleaf (sin directorio ni extensión).
   *
   * <p>Ejemplo: "email-validation" → busca "templates/email/email-validation.html"
   */
  public abstract String getTemplateName();

  /**
   * Asunto del email.
   */
  public abstract String getSubject();

  /**
   * Email del remitente.
   */
  public abstract String getFromAddress();

  /**
   * Nombre del remitente.
   */
  public abstract String getFromName();

  /**
   * Variables que se pasan al template Thymeleaf.
   *
   * <p>Estos son los valores que Thymeleaf reemplazará en el template (e.g., ${code}, ${userName}).
   */
  public abstract Map<String, Object> getTemplateVariables();

  /**
   * Retorna el comando original.
   */
  public SendEmailCommand getCommand() {
    return cmd;
  }
}

