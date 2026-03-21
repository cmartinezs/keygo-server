package io.cmartinezs.keygo.domain.clientapp.exception;

/**
 * Exception thrown when a client application is not found.
 * <p>Excepción lanzada cuando no se encuentra una aplicación cliente.
 * @author cmartinezs
 * @version 1.0
 */
public class ClientAppNotFoundException extends RuntimeException {

  public ClientAppNotFoundException(String clientId) {
    super("Client application with clientId '" + clientId + "' was not found");
  }
}

