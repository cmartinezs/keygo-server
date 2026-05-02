package io.cmartinezs.keygo.app.user.port;

import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.NotificationPreferences;
import io.cmartinezs.keygo.domain.user.model.UserId;

import java.util.Optional;

/**
 * Puerto OUT: persistencia de preferencias de notificación del usuario.
 */
public interface NotificationPreferencesRepositoryPort {

  /**
   * Busca las preferencias de un usuario en un tenant.
   *
   * @param userId   ID del usuario
   * @param tenantId ID del tenant
   * @return las preferencias si existen
   */
  Optional<NotificationPreferences> findByUserIdAndTenantId(UserId userId, TenantId tenantId);

  /**
   * Persiste las preferencias (crea o actualiza según existan).
   *
   * @param preferences preferencias a guardar
   * @return las preferencias persistidas
   */
  NotificationPreferences saveOrUpdate(NotificationPreferences preferences);
}
