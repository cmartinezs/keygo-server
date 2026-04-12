package io.cmartinezs.keygo.supabase.membership.adapter;

import io.cmartinezs.keygo.app.membership.port.AppRoleHierarchyPort;
import io.cmartinezs.keygo.supabase.membership.entity.AppRoleEntity;
import io.cmartinezs.keygo.supabase.membership.entity.AppRoleHierarchyEntity;
import io.cmartinezs.keygo.supabase.membership.repository.AppRoleJpaRepository;
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
  private final AppRoleJpaRepository appRoleJpaRepository;

  public AppRoleHierarchyAdapter(
      AppRoleHierarchyJpaRepository jpaRepository,
      AppRoleJpaRepository appRoleJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.appRoleJpaRepository = appRoleJpaRepository;
  }

  @Override
  @Transactional
  public void assignParent(UUID childRoleId, UUID parentRoleId) {
    // Replace any existing parent link (idempotent)
    jpaRepository.deleteByChildRoleId(childRoleId);

    AppRoleEntity childRef =
        appRoleJpaRepository
            .findById(childRoleId)
            .orElseThrow(() -> new IllegalArgumentException("Child role not found: " + childRoleId));

    AppRoleEntity parentRef =
        appRoleJpaRepository
            .findById(parentRoleId)
            .orElseThrow(
                () -> new IllegalArgumentException("Parent role not found: " + parentRoleId));

    UUID clientAppId = childRef.getClientApp().getId();
    if (!clientAppId.equals(parentRef.getClientApp().getId())) {
      throw new IllegalArgumentException(
          "App roles must belong to the same client app: "
              + childRoleId
              + " -> "
              + parentRoleId);
    }

    jpaRepository.save(
        AppRoleHierarchyEntity.builder()
            .childRoleId(childRoleId)
            .childRole(childRef)
            .parentRoleId(parentRoleId)
            .parentRole(parentRef)
            .clientAppId(clientAppId)
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
