package io.cmartinezs.keygo.domain.auth.model;

/**
 * Estados del código de autorización en el ciclo de vida de OAuth 2.0.
 *
 * <ul>
 *   <li><b>PENDING</b>: Código recién generado, sin canjear
 *   <li><b>USED</b>: Código canjeado por token
 *   <li><b>EXPIRED</b>: Código expirado sin canje
 *   <li><b>REVOKED</b>: Código revocado por el usuario
 * </ul>
 */
public enum AuthorizationCodeStatus {
  PENDING("pending"),
  USED("used"),
  EXPIRED("expired"),
  REVOKED("revoked");

  private final String value;

  AuthorizationCodeStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static AuthorizationCodeStatus fromValue(String value) {
    for (AuthorizationCodeStatus status : AuthorizationCodeStatus.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown AuthorizationCodeStatus: " + value);
  }
}

