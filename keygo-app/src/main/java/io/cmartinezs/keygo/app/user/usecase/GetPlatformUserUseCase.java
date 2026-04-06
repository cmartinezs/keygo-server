package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;

/**
 * Use case: retrieve a global platform user by ID.
 * <p>Caso de uso: obtener un usuario global de la plataforma por ID.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class GetPlatformUserUseCase {

  private final PlatformUserRepositoryPort platformUserRepositoryPort;

  public GetPlatformUserUseCase(PlatformUserRepositoryPort platformUserRepositoryPort) {
    this.platformUserRepositoryPort = platformUserRepositoryPort;
  }

  /**
   * Execute the use case.
   *
   * @param userId the platform user ID
   * @return the PlatformUser
   * @throws UserNotFoundException if no user exists with the given ID
   */
  public PlatformUser execute(UserId userId) {
    return platformUserRepositoryPort.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("id", userId.value().toString()));
  }
}
