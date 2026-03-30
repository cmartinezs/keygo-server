package io.cmartinezs.keygo.supabase.billing.repository;

import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanStatus;
import io.cmartinezs.keygo.supabase.billing.entity.AppPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppPlanJpaRepository extends JpaRepository<AppPlanEntity, UUID> {
  List<AppPlanEntity> findByClientAppIdAndIsPublicTrueAndStatusOrderBySortOrderAsc(UUID clientAppId, AppPlanStatus status);
  List<AppPlanEntity> findByClientAppId(UUID clientAppId);
  Optional<AppPlanEntity> findByClientAppIdAndCode(UUID clientAppId, String code);
  boolean existsByClientAppIdAndCode(UUID clientAppId, String code);
}
