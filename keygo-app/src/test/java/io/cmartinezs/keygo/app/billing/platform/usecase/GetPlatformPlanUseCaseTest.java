package io.cmartinezs.keygo.app.billing.platform.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanBillingOptionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.result.AppPlanResult;
import io.cmartinezs.keygo.app.billing.platform.exception.PlanNotFoundException;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanStatus;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersionStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPlatformPlanUseCase")
class GetPlatformPlanUseCaseTest {

  @Mock private AppPlanRepositoryPort planRepo;
  @Mock private AppPlanVersionRepositoryPort versionRepo;
  @Mock private AppPlanBillingOptionRepositoryPort billingOptionRepo;
  @Mock private AppPlanEntitlementRepositoryPort entitlementRepo;

  private GetPlatformPlanUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetPlatformPlanUseCase(planRepo, versionRepo, billingOptionRepo, entitlementRepo);
  }

  @Test
  @DisplayName("Debe retornar plan de plataforma por código con versions y entitlements")
  void execute_returnsPlanByCode() {
    // Given
    String planCode = "PERSONAL";
    var planId = UUID.randomUUID();
    var versionId = UUID.randomUUID();
    AppPlan plan = AppPlan.builder()
        .id(planId).code(planCode).name("Personal")
        .status(AppPlanStatus.ACTIVE).isPublic(true).sortOrder(2).build();
    AppPlanVersion version = AppPlanVersion.builder()
        .id(versionId).appPlanId(planId).version("v1.0")
        .currency("USD").setupFee(BigDecimal.ZERO).trialDays(0)
        .effectiveFrom(LocalDate.now()).status(AppPlanVersionStatus.ACTIVE).build();

    when(planRepo.findPlatformPlanByCode(planCode)).thenReturn(Optional.of(plan));
    when(versionRepo.findActiveByAppPlanId(planId)).thenReturn(List.of(version));
    when(billingOptionRepo.findByAppPlanVersionId(versionId)).thenReturn(List.of());
    when(entitlementRepo.findByAppPlanVersionId(versionId)).thenReturn(List.of());

    // When
    AppPlanResult result = useCase.execute(planCode);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.plan().getCode()).isEqualTo(planCode);
    assertThat(result.versions()).hasSize(1);
    verify(planRepo).findPlatformPlanByCode(planCode);
  }

  @Test
  @DisplayName("Debe lanzar PlanNotFoundException si el plan no existe")
  void execute_throwsWhenPlanNotFound() {
    // Given
    String planCode = "NONEXISTENT";
    when(planRepo.findPlatformPlanByCode(planCode)).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(planCode))
        .isInstanceOf(PlanNotFoundException.class)
        .hasMessageContaining(planCode);
    verify(planRepo).findPlatformPlanByCode(planCode);
  }
}
