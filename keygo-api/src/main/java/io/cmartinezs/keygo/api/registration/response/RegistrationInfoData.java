package io.cmartinezs.keygo.api.registration.response;

import io.cmartinezs.keygo.domain.user.model.UserStatus;

import java.util.UUID;

/**
 * Response data for retrieving registration info by registration_id.
 * <p>Datos de info del registro找回 por registration_id.
 * @param id        the user's UUID
 * @param email     the user's email
 * @param firstName the user's first name
 * @param lastName  the user's last name
 * @param username the user's username
 * @param status   PENDING — waiting for email verification
 * @author cmartinezs
 * @version 1.0
 */
public record RegistrationInfoData(
    UUID id,
    String email,
    String tenantSlug,
    String tenantName,
    String clientAppId,
    String clientAppName,
    String clientAppDescription,
    String firstName,
    String lastName,
    String username,
    UserStatus status
) {
}