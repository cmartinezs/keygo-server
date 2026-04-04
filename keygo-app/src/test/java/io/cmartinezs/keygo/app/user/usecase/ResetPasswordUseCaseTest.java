package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.ResetPasswordCommand;
import io.cmartinezs.keygo.app.user.exception.IncorrectCurrentPasswordException;
import io.cmartinezs.keygo.app.user.exception.UserNotInResetPasswordStatusException;
import io.cmartinezs.keygo.app.user.port.PasswordHasherPort;
import io.cmartinezs.keygo.app.user.port.PasswordResetCodeRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.exception.InvalidPasswordResetCodeException;
import io.cmartinezs.keygo.domain.user.exception.PasswordResetCodeExpiredException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PasswordResetCode;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResetPasswordUseCaseTest {

  private static final String TENANT_SLUG    = "acme";
  private static final String USER_EMAIL     = "john@acme.com";
  private static final String TEMP_HASH      = "$2a$10$temphash";
  private static final String NEW_HASH       = "$2a$10$newhash";
  private static final String VALID_NEW_PWD  = "NewSecure@2026!";
  private static final String VALID_CODE     = "123456";

  @Mock TenantRepositoryPort             tenantRepositoryPort;
  @Mock UserRepositoryPort               userRepositoryPort;
  @Mock PasswordHasherPort               passwordHasherPort;
  @Mock PasswordResetCodeRepositoryPort  codeRepositoryPort;

  private ResetPasswordUseCase useCase;
  private Tenant  activeTenant;
  private User    resetPasswordUser;
  private UserId  userId;

  @BeforeEach
  void setUp() {
    useCase = new ResetPasswordUseCase(
        tenantRepositoryPort, userRepositoryPort, passwordHasherPort, codeRepositoryPort);

    activeTenant = Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME").ownerEmail("o@acme.com")
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
    resetPasswordUser.requirePasswordReset();  // → RESET_PASSWORD
  }

  // ─── Helper builders ──────────────────────────────────────────────────────

  private ResetPasswordCommand validCommand() {
    return new ResetPasswordCommand(
        TENANT_SLUG, USER_EMAIL, "tempPass123!", VALID_NEW_PWD, VALID_NEW_PWD, VALID_CODE);
  }

  private PasswordResetCode activeCode() {
    return PasswordResetCode.reconstitute(
        UUID.randomUUID(), userId, VALID_CODE,
        Instant.now().plus(15, ChronoUnit.MINUTES), null, Instant.now());
  }

  // ─── Happy path ───────────────────────────────────────────────────────────

  @Test
  void resetPassword_succeeds_andActivatesUser() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    when(passwordHasherPort.matches("tempPass123!", TEMP_HASH)).thenReturn(true);
    when(passwordHasherPort.hash(VALID_NEW_PWD)).thenReturn(NEW_HASH);
    when(codeRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(activeCode()));
    when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    var result = useCase.execute(validCommand());

    // Then
    assertThat(result.reset()).isTrue();
    assertThat(resetPasswordUser.getPasswordHash().value()).isEqualTo(NEW_HASH);
    assertThat(resetPasswordUser.isActive()).isTrue();
    verify(userRepositoryPort).save(resetPasswordUser);
    verify(codeRepositoryPort).markUsed(any());
  }

  // ─── User / tenant not found ──────────────────────────────────────────────

  @Test
  void resetPassword_throwsWhenTenantNotFound() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(validCommand()))
        .isInstanceOf(TenantNotFoundException.class);
  }

  @Test
  void resetPassword_throwsWhenUserNotFound() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(validCommand()))
        .isInstanceOf(UserNotFoundException.class);
  }

  // ─── Status / password validations ───────────────────────────────────────

  @Test
  void resetPassword_throwsWhenUserNotInResetPasswordStatus() {
    // Given — user is ACTIVE, not RESET_PASSWORD
    resetPasswordUser.activate();
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(resetPasswordUser));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(validCommand()))
        .isInstanceOf(UserNotInResetPasswordStatusException.class);

    verify(passwordHasherPort, never()).matches(any(), any());
    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  void resetPassword_throwsWhenTemporaryPasswordIncorrect() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    when(passwordHasherPort.matches("tempPass123!", TEMP_HASH)).thenReturn(false);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(validCommand()))
        .isInstanceOf(IncorrectCurrentPasswordException.class);

    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  void resetPassword_throwsWhenPasswordsDoNotMatch() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    when(passwordHasherPort.matches("tempPass123!", TEMP_HASH)).thenReturn(true);

    var command = new ResetPasswordCommand(
        TENANT_SLUG, USER_EMAIL, "tempPass123!", VALID_NEW_PWD, "DifferentPassword@1!", VALID_CODE);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("coinciden");

    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  void resetPassword_throwsWhenNewPasswordViolatesPolicy() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    when(passwordHasherPort.matches("tempPass123!", TEMP_HASH)).thenReturn(true);

    var command = new ResetPasswordCommand(
        TENANT_SLUG, USER_EMAIL, "tempPass123!", "weak", "weak", VALID_CODE);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class);

    verify(userRepositoryPort, never()).save(any());
  }

  // ─── Verification code validations ───────────────────────────────────────

  @Test
  void resetPassword_throwsWhenCodeNotFound() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    when(passwordHasherPort.matches("tempPass123!", TEMP_HASH)).thenReturn(true);
    when(codeRepositoryPort.findByUserId(userId)).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(validCommand()))
        .isInstanceOf(InvalidPasswordResetCodeException.class);

    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  void resetPassword_throwsWhenCodeExpired() {
    // Given
    PasswordResetCode expiredCode = PasswordResetCode.reconstitute(
        UUID.randomUUID(), userId, VALID_CODE,
        Instant.now().minus(1, ChronoUnit.MINUTES), null, Instant.now().minus(16, ChronoUnit.MINUTES));

    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    when(passwordHasherPort.matches("tempPass123!", TEMP_HASH)).thenReturn(true);
    when(codeRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(expiredCode));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(validCommand()))
        .isInstanceOf(PasswordResetCodeExpiredException.class);

    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  void resetPassword_throwsWhenCodeIncorrect() {
    // Given
    PasswordResetCode wrongCode = PasswordResetCode.reconstitute(
        UUID.randomUUID(), userId, "999999",
        Instant.now().plus(15, ChronoUnit.MINUTES), null, Instant.now());

    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    when(passwordHasherPort.matches("tempPass123!", TEMP_HASH)).thenReturn(true);
    when(codeRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(wrongCode));

    // When / Then — VALID_CODE="123456" but stored code is "999999"
    assertThatThrownBy(() -> useCase.execute(validCommand()))
        .isInstanceOf(InvalidPasswordResetCodeException.class);

    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  void resetPassword_throwsWhenCodeAlreadyUsed() {
    // Given
    PasswordResetCode usedCode = PasswordResetCode.reconstitute(
        UUID.randomUUID(), userId, VALID_CODE,
        Instant.now().plus(15, ChronoUnit.MINUTES), Instant.now().minus(1, ChronoUnit.MINUTES), Instant.now());

    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    when(passwordHasherPort.matches("tempPass123!", TEMP_HASH)).thenReturn(true);
    when(codeRepositoryPort.findByUserId(userId)).thenReturn(Optional.of(usedCode));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(validCommand()))
        .isInstanceOf(InvalidPasswordResetCodeException.class);

    verify(userRepositoryPort, never()).save(any());
  }
}
