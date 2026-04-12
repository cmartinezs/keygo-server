package io.cmartinezs.keygo.domain.billing.contractor.model;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Association between a contractor and a platform user with a contractor-scoped role.
 */
@Getter
public class ContractorUser {

  private final UUID contractorId;
  private final UUID platformUserId;
  private final ContractorUserRole role;
  private final OffsetDateTime assignedAt;

  @Builder
  private ContractorUser(
      UUID contractorId,
      UUID platformUserId,
      ContractorUserRole role,
      OffsetDateTime assignedAt) {
    if (contractorId == null) throw new IllegalArgumentException("contractorId cannot be null");
    if (platformUserId == null) throw new IllegalArgumentException("platformUserId cannot be null");
    if (role == null) throw new IllegalArgumentException("role cannot be null");

    this.contractorId = contractorId;
    this.platformUserId = platformUserId;
    this.role = role;
    this.assignedAt = assignedAt;
  }
}
