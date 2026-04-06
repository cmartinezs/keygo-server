package io.cmartinezs.keygo.supabase.billing.adapter;

import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contractor.model.Contractor;
import io.cmartinezs.keygo.supabase.billing.entity.ContractorEntity;
import io.cmartinezs.keygo.supabase.billing.repository.ContractorJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: implements ContractorRepositoryPort using JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class ContractorRepositoryAdapter implements ContractorRepositoryPort {

  private final ContractorJpaRepository jpaRepo;
  private final TenantUserJpaRepository tenantUserRepo;

  public ContractorRepositoryAdapter(
      ContractorJpaRepository jpaRepo,
      TenantUserJpaRepository tenantUserRepo) {
    this.jpaRepo = jpaRepo;
    this.tenantUserRepo = tenantUserRepo;
  }

  @Override
  public Contractor save(Contractor contractor) {
    ContractorEntity entity = toEntity(contractor);
    return toDomain(jpaRepo.save(entity));
  }

  @Override
  public Optional<Contractor> findById(UUID id) {
    return jpaRepo.findById(id).map(this::toDomain);
  }

  @Override
  public Optional<Contractor> findByTenantUserId(UUID tenantUserId) {
    return jpaRepo.findByTenantUserId(tenantUserId).map(this::toDomain);
  }

  @Override
  public Optional<Contractor> findByTenantUserEmail(UUID tenantId, String email) {
    return jpaRepo.findByTenantUser_Tenant_IdAndTenantUser_Email(tenantId, email)
        .map(this::toDomain);
  }

  private ContractorEntity toEntity(Contractor c) {
    return ContractorEntity.builder()
        .tenantUser(tenantUserRepo.getReferenceById(c.getTenantUserId()))
        .status(c.getStatus())
        .build();
  }

  private Contractor toDomain(ContractorEntity e) {
    return Contractor.builder()
        .id(e.getId())
        .tenantUserId(e.getTenantUser().getId())
        .status(e.getStatus())
        .createdAt(e.getCreatedAt())
        .updatedAt(e.getUpdatedAt())
        .build();
  }
}

