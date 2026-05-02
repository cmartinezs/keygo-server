package io.cmartinezs.keygo.domain.auth.model;

import java.util.UUID;

/**
 * Identificador único para un refresh token.
 */
public record RefreshTokenId(UUID value) {

  public RefreshTokenId {
    if (value == null) {
      throw new NullPointerException("RefreshTokenId value cannot be null");
    }
  }

  public static RefreshTokenId generate() {
    return new RefreshTokenId(UUID.randomUUID());
  }

  public static RefreshTokenId from(UUID value) {
    return new RefreshTokenId(value);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}

