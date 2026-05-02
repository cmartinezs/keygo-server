package io.cmartinezs.keygo.supabase.membership.repository;

import io.cmartinezs.keygo.supabase.membership.entity.PlatformUserRoleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for PlatformUserRoleEntity.
 * <p>Repositorio Spring Data JPA para PlatformUserRoleEntity.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public interface PlatformUserRoleJpaRepository extends JpaRepository<PlatformUserRoleEntity, UUID> {

  List<PlatformUserRoleEntity> findByPlatformUserId(UUID platformUserId);

  Optional<PlatformUserRoleEntity> findByPlatformUserIdAndPlatformRoleCode(UUID platformUserId, String roleCode);

  boolean existsByPlatformUserIdAndPlatformRoleCode(UUID platformUserId, String roleCode);

  void deleteByPlatformUserIdAndPlatformRoleId(UUID platformUserId, UUID platformRoleId);

  @Query("SELECT pur.platformRole.code FROM PlatformUserRoleEntity pur WHERE pur.platformUser.id = :platformUserId")
  List<String> findRoleCodesByPlatformUserId(@Param("platformUserId") UUID platformUserId);
}
