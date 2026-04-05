package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.ResetUserPasswordCommand;
import io.cmartinezs.keygo.app.user.command.UpdateUserCommand;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.exception.InvalidCredentialsException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.exception.UserPasswordResetRequiredException;
import io.cmartinezs.keygo.domain.user.exception.UserSuspendedException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateResetValidateUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String VALID_HASH = "$2a$10$hash";
  private static final String NEW_HASH = "$2a$10$newhash";

  @Mock TenantRepositoryPort tenantRepositoryPort;
  @Mock UserRepositoryPort userRepositoryPort;
  @Mock CredentialEncoderPort credentialEncoderPort;

  private Tenant activeTenant;
  private User activeUser;

  @BeforeEach
  void setUp() {
    activeTenant = Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME").ownerEmail("o@acme.com")
        .status(TenantStatus.ACTIVE).build();

    activeUser = User.builder()
        .id(UserId.generate())
        .tenantId(activeTenant.getId())
        .username(Username.of("johndoe"))
        .email(EmailAddress.of("john@acme.com"))
        .passwordHash(PasswordHash.of(VALID_HASH))
        .firstName("John").lastName("Doe")
        .status(UserStatus.ACTIVE).build();
  }

  // ─── UpdateUserUseCase ────────────────────────────────────────────────────

  @Test
  void updateUserChangesName() {
    // Given
    UpdateUserUseCase uc = new UpdateUserUseCase(tenantRepositoryPort, userRepositoryPort);
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByIdAndTenantId(any(), any())).thenReturn(Optional.of(activeUser));
    when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    User result = uc.execute(new UpdateUserCommand(TENANT_SLUG, activeUser.getId().toString(),
        "Jane", "Smith", null, null, null, null, null, null));

    // Then
    assertThat(result.getFirstName()).isEqualTo("Jane");
    assertThat(result.getLastName()).isEqualTo("Smith");
  }

  @Test
  void updateUserChangesExtendedProfileFields() {
    // Given
    UpdateUserUseCase uc = new UpdateUserUseCase(tenantRepositoryPort, userRepositoryPort);
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByIdAndTenantId(any(), any())).thenReturn(Optional.of(activeUser));
    when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    User result = uc.execute(new UpdateUserCommand(TENANT_SLUG, activeUser.getId().toString(),
        null, null, "+521234567890", "es-MX", "America/Mexico_City",
        "https://example.com/pic.jpg", "1990-01-15", "https://johndoe.dev"));

    // Then
    assertThat(result.getPhoneNumber()).isEqualTo("+521234567890");
    assertThat(result.getLocale()).isEqualTo("es-MX");
    assertThat(result.getZoneinfo()).isEqualTo("America/Mexico_City");
    assertThat(result.getProfilePictureUrl()).isEqualTo("https://example.com/pic.jpg");
    assertThat(result.getBirthdate()).isEqualTo("1990-01-15");
    assertThat(result.getWebsite()).isEqualTo("https://johndoe.dev");
  }

  @Test
  void updateUserThrowsTenantNotFound() {
    // Given
    UpdateUserUseCase uc = new UpdateUserUseCase(tenantRepositoryPort, userRepositoryPort);
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.empty());
    String userId = UUID.randomUUID().toString();
    UpdateUserCommand command = new UpdateUserCommand(TENANT_SLUG, userId, "X", "Y",
        null, null, null, null, null, null);

    // When / Then
    assertThatThrownBy(() -> uc.execute(command))
        .isInstanceOf(TenantNotFoundException.class);
  }

  @Test
  void updateUserThrowsUserNotFound() {
    // Given
    UpdateUserUseCase uc = new UpdateUserUseCase(tenantRepositoryPort, userRepositoryPort);
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByIdAndTenantId(any(), any())).thenReturn(Optional.empty());
    String userId = UUID.randomUUID().toString();
    UpdateUserCommand command = new UpdateUserCommand(TENANT_SLUG, userId, "X", "Y",
        null, null, null, null, null, null);

    // When / Then
    assertThatThrownBy(() -> uc.execute(command))
        .isInstanceOf(UserNotFoundException.class);
  }

  // ─── ResetUserPasswordUseCase ─────────────────────────────────────────────

  @Test
  void resetPasswordUpdatesHash() {
    // Given
    ResetUserPasswordUseCase uc = new ResetUserPasswordUseCase(tenantRepositoryPort, userRepositoryPort, credentialEncoderPort);
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByIdAndTenantId(any(), any())).thenReturn(Optional.of(activeUser));
    when(credentialEncoderPort.encode("newpass")).thenReturn(NEW_HASH);
    when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    User result = uc.execute(new ResetUserPasswordCommand(TENANT_SLUG, activeUser.getId().toString(), "newpass"));

    // Then
    assertThat(result.getPasswordHash().value()).isEqualTo(NEW_HASH);
  }

  // ─── ValidateUserCredentialsUseCase ───────────────────────────────────────

  @Test
  void validateCredentialsByEmailSucceeds() {
    // Given
    ValidateUserCredentialsUseCase uc = new ValidateUserCredentialsUseCase(tenantRepositoryPort, userRepositoryPort, credentialEncoderPort);
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(activeUser));
    when(credentialEncoderPort.matches("secret", VALID_HASH)).thenReturn(true);

    // When
    User result = uc.execute(TENANT_SLUG, "john@acme.com", "secret");

    // Then
    assertThat(result.getUsername().value()).isEqualTo("johndoe");
  }

  @Test
  void validateCredentialsByUsernameSucceeds() {
    // Given
    ValidateUserCredentialsUseCase uc = new ValidateUserCredentialsUseCase(tenantRepositoryPort, userRepositoryPort, credentialEncoderPort);
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    // "johndoe" is not a valid email → tryFindByEmail catches IAE and returns empty without calling mock
    when(userRepositoryPort.findByTenantIdAndUsername(any(), any())).thenReturn(Optional.of(activeUser));
    when(credentialEncoderPort.matches("secret", VALID_HASH)).thenReturn(true);

    // When
    User result = uc.execute(TENANT_SLUG, "johndoe", "secret");

    // Then
    assertThat(result.getEmail().value()).isEqualTo("john@acme.com");
  }

  @Test
  void validateCredentialsThrowsWhenSuspended() {
    // Given
    ValidateUserCredentialsUseCase uc = new ValidateUserCredentialsUseCase(tenantRepositoryPort, userRepositoryPort, credentialEncoderPort);
    activeUser.suspend();
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(activeUser));

    // When / Then
    assertThatThrownBy(() -> uc.execute(TENANT_SLUG, "john@acme.com", "secret"))
        .isInstanceOf(UserSuspendedException.class);
  }

  @Test
  void validateCredentialsThrowsOnWrongPassword() {
    // Given
    ValidateUserCredentialsUseCase uc = new ValidateUserCredentialsUseCase(tenantRepositoryPort, userRepositoryPort, credentialEncoderPort);
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(activeUser));
    when(credentialEncoderPort.matches(any(), any())).thenReturn(false);

    // When / Then
    assertThatThrownBy(() -> uc.execute(TENANT_SLUG, "john@acme.com", "wrong"))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void validateCredentialsThrowsWhenResetPasswordRequired() {
    // Given — T-103: user has RESET_PASSWORD status and provides correct password
    ValidateUserCredentialsUseCase uc = new ValidateUserCredentialsUseCase(tenantRepositoryPort, userRepositoryPort, credentialEncoderPort);
    activeUser.requirePasswordReset();  // transitions status to RESET_PASSWORD
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(activeUser));
    when(credentialEncoderPort.matches("secret", VALID_HASH)).thenReturn(true);  // password is correct

    // When / Then — must throw after password validation (not before, to prevent timing oracle)
    assertThatThrownBy(() -> uc.execute(TENANT_SLUG, "john@acme.com", "secret"))
        .isInstanceOf(UserPasswordResetRequiredException.class);
  }

  @Test
  void validateCredentialsWithResetPasswordStatus_stillThrowsInvalidCredentialsOnWrongPassword() {
    // Given — T-103: timing oracle guard — wrong password must still yield InvalidCredentialsException
    ValidateUserCredentialsUseCase uc = new ValidateUserCredentialsUseCase(tenantRepositoryPort, userRepositoryPort, credentialEncoderPort);
    activeUser.requirePasswordReset();
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(activeUser));
    when(credentialEncoderPort.matches("wrong", VALID_HASH)).thenReturn(false);

    // When / Then — must NOT reveal RESET_PASSWORD status when password is wrong
    assertThatThrownBy(() -> uc.execute(TENANT_SLUG, "john@acme.com", "wrong"))
        .isInstanceOf(InvalidCredentialsException.class)
        .isNotInstanceOf(UserPasswordResetRequiredException.class);
  }

  @Test
  void validateCredentialsThrowsWhenUserNotFound() {
    // Given
    ValidateUserCredentialsUseCase uc = new ValidateUserCredentialsUseCase(tenantRepositoryPort, userRepositoryPort, credentialEncoderPort);
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    // Use a valid email so the email-lookup mock is actually called
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.empty());

    // When / Then — credential is a valid email so email path is tried; no username fallback occurs
    assertThatThrownBy(() -> uc.execute(TENANT_SLUG, "nobody@acme.com", "secret"))
        .isInstanceOf(UserNotFoundException.class);
  }
}



