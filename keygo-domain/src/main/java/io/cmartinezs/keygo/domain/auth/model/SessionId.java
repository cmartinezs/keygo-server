package io.cmartinezs.keygo.domain.auth.model;

import java.util.UUID;

/**
 * Identificador único para una sesión de usuario.
 */
public record SessionId(UUID value) {

  public SessionId {
    if (value == null) {
      throw new NullPointerException("SessionId value cannot be null");
    }
  }

  public static SessionId generate() {
    return new SessionId(UUID.randomUUID());
  }

  public static SessionId from(UUID value) {
    return new SessionId(value);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}

