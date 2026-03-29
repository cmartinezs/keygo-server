package io.cmartinezs.keygo.api.billing.response;

import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response data for app subscription endpoints.
 */
public record AppSubscriptionData(
    UUID id,
    UUID clientAppId,
    UUID appPlanVersionId,
    SubscriberType subscriberType,
    UUID subscriberTenantId,
    UUID subscriberTenantUserId,
    String status,
    OffsetDateTime currentPeriodStart,
    OffsetDateTime currentPeriodEnd,
    boolean cancelAtPeriodEnd,
    OffsetDateTime nextBillingAt,
    boolean autoRenew,
    OffsetDateTime createdAt
) {
  public static AppSubscriptionData from(AppSubscription s) {
    return new AppSubscriptionData(
        s.getId(),
        s.getClientAppId(),
        s.getAppPlanVersionId(),
        s.getSubscriberType(),
        s.getSubscriberTenantId(),
        s.getSubscriberTenantUserId(),
        s.getStatus().name(),
        s.getCurrentPeriodStart(),
        s.getCurrentPeriodEnd(),
        s.isCancelAtPeriodEnd(),
        s.getNextBillingAt(),
        s.isAutoRenew(),
        s.getCreatedAt()
    );
  }
}

