package io.cmartinezs.keygo.app.user.command;

/**
 * Command to verify a user's email address using the code sent to them.
 * <p>Comando para verificar el email de un usuario usando el código enviado.
 * @param tenantSlug the slug of the tenant
 * @param clientId   the OAuth2 client_id of the app
 * @param email      the user's email address
 * @param code       the 6-digit verification code
 * @author cmartinezs
 * @version 1.0
 */
public record VerifyEmailCommand(
    String tenantSlug,
    String clientId,
    String email,
    String code
) {
}

