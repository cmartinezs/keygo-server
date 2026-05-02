package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.user.command.CheckPlatformUserEmailCommand;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;

/**
 * Use case: check whether a global platform user exists by email.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class CheckPlatformUserEmailUseCase {

  private final PlatformUserRepositoryPort platformUserRepositoryPort;

  public CheckPlatformUserEmailUseCase(PlatformUserRepositoryPort platformUserRepositoryPort) {
    this.platformUserRepositoryPort = platformUserRepositoryPort;
  }

  public boolean execute(CheckPlatformUserEmailCommand command) {
    return platformUserRepositoryPort.existsByEmail(EmailAddress.of(command.email()));
  }
}
