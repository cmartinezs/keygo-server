package io.cmartinezs.keygo.supabase.membership.adapter;

import io.cmartinezs.keygo.app.membership.filter.MembershipFilter;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.domain.membership.exception.MembershipNotFoundException;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.membership.entity.AppRoleEntity;
import io.cmartinezs.keygo.supabase.membership.entity.MembershipEntity;
import io.cmartinezs.keygo.supabase.membership.mapper.MembershipPersistenceMapper;
import io.cmartinezs.keygo.supabase.membership.repository.MembershipJpaRepository;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter: implements MembershipRepositoryPort using JPA persistence.
 *
 * <p>Adaptador: implementa MembershipRepositoryPort usando persistencia JPA.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class MembershipRepositoryAdapter implements MembershipRepositoryPort {

  private final MembershipJpaRepository jpaRepository;
  private final EntityManager entityManager;

  public MembershipRepositoryAdapter(
      MembershipJpaRepository jpaRepository,
      EntityManager entityManager) {
    this.jpaRepository = jpaRepository;
    this.entityManager = entityManager;
  }

  @Override
  public Optional<Membership> findById(MembershipId membershipId) {
    return jpaRepository.findById(membershipId.value()).map(MembershipPersistenceMapper::toDomain);
  }

  @Override
  public Optional<Membership> findByUserAndClientApp(UUID userId, UUID clientAppId) {
    return jpaRepository
        .findByUserIdAndClientAppId(userId, clientAppId)
        .map(MembershipPersistenceMapper::toDomain);
  }

  @Override
  public List<Membership> findByUserId(UUID userId) {
    return jpaRepository.findByUserId(userId).stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public List<Membership> findByUserIdAndTenantSlug(UUID userId, String tenantSlug) {
    return jpaRepository.findByUserIdAndTenantSlug(userId, tenantSlug).stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public List<Membership> findByClientAppId(UUID clientAppId) {
    return jpaRepository.findByClientAppId(clientAppId).stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public List<Membership> findByClientAppIdAndTenantSlug(UUID clientAppId, String tenantSlug) {
    return jpaRepository.findByClientAppIdAndTenantSlug(clientAppId, tenantSlug).stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<Membership> findByIdAndTenantSlug(MembershipId membershipId, String tenantSlug) {
    return jpaRepository
        .findByIdAndTenantSlug(membershipId.value(), tenantSlug)
        .map(MembershipPersistenceMapper::toDomain);
  }

  @Override
  public boolean existsByUserAndClientApp(UUID userId, UUID clientAppId) {
    return jpaRepository.existsByUserIdAndClientAppId(userId, clientAppId);
  }

  @Override
  @Transactional
  public Membership save(Membership membership) {
    MembershipEntity entity = new MembershipEntity();

    // Set FK references (non-managed entities, only IDs)
    TenantUserEntity userRef = new TenantUserEntity();
    userRef.setId(membership.getUserId().value());
    entity.setUser(userRef);
    entity.setTenantUserId(userRef.getId());
    entity.setTenantId(resolveTenantId(membership.getUserId().value()));

    ClientAppEntity appRef = new ClientAppEntity();
    appRef.setId(membership.getClientAppId().value());
    entity.setClientApp(appRef);
    entity.setClientAppId(appRef.getId());

    entity.setStatus(membership.getStatus());

    MembershipEntity saved = jpaRepository.save(entity);
    replaceRoles(saved.getId(), membership.getClientAppId().value(), membership.getRoles().stream()
        .map(role -> role.roleId().value())
        .toList());
    return MembershipPersistenceMapper.toDomain(saved);
  }

  @Override
  public Membership update(Membership membership) {
    MembershipEntity entity =
        jpaRepository
            .findById(membership.getId().value())
            .orElseThrow(
                () -> new MembershipNotFoundException("id", membership.getId().value().toString()));

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

  @Override
  public List<String> findEffectiveRoleCodesByUserAndClientApp(UUID userId, UUID clientAppId) {
    return jpaRepository.findEffectiveRoleCodesByUserIdAndClientAppId(userId, clientAppId);
  }

  @Override
  public PagedResult<Membership> findAllPaged(String tenantSlug, MembershipFilter filter) {
    Specification<MembershipEntity> spec = buildSpecification(tenantSlug, filter);
    PageRequest pageRequest = buildPageRequest(filter);

    Page<MembershipEntity> page = jpaRepository.findAll(spec, pageRequest);

    List<Membership> memberships = page.getContent().stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();

    return PagedResult.of(memberships, page.getNumber(), page.getSize(), page.getTotalElements());
  }

  private Specification<MembershipEntity> buildSpecification(String tenantSlug, MembershipFilter filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // Filter by tenantSlug (scope of security — always applied)
      predicates.add(cb.equal(root.get("user").get("tenant").get("slug"), tenantSlug));

      // Filter by userId (optional)
      if (filter.hasUserId()) {
        predicates.add(cb.equal(root.get("user").get("id"), filter.getUserId()));
      }

      // Filter by clientAppId (optional)
      if (filter.hasClientAppId()) {
        predicates.add(cb.equal(root.get("clientApp").get("id"), filter.getClientAppId()));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  private PageRequest buildPageRequest(MembershipFilter filter) {
    if (filter.hasSorting()) {
      Sort.Direction direction = Sort.Direction.fromString(filter.getSortOrder());
      return PageRequest.of(filter.getPage(), filter.getSize(), Sort.by(direction, filter.getSortBy()));
    }
    return PageRequest.of(filter.getPage(), filter.getSize());
  }

  private UUID resolveTenantId(UUID tenantUserId) {
    return entityManager
        .createQuery(
            "SELECT tu.tenant.id FROM TenantUserEntity tu WHERE tu.id = :tenantUserId",
            UUID.class)
        .setParameter("tenantUserId", tenantUserId)
        .getSingleResult();
  }

  private void replaceRoles(UUID membershipId, UUID clientAppId, List<UUID> roleIds) {
    entityManager
        .createNativeQuery("DELETE FROM app_membership_roles WHERE membership_id = :membershipId")
        .setParameter("membershipId", membershipId)
        .executeUpdate();

    for (UUID roleId : roleIds) {
      entityManager
          .createNativeQuery(
              "INSERT INTO app_membership_roles (membership_id, client_app_id, role_id, assigned_at) "
                  + "VALUES (:membershipId, :clientAppId, :roleId, now())")
          .setParameter("membershipId", membershipId)
          .setParameter("clientAppId", clientAppId)
          .setParameter("roleId", roleId)
          .executeUpdate();
    }
  }
}
