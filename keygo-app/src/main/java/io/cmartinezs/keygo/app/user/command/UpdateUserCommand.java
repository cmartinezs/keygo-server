package io.cmartinezs.keygo.app.user.command;

/**
 * Command to update user profile information.
 * <p>Comando para actualizar información del perfil de un usuario.
 * @param tenantSlug the slug of the tenant
 * @param userId     the UUID string of the user to update
 * @param firstName  new first name (may be null to clear)
 * @param lastName   new last name (may be null to clear)
 * @author cmartinezs
 * @version 1.0
 */
public record UpdateUserCommand(
    String tenantSlug,
    String userId,
    String firstName,
    String lastName
) {
}

