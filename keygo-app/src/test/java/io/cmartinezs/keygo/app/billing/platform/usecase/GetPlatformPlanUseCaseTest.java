package io.cmartinezs.keygo.app.billing.platform.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.billing.catalog.port.AppPlanRepositoryPort;
import io.cmartinezs.keygo.app.billing.platform.exception.PlanNotFoundException;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlan;
import io.cmartinezs.keygo.domain.billing.catalog.model.AppPlanStatus;
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

  private GetPlatformPlanUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetPlatformPlanUseCase(planRepo);
  }

  @Test
  @DisplayName("Debe retornar plan de plataforma por código")
  void execute_returnsPlanByCode() {
    // Given
    String planCode = "PERSONAL";
    AppPlan plan = AppPlan.builder()
        .id(UUID.randomUUID()).code(planCode).name("Personal")
        .status(AppPlanStatus.ACTIVE).isPublic(true).sortOrder(2).build();
    when(planRepo.findPlatformPlanByCode(planCode)).thenReturn(Optional.of(plan));

    // When
    AppPlan result = useCase.execute(planCode);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getCode()).isEqualTo(planCode);
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
