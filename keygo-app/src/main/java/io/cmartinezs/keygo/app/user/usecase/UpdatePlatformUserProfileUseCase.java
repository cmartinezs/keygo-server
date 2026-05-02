package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.user.command.UpdatePlatformUserProfileCommand;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.UserProfileResult;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.UserId;

import java.util.UUID;

/**
 * Caso de uso: actualizar el perfil propio del usuario de plataforma autenticado (self-service).
 *
 * <p>Recibe el {@code userId} (UUID del platform user, claim {@code sub}) ya extraído del
 * SecurityContext por el controller. La verificación del JWT la realiza
 * {@code BootstrapAdminKeyFilter} antes de que llegue el request.
 * Solo actualiza los campos no-nulos del comando — semántica PATCH.
 *
 * <p>Usado por: {@code PATCH /api/v1/platform/account/profile}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class UpdatePlatformUserProfileUseCase {

  private final PlatformUserRepositoryPort platformUserRepository;

  public UpdatePlatformUserProfileUseCase(PlatformUserRepositoryPort platformUserRepository) {
    this.platformUserRepository = platformUserRepository;
  }

  /**
   * Ejecuta la actualización del perfil propio del usuario de plataforma.
   *
   * @param command parámetros del comando (userId + campos de perfil)
   * @return perfil actualizado del platform user
   */
  public UserProfileResult execute(UpdatePlatformUserProfileCommand command) {
    UUID userId;
    try {
      userId = UUID.fromString(command.userId());
    } catch (IllegalArgumentException e) {
      throw new UserNotFoundException("id", command.userId());
    }

    var user = platformUserRepository.findById(new UserId(userId))
        .orElseThrow(() -> new UserNotFoundException("id", command.userId()));

    user.updateProfile(
        command.firstName(),
        command.lastName(),
        command.phoneNumber(),
        command.locale(),
        command.zoneinfo(),
        command.profilePictureUrl());

    var updated = platformUserRepository.save(user);

    return new UserProfileResult(
        updated.getId().value().toString(),
        null,
        updated.getUsername() != null ? updated.getUsername().value() : null,
        updated.getEmail() != null ? updated.getEmail().value() : null,
        updated.getFirstName(),
        updated.getLastName(),
        updated.getStatus() != null ? updated.getStatus().name() : null,
        updated.getPhoneNumber(),
        updated.getLocale(),
        updated.getZoneinfo(),
        updated.getProfilePictureUrl(),
        null,
        null);
  }
}
