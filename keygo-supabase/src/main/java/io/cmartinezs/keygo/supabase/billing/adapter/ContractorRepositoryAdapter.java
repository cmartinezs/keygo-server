package io.cmartinezs.keygo.supabase.billing.adapter;

import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contractor.model.Contractor;
import io.cmartinezs.keygo.supabase.billing.entity.ContractorEntity;
import io.cmartinezs.keygo.supabase.billing.repository.ContractorJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.PlatformUserJpaRepository;
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
  private final PlatformUserJpaRepository platformUserRepo;

  public ContractorRepositoryAdapter(
      ContractorJpaRepository jpaRepo,
      PlatformUserJpaRepository platformUserRepo) {
    this.jpaRepo = jpaRepo;
    this.platformUserRepo = platformUserRepo;
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
  public Optional<Contractor> findByPlatformUserId(UUID platformUserId) {
    return jpaRepo.findByAssociatedPlatformUserId(platformUserId).map(this::toDomain);
  }

  @Override
  public Optional<Contractor> findByPlatformUserEmail(String email) {
    return jpaRepo.findByAssociatedPlatformUserEmail(email)
        .map(this::toDomain);
  }

  private ContractorEntity toEntity(Contractor c) {
    return ContractorEntity.builder()
        .id(c.getId())
        .primaryContactPlatformUser(
            c.getPrimaryContactPlatformUserId() != null
                ? platformUserRepo.getReferenceById(c.getPrimaryContactPlatformUserId())
                : null)
        .type(c.getType())
        .displayName(c.getDisplayName())
        .legalName(c.getLegalName())
        .taxId(c.getTaxId())
        .billingEmail(c.getBillingEmail())
        .status(c.getStatus())
        .build();
  }

  private Contractor toDomain(ContractorEntity e) {
    return Contractor.builder()
        .id(e.getId())
        .primaryContactPlatformUserId(
            e.getPrimaryContactPlatformUser() != null ? e.getPrimaryContactPlatformUser().getId() : null)
        .type(e.getType())
        .displayName(e.getDisplayName())
        .legalName(e.getLegalName())
        .taxId(e.getTaxId())
        .billingEmail(e.getBillingEmail())
        .status(e.getStatus())
        .createdAt(e.getCreatedAt())
        .updatedAt(e.getUpdatedAt())
        .build();
  }
}

