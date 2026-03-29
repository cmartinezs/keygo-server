package io.cmartinezs.keygo.domain.billing.catalog.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AppPlanTest {

  private AppPlan validPlan(AppPlanStatus status, boolean isPublic) {
    return AppPlan.builder()
        .id(UUID.randomUUID())
        .clientAppId(UUID.randomUUID())
        .code("STARTER")
        .name("Starter Plan")
        .status(status)
        .isPublic(isPublic)
        .build();
  }

  @Test
  void isActive_returnsTrue_whenStatusIsActive() {
    // Given / When
    AppPlan plan = validPlan(AppPlanStatus.ACTIVE, true);
    // Then
    assertThat(plan.isActive()).isTrue();
  }

  @Test
  void isActive_returnsFalse_whenStatusIsInactive() {
    // Given / When
    AppPlan plan = validPlan(AppPlanStatus.INACTIVE, true);
    // Then
    assertThat(plan.isActive()).isFalse();
  }

  @Test
  void deactivate_changesStatusToInactive() {
    // Given
    AppPlan plan = validPlan(AppPlanStatus.ACTIVE, true);
    // When
    plan.deactivate();
    // Then
    assertThat(plan.isActive()).isFalse();
    assertThat(plan.getStatus()).isEqualTo(AppPlanStatus.INACTIVE);
  }

  @Test
  void builder_throwsException_whenClientAppIdIsNull() {
    assertThatThrownBy(() -> AppPlan.builder()
        .code("X").name("X").status(AppPlanStatus.ACTIVE).isPublic(true).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("clientAppId");
  }

  @Test
  void builder_throwsException_whenCodeIsBlank() {
    assertThatThrownBy(() -> AppPlan.builder()
        .clientAppId(UUID.randomUUID()).code("  ").name("X")
        .status(AppPlanStatus.ACTIVE).isPublic(true).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("code");
  }

  @Test
  void builder_buildsSuccessfully_withAllRequiredFields() {
    // Given / When
    AppPlan plan = AppPlan.builder()
        .id(UUID.randomUUID())
        .clientAppId(UUID.randomUUID())
        .code("TEAM")
        .name("Team Plan")
        .status(AppPlanStatus.ACTIVE)
        .isPublic(true)
        .build();
    // Then
    assertThat(plan.getCode()).isEqualTo("TEAM");
    assertThat(plan.isActive()).isTrue();
  }
}
