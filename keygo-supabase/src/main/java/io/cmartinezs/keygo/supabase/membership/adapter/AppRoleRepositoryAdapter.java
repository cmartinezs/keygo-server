package io.cmartinezs.keygo.supabase.membership.adapter;

import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.app.role.filter.AppRoleFilter;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.membership.model.AppRoleId;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.membership.entity.AppRoleEntity;
import io.cmartinezs.keygo.supabase.membership.exception.AppRolePersistenceException;
import io.cmartinezs.keygo.supabase.membership.mapper.MembershipPersistenceMapper;
import io.cmartinezs.keygo.supabase.membership.repository.AppRoleJpaRepository;
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

/**
 * Adapter: implements AppRoleRepositoryPort using JPA persistence.
 * <p>Adaptador: implementa AppRoleRepositoryPort usando persistencia JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class AppRoleRepositoryAdapter implements AppRoleRepositoryPort {

  private final AppRoleJpaRepository jpaRepository;

  public AppRoleRepositoryAdapter(AppRoleJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<AppRole> findById(AppRoleId roleId) {
    return jpaRepository.findById(roleId.value())
        .map(MembershipPersistenceMapper::toDomain);
  }

  @Override
  public Optional<AppRole> findByClientAppAndCode(UUID clientAppId, RoleCode roleCode) {
    return jpaRepository.findByClientAppIdAndCode(clientAppId, roleCode.value())
        .map(MembershipPersistenceMapper::toDomain);
  }

  @Override
  public List<AppRole> findByClientAppId(UUID clientAppId) {
    return jpaRepository.findByClientAppId(clientAppId)
        .stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public boolean existsByClientAppAndCode(UUID clientAppId, RoleCode roleCode) {
    return jpaRepository.existsByClientAppIdAndCode(clientAppId, roleCode.value());
  }

  @Override
  public int countByClientAppId(UUID clientAppId) {
    return jpaRepository.countByClientAppId(clientAppId);
  }

  @Override
  public Optional<AppRole> findDefaultByClientApp(UUID clientAppId) {
    return jpaRepository.findByClientAppIdAndIsDefaultTrue(clientAppId)
        .map(MembershipPersistenceMapper::toDomain);
  }

  @Override
  public void clearDefaultByClientApp(UUID clientAppId) {
    jpaRepository.clearDefaultByClientAppId(clientAppId);
  }

  @Override
  public AppRole save(AppRole role) {
    AppRoleEntity entity = new AppRoleEntity();
    entity.setId(role.getId().value());
    entity.setCode(role.getCode().value());
    entity.setDisplayName(role.getDisplayName());
    entity.setDescription(role.getDescription());
    entity.setDefault(role.isDefault());

    // Set FK reference (non-managed entity, only ID)
    ClientAppEntity appRef = new ClientAppEntity();
    appRef.setId(role.getClientAppId().value());
    entity.setClientApp(appRef);

    AppRoleEntity saved = jpaRepository.save(entity);
    return MembershipPersistenceMapper.toDomain(saved);
  }

  @Override
  public AppRole update(AppRole role) {
    AppRoleEntity entity = jpaRepository.findById(role.getId().value())
        .orElseThrow(() -> new AppRolePersistenceException(role.getId().value()));

    entity.setDisplayName(role.getDisplayName());
    entity.setDescription(role.getDescription());
    entity.setDefault(role.isDefault());
    AppRoleEntity updated = jpaRepository.save(entity);
    return MembershipPersistenceMapper.toDomain(updated);
  }

  @Override
  public void deleteById(AppRoleId roleId) {
    jpaRepository.deleteById(roleId.value());
  }

  @Override
  public PagedResult<AppRole> findAllPaged(UUID clientAppId, AppRoleFilter filter) {
    Specification<AppRoleEntity> spec = buildSpecification(clientAppId, filter);
    PageRequest pageRequest = buildPageRequest(filter);

    Page<AppRoleEntity> page = jpaRepository.findAll(spec, pageRequest);

    List<AppRole> roles = page.getContent().stream()
        .map(MembershipPersistenceMapper::toDomain)
        .toList();

    return PagedResult.of(roles, page.getNumber(), page.getSize(), page.getTotalElements());
  }

  private Specification<AppRoleEntity> buildSpecification(UUID clientAppId, AppRoleFilter filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // Filter by clientAppId (scope)
      predicates.add(cb.equal(root.get("clientApp").get("id"), clientAppId));

      // Filter by displayName (case-insensitive LIKE)
      if (filter.hasNameLike()) {
        predicates.add(cb.like(
            cb.lower(root.get("displayName")),
            "%" + filter.getNameLike().toLowerCase() + "%"
        ));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  private PageRequest buildPageRequest(AppRoleFilter filter) {
    if (filter.hasSorting()) {
      Sort.Direction direction = Sort.Direction.fromString(filter.getSortOrder());
      // Map "name" to "displayName" column
      String sortBy = "name".equals(filter.getSortBy()) ? "displayName" : filter.getSortBy();
      return PageRequest.of(filter.getPage(), filter.getSize(), Sort.by(direction, sortBy));
    }
    return PageRequest.of(filter.getPage(), filter.getSize());
  }
}

