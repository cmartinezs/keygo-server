package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import java.util.List;
import java.util.UUID;

/**
 * Use case: load the roles assigned to a membership (for display in API responses).
 * <p>Caso de uso: cargar los roles asignados a una membresía (para mostrar en respuestas de API).
 * @author cmartinezs
 * @version 1.0
 */
public class GetMembershipRolesUseCase {

  private final AppRoleRepositoryPort appRoleRepositoryPort;

  public GetMembershipRolesUseCase(AppRoleRepositoryPort appRoleRepositoryPort) {
    this.appRoleRepositoryPort = appRoleRepositoryPort;
  }

  public List<AppRole> execute(UUID membershipId) {
    return appRoleRepositoryPort.findByMembershipId(membershipId);
  }
}
