package io.cmartinezs.keygo.app.auth.port;

import io.cmartinezs.keygo.domain.auth.model.Session;
import io.cmartinezs.keygo.domain.auth.model.SessionId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto OUT: persistencia de sesiones de usuario.
 *
 * <p>Modelo restructurado (RFC restructure-multitenant):
 * sessions no tiene tenant_id ni user_id; usa platform_user_id (nullable) y client_app_id (nullable).
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
   * Devuelve todas las sesiones de un platform_user (sin filtro de estado).
   *
   * @param platformUserId UUID del platform_user
   * @return lista de sesiones, ordenada por lastAccessedAt DESC
   */
  List<Session> findAllByPlatformUserId(UUID platformUserId);

  /**
   * Devuelve todas las sesiones de un tenant_user en un tenant específico (sin filtro de estado).
   * Garantiza aislamiento: solo devuelve sesiones del usuario en ese tenant.
   *
   * @param tenantUserId UUID del tenant_user
   * @param tenantId     UUID del tenant
   * @return lista de sesiones, ordenada por lastAccessedAt DESC
   */
  List<Session> findAllByTenantUserIdAndTenantId(UUID tenantUserId, UUID tenantId);
}

