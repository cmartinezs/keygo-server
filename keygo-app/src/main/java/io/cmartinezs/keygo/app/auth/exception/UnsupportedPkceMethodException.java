package io.cmartinezs.keygo.app.auth.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;

/**
 * Thrown when the requested PKCE code challenge method is not supported.
 */
public class UnsupportedPkceMethodException extends UseCaseException {

  public UnsupportedPkceMethodException(String method) {
    super("Unsupported PKCE method: %s".formatted(method));
  }
}
