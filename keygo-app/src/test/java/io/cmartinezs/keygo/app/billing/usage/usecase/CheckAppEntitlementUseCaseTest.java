package io.cmartinezs.keygo.app.billing.usage.usecase;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.app.billing.usage.port.UsageCounterRepositoryPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanEntitlement;
import io.cmartinezs.keygo.domain.billing.catalog.model.EnforcementMode;
import io.cmartinezs.keygo.domain.billing.catalog.model.MetricType;
import io.cmartinezs.keygo.domain.billing.catalog.model.PeriodType;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriptionStatus;
import io.cmartinezs.keygo.domain.billing.usage.model.EntitlementCheck;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckAppEntitlementUseCaseTest {

  @Mock AppSubscriptionRepositoryPort subscriptionRepo;
  @Mock AppPlanVersionRepositoryPort versionRepo;
  @Mock AppPlanEntitlementRepositoryPort entitlementRepo;
  @Mock UsageCounterRepositoryPort usageRepo;

  @InjectMocks
  CheckAppEntitlementUseCase useCase;

  private AppSubscription activeSubscription(UUID appId, UUID planVersionId, UUID tenantId) {
    return AppSubscription.builder()
        .id(UUID.randomUUID())
        .clientAppId(appId)
        .appPlanVersionId(planVersionId)
        .subscriberTenantId(tenantId)
        .status(SubscriptionStatus.ACTIVE)
        .currentPeriodStart(OffsetDateTime.now().minusDays(1))
        .currentPeriodEnd(OffsetDateTime.now().plusMonths(1))
        .autoRenew(true)
        .build();
  }

  private AppPlanEntitlement quotaEntitlement(UUID versionId, long limit, EnforcementMode mode) {
    return AppPlanEntitlement.builder()
        .id(UUID.randomUUID())
        .appPlanVersionId(versionId)
        .metricCode("MAX_USERS")
        .metricType(MetricType.QUOTA)
        .limitValue(limit)
        .periodType(PeriodType.NONE)
        .enforcementMode(mode)
        .isEnabled(true)
        .build();
  }

  @Test
  void noSubscription_returnsUnlimited() {
    // Given
    UUID appId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    when(subscriptionRepo.findByClientAppIdAndSubscriberTenantId(appId, tenantId))
        .thenReturn(Optional.empty());
    // When
    EntitlementCheck result = useCase.executeForTenant(appId, tenantId, "MAX_USERS");
    // Then
    assertThat(result.isAllowed()).isTrue();
    assertThat(result.getLimitValue()).isNull();
  }

  @Test
  void hardLimit_underLimit_returnsAllowed() {
    // Given
    UUID appId = UUID.randomUUID();
    UUID planVersionId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    AppSubscription sub = activeSubscription(appId, planVersionId, tenantId);
    AppPlanEntitlement ent = quotaEntitlement(planVersionId, 10L, EnforcementMode.HARD);

    when(subscriptionRepo.findByClientAppIdAndSubscriberTenantId(any(), any()))
        .thenReturn(Optional.of(sub));
    when(entitlementRepo.findByAppPlanVersionId(planVersionId)).thenReturn(List.of(ent));
    when(usageRepo.getCurrentUsageForTenant(any(), any())).thenReturn(Map.of("MAX_USERS", 5L));
    // When
    EntitlementCheck result = useCase.executeForTenant(appId, tenantId, "MAX_USERS");
    // Then
    assertThat(result.isAllowed()).isTrue();
    assertThat(result.getCurrentValue()).isEqualTo(5L);
    assertThat(result.getLimitValue()).isEqualTo(10L);
  }

  @Test
  void hardLimit_atLimit_returnsBlocked() {
    // Given
    UUID appId = UUID.randomUUID();
    UUID planVersionId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    AppSubscription sub = activeSubscription(appId, planVersionId, tenantId);
    AppPlanEntitlement ent = quotaEntitlement(planVersionId, 5L, EnforcementMode.HARD);

    when(subscriptionRepo.findByClientAppIdAndSubscriberTenantId(any(), any()))
        .thenReturn(Optional.of(sub));
    when(entitlementRepo.findByAppPlanVersionId(planVersionId)).thenReturn(List.of(ent));
    when(usageRepo.getCurrentUsageForTenant(any(), any())).thenReturn(Map.of("MAX_USERS", 5L));
    // When
    EntitlementCheck result = useCase.executeForTenant(appId, tenantId, "MAX_USERS");
    // Then
    assertThat(result.isAllowed()).isFalse();
  }

  @Test
  void softLimit_atLimit_returnsAllowed() {
    // Given
    UUID appId = UUID.randomUUID();
    UUID planVersionId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    AppSubscription sub = activeSubscription(appId, planVersionId, tenantId);
    AppPlanEntitlement ent = quotaEntitlement(planVersionId, 5L, EnforcementMode.SOFT);

    when(subscriptionRepo.findByClientAppIdAndSubscriberTenantId(any(), any()))
        .thenReturn(Optional.of(sub));
    when(entitlementRepo.findByAppPlanVersionId(planVersionId)).thenReturn(List.of(ent));
    when(usageRepo.getCurrentUsageForTenant(any(), any())).thenReturn(Map.of("MAX_USERS", 5L));
    // When
    EntitlementCheck result = useCase.executeForTenant(appId, tenantId, "MAX_USERS");
    // Then
    assertThat(result.isAllowed()).isTrue();
  }

  @Test
  void booleanEntitlement_disabled_returnsBlocked() {
    // Given
    UUID appId = UUID.randomUUID();
    UUID planVersionId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    AppSubscription sub = activeSubscription(appId, planVersionId, tenantId);
    AppPlanEntitlement ent = AppPlanEntitlement.builder()
        .id(UUID.randomUUID())
        .appPlanVersionId(planVersionId)
        .metricCode("EXPORT_PDF")
        .metricType(MetricType.BOOLEAN)
        .periodType(PeriodType.NONE)
        .enforcementMode(EnforcementMode.HARD)
        .isEnabled(false)
        .build();

    when(subscriptionRepo.findByClientAppIdAndSubscriberTenantId(any(), any()))
        .thenReturn(Optional.of(sub));
    when(entitlementRepo.findByAppPlanVersionId(planVersionId)).thenReturn(List.of(ent));
    // When
    EntitlementCheck result = useCase.executeForTenant(appId, tenantId, "EXPORT_PDF");
    // Then
    assertThat(result.isAllowed()).isFalse();
  }

  @Test
  void noEntitlementForMetric_returnsUnlimited() {
    // Given
    UUID appId = UUID.randomUUID();
    UUID planVersionId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    AppSubscription sub = activeSubscription(appId, planVersionId, tenantId);

    when(subscriptionRepo.findByClientAppIdAndSubscriberTenantId(any(), any()))
        .thenReturn(Optional.of(sub));
    when(entitlementRepo.findByAppPlanVersionId(planVersionId)).thenReturn(List.of());
    // When
    EntitlementCheck result = useCase.executeForTenant(appId, tenantId, "UNKNOWN_METRIC");
    // Then
    assertThat(result.isAllowed()).isTrue();
    assertThat(result.getLimitValue()).isNull();
  }
}

