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
    String tenantId = UUID.randomUUID().toString();
    String clientAppId = UUID.randomUUID().toString();
    String userId = UUID.randomUUID().toString();

    var command = new OpenSessionCommand(tenantId, clientAppId, userId, expiresAt, now, "agent", "1.2.3.4");

    Session savedSession = Session.open(
        new io.cmartinezs.keygo.domain.tenant.model.TenantId(UUID.fromString(tenantId)),
        new io.cmartinezs.keygo.domain.clientapp.model.ClientAppId(UUID.fromString(clientAppId)),
        new io.cmartinezs.keygo.domain.user.model.UserId(UUID.fromString(userId)),
        expiresAt, now, "agent", "1.2.3.4");

    when(sessionRepository.save(any(Session.class))).thenReturn(savedSession);

    // When
    OpenSessionResult result = useCase.execute(command);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.sessionId()).isNotNull();
    verify(sessionRepository).save(any(Session.class));
  }
}

