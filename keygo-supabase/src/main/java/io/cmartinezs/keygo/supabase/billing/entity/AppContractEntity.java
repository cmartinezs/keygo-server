package io.cmartinezs.keygo.supabase.billing.entity;

import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for app_contracts table — billing model v2 (contractor-centric).
 * The physical mapping follows the Flyway baseline, including the onboarding snapshot columns
 * added for contact/company data.
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
        @Index(name = "idx_app_contracts_client_app",        columnList = "client_app_id"),
        @Index(name = "idx_app_contracts_contractor_id",     columnList = "contractor_id"),
        @Index(name = "idx_app_contracts_status",            columnList = "status"),
        @Index(name = "idx_app_contracts_billing_contact_email",  columnList = "billing_contact_email")
    })
public class AppContractEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "client_app_id", nullable = false)
  private ClientAppEntity clientApp;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "app_plan_version_id", nullable = false)
  private AppPlanVersionEntity selectedPlanVersion;

  /** NULL until payment approval provisions the contractor account. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contractor_id")
  private ContractorEntity contractor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_platform_user_id")
  private PlatformUserEntity createdByPlatformUser;

  @Column(name = "billing_period", nullable = false, length = 20)
  private String billingPeriod;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 40)
  @Builder.Default
  private ContractStatus status = ContractStatus.PENDING_EMAIL_VERIFICATION;

  @Column(name = "billing_contact_email", nullable = false, length = 255)
  private String contractorEmail;

  @Column(name = "contractor_first_name", length = 100)
  private String contractorFirstName;

  @Column(name = "contractor_last_name", length = 100)
  private String contractorLastName;

  @Column(name = "billing_contact_name", length = 255)
  private String billingContactName;

  @Column(name = "company_name", length = 255)
  private String companyName;

  @Column(name = "company_tax_id", length = 100)
  private String companyTaxId;

  @Column(name = "company_address")
  private String companyAddress;

  @Column(name = "contractor_email_verified_at")
  private OffsetDateTime emailVerifiedAt;

  @Column(name = "payment_verified_at")
  private OffsetDateTime paymentVerifiedAt;

  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @PrePersist
  @PreUpdate
  void syncLegacyContactName() {
    String first = contractorFirstName != null ? contractorFirstName.trim() : "";
    String last = contractorLastName != null ? contractorLastName.trim() : "";
    String combined = (first + " " + last).trim();
    if (!combined.isBlank()) {
      billingContactName = combined;
    }
  }
}
