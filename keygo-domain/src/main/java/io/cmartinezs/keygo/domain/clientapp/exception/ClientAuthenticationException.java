package io.cmartinezs.keygo.domain.clientapp.exception;

/**
 * Exception thrown when client authentication fails during the client_credentials grant.
 *
 * <p>Lanzada cuando la autenticación del cliente falla en el grant client_credentials.
 * Scenarios:
 * <ul>
 *   <li>El cliente es de tipo PUBLIC (no puede usar client_credentials)</li>
 *   <li>El client_secret proporcionado no coincide con el almacenado</li>
 * </ul>
 *
 * @author cmartinezs
 * @version 1.0
 */
public class ClientAuthenticationException extends RuntimeException {

  public ClientAuthenticationException(String message) {
    super(message);
  }
}

