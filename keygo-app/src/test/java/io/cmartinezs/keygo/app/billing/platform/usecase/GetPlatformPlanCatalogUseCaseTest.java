package io.cmartinezs.keygo.app.billing.platform.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanStatus;
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

  private GetPlatformPlanCatalogUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetPlatformPlanCatalogUseCase(planRepo);
  }

  @Test
  @DisplayName("Debe retornar planes de plataforma ordenados por sortOrder ascendente")
  void execute_returnsPlatformPlansSortedBySortOrder() {
    // Given
    AppPlan teamPlan = AppPlan.builder()
        .id(UUID.randomUUID()).code("TEAM").name("Team")
        .status(AppPlanStatus.ACTIVE).isPublic(true).sortOrder(3).build();
    AppPlan freePlan = AppPlan.builder()
        .id(UUID.randomUUID()).code("FREE").name("Free")
        .status(AppPlanStatus.ACTIVE).isPublic(true).sortOrder(1).build();
    AppPlan personalPlan = AppPlan.builder()
        .id(UUID.randomUUID()).code("PERSONAL").name("Personal")
        .status(AppPlanStatus.ACTIVE).isPublic(true).sortOrder(2).build();

    when(planRepo.findPlatformPlans()).thenReturn(List.of(teamPlan, freePlan, personalPlan));

    // When
    List<AppPlan> result = useCase.execute();

    // Then
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getCode()).isEqualTo("FREE");
    assertThat(result.get(1).getCode()).isEqualTo("PERSONAL");
    assertThat(result.get(2).getCode()).isEqualTo("TEAM");
    verify(planRepo).findPlatformPlans();
  }

  @Test
  @DisplayName("Debe retornar lista vacía si no hay planes de plataforma")
  void execute_returnsEmptyListWhenNoPlatformPlans() {
    // Given
    when(planRepo.findPlatformPlans()).thenReturn(List.of());

    // When
    List<AppPlan> result = useCase.execute();

    // Then
    assertThat(result).isEmpty();
    verify(planRepo).findPlatformPlans();
  }
}
