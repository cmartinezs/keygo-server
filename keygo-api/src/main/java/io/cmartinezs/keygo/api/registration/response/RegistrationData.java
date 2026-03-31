package io.cmartinezs.keygo.api.registration.response;

import io.cmartinezs.keygo.domain.user.model.UserStatus;

import java.util.UUID;

/**
 * Response data for a newly registered user.
 * <p>Datos de respuesta para un usuario recién registrado.
 * @param id       the user's UUID
 * @param username the chosen username
 * @param email    the registered email address
 * @param status   PENDING — the user must verify their email before logging in
 * @author cmartinezs
 * @version 1.0
 */
public record RegistrationData(
    UUID id,
    String username,
    String email,
    UserStatus status
) {

}
