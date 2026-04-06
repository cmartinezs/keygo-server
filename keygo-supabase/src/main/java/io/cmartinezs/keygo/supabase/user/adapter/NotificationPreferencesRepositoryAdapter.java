package io.cmartinezs.keygo.supabase.user.adapter;

import io.cmartinezs.keygo.app.user.port.NotificationPreferencesRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.NotificationPreferences;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.supabase.tenant.repository.TenantJpaRepository;
import io.cmartinezs.keygo.supabase.user.entity.UserNotificationPreferencesEntity;
import io.cmartinezs.keygo.supabase.user.repository.NotificationPreferencesJpaRepository;
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
  private final TenantJpaRepository tenantJpaRepository;

  public NotificationPreferencesRepositoryAdapter(
      NotificationPreferencesJpaRepository jpaRepository,
      TenantUserJpaRepository tenantUserJpaRepository,
      TenantJpaRepository tenantJpaRepository) {
    this.jpaRepository = jpaRepository;
    this.tenantUserJpaRepository = tenantUserJpaRepository;
    this.tenantJpaRepository = tenantJpaRepository;
  }

  @Override
  public Optional<NotificationPreferences> findByUserIdAndTenantId(
      UserId userId, TenantId tenantId) {
    return jpaRepository
        .findByUser_IdAndTenant_Id(userId.value(), tenantId.value())
        .map(this::toDomain);
  }

  @Override
  public NotificationPreferences saveOrUpdate(NotificationPreferences preferences) {
    var existing = jpaRepository
        .findByUser_IdAndTenant_Id(
            preferences.getUserId().value(),
            preferences.getTenantId().value());

    UserNotificationPreferencesEntity entity;
    if (existing.isPresent()) {
      entity = existing.get();
    } else {
      entity = new UserNotificationPreferencesEntity();
      // Usar proxy JPA (sin SELECT adicional) para establecer las FKs
      entity.setUser(tenantUserJpaRepository.getReferenceById(preferences.getUserId().value()));
      entity.setTenant(tenantJpaRepository.getReferenceById(preferences.getTenantId().value()));
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
        new UserId(entity.getUser().getId()),
        new TenantId(entity.getTenant().getId()),
        entity.isSecurityAlertsEmail(),
        entity.isSecurityAlertsInApp(),
        entity.isBillingAlertsEmail(),
        entity.isProductUpdatesEmail(),
        entity.isWeeklyDigest());
  }
}
