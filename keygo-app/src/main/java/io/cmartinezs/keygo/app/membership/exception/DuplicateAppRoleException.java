package io.cmartinezs.keygo.app.membership.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;
import java.util.UUID;

/**
 * Thrown when a role with the same code already exists in the given client app.
 */
public class DuplicateAppRoleException extends UseCaseException {

  public DuplicateAppRoleException(String code, UUID clientAppId) {
    super("Role code '%s' already exists in app %s".formatted(code, clientAppId));
  }
}
