package io.cmartinezs.keygo.domain.membership.model;

import java.util.UUID;

/**
 * Value object for a platform user role assignment identifier.
 * <p>Objeto de valor para identificador de asignación de rol de plataforma a usuario.
 * @author cmartinezs
 * @version 1.0
 */
public record PlatformUserRoleId(UUID value) {

  public PlatformUserRoleId {
    if (value == null) {
      throw new IllegalArgumentException("PlatformUserRoleId value cannot be null");
    }
  }

  public static PlatformUserRoleId generate() {
    return new PlatformUserRoleId(UUID.randomUUID());
  }

  public static PlatformUserRoleId of(UUID value) {
    return new PlatformUserRoleId(value);
  }

  public static PlatformUserRoleId of(String uuidString) {
    try {
      return new PlatformUserRoleId(UUID.fromString(uuidString));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid UUID format for PlatformUserRoleId: " + uuidString, e);
    }
  }

  @Override
  public String toString() {
    return "PlatformUserRoleId[" + value + "]";
  }
}
