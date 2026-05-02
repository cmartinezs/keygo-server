package io.cmartinezs.keygo.domain.membership.model;

import java.util.UUID;

/**
 * Value object for an application role identifier.
 * <p>Objeto de valor para identificador de rol de aplicación.
 * @author cmartinezs
 * @version 1.0
 */
public record AppRoleId(UUID value) {

  public AppRoleId {
    if (value == null) {
      throw new IllegalArgumentException("AppRoleId value cannot be null");
    }
  }

  public static AppRoleId generate() {
    return new AppRoleId(UUID.randomUUID());
  }

  public static AppRoleId of(UUID value) {
    return new AppRoleId(value);
  }

  public static AppRoleId of(String uuidString) {
    try {
      return new AppRoleId(UUID.fromString(uuidString));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid UUID format: " + uuidString, e);
    }
  }

  @Override
  public String toString() {
    return "AppRoleId[" + value + "]";
  }
}

