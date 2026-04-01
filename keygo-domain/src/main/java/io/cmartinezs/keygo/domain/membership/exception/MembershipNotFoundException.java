package io.cmartinezs.keygo.domain.membership.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when a membership cannot be found.
 */
public class MembershipNotFoundException extends DomainException {

  public MembershipNotFoundException(String field, String value) {
    super("Membership not found by %s: %s".formatted(field, value));
  }
}
