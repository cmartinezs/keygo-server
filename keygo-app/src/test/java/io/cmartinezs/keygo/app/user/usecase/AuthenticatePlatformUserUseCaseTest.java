package io.cmartinezs.keygo.app.user.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.user.command.AuthenticatePlatformUserCommand;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.user.exception.InvalidCredentialsException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.exception.UserPasswordResetRequiredException;
import io.cmartinezs.keygo.domain.user.exception.UserPendingVerificationException;
import io.cmartinezs.keygo.domain.user.exception.UserSuspendedException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticatePlatformUserUseCaseTest {

  private static final String EMAIL = "test@platform.local";
  private static final String RAW_PASSWORD = "SecurePass123!";
  private static final String HASHED_PASSWORD = "$2a$10$hashedValue";
  private static final String USERNAME = "platform_user";

  @Mock private PlatformUserRepositoryPort platformUserRepositoryPort;
  @Mock private CredentialEncoderPort credentialEncoderPort;

  @InjectMocks private AuthenticatePlatformUserUseCase useCase;

  @Test
  void execute_happyPath_returnsAuthenticatedUser() {
    // Given
    AuthenticatePlatformUserCommand command =
        new AuthenticatePlatformUserCommand(EMAIL, RAW_PASSWORD);

    PlatformUser activeUser = buildUser(UserStatus.ACTIVE);

    when(platformUserRepositoryPort.findByEmail(any(EmailAddress.class)))
        .thenReturn(Optional.of(activeUser));
    when(credentialEncoderPort.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);

    // When
    PlatformUser result = useCase.execute(command);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getEmail().value()).isEqualTo(EMAIL);
    assertThat(result.isActive()).isTrue();
  }

  @Test
  void execute_userNotFound_throwsUserNotFoundException() {
    // Given
    AuthenticatePlatformUserCommand command =
        new AuthenticatePlatformUserCommand(EMAIL, RAW_PASSWORD);

    when(platformUserRepositoryPort.findByEmail(any(EmailAddress.class)))
        .thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining(EMAIL);
  }

  @Test
  void execute_wrongPassword_throwsInvalidCredentialsException() {
    // Given
    AuthenticatePlatformUserCommand command =
        new AuthenticatePlatformUserCommand(EMAIL, "wrong-password!!");

    PlatformUser activeUser = buildUser(UserStatus.ACTIVE);

    when(platformUserRepositoryPort.findByEmail(any(EmailAddress.class)))
        .thenReturn(Optional.of(activeUser));
    when(credentialEncoderPort.matches("wrong-password!!", HASHED_PASSWORD)).thenReturn(false);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  @Test
  void execute_suspendedUser_throwsUserSuspendedException() {
    // Given
    AuthenticatePlatformUserCommand command =
        new AuthenticatePlatformUserCommand(EMAIL, RAW_PASSWORD);

    PlatformUser suspendedUser = buildUser(UserStatus.SUSPENDED);

    when(platformUserRepositoryPort.findByEmail(any(EmailAddress.class)))
        .thenReturn(Optional.of(suspendedUser));
    when(credentialEncoderPort.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(UserSuspendedException.class)
        .hasMessageContaining(USERNAME);
  }

  @Test
  void execute_pendingUser_throwsUserPendingVerificationException() {
    // Given
    AuthenticatePlatformUserCommand command =
        new AuthenticatePlatformUserCommand(EMAIL, RAW_PASSWORD);

    PlatformUser pendingUser = buildUser(UserStatus.PENDING);

    when(platformUserRepositoryPort.findByEmail(any(EmailAddress.class)))
        .thenReturn(Optional.of(pendingUser));
    when(credentialEncoderPort.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(UserPendingVerificationException.class)
        .hasMessageContaining(EMAIL);
  }

  @Test
  void execute_resetPasswordUser_throwsUserPasswordResetRequiredException() {
    // Given
    AuthenticatePlatformUserCommand command =
        new AuthenticatePlatformUserCommand(EMAIL, RAW_PASSWORD);

    PlatformUser resetUser = buildUser(UserStatus.RESET_PASSWORD);

    when(platformUserRepositoryPort.findByEmail(any(EmailAddress.class)))
        .thenReturn(Optional.of(resetUser));
    when(credentialEncoderPort.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(UserPasswordResetRequiredException.class)
        .hasMessageContaining(USERNAME);
  }

  private PlatformUser buildUser(UserStatus status) {
    return PlatformUser.builder()
        .id(UserId.generate())
        .username(Username.of(USERNAME))
        .email(EmailAddress.of(EMAIL))
        .passwordHash(PasswordHash.of(HASHED_PASSWORD))
        .status(status)
        .build();
  }
}
