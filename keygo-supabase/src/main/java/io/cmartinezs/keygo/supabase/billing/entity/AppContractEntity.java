package io.cmartinezs.keygo.supabase.billing.entity;

import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;
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
 * JPA entity for app_contracts table.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_contracts",
    indexes = {
        @Index(name = "idx_app_contracts_client_app_id",    columnList = "client_app_id"),
        @Index(name = "idx_app_contracts_status",           columnList = "status"),
        @Index(name = "idx_app_contracts_contractor_email", columnList = "contractor_email")
    })
public class AppContractEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_app_id", nullable = false)
  private ClientAppEntity clientApp;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "selected_plan_version_id", nullable = false)
  private AppPlanVersionEntity selectedPlanVersion;

  @Column(name = "billing_period", nullable = false, length = 20)
  private String billingPeriod;

  @Enumerated(EnumType.STRING)
  @Column(name = "subscriber_type", nullable = false, length = 20)
  private SubscriberType subscriberType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscriber_tenant_id")
  private TenantEntity subscriberTenant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscriber_tenant_user_id")
  private TenantUserEntity subscriberTenantUser;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  @Builder.Default
  private ContractStatus status = ContractStatus.PENDING_EMAIL_VERIFICATION;

  @Column(name = "contractor_email", nullable = false, length = 255)
  private String contractorEmail;

  @Column(name = "contractor_first_name", nullable = false, length = 100)
  private String contractorFirstName;

  @Column(name = "contractor_last_name", nullable = false, length = 100)
  private String contractorLastName;

  @Column(name = "company_name", length = 200)
  private String companyName;

  @Column(name = "company_slug", length = 100, unique = true)
  private String companySlug;

  @Column(name = "company_tax_id", length = 100)
  private String companyTaxId;

  @Column(name = "company_address", columnDefinition = "TEXT")
  private String companyAddress;

  @Column(name = "email_verified_at")
  private OffsetDateTime emailVerifiedAt;

  @Column(name = "payment_verified_at")
  private OffsetDateTime paymentVerifiedAt;

  @Column(name = "verification_code", length = 10)
  private String verificationCode;

  @Column(name = "verification_code_expires_at")
  private OffsetDateTime verificationCodeExpiresAt;

  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}

