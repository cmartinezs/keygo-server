package io.cmartinezs.keygo.domain.billing.catalog.model;

/**
 * Status of a billing plan in the catalog.
 * @author cmartinezs
 * @version 1.0
 */
public enum AppPlanStatus {
  /** Plan is active and can be contracted. */
  ACTIVE,
  /** Plan is inactive and not available for new contracts. */
  INACTIVE
}

