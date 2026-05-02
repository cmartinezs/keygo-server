package io.cmartinezs.keygo.app.auth.result;

/**
 * Resultado de emitir un access_token para el flujo OAuth2 client_credentials.
 *
 * <p>No incluye id_token ni refresh_token — el flujo M2M no representa un usuario final.
 *
 * @param accessToken JWT firmado (access_token)
 * @param tokenType   tipo de token (siempre "Bearer")
 * @param expiresIn   segundos hasta la expiración del access_token
 * @param scope       scopes otorgados, separados por espacio
 */
public record IssueClientCredentialsTokenResult(
    String accessToken,
    String tokenType,
    long expiresIn,
    String scope) {}

