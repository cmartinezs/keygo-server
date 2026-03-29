package io.cmartinezs.keygo.domain.billing.subscription.model;

/**
 * Status of an app subscription.
 * @author cmartinezs
 * @version 1.0
 */
public enum SubscriptionStatus {
  PENDING,
  ACTIVE,
  PAST_DUE,
  SUSPENDED,
  CANCELLED,
  EXPIRED
}

