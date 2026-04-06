package io.cmartinezs.keygo.supabase.membership.adapter;

import io.cmartinezs.keygo.app.membership.port.PlatformUserRoleRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.PlatformUserRole;
import io.cmartinezs.keygo.supabase.membership.entity.PlatformRoleEntity;
import io.cmartinezs.keygo.supabase.membership.entity.PlatformUserRoleEntity;
import io.cmartinezs.keygo.supabase.membership.exception.PlatformRolePersistenceException;
import io.cmartinezs.keygo.supabase.membership.mapper.MembershipPersistenceMapper;
import io.cmartinezs.keygo.supabase.membership.repository.PlatformRoleJpaRepository;
import io.cmartinezs.keygo.supabase.membership.repository.PlatformUserRoleJpaRepository;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import io.cmartinezs.keygo.supabase.user.repository.PlatformUserJpaRepository;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adapter: implements PlatformUserRoleRepositoryPort using JPA persistence.
 * <p>Adaptador: implementa PlatformUserRoleRepositoryPort usando persistencia JPA.
 * <p>Modelo restructurado (RFC restructure-multitenant):
 * FK now references platform_users instead of tenant_users.
 * @author cmartinezs
 * @version 2.0
 */
@Repository
public class PlatformUserRoleRepositoryAdapter implements PlatformUserRoleRepositoryPort {

  private final PlatformUserRoleJpaRepository jpaRepository;
  private final PlatformRoleJpaRepository platformRoleJpaRepository;
  private final PlatformUserJpaRepository platformUserJpaRepository;

  public PlatformUserRoleRepositoryAdapter(
      PlatformUserRoleJpaRepository jpaRepository,
      PlatformRoleJpaRepository platformRoleJpaRepository,
      PlatformUserJpaRepository platformUserJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.platformRoleJpaRepository = platformRoleJpaRepository;
    this.platformUserJpaRepository = platformUserJpaRepository;
  }

  @Override
  @Transactional
  public PlatformUserRole assign(UUID platformUserId, String roleCode) {
    PlatformUserEntity platformUser = platformUserJpaRepository.findById(platformUserId)
        .orElseThrow(() -> new PlatformRolePersistenceException(
            "PlatformUser not found: " + platformUserId));
    PlatformRoleEntity platformRole = platformRoleJpaRepository.findByCode(roleCode)
        .orElseThrow(() -> new PlatformRolePersistenceException(
            "PlatformRole not found: " + roleCode));

    PlatformUserRoleEntity entity = PlatformUserRoleEntity.builder()
        .platformUser(platformUser)
        .platformRole(platformRole)
        .assignedAt(OffsetDateTime.now())
        .build();

    return MembershipPersistenceMapper.toDomain(jpaRepository.save(entity));
  }

  @Override
  @Transactional
  public void revoke(UUID platformUserId, String roleCode) {
    PlatformRoleEntity platformRole = platformRoleJpaRepository.findByCode(roleCode)
        .orElseThrow(() -> new PlatformRolePersistenceException(
            "PlatformRole not found: " + roleCode));
    jpaRepository.deleteByPlatformUserIdAndPlatformRoleId(platformUserId, platformRole.getId());
  }

  @Override
  public List<PlatformUserRole> findByPlatformUserId(UUID platformUserId) {
    return jpaRepository.findByPlatformUserId(platformUserId)
        .stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public boolean hasRole(UUID platformUserId, String roleCode) {
    return jpaRepository.existsByPlatformUserIdAndPlatformRoleCode(platformUserId, roleCode);
  }

  @Override
  public List<String> findRoleCodesByPlatformUserId(UUID platformUserId) {
    return jpaRepository.findRoleCodesByPlatformUserId(platformUserId);
  }
}
