package io.cmartinezs.keygo.domain.billing.contractor.model;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Domain model for a Contractor — the central entity of billing v2.
 * A Contractor is the person or company that signs contracts with the platform.
 * It has a 1:1 link to a PlatformUser (the identity that exists at platform level).
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class Contractor {

  private final UUID id;
  /** Primary contact of the contractor account, if defined. */
  private final UUID primaryContactPlatformUserId;
  private final ContractorType type;
  private final String displayName;
  private final String legalName;
  private final String taxId;
  private final String billingEmail;
  private ContractorStatus status;
  private final OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  @Builder
  private Contractor(
      UUID id,
      UUID primaryContactPlatformUserId,
      ContractorType type,
      String displayName,
      String legalName,
      String taxId,
      String billingEmail,
      ContractorStatus status,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    if (billingEmail == null || billingEmail.isBlank()) {
      throw new IllegalArgumentException("billingEmail cannot be blank");
    }

    this.id = id;
    this.primaryContactPlatformUserId = primaryContactPlatformUserId;
    this.type = type != null ? type : ContractorType.PERSON;
    this.displayName = displayName != null && !displayName.isBlank() ? displayName : billingEmail;
    this.legalName = legalName;
    this.taxId = taxId;
    this.billingEmail = billingEmail;
    this.status = status != null ? status : ContractorStatus.PENDING;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public boolean isActive() {
    return ContractorStatus.ACTIVE.equals(this.status);
  }

  public void activate(OffsetDateTime now) {
    this.status = ContractorStatus.ACTIVE;
    this.updatedAt = now;
  }

  public void suspend(OffsetDateTime now) {
    this.status = ContractorStatus.SUSPENDED;
    this.updatedAt = now;
  }
}

