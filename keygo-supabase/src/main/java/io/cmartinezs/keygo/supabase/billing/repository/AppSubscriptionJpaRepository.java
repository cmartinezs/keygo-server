package io.cmartinezs.keygo.supabase.billing.repository;

import io.cmartinezs.keygo.supabase.billing.entity.AppSubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppSubscriptionJpaRepository extends JpaRepository<AppSubscriptionEntity, UUID> {
  Optional<AppSubscriptionEntity> findByClientAppIdAndContractorId(UUID clientAppId, UUID contractorId);
}
