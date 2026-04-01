package io.cmartinezs.keygo.app.billing.subscription.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;

/**
 * Thrown when a subscription cannot be found for the given criteria.
 */
public class SubscriptionNotFoundException extends UseCaseException {

  public SubscriptionNotFoundException(String field, String value) {
    super("Subscription not found by %s: %s".formatted(field, value));
  }
}
