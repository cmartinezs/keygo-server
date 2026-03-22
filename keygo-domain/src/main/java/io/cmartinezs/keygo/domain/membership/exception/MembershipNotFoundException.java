package io.cmartinezs.keygo.domain.membership.exception;

/**
 * Exception thrown when a membership is not found.
 * <p>Excepción lanzada cuando no se encuentra una membresía.
 * @author cmartinezs
 * @version 1.0
 */
public class MembershipNotFoundException extends RuntimeException {

  public MembershipNotFoundException(String message) {
    super(message);
  }

  public MembershipNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}

