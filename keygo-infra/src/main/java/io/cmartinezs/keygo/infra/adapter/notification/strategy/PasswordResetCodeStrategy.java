package io.cmartinezs.keygo.infra.adapter.notification.strategy;

import io.cmartinezs.keygo.infra.mail.EmailStrategy;
import io.cmartinezs.keygo.infra.mail.SendEmailCommand;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Estrategia para envío de email con código de restablecimiento de contraseña forzado.
 *
 * <p>Se utiliza cuando un usuario con estado {@code RESET_PASSWORD} intenta iniciar sesión.
 * El sistema bloquea el login (HTTP 401) y envía este email con un código de 6 dígitos
 * para que el usuario pueda completar el restablecimiento.
 *
 * <p>Variables esperadas en el comando:
 * - userName: nombre o username del usuario
 * - verificationCode: código numérico de 6 dígitos
 * - expiresInMinutes: minutos de validez del código (por defecto 15)
 */
@Slf4j
public class PasswordResetCodeStrategy extends EmailStrategy {

  public PasswordResetCodeStrategy(SendEmailCommand cmd) {
    super(cmd);
  }

  @Override
  public String getTemplateName() {
    return "html/password-reset-code";
  }

  @Override
  public String getSubject() {
    return "Código para restablecer tu contraseña en KeyGo";
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
    variables.putIfAbsent("userName", cmd.getRecipientName() != null ? cmd.getRecipientName() : "Usuario");
    variables.putIfAbsent("verificationCode", "000000");
    variables.putIfAbsent("expiresInMinutes", 15);
    variables.putIfAbsent("recipientEmail", cmd.getRecipientEmail());

    log.debug(
        "PasswordResetCodeStrategy rendered with variables: {}",
        variables.keySet());

    return variables;
  }
}

