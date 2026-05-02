package io.cmartinezs.keygo.app.user.command;

/**
 * Comando para obtener el perfil del usuario de plataforma autenticado (self-service).
 *
 * <p>Utilizado por el endpoint {@code GET /api/v1/platform/account/profile}.
 * El {@code sub} (UUID del platform user) es extraído del SecurityContext por el controller
 * antes de construir este comando — el filtro ya verificó el token.
 *
 * @param userId UUID del usuario autenticado (claim {@code sub} del JWT)
 * @author cmartinezs
 * @version 1.0
 */
public record GetPlatformUserProfileCommand(String userId) {}
