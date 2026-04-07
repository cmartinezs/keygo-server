package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.user.exception.IncorrectCurrentPasswordException;
import io.cmartinezs.keygo.app.user.exception.PasswordMismatchException;
import io.cmartinezs.keygo.app.user.exception.UserNotInResetPasswordStatusException;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.domain.user.exception.PasswordResetRequestNotFoundException;
import io.cmartinezs.keygo.domain.user.exception.VerificationCodeExpiredException;
import io.cmartinezs.keygo.domain.user.exception.VerificationCodeInvalidException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import io.cmartinezs.keygo.domain.user.model.VerificationCode;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResetPlatformPasswordUseCaseTest {

  @Mock PlatformUserRepositoryPort platformUserRepository;
  @Mock CredentialEncoderPort credentialEncoder;
  @Mock VerificationCodeRepositoryPort codeRepository;

  private ResetPlatformPasswordUseCase useCase;
  private PlatformUser resetUser;
  private UserId userId;
  private UUID requestId;

  @BeforeEach
  void setUp() {
    useCase = new ResetPlatformPasswordUseCase(
        platformUserRepository, credentialEncoder, codeRepository);

    userId = UserId.of(UUID.randomUUID());
    requestId = UUID.randomUUID();
    resetUser = PlatformUser.builder()
        .id(userId).username(Username.of("keygo_admin"))
        .email(EmailAddress.of("admin@keygo.local"))
        .passwordHash(PasswordHash.of("$2a$10$temphash"))
        .firstName("Admin").lastName("KeyGo")
        .status(UserStatus.RESET_PASSWORD).build();
  }

  @Test
  void execute_resetsPasswordSuccessfully() {
    // Given
    String verificationCode = "123456";
    var code = VerificationCode.reconstitute(
        requestId, userId, VerificationPurpose.PASSWORD_RESET,
        verificationCode, Instant.now().plus(15, ChronoUnit.MINUTES), null, Instant.now());

    when(codeRepository.findById(requestId)).thenReturn(Optional.of(code));
    when(platformUserRepository.findById(userId)).thenReturn(Optional.of(resetUser));
    when(credentialEncoder.matches("TempPass123!", "$2a$10$temphash")).thenReturn(true);
    when(credentialEncoder.encode("NewSecure123!")).thenReturn("$2a$10$newhash");
    when(platformUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    var result = useCase.execute(
        requestId.toString(), "TempPass123!", "NewSecure123!", "NewSecure123!", verificationCode);

    // Then
    assertThat(result.reset()).isTrue();
    verify(codeRepository).markUsed(code);
    verify(platformUserRepository).save(resetUser);
  }

  @Test
  void execute_throwsWhenRequestIdNotFound() {
    // Given
    when(codeRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        requestId.toString(), "temp", "new", "new", "123456"))
        .isInstanceOf(PasswordResetRequestNotFoundException.class);
  }

  @Test
  void execute_throwsWhenInvalidRequestIdFormat() {
    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        "not-a-uuid", "temp", "new", "new", "123456"))
        .isInstanceOf(PasswordResetRequestNotFoundException.class);
  }

  @Test
  void execute_throwsWhenVerificationCodeExpired() {
    // Given
    var code = VerificationCode.reconstitute(
        requestId, userId, VerificationPurpose.PASSWORD_RESET,
        "123456", Instant.now().minus(1, ChronoUnit.HOURS), null, Instant.now());

    when(codeRepository.findById(requestId)).thenReturn(Optional.of(code));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        requestId.toString(), "temp", "new", "new", "123456"))
        .isInstanceOf(VerificationCodeExpiredException.class);
  }

  @Test
  void execute_throwsWhenVerificationCodeDoesNotMatch() {
    // Given
    var code = VerificationCode.reconstitute(
        requestId, userId, VerificationPurpose.PASSWORD_RESET,
        "123456", Instant.now().plus(15, ChronoUnit.MINUTES), null, Instant.now());

    when(codeRepository.findById(requestId)).thenReturn(Optional.of(code));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        requestId.toString(), "temp", "new", "new", "999999"))
        .isInstanceOf(VerificationCodeInvalidException.class);
  }

  @Test
  void execute_throwsWhenUserNotInResetPasswordStatus() {
    // Given
    var activeUser = PlatformUser.builder()
        .id(userId).username(Username.of("keygo_admin"))
        .email(EmailAddress.of("admin@keygo.local"))
        .passwordHash(PasswordHash.of("$2a$10$hash"))
        .firstName("Admin").lastName("KeyGo")
        .status(UserStatus.ACTIVE).build();

    var code = VerificationCode.reconstitute(
        requestId, userId, VerificationPurpose.PASSWORD_RESET,
        "123456", Instant.now().plus(15, ChronoUnit.MINUTES), null, Instant.now());

    when(codeRepository.findById(requestId)).thenReturn(Optional.of(code));
    when(platformUserRepository.findById(userId)).thenReturn(Optional.of(activeUser));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        requestId.toString(), "temp", "NewPass1!", "NewPass1!", "123456"))
        .isInstanceOf(UserNotInResetPasswordStatusException.class);
  }

  @Test
  void execute_throwsWhenTemporaryPasswordIncorrect() {
    // Given
    var code = VerificationCode.reconstitute(
        requestId, userId, VerificationPurpose.PASSWORD_RESET,
        "123456", Instant.now().plus(15, ChronoUnit.MINUTES), null, Instant.now());

    when(codeRepository.findById(requestId)).thenReturn(Optional.of(code));
    when(platformUserRepository.findById(userId)).thenReturn(Optional.of(resetUser));
    when(credentialEncoder.matches("WrongTemp!", "$2a$10$temphash")).thenReturn(false);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        requestId.toString(), "WrongTemp!", "NewPass1!", "NewPass1!", "123456"))
        .isInstanceOf(IncorrectCurrentPasswordException.class);
  }

  @Test
  void execute_throwsWhenPasswordsDoNotMatch() {
    // Given
    var code = VerificationCode.reconstitute(
        requestId, userId, VerificationPurpose.PASSWORD_RESET,
        "123456", Instant.now().plus(15, ChronoUnit.MINUTES), null, Instant.now());

    when(codeRepository.findById(requestId)).thenReturn(Optional.of(code));
    when(platformUserRepository.findById(userId)).thenReturn(Optional.of(resetUser));
    when(credentialEncoder.matches("TempPass1!", "$2a$10$temphash")).thenReturn(true);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(
        requestId.toString(), "TempPass1!", "NewPass1!", "Different1!", "123456"))
        .isInstanceOf(PasswordMismatchException.class);
  }
}
