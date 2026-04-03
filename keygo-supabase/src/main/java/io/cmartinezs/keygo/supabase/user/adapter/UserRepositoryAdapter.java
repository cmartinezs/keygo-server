package io.cmartinezs.keygo.supabase.user.adapter;

import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.user.filter.UserFilter;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.Username;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import io.cmartinezs.keygo.supabase.user.mapper.UserPersistenceMapper;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.criteria.Predicate;

/**
 * Adapter implementing UserRepositoryPort using Spring Data JPA.
 * <p>Adaptador que implementa UserRepositoryPort usando Spring Data JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

  private final TenantUserJpaRepository jpaRepository;
  private final UserPersistenceMapper mapper;

  public UserRepositoryAdapter(TenantUserJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
    this.mapper = new UserPersistenceMapper();
  }

  @Override
  public User save(User user) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(user)));
  }

  @Override
  public Optional<User> findByIdAndTenantId(UserId userId, TenantId tenantId) {
    return jpaRepository.findByIdAndTenantId(userId.value(), tenantId.value())
        .map(mapper::toDomain);
  }

  @Override
  public Optional<User> findByTenantIdAndEmail(TenantId tenantId, EmailAddress email) {
    return jpaRepository.findByTenantIdAndEmail(tenantId.value(), email.value())
        .map(mapper::toDomain);
  }

  @Override
  public Optional<User> findByTenantIdAndUsername(TenantId tenantId, Username username) {
    return jpaRepository.findByTenantIdAndUsername(tenantId.value(), username.value())
        .map(mapper::toDomain);
  }

  @Override
  public boolean existsByTenantIdAndEmail(TenantId tenantId, EmailAddress email) {
    return jpaRepository.existsByTenantIdAndEmail(tenantId.value(), email.value());
  }

  @Override
  public boolean existsByTenantIdAndUsername(TenantId tenantId, Username username) {
    return jpaRepository.existsByTenantIdAndUsername(tenantId.value(), username.value());
  }

  @Override
  public List<User> findAllByTenantId(TenantId tenantId) {
    return jpaRepository.findAllByTenantId(tenantId.value()).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public PagedResult<User> findAllPaged(TenantId tenantId, UserFilter filter) {
    Specification<TenantUserEntity> spec = buildSpecification(tenantId, filter);
    PageRequest pageRequest = buildPageRequest(filter);

    Page<TenantUserEntity> page = jpaRepository.findAll(spec, pageRequest);

    List<User> users = page.getContent().stream()
        .map(mapper::toDomain)
        .toList();

    return PagedResult.of(users, page.getNumber(), page.getSize(), page.getTotalElements());
  }

  private Specification<TenantUserEntity> buildSpecification(
      TenantId tenantId, UserFilter filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // Filter by tenantId (scope)
      predicates.add(cb.equal(root.get("tenant").get("id"), tenantId.value()));

      // Filter by status
      if (filter.hasStatus()) {
        predicates.add(cb.equal(root.get("status"), filter.getStatus()));
      }

      // Filter by username (case-insensitive LIKE)
      if (filter.hasUsernameLike()) {
        predicates.add(cb.like(
            cb.lower(root.get("username")),
            "%" + filter.getUsernameLike().toLowerCase() + "%"
        ));
      }

      // Filter by email (case-insensitive LIKE)
      if (filter.hasEmailLike()) {
        predicates.add(cb.like(
            cb.lower(root.get("email")),
            "%" + filter.getEmailLike().toLowerCase() + "%"
        ));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  private PageRequest buildPageRequest(UserFilter filter) {
    if (filter.hasSorting()) {
      Sort.Direction direction = Sort.Direction.fromString(filter.getSortOrder());
      return PageRequest.of(filter.getPage(), filter.getSize(), Sort.by(direction, filter.getSortBy()));
    }
    return PageRequest.of(filter.getPage(), filter.getSize());
  }
}

