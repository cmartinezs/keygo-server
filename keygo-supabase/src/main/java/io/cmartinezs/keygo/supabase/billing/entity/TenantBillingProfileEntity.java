package io.cmartinezs.keygo.supabase.billing.entity;

import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for tenant_billing_profiles table.
 * Fiscal/billing data per Tenant. Used to generate invoices and CFDI.
 * PERSONAL = persona física / individual; COMPANY = persona moral / empresa.
 * At most one profile per tenant may have is_default = TRUE.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tenant_billing_profiles",
    indexes = @Index(name = "idx_tenant_billing_profiles_tenant", columnList = "tenant_id"))
public class TenantBillingProfileEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", nullable = false)
  private TenantEntity tenant;

  @Column(name = "billing_type", nullable = false, length = 20)
  private String billingType;

  @Column(name = "billing_name", nullable = false, length = 300)
  private String billingName;

  @Column(name = "tax_id", length = 100)
  private String taxId;

  @Column(name = "tax_regime", length = 100)
  private String taxRegime;

  @Column(name = "address_line1", length = 300)
  private String addressLine1;

  @Column(name = "address_line2", length = 300)
  private String addressLine2;

  @Column(length = 100)
  private String city;

  @Column(length = 100)
  private String state;

  @Column(nullable = false, length = 2)
  @Builder.Default
  private String country = "MX";

  @Column(name = "postal_code", length = 20)
  private String postalCode;

  @Column(name = "contact_email", length = 255)
  private String contactEmail;

  @Column(name = "contact_phone", length = 50)
  private String contactPhone;

  @Column(name = "is_default", nullable = false)
  @Builder.Default
  private boolean isDefault = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}

