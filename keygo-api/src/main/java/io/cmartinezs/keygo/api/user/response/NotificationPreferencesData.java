package io.cmartinezs.keygo.api.user.response;

import io.cmartinezs.keygo.app.user.result.NotificationPreferencesResult;

/**
 * DTO de presentación: preferencias de notificación del usuario.
 *
 * @param securityAlertsEmail alertas de seguridad por email
 * @param securityAlertsInApp alertas de seguridad in-app
 * @param billingAlertsEmail  alertas de billing por email
 * @param productUpdatesEmail actualizaciones de producto por email
 * @param weeklyDigest        resumen semanal por email
 */
public record NotificationPreferencesData(
    boolean securityAlertsEmail,
    boolean securityAlertsInApp,
    boolean billingAlertsEmail,
    boolean productUpdatesEmail,
    boolean weeklyDigest) {

  public static NotificationPreferencesData from(NotificationPreferencesResult result) {
    return new NotificationPreferencesData(
        result.securityAlertsEmail(),
        result.securityAlertsInApp(),
        result.billingAlertsEmail(),
        result.productUpdatesEmail(),
        result.weeklyDigest());
  }
}
