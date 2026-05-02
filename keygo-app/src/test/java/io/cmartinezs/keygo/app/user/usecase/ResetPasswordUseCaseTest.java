package io.cmartinezs.keygo.app.user.usecase;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.ResetPasswordCommand;
import io.cmartinezs.keygo.app.user.exception.IncorrectCurrentPasswordException;
import io.cmartinezs.keygo.app.user.exception.PasswordMismatchException;
import io.cmartinezs.keygo.app.user.exception.UserNotInResetPasswordStatusException;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.exception.VerificationCodeInvalidException;
import io.cmartinezs.keygo.domain.user.exception.VerificationCodeExpiredException;
import io.cmartinezs.keygo.domain.user.exception.PasswordResetRequestNotFoundException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.VerificationCode;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class ResetPasswordUseCaseTest {
  private static final String TENANT_SLUG   = "acme";
  private static final String USER_EMAIL    = "john@acme.com";
  private static final String TEMP_HASH     = "$2a$10$temphash";
  private static final String NEW_HASH      = "$2a$10$newhash";
  private static final String VALID_NEW_PWD = "NewSecure@2026!";
  private static final String VALID_CODE    = "123456";
  @Mock TenantRepositoryPort            tenantRepositoryPort;
  @Mock UserRepositoryPort              userRepositoryPort;
  @Mock CredentialEncoderPort              credentialEncoderPort;
  @Mock VerificationCodeRepositoryPort codeRepositoryPort;
  private ResetPasswordUseCase useCase;
  private Tenant activeTenant;
  private User   resetPasswordUser;
  private UserId userId;
  private UUID   requestId;
  @BeforeEach
  void setUp() {
    useCase = new ResetPasswordUseCase(
        tenantRepositoryPort, userRepositoryPort, credentialEncoderPort, codeRepositoryPort);
    activeTenant = Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME")
        .status(TenantStatus.ACTIVE).build();
    userId = UserId.generate();
    resetPasswordUser = User.builder()
        .id(userId)
        .tenantId(activeTenant.getId())
        .username(Username.of("johndoe"))
        .email(EmailAddress.of(USER_EMAIL))
        .passwordHash(PasswordHash.of(TEMP_HASH))
        .firstName("John").lastName("Doe")
        .status(UserStatus.ACTIVE).build();
    resetPasswordUser.requirePasswordReset();
    requestId = UUID.randomUUID();
  }
  private ResetPasswordCommand validCommand() {
    return new ResetPasswordCommand(
        TENANT_SLUG, requestId.toString(), "tempPass123!", VALID_NEW_PWD, VALID_NEW_PWD, VALID_CODE);
  }
  private VerificationCode activeCode() {
    return VerificationCode.reconstitute(
        requestId, userId, VerificationPurpose.PASSWORD_RESET, VALID_CODE,
        Instant.now().plus(15, ChronoUnit.MINUTES), null, Instant.now());
  }
  @Test
  void resetPassword_succeeds_andActivatesUser() {
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(codeRepositoryPort.findById(requestId)).thenReturn(Optional.of(activeCode()));
    when(userRepositoryPort.findByTenantIdAndPlatformUserId(any(), eq(userId))).thenReturn(Optional.of(resetPasswordUser));
    when(credentialEncoderPort.matches("tempPass123!", TEMP_HASH)).thenReturn(true);
    when(credentialEncoderPort.encode(VALID_NEW_PWD)).thenReturn(NEW_HASH);
    when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    var result = useCase.execute(validCommand());
    assertThat(result.reset()).isTrue();
    assertThat(resetPasswordUser.getPasswordHash().value()).isEqualTo(NEW_HASH);
    assertThat(resetPasswordUser.isActive()).isTrue();
    verify(userRepositoryPort).save(resetPasswordUser);
    verify(codeRepositoryPort).markUsed(any());
  }
  @Test
  void resetPassword_throwsWhenTenantNotFound() {
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> useCase.execute(validCommand()))
        .isInstanceOf(TenantNotFoundException.class);
  }
  @Test
  void resetPassword_throwsWhenRequestIdNotFound() {
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(codeRepositoryPort.findById(requestId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> useCase.execute(validCommand()))
        .isInstanceOf(PasswordResetRequestNotFoundException.class);
    verify(userRepositoryPort, never()).findByTenantIdAndPlatformUserId(any(), any());
  }
  @Test
  void resetPassword_throwsWhenRequestIdIsNotValidUUID() {
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    var command = new ResetPasswordCommand(
        TENANT_SLUG, "not-a-uuid", "tempPass123!", VALID_NEW_PWD, VALID_NEW_PWD, VALID_CODE);
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(PasswordResetRequestNotFoundException.class);
  }
  @Test
  void resetPassword_throwsWhenCodeAlreadyUsed() {
    VerificationCode usedCode = VerificationCode.reconstitute(
        requestId, userId, VerificationPurpose.PASSWORD_RESET, VALID_CODE,
        Instant.now().plus(15, ChronoUnit.MINUTES),
        Instant.now().minus(1, ChronoUnit.MINUTES), Instant.now());
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(codeRepositoryPort.findById(requestId)).thenReturn(Optional.of(usedCode));
    assertThatThrownBy(() -> useCase.execute(validCommand()))
        .isInstanceOf(VerificationCodeInvalidException.class);
    verify(userRepositoryPort, never()).findByTenantIdAndPlatformUserId(any(), any());
    verify(userRepositoryPort, never()).save(any());
  }
  @Test
  void resetPassword_throwsWhenCodeExpired() {
    VerificationCode expiredCode = VerificationCode.reconstitute(
        requestId, userId, VerificationPurpose.PASSWORD_RESET, VALID_CODE,
        Instant.now().minus(1, ChronoUnit.MINUTES), null,
        Instant.now().minus(16, ChronoUnit.MINUTES));
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(codeRepositoryPort.findById(requestId)).thenReturn(Optional.of(expiredCode));
    assertThatThrownBy(() -> useCase.execute(validCommand()))
        .isInstanceOf(VerificationCodeExpiredException.class);
    verify(userRepositoryPort, never()).findByTenantIdAndPlatformUserId(any(), any());
    verify(userRepositoryPort, never()).save(any());
  }
  @Test
  void resetPassword_throwsWhenCodeIncorrect() {
    VerificationCode wrongCode = VerificationCode.reconstitute(
        requestId, userId, VerificationPurpose.PASSWORD_RESET, "999999",
        Instant.now().plus(15, ChronoUnit.MINUTES), null, Instant.now());
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(codeRepositoryPort.findById(requestId)).thenReturn(Optional.of(wrongCode));
    assertThatThrownBy(() -> useCase.execute(validCommand()))
        .isInstanceOf(VerificationCodeInvalidException.class);
    verify(userRepositoryPort, never()).findByTenantIdAndPlatformUserId(any(), any());
    verify(userRepositoryPort, never()).save(any());
  }
  @Test
  void resetPassword_throwsWhenUserDoesNotBelongToTenant() {
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(codeRepositoryPort.findById(requestId)).thenReturn(Optional.of(activeCode()));
    when(userRepositoryPort.findByTenantIdAndPlatformUserId(any(), any())).thenReturn(Optional.empty());
    assertThatThrownBy(() -> useCase.execute(validCommand()))
        .isInstanceOf(UserNotFoundException.class);
    verify(userRepositoryPort, never()).save(any());
  }
  @Test
  void resetPassword_throwsWhenUserNotInResetPasswordStatus() {
    resetPasswordUser.activate();
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(codeRepositoryPort.findById(requestId)).thenReturn(Optional.of(activeCode()));
    when(userRepositoryPort.findByTenantIdAndPlatformUserId(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    assertThatThrownBy(() -> useCase.execute(validCommand()))
        .isInstanceOf(UserNotInResetPasswordStatusException.class);
    verify(credentialEncoderPort, never()).matches(any(), any());
    verify(userRepositoryPort, never()).save(any());
  }
  @Test
  void resetPassword_throwsWhenTemporaryPasswordIncorrect() {
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(codeRepositoryPort.findById(requestId)).thenReturn(Optional.of(activeCode()));
    when(userRepositoryPort.findByTenantIdAndPlatformUserId(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    when(credentialEncoderPort.matches("tempPass123!", TEMP_HASH)).thenReturn(false);
    assertThatThrownBy(() -> useCase.execute(validCommand()))
        .isInstanceOf(IncorrectCurrentPasswordException.class);
    verify(userRepositoryPort, never()).save(any());
  }
  @Test
  void resetPassword_throwsWhenPasswordsDoNotMatch() {
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(codeRepositoryPort.findById(requestId)).thenReturn(Optional.of(activeCode()));
    when(userRepositoryPort.findByTenantIdAndPlatformUserId(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    when(credentialEncoderPort.matches("tempPass123!", TEMP_HASH)).thenReturn(true);
    var command = new ResetPasswordCommand(
        TENANT_SLUG, requestId.toString(), "tempPass123!", VALID_NEW_PWD, "DifferentPassword@1!", VALID_CODE);
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(PasswordMismatchException.class);
    verify(userRepositoryPort, never()).save(any());
  }
  @Test
  void resetPassword_throwsWhenNewPasswordViolatesPolicy() {
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(codeRepositoryPort.findById(requestId)).thenReturn(Optional.of(activeCode()));
    when(userRepositoryPort.findByTenantIdAndPlatformUserId(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    when(credentialEncoderPort.matches("tempPass123!", TEMP_HASH)).thenReturn(true);
    var command = new ResetPasswordCommand(
        TENANT_SLUG, requestId.toString(), "tempPass123!", "weak", "weak", VALID_CODE);
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(io.cmartinezs.keygo.domain.user.exception.InvalidPasswordException.class);
    verify(userRepositoryPort, never()).save(any());
  }
}
