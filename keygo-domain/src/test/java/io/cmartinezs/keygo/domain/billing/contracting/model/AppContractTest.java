package io.cmartinezs.keygo.domain.billing.contracting.model;

import io.cmartinezs.keygo.domain.billing.contracting.exception.ContractStateViolationException;
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
        .status(status)
        .contractorEmail("admin@acme.com")
        .contractorFirstName("John")
        .contractorLastName("Doe")
        .contractorId(status == ContractStatus.READY_TO_ACTIVATE ? UUID.randomUUID() : null)
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
  void activate_setsActiveStatus() {
    // Given — contractorId already set after payment approval provisioning
    UUID contractorId = UUID.randomUUID();
    AppContract contract = AppContract.builder()
        .id(UUID.randomUUID())
        .clientAppId(UUID.randomUUID())
        .selectedPlanVersionId(UUID.randomUUID())
        .billingPeriod("MONTHLY")
        .status(ContractStatus.READY_TO_ACTIVATE)
        .contractorEmail("admin@acme.com")
        .contractorFirstName("John")
        .contractorLastName("Doe")
        .contractorId(contractorId)
        .expiresAt(OffsetDateTime.now().plusHours(48))
        .createdAt(OffsetDateTime.now())
        .updatedAt(OffsetDateTime.now())
        .build();
    // When
    contract.activate(OffsetDateTime.now());
    // Then
    assertThat(contract.isActive()).isTrue();
    assertThat(contract.getContractorId()).isEqualTo(contractorId);
  }

  @Test
  void activate_throwsException_whenNotReadyToActivate() {
    // Given
    AppContract contract = validContract(ContractStatus.PENDING_PAYMENT);
    // When / Then
    assertThatThrownBy(() -> contract.activate(OffsetDateTime.now()))
        .isInstanceOf(ContractStateViolationException.class)
        .hasMessageContaining("activate");
  }

  @Test
  void activate_throwsException_whenNoContractorLinked() {
    // Given — READY_TO_ACTIVATE but no contractorId
    AppContract contract = AppContract.builder()
        .id(UUID.randomUUID())
        .clientAppId(UUID.randomUUID())
        .selectedPlanVersionId(UUID.randomUUID())
        .billingPeriod("MONTHLY")
        .status(ContractStatus.READY_TO_ACTIVATE)
        .contractorEmail("admin@acme.com")
        .contractorFirstName("John")
        .contractorLastName("Doe")
        .contractorId(null)
        .expiresAt(OffsetDateTime.now().plusHours(48))
        .createdAt(OffsetDateTime.now())
        .updatedAt(OffsetDateTime.now())
        .build();
    // When / Then
    assertThatThrownBy(() -> contract.activate(OffsetDateTime.now()))
        .isInstanceOf(ContractStateViolationException.class)
        .hasMessageContaining("contractor");
  }

  @Test
  void builder_throwsException_whenContractorEmailIsBlank() {
    assertThatThrownBy(() -> AppContract.builder()
        .clientAppId(UUID.randomUUID())
        .selectedPlanVersionId(UUID.randomUUID())
        .status(ContractStatus.PENDING_EMAIL_VERIFICATION)
        .contractorEmail("")
        .contractorFirstName("John")
        .contractorLastName("Doe")
        .expiresAt(OffsetDateTime.now().plusHours(48))
        .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("contractorEmail");
  }

  // ── generateUsername ────────────────────────────────────────────────────────

  @Test
  void generateUsername_standardName_returnsInitialPlusLastname() {
    // Given
    AppContract contract = contractWith("John", "Doe");
    // When / Then
    assertThat(contract.generateUsername()).isEqualTo("jdoe");
  }

  @Test
  void generateUsername_accentedName_stripsAccents() {
    // Given — "Carlos" / "Martínez"
    AppContract contract = contractWith("Carlos", "Martínez");
    // When / Then
    assertThat(contract.generateUsername()).isEqualTo("cmartinez");
  }

  @Test
  void generateUsername_ennye_isNormalized() {
    // Given — "José" / "Ñoño"
    AppContract contract = contractWith("José", "Ñoño");
    // When / Then
    assertThat(contract.generateUsername()).isEqualTo("jnono");
  }

  @Test
  void generateUsername_nameWithSpaces_removesSpaces() {
    // Given — "Ana María" / "López González"
    AppContract contract = contractWith("Ana María", "López González");
    // When / Then
    // 'A' → 'a', "LópezGonzález" stripped → "lopezgonzalez"
    assertThat(contract.generateUsername()).isEqualTo("alopezgonzalez");
  }

  @Test
  void generateUsername_shortResult_isPaddedToMinimum3() {
    // Given — first initial 'a', last name stripped to 'i' → "ai" (len 2) → pad → "ai_"
    AppContract contract = contractWith("A", "I");
    // When / Then
    String username = contract.generateUsername();
    assertThat(username).hasSize(3).isEqualTo("ai_");
  }

  @Test
  void generateUsername_veryLongLastname_isTruncatedTo100() {
    // Given
    String longLastName = "A".repeat(200);
    AppContract contract = contractWith("Carlos", longLastName);
    // When
    String username = contract.generateUsername();
    // Then
    assertThat(username).hasSize(100);
  }

  @Test
  void generateUsername_resultIsValidForUsernameValueObject() {
    // Given — accented, with space, mixed case
    AppContract contract = contractWith("María José", "Ñúñez-García");
    // When
    String raw = contract.generateUsername();
    // Then — must match [a-zA-Z0-9_.\\-]{3,100}
    assertThat(raw).matches("^[a-zA-Z0-9_\\-.]{3,100}$");
  }

  private AppContract contractWith(String firstName, String lastName) {
    return AppContract.builder()
        .id(UUID.randomUUID())
        .clientAppId(UUID.randomUUID())
        .selectedPlanVersionId(UUID.randomUUID())
        .billingPeriod("MONTHLY")
        .status(ContractStatus.PENDING_EMAIL_VERIFICATION)
        .contractorEmail("test@example.com")
        .contractorFirstName(firstName)
        .contractorLastName(lastName)
        .expiresAt(OffsetDateTime.now().plusHours(48))
        .createdAt(OffsetDateTime.now())
        .updatedAt(OffsetDateTime.now())
        .build();
  }
}
