package io.cmartinezs.keygo.domain.membership.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when an invalid role assignment is attempted.
 */
public class InvalidRoleAssignmentException extends DomainException {

  public InvalidRoleAssignmentException(String reason) {
    super("Invalid role assignment: %s".formatted(reason));
  }
}
