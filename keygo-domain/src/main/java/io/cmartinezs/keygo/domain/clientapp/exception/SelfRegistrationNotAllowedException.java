package io.cmartinezs.keygo.domain.clientapp.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Exception thrown when a self-registration attempt is made against a client app
 * whose registration policy does not allow it (e.g. INVITE_ONLY).
 *
 * <p>Excepción lanzada cuando se intenta auto-registro en una app cuya política no lo permite
 * (e.g. INVITE_ONLY).
 *
 * @author cmartinezs
 * @version 1.0
 */
public class SelfRegistrationNotAllowedException extends DomainException {

  public SelfRegistrationNotAllowedException(String clientId) {
    super("Self-registration is not allowed for client app: %s".formatted(clientId));
  }
}
