package io.cmartinezs.keygo.app.auth.exception;

import io.cmartinezs.keygo.app.shared.exception.PortException;

/**
 * Thrown when a required hashing algorithm is not available in the JVM.
 */
public class HashingUnavailableException extends PortException {

  public HashingUnavailableException(String algorithm, Throwable cause) {
    super("Hashing algorithm unavailable: %s".formatted(algorithm), cause);
  }
}
