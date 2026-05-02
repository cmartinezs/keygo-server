package io.cmartinezs.keygo.domain.membership.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when a role hierarchy would exceed the maximum allowed depth.
 */
public class RoleHierarchyDepthExceededException extends DomainException {

  public static final int MAX_DEPTH = 5;

  public RoleHierarchyDepthExceededException() {
    super("Role hierarchy depth cannot exceed %d levels".formatted(MAX_DEPTH));
  }
}
