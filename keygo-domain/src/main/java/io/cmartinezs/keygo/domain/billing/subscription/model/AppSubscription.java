package io.cmartinezs.keygo.domain.billing.subscription.model;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain model for an active subscription between a ClientApp and its subscriber.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class AppSubscription {

  private final UUID id;
  private final UUID clientAppId;
  private final UUID appPlanVersionId;
  private final UUID contractId;
  private final SubscriberType subscriberType;
  /** Non-null only when subscriberType = TENANT. */
  private final UUID subscriberTenantId;
  /** Non-null only when subscriberType = TENANT_USER. */
  private final UUID subscriberTenantUserId;
  private SubscriptionStatus status;
  private final OffsetDateTime currentPeriodStart;
  private OffsetDateTime currentPeriodEnd;
  private boolean cancelAtPeriodEnd;
  private OffsetDateTime cancelledAt;
  private OffsetDateTime nextBillingAt;
  private final boolean autoRenew;
  private final OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  @Builder
  private AppSubscription(
      UUID id,
      UUID clientAppId,
      UUID appPlanVersionId,
      UUID contractId,
      SubscriberType subscriberType,
      UUID subscriberTenantId,
      UUID subscriberTenantUserId,
      SubscriptionStatus status,
      OffsetDateTime currentPeriodStart,
      OffsetDateTime currentPeriodEnd,
      boolean cancelAtPeriodEnd,
      OffsetDateTime cancelledAt,
      OffsetDateTime nextBillingAt,
      boolean autoRenew,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    if (clientAppId == null) throw new IllegalArgumentException("clientAppId cannot be null");
    if (appPlanVersionId == null) throw new IllegalArgumentException("appPlanVersionId cannot be null");
    if (subscriberType == null) throw new IllegalArgumentException("subscriberType cannot be null");
    if (subscriberTenantId != null && subscriberTenantUserId != null)
      throw new IllegalArgumentException("Cannot set both subscriberTenantId and subscriberTenantUserId");
    if (status == null) throw new IllegalArgumentException("status cannot be null");
    if (currentPeriodStart == null) throw new IllegalArgumentException("currentPeriodStart cannot be null");
    if (currentPeriodEnd == null) throw new IllegalArgumentException("currentPeriodEnd cannot be null");

    this.id = id;
    this.clientAppId = clientAppId;
    this.appPlanVersionId = appPlanVersionId;
    this.contractId = contractId;
    this.subscriberType = subscriberType;
    this.subscriberTenantId = subscriberTenantId;
    this.subscriberTenantUserId = subscriberTenantUserId;
    this.status = status;
    this.currentPeriodStart = currentPeriodStart;
    this.currentPeriodEnd = currentPeriodEnd;
    this.cancelAtPeriodEnd = cancelAtPeriodEnd;
    this.cancelledAt = cancelledAt;
    this.nextBillingAt = nextBillingAt;
    this.autoRenew = autoRenew;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public boolean isActive() {
    return SubscriptionStatus.ACTIVE.equals(this.status);
  }

  public void markCancelAtPeriodEnd(OffsetDateTime now) {
    this.cancelAtPeriodEnd = true;
    this.updatedAt = now;
  }

  public void cancel(OffsetDateTime now) {
    this.status = SubscriptionStatus.CANCELLED;
    this.cancelledAt = now;
    this.updatedAt = now;
  }

  public void extendPeriod(OffsetDateTime newPeriodEnd, OffsetDateTime nextBillingAt, OffsetDateTime now) {
    this.currentPeriodEnd = newPeriodEnd;
    this.nextBillingAt = nextBillingAt;
    this.updatedAt = now;
  }
}

