package io.cmartinezs.keygo.supabase.tenant.adapter;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.supabase.tenant.mapper.TenantPersistenceMapper;
import io.cmartinezs.keygo.supabase.tenant.repository.TenantJpaRepository;
import org.springframework.stereotype.Repository;

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
}

