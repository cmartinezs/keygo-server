package io.cmartinezs.keygo.app.auth.port;

import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import java.util.List;
import java.util.Map;

/**
 * Puerto OUT: verificación de access tokens JWT.
 *
 * <p>Implementado en {@code keygo-infra} con Nimbus JOSE+JWT.
 */
public interface AccessTokenVerifierPort {

  /**
   * Verifica la firma y expiración de un JWT, y retorna sus claims.
   *
   * @param accessToken  JWT a verificar (formato compact serialization)
   * @param publicKeys   claves públicas disponibles para verificación
   * @return mapa de claims extraídos del payload
   * @throws io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException si la firma es inválida o el token está expirado
   */
  Map<String, Object> verify(String accessToken, List<SigningKey> publicKeys);
}

