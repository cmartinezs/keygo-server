package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import java.util.List;
import java.util.UUID;

/**
 * Use case: list and retrieve app roles within a client app.
 * <p>Caso de uso: listar y recuperar roles de app dentro de una app de cliente.
 * @author cmartinezs
 * @version 1.0
 */
public class ListAppRolesUseCase {

  private final AppRoleRepositoryPort appRoleRepositoryPort;

  public ListAppRolesUseCase(AppRoleRepositoryPort appRoleRepositoryPort) {
    this.appRoleRepositoryPort = appRoleRepositoryPort;
  }

  /**
   * List all roles for a given client app.
   * <p>Lista todos los roles de una app de cliente.
   * @param clientAppId the client app ID
   * @return list of roles
   */
  public List<AppRole> execute(UUID clientAppId) {
    return appRoleRepositoryPort.findByClientAppId(clientAppId);
  }
}

