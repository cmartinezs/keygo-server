package io.cmartinezs.keygo.domain.billing.catalog.model;

/**
 * Enforcement mode when an entitlement limit is reached.
 * @author cmartinezs
 * @version 1.0
 */
public enum EnforcementMode {
  /** Hard limit: the operation is blocked when the limit is reached. */
  HARD,
  /** Soft limit: the operation is allowed but an alert is generated. */
  SOFT
}

