package io.cmartinezs.keygo.domain.billing.invoice.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain model for a billing invoice (historical snapshot).
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class Invoice {

  private final UUID id;
  private final UUID subscriptionId;
  private final String invoiceNumber;
  private InvoiceStatus status;
  private final LocalDate issueDate;
  private final LocalDate dueDate;
  private final LocalDate periodStart;
  private final LocalDate periodEnd;
  private final String currency;
  private final BigDecimal subtotal;
  private final BigDecimal taxAmount;
  private final BigDecimal total;
  // Historical snapshots
  private final String billingNameSnapshot;
  private final String billingTaxIdSnapshot;
  private final String billingAddressSnapshot;
  private final String planNameSnapshot;
  private final String planVersionSnapshot;
  private final String pdfUrl;
  private final OffsetDateTime createdAt;

  public boolean isPaid() {
    return InvoiceStatus.PAID.equals(this.status);
  }
}

