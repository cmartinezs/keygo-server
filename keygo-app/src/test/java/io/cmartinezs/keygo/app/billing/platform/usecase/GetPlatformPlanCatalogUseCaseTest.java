package io.cmartinezs.keygo.app.billing.platform.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanBillingOptionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanEntitlementRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanVersionRepositoryPort;
import io.cmartinezs.keygo.app.billing.catalog.result.AppPlanResult;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanStatus;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersion;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanVersionStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPlatformPlanCatalogUseCase")
class GetPlatformPlanCatalogUseCaseTest {

  @Mock private AppPlanRepositoryPort planRepo;
  @Mock private AppPlanVersionRepositoryPort versionRepo;
  @Mock private AppPlanBillingOptionRepositoryPort billingOptionRepo;
  @Mock private AppPlanEntitlementRepositoryPort entitlementRepo;

  private GetPlatformPlanCatalogUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetPlatformPlanCatalogUseCase(planRepo, versionRepo, billingOptionRepo, entitlementRepo);
  }

  @Test
  @DisplayName("Debe retornar planes de plataforma ordenados por sortOrder con versions y entitlements")
  void execute_returnsPlatformPlansSortedBySortOrder() {
    // Given
    var teamId = UUID.randomUUID();
    var freeId = UUID.randomUUID();
    var personalId = UUID.randomUUID();
    AppPlan teamPlan = AppPlan.builder()
        .id(teamId).code("TEAM").name("Team")
        .status(AppPlanStatus.ACTIVE).isPublic(true).sortOrder(3).build();
    AppPlan freePlan = AppPlan.builder()
        .id(freeId).code("FREE").name("Free")
        .status(AppPlanStatus.ACTIVE).isPublic(true).sortOrder(1).build();
    AppPlan personalPlan = AppPlan.builder()
        .id(personalId).code("PERSONAL").name("Personal")
        .status(AppPlanStatus.ACTIVE).isPublic(true).sortOrder(2).build();

    var versionId = UUID.randomUUID();
    AppPlanVersion version = AppPlanVersion.builder()
        .id(versionId).appPlanId(freeId).version("v1.0")
        .currency("USD").setupFee(BigDecimal.ZERO).trialDays(0)
        .effectiveFrom(LocalDate.now()).status(AppPlanVersionStatus.ACTIVE).build();

    when(planRepo.findPlatformPlans()).thenReturn(List.of(teamPlan, freePlan, personalPlan));
    when(versionRepo.findActiveByAppPlanId(any())).thenReturn(List.of(version));
    when(billingOptionRepo.findByAppPlanVersionId(versionId)).thenReturn(List.of());
    when(entitlementRepo.findByAppPlanVersionId(versionId)).thenReturn(List.of());

    // When
    List<AppPlanResult> result = useCase.execute();

    // Then
    assertThat(result).hasSize(3);
    assertThat(result.get(0).plan().getCode()).isEqualTo("FREE");
    assertThat(result.get(1).plan().getCode()).isEqualTo("PERSONAL");
    assertThat(result.get(2).plan().getCode()).isEqualTo("TEAM");
    assertThat(result.get(0).versions()).hasSize(1);
    verify(planRepo).findPlatformPlans();
  }

  @Test
  @DisplayName("Debe retornar lista vacía si no hay planes de plataforma")
  void execute_returnsEmptyListWhenNoPlatformPlans() {
    // Given
    when(planRepo.findPlatformPlans()).thenReturn(List.of());

    // When
    List<AppPlanResult> result = useCase.execute();

    // Then
    assertThat(result).isEmpty();
    verify(planRepo).findPlatformPlans();
  }
}
