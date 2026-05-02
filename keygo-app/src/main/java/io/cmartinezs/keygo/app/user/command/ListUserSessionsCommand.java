package io.cmartinezs.keygo.app.user.command;

/**
 * Comando para listar las sesiones activas del usuario autenticado.
 *
 * @param tenantSlug       slug del tenant
 * @param bearerToken      access token JWT (sin el prefijo "Bearer ")
 * @param requestUserAgent User-Agent de la petición HTTP actual (para marcar sesión actual)
 * @param requestIpAddress IP de la petición HTTP actual (para marcar sesión actual)
 */
public record ListUserSessionsCommand(
    String tenantSlug,
    String bearerToken,
    String requestUserAgent,
    String requestIpAddress) {}
