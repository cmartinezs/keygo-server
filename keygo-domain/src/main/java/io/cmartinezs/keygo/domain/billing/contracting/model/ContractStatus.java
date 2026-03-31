package io.cmartinezs.keygo.domain.billing.contracting.model;

/**
 * Status of an app contract in the contracting flow.
 * @author cmartinezs
 * @version 1.0
 */
public enum ContractStatus {
  PENDING_EMAIL_VERIFICATION,
  PENDING_PAYMENT,
  READY_TO_ACTIVATE,
  ACTIVE,
  SUPERSEDED,
  FINALIZED,
  CANCELLED,
  EXPIRED,
  FAILED
}

