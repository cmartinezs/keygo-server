package io.cmartinezs.keygo.api.registration.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.cmartinezs.keygo.domain.user.model.UserStatus;

import java.util.UUID;

/**
 * Response data for a newly registered user.
 * <p>Datos de respuesta para un usuario recién registrado.
 * @param id                the user's UUID
 * @param username          the chosen username
 * @param notificationEmail email ofuscado al que se envió la verificación
 * @param status            PENDING — the user must verify their email before logging in
 * @author cmartinezs
 * @version 1.1
 */
public record RegistrationData(
    UUID id,
    String username,
    @JsonProperty("notification_email") String notificationEmail,
    UserStatus status
) {

}
