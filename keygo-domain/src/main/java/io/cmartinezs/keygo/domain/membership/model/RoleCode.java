package io.cmartinezs.keygo.domain.membership.model;

/**
 * Well-known application role codes within a tenant.
 * <p>Códigos de roles bien conocidos dentro de una aplicación en un tenant.
 * @author cmartinezs
 * @version 1.0
 */
public record RoleCode(String value) {

  /* Standard role codes for applications */
  public static final String ADMIN = "admin";
  public static final String ADMIN_TENANT = "admin_tenant";
  public static final String EDITOR = "editor";
  public static final String VIEWER = "viewer";
  public static final String OPERATOR = "operator";

  public RoleCode {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("RoleCode value cannot be null or blank");
    }
    if (!value.matches("^[a-z][a-z0-9_-]*$")) {
      throw new IllegalArgumentException(
          "RoleCode must start with lowercase letter and contain only lowercase, digits, underscores or hyphens");
    }
  }

  /* Factory methods */
  public static RoleCode of(String value) {
    return new RoleCode(value);
  }

  public static RoleCode adminRole() {
    return new RoleCode(ADMIN);
  }

  public static RoleCode adminTenantRole() {
    return new RoleCode(ADMIN_TENANT);
  }

  public static RoleCode editorRole() {
    return new RoleCode(EDITOR);
  }

  public static RoleCode viewerRole() {
    return new RoleCode(VIEWER);
  }

  public static RoleCode operatorRole() {
    return new RoleCode(OPERATOR);
  }

  @Override
  public String toString() {
    return "RoleCode[" + value + "]";
  }
}

