package io.cmartinezs.keygo.domain.membership.exception;

/**
 * Exception thrown when a membership is inactive and operation requires active status.
 * <p>Excepción lanzada cuando una membresía está inactiva y la operación requiere estatus activo.
 * @author cmartinezs
 * @version 1.0
 */
public class MembershipInactiveException extends RuntimeException {

  public MembershipInactiveException(String message) {
    super(message);
  }

  public MembershipInactiveException(String message, Throwable cause) {
    super(message, cause);
  }
}

