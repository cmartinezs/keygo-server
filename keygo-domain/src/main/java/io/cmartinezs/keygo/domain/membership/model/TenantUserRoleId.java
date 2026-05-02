package io.cmartinezs.keygo.domain.membership.model;

import java.util.UUID;

/**
 * Value object for a tenant user role assignment identifier.
 * <p>Objeto de valor para identificador de asignación de rol de tenant a usuario.
 * @author cmartinezs
 * @version 1.0
 */
public record TenantUserRoleId(UUID value) {

  public TenantUserRoleId {
    if (value == null) {
      throw new IllegalArgumentException("TenantUserRoleId value cannot be null");
    }
  }

  public static TenantUserRoleId generate() {
    return new TenantUserRoleId(UUID.randomUUID());
  }

  public static TenantUserRoleId of(UUID value) {
    return new TenantUserRoleId(value);
  }

  public static TenantUserRoleId of(String uuidString) {
    try {
      return new TenantUserRoleId(UUID.fromString(uuidString));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid UUID format for TenantUserRoleId: " + uuidString, e);
    }
  }

  @Override
  public String toString() {
    return "TenantUserRoleId[" + value + "]";
  }
}
