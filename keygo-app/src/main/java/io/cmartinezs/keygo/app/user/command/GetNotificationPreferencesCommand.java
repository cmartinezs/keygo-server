package io.cmartinezs.keygo.app.user.command;

/**
 * Comando para obtener las preferencias de notificación del usuario autenticado.
 *
 * @param tenantSlug  slug del tenant
 * @param bearerToken access token JWT (sin el prefijo "Bearer ")
 */
public record GetNotificationPreferencesCommand(String tenantSlug, String bearerToken) {}
