package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.app.user.port.VerificationCodeRepositoryPort;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import io.cmartinezs.keygo.domain.user.model.VerificationCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForgotPlatformPasswordUseCaseTest {

  private static final String USER_EMAIL = "admin@keygo.local";

  @Mock PlatformUserRepositoryPort platformUserRepository;
  @Mock VerificationCodeRepositoryPort codeRepository;
  @Mock EmailNotificationPort emailNotification;

  private ForgotPlatformPasswordUseCase useCase;
  private PlatformUser activePlatformUser;

  @BeforeEach
  void setUp() {
    useCase = new ForgotPlatformPasswordUseCase(
        platformUserRepository, codeRepository, emailNotification);

    activePlatformUser = PlatformUser.builder()
        .id(UserId.of(UUID.randomUUID()))
        .username(Username.of("keygo_admin"))
        .email(EmailAddress.of(USER_EMAIL))
        .passwordHash(PasswordHash.of("$2a$10$hash"))
        .firstName("Admin").lastName("KeyGo")
        .status(UserStatus.ACTIVE).build();
  }

  @Test
  void execute_sendsEmailWhenUserExists() {
    // Given
    when(platformUserRepository.findByEmail(any())).thenReturn(Optional.of(activePlatformUser));
    when(codeRepository.upsert(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    var result = useCase.execute(USER_EMAIL);

    // Then
    assertThat(result.sent()).isTrue();
    verify(codeRepository).upsert(any(VerificationCode.class));
    verify(emailNotification).sendEmail(
        eq(EmailNotificationPort.TYPE_PASSWORD_RECOVERY), anyString(), anyString(), any(Map.class));
  }

  @Test
  void execute_returnsSentTrueWhenEmailNotFound_antiEnumeration() {
    // Given
    when(platformUserRepository.findByEmail(any())).thenReturn(Optional.empty());

    // When
    var result = useCase.execute("unknown@keygo.local");

    // Then
    assertThat(result.sent()).isTrue();
    verify(codeRepository, never()).upsert(any());
    verify(emailNotification, never()).sendEmail(any(), any(), any(), any());
  }
}
