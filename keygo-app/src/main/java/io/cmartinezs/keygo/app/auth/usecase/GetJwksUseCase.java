package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.port.JwksBuilderPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import java.util.Map;

/**
 * Caso de uso: obtener el JWK Set con las claves públicas publicables.
 *
 * <p>Retorna todas las claves con estado ACTIVE o RETIRED convertidas al formato RFC 7517.
 */
public class GetJwksUseCase {

  private final SigningKeyRepositoryPort signingKeyRepository;
  private final JwksBuilderPort jwksBuilder;

  public GetJwksUseCase(SigningKeyRepositoryPort signingKeyRepository, JwksBuilderPort jwksBuilder) {
    this.signingKeyRepository = signingKeyRepository;
    this.jwksBuilder = jwksBuilder;
  }

  /**
   * Obtiene el JWK Set para el endpoint {@code /.well-known/jwks.json}.
   *
   * @return mapa con estructura {@code {"keys": [...]}} listo para serializar a JSON
   */
  public Map<String, Object> execute() {
    var keys = signingKeyRepository.findPublishableKeys();
    return jwksBuilder.buildJwkSet(keys);
  }
}
