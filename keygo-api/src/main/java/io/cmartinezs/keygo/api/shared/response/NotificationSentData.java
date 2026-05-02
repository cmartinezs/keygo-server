package io.cmartinezs.keygo.api.shared.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO genérico para respuestas donde el dato principal es la confirmación de envío
 * de una notificación por email.
 *
 * @param notificationEmail email ofuscado al que se envió la notificación
 * @author cmartinezs
 * @version 1.0
 */
public record NotificationSentData(
    @JsonProperty("notification_email") String notificationEmail
) {}
