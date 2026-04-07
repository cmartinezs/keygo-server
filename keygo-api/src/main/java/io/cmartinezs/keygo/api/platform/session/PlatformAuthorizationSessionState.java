package io.cmartinezs.keygo.api.platform.session;

import java.io.Serial;
import java.io.Serializable;

/**
 * Estado de autorización de plataforma almacenado en sesión HTTP.
 *
 * <p>Se guarda después de validar la solicitud en GET /platform/oauth2/authorize
 * y se recupera en POST /platform/account/login para emitir el authorization code.
 *
 * @param redirectUri URI de redirección validada contra allowlist
 * @param scope scopes solicitados
 * @param codeChallenge desafío PKCE (SHA-256 del code_verifier)
 * @param codeChallengeMethod método PKCE (solo "S256" soportado)
 * @author cmartinezs
 * @version 1.0
 */
public record PlatformAuthorizationSessionState(
    String redirectUri,
    String scope,
    String codeChallenge,
    String codeChallengeMethod)
    implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
}
