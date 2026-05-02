package io.cmartinezs.keygo.domain.billing.catalog.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Domain model for a billing period option within a plan version.
 * <p>
 * A plan version may have zero or more billing options:
 * <ul>
 *   <li>Zero options → the plan is <strong>free</strong> (no payment required).</li>
 *   <li>One or more options → the subscriber must choose a billing period (e.g. MONTHLY, YEARLY).</li>
 * </ul>
 * The {@code discountPct} field is informational: it expresses the saving percentage
 * compared to paying the equivalent number of monthly periods.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class AppPlanBillingOption {

  private final UUID id;
  private final UUID appPlanVersionId;
  private final BillingPeriod billingPeriod;
  /** Price for the full billing period (e.g. annual total for YEARLY). */
  private final BigDecimal basePrice;
  /** Discount percentage vs equivalent monthly billing (0–100). Informational for UI. */
  private final BigDecimal discountPct;
  /** Whether this option is pre-selected in the UI. */
  private final boolean isDefault;
}

