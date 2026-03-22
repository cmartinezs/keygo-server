package io.cmartinezs.keygo.app.auth.port;

import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import java.util.List;
import java.util.Optional;

/**
 * Puerto OUT: persistencia de claves de firma RSA.
 */
public interface SigningKeyRepositoryPort {

  /**
   * Busca la clave de firma activa.
   *
   * @return clave activa, o {@code Optional.empty()} si no existe
   */
  Optional<SigningKey> findActiveKey();

  /**
   * Busca todas las claves publicables (ACTIVE + RETIRED) para el JWKS endpoint.
   *
   * @return lista de claves cuya clave pública debe publicarse
   */
  List<SigningKey> findPublishableKeys();

  /**
   * Persiste una clave de firma (nuevo registro o actualización).
   *
   * @param key clave a guardar
   * @return clave guardada
   */
  SigningKey save(SigningKey key);
}

