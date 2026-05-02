package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.command.OpenSessionCommand;
import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.app.auth.result.OpenSessionResult;
import io.cmartinezs.keygo.domain.auth.model.Session;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import java.util.UUID;

/**
 * Caso de uso: abrir una nueva sesión tras el canje exitoso de un authorization code
 * o un login de plataforma.
 *
 * <p>Crea una {@link Session} en estado ACTIVE con el platform_user y (opcionalmente) la app indicada.
 *
 * <p>Modelo restructurado (RFC restructure-multitenant):
 * <ul>
 *   <li>{@code platformUserId} — identidad global (nullable para MVP)
 *   <li>{@code clientAppId} — null para sesiones de plataforma, no-null para tenant app sessions
 * </ul>
 */
public class OpenSessionUseCase {

  private final SessionRepositoryPort sessionRepository;

  public OpenSessionUseCase(SessionRepositoryPort sessionRepository) {
    this.sessionRepository = sessionRepository;
  }

  /**
   * Abre una nueva sesión.
   *
   * @param command parámetros de la sesión a crear
   * @return resultado con el ID de la sesión creada
   */
  public OpenSessionResult execute(OpenSessionCommand command) {
    ClientAppId clientAppId = command.clientAppId() != null
        ? new ClientAppId(UUID.fromString(command.clientAppId()))
        : null;

    var session = Session.open(
        command.platformUserId(),
        clientAppId,
        command.expiresAt(),
        command.now(),
        command.userAgent(),
        command.ipAddress(),
        command.signingKeyId());

    var saved = sessionRepository.save(session);
    return new OpenSessionResult(saved.getId().value().toString());
  }
}
