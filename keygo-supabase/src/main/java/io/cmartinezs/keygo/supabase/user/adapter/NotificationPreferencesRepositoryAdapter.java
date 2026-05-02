package io.cmartinezs.keygo.supabase.user.adapter;

import io.cmartinezs.keygo.app.user.port.NotificationPreferencesRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.NotificationPreferences;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.supabase.user.entity.UserNotificationPreferencesEntity;
import io.cmartinezs.keygo.supabase.user.repository.NotificationPreferencesJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.PlatformUserJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador: implementación de {@link NotificationPreferencesRepositoryPort} usando JPA.
 */
@Component
public class NotificationPreferencesRepositoryAdapter
    implements NotificationPreferencesRepositoryPort {

  private final NotificationPreferencesJpaRepository jpaRepository;
  private final TenantUserJpaRepository tenantUserJpaRepository;
  private final PlatformUserJpaRepository platformUserJpaRepository;

  public NotificationPreferencesRepositoryAdapter(
      NotificationPreferencesJpaRepository jpaRepository,
      TenantUserJpaRepository tenantUserJpaRepository,
      PlatformUserJpaRepository platformUserJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.tenantUserJpaRepository = tenantUserJpaRepository;
    this.platformUserJpaRepository = platformUserJpaRepository;
  }

  @Override
  public Optional<NotificationPreferences> findByUserIdAndTenantId(
      UserId userId, TenantId tenantId) {
    var platformUserId = resolvePlatformUserId(userId);
    return jpaRepository.findByPlatformUser_Id(platformUserId)
        .map(entity -> toDomain(entity, userId, tenantId));
  }

  @Override
  public NotificationPreferences saveOrUpdate(NotificationPreferences preferences) {
    var platformUserId = resolvePlatformUserId(preferences.getUserId());
    var existing = jpaRepository.findByPlatformUser_Id(platformUserId);

    UserNotificationPreferencesEntity entity;
    if (existing.isPresent()) {
      entity = existing.get();
    } else {
      entity = new UserNotificationPreferencesEntity();
      entity.setPlatformUser(platformUserJpaRepository.getReferenceById(platformUserId));
    }

    entity.setSecurityAlertsEmail(preferences.isSecurityAlertsEmail());
    entity.setSecurityAlertsInApp(preferences.isSecurityAlertsInApp());
    entity.setBillingAlertsEmail(preferences.isBillingAlertsEmail());
    entity.setProductUpdatesEmail(preferences.isProductUpdatesEmail());
    entity.setWeeklyDigest(preferences.isWeeklyDigest());

    UserNotificationPreferencesEntity saved = jpaRepository.save(entity);
    return toDomain(saved, preferences.getUserId(), preferences.getTenantId());
  }

  private NotificationPreferences toDomain(
      UserNotificationPreferencesEntity entity, UserId userId, TenantId tenantId) {
    return NotificationPreferences.reconstitute(
        userId,
        tenantId,
        entity.isSecurityAlertsEmail(),
        entity.isSecurityAlertsInApp(),
        entity.isBillingAlertsEmail(),
        entity.isProductUpdatesEmail(),
        entity.isWeeklyDigest());
  }

  private java.util.UUID resolvePlatformUserId(UserId userId) {
    return tenantUserJpaRepository
        .findById(userId.value())
        .map(tenantUser -> tenantUser.getPlatformUser().getId())
        .orElse(userId.value());
  }
}
