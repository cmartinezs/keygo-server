package io.cmartinezs.keygo.domain.billing.catalog.model;

/**
 * Type of an entitlement metric.
 * @author cmartinezs
 * @version 1.0
 */
public enum MetricType {
  /** A numeric quota (e.g. max 5 users). */
  QUOTA,
  /** A boolean feature flag (enabled/disabled). */
  BOOLEAN,
  /** A rate limit (e.g. 1000 calls per day). */
  RATE
}

