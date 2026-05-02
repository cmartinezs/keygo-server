package io.cmartinezs.keygo.app.billing.catalog.usecase;

import io.cmartinezs.keygo.app.billing.catalog.command.CreateAppPlanCommand;
import io.cmartinezs.keygo.app.billing.catalog.exception.DuplicatePlanCodeException;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanBillingOptionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.result.AppPlanResult;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanStatus;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersionStatus;
import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;
import io.cmartinezs.keygo.domain.billing.catalog.model.EnforcementMode;
import io.cmartinezs.keygo.domain.billing.catalog.model.MetricType;
import io.cmartinezs.keygo.domain.billing.catalog.model.PeriodType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateAppPlanUseCaseTest {

  @Mock AppPlanRepositoryPort planRepo;
  @Mock AppPlanVersionRepositoryPort versionRepo;
  @Mock AppPlanBillingOptionRepositoryPort billingOptionRepo;
  @Mock AppPlanEntitlementRepositoryPort entitlementRepo;

  @InjectMocks
  CreateAppPlanUseCase useCase;

  /** Helper — command with one MONTHLY billing option and two entitlements. */
  private CreateAppPlanCommand validCommand(UUID appId) {
    return new CreateAppPlanCommand(
        appId,
        "STARTER",
        "Starter Plan",
        "Plan básico para startups",
        true,
        1,   // sortOrder
        "1.0",
        "MXN",
        14,
        LocalDate.of(2026, 1, 1),
        List.of(
            new CreateAppPlanCommand.BillingOptionDef(
                BillingPeriod.MONTHLY, new BigDecimal("299.00"), BigDecimal.ZERO, true),
            new CreateAppPlanCommand.BillingOptionDef(
                BillingPeriod.YEARLY, new BigDecimal("2990.00"), new BigDecimal("16.62"), false)
        ),
        List.of(
            new CreateAppPlanCommand.EntitlementDef(
                "MAX_USERS", MetricType.QUOTA, new BigDecimal("25.0000"),
                PeriodType.NONE, EnforcementMode.HARD, true),
            new CreateAppPlanCommand.EntitlementDef(
                "SOCIAL_LOGIN", MetricType.BOOLEAN, null, PeriodType.NONE, EnforcementMode.HARD, true)
        )
    );
  }

  private AppPlan savedPlan(UUID appId, String code) {
    return AppPlan.builder()
        .id(UUID.randomUUID())
        .clientAppId(appId)
        .code(code)
        .name("Starter Plan")
        .status(AppPlanStatus.ACTIVE)
        .isPublic(true)
        .sortOrder(1)
        .build();
  }

  private AppPlanVersion savedVersion(UUID planId) {
    return AppPlanVersion.builder()
        .id(UUID.randomUUID())
        .appPlanId(planId)
        .version("1.0")
        .currency("MXN")
        .setupFee(BigDecimal.ZERO)
        .trialDays(14)
        .effectiveFrom(LocalDate.of(2026, 1, 1))
        .status(AppPlanVersionStatus.ACTIVE)
        .build();
  }

  @Test
  void execute_happyPath_createsPlanVersionAndEntitlements() {
    // Given
    UUID appId = UUID.randomUUID();
    CreateAppPlanCommand cmd = validCommand(appId);
    AppPlan plan = savedPlan(appId, "STARTER");
    AppPlanVersion version = savedVersion(plan.getId());

    when(planRepo.existsByClientAppIdAndCode(appId, "STARTER")).thenReturn(false);
    when(planRepo.save(any())).thenReturn(plan);
    when(versionRepo.save(any())).thenReturn(version);

    // When
    AppPlanResult result = useCase.execute(cmd);

    // Then
    assertThat(result.plan()).isNotNull();
    assertThat(result.plan().getCode()).isEqualTo("STARTER");
    assertThat(result.plan().isActive()).isTrue();
    assertThat(result.plan().getSortOrder()).isEqualTo(1);
    assertThat(result.versions()).hasSize(1);
    assertThat(result.versions().get(0).getVersion()).isEqualTo("1.0");
    assertThat(result.entitlements()).hasSize(2);

    verify(planRepo).save(any());
    verify(versionRepo).save(any());
    verify(billingOptionRepo).saveAll(any());
    verify(entitlementRepo).saveAll(any());
  }

  @Test
  void execute_freePlan_skipsBillingOptionSave() {
    // Given — empty billingOptions list = free plan
    UUID appId = UUID.randomUUID();
    CreateAppPlanCommand cmd = new CreateAppPlanCommand(
        appId, "FREE", "Free Plan", null, true,
        0, "1.0", "USD",
        0, LocalDate.now(), List.of(), List.of());

    AppPlan plan = savedPlan(appId, "FREE");
    AppPlanVersion version = savedVersion(plan.getId());

    when(planRepo.existsByClientAppIdAndCode(appId, "FREE")).thenReturn(false);
    when(planRepo.save(any())).thenReturn(plan);
    when(versionRepo.save(any())).thenReturn(version);

    // When
    AppPlanResult result = useCase.execute(cmd);

    // Then — no billing options saved, plan is effectively free
    assertThat(result.billingOptionsByVersion().get(version.getId())).isEmpty();
    verify(billingOptionRepo, never()).saveAll(any());
  }

  @Test
  void execute_planWithNoEntitlements_skipsEntitlementSave() {
    // Given
    UUID appId = UUID.randomUUID();
    CreateAppPlanCommand cmd = new CreateAppPlanCommand(
        appId, "FREE", "Free Plan", null, true,
        0, "1.0", "USD",
        0, LocalDate.now(), List.of(), List.of());

    AppPlan plan = savedPlan(appId, "FREE");
    AppPlanVersion version = savedVersion(plan.getId());

    when(planRepo.existsByClientAppIdAndCode(appId, "FREE")).thenReturn(false);
    when(planRepo.save(any())).thenReturn(plan);
    when(versionRepo.save(any())).thenReturn(version);

    // When
    AppPlanResult result = useCase.execute(cmd);

    // Then
    assertThat(result.entitlements()).isEmpty();
    verify(entitlementRepo, never()).saveAll(any());
  }

  @Test
  void execute_duplicateCode_throwsIllegalArgument() {
    // Given
    UUID appId = UUID.randomUUID();
    CreateAppPlanCommand cmd = validCommand(appId);
    when(planRepo.existsByClientAppIdAndCode(appId, "STARTER")).thenReturn(true);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(cmd))
        .isInstanceOf(DuplicatePlanCodeException.class)
        .hasMessageContaining("STARTER");

    verify(planRepo, never()).save(any());
    verify(versionRepo, never()).save(any());
    verifyNoInteractions(billingOptionRepo, entitlementRepo);
  }

  @Test
  void execute_planSavedWithCorrectStatus() {
    // Given
    UUID appId = UUID.randomUUID();
    CreateAppPlanCommand cmd = validCommand(appId);
    AppPlan plan = savedPlan(appId, "STARTER");
    AppPlanVersion version = savedVersion(plan.getId());

    when(planRepo.existsByClientAppIdAndCode(appId, "STARTER")).thenReturn(false);
    when(planRepo.save(any())).thenReturn(plan);
    when(versionRepo.save(any())).thenReturn(version);

    ArgumentCaptor<AppPlan> planCaptor = ArgumentCaptor.forClass(AppPlan.class);
    ArgumentCaptor<AppPlanVersion> versionCaptor = ArgumentCaptor.forClass(AppPlanVersion.class);

    // When
    useCase.execute(cmd);

    // Then
    verify(planRepo).save(planCaptor.capture());
    verify(versionRepo).save(versionCaptor.capture());

    assertThat(planCaptor.getValue().getStatus()).isEqualTo(AppPlanStatus.ACTIVE);
    assertThat(planCaptor.getValue().isPublic()).isTrue();
    assertThat(planCaptor.getValue().getSortOrder()).isEqualTo(1);
    assertThat(versionCaptor.getValue().getCurrency()).isEqualTo("MXN");
    assertThat(versionCaptor.getValue().getTrialDays()).isEqualTo(14);
  }

  @Test
  void execute_defaultCurrencyUSD_whenCurrencyIsNull() {
    // Given
    UUID appId = UUID.randomUUID();
    CreateAppPlanCommand cmd = new CreateAppPlanCommand(
        appId, "BASIC", "Basic", null, true,
        0, "1.0",
        null, // null currency → should default to USD
        0, LocalDate.now(), List.of(), List.of());

    AppPlan plan = savedPlan(appId, "BASIC");
    AppPlanVersion version = savedVersion(plan.getId());

    when(planRepo.existsByClientAppIdAndCode(appId, "BASIC")).thenReturn(false);
    when(planRepo.save(any())).thenReturn(plan);
    when(versionRepo.save(any())).thenReturn(version);

    ArgumentCaptor<AppPlanVersion> versionCaptor = ArgumentCaptor.forClass(AppPlanVersion.class);

    // When
    useCase.execute(cmd);

    // Then — default currency is USD
    verify(versionRepo).save(versionCaptor.capture());
    assertThat(versionCaptor.getValue().getCurrency()).isEqualTo("USD");
  }
}

