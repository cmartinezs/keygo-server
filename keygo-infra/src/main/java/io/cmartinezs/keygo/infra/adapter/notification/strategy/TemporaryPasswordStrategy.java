package io.cmartinezs.keygo.infra.adapter.notification.strategy;

import io.cmartinezs.keygo.infra.mail.EmailStrategy;
import io.cmartinezs.keygo.infra.mail.SendEmailCommand;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Estrategia para envío de email con contraseña temporal.
 *
 * <p>Se utiliza cuando un usuario es creado por un administrador y necesita una contraseña
 * temporal para primer acceso. El template incluye:
 * - Contraseña temporal
 * - Instrucciones para cambiarla al primer login
 * - Advertencia de seguridad
 *
 * <p>Variables esperadas en el comando:
 * - temporaryPassword: contraseña temporal generada
 * - userName: nombre del usuario
 */
@Slf4j
public class TemporaryPasswordStrategy extends EmailStrategy {

  public TemporaryPasswordStrategy(SendEmailCommand cmd) {
    super(cmd);
  }

  @Override
  public String getTemplateName() {
    return "html/temporary-password";
  }

  @Override
  public String getSubject() {
    return "Tu contraseña temporal en KeyGo";
  }

  @Override
  public String getFromAddress() {
    return "noreply@keygo.local";
  }

  @Override
  public String getFromName() {
    return "KeyGo - Cuenta";
  }

  @Override
  public Map<String, Object> getTemplateVariables() {
    final var variables = new HashMap<>(cmd.getVariables());

    // Asegurar que existan variables mínimas
    variables.putIfAbsent("userName", cmd.getRecipientName() != null ? cmd.getRecipientName() : "Usuario");
    variables.putIfAbsent("temporaryPassword", "TempPass123!");
    variables.putIfAbsent("recipientEmail", cmd.getRecipientEmail());

    log.debug(
        "TemporaryPasswordStrategy rendered with variables: {}",
        variables.keySet());

    return variables;
  }
}

