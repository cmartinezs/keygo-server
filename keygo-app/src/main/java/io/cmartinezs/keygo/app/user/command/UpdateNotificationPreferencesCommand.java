package io.cmartinezs.keygo.app.user.command;

/**
 * Comando para actualizar las preferencias de notificación del usuario autenticado.
 *
 * @param tenantSlug          slug del tenant
 * @param bearerToken         access token JWT (sin el prefijo "Bearer ")
 * @param securityAlertsEmail alertas de seguridad por email
 * @param securityAlertsInApp alertas de seguridad in-app
 * @param billingAlertsEmail  alertas de billing por email
 * @param productUpdatesEmail actualizaciones de producto por email
 * @param weeklyDigest        resumen semanal por email
 */
public record UpdateNotificationPreferencesCommand(
    String tenantSlug,
    String bearerToken,
    boolean securityAlertsEmail,
    boolean securityAlertsInApp,
    boolean billingAlertsEmail,
    boolean productUpdatesEmail,
    boolean weeklyDigest) {}
