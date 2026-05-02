package io.cmartinezs.keygo.domain.membership.model;

import java.util.UUID;

/**
 * Value object for a tenant role identifier.
 * <p>Objeto de valor para identificador de rol de tenant.
 * @author cmartinezs
 * @version 1.0
 */
public record TenantRoleId(UUID value) {

  public TenantRoleId {
    if (value == null) {
      throw new IllegalArgumentException("TenantRoleId value cannot be null");
    }
  }

  public static TenantRoleId generate() {
    return new TenantRoleId(UUID.randomUUID());
  }

  public static TenantRoleId of(UUID value) {
    return new TenantRoleId(value);
  }

  public static TenantRoleId of(String uuidString) {
    try {
      return new TenantRoleId(UUID.fromString(uuidString));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid UUID format for TenantRoleId: " + uuidString, e);
    }
  }

  @Override
  public String toString() {
    return "TenantRoleId[" + value + "]";
  }
}
