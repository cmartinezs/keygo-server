package io.cmartinezs.keygo.app.auth.command;

/**
 * Comando: rotar un refresh token existente para obtener nuevos tokens.
 *
 * @param tenantSlug      slug del tenant
 * @param clientId        client_id de la app (para validar pertenencia del token)
 * @param rawRefreshToken token plano recibido del cliente
 * @param scope           scopes solicitados (puede ser null para mantener los del token original)
 */
public record RotateRefreshTokenCommand(
    String tenantSlug,
    String clientId,
    String rawRefreshToken,
    String scope) {}

