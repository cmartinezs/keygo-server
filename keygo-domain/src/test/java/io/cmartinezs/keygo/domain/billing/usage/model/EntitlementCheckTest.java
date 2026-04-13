package io.cmartinezs.keygo.domain.billing.usage.model;

import io.cmartinezs.keygo.domain.billing.catalog.model.EnforcementMode;
import io.cmartinezs.keygo.domain.billing.catalog.model.MetricType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EntitlementCheckTest {

  @Test
  void unlimited_returnsAllowedTrueWithNullLimit() {
    // Given / When
    EntitlementCheck check = EntitlementCheck.unlimited("MAX_USERS");
    // Then
    assertThat(check.isAllowed()).isTrue();
    assertThat(check.getLimitValue()).isNull();
    assertThat(check.getMetricCode()).isEqualTo("MAX_USERS");
  }

  @Test
  void hardLimit_blocked_whenAtLimit() {
    // Given / When
    EntitlementCheck check = EntitlementCheck.builder()
        .metricCode("MAX_USERS")
        .metricType(MetricType.QUOTA)
        .enforcementMode(EnforcementMode.HARD)
        .currentValue(new BigDecimal("5.0000"))
        .limitValue(new BigDecimal("5.0000"))
        .allowed(false)
        .build();
    // Then
    assertThat(check.isAllowed()).isFalse();
  }

  @Test
  void softLimit_allowed_evenAtLimit() {
    // Given / When
    EntitlementCheck check = EntitlementCheck.builder()
        .metricCode("MAX_USERS")
        .metricType(MetricType.QUOTA)
        .enforcementMode(EnforcementMode.SOFT)
        .currentValue(new BigDecimal("5.0000"))
        .limitValue(new BigDecimal("5.0000"))
        .allowed(true) // SOFT allows over-limit
        .build();
    // Then
    assertThat(check.isAllowed()).isTrue();
  }

  @Test
  void booleanFeature_disabled_returnsFalse() {
    // Given / When
    EntitlementCheck check = EntitlementCheck.builder()
        .metricCode("EXPORT_PDF")
        .metricType(MetricType.BOOLEAN)
        .enforcementMode(EnforcementMode.HARD)
        .currentValue(BigDecimal.ZERO)
        .limitValue(null)
        .allowed(false)
        .build();
    // Then
    assertThat(check.isAllowed()).isFalse();
  }
}

