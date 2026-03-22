package io.cmartinezs.keygo.app.user.command;

/**
 * Command to reset a user's password.
 * <p>Comando para resetear la contraseña de un usuario.
 * @param tenantSlug     the slug of the tenant
 * @param userId         the UUID string of the user
 * @param newRawPassword the new raw password (will be hashed before storage)
 * @author cmartinezs
 * @version 1.0
 */
public record ResetUserPasswordCommand(
    String tenantSlug,
    String userId,
    String newRawPassword
) {
}

