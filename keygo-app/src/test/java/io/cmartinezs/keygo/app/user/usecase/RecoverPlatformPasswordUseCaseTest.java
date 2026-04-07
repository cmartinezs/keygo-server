package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.exception.VerificationCodeAlreadyUsedException;
import io.cmartinezs.keygo.domain.user.exception.VerificationCodeExpiredException;
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
class RecoverPlatformPasswordUseCaseTest {

  @Mock PlatformUserRepositoryPort platformUserRepository;
  @Mock VerificationCodeRepositoryPort codeRepository;
  @Mock CredentialEncoderPort credentialEncoder;

  private RecoverPlatformPasswordUseCase useCase;
  private PlatformUser activeUser;
  private UserId userId;

  @BeforeEach
  void setUp() {
    useCase = new RecoverPlatformPasswordUseCase(
        platformUserRepository, codeRepository, credentialEncoder);

    userId = UserId.of(UUID.randomUUID());
    activeUser = PlatformUser.builder()
        .id(userId).username(Username.of("keygo_admin"))
        .email(EmailAddress.of("admin@keygo.local"))
        .passwordHash(PasswordHash.of("$2a$10$oldhash"))
        .firstName("Admin").lastName("KeyGo")
        .status(UserStatus.ACTIVE).build();
  }

  @Test
  void execute_recoversPasswordSuccessfully() {
    // Given
    String token = UUID.randomUUID().toString().replace("-", "");
    var code = VerificationCode.create(
        userId, VerificationPurpose.PASSWORD_RECOVERY, token,
        Instant.now().plus(30, ChronoUnit.MINUTES));

    when(codeRepository.findByCodeAndPurpose(token, VerificationPurpose.PASSWORD_RECOVERY))
        .thenReturn(Optional.of(code));
    when(platformUserRepository.findById(userId)).thenReturn(Optional.of(activeUser));
    when(credentialEncoder.encode("NewPasswrd123!")).thenReturn("$2a$10$newhash");
    when(platformUserRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    var result = useCase.execute(token, "NewPasswrd123!");

    // Then
    assertThat(result.recovered()).isTrue();
    verify(codeRepository).markUsed(code);
    verify(platformUserRepository).save(activeUser);
  }

  @Test
  void execute_throwsWhenTokenNotFound() {
    // Given
    when(codeRepository.findByCodeAndPurpose(any(), any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute("invalid-token", "NewPass123!"))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void execute_throwsWhenTokenExpired() {
    // Given
    String token = "expired-token";
    var code = VerificationCode.create(
        userId, VerificationPurpose.PASSWORD_RECOVERY, token,
        Instant.now().minus(1, ChronoUnit.HOURS));

    when(codeRepository.findByCodeAndPurpose(token, VerificationPurpose.PASSWORD_RECOVERY))
        .thenReturn(Optional.of(code));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(token, "NewPass123!"))
        .isInstanceOf(VerificationCodeExpiredException.class);
  }

  @Test
  void execute_throwsWhenTokenAlreadyUsed() {
    // Given
    String token = "used-token";
    var code = VerificationCode.create(
        userId, VerificationPurpose.PASSWORD_RECOVERY, token,
        Instant.now().plus(30, ChronoUnit.MINUTES));
    code.markUsed();

    when(codeRepository.findByCodeAndPurpose(token, VerificationPurpose.PASSWORD_RECOVERY))
        .thenReturn(Optional.of(code));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(token, "NewPass123!"))
        .isInstanceOf(VerificationCodeAlreadyUsedException.class);
  }
}
