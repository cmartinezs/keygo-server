package io.cmartinezs.keygo.app.auth.exception;

import io.cmartinezs.keygo.app.shared.exception.PortException;

/**
 * Thrown when JWT signing fails due to a key or cryptographic error.
 */
public class JwtSigningException extends PortException {

  public JwtSigningException(String reason, Throwable cause) {
    super("Failed to sign JWT: %s".formatted(reason), cause);
  }
}
