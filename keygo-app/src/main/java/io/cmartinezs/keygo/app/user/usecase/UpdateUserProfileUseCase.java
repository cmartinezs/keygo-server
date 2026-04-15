package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.UpdateUserProfileCommand;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.UserProfileResult;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.UserId;

import java.util.UUID;

/**
 * Caso de uso: actualizar el perfil propio del usuario autenticado (self-service).
 *
 * <p>Recibe el {@code userId} (UUID del usuario, claim {@code sub}) ya extraído del
 * SecurityContext por el controller. La verificación del JWT la realiza
 * {@code BootstrapAdminKeyFilter} antes de que llegue el request.
 * Solo actualiza los campos no-nulos del comando — semántica PATCH.
 *
 * <p>Usado por: {@code PATCH /api/v1/tenants/{slug}/account/profile}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class UpdateUserProfileUseCase {

  private final UserRepositoryPort userRepository;
  private final TenantRepositoryPort tenantRepository;

  public UpdateUserProfileUseCase(
      UserRepositoryPort userRepository,
      TenantRepositoryPort tenantRepository) {
    this.userRepository = userRepository;
    this.tenantRepository = tenantRepository;
  }

  /**
   * Ejecuta la actualización del perfil propio del usuario.
   *
   * @param command parámetros del comando (tenantSlug + userId + campos de perfil)
   * @return perfil actualizado del usuario
   */
  public UserProfileResult execute(UpdateUserProfileCommand command) {
    var tenant = tenantRepository.findBySlug(new TenantSlug(command.tenantSlug()))
        .orElseThrow(() -> new UserNotFoundException("tenantSlug", command.tenantSlug()));

    UUID userId;
    try {
      userId = UUID.fromString(command.userId());
    } catch (IllegalArgumentException e) {
      throw new UserNotFoundException("id", command.userId());
    }

    var user = userRepository.findByIdAndTenantId(new UserId(userId), tenant.getId())
        .orElseThrow(() -> new UserNotFoundException("id", command.userId()));

    user.updateProfile(
        command.firstName(),
        command.lastName(),
        command.phoneNumber(),
        command.locale(),
        command.zoneinfo(),
        command.profilePictureUrl(),
        command.birthdate(),
        command.website());

    var updated = userRepository.save(user);

    return new UserProfileResult(
        updated.getId().value().toString(),
        updated.getTenantId().value().toString(),
        updated.getUsername() != null ? updated.getUsername().value() : null,
        updated.getEmail() != null ? updated.getEmail().value() : null,
        updated.getFirstName(),
        updated.getLastName(),
        updated.getStatus() != null ? updated.getStatus().name() : null,
        updated.getPhoneNumber(),
        updated.getLocale(),
        updated.getZoneinfo(),
        updated.getProfilePictureUrl(),
        updated.getBirthdate(),
        updated.getWebsite());
  }
}
