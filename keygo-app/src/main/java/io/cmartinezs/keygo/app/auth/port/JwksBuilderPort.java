package io.cmartinezs.keygo.app.auth.port;

import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import java.util.List;
import java.util.Map;

/**
 * Puerto OUT: convierte claves de dominio al formato JWK Set (RFC 7517).
 *
 * <p>La implementación concreta usa Nimbus JOSE+JWT y vive en {@code keygo-infra}.
 */
public interface JwksBuilderPort {

  /**
   * Construye el JWK Set como mapa serializable a JSON.
   *
   * @param publishableKeys claves publicables (ACTIVE + RETIRED)
   * @return mapa con estructura {@code {"keys": [...]}}
   */
  Map<String, Object> buildJwkSet(List<SigningKey> publishableKeys);
}

