package io.cmartinezs.keygo.app.auth.command;

/**
 * Comando: revocar un token (RFC 7009).
 *
 * @param tenantSlug    slug del tenant
 * @param clientId      client_id de la app
 * @param token         valor del token a revocar
 * @param tokenTypeHint hint del tipo de token ("refresh_token" o "access_token"); puede ser null
 */
public record RevokeTokenCommand(
    String tenantSlug,
    String clientId,
    String token,
    String tokenTypeHint) {}

