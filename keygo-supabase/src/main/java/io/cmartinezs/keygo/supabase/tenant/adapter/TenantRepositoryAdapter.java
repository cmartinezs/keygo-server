package io.cmartinezs.keygo.supabase.tenant.adapter;

import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.tenant.filter.TenantFilter;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import io.cmartinezs.keygo.supabase.tenant.mapper.TenantPersistenceMapper;
import io.cmartinezs.keygo.supabase.tenant.repository.TenantJpaRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing TenantRepositoryPort using Spring Data JPA.
 * Adaptador que implementa TenantRepositoryPort usando Spring Data JPA.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class TenantRepositoryAdapter implements TenantRepositoryPort {

  private final TenantJpaRepository jpaRepository;
  private final TenantPersistenceMapper mapper;

  public TenantRepositoryAdapter(TenantJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
    this.mapper = new TenantPersistenceMapper();
  }

  @Override
  public Tenant save(Tenant tenant) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(tenant)));
  }

  @Override
  public Optional<Tenant> findBySlug(TenantSlug slug) {
    return jpaRepository.findBySlug(slug.value()).map(mapper::toDomain);
  }

  @Override
  public boolean existsBySlug(TenantSlug slug) {
    return jpaRepository.existsBySlug(slug.value());
  }

  @Override
  public PagedResult<Tenant> findAll(TenantFilter filter) {
    Specification<TenantEntity> spec = buildSpecification(filter);
    PageRequest pageRequest = buildPageRequest(filter);

    Page<TenantEntity> page = jpaRepository.findAll(spec, pageRequest);

    List<Tenant> tenants = page.getContent().stream()
        .map(mapper::toDomain)
        .toList();

    return PagedResult.of(tenants, page.getNumber(), page.getSize(), page.getTotalElements());
  }

  // ─── Private helpers ──────────────────────────────────────────────────────

  private Specification<TenantEntity> buildSpecification(TenantFilter filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (filter.hasStatus()) {
        predicates.add(cb.equal(root.get("status"), filter.getStatus()));
      }

      if (filter.hasNameLike()) {
        predicates.add(cb.like(
            cb.lower(root.get("name")),
            "%" + filter.getNameLike().toLowerCase() + "%"
        ));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  private PageRequest buildPageRequest(TenantFilter filter) {
    if (filter.hasSorting()) {
      Sort.Direction direction = Sort.Direction.fromString(filter.getSortOrder());
      return PageRequest.of(filter.getPage(), filter.getSize(), Sort.by(direction, filter.getSortBy()));
    }
    return PageRequest.of(filter.getPage(), filter.getSize());
  }
}

