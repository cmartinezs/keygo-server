package io.cmartinezs.keygo.supabase.billing.repository;

import io.cmartinezs.keygo.supabase.billing.entity.AppPlanEntitlementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppPlanEntitlementJpaRepository extends JpaRepository<AppPlanEntitlementEntity, UUID> {
  List<AppPlanEntitlementEntity> findByAppPlanVersionId(UUID appPlanVersionId);
}

