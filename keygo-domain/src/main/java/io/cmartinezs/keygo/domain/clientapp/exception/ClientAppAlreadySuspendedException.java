package io.cmartinezs.keygo.domain.clientapp.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when a suspension is attempted on a client application that is already suspended.
 */
public class ClientAppAlreadySuspendedException extends DomainException {

  public ClientAppAlreadySuspendedException(String clientId) {
    super("Client app '%s' is already suspended".formatted(clientId));
  }
}
