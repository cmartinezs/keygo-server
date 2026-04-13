package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.membership.port.PlatformRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.result.GetPlatformRolesCatalogResult;
import io.cmartinezs.keygo.domain.membership.model.PlatformRole;
import java.util.Comparator;
import java.util.List;

/**
 * Use case: list platform roles available for assignment in the platform catalog.
 * <p>Caso de uso: listar los roles de plataforma disponibles para asignacion.
 */
public class GetPlatformRolesCatalogUseCase {

  private final PlatformRoleRepositoryPort platformRoleRepositoryPort;

  public GetPlatformRolesCatalogUseCase(PlatformRoleRepositoryPort platformRoleRepositoryPort) {
    this.platformRoleRepositoryPort = platformRoleRepositoryPort;
  }

  public List<GetPlatformRolesCatalogResult> execute() {
    return platformRoleRepositoryPort.findAll().stream()
        .sorted(Comparator.comparing(PlatformRole::getCode, String.CASE_INSENSITIVE_ORDER))
        .map(
            role ->
                new GetPlatformRolesCatalogResult(
                    role.getId().value(),
                    role.getCode(),
                    role.getName(),
                    role.getDescription()))
        .toList();
  }
}
