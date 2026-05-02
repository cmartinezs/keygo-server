package io.cmartinezs.keygo.domain.clientapp.model;

/**
 * Enum representing the lifecycle status of a client application.
 * <p>Enum que representa el estado del ciclo de vida de una aplicación cliente.
 * @author cmartinezs
 * @version 1.0
 */
public enum ClientAppStatus {
  /** Application is active and operational. */
  ACTIVE,
  /** Application has been suspended and cannot be used. */
  SUSPENDED,
  /** Application is pending activation. */
  PENDING
}

