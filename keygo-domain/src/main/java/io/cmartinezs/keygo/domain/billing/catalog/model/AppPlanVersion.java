package io.cmartinezs.keygo.domain.billing.catalog.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain model for a versioned plan definition.
 * Subscriptions reference a specific version, freezing the pricing.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class AppPlanVersion {

  private final UUID id;
  private final UUID appPlanId;
  private final String version;
  private final String currency;
  private final BillingPeriod billingPeriod;
  private final BigDecimal basePrice;
  private final BigDecimal setupFee;
  private final int trialDays;
  private final LocalDate effectiveFrom;
  private final LocalDate effectiveTo;
  private AppPlanVersionStatus status;

  public boolean isActive() {
    return AppPlanVersionStatus.ACTIVE.equals(this.status);
  }

  public void deprecate() {
    this.status = AppPlanVersionStatus.DEPRECATED;
  }
}

