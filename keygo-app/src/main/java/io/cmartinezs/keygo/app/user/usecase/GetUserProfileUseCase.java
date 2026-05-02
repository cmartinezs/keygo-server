package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.GetUserProfileCommand;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.UserProfileResult;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.UserId;

import java.util.UUID;

/**
 * Caso de uso: obtener perfil completo del usuario autenticado (self-service).
 *
 * <p>Recibe el {@code userId} (UUID del usuario, claim {@code sub}) ya extraído del
 * SecurityContext por el controller. La verificación del JWT la realiza
 * {@code BootstrapAdminKeyFilter} antes de que llegue el request.
 *
 * <p>Usado por: {@code GET /api/v1/tenants/{slug}/account/profile}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class GetUserProfileUseCase {

  private final UserRepositoryPort userRepository;
  private final TenantRepositoryPort tenantRepository;

  public GetUserProfileUseCase(
      UserRepositoryPort userRepository,
      TenantRepositoryPort tenantRepository) {
    this.userRepository = userRepository;
    this.tenantRepository = tenantRepository;
  }

  /**
   * Ejecuta la obtención del perfil propio del usuario.
   *
   * @param command parámetros del comando (tenantSlug + userId)
   * @return perfil completo del usuario autenticado
   */
  public UserProfileResult execute(GetUserProfileCommand command) {
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

    return new UserProfileResult(
        user.getId().value().toString(),
        user.getTenantId().value().toString(),
        user.getUsername() != null ? user.getUsername().value() : null,
        user.getEmail() != null ? user.getEmail().value() : null,
        user.getFirstName(),
        user.getLastName(),
        user.getStatus() != null ? user.getStatus().name() : null,
        user.getPhoneNumber(),
        user.getLocale(),
        user.getZoneinfo(),
        user.getProfilePictureUrl(),
        user.getBirthdate(),
        user.getWebsite());
  }
}
