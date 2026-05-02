package io.cmartinezs.keygo.supabase.billing.entity;

import io.cmartinezs.keygo.supabase.clientapp.entity.ClientAppEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for payment_transactions table.
 * Records each payment event (initial activation, renewal, etc.).
 * raw_response stores PSP payload for auditing.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_transactions",
    indexes = {
        @Index(name = "idx_payment_tx_contract",     columnList = "contract_id"),
        @Index(name = "idx_payment_tx_subscription", columnList = "subscription_id"),
        @Index(name = "idx_payment_tx_status",       columnList = "status")
    })
public class PaymentTransactionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contractor_id", nullable = false)
  private ContractorEntity contractor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "client_app_id")
  private ClientAppEntity clientApp;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contract_id")
  private AppContractEntity contract;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_id")
  private AppSubscriptionEntity subscription;

  @Column(nullable = false, length = 20)
  @Builder.Default
  private String provider = "MOCK";

  @Column(name = "provider_reference", length = 255)
  private String providerReference;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  @Builder.Default
  private String currency = "USD";

  @Column(nullable = false, length = 20)
  @Builder.Default
  private String status = "PENDING";

  @Column(name = "paid_at")
  private OffsetDateTime paidAt;

  @Column(name = "refunded_at")
  private OffsetDateTime refundedAt;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "provider_payload", nullable = false, columnDefinition = "jsonb")
  private String rawResponse;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;
}

