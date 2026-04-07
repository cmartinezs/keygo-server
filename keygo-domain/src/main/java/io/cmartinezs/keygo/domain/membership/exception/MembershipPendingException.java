package io.cmartinezs.keygo.domain.membership.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when an operation requires an active membership but it is still pending approval.
 * <p>Se lanza cuando una operación requiere una membresía activa pero aún está pendiente de aprobación.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class MembershipPendingException extends DomainException {

  public MembershipPendingException(String reason) {
    super("Membership is pending approval: %s".formatted(reason));
  }
}
