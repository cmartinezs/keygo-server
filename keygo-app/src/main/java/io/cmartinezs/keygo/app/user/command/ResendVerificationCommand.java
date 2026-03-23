package io.cmartinezs.keygo.app.user.command;

/**
 * Command to resend a verification email to a pending user.
 * <p>Comando para reenviar un email de verificación a un usuario pendiente.
 * Only allowed if the previous verification code has already expired.
 * <p>Solo se permite si el código de verificación anterior ya venció.
 * @param tenantSlug the slug of the tenant
 * @param clientId   the OAuth2 client_id of the app
 * @param email      the user's email address
 * @author cmartinezs
 * @version 1.0
 */
public record ResendVerificationCommand(
    String tenantSlug,
    String clientId,
    String email
) {
}

