package io.cmartinezs.keygo.supabase.billing.repository;

import io.cmartinezs.keygo.supabase.billing.entity.AppContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppContractJpaRepository extends JpaRepository<AppContractEntity, UUID> {
  Optional<AppContractEntity> findByClientAppIdAndContractorEmail(UUID clientAppId, String email);
  List<AppContractEntity> findByContractorId(UUID contractorId);
}
