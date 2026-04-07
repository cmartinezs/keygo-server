package io.cmartinezs.keygo.infra.adapter.notification.strategy;

import io.cmartinezs.keygo.infra.mail.EmailStrategy;
import io.cmartinezs.keygo.infra.mail.SendEmailCommand;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Estrategia para envío de email de notificación de membresía aprobada.
 *
 * <p>Se utiliza cuando un administrador aprueba una membresía PENDING → ACTIVE. El template
 * incluye:
 *
 * <ul>
 *   <li>Nombre del usuario
 *   <li>Nombre de la aplicación a la que ahora tiene acceso
 * </ul>
 *
 * <p>Variables esperadas en el comando:
 *
 * <ul>
 *   <li>userName: nombre del usuario
 *   <li>appName: nombre de la aplicación
 * </ul>
 */
@Slf4j
public class MembershipApprovedStrategy extends EmailStrategy {

  public MembershipApprovedStrategy(SendEmailCommand cmd) {
    super(cmd);
  }

  @Override
  public String getTemplateName() {
    return "html/membership-approved";
  }

  @Override
  public String getSubject() {
    return "Tu acceso ha sido aprobado — KeyGo";
  }

  @Override
  public String getFromAddress() {
    return "noreply@keygo.local";
  }

  @Override
  public String getFromName() {
    return "KeyGo - Acceso";
  }

  @Override
  public Map<String, Object> getTemplateVariables() {
    final var variables = new HashMap<>(cmd.getVariables());
    variables.putIfAbsent(
        "userName", cmd.getRecipientName() != null ? cmd.getRecipientName() : "Usuario");
    variables.putIfAbsent("appName", "la aplicación");

    log.debug("MembershipApprovedStrategy rendered with variables: {}", variables.keySet());

    return variables;
  }
}
