package io.cmartinezs.keygo.app.auth.command;

/**
 * Comando: obtener información del usuario autenticado (OIDC §5.3 userinfo).
 *
 * @param tenantSlug  slug del tenant
 * @param bearerToken access_token JWT extraído del header Authorization
 */
public record GetUserInfoCommand(
    String tenantSlug,
    String bearerToken) {}

