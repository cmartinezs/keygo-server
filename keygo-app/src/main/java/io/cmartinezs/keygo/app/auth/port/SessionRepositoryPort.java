package io.cmartinezs.keygo.app.auth.port;

import io.cmartinezs.keygo.domain.auth.model.Session;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.util.List;
import java.util.Optional;

/**
 * Puerto OUT: persistencia de sesiones de usuario.
 */
public interface SessionRepositoryPort {

  /**
   * Persiste una sesión nueva.
   *
   * @param session sesión a guardar
   * @return la sesión guardada
   */
  Session save(Session session);

  /**
   * Busca una sesión por su ID.
   *
   * @param sessionId ID de la sesión
   * @return la sesión si existe
   */
  Optional<Session> findById(SessionId sessionId);

  /**
   * Actualiza una sesión existente.
   *
   * @param session sesión con datos actualizados
   */
  void update(Session session);

  /**
   * Devuelve todas las sesiones de un usuario en un tenant (sin filtro de estado).
   *
   * @param userId   ID del usuario
   * @param tenantId ID del tenant
   * @return lista de sesiones, ordenada por lastAccessedAt DESC
   */
  List<Session> findAllByUserIdAndTenantId(UserId userId, TenantId tenantId);
}

