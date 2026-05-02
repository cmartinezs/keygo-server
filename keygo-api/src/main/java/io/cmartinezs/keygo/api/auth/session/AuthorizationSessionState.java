package io.cmartinezs.keygo.api.auth.session;

import java.io.Serializable;

/**
 * Estado de autorización almacenado en sesión HTTP.
 *
 * <p>Se guarda después de validar la solicitud de autorización en GET /authorize y se recupera en
 * POST /account/login para emitir el authorization code.
 *
 * @param tenantSlug identificador del tenant
 * @param clientId OAuth2 client_id
 * @param redirectUri URI de redirección validada
 * @param scope scopes solicitados
 * @param codeChallenge desafío PKCE
 * @param codeChallengeMethod método PKCE ("S256" o "plain")
 * @author cmartinezs
 * @version 1.0
 */
public record AuthorizationSessionState(
    String tenantSlug,
    String clientId,
    String redirectUri,
    String scope,
    String codeChallenge,
    String codeChallengeMethod)
    implements Serializable {
  private static final long serialVersionUID = 1L;
}

