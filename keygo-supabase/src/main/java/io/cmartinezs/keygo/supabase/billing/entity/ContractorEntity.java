package io.cmartinezs.keygo.supabase.billing.entity;

import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorStatus;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorType;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for contractors table (billing model v2).
 * A Contractor is the central billing entity — a person or company that signs contracts with the
 * platform. The primary contact is a platform user, but the contractor has its own commercial data.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contractors",
    indexes = @Index(name = "idx_contractors_status", columnList = "status"))
public class ContractorEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "primary_contact_platform_user_id")
  private PlatformUserEntity primaryContactPlatformUser;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20)
  @Builder.Default
  private ContractorType type = ContractorType.PERSON;

  @Column(name = "display_name", nullable = false, length = 255)
  private String displayName;

  @Column(name = "legal_name", length = 255)
  private String legalName;

  @Column(name = "tax_id", length = 100)
  private String taxId;

  @Column(name = "billing_email", nullable = false, length = 255)
  private String billingEmail;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private ContractorStatus status = ContractorStatus.PENDING;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @PrePersist
  @PreUpdate
  void syncDerivedCommercialFields() {
    if (billingEmail == null && primaryContactPlatformUser != null) {
      billingEmail = primaryContactPlatformUser.getEmail();
    }
    if (displayName == null || displayName.isBlank()) {
      if (primaryContactPlatformUser != null
          && primaryContactPlatformUser.getDisplayName() != null
          && !primaryContactPlatformUser.getDisplayName().isBlank()) {
        displayName = primaryContactPlatformUser.getDisplayName();
      } else if (primaryContactPlatformUser != null && primaryContactPlatformUser.getEmail() != null) {
        displayName = primaryContactPlatformUser.getEmail();
      }
    }
  }
}

