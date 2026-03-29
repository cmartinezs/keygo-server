package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriberType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyContractEmailUseCaseTest {

  @Mock AppContractRepositoryPort contractRepo;

  @InjectMocks
  VerifyContractEmailUseCase useCase;

  private AppContract pendingEmailContract(String code, OffsetDateTime codeExpiry) {
    return AppContract.builder()
        .id(UUID.randomUUID())
        .clientAppId(UUID.randomUUID())
        .selectedPlanVersionId(UUID.randomUUID())
        .billingPeriod("MONTHLY")
        .subscriberType(SubscriberType.TENANT)
        .status(ContractStatus.PENDING_EMAIL_VERIFICATION)
        .contractorEmail("admin@acme.com")
        .contractorFirstName("John").contractorLastName("Doe")
        .companySlug("acme")
        .verificationCode(code)
        .verificationCodeExpiresAt(codeExpiry)
        .expiresAt(OffsetDateTime.now().plusHours(48))
        .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
        .build();
  }

  @Test
  void execute_validCode_advancesToPendingPayment() {
    // Given
    String code = "123456";
    AppContract contract = pendingEmailContract(code, OffsetDateTime.now().plusMinutes(30));
    UUID contractId = contract.getId();
    when(contractRepo.findById(contractId)).thenReturn(Optional.of(contract));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    var result = useCase.execute(contractId, code);

    // Then
    assertThat(result.contract().getStatus()).isEqualTo(ContractStatus.PENDING_PAYMENT);
    assertThat(result.contract().isEmailVerified()).isTrue();
    verify(contractRepo).save(any());
  }

  @Test
  void execute_wrongCode_throwsIllegalArgument() {
    // Given
    AppContract contract = pendingEmailContract("123456", OffsetDateTime.now().plusMinutes(30));
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(contract.getId(), "999999"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Código de verificación inválido");
    verify(contractRepo, never()).save(any());
  }

  @Test
  void execute_expiredCode_throwsIllegalState() {
    // Given — code expired 1 minute ago
    AppContract contract = pendingEmailContract("123456", OffsetDateTime.now().minusMinutes(1));
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(contract.getId(), "123456"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("expirado");
    verify(contractRepo, never()).save(any());
  }

  @Test
  void execute_contractNotFound_throwsIllegalArgument() {
    // Given
    UUID contractId = UUID.randomUUID();
    when(contractRepo.findById(contractId)).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(contractId, "123456"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Contrato no encontrado");
  }

  @Test
  void execute_alreadyInPendingPayment_throwsIllegalState() {
    // Given — contract already verified (PENDING_PAYMENT)
    AppContract contract = AppContract.builder()
        .id(UUID.randomUUID())
        .clientAppId(UUID.randomUUID())
        .selectedPlanVersionId(UUID.randomUUID())
        .billingPeriod("MONTHLY")
        .subscriberType(SubscriberType.TENANT)
        .status(ContractStatus.PENDING_PAYMENT)
        .contractorEmail("admin@acme.com")
        .contractorFirstName("John").contractorLastName("Doe")
        .companySlug("acme")
        .verificationCode("123456")
        .verificationCodeExpiresAt(OffsetDateTime.now().plusMinutes(30))
        .expiresAt(OffsetDateTime.now().plusHours(48))
        .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
        .build();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(contract.getId(), "123456"))
        .isInstanceOf(IllegalStateException.class);
  }
}

