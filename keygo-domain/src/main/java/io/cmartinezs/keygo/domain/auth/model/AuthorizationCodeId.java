package io.cmartinezs.keygo.domain.auth.model;

import java.util.UUID;

/**
 * Identificador único para un código de autorización.
 *
 * <p>Es un value object (record) que encapsula un UUID generado internamente.
 */
public record AuthorizationCodeId(UUID id) {

  public AuthorizationCodeId {
    if (id == null) {
      throw new NullPointerException("AuthorizationCodeId id cannot be null");
    }
  }

  /**
   * Crea un nuevo AuthorizationCodeId con un UUID aleatorio.
   *
   * @return AuthorizationCodeId con UUID único
   */
  public static AuthorizationCodeId generate() {
    return new AuthorizationCodeId(UUID.randomUUID());
  }

  /**
   * Reconstruye un AuthorizationCodeId a partir de un UUID existente.
   *
   * @param id UUID existente
   * @return AuthorizationCodeId
   * @throws NullPointerException si id es null
   */
  public static AuthorizationCodeId from(UUID id) {
    return new AuthorizationCodeId(id);
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public String toString() {
    return id.toString();
  }
}

