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

import java.math.BigDecimal;
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

  /** Helper: builds an ACTIVE subscription for model v2 (contractor-centric). */
  private AppSubscription activeSubscription(UUID appId, UUID planVersionId, UUID contractorId) {
    return AppSubscription.builder()
        .id(UUID.randomUUID())
        .clientAppId(appId)
        .appPlanVersionId(planVersionId)
        .contractorId(contractorId)
        .status(SubscriptionStatus.ACTIVE)
        .currentPeriodStart(OffsetDateTime.now().minusDays(1))
        .currentPeriodEnd(OffsetDateTime.now().plusMonths(1))
        .autoRenew(true)
        .build();
  }

  private AppPlanEntitlement quotaEntitlement(UUID versionId, BigDecimal limit, EnforcementMode mode) {
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
    UUID contractorId = UUID.randomUUID();
    when(subscriptionRepo.findByClientAppIdAndContractorId(appId, contractorId))
        .thenReturn(Optional.empty());
    // When
    EntitlementCheck result = useCase.executeForContractor(appId, contractorId, "MAX_USERS");
    // Then
    assertThat(result.isAllowed()).isTrue();
    assertThat(result.getLimitValue()).isNull();
  }

  @Test
  void hardLimit_underLimit_returnsAllowed() {
    // Given
    UUID appId = UUID.randomUUID();
    UUID planVersionId = UUID.randomUUID();
    UUID contractorId = UUID.randomUUID();
    AppSubscription sub = activeSubscription(appId, planVersionId, contractorId);
    AppPlanEntitlement ent = quotaEntitlement(planVersionId, new BigDecimal("10.0000"), EnforcementMode.HARD);

    when(subscriptionRepo.findByClientAppIdAndContractorId(any(), any())).thenReturn(Optional.of(sub));
    when(entitlementRepo.findByAppPlanVersionId(planVersionId)).thenReturn(List.of(ent));
    when(usageRepo.getCurrentUsageForContractor(any(), any()))
        .thenReturn(Map.of("MAX_USERS", new BigDecimal("5.0000")));
    // When
    EntitlementCheck result = useCase.executeForContractor(appId, contractorId, "MAX_USERS");
    // Then
    assertThat(result.isAllowed()).isTrue();
    assertThat(result.getCurrentValue()).isEqualByComparingTo("5.0000");
    assertThat(result.getLimitValue()).isEqualByComparingTo("10.0000");
  }

  @Test
  void hardLimit_atLimit_returnsBlocked() {
    // Given
    UUID appId = UUID.randomUUID();
    UUID planVersionId = UUID.randomUUID();
    UUID contractorId = UUID.randomUUID();
    AppSubscription sub = activeSubscription(appId, planVersionId, contractorId);
    AppPlanEntitlement ent = quotaEntitlement(planVersionId, new BigDecimal("5.0000"), EnforcementMode.HARD);

    when(subscriptionRepo.findByClientAppIdAndContractorId(any(), any())).thenReturn(Optional.of(sub));
    when(entitlementRepo.findByAppPlanVersionId(planVersionId)).thenReturn(List.of(ent));
    when(usageRepo.getCurrentUsageForContractor(any(), any()))
        .thenReturn(Map.of("MAX_USERS", new BigDecimal("5.0000")));
    // When
    EntitlementCheck result = useCase.executeForContractor(appId, contractorId, "MAX_USERS");
    // Then
    assertThat(result.isAllowed()).isFalse();
  }

  @Test
  void softLimit_atLimit_returnsAllowed() {
    // Given
    UUID appId = UUID.randomUUID();
    UUID planVersionId = UUID.randomUUID();
    UUID contractorId = UUID.randomUUID();
    AppSubscription sub = activeSubscription(appId, planVersionId, contractorId);
    AppPlanEntitlement ent = quotaEntitlement(planVersionId, new BigDecimal("5.0000"), EnforcementMode.SOFT);

    when(subscriptionRepo.findByClientAppIdAndContractorId(any(), any())).thenReturn(Optional.of(sub));
    when(entitlementRepo.findByAppPlanVersionId(planVersionId)).thenReturn(List.of(ent));
    when(usageRepo.getCurrentUsageForContractor(any(), any()))
        .thenReturn(Map.of("MAX_USERS", new BigDecimal("5.0000")));
    // When
    EntitlementCheck result = useCase.executeForContractor(appId, contractorId, "MAX_USERS");
    // Then
    assertThat(result.isAllowed()).isTrue();
  }

  @Test
  void booleanEntitlement_disabled_returnsBlocked() {
    // Given
    UUID appId = UUID.randomUUID();
    UUID planVersionId = UUID.randomUUID();
    UUID contractorId = UUID.randomUUID();
    AppSubscription sub = activeSubscription(appId, planVersionId, contractorId);
    AppPlanEntitlement ent = AppPlanEntitlement.builder()
        .id(UUID.randomUUID())
        .appPlanVersionId(planVersionId)
        .metricCode("EXPORT_PDF")
        .metricType(MetricType.BOOLEAN)
        .periodType(PeriodType.NONE)
        .enforcementMode(EnforcementMode.HARD)
        .isEnabled(false)
        .build();

    when(subscriptionRepo.findByClientAppIdAndContractorId(any(), any())).thenReturn(Optional.of(sub));
    when(entitlementRepo.findByAppPlanVersionId(planVersionId)).thenReturn(List.of(ent));
    // When
    EntitlementCheck result = useCase.executeForContractor(appId, contractorId, "EXPORT_PDF");
    // Then
    assertThat(result.isAllowed()).isFalse();
  }

  @Test
  void noEntitlementForMetric_returnsUnlimited() {
    // Given
    UUID appId = UUID.randomUUID();
    UUID planVersionId = UUID.randomUUID();
    UUID contractorId = UUID.randomUUID();
    AppSubscription sub = activeSubscription(appId, planVersionId, contractorId);

    when(subscriptionRepo.findByClientAppIdAndContractorId(any(), any())).thenReturn(Optional.of(sub));
    when(entitlementRepo.findByAppPlanVersionId(planVersionId)).thenReturn(List.of());
    // When
    EntitlementCheck result = useCase.executeForContractor(appId, contractorId, "UNKNOWN_METRIC");
    // Then
    assertThat(result.isAllowed()).isTrue();
    assertThat(result.getLimitValue()).isNull();
  }
}

