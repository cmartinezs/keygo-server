package io.cmartinezs.keygo.supabase.user.repository;

import io.cmartinezs.keygo.supabase.user.entity.UserNotificationPreferencesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para la tabla {@code user_notification_preferences}.
 */
public interface NotificationPreferencesJpaRepository
    extends JpaRepository<UserNotificationPreferencesEntity, UUID> {

  /**
   * Busca las preferencias globales de notificación del platform user.
   */
  Optional<UserNotificationPreferencesEntity> findByPlatformUser_Id(UUID platformUserId);
}
