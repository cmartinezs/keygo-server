package io.cmartinezs.keygo.domain.membership.model;

/**
 * Enumeration of platform roles.
 * Source of truth: {@code keygo-supabase/src/main/resources/db/migration/V16__seed_foundation.sql}
 */
public enum PlatformRoleCode {
  KEYGO_ADMIN("KEYGO_ADMIN", "Keygo Admin"),
  KEYGO_ACCOUNT_ADMIN("KEYGO_ACCOUNT_ADMIN", "Keygo Account Admin"),
  KEYGO_USER("KEYGO_USER", "Keygo User");

  private final String code;
  private final String displayName;

  PlatformRoleCode(String code, String displayName) {
    this.code = code;
    this.displayName = displayName;
  }

  public String code() {
    return code;
  }

  public String displayName() {
    return displayName;
  }
}
