package io.cmartinezs.keygo.supabase.clientapp.adapter;

import io.cmartinezs.keygo.app.clientapp.filter.ClientAppFilter;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.clientapp.mapper.ClientAppPersistenceMapper;
import io.cmartinezs.keygo.supabase.clientapp.repository.ClientAppJpaRepository;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing ClientAppRepositoryPort using Spring Data JPA.
 * <p>Adaptador que implementa ClientAppRepositoryPort usando Spring Data JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class ClientAppRepositoryAdapter implements ClientAppRepositoryPort {

  private final ClientAppJpaRepository jpaRepository;
  private final ClientAppPersistenceMapper mapper;

  public ClientAppRepositoryAdapter(ClientAppJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
    this.mapper = new ClientAppPersistenceMapper();
  }

  @Override
  public ClientApp save(ClientApp clientApp) {
    ClientAppEntity entity = mapper.toEntity(clientApp);

    // Ensure tenant reference is set (JPA requires managed entity for FK)
    TenantEntity tenantRef = new TenantEntity();
    tenantRef.setId(clientApp.getTenantId().value());
    entity.setTenant(tenantRef);

    return mapper.toDomain(jpaRepository.save(entity));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ClientApp> findByClientIdAndTenantId(ClientId clientId, TenantId tenantId) {
    return jpaRepository
        .findByClientIdAndTenantId(clientId.value(), tenantId.value())
        .map(mapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ClientApp> findAllByTenantId(TenantId tenantId) {
    return jpaRepository
        .findAllByTenantId(tenantId.value())
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public boolean existsByClientId(ClientId clientId) {
    return jpaRepository.existsByClientId(clientId.value());
  }

  @Override
  @Transactional(readOnly = true)
  public java.util.Optional<ClientApp> findById(ClientAppId id) {
    return jpaRepository.findById(id.value()).map(mapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public java.util.Optional<ClientApp> findByClientId(ClientId clientId) {
    return jpaRepository.findByClientId(clientId.value()).map(mapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public PagedResult<ClientApp> findAllPaged(TenantId tenantId, ClientAppFilter filter) {
    Specification<ClientAppEntity> spec = buildSpecification(tenantId, filter);
    PageRequest pageRequest = buildPageRequest(filter);

    Page<ClientAppEntity> page = jpaRepository.findAll(spec, pageRequest);

    List<ClientApp> apps = page.getContent().stream()
        .map(mapper::toDomain)
        .toList();

    return PagedResult.of(apps, page.getNumber(), page.getSize(), page.getTotalElements());
  }

  private Specification<ClientAppEntity> buildSpecification(TenantId tenantId, ClientAppFilter filter) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();

      // Filter by tenantId (scope)
      predicates.add(cb.equal(root.get("tenant").get("id"), tenantId.value()));

      // Filter by status
      if (filter.hasStatus()) {
        predicates.add(cb.equal(root.get("status"), filter.getStatus()));
      }

      // Filter by name (case-insensitive LIKE)
      if (filter.hasNameLike()) {
        predicates.add(cb.like(
            cb.lower(root.get("name")),
            "%" + filter.getNameLike().toLowerCase() + "%"
        ));
      }

      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }

  private PageRequest buildPageRequest(ClientAppFilter filter) {
    if (filter.hasSorting()) {
      Sort.Direction direction = Sort.Direction.fromString(filter.getSortOrder());
      return PageRequest.of(filter.getPage(), filter.getSize(), Sort.by(direction, filter.getSortBy()));
    }
    return PageRequest.of(filter.getPage(), filter.getSize());
  }
}
