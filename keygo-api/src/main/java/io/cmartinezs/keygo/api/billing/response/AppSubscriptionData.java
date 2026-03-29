package io.cmartinezs.keygo.api.billing.response;

import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response data for app subscription endpoints.
 */
public record AppSubscriptionData(
    UUID id,
    UUID clientAppId,
    UUID appPlanVersionId,
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

  /** Solo para referencia de schema OpenAPI — no instanciar en lógica de negocio. */
  public static final class Response extends BaseResponse<AppSubscriptionData> {}
}
