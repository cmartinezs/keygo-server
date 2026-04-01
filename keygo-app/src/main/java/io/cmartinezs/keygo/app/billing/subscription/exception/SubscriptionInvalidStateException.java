package io.cmartinezs.keygo.app.billing.subscription.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;

/**
 * Thrown when a subscription operation cannot proceed because the subscription is not active.
 */
public class SubscriptionInvalidStateException extends UseCaseException {

  public SubscriptionInvalidStateException(String status) {
    super("Subscription is not active, cannot cancel. Current status: %s".formatted(status));
  }
}
