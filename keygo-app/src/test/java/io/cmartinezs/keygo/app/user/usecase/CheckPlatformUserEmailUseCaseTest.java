package io.cmartinezs.keygo.app.user.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.user.command.CheckPlatformUserEmailCommand;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CheckPlatformUserEmailUseCaseTest {

  @Mock private PlatformUserRepositoryPort platformUserRepositoryPort;

  @InjectMocks private CheckPlatformUserEmailUseCase useCase;

  @Test
  void execute_whenEmailExists_returnsTrue() {
    when(platformUserRepositoryPort.existsByEmail(any(EmailAddress.class))).thenReturn(true);

    boolean result = useCase.execute(new CheckPlatformUserEmailCommand("admin@keygo.local"));

    assertThat(result).isTrue();
    verify(platformUserRepositoryPort).existsByEmail(EmailAddress.of("admin@keygo.local"));
  }

  @Test
  void execute_whenEmailDoesNotExist_returnsFalse() {
    when(platformUserRepositoryPort.existsByEmail(any(EmailAddress.class))).thenReturn(false);

    boolean result = useCase.execute(new CheckPlatformUserEmailCommand("missing@keygo.local"));

    assertThat(result).isFalse();
    verify(platformUserRepositoryPort).existsByEmail(EmailAddress.of("missing@keygo.local"));
  }
}
