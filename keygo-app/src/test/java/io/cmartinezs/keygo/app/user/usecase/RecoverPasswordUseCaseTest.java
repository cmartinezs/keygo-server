package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.RecoverPasswordCommand;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.exception.InvalidPasswordException;
import io.cmartinezs.keygo.domain.user.exception.VerificationCodeAlreadyUsedException;
import io.cmartinezs.keygo.domain.user.exception.VerificationCodeExpiredException;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecoverPasswordUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String RAW_TOKEN = "abc123def456abc123def456abc12300";
  private static final String VALID_NEW_PASSWORD = "NewSecure@2026!";
  private static final String NEW_HASH = "$2a$10$newhash";

  @Mock TenantRepositoryPort tenantRepositoryPort;
  @Mock UserRepositoryPort userRepositoryPort;
  @Mock VerificationCodeRepositoryPort tokenRepositoryPort;
  @Mock CredentialEncoderPort credentialEncoderPort;

  private RecoverPasswordUseCase useCase;
  private Tenant activeTenant;
  private User activeUser;
  private VerificationCode validToken;

  @BeforeEach
  void setUp() {
    useCase = new RecoverPasswordUseCase(
        tenantRepositoryPort, userRepositoryPort, tokenRepositoryPort, credentialEncoderPort);

    activeTenant = Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME")
        .status(TenantStatus.ACTIVE).build();

    UserId userId = UserId.generate();
    activeUser = User.builder()
        .id(userId)
        .tenantId(activeTenant.getId())
        .username(Username.of("johndoe"))
        .email(EmailAddress.of("john@acme.com"))
        .passwordHash(PasswordHash.of("$2a$10$oldhash"))
        .firstName("John").lastName("Doe")
        .status(UserStatus.ACTIVE).build();

    validToken = VerificationCode.create(
        userId, VerificationPurpose.PASSWORD_RECOVERY, RAW_TOKEN,
        Instant.now().plus(30, ChronoUnit.MINUTES));
  }

  @Test
  void recoverPassword_succeeds() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(tokenRepositoryPort.findByCodeAndPurpose(RAW_TOKEN, VerificationPurpose.PASSWORD_RECOVERY)).thenReturn(Optional.of(validToken));
    when(userRepositoryPort.findByTenantIdAndPlatformUserId(any(), any())).thenReturn(Optional.of(activeUser));
    when(credentialEncoderPort.encode(VALID_NEW_PASSWORD)).thenReturn(NEW_HASH);
    when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    var result = useCase.execute(new RecoverPasswordCommand(TENANT_SLUG, RAW_TOKEN, VALID_NEW_PASSWORD));

    // Then
    assertThat(result.recovered()).isTrue();
    assertThat(activeUser.getPasswordHash().value()).isEqualTo(NEW_HASH);
    verify(tokenRepositoryPort).markUsed(validToken);
    verify(userRepositoryPort).save(activeUser);
  }

  @Test
  void recoverPassword_throwsWhenTokenNotFound() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(tokenRepositoryPort.findByCodeAndPurpose(any(), any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        new RecoverPasswordCommand(TENANT_SLUG, "unknowntoken", VALID_NEW_PASSWORD)))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void recoverPassword_throwsWhenTokenExpired() {
    // Given — token expired 1 minute ago
    VerificationCode expiredToken = VerificationCode.create(
        activeUser.getId(), VerificationPurpose.PASSWORD_RECOVERY, RAW_TOKEN,
        Instant.now().minus(1, ChronoUnit.MINUTES));

    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(tokenRepositoryPort.findByCodeAndPurpose(RAW_TOKEN, VerificationPurpose.PASSWORD_RECOVERY)).thenReturn(Optional.of(expiredToken));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        new RecoverPasswordCommand(TENANT_SLUG, RAW_TOKEN, VALID_NEW_PASSWORD)))
        .isInstanceOf(VerificationCodeExpiredException.class);

    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  void recoverPassword_throwsWhenTokenAlreadyUsed() {
    // Given — token already used (usedAt not null)
    VerificationCode usedToken = VerificationCode.reconstitute(
        UUID.randomUUID(), activeUser.getId(), VerificationPurpose.PASSWORD_RECOVERY, RAW_TOKEN,
        Instant.now().plus(30, ChronoUnit.MINUTES),
        Instant.now().minus(5, ChronoUnit.MINUTES),  // usedAt set
        Instant.now().minus(10, ChronoUnit.MINUTES));

    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(tokenRepositoryPort.findByCodeAndPurpose(RAW_TOKEN, VerificationPurpose.PASSWORD_RECOVERY)).thenReturn(Optional.of(usedToken));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        new RecoverPasswordCommand(TENANT_SLUG, RAW_TOKEN, VALID_NEW_PASSWORD)))
        .isInstanceOf(VerificationCodeAlreadyUsedException.class);

    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  void recoverPassword_throwsWhenNewPasswordViolatesPolicy() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(tokenRepositoryPort.findByCodeAndPurpose(RAW_TOKEN, VerificationPurpose.PASSWORD_RECOVERY)).thenReturn(Optional.of(validToken));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        new RecoverPasswordCommand(TENANT_SLUG, RAW_TOKEN, "weak")))
        .isInstanceOf(InvalidPasswordException.class);

    verify(userRepositoryPort, never()).save(any());
  }

  @Test
  void recoverPassword_activatesUserIfPending() {
    // Given — pending user (email not verified yet, using recovery flow)
    activeUser.suspend();  // make it non-active first
    activeUser = User.builder()
        .id(activeUser.getId())
        .tenantId(activeTenant.getId())
        .username(Username.of("johndoe"))
        .email(EmailAddress.of("john@acme.com"))
        .passwordHash(PasswordHash.of("$2a$10$oldhash"))
        .firstName("John").lastName("Doe")
        .status(UserStatus.PENDING).build();

    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(tokenRepositoryPort.findByCodeAndPurpose(RAW_TOKEN, VerificationPurpose.PASSWORD_RECOVERY)).thenReturn(Optional.of(validToken));
    when(userRepositoryPort.findByTenantIdAndPlatformUserId(any(), any())).thenReturn(Optional.of(activeUser));
    when(credentialEncoderPort.encode(VALID_NEW_PASSWORD)).thenReturn(NEW_HASH);
    when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(new RecoverPasswordCommand(TENANT_SLUG, RAW_TOKEN, VALID_NEW_PASSWORD));

    // Then — pending user must be activated after recovery
    assertThat(activeUser.isActive()).isTrue();
  }
}
