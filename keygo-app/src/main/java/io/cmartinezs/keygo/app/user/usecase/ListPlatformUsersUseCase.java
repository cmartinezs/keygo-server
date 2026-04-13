package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.user.filter.PlatformUserFilter;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;

/**
 * Use case: list global platform users with pagination, filtering, and sorting.
 * <p>Caso de uso: listar usuarios globales de plataforma con paginacion, filtrado y ordenamiento.
 */
public class ListPlatformUsersUseCase {

  private final PlatformUserRepositoryPort platformUserRepositoryPort;

  public ListPlatformUsersUseCase(PlatformUserRepositoryPort platformUserRepositoryPort) {
    this.platformUserRepositoryPort = platformUserRepositoryPort;
  }

  public PagedResult<PlatformUser> execute(PlatformUserFilter filter) {
    return platformUserRepositoryPort.findAllPaged(filter);
  }
}
