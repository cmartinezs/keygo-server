package io.cmartinezs.keygo.app.clientapp.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;

/**
 * Thrown when a client application exists but is not active.
 */
public class ClientAppInactiveException extends UseCaseException {

  public ClientAppInactiveException(String clientId) {
    super("Client application '%s' is not active".formatted(clientId));
  }
}
