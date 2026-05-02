package io.cmartinezs.keygo.domain.clientapp.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Exception thrown when a redirect URI is invalid or not allowed.
 * <p>Excepción lanzada cuando un URI de redirección es inválido o no está permitido.
 * @author cmartinezs
 * @version 1.0
 */
public class InvalidRedirectUriException extends DomainException {

  public InvalidRedirectUriException(String uri) {
    super("Invalid redirect URI: %s".formatted(uri));
  }
}

