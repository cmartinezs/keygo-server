package io.cmartinezs.keygo.app.auth.result;

/**
 * Resultado de rotar un refresh token: nuevos tokens emitidos y nuevo refresh token.
 *
 * @param accessToken       nuevo JWT access_token
 * @param idToken           nuevo JWT id_token
 * @param rawRefreshToken   nuevo refresh token plano (entregar al cliente, NO persistir)
 * @param tokenType         tipo de token (siempre "Bearer")
 * @param expiresIn         segundos hasta la expiración del access_token
 * @param scope             scopes otorgados
 */
public record RotateRefreshTokenResult(
    String accessToken,
    String idToken,
    String rawRefreshToken,
    String tokenType,
    long expiresIn,
    String scope) {}

