package io.cmartinezs.keygo.api.user.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Request body para PATCH /account/notification-preferences.
 *
 * <p>Todos los campos son requeridos (boolean). La anotación
 * {@code @JsonIgnoreProperties(ignoreUnknown = false)} rechaza campos desconocidos
 * con una excepción que el {@code GlobalExceptionHandler} mapea a 400 INVALID_INPUT.
 *
 * @param securityAlertsEmail alertas de seguridad por email
 * @param securityAlertsInApp alertas de seguridad in-app
 * @param billingAlertsEmail  alertas de billing por email
 * @param productUpdatesEmail actualizaciones de producto por email
 * @param weeklyDigest        resumen semanal por email
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public record UpdateNotificationPreferencesRequest(
    boolean securityAlertsEmail,
    boolean securityAlertsInApp,
    boolean billingAlertsEmail,
    boolean productUpdatesEmail,
    boolean weeklyDigest) {}
