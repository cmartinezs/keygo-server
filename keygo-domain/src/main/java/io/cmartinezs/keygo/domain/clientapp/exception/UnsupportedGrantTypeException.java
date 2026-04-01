package io.cmartinezs.keygo.domain.clientapp.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Exception thrown when an unsupported OAuth2 grant type is requested.
 * <p>Excepción lanzada cuando se solicita un grant type OAuth2 no soportado.
 * @author cmartinezs
 * @version 1.0
 */
public class UnsupportedGrantTypeException extends DomainException {

  public UnsupportedGrantTypeException(String grantType) {
    super("Unsupported grant type: %s".formatted(grantType));
  }
}

