package io.cmartinezs.keygo.supabase.user.adapter;

import io.cmartinezs.keygo.app.user.port.NotificationPreferencesRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.NotificationPreferences;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.supabase.user.entity.UserNotificationPreferencesEntity;
import io.cmartinezs.keygo.supabase.user.repository.NotificationPreferencesJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador: implementación de {@link NotificationPreferencesRepositoryPort} usando JPA.
 */
@Component
public class NotificationPreferencesRepositoryAdapter
    implements NotificationPreferencesRepositoryPort {

  private final NotificationPreferencesJpaRepository jpaRepository;

  public NotificationPreferencesRepositoryAdapter(
      NotificationPreferencesJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<NotificationPreferences> findByUserIdAndTenantId(
      UserId userId, TenantId tenantId) {
    return jpaRepository
        .findByUserIdAndTenantId(userId.value(), tenantId.value())
        .map(this::toDomain);
  }

  @Override
  public NotificationPreferences saveOrUpdate(NotificationPreferences preferences) {
    var existing = jpaRepository
        .findByUserIdAndTenantId(preferences.getUserId().value(), preferences.getTenantId().value());

    UserNotificationPreferencesEntity entity;
    if (existing.isPresent()) {
      entity = existing.get();
    } else {
      entity = new UserNotificationPreferencesEntity();
      entity.setUserId(preferences.getUserId().value());
      entity.setTenantId(preferences.getTenantId().value());
    }

    entity.setSecurityAlertsEmail(preferences.isSecurityAlertsEmail());
    entity.setSecurityAlertsInApp(preferences.isSecurityAlertsInApp());
    entity.setBillingAlertsEmail(preferences.isBillingAlertsEmail());
    entity.setProductUpdatesEmail(preferences.isProductUpdatesEmail());
    entity.setWeeklyDigest(preferences.isWeeklyDigest());

    UserNotificationPreferencesEntity saved = jpaRepository.save(entity);
    return toDomain(saved);
  }

  private NotificationPreferences toDomain(UserNotificationPreferencesEntity entity) {
    return NotificationPreferences.reconstitute(
        new UserId(entity.getUserId()),
        new TenantId(entity.getTenantId()),
        entity.isSecurityAlertsEmail(),
        entity.isSecurityAlertsInApp(),
        entity.isBillingAlertsEmail(),
        entity.isProductUpdatesEmail(),
        entity.isWeeklyDigest());
  }
}
