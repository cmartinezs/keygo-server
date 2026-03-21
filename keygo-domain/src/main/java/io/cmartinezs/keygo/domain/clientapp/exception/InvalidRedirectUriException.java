package io.cmartinezs.keygo.domain.clientapp.exception;

/**
 * Exception thrown when a redirect URI is invalid or not allowed.
 * <p>Excepción lanzada cuando un URI de redirección es inválido o no está permitido.
 * @author cmartinezs
 * @version 1.0
 */
public class InvalidRedirectUriException extends RuntimeException {

  public InvalidRedirectUriException(String uri) {
    super("Invalid redirect URI: '" + uri + "'. Must start with https://, http://localhost or http://127.0.0.1");
  }
}

