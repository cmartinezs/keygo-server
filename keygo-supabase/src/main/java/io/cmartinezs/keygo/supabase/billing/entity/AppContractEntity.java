package io.cmartinezs.keygo.supabase.billing.entity;

import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for app_contracts table — billing model v2 (contractor-centric).
 * contractor_id is NULL until email is verified and Contractor is created/found.
 * From PENDING_PAYMENT onwards contractor_id is always NOT NULL.
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
        @Index(name = "idx_app_contracts_contractor_email",  columnList = "contractor_email")
    })
public class AppContractEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_app_id")  // nullable for platform contracts
  private ClientAppEntity clientApp;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "selected_plan_version_id", nullable = false)
  private AppPlanVersionEntity selectedPlanVersion;

  /** NULL until email verified. Set when Contractor is created/identified. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contractor_id")
  private ContractorEntity contractor;

  @Column(name = "billing_period", nullable = false, length = 20)
  private String billingPeriod;

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

  // Company data (optional, for B2B invoicing)
  @Column(name = "company_name", length = 200)
  private String companyName;

  @Column(name = "company_tax_id", length = 100)
  private String companyTaxId;

  @Column(name = "company_address", columnDefinition = "TEXT")
  private String companyAddress;

  @Column(name = "verification_code", length = 10)
  private String verificationCode;

  @Column(name = "verification_code_expires_at")
  private OffsetDateTime verificationCodeExpiresAt;

  @Column(name = "email_verified_at")
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
}
