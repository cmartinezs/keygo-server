package io.cmartinezs.keygo.app.billing.catalog.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.result.AppPlanResult;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanStatus;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersionStatus;
import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAppPlanCatalogUseCaseTest {

  @Mock AppPlanRepositoryPort planRepo;
  @Mock AppPlanVersionRepositoryPort versionRepo;
  @Mock AppPlanEntitlementRepositoryPort entitlementRepo;

  @InjectMocks
  GetAppPlanCatalogUseCase useCase;

  private AppPlan activePlan(UUID appId, String code) {
    return AppPlan.builder()
        .id(UUID.randomUUID()).clientAppId(appId).code(code).name(code)
        .status(AppPlanStatus.ACTIVE).isPublic(true).build();
  }

  private AppPlanVersion activeVersion(UUID planId) {
    return AppPlanVersion.builder()
        .id(UUID.randomUUID()).appPlanId(planId).version("1.0")
        .currency("USD").billingPeriod(BillingPeriod.MONTHLY)
        .basePrice(BigDecimal.ZERO).setupFee(BigDecimal.ZERO)
        .trialDays(0).effectiveFrom(LocalDate.now())
        .status(AppPlanVersionStatus.ACTIVE).build();
  }

  @Test
  void execute_returnsAllPublicActivePlans() {
    // Given
    UUID appId = UUID.randomUUID();
    AppPlan free = activePlan(appId, "FREE");
    AppPlan team = activePlan(appId, "TEAM");
    when(planRepo.findPublicByClientAppId(appId)).thenReturn(List.of(free, team));
    when(versionRepo.findActiveByAppPlanId(any())).thenReturn(List.of(activeVersion(UUID.randomUUID())));
    when(entitlementRepo.findByAppPlanVersionId(any())).thenReturn(List.of());
    // When
    List<AppPlanResult> results = useCase.execute(appId);
    // Then
    assertThat(results).hasSize(2);
  }

  @Test
  void execute_excludesInactivePlans() {
    // Given
    UUID appId = UUID.randomUUID();
    AppPlan inactive = AppPlan.builder()
        .id(UUID.randomUUID()).clientAppId(appId).code("OLD").name("Old")
        .status(AppPlanStatus.INACTIVE).isPublic(true).build();
    when(planRepo.findPublicByClientAppId(appId)).thenReturn(List.of(inactive));
    // When
    List<AppPlanResult> results = useCase.execute(appId);
    // Then
    assertThat(results).isEmpty();
    verifyNoInteractions(versionRepo);
  }
}
