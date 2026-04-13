package io.cmartinezs.keygo.domain.billing.usage.model;

import io.cmartinezs.keygo.domain.billing.catalog.model.EnforcementMode;
import io.cmartinezs.keygo.domain.billing.catalog.model.MetricType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Result of checking an entitlement for a specific metric.
 * Used internally by CheckAppEntitlementUseCase.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class EntitlementCheck {

  private final String metricCode;
  private final MetricType metricType;
  private final EnforcementMode enforcementMode;
  /** Current usage value. */
  private final BigDecimal currentValue;
  /** Maximum allowed value. Null = unlimited. */
  private final BigDecimal limitValue;
  /** Whether the operation is allowed. */
  private final boolean allowed;

  /**
   * Factory: unlimited access (no subscription or entitlement found).
   * Backward compatible: existing apps without billing are unrestricted.
   */
  public static EntitlementCheck unlimited(String metricCode) {
    return EntitlementCheck.builder()
        .metricCode(metricCode)
        .metricType(MetricType.QUOTA)
        .enforcementMode(EnforcementMode.SOFT)
        .currentValue(BigDecimal.ZERO)
        .limitValue(null)
        .allowed(true)
        .build();
  }
}

