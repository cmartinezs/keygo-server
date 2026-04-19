package io.cmartinezs.keygo.domain.billing.contracting.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ContractEmailVerificationTest {

  @Test
  void builder_withValidArgs_shouldCreate() {
    UUID id = UUID.randomUUID();
    UUID contractId = UUID.randomUUID();
    OffsetDateTime now = OffsetDateTime.now();

    ContractEmailVerification v = ContractEmailVerification.builder()
        .id(id)
        .contractId(contractId)
        .code("ABC123")
        .expiresAt(now.plusHours(24))
        .createdAt(now)
        .build();

    assertEquals(id, v.getId());
    assertEquals(contractId, v.getContractId());
    assertEquals("ABC123", v.getCode());
  }

  @Test
  void builder_shouldSetAllFields() {
    OffsetDateTime now = OffsetDateTime.now();

    ContractEmailVerification v = ContractEmailVerification.builder()
        .id(UUID.randomUUID())
        .contractId(UUID.randomUUID())
        .code("XYZ789")
        .expiresAt(now.plusHours(48))
        .usedAt(now.minusHours(1))
        .createdAt(now)
        .updatedAt(now)
        .build();

    assertEquals("XYZ789", v.getCode());
    assertNotNull(v.getUsedAt());
  }
}