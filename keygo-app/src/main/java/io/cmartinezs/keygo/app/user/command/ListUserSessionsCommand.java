package io.cmartinezs.keygo.app.user.command;

/**
 * Comando para listar las sesiones activas del usuario autenticado.
 *
 * @param tenantSlug  slug del tenant
 * @param bearerToken access token JWT (sin el prefijo "Bearer ")
 */
public record ListUserSessionsCommand(
    String tenantSlug,
    String bearerToken) {}
