package io.cmartinezs.keygo.app.auth.port;

import io.cmartinezs.keygo.domain.auth.model.AuthorizationCode;
import io.cmartinezs.keygo.domain.auth.model.AuthorizationCodeId;
import java.util.Optional;

/**
 * Puerto OUT (salida) para persistencia de códigos de autorización.
 *
 * <p>Define las operaciones que se pueden realizar sobre códigos de autorización en la base de
 * datos.
 */
public interface AuthorizationCodeRepositoryPort {

  /**
   * Persiste un código de autorización nuevo.
   *
   * @param authorizationCode código a guardar
   * @return el código guardado
   */
  AuthorizationCode save(AuthorizationCode authorizationCode);

  /**
   * Busca un código por su valor (string aleatorio).
   *
   * @param code valor del código
   * @return el código si existe
   */
  Optional<AuthorizationCode> findByCode(String code);

  /**
   * Busca un código por su ID.
   *
   * @param authorizationCodeId ID del código
   * @return el código si existe
   */
  Optional<AuthorizationCode> findById(AuthorizationCodeId authorizationCodeId);

  /**
   * Actualiza el estado de un código existente.
   *
   * @param authorizationCode código actualizado
   * @return el código después de la actualización
   */
  AuthorizationCode update(AuthorizationCode authorizationCode);
}

