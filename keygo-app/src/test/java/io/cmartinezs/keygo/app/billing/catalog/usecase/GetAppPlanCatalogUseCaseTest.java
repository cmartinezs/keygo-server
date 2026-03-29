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
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;
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

  private AppPlan tenantPlan(UUID appId) {
    return AppPlan.builder()
        .id(UUID.randomUUID()).clientAppId(appId).code("STARTER").name("Starter")
        .subscriberType(SubscriberType.TENANT).status(AppPlanStatus.ACTIVE).isPublic(true).build();
  }

  private AppPlan userPlan(UUID appId) {
    return AppPlan.builder()
        .id(UUID.randomUUID()).clientAppId(appId).code("TEACHER_PRO").name("Teacher Pro")
        .subscriberType(SubscriberType.TENANT_USER).status(AppPlanStatus.ACTIVE).isPublic(true).build();
  }

  private AppPlanVersion activeVersion(UUID planId) {
    return AppPlanVersion.builder()
        .id(UUID.randomUUID()).appPlanId(planId).version("1.0")
        .currency("MXN").billingPeriod(BillingPeriod.MONTHLY)
        .basePrice(BigDecimal.ZERO).setupFee(BigDecimal.ZERO)
        .trialDays(0).effectiveFrom(LocalDate.now())
        .status(AppPlanVersionStatus.ACTIVE).build();
  }

  @Test
  void execute_withoutFilter_returnsAllPublicActivePlans() {
    // Given
    UUID appId = UUID.randomUUID();
    AppPlan t = tenantPlan(appId);
    AppPlan u = userPlan(appId);
    when(planRepo.findPublicByClientAppId(appId)).thenReturn(List.of(t, u));
    when(versionRepo.findActiveByAppPlanId(any())).thenReturn(List.of(activeVersion(UUID.randomUUID())));
    when(entitlementRepo.findByAppPlanVersionId(any())).thenReturn(List.of());
    // When
    List<AppPlanResult> results = useCase.execute(appId, null);
    // Then
    assertThat(results).hasSize(2);
  }

  @Test
  void execute_withTenantFilter_returnsOnlyTenantPlans() {
    // Given
    UUID appId = UUID.randomUUID();
    AppPlan t = tenantPlan(appId);
    when(planRepo.findPublicByClientAppIdAndSubscriberType(appId, SubscriberType.TENANT))
        .thenReturn(List.of(t));
    when(versionRepo.findActiveByAppPlanId(any())).thenReturn(List.of(activeVersion(UUID.randomUUID())));
    when(entitlementRepo.findByAppPlanVersionId(any())).thenReturn(List.of());
    // When
    List<AppPlanResult> results = useCase.execute(appId, SubscriberType.TENANT);
    // Then
    assertThat(results).hasSize(1);
    assertThat(results.get(0).plan().getSubscriberType()).isEqualTo(SubscriberType.TENANT);
  }

  @Test
  void execute_excludesInactivePlans() {
    // Given
    UUID appId = UUID.randomUUID();
    AppPlan inactive = AppPlan.builder()
        .id(UUID.randomUUID()).clientAppId(appId).code("OLD").name("Old")
        .subscriberType(SubscriberType.TENANT).status(AppPlanStatus.INACTIVE).isPublic(true).build();
    when(planRepo.findPublicByClientAppId(appId)).thenReturn(List.of(inactive));
    // When
    List<AppPlanResult> results = useCase.execute(appId, null);
    // Then
    assertThat(results).isEmpty();
    verifyNoInteractions(versionRepo);
  }
}

