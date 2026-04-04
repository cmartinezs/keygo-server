package io.cmartinezs.keygo.infra.adapter.notification.strategy;

import io.cmartinezs.keygo.infra.mail.EmailStrategy;
import io.cmartinezs.keygo.infra.mail.SendEmailCommand;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Estrategia para envío de email de verificación de contrato de suscripción.
 *
 * <p>Se utiliza cuando un usuario inicia un contrato de suscripción. El template incluye:
 * - Código de verificación de email
 * - ID del contrato
 * - Información de expiración
 *
 * <p>Variables esperadas en el comando:
 * - verificationCode: código de 6 dígitos
 * - contractId: UUID del contrato
 * - expiresInMinutes: minutos de validez del código
 * - userName: nombre del usuario
 */
@Slf4j
public class ContractVerificationStrategy extends EmailStrategy {

  public ContractVerificationStrategy(SendEmailCommand cmd) {
    super(cmd);
  }

  @Override
  public String getTemplateName() {
    return "html/contract-verification";
  }

  @Override
  public String getSubject() {
    return "Verifica tu email para completar la suscripción";
  }

  @Override
  public String getFromAddress() {
    return "noreply@keygo.local";
  }

  @Override
  public String getFromName() {
    return "KeyGo - Suscripción";
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
        "ContractVerificationStrategy rendered with variables: {}",
        variables.keySet());

    return variables;
  }
}

