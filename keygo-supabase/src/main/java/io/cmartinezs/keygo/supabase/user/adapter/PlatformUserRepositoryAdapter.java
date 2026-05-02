package io.cmartinezs.keygo.supabase.user.adapter;

import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.user.filter.PlatformUserFilter;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.Username;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import io.cmartinezs.keygo.supabase.user.mapper.PlatformUserPersistenceMapper;
import io.cmartinezs.keygo.supabase.user.repository.PlatformUserJpaRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

/**
 * Adapter: implements PlatformUserRepositoryPort using JPA persistence.
 * <p>Adaptador: implementa PlatformUserRepositoryPort usando persistencia JPA.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class PlatformUserRepositoryAdapter implements PlatformUserRepositoryPort {

  private final PlatformUserJpaRepository jpaRepository;
  private final PlatformUserPersistenceMapper mapper;

  public PlatformUserRepositoryAdapter(PlatformUserJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
    this.mapper = new PlatformUserPersistenceMapper();
  }

  @Override
  public PlatformUser save(PlatformUser user) {
    PlatformUserEntity entity = mapper.toEntity(user);
    PlatformUserEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<PlatformUser> findByEmail(EmailAddress email) {
    return jpaRepository.findByEmail(email.value()).map(mapper::toDomain);
  }

  @Override
  public Optional<PlatformUser> findByUsername(Username username) {
    return jpaRepository.findByUsername(username.value()).map(mapper::toDomain);
  }

  @Override
  public Optional<PlatformUser> findById(UserId userId) {
    return jpaRepository.findById(userId.value()).map(mapper::toDomain);
  }

  @Override
  public boolean existsByEmail(EmailAddress email) {
    return jpaRepository.existsByEmail(email.value());
  }

  @Override
  public boolean existsByUsername(Username username) {
    return jpaRepository.existsByUsername(username.value());
  }

  @Override
  public PagedResult<PlatformUser> findAllPaged(PlatformUserFilter filter) {
    Specification<PlatformUserEntity> specification = buildSpecification(filter);
    PageRequest pageRequest = buildPageRequest(filter);
    Page<PlatformUserEntity> page = jpaRepository.findAll(specification, pageRequest);

    List<PlatformUser> users = page.getContent().stream().map(mapper::toDomain).toList();
    return PagedResult.of(users, page.getNumber(), page.getSize(), page.getTotalElements());
  }

  private Specification<PlatformUserEntity> buildSpecification(PlatformUserFilter filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (filter.hasStatus()) {
        predicates.add(cb.equal(root.get("status"), filter.getStatus().name()));
      }

      if (filter.hasUsernameLike()) {
        predicates.add(
            cb.like(
                cb.lower(root.get("displayName")),
                "%" + filter.getUsernameLike().toLowerCase() + "%"));
      }

      if (filter.hasEmailLike()) {
        predicates.add(
            cb.like(cb.lower(root.get("email")), "%" + filter.getEmailLike().toLowerCase() + "%"));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  private PageRequest buildPageRequest(PlatformUserFilter filter) {
    if (!filter.hasSorting()) {
      return PageRequest.of(filter.getPage(), filter.getSize());
    }

    Sort.Direction direction = Sort.Direction.fromString(filter.getSortOrder());
    return PageRequest.of(filter.getPage(), filter.getSize(), Sort.by(direction, mapSortField(filter.getSortBy())));
  }

  private String mapSortField(String sortBy) {
    return switch (sortBy) {
      case "username" -> "displayName";
      case "createdAt" -> "createdAt";
      case "email" -> "email";
      case "status" -> "status";
      case "firstName" -> "firstName";
      case "lastName" -> "lastName";
      default -> sortBy;
    };
  }
}
