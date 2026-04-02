package io.cmartinezs.keygo.domain.membership.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

import java.util.UUID;

/**
 * Thrown when a suspension is attempted on a membership that is already suspended.
 */
public class MembershipAlreadySuspendedException extends DomainException {

  public MembershipAlreadySuspendedException(UUID userId, UUID clientAppId) {
    super("Membership for user %s in app %s is already suspended".formatted(userId, clientAppId));
  }
}
