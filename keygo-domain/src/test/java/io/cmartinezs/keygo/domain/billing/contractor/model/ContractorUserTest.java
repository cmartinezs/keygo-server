package io.cmartinezs.keygo.domain.billing.contractor.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ContractorUserTest {

  @Test
  void builder_withValidArgs_shouldCreate() {
    UUID contractorId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    UUID platformUserId = UUID.fromString("660e8500-e29b-41d4-a716-446655440001");
    OffsetDateTime now = OffsetDateTime.now();

    ContractorUser user = ContractorUser.builder()
        .contractorId(contractorId)
        .platformUserId(platformUserId)
        .role(ContractorUserRole.OWNER)
        .assignedAt(now)
        .build();

    assertEquals(contractorId, user.getContractorId());
    assertEquals(platformUserId, user.getPlatformUserId());
    assertEquals(ContractorUserRole.OWNER, user.getRole());
    assertEquals(now, user.getAssignedAt());
  }

  @Test
  void builder_withNullContractorId_shouldThrow() {
    assertThrows(IllegalArgumentException.class, () ->
        ContractorUser.builder()
            .contractorId(null)
            .platformUserId(UUID.randomUUID())
            .role(ContractorUserRole.OWNER)
            .build()
    );
  }

  @Test
  void builder_withNullPlatformUserId_shouldThrow() {
    assertThrows(IllegalArgumentException.class, () ->
        ContractorUser.builder()
            .contractorId(UUID.randomUUID())
            .platformUserId(null)
            .role(ContractorUserRole.OWNER)
            .build()
    );
  }

  @Test
  void builder_withNullRole_shouldThrow() {
    assertThrows(IllegalArgumentException.class, () ->
        ContractorUser.builder()
            .contractorId(UUID.randomUUID())
            .platformUserId(UUID.randomUUID())
            .role(null)
            .build()
    );
  }
}