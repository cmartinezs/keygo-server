package io.cmartinezs.keygo.app.user.command;

/**
 * Comando para revocar una sesión propia del usuario autenticado.
 *
 * @param tenantSlug  slug del tenant
 * @param bearerToken access token JWT (sin el prefijo "Bearer ")
 * @param sessionId   UUID de la sesión a revocar (como String)
 */
public record RevokeUserSessionCommand(
    String tenantSlug,
    String bearerToken,
    String sessionId) {}
