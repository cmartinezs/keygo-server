package io.cmartinezs.keygo.app.membership.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;

import java.util.UUID;

/**
 * Thrown when a required {@code AppRole} is not found for the given client application.
 */
public class AppRoleNotFoundException extends UseCaseException {

  public AppRoleNotFoundException(UUID clientAppId, RoleCode code) {
    super("Role '%s' not found for clientApp: %s".formatted(code, clientAppId));
  }
}
