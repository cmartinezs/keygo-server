package io.cmartinezs.keygo.app.auth.command;

/**
 * Comando: Iniciar el flujo de autorización.
 *
 * <p>Encapsula los parámetros que el cliente OAuth 2.0 envía en la URL de autorización.
 *
 * @param tenantSlug identificador del tenant
 * @param clientId client_id del cliente OAuth
 * @param redirectUri URI de redirección registrada
 * @param scopes espacios separados por espacios (p. ej. "openid profile email")
 * @param state valor de seguridad generado por el cliente (previene CSRF)
 * @param codeChallenge desafío PKCE (SHA256 base64url del verifier)
 * @param codeChallengeMethod "S256" o "plain"
 */
public record InitiateAuthorizationCommand(
    String tenantSlug,
    String clientId,
    String redirectUri,
    String scopes,
    String state,
    String codeChallenge,
    String codeChallengeMethod) {}

