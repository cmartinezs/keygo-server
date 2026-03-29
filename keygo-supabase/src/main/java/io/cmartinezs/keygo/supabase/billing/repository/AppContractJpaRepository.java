package io.cmartinezs.keygo.supabase.billing.repository;

import io.cmartinezs.keygo.supabase.billing.entity.AppContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppContractJpaRepository extends JpaRepository<AppContractEntity, UUID> {
  Optional<AppContractEntity> findByClientAppIdAndCompanySlug(UUID clientAppId, String companySlug);
  Optional<AppContractEntity> findByClientAppIdAndContractorEmail(UUID clientAppId, String email);
  boolean existsByClientAppIdAndCompanySlug(UUID clientAppId, String companySlug);
}

