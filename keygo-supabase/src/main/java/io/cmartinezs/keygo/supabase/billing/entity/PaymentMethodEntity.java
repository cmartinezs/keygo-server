package io.cmartinezs.keygo.supabase.billing.entity;

import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for payment_methods table.
 * PSP payment method tokens per Tenant.
 * NEVER stores raw card data (PAN/CVV) — only display info and PSP tokens.
 * At most one method per tenant may have is_default = TRUE.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_methods",
    indexes = {
        @Index(name = "idx_payment_methods_tenant",  columnList = "tenant_id"),
        @Index(name = "idx_payment_methods_status",  columnList = "tenant_id, status")
    })
public class PaymentMethodEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", nullable = false)
  private TenantEntity tenant;

  @Column(nullable = false, length = 50)
  private String provider;

  @Column(name = "method_type", nullable = false, length = 20)
  private String methodType;

  @Column(name = "provider_token", length = 500)
  private String providerToken;

  @Column(name = "last_four", length = 4)
  private String lastFour;

  @Column(name = "card_brand", length = 50)
  private String cardBrand;

  @Column(name = "expiry_month")
  private Short expiryMonth;

  @Column(name = "expiry_year")
  private Short expiryYear;

  @Column(name = "paypal_email", length = 255)
  private String paypalEmail;

  @Column(name = "display_label", length = 100)
  private String displayLabel;

  @Column(name = "is_default", nullable = false)
  @Builder.Default
  private boolean isDefault = false;

  @Column(nullable = false, length = 20)
  @Builder.Default
  private String status = "ACTIVE";

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}

