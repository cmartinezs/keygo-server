package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.contracting.exception.ContractNotFoundException;
import io.cmartinezs.keygo.domain.billing.contracting.exception.ContractStateViolationException;
import io.cmartinezs.keygo.domain.billing.contracting.exception.ContractVerificationCodeInvalidException;
import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.PlatformUserRoleRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contractor.model.Contractor;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorStatus;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyContractEmailUseCaseTest {

  @Mock AppContractRepositoryPort contractRepo;
  @Mock PlatformUserRepositoryPort platformUserRepo;
  @Mock PlatformUserRoleRepositoryPort platformUserRoleRepo;
  @Mock ContractorRepositoryPort contractorRepo;
  @Mock CredentialEncoderPort credentialEncoder;
  @Mock EmailNotificationPort emailNotification;

  @InjectMocks
  VerifyContractEmailUseCase useCase;

  // ── Helpers ───────────────────────────────────────────────────────────────

  private AppContract pendingEmailContract(String code, OffsetDateTime codeExpiry) {
    return AppContract.builder()
        .id(UUID.randomUUID())
        .clientAppId(UUID.randomUUID())
        .selectedPlanVersionId(UUID.randomUUID())
        .billingPeriod("MONTHLY")
        .status(ContractStatus.PENDING_EMAIL_VERIFICATION)
        .contractorEmail("admin@acme.com")
        .contractorFirstName("John").contractorLastName("Doe")
        .verificationCode(code)
        .verificationCodeExpiresAt(codeExpiry)
        .expiresAt(OffsetDateTime.now().plusHours(48))
        .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
        .build();
  }

  /**
   * Stubs for the "existing platform user" flow.
   * Returns the contractorId that will be resolved.
   */
  private UUID stubExistingPlatformUserFlow() {
    UUID platformUserId = UUID.randomUUID();
    PlatformUser user = mock(PlatformUser.class);
    when(user.getId()).thenReturn(UserId.of(platformUserId));
    when(platformUserRepo.findByEmail(any())).thenReturn(Optional.of(user));

    // Roles already assigned
    lenient().when(platformUserRoleRepo.hasRole(platformUserId, "keygo_user")).thenReturn(true);
    lenient().when(platformUserRoleRepo.hasRole(platformUserId, "keygo_tenant_admin")).thenReturn(true);

    UUID contractorId = UUID.randomUUID();
    Contractor contractor = Contractor.builder()
        .id(contractorId)
        .platformUserId(platformUserId)
        .status(ContractorStatus.PENDING)
        .build();
    when(contractorRepo.findByPlatformUserId(platformUserId)).thenReturn(Optional.of(contractor));

    return contractorId;
  }

  /**
   * Stubs for the "new platform user" flow: user does not exist, must be created with a temporary password.
   */
  private UUID stubNewPlatformUserFlow() {
    // PlatformUser does not exist → triggers creation
    when(platformUserRepo.findByEmail(any())).thenReturn(Optional.empty());
    when(credentialEncoder.encode(anyString())).thenReturn("$2a$10$hashedtemppassword");

    UUID platformUserId = UUID.randomUUID();
    PlatformUser savedUser = mock(PlatformUser.class);
    when(savedUser.getId()).thenReturn(UserId.of(platformUserId));
    when(platformUserRepo.save(any())).thenReturn(savedUser);

    // Roles not assigned yet
    lenient().when(platformUserRoleRepo.hasRole(platformUserId, "keygo_user")).thenReturn(false);
    lenient().when(platformUserRoleRepo.hasRole(platformUserId, "keygo_tenant_admin")).thenReturn(false);

    UUID contractorId = UUID.randomUUID();
    Contractor contractor = Contractor.builder()
        .id(contractorId)
        .platformUserId(platformUserId)
        .status(ContractorStatus.PENDING)
        .build();
    when(contractorRepo.findByPlatformUserId(platformUserId)).thenReturn(Optional.of(contractor));

    return contractorId;
  }

  // ── Tests: existing platform user ─────────────────────────────────────────

  @Test
  void execute_validCode_advancesToPendingPayment() {
    // Given
    String code = "123456";
    AppContract contract = pendingEmailContract(code, OffsetDateTime.now().plusMinutes(30));
    UUID contractId = contract.getId();
    UUID contractorId = stubExistingPlatformUserFlow();
    when(contractRepo.findById(contractId)).thenReturn(Optional.of(contract));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    var result = useCase.execute(contractId, code);

    // Then
    assertThat(result.contract().getStatus()).isEqualTo(ContractStatus.PENDING_PAYMENT);
    assertThat(result.contract().isEmailVerified()).isTrue();
    assertThat(result.contract().getContractorId()).isEqualTo(contractorId);
    verify(contractRepo).save(any());
  }

  @Test
  void execute_wrongCode_throwsCodeInvalidException_noUserCreated() {
    // Given — code validated BEFORE any user/contractor creation
    AppContract contract = pendingEmailContract("123456", OffsetDateTime.now().plusMinutes(30));
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(contract.getId(), "999999"))
        .isInstanceOf(ContractVerificationCodeInvalidException.class)
        .hasMessageContaining("invalid");
    verify(contractRepo, never()).save(any());
    verify(platformUserRepo, never()).save(any());
    verify(emailNotification, never()).sendEmail(any(), any(), any(), any());
  }

  @Test
  void execute_expiredCode_throwsCodeInvalidException_noUserCreated() {
    // Given — code expired 1 minute ago; validated BEFORE user creation
    AppContract contract = pendingEmailContract("123456", OffsetDateTime.now().minusMinutes(1));
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(contract.getId(), "123456"))
        .isInstanceOf(ContractVerificationCodeInvalidException.class)
        .hasMessageContaining("expired");
    verify(contractRepo, never()).save(any());
    verify(platformUserRepo, never()).save(any());
    verify(emailNotification, never()).sendEmail(any(), any(), any(), any());
  }

  @Test
  void execute_contractNotFound_throwsContractNotFoundException() {
    // Given — fails immediately, no downstream deps needed
    UUID contractId = UUID.randomUUID();
    when(contractRepo.findById(contractId)).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(contractId, "123456"))
        .isInstanceOf(ContractNotFoundException.class)
        .hasMessageContaining(contractId.toString());
  }

  @Test
  void execute_alreadyInPendingPayment_throwsContractStateViolation_noUserCreated() {
    // Given — contract already verified (PENDING_PAYMENT); validated BEFORE user creation
    AppContract contract = AppContract.builder()
        .id(UUID.randomUUID())
        .clientAppId(UUID.randomUUID())
        .selectedPlanVersionId(UUID.randomUUID())
        .billingPeriod("MONTHLY")
        .status(ContractStatus.PENDING_PAYMENT)
        .contractorEmail("admin@acme.com")
        .contractorFirstName("John").contractorLastName("Doe")
        .verificationCode("123456")
        .verificationCodeExpiresAt(OffsetDateTime.now().plusMinutes(30))
        .expiresAt(OffsetDateTime.now().plusHours(48))
        .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
        .build();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(contract.getId(), "123456"))
        .isInstanceOf(ContractStateViolationException.class);
    verify(platformUserRepo, never()).save(any());
    verify(emailNotification, never()).sendEmail(any(), any(), any(), any());
  }

  // ── Tests: new platform user (RESET_PASSWORD + temp password email) ───────

  @Test
  void execute_newUser_createsPlatformUserWithResetPasswordStatus() {
    // Given
    String code = "654321";
    AppContract contract = pendingEmailContract(code, OffsetDateTime.now().plusMinutes(30));
    stubNewPlatformUserFlow();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(contract.getId(), code);

    // Then — the new platform user is saved with status RESET_PASSWORD
    verify(platformUserRepo).save(argThat(u -> UserStatus.RESET_PASSWORD.equals(u.getStatus())));
  }

  @Test
  void execute_newUser_sendsTemporaryPasswordEmail() {
    // Given
    String code = "654321";
    AppContract contract = pendingEmailContract(code, OffsetDateTime.now().plusMinutes(30));
    stubNewPlatformUserFlow();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(contract.getId(), code);

    // Then — temp password email sent
    verify(emailNotification).sendEmail(
        eq(EmailNotificationPort.TYPE_TEMPORARY_PASSWORD), anyString(), anyString(), any(Map.class));
  }

  @Test
  void execute_newUser_hashesPasswordBeforeSaving() {
    // Given
    String code = "654321";
    AppContract contract = pendingEmailContract(code, OffsetDateTime.now().plusMinutes(30));
    stubNewPlatformUserFlow();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(contract.getId(), code);

    // Then — credential encoder invoked
    verify(credentialEncoder).encode(anyString());
  }

  @Test
  void execute_existingUser_doesNotSendTemporaryPasswordEmail() {
    // Given — user already exists; no temp password email
    String code = "123456";
    AppContract contract = pendingEmailContract(code, OffsetDateTime.now().plusMinutes(30));
    stubExistingPlatformUserFlow();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(contract.getId(), code);

    // Then
    verify(emailNotification, never()).sendEmail(any(), any(), any(), any());
    verify(platformUserRepo, never()).save(any());
  }

  // ── Tests: platform role assignment ───────────────────────────────────────

  @Test
  void execute_newUser_assignsPlatformRoles() {
    // Given
    String code = "654321";
    AppContract contract = pendingEmailContract(code, OffsetDateTime.now().plusMinutes(30));
    stubNewPlatformUserFlow();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(contract.getId(), code);

    // Then — both platform roles are assigned
    verify(platformUserRoleRepo).assign(any(UUID.class), eq("keygo_user"));
    verify(platformUserRoleRepo).assign(any(UUID.class), eq("keygo_tenant_admin"));
  }

  @Test
  void execute_existingUser_doesNotReassignRoles() {
    // Given — roles already assigned
    String code = "123456";
    AppContract contract = pendingEmailContract(code, OffsetDateTime.now().plusMinutes(30));
    stubExistingPlatformUserFlow();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(contract.getId(), code);

    // Then — roles NOT assigned again (hasRole returned true)
    verify(platformUserRoleRepo, never()).assign(any(UUID.class), anyString());
  }

  // ── Tests: generateTemporaryPassword ──────────────────────────────────────

  @Test
  void generateTemporaryPassword_hasCorrectLength() {
    // When
    String pwd = useCase.generateTemporaryPassword();

    // Then
    assertThat(pwd).hasSize(14);
  }

  @Test
  void generateTemporaryPassword_containsAtLeastOneUppercase() {
    // When
    String pwd = useCase.generateTemporaryPassword();

    // Then
    assertThat(pwd).matches(".*[A-Z].*");
  }

  @Test
  void generateTemporaryPassword_containsAtLeastOneLowercase() {
    // When
    String pwd = useCase.generateTemporaryPassword();

    // Then
    assertThat(pwd).matches(".*[a-z].*");
  }

  @Test
  void generateTemporaryPassword_containsAtLeastOneDigit() {
    // When
    String pwd = useCase.generateTemporaryPassword();

    // Then
    assertThat(pwd).matches(".*[0-9].*");
  }

  @Test
  void generateTemporaryPassword_containsAtLeastOneSpecialChar() {
    // When
    String pwd = useCase.generateTemporaryPassword();

    // Then
    assertThat(pwd).matches(".*[!@#$%&*].*");
  }

  @Test
  void generateTemporaryPassword_twoCallsReturnDifferentValues() {
    // When
    String pwd1 = useCase.generateTemporaryPassword();
    String pwd2 = useCase.generateTemporaryPassword();

    // Then — with 70^14 combinations, collision probability is negligible
    assertThat(pwd1).isNotEqualTo(pwd2);
  }
}
