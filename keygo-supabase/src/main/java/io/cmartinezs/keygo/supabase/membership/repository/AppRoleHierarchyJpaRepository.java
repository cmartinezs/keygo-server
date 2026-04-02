package io.cmartinezs.keygo.supabase.membership.repository;

import io.cmartinezs.keygo.supabase.membership.entity.AppRoleHierarchyEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for AppRoleHierarchyEntity.
 */
@Repository
public interface AppRoleHierarchyJpaRepository
    extends JpaRepository<AppRoleHierarchyEntity, UUID> {

  /** Remove the parent link of a given child role (no-op if none). */
  void deleteByChildRoleId(UUID childRoleId);

  /**
   * Recursively find all ancestor role IDs of {@code roleId} via CTE.
   * Returns an empty list if the role has no parent.
   */
  @Query(value = """
      WITH RECURSIVE ancestors AS (
        SELECT parent_role_id AS role_id
        FROM   app_role_hierarchy
        WHERE  child_role_id = :roleId
        UNION
        SELECT arh.parent_role_id
        FROM   app_role_hierarchy arh
        JOIN   ancestors a ON arh.child_role_id = a.role_id
      )
      SELECT role_id FROM ancestors
      """, nativeQuery = true)
  List<UUID> findAncestorIds(@Param("roleId") UUID roleId);
}
