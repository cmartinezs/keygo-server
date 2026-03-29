package io.cmartinezs.keygo.domain.billing.contracting.model;

import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AppContractTest {

  private AppContract validContract(ContractStatus status) {
    return AppContract.builder()
        .id(UUID.randomUUID())
        .clientAppId(UUID.randomUUID())
        .selectedPlanVersionId(UUID.randomUUID())
        .billingPeriod("MONTHLY")
        .subscriberType(SubscriberType.TENANT)
        .status(status)
        .contractorEmail("admin@acme.com")
        .contractorFirstName("John")
        .contractorLastName("Doe")
        .companySlug("acme")
        .expiresAt(OffsetDateTime.now().plusHours(48))
        .createdAt(OffsetDateTime.now())
        .updatedAt(OffsetDateTime.now())
        .build();
  }

  @Test
  void markEmailVerified_advancesStatusToPendingPayment() {
    // Given
    AppContract contract = validContract(ContractStatus.PENDING_EMAIL_VERIFICATION);
    // When
    contract.markEmailVerified(OffsetDateTime.now());
    // Then
    assertThat(contract.getStatus()).isEqualTo(ContractStatus.PENDING_PAYMENT);
    assertThat(contract.isEmailVerified()).isTrue();
  }

  @Test
  void markPaymentApproved_advancesStatusToReadyToActivate() {
    // Given
    AppContract contract = validContract(ContractStatus.PENDING_PAYMENT);
    // When
    contract.markPaymentApproved(OffsetDateTime.now());
    // Then
    assertThat(contract.getStatus()).isEqualTo(ContractStatus.READY_TO_ACTIVATE);
    assertThat(contract.isPaymentVerified()).isTrue();
  }

  @Test
  void activate_withTenantId_setsSubscriberAndActivatedStatus() {
    // Given
    AppContract contract = validContract(ContractStatus.READY_TO_ACTIVATE);
    UUID tenantId = UUID.randomUUID();
    // When
    contract.activate(tenantId, null, OffsetDateTime.now());
    // Then
    assertThat(contract.isActivated()).isTrue();
    assertThat(contract.getSubscriberTenantId()).isEqualTo(tenantId);
  }

  @Test
  void activate_throwsException_whenNotReadyToActivate() {
    // Given
    AppContract contract = validContract(ContractStatus.PENDING_PAYMENT);
    // When / Then
    assertThatThrownBy(() -> contract.activate(UUID.randomUUID(), null, OffsetDateTime.now()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("READY_TO_ACTIVATE");
  }

  @Test
  void activate_throwsException_whenBothSubscriberIdsProvided() {
    // Given
    AppContract contract = validContract(ContractStatus.READY_TO_ACTIVATE);
    // When / Then
    assertThatThrownBy(() ->
        contract.activate(UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void builder_throwsException_whenContractorEmailIsBlank() {
    assertThatThrownBy(() -> AppContract.builder()
        .clientAppId(UUID.randomUUID())
        .selectedPlanVersionId(UUID.randomUUID())
        .subscriberType(SubscriberType.TENANT)
        .status(ContractStatus.PENDING_EMAIL_VERIFICATION)
        .contractorEmail("")
        .contractorFirstName("John")
        .contractorLastName("Doe")
        .expiresAt(OffsetDateTime.now().plusHours(48))
        .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("contractorEmail");
  }
}

