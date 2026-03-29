package io.cmartinezs.keygo.supabase.billing.entity;

import io.cmartinezs.keygo.domain.billing.invoice.model.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA entity for invoices table.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "invoices",
    indexes = {
        @Index(name = "idx_invoices_subscription", columnList = "subscription_id"),
        @Index(name = "idx_invoices_status",        columnList = "status")
    })
public class InvoiceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_id", nullable = false)
  private AppSubscriptionEntity subscription;

  @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
  private String invoiceNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private InvoiceStatus status = InvoiceStatus.DRAFT;

  @Column(name = "issue_date", nullable = false)
  private LocalDate issueDate;

  @Column(name = "due_date", nullable = false)
  private LocalDate dueDate;

  @Column(name = "period_start", nullable = false)
  private LocalDate periodStart;

  @Column(name = "period_end", nullable = false)
  private LocalDate periodEnd;

  @Column(nullable = false, length = 3)
  @Builder.Default
  private String currency = "MXN";

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal subtotal;

  @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
  @Builder.Default
  private BigDecimal taxAmount = BigDecimal.ZERO;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal total;

  @Column(name = "billing_name_snapshot", length = 300)
  private String billingNameSnapshot;

  @Column(name = "billing_tax_id_snapshot", length = 100)
  private String billingTaxIdSnapshot;

  @Column(name = "billing_address_snapshot", columnDefinition = "TEXT")
  private String billingAddressSnapshot;

  @Column(name = "plan_name_snapshot", length = 100)
  private String planNameSnapshot;

  @Column(name = "plan_version_snapshot", length = 20)
  private String planVersionSnapshot;

  @Column(name = "pdf_url", columnDefinition = "TEXT")
  private String pdfUrl;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;
}

