package io.cmartinezs.keygo.api.billing.response;

import io.cmartinezs.keygo.domain.billing.invoice.model.Invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response data for invoice endpoints.
 */
public record AppInvoiceData(
    UUID id,
    UUID subscriptionId,
    String invoiceNumber,
    String status,
    LocalDate issueDate,
    LocalDate dueDate,
    LocalDate periodStart,
    LocalDate periodEnd,
    String currency,
    BigDecimal subtotal,
    BigDecimal taxAmount,
    BigDecimal total,
    String billingNameSnapshot,
    String planVersionSnapshot,
    String pdfUrl,
    OffsetDateTime createdAt
) {
  public static AppInvoiceData from(Invoice i) {
    return new AppInvoiceData(
        i.getId(),
        i.getSubscriptionId(),
        i.getInvoiceNumber(),
        i.getStatus().name(),
        i.getIssueDate(),
        i.getDueDate(),
        i.getPeriodStart(),
        i.getPeriodEnd(),
        i.getCurrency(),
        i.getSubtotal(),
        i.getTaxAmount(),
        i.getTotal(),
        i.getBillingNameSnapshot(),
        i.getPlanVersionSnapshot(),
        i.getPdfUrl(),
        i.getCreatedAt()
    );
  }

}
