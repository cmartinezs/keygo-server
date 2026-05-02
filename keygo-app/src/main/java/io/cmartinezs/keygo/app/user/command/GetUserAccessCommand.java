package io.cmartinezs.keygo.app.user.command;

/**
 * Comando para obtener las apps y roles del usuario autenticado (self-service).
 *
 * @param tenantSlug  slug del tenant
 * @param bearerToken access token JWT (sin el prefijo "Bearer ")
 */
public record GetUserAccessCommand(String tenantSlug, String bearerToken) {}
