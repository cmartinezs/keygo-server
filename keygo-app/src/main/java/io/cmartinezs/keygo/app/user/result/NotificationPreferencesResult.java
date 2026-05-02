package io.cmartinezs.keygo.app.user.result;

import io.cmartinezs.keygo.domain.user.model.NotificationPreferences;

/**
 * Resultado que contiene las preferencias de notificación del usuario.
 *
 * @param securityAlertsEmail alertas de seguridad por email
 * @param securityAlertsInApp alertas de seguridad in-app
 * @param billingAlertsEmail  alertas de billing por email
 * @param productUpdatesEmail actualizaciones de producto por email
 * @param weeklyDigest        resumen semanal por email
 */
public record NotificationPreferencesResult(
    boolean securityAlertsEmail,
    boolean securityAlertsInApp,
    boolean billingAlertsEmail,
    boolean productUpdatesEmail,
    boolean weeklyDigest) {

  public static NotificationPreferencesResult from(NotificationPreferences prefs) {
    return new NotificationPreferencesResult(
        prefs.isSecurityAlertsEmail(),
        prefs.isSecurityAlertsInApp(),
        prefs.isBillingAlertsEmail(),
        prefs.isProductUpdatesEmail(),
        prefs.isWeeklyDigest());
  }
}
