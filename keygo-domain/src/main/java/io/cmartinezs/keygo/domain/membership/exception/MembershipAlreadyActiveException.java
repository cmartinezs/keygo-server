package io.cmartinezs.keygo.domain.membership.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

import java.util.UUID;

/**
 * Thrown when an approval is attempted on a membership that is already active.
 * <p>Se lanza cuando se intenta aprobar una membresía que ya está activa.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class MembershipAlreadyActiveException extends DomainException {

  public MembershipAlreadyActiveException(UUID userId, UUID clientAppId) {
    super("Membership for user %s in app %s is already active".formatted(userId, clientAppId));
  }
}
