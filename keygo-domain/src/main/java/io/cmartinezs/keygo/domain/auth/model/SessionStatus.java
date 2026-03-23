package io.cmartinezs.keygo.domain.auth.model;

/**
 * Estados posibles de una sesión de usuario.
 *
 * <ul>
 *   <li><b>ACTIVE</b>: Sesión activa y válida
 *   <li><b>TERMINATED</b>: Sesión terminada por el usuario o el sistema
 *   <li><b>EXPIRED</b>: Sesión expirada por inactividad o tiempo máximo
 * </ul>
 */
public enum SessionStatus {
  ACTIVE("ACTIVE"),
  TERMINATED("TERMINATED"),
  EXPIRED("EXPIRED");

  private final String value;

  SessionStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static SessionStatus fromValue(String value) {
    for (SessionStatus status : SessionStatus.values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown SessionStatus: " + value);
  }
}

