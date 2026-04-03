package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.ResetPasswordCommand;
import io.cmartinezs.keygo.app.user.exception.IncorrectCurrentPasswordException;
import io.cmartinezs.keygo.app.user.exception.UserNotInResetPasswordStatusException;
import io.cmartinezs.keygo.app.user.port.PasswordHasherPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

  private static final String TENANT_SLUG = "acme";
  private static final String USER_EMAIL = "john@acme.com";
  private static final String TEMP_HASH = "$2a$10$temphash";
  private static final String NEW_HASH = "$2a$10$newhash";
  private static final String VALID_NEW_PASSWORD = "NewSecure@2026!";

  @Mock TenantRepositoryPort tenantRepositoryPort;
  @Mock UserRepositoryPort userRepositoryPort;
  @Mock PasswordHasherPort passwordHasherPort;

  private ResetPasswordUseCase useCase;
  private Tenant activeTenant;
  private User resetPasswordUser;

  @BeforeEach
  void setUp() {
    useCase = new ResetPasswordUseCase(tenantRepositoryPort, userRepositoryPort, passwordHasherPort);

    activeTenant = Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME").ownerEmail("o@acme.com")
        .status(TenantStatus.ACTIVE).build();

    resetPasswordUser = User.builder()
        .id(UserId.generate())
        .tenantId(activeTenant.getId())
        .username(Username.of("johndoe"))
        .email(EmailAddress.of(USER_EMAIL))
        .passwordHash(PasswordHash.of(TEMP_HASH))
        .firstName("John").lastName("Doe")
        .status(UserStatus.ACTIVE).build();
    resetPasswordUser.requirePasswordReset();  // set to RESET_PASSWORD status
  }

  @Test
  void resetPassword_succeeds_andActivatesUser() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    when(passwordHasherPort.matches("tempPass123!", TEMP_HASH)).thenReturn(true);
    when(passwordHasherPort.hash(VALID_NEW_PASSWORD)).thenReturn(NEW_HASH);
    when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    var result = useCase.execute(new ResetPasswordCommand(TENANT_SLUG, USER_EMAIL, "tempPass123!", VALID_NEW_PASSWORD));

    // Then
    assertThat(result.reset()).isTrue();
    assertThat(resetPasswordUser.getPasswordHash().value()).isEqualTo(NEW_HASH);
    assertThat(resetPasswordUser.isResetPassword()).isFalse();
    verify(userRepositoryPort).save(resetPasswordUser);
  }

  @Test
  void resetPassword_throwsWhenUserNotInResetPasswordStatus() {
    // Given — user is ACTIVE, not RESET_PASSWORD
    resetPasswordUser.activate();  // back to ACTIVE
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(resetPasswordUser));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        new ResetPasswordCommand(TENANT_SLUG, USER_EMAIL, "tempPass123!", VALID_NEW_PASSWORD)))
        .isInstanceOf(UserNotInResetPasswordStatusException.class);

    verify(passwordHasherPort, never()).matches(any(), any());
    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  void resetPassword_throwsWhenTemporaryPasswordIncorrect() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    when(passwordHasherPort.matches("wrongTemp!", TEMP_HASH)).thenReturn(false);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        new ResetPasswordCommand(TENANT_SLUG, USER_EMAIL, "wrongTemp!", VALID_NEW_PASSWORD)))
        .isInstanceOf(IncorrectCurrentPasswordException.class);

    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  void resetPassword_throwsWhenNewPasswordViolatesPolicy() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(resetPasswordUser));
    when(passwordHasherPort.matches("tempPass123!", TEMP_HASH)).thenReturn(true);

    // When / Then — "weak" violates policy
    assertThatThrownBy(() -> useCase.execute(
        new ResetPasswordCommand(TENANT_SLUG, USER_EMAIL, "tempPass123!", "weak")))
        .isInstanceOf(IllegalArgumentException.class);

    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  void resetPassword_throwsWhenTenantNotFound() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        new ResetPasswordCommand(TENANT_SLUG, USER_EMAIL, "tempPass123!", VALID_NEW_PASSWORD)))
        .isInstanceOf(TenantNotFoundException.class);
  }

  @Test
  void resetPassword_throwsWhenUserNotFound() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        new ResetPasswordCommand(TENANT_SLUG, USER_EMAIL, "tempPass123!", VALID_NEW_PASSWORD)))
        .isInstanceOf(UserNotFoundException.class);
  }
}
