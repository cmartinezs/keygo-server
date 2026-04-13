package io.cmartinezs.keygo.domain.billing.catalog.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain model for a single entitlement (limit or feature) within a plan version.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class AppPlanEntitlement {

  private final UUID id;
  private final UUID appPlanVersionId;
  private final String metricCode;
  private final MetricType metricType;
  /** Null means unlimited (only relevant for QUOTA and RATE types). */
  private final BigDecimal limitValue;
  private final PeriodType periodType;
  private final EnforcementMode enforcementMode;
  private final boolean isEnabled;
}

