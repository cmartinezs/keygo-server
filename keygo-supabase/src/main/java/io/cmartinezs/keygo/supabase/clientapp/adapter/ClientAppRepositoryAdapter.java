package io.cmartinezs.keygo.supabase.clientapp.adapter;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.clientapp.mapper.ClientAppPersistenceMapper;
import io.cmartinezs.keygo.supabase.clientapp.repository.ClientAppJpaRepository;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import org.springframework.stereotype.Repository;

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
  public Optional<ClientApp> findByClientIdAndTenantId(ClientId clientId, TenantId tenantId) {
    return jpaRepository
        .findByClientIdAndTenantId(clientId.value(), tenantId.value())
        .map(mapper::toDomain);
  }

  @Override
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
}
