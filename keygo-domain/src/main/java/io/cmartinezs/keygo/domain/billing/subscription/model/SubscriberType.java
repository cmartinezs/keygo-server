package io.cmartinezs.keygo.domain.billing.subscription.model;

/**
 * Identifies who the billing subscriber is.
 * TENANT = a whole organization (B2B model).
 * TENANT_USER = an individual user (B2C model).
 * @author cmartinezs
 * @version 1.0
 */
public enum SubscriberType {
  /** The subscriber is the platform itself (platform-level plans/subscriptions). */
  PLATFORM,
  /** The subscriber is a Tenant (organization). */
  TENANT,
  /** The subscriber is an individual TenantUser. */
  TENANT_USER
}

