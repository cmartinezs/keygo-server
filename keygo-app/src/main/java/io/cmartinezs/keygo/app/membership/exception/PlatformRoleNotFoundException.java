package io.cmartinezs.keygo.app.membership.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;

/**
 * Thrown when a required PlatformRole is not found.
 */
public class PlatformRoleNotFoundException extends UseCaseException {

  public PlatformRoleNotFoundException(String code) {
    super("PlatformRole not found: " + code);
  }
}
