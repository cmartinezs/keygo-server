package io.cmartinezs.keygo.supabase.billing.entity;

import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriptionStatus;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for app_subscriptions table.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_subscriptions",
    indexes = {
        @Index(name = "idx_app_subscriptions_client_app", columnList = "client_app_id"),
        @Index(name = "idx_app_subscriptions_status",     columnList = "status")
    })
public class AppSubscriptionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_app_id", nullable = false)
  private ClientAppEntity clientApp;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "app_plan_version_id", nullable = false)
  private AppPlanVersionEntity appPlanVersion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contract_id")
  private AppContractEntity contract;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscriber_tenant_id")
  private TenantEntity subscriberTenant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscriber_tenant_user_id")
  private TenantUserEntity subscriberTenantUser;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private SubscriptionStatus status = SubscriptionStatus.PENDING;

  @Column(name = "current_period_start", nullable = false)
  private OffsetDateTime currentPeriodStart;

  @Column(name = "current_period_end", nullable = false)
  private OffsetDateTime currentPeriodEnd;

  @Column(name = "cancel_at_period_end", nullable = false)
  @Builder.Default
  private boolean cancelAtPeriodEnd = false;

  @Column(name = "cancelled_at")
  private OffsetDateTime cancelledAt;

  @Column(name = "next_billing_at")
  private OffsetDateTime nextBillingAt;

  @Column(name = "auto_renew", nullable = false)
  @Builder.Default
  private boolean autoRenew = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}
