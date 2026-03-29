package io.cmartinezs.keygo.supabase.billing.repository;

import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersionStatus;
import io.cmartinezs.keygo.supabase.billing.entity.AppPlanVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppPlanVersionJpaRepository extends JpaRepository<AppPlanVersionEntity, UUID> {
  List<AppPlanVersionEntity> findByAppPlanIdAndStatus(UUID appPlanId, AppPlanVersionStatus status);
}

