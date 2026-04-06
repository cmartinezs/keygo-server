package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.app.auth.command.OpenSessionCommand;
import io.cmartinezs.keygo.app.auth.result.OpenSessionResult;
import io.cmartinezs.keygo.domain.auth.model.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenSessionUseCaseTest {

  @Mock SessionRepositoryPort sessionRepository;

  OpenSessionUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new OpenSessionUseCase(sessionRepository);
  }

  @Test
  void givenValidCommand_whenExecute_thenCreatesAndSavesSession() {
    // Given
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(86400L * 30);
    UUID platformUserId = UUID.randomUUID();
    String clientAppId = UUID.randomUUID().toString();

    var command = new OpenSessionCommand(platformUserId, clientAppId, expiresAt, now, "agent", "1.2.3.4", null);

    Session savedSession = Session.open(
        platformUserId,
        new io.cmartinezs.keygo.domain.clientapp.model.ClientAppId(UUID.fromString(clientAppId)),
        expiresAt, now, "agent", "1.2.3.4", null);

    when(sessionRepository.save(any(Session.class))).thenReturn(savedSession);

    // When
    OpenSessionResult result = useCase.execute(command);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.sessionId()).isNotNull();
    verify(sessionRepository).save(any(Session.class));
  }
}

