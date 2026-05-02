package io.cmartinezs.keygo.app.user.command;

/**
 * Command to resend a verification email to a pending user.
 * <p>Comando para reenviar un email de verificación a un usuario pendiente.
 * If the previous verification code is still valid, the same code is resent.
 * A new code is only generated when the previous one has already expired or does not exist.
 * <p>Si el código de verificación anterior aún es válido, se reenvía el mismo código.
 * Solo se genera uno nuevo cuando el anterior ya expiró o no existe.
 * @param tenantSlug the slug of the tenant
 * @param clientId   the OAuth2 client_id of the app
 * @param email      the user's email address
 * @author cmartinezs
 * @version 1.1
 */
public record ResendVerificationCommand(
    String tenantSlug,
    String clientId,
    String email
) {
}

