package io.cmartinezs.keygo.app.auth.command;

/**
 * Comando: Emitir un código de autorización.
 *
 * @param tenantSlug identificador del tenant
 * @param clientId client_id de la app cliente
 * @param userId ID del usuario que autoriza
 * @param redirectUri URI de redirección (validada)
 * @param scopes permisos solicitados
 * @param codeChallenge desafío PKCE
 * @param codeChallengeMethod "S256" o "plain"
 */
public record IssueAuthorizationCodeCommand(
    String tenantSlug,
    String clientId,
    String userId,
    String redirectUri,
    String scopes,
    String codeChallenge,
    String codeChallengeMethod) {}

