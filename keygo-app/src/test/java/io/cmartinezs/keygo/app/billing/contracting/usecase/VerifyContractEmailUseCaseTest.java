package io.cmartinezs.keygo.app.billing.contracting.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.billing.contracting.exception.ContractNotFoundException;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.port.ContractEmailVerificationRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contracting.exception.ContractStateViolationException;
import io.cmartinezs.keygo.domain.billing.contracting.exception.ContractVerificationCodeInvalidException;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractEmailVerification;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerifyContractEmailUseCaseTest {

  @Mock AppContractRepositoryPort contractRepo;
  @Mock ContractEmailVerificationRepositoryPort contractVerificationRepo;

  @InjectMocks VerifyContractEmailUseCase useCase;

  private AppContract pendingEmailContract() {
    return AppContract.builder()
        .id(UUID.randomUUID())
        .clientAppId(UUID.randomUUID())
        .selectedPlanVersionId(UUID.randomUUID())
        .billingPeriod("MONTHLY")
        .status(ContractStatus.PENDING_EMAIL_VERIFICATION)
        .contractorEmail("admin@acme.com")
        .contractorFirstName("John")
        .contractorLastName("Doe")
        .expiresAt(OffsetDateTime.now().plusHours(48))
        .createdAt(OffsetDateTime.now())
        .updatedAt(OffsetDateTime.now())
        .build();
  }

  private ContractEmailVerification verificationFor(UUID contractId, String code, OffsetDateTime expiresAt) {
    return ContractEmailVerification.builder()
        .contractId(contractId)
        .code(code)
        .expiresAt(expiresAt)
        .build();
  }

  @Test
  void execute_validCode_marksVerificationUsedAndAdvancesToPendingPayment() {
    AppContract contract = pendingEmailContract();
    ContractEmailVerification verification =
        verificationFor(contract.getId(), "123456", OffsetDateTime.now().plusMinutes(30));
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractVerificationRepo.findByContractId(contract.getId())).thenReturn(Optional.of(verification));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var result = useCase.execute(contract.getId(), "123456");

    assertThat(result.contract().getStatus()).isEqualTo(ContractStatus.PENDING_PAYMENT);
    assertThat(result.contract().isEmailVerified()).isTrue();
    assertThat(verification.isUsed()).isTrue();
    verify(contractVerificationRepo).markUsed(verification);
    verify(contractRepo).save(contract);
  }

  @Test
  void execute_wrongCode_throwsInvalidCodeWithoutSaving() {
    AppContract contract = pendingEmailContract();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractVerificationRepo.findByContractId(contract.getId()))
        .thenReturn(Optional.of(verificationFor(contract.getId(), "123456", OffsetDateTime.now().plusMinutes(30))));

    assertThatThrownBy(() -> useCase.execute(contract.getId(), "999999"))
        .isInstanceOf(ContractVerificationCodeInvalidException.class);

    verify(contractVerificationRepo, never()).markUsed(any());
    verify(contractRepo, never()).save(any());
  }

  @Test
  void execute_expiredCode_throwsInvalidCodeWithoutSaving() {
    AppContract contract = pendingEmailContract();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractVerificationRepo.findByContractId(contract.getId()))
        .thenReturn(Optional.of(verificationFor(contract.getId(), "123456", OffsetDateTime.now().minusMinutes(1))));

    assertThatThrownBy(() -> useCase.execute(contract.getId(), "123456"))
        .isInstanceOf(ContractVerificationCodeInvalidException.class)
        .hasMessageContaining("expired");

    verify(contractVerificationRepo, never()).markUsed(any());
    verify(contractRepo, never()).save(any());
  }

  @Test
  void execute_missingVerification_throwsInvalidCode() {
    AppContract contract = pendingEmailContract();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractVerificationRepo.findByContractId(contract.getId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(contract.getId(), "123456"))
        .isInstanceOf(ContractVerificationCodeInvalidException.class)
        .hasMessageContaining("not found");
  }

  @Test
  void execute_contractNotFound_throwsContractNotFoundException() {
    UUID contractId = UUID.randomUUID();
    when(contractRepo.findById(contractId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(contractId, "123456"))
        .isInstanceOf(ContractNotFoundException.class)
        .hasMessageContaining(contractId.toString());
  }

  @Test
  void execute_invalidState_throwsContractStateViolationException() {
    AppContract contract = AppContract.builder()
        .id(UUID.randomUUID())
        .clientAppId(UUID.randomUUID())
        .selectedPlanVersionId(UUID.randomUUID())
        .billingPeriod("MONTHLY")
        .status(ContractStatus.PENDING_PAYMENT)
        .contractorEmail("admin@acme.com")
        .contractorFirstName("John")
        .contractorLastName("Doe")
        .expiresAt(OffsetDateTime.now().plusHours(48))
        .createdAt(OffsetDateTime.now())
        .updatedAt(OffsetDateTime.now())
        .build();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));

    assertThatThrownBy(() -> useCase.execute(contract.getId(), "123456"))
        .isInstanceOf(ContractStateViolationException.class);

    verify(contractVerificationRepo, never()).findByContractId(any());
    verify(contractRepo, never()).save(any());
  }
}
