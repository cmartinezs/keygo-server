package io.cmartinezs.keygo.domain.billing.contracting.model;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Verification code used during contract onboarding before a contractor account exists.
 */
@Getter
public class ContractEmailVerification {

  private final UUID id;
  private final UUID contractId;
  private String code;
  private OffsetDateTime expiresAt;
  private OffsetDateTime usedAt;
  private final OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  @Builder
  private ContractEmailVerification(
      UUID id,
      UUID contractId,
      String code,
      OffsetDateTime expiresAt,
      OffsetDateTime usedAt,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    if (contractId == null) throw new IllegalArgumentException("contractId cannot be null");
    if (code == null || code.isBlank()) throw new IllegalArgumentException("code cannot be blank");
    if (expiresAt == null) throw new IllegalArgumentException("expiresAt cannot be null");

    this.id = id;
    this.contractId = contractId;
    this.code = code;
    this.expiresAt = expiresAt;
    this.usedAt = usedAt;
    this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
    this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
  }

  public boolean isExpired(OffsetDateTime now) {
    return now.isAfter(expiresAt);
  }

  public boolean isUsed() {
    return usedAt != null;
  }

  public boolean matches(String inputCode) {
    return code.equalsIgnoreCase(inputCode);
  }

  public void renew(String newCode, OffsetDateTime newExpiresAt, OffsetDateTime now) {
    if (newCode == null || newCode.isBlank()) {
      throw new IllegalArgumentException("newCode cannot be blank");
    }
    if (newExpiresAt == null) {
      throw new IllegalArgumentException("newExpiresAt cannot be null");
    }
    this.code = newCode;
    this.expiresAt = newExpiresAt;
    this.usedAt = null;
    this.updatedAt = now;
  }

  public void markUsed(OffsetDateTime now) {
    this.usedAt = now;
    this.updatedAt = now;
  }
}
