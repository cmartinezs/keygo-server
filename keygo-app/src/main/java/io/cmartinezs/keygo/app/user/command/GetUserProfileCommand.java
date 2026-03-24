package io.cmartinezs.keygo.app.user.command;

/**
 * Comando para obtener el perfil del usuario autenticado (self-service).
 *
 * <p>Utilizado por el endpoint {@code GET /api/v1/tenants/{slug}/account/profile}.
 * El Bearer token se verifica para extraer el {@code sub} (UUID del usuario).
 *
 * @param tenantSlug  slug del tenant
 * @param bearerToken access_token JWT extraído del header Authorization
 * @author cmartinezs
 * @version 1.0
 */
public record GetUserProfileCommand(
    String tenantSlug,
    String bearerToken) {}

