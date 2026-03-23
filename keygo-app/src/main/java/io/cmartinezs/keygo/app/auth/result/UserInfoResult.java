package io.cmartinezs.keygo.app.auth.result;

/**
 * Resultado del endpoint userinfo (OIDC §5.3).
 *
 * @param sub               identificador único del usuario (UUID)
 * @param email             email del usuario
 * @param name              nombre completo
 * @param preferredUsername username preferido
 */
public record UserInfoResult(
    String sub,
    String email,
    String name,
    String preferredUsername) {}

