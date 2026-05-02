package io.cmartinezs.keygo.domain.billing.payment.model;

/**
 * Status of a payment transaction.
 * @author cmartinezs
 * @version 1.0
 */
public enum PaymentStatus {
  PENDING,
  APPROVED,
  REJECTED,
  CANCELLED,
  EXPIRED
}

