package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.command.OpenSessionCommand;
import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.app.auth.result.OpenSessionResult;
import io.cmartinezs.keygo.domain.auth.model.Session;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.util.UUID;

/**
 * Caso de uso: abrir una nueva sesión de usuario tras el canje exitoso de un authorization code.
 *
 * <p>Crea una {@link Session} en estado ACTIVE con el tenant, app y usuario indicados.
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
    var session = Session.open(
        new TenantId(UUID.fromString(command.tenantId())),
        new ClientAppId(UUID.fromString(command.clientAppId())),
        new UserId(UUID.fromString(command.userId())),
        command.expiresAt(),
        command.now(),
        command.userAgent(),
        command.ipAddress(),
        command.signingKeyId());   // nuevo campo — UUID de la clave firmante

    var saved = sessionRepository.save(session);
    return new OpenSessionResult(saved.getId().value().toString());
  }
}
