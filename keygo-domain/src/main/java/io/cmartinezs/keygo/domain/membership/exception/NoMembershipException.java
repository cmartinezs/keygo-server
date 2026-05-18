package io.cmartinezs.keygo.domain.membership.exception;

/**
 * Thrown when a user has no membership for a CLOSED app and tries to obtain a token.
 */
public class NoMembershipException extends RuntimeException {

  public NoMembershipException(String message) {
    super(message);
  }
}
