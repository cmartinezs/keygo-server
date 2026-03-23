package io.cmartinezs.keygo.domain.auth.model;

/**
 * Estados posibles de un refresh token.
 *
 * <ul>
 *   <li><b>ACTIVE</b>: Token válido para solicitar un nuevo access_token
 *   <li><b>USED</b>: Token ya utilizado en una rotación (single-use)
 *   <li><b>EXPIRED</b>: Token expirado por tiempo
 *   <li><b>REVOKED</b>: Token revocado explícitamente
 * </ul>
 */
public enum RefreshTokenStatus {
  ACTIVE("ACTIVE"),
  USED("USED"),
  EXPIRED("EXPIRED"),
  REVOKED("REVOKED");

  private final String value;

  RefreshTokenStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static RefreshTokenStatus fromValue(String value) {
    for (RefreshTokenStatus status : RefreshTokenStatus.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown RefreshTokenStatus: " + value);
  }
}

