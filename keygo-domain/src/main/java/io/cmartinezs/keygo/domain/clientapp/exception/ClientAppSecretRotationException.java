package io.cmartinezs.keygo.domain.clientapp.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when a secret rotation is attempted on a client application that does not support it.
 */
public class ClientAppSecretRotationException extends DomainException {

  public ClientAppSecretRotationException(String clientId) {
    super("Cannot rotate secret for PUBLIC client app: %s".formatted(clientId));
  }
}
