package io.cmartinezs.keygo.domain.clientapp.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Exception thrown when a client application has an invalid OAuth configuration.
 * <p>Excepción lanzada cuando una aplicación cliente tiene configuración OAuth inválida.
 * @author cmartinezs
 * @version 1.0
 */
public class InvalidClientAppConfigException extends DomainException {

  public InvalidClientAppConfigException(String message) {
    super(message);
  }
}
