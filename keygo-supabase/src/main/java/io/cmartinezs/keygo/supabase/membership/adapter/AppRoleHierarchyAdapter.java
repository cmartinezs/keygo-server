package io.cmartinezs.keygo.supabase.membership.adapter;

import io.cmartinezs.keygo.app.membership.port.AppRoleHierarchyPort;
import io.cmartinezs.keygo.supabase.membership.entity.AppRoleEntity;
import io.cmartinezs.keygo.supabase.membership.entity.AppRoleHierarchyEntity;
import io.cmartinezs.keygo.supabase.membership.repository.AppRoleHierarchyJpaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter: implements AppRoleHierarchyPort using JPA persistence.
 */
@Repository
public class AppRoleHierarchyAdapter implements AppRoleHierarchyPort {

  private final AppRoleHierarchyJpaRepository jpaRepository;

  public AppRoleHierarchyAdapter(AppRoleHierarchyJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  @Transactional
  public void assignParent(UUID childRoleId, UUID parentRoleId) {
    // Replace any existing parent link (idempotent)
    jpaRepository.deleteByChildRoleId(childRoleId);

    AppRoleEntity childRef = new AppRoleEntity();
    childRef.setId(childRoleId);

    AppRoleEntity parentRef = new AppRoleEntity();
    parentRef.setId(parentRoleId);

    jpaRepository.save(
        AppRoleHierarchyEntity.builder()
            .childRole(childRef)
            .parentRole(parentRef)
            .build());
  }

  @Override
  @Transactional
  public void removeParent(UUID childRoleId) {
    jpaRepository.deleteByChildRoleId(childRoleId);
  }

  @Override
  public List<UUID> findAncestorIds(UUID roleId) {
    return jpaRepository.findAncestorIds(roleId);
  }
}
