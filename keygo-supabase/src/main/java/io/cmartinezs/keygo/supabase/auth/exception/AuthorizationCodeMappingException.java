package io.cmartinezs.keygo.supabase.auth.exception;

import io.cmartinezs.keygo.app.shared.exception.PortException;

/**
 * Thrown when an {@code AuthorizationCode} cannot be reconstructed from persisted data.
 */
public class AuthorizationCodeMappingException extends PortException {

  public AuthorizationCodeMappingException(Throwable cause) {
    super("Failed to reconstruct AuthorizationCode from persisted data", cause);
  }
}
