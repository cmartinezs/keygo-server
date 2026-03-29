package io.cmartinezs.keygo.domain.billing.catalog.model;

import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;
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
        .subscriberType(SubscriberType.TENANT)
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
        .code("X").name("X").subscriberType(SubscriberType.TENANT)
        .status(AppPlanStatus.ACTIVE).isPublic(true).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("clientAppId");
  }

  @Test
  void builder_throwsException_whenCodeIsBlank() {
    assertThatThrownBy(() -> AppPlan.builder()
        .clientAppId(UUID.randomUUID()).code("  ").name("X")
        .subscriberType(SubscriberType.TENANT).status(AppPlanStatus.ACTIVE).isPublic(true).build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("code");
  }

  @Test
  void subscriberType_isTenantUser_forB2CPlans() {
    // Given / When
    AppPlan plan = AppPlan.builder()
        .id(UUID.randomUUID())
        .clientAppId(UUID.randomUUID())
        .code("TEACHER_PRO")
        .name("Teacher Pro")
        .subscriberType(SubscriberType.TENANT_USER)
        .status(AppPlanStatus.ACTIVE)
        .isPublic(true)
        .build();
    // Then
    assertThat(plan.getSubscriberType()).isEqualTo(SubscriberType.TENANT_USER);
  }
}

