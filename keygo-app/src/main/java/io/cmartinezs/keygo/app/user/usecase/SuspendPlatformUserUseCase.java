package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;

/**
 * Use case: suspend a global platform user.
 * <p>Caso de uso: suspender un usuario global de la plataforma.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class SuspendPlatformUserUseCase {

  private final PlatformUserRepositoryPort platformUserRepositoryPort;

  public SuspendPlatformUserUseCase(PlatformUserRepositoryPort platformUserRepositoryPort) {
    this.platformUserRepositoryPort = platformUserRepositoryPort;
  }

  /**
   * Suspend the platform user identified by the given ID.
   *
   * @param userId the platform user ID
   * @return the suspended PlatformUser
   * @throws UserNotFoundException if no user exists with the given ID
   */
  public PlatformUser execute(UserId userId) {
    PlatformUser user = platformUserRepositoryPort.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("id", userId.value().toString()));

    user.suspend();

    return platformUserRepositoryPort.save(user);
  }
}
