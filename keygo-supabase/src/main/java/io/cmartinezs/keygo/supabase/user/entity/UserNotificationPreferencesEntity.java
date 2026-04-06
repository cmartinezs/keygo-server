package io.cmartinezs.keygo.supabase.user.entity;

import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad JPA: Preferencias de notificación del usuario.
 *
 * <p>Mapea la tabla {@code user_notification_preferences} de la base de datos.
 * Un registro por par (user_id, tenant_id) — restricción UNIQUE en la tabla.
 */
@Entity
@Table(name = "user_notification_preferences")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationPreferencesEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  /** Relación con el usuario tenant propietario de las preferencias. FK: user_id → tenant_users(id) */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private TenantUserEntity user;

  /** Relación con el tenant al que pertenece el registro. FK: tenant_id → tenants(id) */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "tenant_id", nullable = false)
  private TenantEntity tenant;

  @Column(name = "security_alerts_email", nullable = false)
  private boolean securityAlertsEmail;

  @Column(name = "security_alerts_in_app", nullable = false)
  private boolean securityAlertsInApp;

  @Column(name = "billing_alerts_email", nullable = false)
  private boolean billingAlertsEmail;

  @Column(name = "product_updates_email", nullable = false)
  private boolean productUpdatesEmail;

  @Column(name = "weekly_digest", nullable = false)
  private boolean weeklyDigest;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
