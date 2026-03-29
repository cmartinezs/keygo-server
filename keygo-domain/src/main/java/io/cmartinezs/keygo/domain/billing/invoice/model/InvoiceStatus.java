package io.cmartinezs.keygo.domain.billing.invoice.model;

/**
 * Status of an invoice.
 * @author cmartinezs
 * @version 1.0
 */
public enum InvoiceStatus {
  DRAFT,
  ISSUED,
  PAID,
  VOID,
  OVERDUE
}

