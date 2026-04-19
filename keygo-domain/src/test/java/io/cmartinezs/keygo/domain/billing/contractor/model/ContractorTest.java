package io.cmartinezs.keygo.domain.billing.contractor.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ContractorTest {

  @Test
  void builder_withMinimalArgs_shouldCreatePendingContractor() {
    Contractor contractor = Contractor.builder()
        .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
        .billingEmail("billing@test.com")
        .build();

    assertNotNull(contractor);
    assertEquals("billing@test.com", contractor.getBillingEmail());
    assertEquals(ContractorType.PERSON, contractor.getType());
    assertEquals(ContractorStatus.PENDING, contractor.getStatus());
    assertEquals("billing@test.com", contractor.getDisplayName());
  }

  @Test
  void builder_withAllArgs_shouldCreateContractor() {
    OffsetDateTime now = OffsetDateTime.now();
    Contractor contractor = Contractor.builder()
        .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
        .primaryContactPlatformUserId(UUID.fromString("660e8500-e29b-41d4-a716-446655440001"))
        .type(ContractorType.COMPANY)
        .displayName("Acme Corp")
        .legalName("Acme Corporation SA de CV")
        .taxId("ACM-123456")
        .billingEmail("billing@acme.com")
        .status(ContractorStatus.ACTIVE)
        .createdAt(now)
        .updatedAt(now)
        .build();

    assertEquals(ContractorType.COMPANY, contractor.getType());
    assertEquals("Acme Corp", contractor.getDisplayName());
    assertEquals("Acme Corporation SA de CV", contractor.getLegalName());
    assertEquals("ACM-123456", contractor.getTaxId());
    assertEquals(ContractorStatus.ACTIVE, contractor.getStatus());
  }

  @Test
  void builder_withNullType_shouldDefaultToPerson() {
    Contractor contractor = Contractor.builder()
        .billingEmail("test@test.com")
        .build();

    assertEquals(ContractorType.PERSON, contractor.getType());
  }

  @Test
  void builder_withNullStatus_shouldDefaultToPending() {
    Contractor contractor = Contractor.builder()
        .billingEmail("test@test.com")
        .build();

    assertEquals(ContractorStatus.PENDING, contractor.getStatus());
  }

  @Test
  void builder_withBlankDisplayName_shouldUseBillingEmail() {
    Contractor contractor = Contractor.builder()
        .billingEmail("test@test.com")
        .displayName("   ")
        .build();

    assertEquals("test@test.com", contractor.getDisplayName());
  }

  @Test
  void builder_withBlankBillingEmail_shouldThrowException() {
    assertThrows(IllegalArgumentException.class, () ->
        Contractor.builder()
            .billingEmail("   ")
            .build()
    );
  }

  @Test
  void isActive_whenActive_shouldReturnTrue() {
    Contractor contractor = Contractor.builder()
        .billingEmail("test@test.com")
        .status(ContractorStatus.ACTIVE)
        .build();

    assertTrue(contractor.isActive());
  }

  @Test
  void isActive_whenNotActive_shouldReturnFalse() {
    Contractor contractor = Contractor.builder()
        .billingEmail("test@test.com")
        .status(ContractorStatus.PENDING)
        .build();

    assertFalse(contractor.isActive());
  }

  @Test
  void activate_shouldChangeStatusToActive() {
    Contractor contractor = Contractor.builder()
        .billingEmail("test@test.com")
        .build();

    contractor.activate(OffsetDateTime.now());

    assertEquals(ContractorStatus.ACTIVE, contractor.getStatus());
    assertNotNull(contractor.getUpdatedAt());
  }

  @Test
  void suspend_shouldChangeStatusToSuspended() {
    Contractor contractor = Contractor.builder()
        .billingEmail("test@test.com")
        .status(ContractorStatus.ACTIVE)
        .build();

    contractor.suspend(OffsetDateTime.now());

    assertEquals(ContractorStatus.SUSPENDED, contractor.getStatus());
    assertNotNull(contractor.getUpdatedAt());
  }
}