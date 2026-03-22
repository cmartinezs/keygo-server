package io.cmartinezs.keygo.domain.membership.exception;

/**
 * Exception thrown when an invalid role assignment is attempted.
 * <p>Excepción lanzada cuando se intenta una asignación de rol inválida.
 * @author cmartinezs
 * @version 1.0
 */
public class InvalidRoleAssignmentException extends RuntimeException {

  public InvalidRoleAssignmentException(String message) {
    super(message);
  }

  public InvalidRoleAssignmentException(String message, Throwable cause) {
    super(message, cause);
  }
}

