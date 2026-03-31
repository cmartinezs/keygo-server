package io.cmartinezs.keygo.api.billing.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.cmartinezs.keygo.api.billing.request.CreateAppContractRequest;
import io.cmartinezs.keygo.api.billing.request.VerifyContractEmailRequest;
import io.cmartinezs.keygo.app.billing.contracting.result.AppContractResult;
import io.cmartinezs.keygo.app.billing.contracting.usecase.ActivateAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.CreateAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.GetAppContractUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.MockApprovePaymentUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.ResendContractVerificationUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.ResumeContractOnboardingUseCase;
import io.cmartinezs.keygo.app.billing.contracting.usecase.VerifyContractEmailUseCase;
import io.cmartinezs.keygo.domain.billing.catalog.model.BillingPeriod;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AppBillingContractControllerTest {

  @Mock CreateAppContractUseCase createContractUseCase;
  @Mock GetAppContractUseCase getContractUseCase;
  @Mock MockApprovePaymentUseCase mockApprovePaymentUseCase;
  @Mock ActivateAppContractUseCase activateContractUseCase;
  @Mock VerifyContractEmailUseCase verifyContractEmailUseCase;
  @Mock ResumeContractOnboardingUseCase resumeContractOnboardingUseCase;
  @Mock ResendContractVerificationUseCase resendContractVerificationUseCase;

  @InjectMocks AppBillingContractController controller;

  // ── Helpers ───────────────────────────────────────────────────────────────

  private AppContract contract(ContractStatus status) {
    return AppContract.builder()
        .id(UUID.randomUUID())
        .clientAppId(UUID.randomUUID())
        .selectedPlanVersionId(UUID.randomUUID())
        .billingPeriod("MONTHLY")
        .status(status)
        .contractorEmail("admin@acme.com")
        .contractorFirstName("John")
        .contractorLastName("Doe")
        .companyName("ACME Corp")
        .expiresAt(OffsetDateTime.now().plusHours(48))
        .createdAt(OffsetDateTime.now())
        .updatedAt(OffsetDateTime.now())
        .build();
  }

  private AppContract contractWithCode(ContractStatus status, OffsetDateTime codeExpiresAt) {
    return AppContract.builder()
        .id(UUID.randomUUID())
        .clientAppId(UUID.randomUUID())
        .selectedPlanVersionId(UUID.randomUUID())
        .billingPeriod("MONTHLY")
        .status(status)
        .contractorEmail("admin@acme.com")
        .contractorFirstName("John")
        .contractorLastName("Doe")
        .companyName("ACME Corp")
        .verificationCode("123456")
        .verificationCodeExpiresAt(codeExpiresAt)
        .expiresAt(OffsetDateTime.now().plusHours(48))
        .createdAt(OffsetDateTime.now())
        .updatedAt(OffsetDateTime.now())
        .build();
  }

  // ── Tests — endpoints existentes ──────────────────────────────────────────

  @Test
  void createContract_happyPath_returns201() {
    // Given
    AppContract c = contract(ContractStatus.PENDING_EMAIL_VERIFICATION);
    when(createContractUseCase.execute(any())).thenReturn(new AppContractResult(c, null));

    CreateAppContractRequest request =
        new CreateAppContractRequest(
            UUID.randomUUID().toString(), // clientAppId
            UUID.randomUUID().toString(), // planVersionId
            BillingPeriod.MONTHLY,
            "admin@acme.com",
            "John",
            "Doe",
            "ACME Corp",
            "RFC123",
            "Calle 1");

    // When
    var response = controller.createContract(request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().status()).isEqualTo("PENDING_EMAIL_VERIFICATION");
  }

  @Test
  void getContract_existing_returns200() {
    // Given
    UUID contractId = UUID.randomUUID();
    AppContract c = contract(ContractStatus.PENDING_PAYMENT);
    when(getContractUseCase.execute(contractId)).thenReturn(new AppContractResult(c, null));

    // When
    var response = controller.getContract(contractId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().status()).isEqualTo("PENDING_PAYMENT");
  }

  @Test
  void mockApprovePayment_enabled_returns200() {
    // Given
    UUID contractId = UUID.randomUUID();
    AppContract c = contract(ContractStatus.READY_TO_ACTIVATE);
    when(mockApprovePaymentUseCase.isMockEnabled()).thenReturn(true);
    when(mockApprovePaymentUseCase.execute(contractId)).thenReturn(new AppContractResult(c, null));

    // When
    var response = controller.mockApprovePayment(contractId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().status()).isEqualTo("READY_TO_ACTIVATE");
  }

  @Test
  void mockApprovePayment_disabled_returns404() {
    // Given
    UUID contractId = UUID.randomUUID();
    when(mockApprovePaymentUseCase.isMockEnabled()).thenReturn(false);

    // When
    var response = controller.mockApprovePayment(contractId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    verify(mockApprovePaymentUseCase, never()).execute(any());
  }

  @Test
  void activateContract_happyPath_returns200WithActiveStatus() {
    // Given
    UUID contractId = UUID.randomUUID();
    AppContract c = contract(ContractStatus.ACTIVE);
    when(activateContractUseCase.execute(contractId)).thenReturn(new AppContractResult(c, null));

    // When
    var response = controller.activateContract(contractId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().status()).isEqualTo("ACTIVE");
  }

  @Test
  void verifyEmail_validCode_returns200WithPendingPaymentStatus() {
    // Given
    UUID contractId = UUID.randomUUID();
    AppContract c = contract(ContractStatus.PENDING_PAYMENT);
    when(verifyContractEmailUseCase.execute(contractId, "123456"))
        .thenReturn(new AppContractResult(c, null));

    // When
    var response = controller.verifyEmail(contractId, new VerifyContractEmailRequest("123456"));

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().status()).isEqualTo("PENDING_PAYMENT");
  }

  // ── Tests — resumeOnboarding ──────────────────────────────────────────────

  @Test
  void resumeOnboarding_pendingEmailVerification_withValidCode_returnsEnterCode() {
    // Given
    UUID contractId = UUID.randomUUID();
    // Código expira en el futuro → todavía válido
    AppContract c = contractWithCode(ContractStatus.PENDING_EMAIL_VERIFICATION,
        OffsetDateTime.now().plusMinutes(20));
    when(resumeContractOnboardingUseCase.execute(contractId))
        .thenReturn(new AppContractResult(c, null));

    // When
    var response = controller.resumeOnboarding(contractId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().status()).isEqualTo("PENDING_EMAIL_VERIFICATION");
    assertThat(response.getBody().getData().verificationCodeExpired()).isFalse();
    assertThat(response.getBody().getData().nextAction()).isEqualTo("ENTER_VERIFICATION_CODE");
  }

  @Test
  void resumeOnboarding_pendingEmailVerification_withExpiredCode_returnsRequestNewCode() {
    // Given
    UUID contractId = UUID.randomUUID();
    // Código expiró hace 5 minutos
    AppContract c = contractWithCode(ContractStatus.PENDING_EMAIL_VERIFICATION,
        OffsetDateTime.now().minusMinutes(5));
    when(resumeContractOnboardingUseCase.execute(contractId))
        .thenReturn(new AppContractResult(c, null));

    // When
    var response = controller.resumeOnboarding(contractId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().verificationCodeExpired()).isTrue();
    assertThat(response.getBody().getData().nextAction()).isEqualTo("REQUEST_NEW_CODE");
  }

  @Test
  void resumeOnboarding_pendingPayment_returnsCompletePaymentAction() {
    // Given
    UUID contractId = UUID.randomUUID();
    AppContract c = contract(ContractStatus.PENDING_PAYMENT);
    when(resumeContractOnboardingUseCase.execute(contractId))
        .thenReturn(new AppContractResult(c, null));

    // When
    var response = controller.resumeOnboarding(contractId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().nextAction()).isEqualTo("COMPLETE_PAYMENT");
    assertThat(response.getBody().getData().verificationCodeExpired()).isFalse();
  }

  @Test
  void resumeOnboarding_readyToActivate_returnsActivateAction() {
    // Given
    UUID contractId = UUID.randomUUID();
    AppContract c = contract(ContractStatus.READY_TO_ACTIVATE);
    when(resumeContractOnboardingUseCase.execute(contractId))
        .thenReturn(new AppContractResult(c, null));

    // When
    var response = controller.resumeOnboarding(contractId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().nextAction()).isEqualTo("ACTIVATE");
  }

  @Test
  void resumeOnboarding_active_returnsCompleteAction() {
    // Given
    UUID contractId = UUID.randomUUID();
    AppContract c = contract(ContractStatus.ACTIVE);
    when(resumeContractOnboardingUseCase.execute(contractId))
        .thenReturn(new AppContractResult(c, null));

    // When
    var response = controller.resumeOnboarding(contractId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().nextAction()).isEqualTo("COMPLETE");
  }

  // ── Tests — resendVerification ────────────────────────────────────────────

  @Test
  void resendVerification_pendingEmailVerification_returns200() {
    // Given
    UUID contractId = UUID.randomUUID();
    AppContract c = contractWithCode(ContractStatus.PENDING_EMAIL_VERIFICATION,
        OffsetDateTime.now().plusMinutes(20));
    when(resendContractVerificationUseCase.execute(contractId))
        .thenReturn(new AppContractResult(c, null));

    // When
    var response = controller.resendVerification(contractId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().status()).isEqualTo("PENDING_EMAIL_VERIFICATION");
  }

  @Test
  void resendVerification_expiredCode_returns200AfterRenewal() {
    // Given
    UUID contractId = UUID.randomUUID();
    // El use case devuelve el contrato con código renovado — para el controller solo importa el status
    AppContract c = contractWithCode(ContractStatus.PENDING_EMAIL_VERIFICATION,
        OffsetDateTime.now().plusMinutes(30)); // nuevo código con nueva expiración
    when(resendContractVerificationUseCase.execute(contractId))
        .thenReturn(new AppContractResult(c, null));

    // When
    var response = controller.resendVerification(contractId);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().status()).isEqualTo("PENDING_EMAIL_VERIFICATION");
  }
}


