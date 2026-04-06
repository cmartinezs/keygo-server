package io.cmartinezs.keygo.domain.membership.model;

import java.util.UUID;

/**
 * Value object for a platform role identifier.
 * <p>Objeto de valor para identificador de rol de plataforma.
 * @author cmartinezs
 * @version 1.0
 */
public record PlatformRoleId(UUID value) {

  public PlatformRoleId {
    if (value == null) {
      throw new IllegalArgumentException("PlatformRoleId value cannot be null");
    }
  }

  public static PlatformRoleId generate() {
    return new PlatformRoleId(UUID.randomUUID());
  }

  public static PlatformRoleId of(UUID value) {
    return new PlatformRoleId(value);
  }

  public static PlatformRoleId of(String uuidString) {
    try {
      return new PlatformRoleId(UUID.fromString(uuidString));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid UUID format for PlatformRoleId: " + uuidString, e);
    }
  }

  @Override
  public String toString() {
    return "PlatformRoleId[" + value + "]";
  }
}
