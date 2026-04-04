package io.cmartinezs.keygo.infra.adapter.notification.strategy;

import io.cmartinezs.keygo.infra.mail.EmailStrategy;
import io.cmartinezs.keygo.infra.mail.SendEmailCommand;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Estrategia para envío de email de recuperación de contraseña.
 *
 * <p>Se utiliza cuando un usuario solicita restablecer su contraseña. El template incluye:
 * - Código de verificación (y/o enlace)
 * - Información de seguridad (aviso sobre no compartir)
 * - Tiempo de expiración
 *
 * <p>Variables esperadas en el comando:
 * - verificationCode: código único
 * - userName: nombre del usuario
 * - recoveryLink (opcional): enlace directo
 */
@Slf4j
public class PasswordRecoveryStrategy extends EmailStrategy {

  public PasswordRecoveryStrategy(SendEmailCommand cmd) {
    super(cmd);
  }

  @Override
  public String getTemplateName() {
    return "password-recovery";
  }

  @Override
  public String getSubject() {
    return "Restablecer tu contraseña en KeyGo";
  }

  @Override
  public String getFromAddress() {
    return "noreply@keygo.local";
  }

  @Override
  public String getFromName() {
    return "KeyGo - Seguridad";
  }

  @Override
  public Map<String, Object> getTemplateVariables() {
    final var variables = new HashMap<>(cmd.getVariables());

    // Asegurar que existan variables mínimas
    variables.putIfAbsent("userName", cmd.getRecipientName() != null ? cmd.getRecipientName() : "Usuario");
    variables.putIfAbsent("verificationCode", "000000");

    log.debug(
        "PasswordRecoveryStrategy rendered with variables: {}",
        variables.keySet());

    return variables;
  }
}

