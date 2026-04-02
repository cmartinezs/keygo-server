package io.cmartinezs.keygo.app.membership.port;

import java.util.List;
import java.util.UUID;

/**
 * Port OUT for role hierarchy persistence operations.
 * <p>Puerto OUT para operaciones de persistencia de la jerarquía de roles.
 * Manages parent-child relationships between AppRoles within the same application.
 * Each role may have at most one parent; hierarchy depth is limited to {@code MAX_DEPTH} levels.
 */
public interface AppRoleHierarchyPort {

  /**
   * Assign {@code parentRoleId} as the direct parent of {@code childRoleId}.
   * If the child already has a parent it will be replaced.
   *
   * @param childRoleId  ID of the child role
   * @param parentRoleId ID of the parent role
   */
  void assignParent(UUID childRoleId, UUID parentRoleId);

  /**
   * Remove the parent of {@code childRoleId} (no-op if it has none).
   *
   * @param childRoleId ID of the role whose parent link should be removed
   */
  void removeParent(UUID childRoleId);

  /**
   * Return the IDs of all ancestor roles of {@code roleId}, traversing the tree upward.
   * Returns an empty list if the role has no parent.
   *
   * @param roleId the starting role
   * @return ordered list of ancestor IDs (closest ancestor first)
   */
  List<UUID> findAncestorIds(UUID roleId);
}
