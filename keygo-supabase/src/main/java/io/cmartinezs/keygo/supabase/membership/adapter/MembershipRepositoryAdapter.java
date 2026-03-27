package io.cmartinezs.keygo.supabase.membership.adapter;

import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.membership.entity.MembershipEntity;
import io.cmartinezs.keygo.supabase.membership.mapper.MembershipPersistenceMapper;
import io.cmartinezs.keygo.supabase.membership.repository.MembershipJpaRepository;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adapter: implements MembershipRepositoryPort using JPA persistence.
 * <p>Adaptador: implementa MembershipRepositoryPort usando persistencia JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class MembershipRepositoryAdapter implements MembershipRepositoryPort {

  private final MembershipJpaRepository jpaRepository;

  public MembershipRepositoryAdapter(MembershipJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<Membership> findById(MembershipId membershipId) {
    return jpaRepository.findById(membershipId.value())
        .map(MembershipPersistenceMapper::toDomain);
  }

  @Override
  public Optional<Membership> findByUserAndClientApp(UUID userId, UUID clientAppId) {
    return jpaRepository.findByUserIdAndClientAppId(userId, clientAppId)
        .map(MembershipPersistenceMapper::toDomain);
  }

  @Override
  public List<Membership> findByUserId(UUID userId) {
    return jpaRepository.findByUserId(userId)
        .stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public List<Membership> findByUserIdAndTenantSlug(UUID userId, String tenantSlug) {
    return jpaRepository.findByUserIdAndTenantSlug(userId, tenantSlug)
        .stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public List<Membership> findByClientAppId(UUID clientAppId) {
    return jpaRepository.findByClientAppId(clientAppId)
        .stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public List<Membership> findByClientAppIdAndTenantSlug(UUID clientAppId, String tenantSlug) {
    return jpaRepository.findByClientAppIdAndTenantSlug(clientAppId, tenantSlug)
        .stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<Membership> findByIdAndTenantSlug(MembershipId membershipId, String tenantSlug) {
    return jpaRepository.findByIdAndTenantSlug(membershipId.value(), tenantSlug)
        .map(MembershipPersistenceMapper::toDomain);
  }

  @Override
  public boolean existsByUserAndClientApp(UUID userId, UUID clientAppId) {
    return jpaRepository.existsByUserIdAndClientAppId(userId, clientAppId);
  }

  @Override
  public Membership save(Membership membership) {
    MembershipEntity entity = new MembershipEntity();
    entity.setId(membership.getId().value());

    // Set FK references (non-managed entities, only IDs)
    TenantUserEntity userRef = new TenantUserEntity();
    userRef.setId(membership.getUserId().value());
    entity.setUser(userRef);

    ClientAppEntity appRef = new ClientAppEntity();
    appRef.setId(membership.getClientAppId().value());
    entity.setClientApp(appRef);

    entity.setStatus(membership.getStatus());

    MembershipEntity saved = jpaRepository.save(entity);
    return MembershipPersistenceMapper.toDomain(saved);
  }

  @Override
  public Membership update(Membership membership) {
    MembershipEntity entity = jpaRepository.findById(membership.getId().value())
        .orElseThrow(() -> new IllegalArgumentException("Membership not found: " + membership.getId()));

    entity.setStatus(membership.getStatus());
    MembershipEntity updated = jpaRepository.save(entity);
    return MembershipPersistenceMapper.toDomain(updated);
  }

  @Override
  public void deleteById(MembershipId membershipId) {
    jpaRepository.deleteById(membershipId.value());
  }

  @Override
  public List<String> findRoleCodesByUserAndClientApp(UUID userId, UUID clientAppId) {
    return jpaRepository.findRoleCodesByUserIdAndClientAppId(userId, clientAppId);
  }
}

