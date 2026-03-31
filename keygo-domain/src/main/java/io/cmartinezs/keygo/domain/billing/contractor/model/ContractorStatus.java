package io.cmartinezs.keygo.domain.billing.contractor.model;

/**
 * Life-cycle status of a Contractor in the billing model v2.
 * @author cmartinezs
 * @version 1.0
 */
public enum ContractorStatus {
  /** Email verified, awaiting first payment. */
  PENDING,
  /** First contract activated successfully. */
  ACTIVE,
  /** Suspended due to payment issue or administrative decision. */
  SUSPENDED
}

