package io.cmartinezs.keygo.app.billing.contracting.usecase;

import io.cmartinezs.keygo.app.billing.contracting.exception.ContractNotFoundException;
import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.billing.contracting.port.AppContractRepositoryPort;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.PasswordHasherPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contractor.model.Contractor;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorStatus;
import io.cmartinezs.keygo.domain.billing.contracting.model.AppContract;
import io.cmartinezs.keygo.domain.billing.contracting.model.ContractStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.membership.model.AppRoleId;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerifyContractEmailUseCaseTest {

  @Mock AppContractRepositoryPort contractRepo;
  @Mock ClientAppRepositoryPort clientAppRepo;
  @Mock UserRepositoryPort userRepo;
  @Mock ContractorRepositoryPort contractorRepo;
  @Mock MembershipRepositoryPort membershipRepo;
  @Mock AppRoleRepositoryPort appRoleRepo;
  @Mock PasswordHasherPort passwordHasher;
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
   * Sets up standard stubs for clientAppRepo, userRepo, contractorRepo (called before verifyCode).
   * Returns the contractorId that will be resolved.
   */
  private UUID stubDownstreamDeps() {
    TenantId tenantId = TenantId.of(UUID.randomUUID());
    ClientApp providerApp = mock(ClientApp.class);
    when(providerApp.getTenantId()).thenReturn(tenantId);
    when(clientAppRepo.findById(any())).thenReturn(Optional.of(providerApp));

    UUID tenantUserId = UUID.randomUUID();
    User user = mock(User.class);
    when(user.getId()).thenReturn(UserId.of(tenantUserId));
    when(userRepo.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(user));

    UUID contractorId = UUID.randomUUID();
    Contractor contractor = Contractor.builder()
        .id(contractorId)
        .tenantUserId(tenantUserId)
        .status(ContractorStatus.PENDING)
        .build();
    when(contractorRepo.findByTenantUserId(any())).thenReturn(Optional.of(contractor));

    // Membership does not exist → will be created (lenient: some tests override this stub)
    lenient().when(membershipRepo.existsByUserAndClientApp(any(), any())).thenReturn(false);
    lenient().when(membershipRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // Stub admin_tenant role lookup (needed when membership is created)
    lenient().when(appRoleRepo.findByClientAppAndCode(any(), eq(RoleCode.adminTenantRole())))
        .thenAnswer(inv -> {
          UUID clientAppId = inv.getArgument(0);
          AppRole role = AppRole.builder()
              .id(AppRoleId.generate())
              .clientAppId(ClientAppId.of(clientAppId))
              .code(RoleCode.adminTenantRole())
              .displayName("Tenant Admin")
              .build();
          return Optional.of(role);
        });

    return contractorId;
  }

  /**
   * Stubs for the "new user" flow: user does not exist, must be created with a temporary password.
   */
  private UUID stubNewUserFlow() {
    TenantId tenantId = TenantId.of(UUID.randomUUID());
    ClientApp providerApp = mock(ClientApp.class);
    when(providerApp.getTenantId()).thenReturn(tenantId);
    when(clientAppRepo.findById(any())).thenReturn(Optional.of(providerApp));

    // User does not exist → triggers creation
    when(userRepo.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.empty());
    when(passwordHasher.hash(anyString())).thenReturn("$2a$10$hashedtemppassword");

    UUID tenantUserId = UUID.randomUUID();
    User savedUser = mock(User.class);
    when(savedUser.getId()).thenReturn(UserId.of(tenantUserId));
    when(userRepo.save(any())).thenReturn(savedUser);

    UUID contractorId = UUID.randomUUID();
    Contractor contractor = Contractor.builder()
        .id(contractorId)
        .tenantUserId(tenantUserId)
        .status(ContractorStatus.PENDING)
        .build();
    when(contractorRepo.findByTenantUserId(any())).thenReturn(Optional.of(contractor));

    // Membership does not exist → will be created (lenient: some tests override this stub)
    lenient().when(membershipRepo.existsByUserAndClientApp(any(), any())).thenReturn(false);
    lenient().when(membershipRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // Stub admin_tenant role lookup (needed when membership is created)
    lenient().when(appRoleRepo.findByClientAppAndCode(any(), eq(RoleCode.adminTenantRole())))
        .thenAnswer(inv -> {
          UUID clientAppId = inv.getArgument(0);
          AppRole role = AppRole.builder()
              .id(AppRoleId.generate())
              .clientAppId(ClientAppId.of(clientAppId))
              .code(RoleCode.adminTenantRole())
              .displayName("Tenant Admin")
              .build();
          return Optional.of(role);
        });

    return contractorId;
  }

  // ── Tests: usuario existente ───────────────────────────────────────────────

  @Test
  void execute_validCode_advancesToPendingPayment() {
    // Given
    String code = "123456";
    AppContract contract = pendingEmailContract(code, OffsetDateTime.now().plusMinutes(30));
    UUID contractId = contract.getId();
    UUID contractorId = stubDownstreamDeps();
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
  void execute_wrongCode_throwsIllegalArgument() {
    // Given — downstream deps are resolved before verifyCode throws
    AppContract contract = pendingEmailContract("123456", OffsetDateTime.now().plusMinutes(30));
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    stubDownstreamDeps();

    // When / Then
    assertThatThrownBy(() -> useCase.execute(contract.getId(), "999999"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Código de verificación inválido");
    verify(contractRepo, never()).save(any());
  }

  @Test
  void execute_expiredCode_throwsIllegalState() {
    // Given — code expired 1 minute ago; downstream deps still resolved before verifyCode throws
    AppContract contract = pendingEmailContract("123456", OffsetDateTime.now().minusMinutes(1));
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    stubDownstreamDeps();

    // When / Then
    assertThatThrownBy(() -> useCase.execute(contract.getId(), "123456"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("expirado");
    verify(contractRepo, never()).save(any());
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
  void execute_alreadyInPendingPayment_throwsIllegalState() {
    // Given — contract already verified (PENDING_PAYMENT); downstream deps resolved before verifyCode throws
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
    stubDownstreamDeps();

    // When / Then
    assertThatThrownBy(() -> useCase.execute(contract.getId(), "123456"))
        .isInstanceOf(IllegalStateException.class);
  }

  // ── Tests: usuario nuevo (RESET_PASSWORD + envío de contraseña temporal) ──

  @Test
  void execute_newUser_createsUserWithResetPasswordStatus() {
    // Given
    String code = "654321";
    AppContract contract = pendingEmailContract(code, OffsetDateTime.now().plusMinutes(30));
    stubNewUserFlow();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(contract.getId(), code);

    // Then — el nuevo usuario se guarda con status RESET_PASSWORD
    verify(userRepo).save(argThat(u -> UserStatus.RESET_PASSWORD.equals(u.getStatus())));
  }

  @Test
  void execute_newUser_sendsTemporaryPasswordEmail() {
    // Given
    String code = "654321";
    AppContract contract = pendingEmailContract(code, OffsetDateTime.now().plusMinutes(30));
    stubNewUserFlow();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(contract.getId(), code);

    // Then — se envía email de contraseña temporal al email del contratante
    verify(emailNotification).sendTemporaryPasswordEmail(
        eq("admin@acme.com"), anyString(), anyString());
  }

  @Test
  void execute_newUser_hashesPasswordBeforeSaving() {
    // Given
    String code = "654321";
    AppContract contract = pendingEmailContract(code, OffsetDateTime.now().plusMinutes(30));
    stubNewUserFlow();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(contract.getId(), code);

    // Then — se invoca el hasher antes de persistir la contraseña
    verify(passwordHasher).hash(anyString());
    verify(userRepo).save(argThat(u ->
        "$2a$10$hashedtemppassword".equals(u.getPasswordHash().value())));
  }

  @Test
  void execute_existingUser_doesNotSendTemporaryPasswordEmail() {
    // Given — usuario ya existe; no debe enviarse email de contraseña temporal
    String code = "123456";
    AppContract contract = pendingEmailContract(code, OffsetDateTime.now().plusMinutes(30));
    stubDownstreamDeps();
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(contract.getId(), code);

    // Then
    verify(emailNotification, never()).sendTemporaryPasswordEmail(anyString(), anyString(), anyString());
    verify(userRepo, never()).save(any());
  }

  // ── Tests: membership creation ────────────────────────────────────────────

  @Test
  void execute_validCode_createsMembershipWhenItDoesNotExist() {
    // Given
    String code = "123456";
    AppContract contract = pendingEmailContract(code, OffsetDateTime.now().plusMinutes(30));
    stubDownstreamDeps(); // membershipRepo.existsByUserAndClientApp → false
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(contract.getId(), code);

    // Then — membership saved with ACTIVE status
    verify(membershipRepo).save(argThat(m ->
        m instanceof Membership membership && membership.isActive()));
  }

  @Test
  void execute_validCode_doesNotCreateMembershipWhenAlreadyExists() {
    // Given
    String code = "123456";
    AppContract contract = pendingEmailContract(code, OffsetDateTime.now().plusMinutes(30));
    stubDownstreamDeps();
    // Override: membership already exists
    when(membershipRepo.existsByUserAndClientApp(any(), any())).thenReturn(true);
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(contract.getId(), code);

    // Then — membership NOT saved again
    verify(membershipRepo, never()).save(any());
  }

  @Test
  void execute_newUser_createsMembership() {
    // Given
    String code = "654321";
    AppContract contract = pendingEmailContract(code, OffsetDateTime.now().plusMinutes(30));
    stubNewUserFlow(); // membershipRepo.existsByUserAndClientApp → false
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(contract.getId(), code);

    // Then — membership saved with ACTIVE status for the newly created user
    verify(membershipRepo).save(any(Membership.class));
  }

  @Test
  void execute_validCode_membershipHasAdminTenantRole() {
    // Given
    String code = "123456";
    AppContract contract = pendingEmailContract(code, OffsetDateTime.now().plusMinutes(30));
    stubDownstreamDeps(); // membershipRepo.existsByUserAndClientApp → false
    when(contractRepo.findById(contract.getId())).thenReturn(Optional.of(contract));
    when(contractRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(contract.getId(), code);

    // Then — membership saved with the admin_tenant role assigned
    verify(membershipRepo).save(argThat(m ->
        m instanceof Membership membership &&
        membership.isActive() &&
        !membership.getRoles().isEmpty()));
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
