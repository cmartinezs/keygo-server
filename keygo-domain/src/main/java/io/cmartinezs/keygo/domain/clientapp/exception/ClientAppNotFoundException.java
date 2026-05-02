package io.cmartinezs.keygo.domain.clientapp.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Exception thrown when a client application is not found.
 * <p>Excepción lanzada cuando no se encuentra una aplicación cliente.
 * @author cmartinezs
 * @version 1.0
 */
public class ClientAppNotFoundException extends DomainException {

  public ClientAppNotFoundException(String clientId) {
    super("Client app not found by clientId: %s".formatted(clientId));
  }
}

