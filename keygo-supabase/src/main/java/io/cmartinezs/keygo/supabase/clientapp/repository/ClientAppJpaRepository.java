package io.cmartinezs.keygo.supabase.clientapp.repository;

import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for ClientAppEntity.
 * <p>Repositorio Spring Data JPA para ClientAppEntity.
 * @author cmartinezs
 * @version 1.0
 */
public interface ClientAppJpaRepository extends JpaRepository<ClientAppEntity, UUID> {

  Optional<ClientAppEntity> findByClientId(String clientId);

  Optional<ClientAppEntity> findByClientIdAndTenantId(String clientId, UUID tenantId);

  List<ClientAppEntity> findAllByTenantId(UUID tenantId);

  boolean existsByClientId(String clientId);
}

