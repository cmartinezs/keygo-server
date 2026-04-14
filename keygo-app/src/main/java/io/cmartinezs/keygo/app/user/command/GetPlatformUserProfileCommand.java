package io.cmartinezs.keygo.app.user.command;

/**
 * Comando para obtener el perfil del usuario de plataforma autenticado (self-service).
 *
 * <p>Utilizado por el endpoint {@code GET /api/v1/platform/account/profile}.
 * El Bearer token se verifica para extraer el {@code sub} (UUID del platform user).
 *
 * @param bearerToken access_token JWT extraído del header Authorization
 * @author cmartinezs
 * @version 1.0
 */
public record GetPlatformUserProfileCommand(String bearerToken) {}
