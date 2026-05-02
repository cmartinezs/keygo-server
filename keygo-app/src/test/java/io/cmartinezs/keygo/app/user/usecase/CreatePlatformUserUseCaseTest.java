package io.cmartinezs.keygo.app.user.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.membership.command.AssignPlatformRoleCommand;
import io.cmartinezs.keygo.app.membership.usecase.AssignPlatformRoleUseCase;
import io.cmartinezs.keygo.app.user.command.CreatePlatformUserCommand;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.user.exception.DuplicateUserException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreatePlatformUserUseCaseTest {

  private static final String EMAIL = "test@platform.local";
  private static final String USERNAME = "platform_user";
  private static final String RAW_PASSWORD = "SecurePass123!";
  private static final String HASHED_PASSWORD = "$2a$10$hashedValue";

  @Mock private PlatformUserRepositoryPort platformUserRepositoryPort;
  @Mock private CredentialEncoderPort credentialEncoderPort;
  @Mock private AssignPlatformRoleUseCase assignPlatformRoleUseCase;

  @InjectMocks private CreatePlatformUserUseCase useCase;

  @Test
  void execute_happyPath_createsUserAndAssignsDefaultRole() {
    // Given
    CreatePlatformUserCommand command =
        new CreatePlatformUserCommand(USERNAME, EMAIL, RAW_PASSWORD, "John", "Doe");

    when(platformUserRepositoryPort.existsByEmail(any(EmailAddress.class))).thenReturn(false);
    when(platformUserRepositoryPort.existsByUsername(any(Username.class))).thenReturn(false);
    when(credentialEncoderPort.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
    when(platformUserRepositoryPort.save(any(PlatformUser.class)))
        .thenAnswer(invocation -> {
          PlatformUser input = invocation.getArgument(0);
          return PlatformUser.builder()
              .id(UserId.of(UUID.randomUUID()))
              .username(input.getUsername())
              .email(input.getEmail())
              .passwordHash(input.getPasswordHash())
              .firstName(input.getFirstName())
              .lastName(input.getLastName())
              .status(input.getStatus())
              .build();
        });

    // When
    PlatformUser result = useCase.execute(command);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUsername().value()).isEqualTo(USERNAME);
    assertThat(result.getEmail().value()).isEqualTo(EMAIL);
    assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(result.getFirstName()).isEqualTo("John");
    assertThat(result.getLastName()).isEqualTo("Doe");

    verify(credentialEncoderPort).encode(RAW_PASSWORD);
    verify(platformUserRepositoryPort).save(any(PlatformUser.class));
    verify(assignPlatformRoleUseCase).execute(any(AssignPlatformRoleCommand.class));
  }

  @Test
  void execute_duplicateEmail_throwsDuplicateUserException() {
    // Given
    CreatePlatformUserCommand command =
        new CreatePlatformUserCommand(USERNAME, EMAIL, RAW_PASSWORD, null, null);

    when(platformUserRepositoryPort.existsByEmail(any(EmailAddress.class))).thenReturn(true);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(DuplicateUserException.class)
        .hasMessageContaining("email")
        .hasMessageContaining(EMAIL);

    verify(platformUserRepositoryPort, never()).save(any());
    verify(assignPlatformRoleUseCase, never()).execute(any());
  }

  @Test
  void execute_duplicateUsername_throwsDuplicateUserException() {
    // Given
    CreatePlatformUserCommand command =
        new CreatePlatformUserCommand(USERNAME, EMAIL, RAW_PASSWORD, null, null);

    when(platformUserRepositoryPort.existsByEmail(any(EmailAddress.class))).thenReturn(false);
    when(platformUserRepositoryPort.existsByUsername(any(Username.class))).thenReturn(true);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(DuplicateUserException.class)
        .hasMessageContaining("username")
        .hasMessageContaining(USERNAME);

    verify(platformUserRepositoryPort, never()).save(any());
    verify(assignPlatformRoleUseCase, never()).execute(any());
  }
}
