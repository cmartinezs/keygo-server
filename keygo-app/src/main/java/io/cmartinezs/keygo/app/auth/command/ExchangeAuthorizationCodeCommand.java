package io.cmartinezs.keygo.app.auth.command;

/**
 * Comando: Canjear un código de autorización por un token de acceso.
 *
 * @param tenantSlug identificador del tenant
 * @param clientId client_id de la app cliente
 * @param code código de autorización
 * @param redirectUri URI de redirección (debe coincidir)
 * @param codeVerifier verifier PKCE (será validado contra el challenge)
 */
public record ExchangeAuthorizationCodeCommand(
    String tenantSlug, String clientId, String code, String redirectUri, String codeVerifier) {}

