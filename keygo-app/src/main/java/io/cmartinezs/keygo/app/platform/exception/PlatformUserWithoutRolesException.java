package io.cmartinezs.keygo.app.platform.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;
import java.util.UUID;

/**
 * Thrown when a platform user has no roles assigned and cannot obtain a token.
 */
public class PlatformUserWithoutRolesException extends UseCaseException {

  public PlatformUserWithoutRolesException(UUID platformUserId) {
    super("Platform user has no roles assigned: " + platformUserId);
  }
}
