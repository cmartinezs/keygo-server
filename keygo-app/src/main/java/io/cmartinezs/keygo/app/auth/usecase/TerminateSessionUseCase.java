package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.port.RefreshTokenRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import java.util.UUID;

/**
 * Caso de uso: terminar una sesión y revocar todos sus refresh tokens.
 */
public class TerminateSessionUseCase {

  private final SessionRepositoryPort sessionRepository;
  private final RefreshTokenRepositoryPort refreshTokenRepository;

  public TerminateSessionUseCase(
      SessionRepositoryPort sessionRepository,
      RefreshTokenRepositoryPort refreshTokenRepository) {
    this.sessionRepository = sessionRepository;
    this.refreshTokenRepository = refreshTokenRepository;
  }

  /**
   * Termina la sesión identificada por {@code sessionId}, revocando todos sus refresh tokens activos.
   *
   * @param sessionId UUID de la sesión como String
   * @throws IllegalArgumentException si la sesión no existe
   * @throws IllegalStateException    si la sesión no está ACTIVE
   */
  public void execute(String sessionId) {
    var id = SessionId.from(UUID.fromString(sessionId));

    var session = sessionRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

    // Revocar todos los refresh tokens de la sesión antes de terminar
    refreshTokenRepository.revokeAllForSession(id);

    // Terminar sesión (lanza IllegalStateException si ya estaba terminada)
    session.terminate();
    sessionRepository.update(session);
  }
}

