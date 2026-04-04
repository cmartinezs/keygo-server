package io.cmartinezs.keygo.infra.adapter.notification.strategy;

import io.cmartinezs.keygo.infra.mail.EmailStrategy;
import io.cmartinezs.keygo.infra.mail.SendEmailCommand;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Estrategia para envío de email de validación de correo.
 *
 * <p>Se utiliza cuando un usuario se registra o cambia su email. El template incluye:
 * - Código de verificación
 * - Enlace directo (si disponible)
 * - Información de expiración
 *
 * <p>Variables esperadas en el comando:
 * - verificationCode: código de 6 dígitos o más
 * - expiresInMinutes: minutos de validez del código
 * - verificationLink (opcional): enlace directo
 * - userName: nombre del usuario
 */
@Slf4j
public class EmailValidationStrategy extends EmailStrategy {

  public EmailValidationStrategy(SendEmailCommand cmd) {
    super(cmd);
  }

  @Override
  public String getTemplateName() {
    return "email-validation";
  }

  @Override
  public String getSubject() {
    return "Verifica tu email en KeyGo";
  }

  @Override
  public String getFromAddress() {
    return "noreply@keygo.local";
  }

  @Override
  public String getFromName() {
    return "KeyGo - Verificación";
  }

  @Override
  public Map<String, Object> getTemplateVariables() {
    final var variables = new HashMap<>(cmd.getVariables());

    // Asegurar que existan variables mínimas
    variables.putIfAbsent("userName", cmd.getRecipientName() != null ? cmd.getRecipientName() : "Usuario");
    variables.putIfAbsent("verificationCode", "000000");
    variables.putIfAbsent("expiresInMinutes", 30);
    variables.putIfAbsent("recipientEmail", cmd.getRecipientEmail());

    log.debug(
        "EmailValidationStrategy rendered with variables: {}",
        variables.keySet());

    return variables;
  }
}

