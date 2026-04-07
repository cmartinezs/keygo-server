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
  /** 1:1 link to PlatformUser (platform-level identity). */
  private final UUID platformUserId;
  private ContractorStatus status;
  private final OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  @Builder
  private Contractor(
      UUID id,
      UUID platformUserId,
      ContractorStatus status,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    if (platformUserId == null) throw new IllegalArgumentException("platformUserId cannot be null");
    if (status == null) throw new IllegalArgumentException("status cannot be null");

    this.id = id;
    this.platformUserId = platformUserId;
    this.status = status;
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

