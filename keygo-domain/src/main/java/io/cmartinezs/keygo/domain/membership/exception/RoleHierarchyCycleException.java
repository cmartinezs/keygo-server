package io.cmartinezs.keygo.domain.membership.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;
import java.util.UUID;

/**
 * Thrown when assigning a parent role would create a cycle in the hierarchy.
 */
public class RoleHierarchyCycleException extends DomainException {

  public RoleHierarchyCycleException(UUID childRoleId, UUID parentRoleId) {
    super("Assigning role %s as parent of %s would create a hierarchy cycle"
        .formatted(parentRoleId, childRoleId));
  }
}
