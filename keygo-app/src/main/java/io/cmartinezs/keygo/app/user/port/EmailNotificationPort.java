package io.cmartinezs.keygo.app.user.port;

import java.util.Map;

/**
 * Port OUT — contrato para el envío de notificaciones por email.
 *
 * <p>Los tipos de email se definen en YAML ({@code keygo.email.types}). Este port solo expone
 * {@link #sendEmail} — la resolución de template, subject y remitente ocurre en el adapter basado
 * en la configuración.
 *
 * <p>Las constantes de tipo definen los identificadores que deben coincidir con las keys en YAML.
 *
 * @author cmartinezs
 * @version 3.0 — YAML-driven (configurable, extensible)
 */
public interface EmailNotificationPort {

  // ── Constantes de tipo de email (deben coincidir con keys en keygo.email.types) ──
  String TYPE_EMAIL_VERIFICATION = "email-verification";
  String TYPE_PASSWORD_RECOVERY = "password-recovery";
  String TYPE_PASSWORD_RESET_CODE = "password-reset-code";
  String TYPE_TEMPORARY_PASSWORD = "temporary-password";
  String TYPE_CONTRACT_VERIFICATION = "contract-verification";
  String TYPE_MEMBERSHIP_APPROVED = "membership-approved";

  /**
   * Envía un email del tipo especificado.
   *
   * <p>El tipo debe coincidir con una key registrada en {@code keygo.email.types} en
   * application.yml. El adapter resuelve subject, from, template y defaults desde la configuración
   * YAML.
   *
   * @param emailType identificador del tipo de email (usar constantes {@code TYPE_*})
   * @param recipientEmail dirección de correo del destinatario
   * @param recipientName nombre del destinatario (para personalización)
   * @param templateVariables variables a renderizar en el template (sobreescriben defaults YAML)
   */
  void sendEmail(
      String emailType,
      String recipientEmail,
      String recipientName,
      Map<String, Object> templateVariables);
}
