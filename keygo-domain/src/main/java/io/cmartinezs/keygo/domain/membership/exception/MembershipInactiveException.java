package io.cmartinezs.keygo.domain.membership.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when an operation requires an active membership but it is inactive.
 */
public class MembershipInactiveException extends DomainException {

  public MembershipInactiveException(String reason) {
    super("Membership is inactive: %s".formatted(reason));
  }
}
