package io.cmartinezs.keygo.app.membership.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;
import java.util.UUID;

/**
 * Thrown when a membership already exists for the given user and client app.
 */
public class DuplicateMembershipException extends UseCaseException {

  public DuplicateMembershipException(UUID userId, UUID clientAppId) {
    super("Membership already exists for user %s in app %s".formatted(userId, clientAppId));
  }
}
