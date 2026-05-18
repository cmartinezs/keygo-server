package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.SessionRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.app.user.result.SessionInfoResult;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.util.List;
import java.util.UUID;

/**
 * Caso de uso admin: listar sesiones de un usuario en un tenant.
 * Aislamiento garantizado: solo devuelve sesiones del usuario en ese tenant específico.
 */
public class ListAdminUserSessionsUseCase {

  private final TenantRepositoryPort tenantRepositoryPort;
  private final UserRepositoryPort userRepositoryPort;
  private final SessionRepositoryPort sessionRepositoryPort;

  public ListAdminUserSessionsUseCase(
      TenantRepositoryPort tenantRepositoryPort,
      UserRepositoryPort userRepositoryPort,
      SessionRepositoryPort sessionRepositoryPort) {
    this.tenantRepositoryPort = tenantRepositoryPort;
    this.userRepositoryPort = userRepositoryPort;
    this.sessionRepositoryPort = sessionRepositoryPort;
  }

  public List<SessionInfoResult> execute(String tenantSlug, String userId) {
    var tenant = tenantRepositoryPort.findBySlug(TenantSlug.of(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));

    var user = userRepositoryPort.findByIdAndTenantId(UserId.of(userId), tenant.getId())
        .orElseThrow(() -> new UserNotFoundException("id", userId));

    UUID tenantUserId = user.getId().value();
    UUID tenantId = tenant.getId().value();

    return sessionRepositoryPort.findAllByTenantUserIdAndTenantId(tenantUserId, tenantId)
        .stream()
        .map(s -> new SessionInfoResult(
            s.getId().value(),
            s.getStatus().getValue(),
            s.getUserAgent(),
            s.getIpAddress(),
            s.getCreatedAt(),
            s.getLastAccessedAt(),
            s.getExpiresAt(),
            false))
        .toList();
  }
}
