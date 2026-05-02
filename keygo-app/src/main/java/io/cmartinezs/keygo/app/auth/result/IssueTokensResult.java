package io.cmartinezs.keygo.app.auth.result;

/**
 * Resultado de emitir tokens tras el canje de un authorization code.
 *
 * @param accessToken         JWT firmado (access_token)
 * @param idToken             JWT firmado (id_token OIDC)
 * @param tokenType           tipo de token (siempre "Bearer")
 * @param expiresIn           segundos hasta la expiración del access_token
 * @param scope               scopes otorgados, separados por espacio
 * @param authorizationCodeId ID del código canjeado (para auditoría)
 * @param signingKeyId        UUID de la clave RSA usada para firmar (para auditoría en sesión/RT)
 */
public record IssueTokensResult(
    String accessToken,
    String idToken,
    String tokenType,
    long expiresIn,
    String scope,
    String authorizationCodeId,
    String signingKeyId) {}
