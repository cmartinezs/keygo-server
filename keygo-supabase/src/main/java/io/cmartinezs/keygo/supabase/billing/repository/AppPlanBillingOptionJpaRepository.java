package io.cmartinezs.keygo.supabase.billing.repository;

import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;
import io.cmartinezs.keygo.supabase.billing.entity.AppPlanBillingOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppPlanBillingOptionJpaRepository extends JpaRepository<AppPlanBillingOptionEntity, UUID> {
  List<AppPlanBillingOptionEntity> findByAppPlanVersionId(UUID appPlanVersionId);
  Optional<AppPlanBillingOptionEntity> findByAppPlanVersionIdAndBillingPeriod(UUID appPlanVersionId, BillingPeriod billingPeriod);
}



