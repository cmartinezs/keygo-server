package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.user.command.GetPlatformUserProfileCommand;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.UserProfileResult;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.UserId;

import java.util.UUID;

/**
 * Caso de uso: obtener perfil completo del usuario de plataforma autenticado (self-service).
 *
 * <p>Recibe el {@code userId} (UUID del platform user, claim {@code sub}) ya extraído del
 * SecurityContext por el controller. La verificación del JWT la realiza
 * {@code BootstrapAdminKeyFilter} antes de que llegue el request.
 * A diferencia de {@link GetUserProfileUseCase}, no resuelve ningún tenant.
 *
 * <p>Usado por: {@code GET /api/v1/platform/account/profile}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class GetPlatformUserProfileUseCase {

  private final PlatformUserRepositoryPort platformUserRepository;

  public GetPlatformUserProfileUseCase(PlatformUserRepositoryPort platformUserRepository) {
    this.platformUserRepository = platformUserRepository;
  }

  /**
   * Ejecuta la obtención del perfil propio del usuario de plataforma.
   *
   * @param command parámetros del comando (userId)
   * @return perfil completo del platform user autenticado
   */
  public UserProfileResult execute(GetPlatformUserProfileCommand command) {
    UUID userId;
    try {
      userId = UUID.fromString(command.userId());
    } catch (IllegalArgumentException e) {
      throw new UserNotFoundException("id", command.userId());
    }

    var user = platformUserRepository.findById(new UserId(userId))
        .orElseThrow(() -> new UserNotFoundException("id", command.userId()));

    return new UserProfileResult(
        user.getId().value().toString(),
        null,
        user.getUsername() != null ? user.getUsername().value() : null,
        user.getEmail() != null ? user.getEmail().value() : null,
        user.getFirstName(),
        user.getLastName(),
        user.getStatus() != null ? user.getStatus().name() : null,
        user.getPhoneNumber(),
        user.getLocale(),
        user.getZoneinfo(),
        user.getProfilePictureUrl(),
        null,
        null);
  }
}
