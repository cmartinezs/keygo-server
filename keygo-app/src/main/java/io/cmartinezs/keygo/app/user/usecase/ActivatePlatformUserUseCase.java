package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;

/**
 * Use case: activate a previously suspended or pending global platform user.
 * <p>Caso de uso: activar un usuario global de la plataforma previamente suspendido o pendiente.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class ActivatePlatformUserUseCase {

  private final PlatformUserRepositoryPort platformUserRepositoryPort;

  public ActivatePlatformUserUseCase(PlatformUserRepositoryPort platformUserRepositoryPort) {
    this.platformUserRepositoryPort = platformUserRepositoryPort;
  }

  /**
   * Activate the platform user identified by the given ID.
   *
   * @param userId the platform user ID
   * @return the activated PlatformUser
   * @throws UserNotFoundException if no user exists with the given ID
   */
  public PlatformUser execute(UserId userId) {
    PlatformUser user = platformUserRepositoryPort.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("id", userId.value().toString()));

    user.activate();

    return platformUserRepositoryPort.save(user);
  }
}
