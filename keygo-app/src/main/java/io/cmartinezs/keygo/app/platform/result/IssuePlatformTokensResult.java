package io.cmartinezs.keygo.app.platform.result;

/**
 * Resultado de emitir tokens de plataforma tras autenticación directa.
 *
 * @param accessToken  JWT firmado (access_token)
 * @param idToken      JWT firmado (id_token OIDC)
 * @param refreshToken token plano para rotación
 * @param tokenType    tipo de token (siempre "Bearer")
 * @param expiresIn    segundos hasta la expiración del access_token
 * @param signingKeyId UUID de la clave RSA usada para firmar (para auditoría)
 */
public record IssuePlatformTokensResult(
    String accessToken,
    String idToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    String signingKeyId) {}
