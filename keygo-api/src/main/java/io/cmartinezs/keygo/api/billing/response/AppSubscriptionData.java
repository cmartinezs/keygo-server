package io.cmartinezs.keygo.api.billing.response;

import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response data for app subscription endpoints — billing model v2 (contractor-centric).
 */
public record AppSubscriptionData(
    UUID id,
    UUID clientAppId,
    UUID appPlanVersionId,
    UUID contractorId,
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
        s.getContractorId(),
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
